# Implementation Plan: Listen To Story (On-Device Text-To-Speech)

**Branch**: `003-listen-tts-playback` | **Date**: 2026-08-14 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-listen-tts-playback/spec.md`

## Summary

Wire the existing (currently non-functional) `ListenPill` control in `:feature:reader` to real
on-device narration: Android's `TextToSpeech`, driven through a new `:core:audio` module that also
hosts a `MediaSessionService`-backed foreground service so playback survives backgrounding and is
controllable from a system media notification, per the user's "B" choice. `:feature:reader` only
ever talks to a `StoryNarrator` interface in `:core:domain`.

## Technical Context

**Language/Version**: Kotlin, existing project toolchain (Java 11, compileSdk/targetSdk 36, minSdk 24)

**Primary Dependencies**: `android.speech.tts.TextToSpeech` (platform, no new dep); **new**:
`androidx.media3:media3-session` (foreground service + `MediaSession` + system notification for
background playback controls — the Media3 decision CLAUDE.md flagged as pending)

**Storage**: N/A — narration position is in-memory only for the current screen visit (per spec)

**Testing**: JVM unit tests for the reducer/state-machine pieces (`StoryNarrator` state transitions,
`ReaderReducer` additions); no new instrumented tests planned beyond manual verification of the
foreground service/notification (not practically unit-testable)

**Target Platform**: Android (existing app)

**Project Type**: mobile-app (existing multi-module Android project)

**Performance Goals**: narration starts audibly within 1s of tap (SC-001)

**Constraints**: offline-capable (no network dependency), resume within one sentence of pause point
(SC-002), narration must respect existing premium/entitlement gating (FR-010)

**Scale/Scope**: single active narration session per app process; one new module, changes confined
to `:core:domain`, `:core:audio` (new), `:feature:reader`, `:app` DI wiring

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Clean Architecture / model separation**: `StoryNarrator` + `NarrationState` are defined in
  `:core:domain` as plain Kotlin; `:feature:reader` depends only on that interface, never on
  `:core:audio` directly — same shape as `EntitlementRepository`. PASS.
- **MVI, single mutation path**: narration state folds into `ReaderUiState` as an intent
  (`NarrationStateChanged`), not a ViewModel-side branch; pause/resume/listen stay a single
  `ListenClicked`-style intent set handled only by the reducer. PASS.
- **Hilt-only DI**: `StoryNarrator` bound via `@Binds` in `:core:audio`'s `di/` package, no service
  locators. PASS.
- **New module** (`:core:audio`): not in CLAUDE.md's current module list — flagged below in
  Complexity Tracking since it's a real architectural addition, not boilerplate.

## Project Structure

### Documentation (this feature)

```text
specs/003-listen-tts-playback/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
```

### Source Code (repository root)

Only the paths that change or are added:

```text
core/domain/src/main/kotlin/com/example/nativeminds/domain/
├── model/NarrationState.kt          # NEW: Idle / Playing(position) / Paused(position)
├── narration/StoryNarrator.kt       # NEW: start/pause/resume/stop + Flow<NarrationState>
└── usecase/ObserveNarrationUseCase.kt   # NEW, thin — mirrors ObserveStoryDetailUseCase shape

core/audio/                          # NEW MODULE
├── build.gradle.kts
└── src/main/java/com/example/nativeminds/audio/
    ├── TextToSpeechNarrator.kt      # StoryNarrator impl, sentence-chunked utterances
    ├── NarrationSessionService.kt   # MediaSessionService (media3-session), foreground notification
    └── di/AudioModule.kt            # @Binds StoryNarrator, @Provides TextToSpeech

feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/
├── ReaderContract.kt                # CHANGED: ListenClicked → richer intents, narration state in ReaderUiState
├── ReaderReducer.kt                 # CHANGED: fold NarrationState into progress/pill state
├── ReaderViewModel.kt               # CHANGED: observe StoryNarrator, stop() on onCleared
└── components/ListenPill.kt         # CHANGED: three visual states (Listen/Pause/Resume)

app/src/main/AndroidManifest.xml     # CHANGED: register NarrationSessionService + POST_NOTIFICATIONS/FOREGROUND_SERVICE_MEDIA_PLAYBACK permissions
gradle/libs.versions.toml            # CHANGED: add media3 version + media3-session alias
settings.gradle.kts                  # CHANGED: include(":core:audio")
```

**Structure Decision**: New `:core:audio` module (concrete, Android-facing) sits alongside
`:core:data`/`:core:database` — depended on only by `:app` (composition root) and bound to the
`:core:domain` `StoryNarrator` interface, matching the existing repository-implementation pattern.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|---------------------------------------|
| New `:core:audio` module | TTS + `MediaSessionService` + foreground-service notification is Android-platform code that cannot live in `:core:domain` (pure Kotlin) and doesn't semantically belong in `:core:data` (repositories/remote sources only, per constitution) | Putting it in `:core:data` would make that module's purpose ambiguous and mix "sync data" concerns with "play audio" concerns; putting it in `:feature:reader` would violate the rule that features never hold concrete platform implementations and would block reuse if another screen ever narrates |
