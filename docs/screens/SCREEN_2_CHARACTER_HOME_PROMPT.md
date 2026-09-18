# Screen 2: Character Home Dashboard (Main Hub)
## Detailed UI/UX Specification & Google Stitch Design Prompt

---

## 🎯 Purpose of this Document
This document provides a comprehensive, production-grade UI/UX specification and a copy-pasteable design prompt tailored for **Google Stitch** (and other AI UI/UX design tools like Figma AI, Midjourney UI, or Galileo).

---

# Part 1: Ready-to-Copy Google Stitch Prompt

```text
Design a high-fidelity, polished, mobile casual game home dashboard UI for an AI English speaking learning app called "AiSpeaking Plus". 
Platform: Mobile iOS & Android (Aspect ratio 9:19.5, 393x852pt screen).
Visual Style: 3D stylized casual game aesthetic (similar to Clash Royale, Brawl Stars, and Duolingo Max), vibrant rich colors, soft ambient occlusion, glossy volumetric game buttons, friendly playful vibe with high polish.

--- SCREEN LAYOUT & HIERARCHY ---

1. TOP STATUS BAR (Gamified Header):
- Semi-transparent dark frosted glass pill containers floating at the top with safe area padding.
- Left Container: Character Level & XP. A shiny golden star icon (⭐) attached to a level pill ("Lv. 3"), with a horizontal sleek glowing progress bar showing XP (e.g., "750/1000 XP").
- Center/Right Container: Streak Flame (🔥). A glossy orange/amber flame badge with a clean counter: "5 Days".
- Right Container: Gems/Coins (💎). A glowing cyan diamond / gold coin badge with counter: "120".

2. CENTER STAGE (3D Character & Environment):
- Background: Atmospheric fantasy jungle/island landscape with soft vertical depth, sun rays filtering through lush stylized trees, floating magical dust/leaf particles.
- Stage: An ancient carved stone and moss-covered floating round podium in the center.
- Character: A charismatic, cute 3D stylized tribal/explorer warrior avatar standing on the podium with a friendly welcoming smile and energetic posture (similar to a Pixar / Supercell stylized 3D character).
- Identity Label: Above/below the character, clean typography displaying:
  - Character Name: "Indie" (Bold, 20pt)
  - Subtitle: "Level 3 Explorer" (14pt, soft gold/white)
- Dialogue Speech Bubble: A glossy white/soft blue floating speech bubble next to the character with a small tail pointing to the avatar, with Persian text: "سلام علی! آماده‌ای برای ماجراجویی مرحله ۴؟" (Hey Ali! Ready for Chapter 4 adventure?) and a small speaker audio icon.

3. BOTTOM FLOATING NAVIGATION (Arc Curved Gamified Dock):
- A curved arc arrangement of 5 floating, glossy, volumetric diamond/squircle game action buttons with drop shadows and bevel borders.
- Every button contains BOTH a crisp vector game icon AND a clear Persian label below it:
  - Button 1 (Far Left): Profile Icon (👤) with label "پروفایل" (Profile).
  - Button 2 (Mid Left): Golden Crown/Trophy Icon (👑) with label "برترین‌ها" (Leaderboard).
  - Button 3 (CENTER - MAIN CTA): Prominently larger (1.4x scale), glowing golden-amber gradient, floating higher in the center of the arc, with a pulsing play/compass icon (🧭 / ▶️) and glowing bold text "ادامه ماجراجویی (مرحله ۴)" (Continue Adventure - Level 4).
  - Button 4 (Mid Right): Vocabulary Cards Icon (🃏) with label "جعبه لغات" (Lightener Box).
  - Button 5 (Far Right): VIP Sparkling Diamond Icon (💎) with label "اشتراک ویژه" (Premium).

--- COLOR PALETTE & DESIGN TOKENS ---
- Primary Background: Fantasy Teal/Emerald deep gradient (#0D2329 to #1A444E).
- Accent Gold/Amber (CTA & Star): #FFB800 to #FF8A00 with a bright yellow highlight (#FFE600).
- Accent Cyan/Diamond (Gems & Magic): #00E5FF to #0091EA.
- Surface Pill Containers: Dark Navy Glassmorphism (rgba(15, 23, 42, 0.75)) with subtle 1px border (#334155).
- Typography: Vazirmatn / Plus Jakarta Sans, rounded, friendly, high legibility.
```

---

# Part 2: Detailed Component Breakdown & Specs

| Section | Component | Purpose & Interaction | Visual Specs |
| :--- | :--- | :--- | :--- |
| **Top Bar** | `LevelXPBar` | Displays current level and XP progress toward next level. | Gold star badge + gradient fill progress bar + "Lv. 3" text. |
| **Top Bar** | `StreakBadge` | Tracks consecutive daily practice days (Retention driver). | Fire flame icon with pulsating ambient glow + number of days. |
| **Top Bar** | `GemsCounter` | Displays virtual reward currency earned from 3-star stages. | Cyan diamond icon + pill container. |
| **Center Stage** | `AvatarPodium` | Displays user's active character in a living 3D environment. | 3D character on stone stage with subtle breathing/idle loop. |
| **Center Stage** | `DialogueBubble` | Contextual greeting referencing the next uncompleted level. | Floating glass card with tail, audio preview icon, RTL Persian text. |
| **Bottom Dock** | `ProfileButton` | Opens user profile, achievements, and settings. | Diamond squircle button, deep navy with icon + "پروفایل". |
| **Bottom Dock** | `LeaderboardButton`| Opens the Top 3 podium & community rankings. | Diamond squircle button, gold trophy icon + "برترین‌ها". |
| **Bottom Dock** | `MainPlayCTA` | **Primary Action:** Directly launches Level Map / Next Stage. | **Large central glowing amber button**, 1.4x size, pulsing animation. |
| **Bottom Dock** | `LightenerButton` | Opens flashcard vocabulary box for words learned. | Diamond squircle button, flashcard icon + "جعبه لغات". |
| **Bottom Dock** | `StoreButton` | Opens Paywall / Subscription purchase sheet. | Diamond squircle button, VIP diamond icon + "اشتراک ویژه". |

---

# Part 3: States & Micro-Interactions

### 1. Main Play CTA Button
* **Default State:** Floating slightly elevated with a soft golden outer glow and breathing scale pulse (1.0 to 1.04 scale loop).
* **Pressed State:** Scales down to 0.95 with haptic feedback, triggers immediate smooth page transition to the **Level Map**.

### 2. Dialogue Bubble
* **Interaction:** Tapping the speech bubble or avatar plays a short, encouraging audio greeting in natural English/Persian from the character voice.

### 3. Top Bar Badges
* **Interaction:** Tapping the Streak badge opens a small tooltip showing the weekly calendar streak. Tapping the Gems badge opens the store modal.

---

# Part 4: Technical Layout Grid (For Developers)

```
+-------------------------------------------------------------+
| [⭐ Lv.3 ====== 750/1000]       [🔥 5 Days]      [💎 120]  |  <-- Top Bar (Padding: 16dp, Height: 56dp)
+-------------------------------------------------------------+
|                                                             |
|                      [ 💬 "سلام علی!..." ]                  |
|                                                             |
|                          (  3D  )                           |
|                          (AVATAR)                           |
|                         [__STAGE__]                         |
|                                                             |
|                          "Indie"                            |
|                     "Level 3 Explorer"                      |
|                                                             |
+-------------------------------------------------------------+
|           [👑]               [ ▶️ ]               [🃏]       |
|    [👤]  برترین‌ها       ادامه ماجراجویی         لغات   [💎] |  <-- Arc Floating Dock
|   پروفایل                  (مرحله ۴)                  اشتراک |
+-------------------------------------------------------------+
```
