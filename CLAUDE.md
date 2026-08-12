# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NativeMinds is a **mobile developer case study project**: a bite-sized story/content app (Android, Kotlin + Jetpack Compose). Users browse and search a list of short stories, read them, or listen to them via audio/TTS. Free users get limited access; premium (subscriber) users unlock full content and an AI-powered feature.

- Application ID / namespace: `com.example.nativeminds`
- compileSdk/targetSdk 36, minSdk 24, Java 11, Compose BOM + Material 3
- Dependency versions are managed via the Gradle version catalog at `gradle/libs.versions.toml`

## Case Study Requirements (must all be demonstrably working)

These are the acceptance criteria — every feature decision should trace back to one of them:

1. **Content experience**: browsable + searchable story list; stories can be **read** and **listened to** (audio playback / TTS).
2. **Premium gating**: non-subscribers see a taste (e.g. limited stories or partial content); subscribers unlock everything. A mock/sandbox subscription flow is acceptable — real payments are out of scope.
3. **AI feature (premium)**: at least one AI capability that genuinely improves the product (not a gimmick).
4. **Production concerns**:
   - Stays fast at scale (list performance, paging, image/audio caching).
   - Works offline (cached content readable without network; graceful degradation).
   - Observability: analytics for user behavior + crash/error reporting.
5. **Deliverables**: GitHub repo, architecture diagram (Miro/Excalidraw), deployed/installable build, 2–5 min demo video, README documenting decisions and AI workflow.

## Engineering Rules

- **Every architectural decision must be defensible.** The case evaluates reasoning, not just output. When making a non-obvious choice (library, pattern, data model), record it briefly in the README's "Key Decisions" section — what was chosen, why, what the trade-off is, and what would change at 10× scale.
- **Known shortcuts must be logged.** If a corner is knowingly cut (mock billing, hardcoded content seed, skipped edge case), add it to the README's "Cut Corners / Assumptions" section immediately — don't leave it undocumented.
- Keep the slice **vertical and working end-to-end** rather than broad and half-finished. A small feature set that runs, syncs offline, and reports errors beats many stub screens.
- Failures must be visible: no silently swallowed exceptions; surface errors to the user where relevant and to crash reporting always.
- UI text goes through string resources, not hardcoded literals (the app may need localization later).
- New dependencies are declared in `gradle/libs.versions.toml` and referenced via `libs.` aliases; justify each new dependency (prefer fewer, well-known libraries).
- Write unit tests for domain/repository logic that carries business rules (gating logic, paging, offline fallback). UI can be verified via a small number of Compose tests.

## Architecture (living section — update as decisions are made)

Intended shape (adjust as the implementation evolves and keep this section current):

- **Pattern**: Clean Architecture + MVVM with unidirectional data flow (ViewModel → StateFlow → Compose), repository layer between UI and data sources.
- **Layers**: `ui/` (Compose screens + ViewModels), `domain/` (models, use cases if needed), `data/` (repositories, local cache, remote source).
- **Model separation (mandatory)**: data model (DTO/entity), domain model, and UI model are always separate classes. Conversions go through mappers written as **extension functions** (e.g. `StoryDto.toDomain()`, `Story.toUiModel()`). Never pass a DTO/entity directly to the UI.
- **Offline-first**: local database (Room) is the single source of truth; remote data syncs into it. Reading works fully offline; audio gracefully degrades.
- **Audio**: TTS or pre-generated audio via Media3 — decision to be recorded when made.
- **Premium state**: a single subscription/entitlement source of truth that all gating checks go through (never scatter `isPremium` checks against raw storage).
- **AI feature**: server-side or direct API call — decision + prompt design to be recorded when made.
- **Observability**: Firebase Analytics + Crashlytics (or equivalent) — log key funnel events: content viewed, listen started, paywall shown, subscription started, AI feature used.

## Commands

All commands run from the project root with the Gradle wrapper:

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on a connected device/emulator
./gradlew test                   # Run local unit tests (app/src/test)
./gradlew connectedAndroidTest   # Run instrumented tests (needs device/emulator)
./gradlew lint                   # Run Android Lint
```

Run a single unit test class:

```bash
./gradlew test --tests "com.example.nativeminds.ExampleUnitTest"
```

## Working With AI in This Repo

Part 2 of the case study evaluates *how AI was directed*. To support that:

- When a prompt/iteration leads to a notable decision, correction, or rejected AI suggestion, note it in README's "How I Worked With AI" section (brief, concrete examples — what was asked, what came back wrong, what was done instead).
- Prefer small, reviewable steps over large generated dumps; the author must be able to defend every line.
