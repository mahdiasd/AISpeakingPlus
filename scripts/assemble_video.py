import subprocess
import os
import sys

FFMPEG = "/private/tmp/ffmpeg-tool/node_modules/ffmpeg-static/ffmpeg"
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCENES_DIR = os.path.join(BASE_DIR, "generated_scenes")
AUDIO_DIR = os.path.join(SCENES_DIR, "audio")
PROCESSED_DIR = os.path.join(SCENES_DIR, "processed")
OUTPUT_FILE = os.path.join(BASE_DIR, "AiSpeaking_Intro_Video_Master.mp4")

os.makedirs(PROCESSED_DIR, exist_ok=True)

# Scene configurations:
# (index, original_video_duration, target_duration, audio_start_delay_ms)
SCENE_CONFIGS = [
    {
        "index": 1,
        "video": os.path.join(SCENES_DIR, "scene_1.mp4"),
        "audio": os.path.join(AUDIO_DIR, "user_voice_1.mp3"),
        "target_dur": 10.4,
        "delay_ms": 300,
        "fade_in": True,
        "fade_out": False
    },
    {
        "index": 2,
        "video": os.path.join(SCENES_DIR, "scene_2.mp4"),
        "audio": os.path.join(AUDIO_DIR, "user_voice_2.mp3"),
        "target_dur": 9.2,
        "delay_ms": 300,
        "fade_in": False,
        "fade_out": False
    },
    {
        "index": 3,
        "video": os.path.join(SCENES_DIR, "scene_3.mp4"),
        "audio": os.path.join(AUDIO_DIR, "user_voice_3.mp3"),
        "target_dur": 13.4,
        "delay_ms": 300,
        "fade_in": False,
        "fade_out": False
    },
    {
        "index": 4,
        "video": os.path.join(SCENES_DIR, "scene_4.mp4"),
        "audio": os.path.join(AUDIO_DIR, "user_voice_4.mp3"),
        "target_dur": 16.4,
        "delay_ms": 300,
        "fade_in": False,
        "fade_out": False
    },
    {
        "index": 5,
        "video": os.path.join(SCENES_DIR, "scene_5.mp4"),
        "audio": os.path.join(AUDIO_DIR, "user_voice_5.mp3"),
        "target_dur": 11.5,
        "delay_ms": 300,
        "fade_in": False,
        "fade_out": True
    }
]

def render_scene(cfg):
    idx = cfg["index"]
    out_scene = os.path.join(PROCESSED_DIR, f"scene_{idx}_processed.mp4")
    target_dur = cfg["target_dur"]
    delay_ms = cfg["delay_ms"]
    
    # Original duration is ~10.03s for all clips
    orig_dur = 10.03 if idx != 5 else 10.01
    pts_factor = target_dur / orig_dur
    
    print(f"\n>>> Processing Scene {idx}: Target Duration = {target_dur}s (Speed Factor: {pts_factor:.3f}x)")
    
    # Video filters:
    # 1. Slow down / speed adjust with minterpolate blend
    # 2. Fade in on scene 1, fade out on scene 5
    vfilters = []
    if abs(pts_factor - 1.0) > 0.01:
        vfilters.append(f"setpts={pts_factor:.4f}*PTS")
        vfilters.append("minterpolate=fps=24:mi_mode=blend")
    else:
        vfilters.append("fps=24")
        
    if cfg.get("fade_in"):
        vfilters.append("fade=t=in:st=0:d=0.5")
    if cfg.get("fade_out"):
        vfilters.append(f"fade=t=out:st={target_dur - 0.8:.2f}:d=0.8")
        
    vf_str = ",".join(vfilters)
    
    # Audio filters:
    # Delay audio by delay_ms, pad with silence up to target_dur, gentle fade out at end
    af_str = f"adelay={delay_ms}|{delay_ms},apad=pad_dur=2.0,afade=t=out:st={target_dur - 0.2:.2f}:d=0.2"
    
    cmd = [
        FFMPEG, "-y",
        "-i", cfg["video"],
        "-i", cfg["audio"],
        "-filter_complex",
        f"[0:v]{vf_str},trim=duration={target_dur}[v];[1:a]{af_str},atrim=duration={target_dur}[a]",
        "-map", "[v]",
        "-map", "[a]",
        "-c:v", "libx264",
        "-preset", "fast",
        "-crf", "18",
        "-c:a", "aac",
        "-b:a", "192k",
        "-ar", "48000",
        out_scene
    ]
    
    subprocess.run(cmd, check=True)
    print(f"✓ Scene {idx} rendered successfully to {out_scene}")
    return out_scene

def stitch_all(scene_files):
    print("\n>>> Stitching all 5 scenes with smooth transitions...")
    # We will use xfade between scenes
    # Scene 1: 10.4s
    # Scene 2: 9.2s
    # Scene 3: 13.4s
    # Scene 4: 16.4s
    # Scene 5: 11.5s
    # Crossfade duration: 0.5s
    
    durs = [cfg["target_dur"] for cfg in SCENE_CONFIGS]
    XFADE_DUR = 0.5
    
    # Calculate offset for each transition:
    # offset 1 = dur0 - xfade
    # offset 2 = offset 1 + dur1 - xfade
    # etc.
    offsets = []
    curr_offset = durs[0] - XFADE_DUR
    offsets.append(curr_offset)
    for i in range(1, len(durs) - 1):
        curr_offset += durs[i] - XFADE_DUR
        offsets.append(curr_offset)
        
    print(f"Xfade transition offsets: {[round(o, 2) for o in offsets]}")
    
    # Build filter_complex string
    inputs = []
    for f in scene_files:
        inputs.extend(["-i", f])
        
    # Video crossfades
    v_lines = []
    # [0:v][1:v]xfade=transition=fade:duration=0.5:offset=offset0[v1];
    # [v1][2:v]xfade=transition=fade:duration=0.5:offset=offset1[v2]; ...
    last_v = "0:v"
    for i in range(len(offsets)):
        next_v = f"{i+1}:v"
        out_v = f"v_x_{i+1}" if i < len(offsets) - 1 else "v_final"
        v_lines.append(f"[{last_v}][{next_v}]xfade=transition=fade:duration={XFADE_DUR}:offset={offsets[i]:.2f}[{out_v}]")
        last_v = out_v
        
    # Audio crossfades
    a_lines = []
    last_a = "0:a"
    for i in range(len(offsets)):
        next_a = f"{i+1}:a"
        out_a = f"a_x_{i+1}" if i < len(offsets) - 1 else "a_final"
        a_lines.append(f"[{last_a}][{next_a}]acrossfade=d={XFADE_DUR}:c1=tri:c2=tri[{out_a}]")
        last_a = out_a
        
    fc = ";".join(v_lines + a_lines)
    
    cmd = [
        FFMPEG, "-y"
    ] + inputs + [
        "-filter_complex", fc,
        "-map", "[v_final]",
        "-map", "[a_final]",
        "-c:v", "libx264",
        "-preset", "medium",
        "-crf", "18",
        "-c:a", "aac",
        "-b:a", "192k",
        "-ar", "48000",
        "-movflags", "+faststart",
        OUTPUT_FILE
    ]
    
    subprocess.run(cmd, check=True)
    print(f"\n🎉 MASTER VIDEO CREATED: {OUTPUT_FILE}")

def main():
    rendered_files = []
    for cfg in SCENE_CONFIGS:
        out = render_scene(cfg)
        rendered_files.append(out)
    stitch_all(rendered_files)

if __name__ == "__main__":
    main()
