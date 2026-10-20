# AI Speaking Plus — Web (Compose Multiplatform / WASM) — Bug Report

- **Date:** 2026-10-19
- **Target:** `webApp` (Compose Multiplatform, `wasmJs`) — dev bundle built from the working tree on 2026-10-19
- **Backend:** Ktor `:8080` (reached through the webpack-dev-server proxy), AI gateway `:20128`, Postgres + Redis up
- **Viewports:** 360x640, 390x844, 430x932, 740x900, 1280x900 (+ light / dark / system theme, `fa-IR`, RTL)
- **Result:** 1 critical, 7 high, 9 medium/low findings. No uncaught JS errors in any flow.

## Test method

1. Built and served the real dev bundle: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
   (port 8081, proxy rules from `webApp/webpack.config.d/proxy.js`, so `/api/**`, `/v1/**`,
   `/resources/**` are same-origin as in normal local development).
2. Drove the UI with Playwright through the Compose **semantics DOM**: Compose Web mirrors the
   semantics tree into `#cmp_a11y_root` inside the canvas host's shadow root, where every node has
   `role` / `aria-label`, its text, and `position: fixed` geometry in CSS pixels. Clicks and typing were
   sent to the canvas at those coordinates, so this is real end-user interaction, not a mocked layer.
3. Captured console output, uncaught errors, every request/response and screenshots; used macOS Vision
   OCR as a visual sanity check.
4. Made chat states deterministic (grammar-error card, objective completion, translation, finish flow)
   with `page.route` mocks of `POST /api/v2/stages/*/chat/stream` (SSE), and separately verified the
   live backend SSE stream.
5. Flows exercised: splash → main (guest), briefing dialog, stages list, chat (live AI + mocked),
   hint sheet, objective sheet, evaluation result card + save, profile (guest & authenticated),
   avatar picker, edit-name dialog, theme modes, subscription screen, login/OTP (validation, wrong OTP,
   bypass OTP), leaderboard sheet, browser back/forward, deep links + reload, offline (API blocked),
   responsive breakpoints.

---

## P1 — Critical

### BUG-01 — Chat deep link / reload opens the WRONG stage (route state loss)

- **Area:** web routing — `navigation/.../AppNavigation.kt`, `webApp/.../WebBrowserNavigation.kt`
- **Repro:**
  1. Stages list → open stage 7 card ("خرید سیم‌کارت و بسته اینترنت دانشجویی").
  2. URL becomes `#chat%3FstageId%3Dstage-07-mobile-network-store` (`?` and `=` percent-encoded).
  3. Reload the page (F5).
- **Actual:** stage **1** ("پرواز به سوی آینده (آغاز سفر در آسمان)") loads; the URL stays encoded.
- **Expected:** stage 7 is restored.
- **Cause:** the fragment is written URL-encoded, but `restoreKey` matches raw text
  (`fragment.contains("stageId=")` / `startsWith("chat")`), so the encoded fragment never matches and
  falls through to the hard-coded default `"stage-01-inflight-london"`. The same fragment is unusable
  for sharing or bookmarking.
- **Side effect:** reload also restarts the conversation (a fresh `POST .../chat/stream` starter turn),
  so the learner loses context as well.
- **Evidence:** reload of the stage-07 URL issues `GET /api/v2/stages/stage-01-inflight-london`
  immediately afterwards. A hand-written unencoded fragment (`#chat?stageId=…`) *is* parsed correctly,
  which isolates the bug to encoding.

---

## P2 — High

### BUG-02 — Redundant API calls on every screen entry (2–3x per endpoint)

| Transition | Observed |
|---|---|
| cold boot (guest) | `GET /api/v2/stages` **x3** |
| open stages list | `GET /api/v2/stages` **x2** |
| open profile (authenticated) | `GET /api/v2/user/profile` **x2** |
| cold boot after login / reload | `stages` **x3** + `subscriptions/status` **x3** |

- **Cause:** triple trigger — ViewModel `init` + `LaunchedEffect(Unit)` + `OnResume`
  (`JourneyMapScreen.kt:77-83` next to `JourneyMapViewModel.init`; `StagesListScreen` has
  `init { loadStages() }` *and* `OnResume { loadStages() }`; `ProfileScreen` adds `OnResume { refresh() }`
  on top of the VM init).
- **Impact:** wasted traffic/battery, request races (duplicate requests observed aborted),
  possible flicker, and a tripled failure surface on flaky networks.

### BUG-03 — Escape key skips open modals, and modals cannot be closed by Escape

- Chat screen with the "هدف مرحله" sheet open → **Escape navigates back to `#main`**; the whole screen is
  popped instead of closing the sheet.
- Conversely Escape does nothing for: briefing dialog, hint confirm, edit-name dialog (no keyboard close
  at all), leaderboard sheet.
- **Expected:** Escape closes the topmost modal; only pops the screen when nothing is open.
- **Evidence:** matrix run — MAIN: no-op; STAGES_LIST / PROFILE / CHAT: → MAIN; CHAT + sheet: → MAIN.
- **Impact:** keyboard users lose the conversation by pressing Esc, and cannot dismiss any dialog by keyboard.

### BUG-04 — Accessibility: modals exposed as one giant button + unlabeled controls

- Whole modal cards become `role=button` with all content concatenated, e.g.
  `[button] (20,136 390x660) 'مرحله ۱Eپرواز به سوی آینده … هدف این مکالمه …'` (briefing dialog), same for
  the objective sheet, evaluation card, edit-name dialog and stage-list cards. Inner buttons
  ("بستن", "شروع گفتگو") are nested **inside** that button → invalid nesting, screen readers announce one
  undifferentiated blob.
- Unlabeled full-screen scrim buttons: `[button] (0,0 430x932) ''` (briefing, objective, avatar picker,
  leaderboard) — focusable controls with no accessible name.
- Unlabeled text inputs: chat composer (`role=textbox`, no name, no placeholder exposed) and the
  subscription discount-code field (`role=textbox (101,713 312x56)`).
- Zero-size semantics nodes for off-screen list content (`0x0` buttons/texts) on stages list and profile.

### BUG-05 — Accessibility: dismissed ModalBottomSheet keeps stale semantics nodes

- Open the leaderboard (star pill) → tap the scrim: the sheet is visually gone (screenshot pixel-identical
  to the pre-sheet frame, mean diff 0.0) but the a11y tree still contains the sheet title **and** the
  full-screen "بستن برگ" dismiss button, unchanged 5 s later.
- **Impact:** assistive tech (and any a11y-driven automation) still sees a phantom modal covering the app.

### BUG-06 — No keyboard navigation: Tab does not move focus

- Five consecutive `Tab` presses leave `document.activeElement` on the canvas container (`DIV#`);
  focus never reaches a control. Keyboard-only users cannot operate the app.

### BUG-07 — AI `corrected_sentence` is received but never shown

- The live backend returns it separately, e.g.
  `"has_grammar_error":true, "corrected_sentence":"I want a chicken sandwich, please."`.
- `feature/chat/.../ChatViewModel.kt:594` stores it in `ChatStatus.Answered.correctedSentence`, but
  `component/UserChatItem.kt:98` renders only `status.grammar` — the corrected sentence is dead data.
- **Impact:** learners see the explanation but never the corrected sentence the model produced.

### BUG-08 — "Grammatically correct" tag can be shown for a wrong sentence

- `component/UserChatItem.kt:98`: `if (status.grammar.isBlank()) GrammarOkTag() else GrammarTipCard(...)`,
  while the VM computes `hasGrammarError = turn.hasGrammarError || turn.grammarFeedbackFa.isNotBlank()`.
- If the model sets `has_grammar_error = true` but leaves `grammar_feedback_fa` blank, the UI shows the
  green "گرامر درست بود" tag for an erroneous sentence.

---

## P3 — Medium / Low

### BUG-09 — Chat error state: misleading copy and inconsistent defaults
- When the AI stream fails, the screen shows **"خطا در باز کردن مرحله"** (error *opening the stage*) even
  though the stage GET succeeded; a second variant reads "امکان بارگذاری اطلاعات این مرحله وجود ندارد…".
- The header falls back to the generic "مکالمه انگلیسی" and the progress chip degrades to
  **"۰ از ۱ پیام"** instead of the stage's real budget (10).
- Repro: make `chat/stream` fail (a real HTTP 500 was observed from the backend when the AI returned
  malformed JSON).

### BUG-10 — Hint sheet is reachable while the conversation never started
- In the error state the bottom bar stays live; "راهنما" opens the star-cost confirm sheet
  ("هر بار گرفتن راهنما از ستاره‌های این مرحله کم می‌کند") for a conversation that is not running.

### BUG-11 — OTP resend countdown uses the OTP TTL instead of the server cooldown
- After sending the code the screen says "ارسال دوباره کد تا ۱۱۲ ثانیه دیگر".
- `LoginViewModel.kt:107-118` uses `expiresInSeconds` (= `OTP_TTL_SECONDS` 120 s) as the resend gate,
  while the backend's resend cooldown is `OTP_COOLDOWN_SECONDS = 60 s`.
- **Impact:** resend is blocked roughly twice as long as the server requires.

### BUG-12 — "اشتراک ویژه" is a dead control for subscribers
- `JourneyMapScreen.kt:298-304`: subscribers get `GameStatPill(text = "اشتراک ویژه", …)` with **no
  `onClick`** (and no a11y role), so tapping it does nothing — the main-screen entry point to the
  subscription screen disappears once a plan is active.

### BUG-13 — Stream payload `message` is ignored when chunks exist
- `ChatViewModel` builds the final AI bubble from the concatenated deltas and only falls back to
  `turn.message` when no delta arrived. If a stream ends with a truncated delta set but a complete
  `done.payload.message`, the UI shows the truncated text while TTS reads the full reply.

### BUG-14 — Malformed SSE events are silently swallowed
- `network/.../StageApi.streamStageChatMessage` wraps decoding in `catch (_: Exception) {}`; schema drift
  or a non-JSON keep-alive produces a silently empty/hung answer instead of an error.

### BUG-15 — Copy button gives no feedback
- Copy works (clipboard verified: "Hello! Welcome. Do you want a SIM card?") but no toast, snackbar or
  label change confirms it.

### BUG-16 — Touch targets below 44 px (measured, fully on-screen controls)
`پروفایل من` 40x40 · star pill 85x37 · `اشتراک ویژه` 140x40 · dialog `بستن` 32x32 ·
`ویرایش نام` (pencil) 28x28 · `تغییر` (level) 43x25 · `تمدید` 56x33 · `تمدید سریع` 77x29 ·
back buttons 40x40 (stages / profile / subscription).

### BUG-17 — (cross-cutting, backend-adjacent) AI failure surfaces as HTTP 500 + generic UI error
- Server log during testing: `ERROR StageChatService - Error in generateAiStarterTurnStream:
  Unexpected JSON token …` → `ChannelWriteException` → `500`.
- The web app shows only the generic error of BUG-09; network failure, provider failure and schema
  failure are indistinguishable, and there is no automatic retry.

---

## Verified working (no bug found)

- Splash → main as guest; journey map data from API; "مرحله ۱ از ۱۵" matches the GUEST tier (15 free stages).
- Login: short-number validation message; wrong OTP → 401 + "کد تایید نامعتبر است یا منقضی شده است";
  correct OTP → token + user persisted in `localStorage`, session survives reload.
- Chat with the live backend: SSE streaming, AI reply, TTS autoplay (`پخش صدا` → `توقف صدا`),
  translation toggle (`ترجمه` → `پنهان کردن ترجمه`), grammar tip card, objective sheet.
- Evaluation: result card (score 850, 2 stars, coach note) → "ثبت نتیجه و ادامه مسیر" → stars 13 → 15,
  active stage 7 → 8, list shows "۷ انجام شده".
- STT: `ws://…/api/v2/stt/live?token=…` → `101 Switching Protocols`; with a silent fake mic the session
  is closed with no transcript (expected).
- Leaderboard sheet: `GET /api/v2/leaderboard/journey?page=1&pageSize=20` 200, rank row rendered.
- Browser back/forward across `#main` ↔ `#stages`.
- Theme dark / light / system applies, persists (`theme_mode` in `localStorage`) and follows
  `prefers-color-scheme`.
- Avatar picker and edit-name dialog render; sign-out button present.
- Offline (all `/api/**` aborted): "مرحله‌ها بارگذاری نشدند. اتصال اینترنت را بررسی کن." + "تلاش دوباره".
- Responsive: 390/430 full-bleed; 740 centered phone frame; 1280 phone frame + brand panel; 360x640 no overflow.
- No uncaught JS errors in any exercised flow.

## Not covered / limitations

- Real microphone and STT accuracy (headless uses a fake capture device) — connect/teardown only.
- Real payment (Myket / in-app) — the subscription screen was captured, no purchase executed.
- Push notifications (FCM) beyond service-worker registration.
- iOS / Android targets — out of scope (web frontend only).
- Persian OCR quality was too low for pixel-level text verification, so every UI assertion above is based
  on the Compose semantics tree plus screenshots, not OCR guesses.

## Deliberately excluded (false positives found while testing)

These looked like bugs in early runs and were disproven on re-check; listed so they are not re-reported:

- Duplicate AI chat bubbles / lost greeting — caused by a test mock that answered the starter turn with
  the wrong payload; message list appends correctly in both chunked and `done`-only streams.
- "Light theme does not apply" — the harness matched the description text node "…تیره، روشن…" instead of
  the button; light/dark/system all work.
- Duplicate/cancelled `bg_1.webp` request — seen once during the splash transition only; a clean boot
  issues a single request per image.
- "Theme pills collapse to 25 px in the guest profile" — Compose clamps semantics bounds to the visible
  clip; the row is 44 px tall and simply extends below the fold behind a normal scroll.
