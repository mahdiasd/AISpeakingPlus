#!/usr/bin/env python3
"""
Comprehensive Concurrency and Resource Load Testing Suite for AI Speaking Plus
Evaluates STT (SenseVoice) and TTS (Kokoro-82M) resource consumption and latency
as concurrent users scale from 1 to 20+.
"""

import argparse
import asyncio
import json
import os
import re
import subprocess
import sys
import time
import urllib.request
import wave
from dataclasses import asdict, dataclass, field
from typing import Dict, List, Optional, Tuple

import websockets

# Configuration Defaults
DEFAULT_AUDIO_PATH = os.path.join(os.path.dirname(__file__), "test_16k.wav")
DEFAULT_WS_URL = "ws://127.0.0.1:18080/api/v2/stt/live"
DEFAULT_HTTP_URL = "http://127.0.0.1:18080"
DEFAULT_OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "benchmark_results")
SSH_HOST = "ubuntu@193.93.169.198"
APP_CONTAINER = "server-app-1"


@dataclass
class UserResult:
    user_id: int
    success: bool
    error: Optional[str] = None
    server_busy: bool = False
    connect_time: float = 0.0
    stt_stream_time: float = 0.0
    stt_recognition_time: float = 0.0
    stt_total_time: float = 0.0
    stt_transcript: str = ""
    tts_time: float = 0.0
    tts_audio_url: str = ""
    e2e_total_time: float = 0.0


@dataclass
class ResourceSample:
    timestamp: float
    cpu_percent: float
    mem_used_mb: float
    mem_limit_mb: float


class RemoteServerMonitor:
    """Monitors docker stats on the remote host over SSH during a test run."""

    def __init__(self, ssh_host: str = SSH_HOST, container_name: str = APP_CONTAINER):
        self.ssh_host = ssh_host
        self.container = container_name
        self.samples: List[ResourceSample] = []
        self._running = False
        self._task: Optional[asyncio.Task] = None

    def _parse_mem(self, mem_str: str) -> float:
        """Convert '1.164GiB' or '512MiB' to MB."""
        mem_str = mem_str.strip()
        if "GiB" in mem_str:
            val = float(mem_str.replace("GiB", "").strip())
            return val * 1024.0
        elif "MiB" in mem_str:
            return float(mem_str.replace("MiB", "").strip())
        elif "KiB" in mem_str:
            return float(mem_str.replace("KiB", "").strip()) / 1024.0
        elif "B" in mem_str:
            return float(mem_str.replace("B", "").strip()) / (1024.0 * 1024.0)
        try:
            return float(mem_str)
        except ValueError:
            return 0.0

    def query_stats_sync(self) -> Optional[Tuple[float, float, float]]:
        """Run single synchronous docker stats query."""
        cmd = [
            "ssh",
            "-o",
            "ConnectTimeout=3",
            self.ssh_host,
            f"docker stats --no-stream --format '{{{{.CPUPerc}}}}|{{{{.MemUsage}}}}' {self.container}",
        ]
        try:
            res = subprocess.run(cmd, capture_output=True, text=True, timeout=5)
            if res.returncode == 0 and res.stdout.strip():
                line = res.stdout.strip().split("\n")[0]
                parts = line.split("|")
                if len(parts) >= 2:
                    cpu_str = parts[0].replace("%", "").strip()
                    cpu = float(cpu_str) if cpu_str else 0.0
                    mem_parts = parts[1].split("/")
                    used_mb = self._parse_mem(mem_parts[0])
                    limit_mb = self._parse_mem(mem_parts[1]) if len(mem_parts) > 1 else 0.0
                    return cpu, used_mb, limit_mb
        except Exception:
            pass
        return None

    async def _sample_loop(self):
        while self._running:
            t0 = time.time()
            stats = await asyncio.to_thread(self.query_stats_sync)
            if stats:
                cpu, used_mb, limit_mb = stats
                self.samples.append(ResourceSample(time.time(), cpu, used_mb, limit_mb))
            elapsed = time.time() - t0
            sleep_time = max(0.1, 0.8 - elapsed)
            await asyncio.sleep(sleep_time)

    def get_idle_baseline(self) -> Tuple[float, float]:
        """Sample idle CPU and RAM before test begins."""
        samples = []
        for _ in range(3):
            s = self.query_stats_sync()
            if s:
                samples.append(s)
            time.sleep(0.5)
        if samples:
            avg_cpu = sum(s[0] for s in samples) / len(samples)
            avg_mem = sum(s[1] for s in samples) / len(samples)
            return round(avg_cpu, 2), round(avg_mem, 2)
        return 0.0, 0.0

    def start(self, baseline_cpu: float = 0.0, baseline_mem: float = 0.0):
        self.samples.clear()
        self.baseline_cpu = baseline_cpu
        self.baseline_mem = baseline_mem
        self._running = True
        self._task = asyncio.create_task(self._sample_loop())

    async def stop(self) -> Dict[str, float]:
        self._running = False
        if self._task:
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass

        if not self.samples:
            return {
                "baseline_cpu": self.baseline_cpu,
                "peak_cpu": self.baseline_cpu,
                "avg_cpu": self.baseline_cpu,
                "baseline_mem_mb": self.baseline_mem,
                "peak_mem_mb": self.baseline_mem,
                "delta_mem_mb": 0.0,
            }

        cpus = [s.cpu_percent for s in self.samples]
        mems = [s.mem_used_mb for s in self.samples]
        peak_cpu = max(cpus)
        avg_cpu = sum(cpus) / len(cpus)
        peak_mem = max(mems)
        delta_mem = max(0.0, peak_mem - self.baseline_mem)

        return {
            "baseline_cpu": self.baseline_cpu,
            "peak_cpu": round(peak_cpu, 2),
            "avg_cpu": round(avg_cpu, 2),
            "baseline_mem_mb": self.baseline_mem,
            "peak_mem_mb": round(peak_mem, 2),
            "delta_mem_mb": round(delta_mem, 2),
        }


@dataclass
class StepSummary:
    user_count: int
    total_requests: int
    success_count: int
    failed_count: int
    server_busy_count: int
    success_rate_percent: float
    avg_latency_s: float
    p50_latency_s: float
    p95_latency_s: float
    max_latency_s: float
    baseline_cpu: float
    peak_cpu: float
    avg_cpu: float
    delta_cpu_per_user: float
    baseline_mem_mb: float
    peak_mem_mb: float
    delta_mem_mb: float
    delta_mem_per_user_mb: float
    avg_stt_recognition_s: float = 0.0
    avg_tts_s: float = 0.0


class LoadTester:
    def __init__(
        self,
        ws_url: str = DEFAULT_WS_URL,
        http_url: str = DEFAULT_HTTP_URL,
        audio_path: str = DEFAULT_AUDIO_PATH,
        jwt_token: Optional[str] = None,
    ):
        self.ws_url = ws_url
        self.http_url = http_url.rstrip("/")
        self.audio_path = audio_path
        self.jwt_token = jwt_token
        self.pcm_bytes: bytes = b""
        self.audio_duration_s: float = 0.0
        self._load_audio()

    def _load_audio(self):
        with wave.open(self.audio_path, "rb") as w:
            assert w.getnchannels() == 1, "Audio must be mono"
            assert w.getsampwidth() == 2, "Audio must be 16-bit PCM"
            assert w.getframerate() == 16000, "Sample rate must be 16kHz"
            self.pcm_bytes = w.readframes(w.getnframes())
            self.audio_duration_s = w.getnframes() / float(w.getframerate())
        print(
            f"Loaded test audio: {self.audio_path} ({len(self.pcm_bytes)} bytes, {self.audio_duration_s:.2f}s)"
        )

    async def run_single_stt_user(self, user_id: int) -> UserResult:
        """Simulate a single user streaming voice over WebSocket to STT."""
        res = UserResult(user_id=user_id, success=False)
        t_start = time.time()
        chunk_size = 5120  # 160ms chunk @ 16kHz PCM16 (3200 samples * 2 bytes)

        try:
            t0 = time.time()
            async with websockets.connect(self.ws_url, close_timeout=10, ping_timeout=20) as ws:
                ready_raw = await asyncio.wait_for(ws.recv(), timeout=10.0)
                res.connect_time = time.time() - t0

                # Send audio in paced chunks
                t_stream_start = time.time()
                for i in range(0, len(self.pcm_bytes), chunk_size):
                    chunk = self.pcm_bytes[i : i + chunk_size]
                    await ws.send(chunk)
                    # Paced interval: 50ms per chunk (faster than real-time 160ms, realistic for streaming burst)
                    await asyncio.sleep(0.04)

                res.stt_stream_time = time.time() - t_stream_start

                # Signal stop
                t_stop_sent = time.time()
                await ws.send("stop")

                # Receive final transcript
                transcript = ""
                while True:
                    msg = await asyncio.wait_for(ws.recv(), timeout=90.0)
                    data = json.loads(msg)
                    msg_type = data.get("type")
                    if msg_type == "error":
                        if "server_busy" in data.get("message", ""):
                            res.server_busy = True
                        res.error = data.get("message", "unknown_error")
                        return res
                    elif msg_type == "final":
                        transcript = data.get("text", "")
                        break

                t_final = time.time()
                res.stt_recognition_time = t_final - t_stop_sent
                res.stt_total_time = t_final - t_start
                res.stt_transcript = transcript
                res.e2e_total_time = res.stt_total_time
                res.success = True
        except Exception as e:
            res.error = str(e)
            res.stt_total_time = time.time() - t_start
            res.e2e_total_time = res.stt_total_time
        return res

    async def run_single_tts_user(self, user_id: int) -> UserResult:
        """Simulate a single user requesting Kokoro TTS synthesis."""
        res = UserResult(user_id=user_id, success=False)
        t_start = time.time()
        # Include unique sentence text to ensure we test actual ONNX model inference and not disk cache
        unique_text = f"User {user_id} test turn at {time.time():.2f}: The British Museum is very fascinating to visit today."
        payload = json.dumps({"text": unique_text, "voiceId": 3, "speed": 1.0}).encode("utf-8")

        def _do_post():
            req = urllib.request.Request(
                f"{self.http_url}/api/v2/tts/synthesize",
                data=payload,
                headers={"Content-Type": "application/json"},
            )
            with urllib.request.urlopen(req, timeout=180) as resp:
                return json.loads(resp.read().decode("utf-8"))

        try:
            resp_data = await asyncio.to_thread(_do_post)
            t_end = time.time()
            res.tts_time = t_end - t_start
            res.e2e_total_time = res.tts_time
            if resp_data.get("data") and resp_data["data"].get("audioUrl"):
                res.tts_audio_url = resp_data["data"]["audioUrl"]
                res.success = True
            else:
                res.error = resp_data.get("message", "invalid_tts_response")
        except Exception as e:
            res.error = str(e)
            res.tts_time = time.time() - t_start
            res.e2e_total_time = res.tts_time
        return res

    async def run_single_e2e_user(self, user_id: int) -> UserResult:
        """Simulate full session user: STT voice stream -> Stage Chat & AI Turn -> Character TTS Audio."""
        # Step 1: STT
        stt_res = await self.run_single_stt_user(user_id)
        if not stt_res.success:
            return stt_res

        # Step 2: Stage Chat turn with transcribed text
        t_chat_start = time.time()
        user_msg = stt_res.stt_transcript or "Hello, I would like to order dinner please."
        chat_payload = json.dumps({"message": user_msg}).encode("utf-8")
        headers = {"Content-Type": "application/json"}
        if self.jwt_token:
            headers["Authorization"] = f"Bearer {self.jwt_token}"

        def _do_chat():
            req = urllib.request.Request(
                f"{self.http_url}/api/v2/stages/stage-01-inflight-london/chat",
                data=chat_payload,
                headers=headers,
            )
            with urllib.request.urlopen(req, timeout=180) as resp:
                return json.loads(resp.read().decode("utf-8"))

        try:
            chat_data = await asyncio.to_thread(_do_chat)
            t_chat_end = time.time()
            stt_res.tts_time = t_chat_end - t_chat_start
            stt_res.e2e_total_time = stt_res.stt_total_time + stt_res.tts_time
            if chat_data.get("data") and chat_data["data"].get("audioUrl"):
                stt_res.tts_audio_url = chat_data["data"]["audioUrl"]
                stt_res.success = True
            else:
                stt_res.error = "no_audio_url_in_chat_response"
                stt_res.success = False
        except Exception as e:
            stt_res.error = f"chat_error: {e}"
            stt_res.tts_time = time.time() - t_chat_start
            stt_res.e2e_total_time = stt_res.stt_total_time + stt_res.tts_time
            stt_res.success = False

        return stt_res


def calculate_step_summary(
    user_count: int,
    results: List[UserResult],
    res_stats: Dict[str, float],
    mode: str,
) -> StepSummary:
    total = len(results)
    success = sum(1 for r in results if r.success)
    failed = sum(1 for r in results if not r.success)
    server_busy = sum(1 for r in results if r.server_busy)
    rate = (success / total * 100.0) if total > 0 else 0.0

    latencies = [
        (r.stt_total_time if mode == "stt" else (r.tts_time if mode == "tts" else r.e2e_total_time))
        for r in results
        if r.success
    ]
    if not latencies:
        latencies = [r.e2e_total_time for r in results]

    latencies.sort()
    avg_lat = sum(latencies) / len(latencies) if latencies else 0.0
    min_lat = latencies[0] if latencies else 0.0
    p50_lat = latencies[len(latencies) // 2] if latencies else 0.0
    p95_idx = int(len(latencies) * 0.95)
    p95_lat = latencies[min(p95_idx, len(latencies) - 1)] if latencies else 0.0
    max_lat = latencies[-1] if latencies else 0.0

    avg_stt_rec = (
        sum(r.stt_recognition_time for r in results if r.success) / success if success > 0 else 0.0
    )
    avg_tts = sum(r.tts_time for r in results if r.success) / success if success > 0 else 0.0

    delta_cpu_user = round(
        (res_stats["peak_cpu"] - res_stats["baseline_cpu"]) / user_count, 2
    ) if user_count > 0 else 0.0
    delta_mem_user = round(
        res_stats["delta_mem_mb"] / user_count, 2
    ) if user_count > 0 else 0.0

    return StepSummary(
        user_count=user_count,
        total_requests=total,
        success_count=success,
        failed_count=failed,
        server_busy_count=server_busy,
        success_rate_percent=round(rate, 1),
        avg_latency_s=round(avg_lat, 2),
        p50_latency_s=round(p50_lat, 2),
        p95_latency_s=round(p95_lat, 2),
        max_latency_s=round(max_lat, 2),
        baseline_cpu=res_stats["baseline_cpu"],
        peak_cpu=res_stats["peak_cpu"],
        avg_cpu=res_stats["avg_cpu"],
        delta_cpu_per_user=delta_cpu_user,
        baseline_mem_mb=res_stats["baseline_mem_mb"],
        peak_mem_mb=res_stats["peak_mem_mb"],
        delta_mem_mb=res_stats["delta_mem_mb"],
        delta_mem_per_user_mb=delta_mem_user,
        avg_stt_recognition_s=round(avg_stt_rec, 2),
        avg_tts_s=round(avg_tts, 2),
    )


async def run_benchmark_step(
    tester: LoadTester,
    monitor: RemoteServerMonitor,
    user_count: int,
    mode: str,
) -> Tuple[StepSummary, List[UserResult]]:
    print(f"\n{'='*70}")
    print(f"▶ Starting Step: {user_count} Concurrent Users | Mode: {mode.upper()}")
    print(f"{'='*70}")

    # Cooldown before step & sample baseline
    print("Sampling idle baseline for 2s...")
    base_cpu, base_mem = await asyncio.to_thread(monitor.get_idle_baseline)
    print(f"  Current baseline: CPU={base_cpu}%, RAM={base_mem}MB")

    # Start monitor
    monitor.start(baseline_cpu=base_cpu, baseline_mem=base_mem)
    t_start = time.time()

    # Launch users concurrently
    if mode == "stt":
        tasks = [tester.run_single_stt_user(i) for i in range(user_count)]
    elif mode == "tts":
        tasks = [tester.run_single_tts_user(i) for i in range(user_count)]
    else:  # e2e
        tasks = [tester.run_single_e2e_user(i) for i in range(user_count)]

    results: List[UserResult] = await asyncio.gather(*tasks)
    elapsed = time.time() - t_start

    # Small settle window
    await asyncio.sleep(1)
    res_stats = await monitor.stop()

    summary = calculate_step_summary(user_count, results, res_stats, mode)

    print(
        f"✓ Completed in {elapsed:.2f}s | Success: {summary.success_count}/{summary.total_requests} ({summary.success_rate_percent}%)"
    )
    print(
        f"  Latency: avg={summary.avg_latency_s}s, p50={summary.p50_latency_s}s, p95={summary.p95_latency_s}s, max={summary.max_latency_s}s"
    )
    print(
        f"  Server CPU: base={summary.baseline_cpu}%, peak={summary.peak_cpu}%, avg={summary.avg_cpu}% (ΔCPU/user: {summary.delta_cpu_per_user}%)"
    )
    print(
        f"  Server RAM: base={summary.baseline_mem_mb}MB, peak={summary.peak_mem_mb}MB, Δ={summary.delta_mem_mb}MB (ΔRAM/user: {summary.delta_mem_per_user_mb}MB)"
    )

    return summary, results


def format_markdown_table(summaries: List[StepSummary], title: str) -> str:
    md = [f"### {title}\n"]
    md.append(
        "| کاربران همزمان | نرخ موفقیت | تاخیر میانگین (ثانیه) | تاخیر P95 (ثانیه) | پیک مصرف پردازنده (CPU) | پردازنده به ازای کاربر | پیک حافظه رم (RAM) | افزایش رم (Δ RAM) | افزایش رم به ازای کاربر |"
    )
    md.append(
        "|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|"
    )
    for s in summaries:
        md.append(
            f"| **{s.user_count}** | {s.success_rate_percent}% ({s.success_count}/{s.total_requests}) | {s.avg_latency_s}s | {s.p95_latency_s}s | {s.peak_cpu}% | {s.delta_cpu_per_user}% | {s.peak_mem_mb:.1f} MB | +{s.delta_mem_mb:.1f} MB | +{s.delta_mem_per_user_mb:.1f} MB |"
        )
    md.append("\n")
    return "\n".join(md)


async def main():
    parser = argparse.ArgumentParser(description="AI Speaking Plus Concurrency & Resource Benchmark")
    parser.add_argument("--steps", nargs="+", type=int, default=[1, 2, 5, 10, 15, 20])
    parser.add_argument("--mode", choices=["stt", "tts", "e2e", "all"], default="all")
    parser.add_argument("--ws-url", default=DEFAULT_WS_URL)
    parser.add_argument("--http-url", default=DEFAULT_HTTP_URL)
    parser.add_argument("--audio", default=DEFAULT_AUDIO_PATH)
    parser.add_argument("--token", default=None)
    parser.add_argument("--out-dir", default=DEFAULT_OUTPUT_DIR)
    args = parser.parse_args()

    os.makedirs(args.out_dir, exist_ok=True)

    # Obtain token if needed and not provided
    token = args.token
    if not token and args.mode in ["e2e", "all"]:
        print("Obtaining test user authentication JWT token...")
        try:
            req_data = json.dumps({"mobile": "09152413498", "code": "87799"}).encode("utf-8")
            req = urllib.request.Request(
                f"{args.http_url.rstrip('/')}/api/v2/auth/otp/verify",
                data=req_data,
                headers={"Content-Type": "application/json"},
            )
            with urllib.request.urlopen(req, timeout=10) as resp:
                auth_res = json.loads(resp.read().decode("utf-8"))
                token = auth_res.get("data", {}).get("token")
                print(f"Token acquired: {token[:20]}...")
        except Exception as e:
            print(f"Warning: Failed to obtain token ({e}). Continuing with optional guest auth.")

    tester = LoadTester(
        ws_url=args.ws_url,
        http_url=args.http_url,
        audio_path=args.audio,
        jwt_token=token,
    )
    monitor = RemoteServerMonitor(ssh_host=SSH_HOST, container_name=APP_CONTAINER)

    modes_to_run = ["stt", "tts", "e2e"] if args.mode == "all" else [args.mode]
    all_summaries: Dict[str, List[StepSummary]] = {}

    for mode in modes_to_run:
        print(f"\n=======================================================")
        print(f"  STARTING SUITE: {mode.upper()} CONCURRENCY BENCHMARK")
        print(f"=======================================================")
        summaries = []
        for count in args.steps:
            summary, _ = await run_benchmark_step(tester, monitor, count, mode)
            summaries.append(summary)
            # Brief pause between steps
            await asyncio.sleep(2)
        all_summaries[mode] = summaries

    # Output Tables
    print("\n\n" + "#" * 70)
    print("                 BENCHMARK RESULTS REPORT")
    print("#" * 70 + "\n")

    report_path = os.path.join(args.out_dir, f"benchmark_report_{int(time.time())}.md")
    report_md = ["# نتایج آزمون همزمانی و ارزیابی مصرف منابع سرور (STT و TTS)\n"]
    report_md.append(f"تاریخ آزمون: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    report_md.append(f"مشخصات سرور: VPS لینوکس ۲ هسته CPU، ۴ گیگابایت رم (Docker Ktor Backend)\n")

    for mode, summaries in all_summaries.items():
        title = {
            "stt": "۱. نتایج آزمون استریم صوت و تبدیل گفتار به متن (SenseVoice STT)",
            "tts": "۲. نتایج آزمون سنتز صدای هوش مصنوعی (Kokoro-82M TTS)",
            "e2e": "۳. نتایج آزمون جامع جلسه مکالمه کامل (E2E: STT + AI Chat + TTS)",
        }.get(mode, mode.upper())
        table_md = format_markdown_table(summaries, title)
        print(table_md)
        report_md.append(table_md)

    json_path = os.path.join(args.out_dir, f"benchmark_data_{int(time.time())}.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(
            {m: [asdict(s) for s in slist] for m, slist in all_summaries.items()},
            f,
            indent=2,
            ensure_ascii=False,
        )

    with open(report_path, "w", encoding="utf-8") as f:
        f.write("\n".join(report_md))

    print(f"Full report saved to: {report_path}")
    print(f"Raw JSON data saved to: {json_path}")


if __name__ == "__main__":
    asyncio.run(main())
