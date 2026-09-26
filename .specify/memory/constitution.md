<!--
# Sync Impact Report
- Version change: 1.1.0 -> 1.2.0
- Added sections:
  * Core Principles:
    - VI. Living OpenAPI Specification & Self-Documenting Contracts (API & WebSocket):
      * Mandatory requestBody definitions with schema structures and concrete example payloads.
      * OpenAPI 3.0.3 compatibility guarantee for seamless Apidog Live Sync and desktop UI importing.
      * Resilient input normalization: consumer endpoints (e.g. mobile authentication) must accept standard formats, international codes (+98), Persian/Arabic digits, and common parameter aliases without rejecting valid input.
  * Technology Stack & Architecture Constraints: OpenAPI 3.0.3 compatibility sanitizer.
  * Development Workflow, Quality Gates & Verification: Mandatory Apidog Live Sync verification gate.
- Removed sections: None
- Follow-up TODOs: None
-->

# AISpeakingPlus Constitution

## Core Principles

### I. Clean Architecture Everywhere (Client & Server)
Both Client and Server modules MUST adhere strictly to Clean Architecture principles.
- Codebases are segregated into distinct layers: Domain (Entities, Use Cases/Interactors, Repository Interfaces), Data (Repository Implementations, Data Sources, DTOs, Database/Network Mappings), and Presentation/API (UI/ViewModel on Client, HTTP Routing/Controllers on Server).
- The Dependency Rule is inviolable: Dependencies MUST point inward toward the Domain layer. The Domain layer MUST remain pure Kotlin with zero knowledge of UI frameworks, Ktor routing, or database libraries.
- Domain models MUST be decoupled from external schemas and DTOs using dedicated bidirectional mappers.

### II. Pure Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP) Client
The frontend and shared client application MUST be built using Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP) across targets (Android, Desktop, Web, iOS).
- Shared business logic, state management, networking, and UI components MUST reside in shared modules (`domain`, `data`, `sharedUI`, `feature/*`) rather than platform-specific code.
- Platform-specific code (`actual` implementations) is permitted only for hardware-level or OS-specific capabilities (e.g., native audio record/playback, specific notification channels) and MUST be hidden behind common interfaces.

### III. Ktor Server & Asynchronous Coroutine Pipeline
The backend MUST be built exclusively on Ktor Server running on an asynchronous, non-blocking coroutine engine (Netty).
- Thread-blocking calls (synchronous file I/O, heavy computation, unmanaged socket wait) inside routing handlers are strictly prohibited.
- All database operations (via JetBrains Exposed), Redis commands, external AI SDK calls, and audio processing pipelines MUST be non-blocking or explicitly dispatched to appropriate coroutine dispatchers (e.g., `Dispatchers.IO`).
- Real-time communications (STT/TTS streaming, AI chat SSE, and WebSockets) MUST use structured coroutine concurrency with robust lifecycle and error propagation.

### IV. Compile-Time & Idiomatic Dependency Injection via Koin
Dependency Injection across both Client and Server MUST be managed exclusively with Koin (leveraging compile-time safe annotations/KSP where applicable).
- Service locator anti-patterns (such as ad-hoc resolution in domain entities or global singleton access) are forbidden.
- Dependencies MUST be provided via constructor injection. Modules MUST be declared per feature or infrastructure boundary (`coreModule`, `authModule`, `chatModule`, etc.) to guarantee isolated testability and maintainability.

### V. Ultra-Low Latency & High-Performance API Standards
API speed, throughput, and minimal response latency are critical non-negotiable requirements for the backend platform.
- Endpoints MUST target sub-100ms response times for typical database-backed REST/JSON queries and sub-10ms for cached data.
- Redis MUST be used aggressively for high-throughput transient state (OTP authentication tokens, rate-limiting cooldowns, leaderboard rankings, and active session cache).
- High-volume data streaming (such as conversational STT transcription and TTS audio synthesis) MUST use memory-efficient chunking, streaming protocols (WebSockets/SSE), and caching layers to minimize CPU overhead and bandwidth consumption.
- JSON serialization MUST utilize `kotlinx.serialization` for minimal allocations and peak throughput.

### VI. Living OpenAPI Specification & Self-Documenting Contracts (API & WebSocket)
All HTTP REST and WebSocket endpoints in the backend MUST be self-documenting using Ktor's official OpenAPI metadata (`.describe { ... }`).
- Every new or modified route MUST declare its input specifications (`requestBody` with exact data class schemas and realistic example payloads), path and query parameters (`pathParameter`, `queryParameter`), authentication headers, and response status codes (`responses`).
- Mutating endpoints (POST, PUT, PATCH) MUST define explicit `requestBody` schemas and concrete JSON example payloads. Leaving request bodies empty or untyped is strictly prohibited to ensure that API testing platforms (such as Apidog, Postman, and Swagger UI) immediately display interactive parameter forms and pre-filled request templates upon Live Sync.
- OpenAPI 3.0.3 Compatibility Guarantee: The server's exposed `/openapi` and `/openapi.json` endpoints MUST output schemas fully compatible with OpenAPI 3.0.3 (sanitizing JSON Schema 2020-12 / OpenAPI 3.1 type arrays like `["string", "null"]` into standard `nullable: true` properties), guaranteeing seamless Live Sync in Apidog without schema parsing failures or dropped request bodies.
- Resilient Consumer Input Normalization: Public-facing endpoints receiving regional or user-supplied identifiers (such as Iranian mobile phone numbers) MUST be resilient and permissive on ingress while strict on internal validation. Specifically, mobile number parsers MUST normalize Persian/Arabic digits (`۰-۹`, `٠-٩`), international prefixes (`+98`, `0098`, `98`), standard 11-digit formats (`09XXXXXXXXX`), and accept common field aliases (`mobile`, `phoneNumber`, `phone`) without failing valid user requests with uninformative 400 errors.
- Request and response schemas MUST be derived directly from `kotlinx.serialization` models to guarantee zero drift between runtime code and API documentation.
- The server MUST expose an active `/openapi` endpoint synchronized with the live routing tree, enabling immediate JSON export and real-time live synchronization (Live Sync) in API clients and testing suites such as Apidog.
- No API or socket route may be merged into the main branch without its complete `.describe` metadata block.

## Technology Stack & Architecture Constraints

- **Language & Runtime**: Kotlin JVM (`2.x+`) for Server; Kotlin Multiplatform (`2.x+`) for Client platforms.
- **Server Framework**: Ktor Server (`3.x+`) with Netty engine and official Ktor plugins (ContentNegotiation, StatusPages, JWT Auth, WebSockets, and OpenAPI).
- **Client Framework**: Compose Multiplatform (CMP) for shared declarative UI.
- **Dependency Injection**: Koin (`4.x+`) across Server and Client.
- **Persistence & Caching**: PostgreSQL for durable relational storage with JetBrains Exposed; Redis for distributed caching, OTP management, and rate limiting.
- **Speech & AI Integration**: On-premise/native engine integration (Sherpa-ONNX / Kokoro) paired with external LLM gateways, handled asynchronously through streaming pipelines.
- **API Documentation & Testing**: Official Ktor OpenAPI (`ktor-server-openapi`) with OpenAPI 3.0.3 schema compatibility sanitizer for seamless real-time synchronization with Apidog.

## Development Workflow, Quality Gates & Verification

- **Code Review**: Every pull request MUST verify adherence to layer boundaries (Clean Architecture) and confirm no blocking calls exist on main/request coroutine dispatchers.
- **Testing Gates**:
  * Unit tests for all Domain Use Cases with mock/fake repositories.
  * Integration tests for Ktor routing and API endpoints using Ktor `testApplication`.
  * Performance regression checks: Endpoints MUST not exhibit N+1 database queries or unindexed scans.
- **Contract Enforcement & Living Spec**:
  * REST APIs MUST adhere strictly to resource-oriented conventions and versioned routing (e.g., `/api/v2/...`).
  * All endpoints and WebSockets MUST validate against the OpenAPI schema, and the `/openapi` endpoint must import cleanly and without schema errors into Apidog, with all request bodies, schemas, and examples visible.

## Governance

- The AISpeakingPlus Constitution is the supreme architectural guideline for the repository. All designs, feature specifications, and implementations must conform to its principles.
- Amendments to this constitution require justification, explicit documentation, and version updates adhering to Semantic Versioning:
  * **MAJOR**: Fundamental shifts in architectural paradigm, removal or breaking redefinition of core principles.
  * **MINOR**: Introduction of new principles, architectural components, or substantial constraint additions.
  * **PATCH**: Non-semantic refinements, typographical fixes, and clarifications.
- Compliance MUST be verified during architecture reviews and pull request approvals.

**Version**: 1.2.0 | **Ratified**: 2026-09-17 | **Last Amended**: 2026-09-26
