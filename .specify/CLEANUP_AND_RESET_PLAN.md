# Plan: Codebase Reset & Infrastructure Preservation (AISpeakingPlus)

## Objective
Safely clean up and remove legacy application code across both **Server** and **Compose Multiplatform (Client)** modules, while preserving:
1. All Gradle build scripts, dependency versions, and platform targets.
2. Server infrastructural configurations (Postgres/Exposed connection setup, Redis/Redisson client, Ktor plugin setup, JWT security, STT/TTS native hooks).
3. Client shared core infrastructure (Network Ktor client, KMP Storage/Settings, Design system foundation, build configurations).

This clean state ensures the agent in the subsequent chat can directly execute `/speckit-plan` and build the new story-based architecture without legacy baggage or compilation conflicts.

---

## What to PRESERVE (Do NOT Delete)

### 1. Build & Dependency Configurations
- `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `local.properties`
- `gradle/libs.versions.toml`, `gradle/wrapper/*`, `gradlew`, `gradlew.bat`
- Module-level `build.gradle.kts` files in all retained modules.

### 2. Server Infrastructure (`server/src/main/kotlin/ir/speaking/core/`)
- Database connection & migration setup (`core/database/Databases.kt`, pool configs).
- Redis & Redisson setup (`core/redis/`).
- Ktor core plugins:
  * Serialization (`configureSerialization`)
  * Security & JWT token verification (`configureSecurity`)
  * CORS & WebSockets configuration
  * StatusPages & exception routing handlers
- Native STT / TTS engine wrappers (`Sherpa-ONNX`, `Kokoro` hooks in `feature/stt` and `feature/tts`).
- Server deployment scripts & docker configs (`server/Dockerfile`, `server/docker-compose.yml`, `server/nginx/`).

### 3. Client Infrastructure & Platform Launchers
- Android platform launcher: `androidApp/` (MainActivity, AndroidManifest).
- Desktop launcher: `desktopApp/` (Main.kt, packaging configs).
- Web launcher: `webApp/` (index.html, wasm/js setup).
- iOS app wrapper: `iosApp/`.
- Shared foundation modules:
  * `network/`: Ktor HttpClient, content negotiation, logging, token interceptors.
  * `storage/`: Multiplatform Settings / Key-Value storage drivers.
  * `utils/`: Common helpers, Coroutine dispatchers, mappers.
  * `sharedUI/`: Core theme, typography, color palette, common visual primitives.

### 4. Specification & Assessment Artifacts
- `.specify/` (Constitution, Assessments, Feature Specs, Templates).
- `specs/001-story-based-speaking-journey/` (both English and Persian specs and checklists).
- Architectural markdown documents (`APP_SPECIFICATION_V2.md`, `TECHNICAL_SPECIFICATION.md`, `DATABASE_SCHEMA.md`).

---

## What to REMOVE (Clean Up)

### 1. Server Legacy Feature Domains (`server/src/main/kotlin/ir/speaking/feature/`)
Remove obsolete routes, services, and tables that are superseded by V2 story-based stages:
- `feature/scenario/` & `feature/scenario_detail/` (Legacy scenario catalog)
- `feature/category/` (Legacy scenario categories)
- `feature/challenge/` & `feature/home/` (Legacy dashboard routes)
- `feature/competition/` (Legacy competition mode)
- `feature/lightener/` & `feature/word/` (Legacy flashcards)
- `feature/plan/` & `feature/purchase/` (Legacy payment models — to be replaced by V2 subscription tier routes)
- Clean up obsolete route registrations inside `server/src/main/kotlin/ir/speaking/core/Routing.kt` and DI bindings in `di/`.

### 2. Client Legacy Feature Modules (`feature/*`)
Remove or empty out obsolete UI feature modules that will be replaced:
- `feature/scenarios/`, `feature/scenario_detail/`
- `feature/lightener/`
- `feature/competition/`
- `feature/english_level/` (Legacy placement test)
- `feature/search/`
- `feature/roadmap/` (Replaced by the new 2D story journey map)
- Remove respective `include(":feature:...")` lines in `settings.gradle.kts` for decommissioned modules.

### 3. Client Legacy Domain & Data Implementations (`domain/src/`, `data/src/`)
- Clear obsolete use cases, repository interfaces, and network DTOs related to scenarios, categories, and lightener boxes.
- Keep the directory package structures: `ir.speaking.domain.*` and `ir.speaking.data.*`.

---

## Execution Steps for the Next Agent

1. **Step 1: Backup & Baseline Check**
   - Verify `git status` is clean.
   - Create a clean git branch: `git checkout -b chore/codebase-clean-slate`.

2. **Step 2: Server Feature Cleanup**
   - Delete obsolete directories under `server/src/main/kotlin/ir/speaking/feature/` (`scenario`, `category`, `challenge`, `home`, `competition`, `lightener`, `word`).
   - Clean up `Routing.kt` and Koin modules so the server only keeps core infrastructure (`auth`, `user`, `stt`, `tts`, `subscription` placeholders).
   - Ensure the server module compiles cleanly: `./gradlew :server:compileKotlin`.

3. **Step 3: Client Features & Navigation Cleanup**
   - Remove obsolete submodules from `feature/` and update `settings.gradle.kts`.
   - In `domain/` and `data/`, remove legacy repository implementations and models.
   - Retain common UI components in `sharedUI/` and navigation host in `navigation/`.
   - Ensure shared modules compile cleanly: `./gradlew :domain:compileKotlin :data:compileKotlin :sharedUI:compileKotlin`.

4. **Step 4: Verification & Readiness**
   - Run `./gradlew assemble --dry-run` or targeted compile tasks to ensure zero broken imports.
   - Commit changes: `git commit -m "chore: purge legacy features, retain build and core infrastructure"`.
   - Signal readiness to run `/speckit-plan` for `001-story-based-speaking-journey`.
