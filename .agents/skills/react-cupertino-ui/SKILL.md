---
name: react-cupertino-ui
description: Comprehensive guide, component catalog, API specifications, and design engineering patterns for React Cupertino UI (react-cupertino-ui.pro) and Apple Liquid Glass design system. Use when building, styling, or reviewing Apple-style interfaces, Cupertino components (Atoms, Molecules, Organisms, Templates), Apple Intelligence effects (AILoadingState, SiriWaveform, IntelligenceGlow), frosted glass materials, and iOS-native touch behaviors in React.
license: MIT
metadata:
  author: "andersonlima / react-cupertino-ui.pro"
  version: "2.0.5"
---

# React Cupertino UI & Apple Design System

A master skill for building Apple-grade, Cupertino-styled web applications in React, implementing the visual language of iOS 18, visionOS, and Apple Human Interface Guidelines (HIG) with modern "Liquid Glass" materials and 60–120fps mobile performance.

---

## 1. Core Visual Architecture (Apple Liquid Glass)

The Apple Liquid Glass aesthetic is built from five distinct optical layers:
1. **Translucency & Frosted Blur:** High-blur backdrop (`backdrop-filter: blur(20px) saturate(180%)`).
2. **Specular Highlight (Rim Light):** A 1–1.5px directional gradient edge simulating overhead light reflection.
3. **Refraction & Lens Softness:** Soft pixel bending at curved corners.
4. **Micro-Noise / Texture:** Prevents color banding on translucent surfaces.
5. **Multi-layer Ambient Shadows:** Deep, soft elevation without hard edges.

### Golden CSS Formula for 120fps Mobile Safari & Chrome
```css
.apple-glass {
  position: relative;
  background: rgba(30, 30, 36, 0.65);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 
    0 12px 32px 0 rgba(0, 0, 0, 0.25),
    inset 0 1.5px 0 0 rgba(255, 255, 255, 0.4);
  transform: translateZ(0); /* Promotes to GPU Hardware Compositor Layer */
}

/* Reduced transparency accessibility fallback */
@media (prefers-reduced-transparency: reduce) {
  .apple-glass {
    backdrop-filter: none !important;
    -webkit-backdrop-filter: none !important;
    background-color: rgba(22, 22, 26, 0.96) !important;
  }
}
```

---

## 2. Component Hierarchy & Atomic Design

`react-cupertino-ui` structures components into four atomic tiers:

### A. Atoms (Foundational Elements & Apple Intelligence)

#### 1. `AILoadingState`
Apple Intelligence pulsating orb with expanding ripples and frosted background.
```tsx
import { AILoadingState } from '@react-cupertino-ui/ai-loading-state';
// or from local/custom Cupertino library:

<AILoadingState
  variant="thinking" // "thinking" | "generating" | "searching"
  message="در حال تحلیل تلفظ صوتی..."
/>
```
- **Variants:**
  - `thinking`: Neutral glowing orb with ripple rings.
  - `generating`: Indigo/violet deep glow for text/audio generation.
  - `searching`: Apple blue glow for retrieval / knowledge search.

#### 2. `SiriWaveform`
The iconic Apple Siri fluid audio waveform.
```tsx
import { SiriWaveform } from '@react-cupertino-ui/siri-waveform';

<SiriWaveform
  active={isSpeaking}
  palette="multicolor" // "multicolor" | "mono" | "ocean" | "sunset"
  size="md"            // "sm" | "md" | "lg"
  motion="pulse"       // "pulse" | "calm"
  amplitude={1.2}      // Number between 0.5 and 1.5
/>
```

#### 3. `IntelligenceGlow`
Wraps any element with Apple Intelligence's iridescent perimeter glow.
```tsx
import { IntelligenceGlow } from '@react-cupertino-ui/intelligence-glow';

<IntelligenceGlow active={isProcessing} intensity={0.8}>
  <div className="p-4 bg-black/60 rounded-3xl">
    {/* Content glowing when active */}
  </div>
</IntelligenceGlow>
```

#### 4. Foundation Atoms
- `Avatar`: Circular or squircle with status ring.
- `Badge`: Notification pill with Apple red `#ff3b30` or system hues.
- `Caption`, `Headline`, `Title`, `Paragraph`: Apple typographic hierarchy.
- `PageControl`: Horizontal iOS dot carousel indicator.

---

### B. Molecules (Functional Combinations)

#### 1. `Button`
iOS spring feedback button with multiple style variants.
```tsx
import { Button } from '@react-cupertino-ui/button';

<Button
  variant="glass" // "glass" | "solid" | "secondary" | "outline" | "ghost"
  size="default"  // "default" | "sm" | "lg" | "icon"
  loading={isLoading}
  icon={<Mic className="w-4 h-4" />}
>
  شروع مکالمه
</Button>
```

#### 2. `SegmentedControl`
Iconic iOS sliding pill control with smooth spring inertia.
```tsx
<SegmentedControl
  value={selectedTab}
  onChange={setSelectedTab}
  items={[
    { id: 'daily', label: 'مکالمه روزمره' },
    { id: 'business', label: 'تجاری' },
    { id: 'travel', label: 'سفر' }
  ]}
/>
```

#### 3. `Switcher` (iOS Toggle)
Smooth knob toggle with Apple green (`#34c759`) active fill.
```tsx
<Switcher checked={autoPlay} onChange={setAutoPlay} />
```

#### 4. `AudioMessage`
Interactive voice message bubble with waveform and play/pause controls.
```tsx
import { AudioMessage } from '@react-cupertino-ui/audio-message';

<AudioMessage
  duration="0:42"
  isPlaying={isPlaying}
  onPlayPause={() => setIsPlaying(!isPlaying)}
/>
```

#### 5. Additional Molecules
- `AIPromptInput`: Expandable input pill with audio mic and send action.
- `ActionSheet`: iOS bottom action selector.
- `BottomSheet`: Drag-to-dismiss interactive sheet with rubber-band physics.
- `SearchBar`: iOS frosted search bar with clear button and voice icon.
- `Slider` & `ProgressSlider`: Thick rounded track with tactile thumb.

---

### C. Organisms & Templates

- `NavigationBar`: Frosted glass top app bar.
  - **RTL Requirement**: In Persian interfaces, back button anchor is **top-right** with right-pointing chevron (`>`), and the title is **symmetrically centered**.
- `TabBar`: Bottom iOS frosted dock with safe-area padding (`env(safe-area-inset-bottom)`).
- `AIConversation`: Complete chat interface containing messages, audio bubbles, and input dock.
- `AuthTemplate`, `SettingsTemplate`, `ProfileTemplate`: Pre-composed iOS layout pages.

---

## 3. Spring Physics & Motion Standards

Apple interfaces feel alive because motion is **interruptible, momentum-preserving, and velocity-aware**.

### Mapping to `motion` (Framer Motion v12/14)
```ts
// Apple Spring Constants
export const appleSpring = {
  type: 'spring',
  stiffness: 450,
  damping: 30,
  mass: 0.8,
};

export const appleBounce = {
  type: 'spring',
  stiffness: 400,
  damping: 22, // Lower damping = subtle tactile overshoot
};
```

### Interactive Touch Feedback (Active Scale)
Every touchable element must respond immediately on `pointerdown` and spring back on release:
```tsx
<motion.button
  whileTap={{ scale: 0.96 }}
  transition={{ type: 'spring', stiffness: 500, damping: 25 }}
  className="apple-glass-pill"
>
  لمس کنید
</motion.button>
```

---

## 4. Build Configuration in Vite Projects

When using `@react-cupertino-ui` packages with Vite:

1. **SCSS Preprocessor:**
   Install `sass` as dev dependency:
   ```bash
   npm install -D sass
   ```

2. **Vite Alias & Resolver (`vite.config.ts`):**
   ```ts
   import path from 'path';

   export default defineConfig({
     resolve: {
       alias: [
         {
           find: /^@react-cupertino-ui\/shared\/lib\/(.*)/,
           replacement: '@react-cupertino-ui/shared/dist/lib/$1',
         },
       ],
     },
     css: {
       preprocessorOptions: {
         scss: {
           loadPaths: [
             path.resolve(__dirname, 'node_modules/@react-cupertino-ui/button/dist'),
             path.resolve(__dirname, 'node_modules'),
           ],
         },
       },
     },
   });
   ```

---

## 5. Persian & RTL Best Practices

1. **Font Hierarchy:** Prioritize `Vazirmatn Variable` followed by `-apple-system`, `SF Pro Display`, `sans-serif`.
2. **Back Button Direction:**
   - In RTL, `ic_back` MUST point right (`>`) because navigating back moves in the direction of reading start.
3. **Number Formatting:** Use Persian digits (`۱۲۳`) in UI contexts where Persian prose dominates, keeping Latin digits for timestamps and technical metrics.
4. **Text Selection & Tap Highlights:**
   ```css
   html {
     -webkit-tap-highlight-color: transparent;
   }
   ```
