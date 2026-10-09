# Quickstart: Stages List Screen

**Feature**: `005-stages-list-screen`  
**Date**: 2026-10-07

## Overview
This document describes the validation flow to verify the new Stages List Screen end-to-end.

## Pre-requisites
- Local Gradle build environment (`./gradlew`)
- Android emulator or Desktop app runner

## Verification Steps

### 1. Launch & Navigation Entry
1. Run the application to the main journey map (`JourneyMapScreen`).
2. Locate the "مرحله‌های گذشته" button in the bottom section of `JourneyMapScreen`.
3. Tap on the button.
4. **Expected**: The app smoothly transitions from `JourneyMapScreen` to the new full-page `StagesListScreen` (replaces the old dialog).

### 2. Header & Progress Stats
1. Observe the top bar of `StagesListScreen`.
2. **Expected**:
   - Title indicates "مراحل و پیشرفت یادگیری".
   - `AppBackButton` is displayed on the right (RTL). Tapping it returns to `JourneyMapScreen`.
   - Summary card displays total earned stars (e.g. ⭐ 9 / 45) and completion progress.

### 3. List Item Inspection & Current Stage
1. Observe the list of stages.
2. **Expected**:
   - All stages (1 to 15+) are rendered in sequential order.
   - For previously completed stages, earned stars (1-3) are brightly illuminated with `ic_star`.
   - The user's current active stage is visually emphasized with a glowing border and a badge labeled "مرحله کنونی شما".
   - The list automatically scrolled or positions the active stage in clear view.

### 4. Stage Interactivity
1. Tap on an unlocked or previously played stage.
2. **Expected**: Directly navigates to `StageChatRoute(stageId)` to start/replay the stage.
3. Tap on a locked subscription stage.
4. **Expected**: Directly navigates to `SubscriptionRoute` or provides an appropriate prompt.
