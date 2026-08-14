---

description: "Task list for Listen To Story (On-Device Text-To-Speech)"
---

# Tasks: Listen To Story (On-Device Text-To-Speech)

**Input**: Design documents from `/specs/003-listen-tts-playback/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/story-narrator.md, quickstart.md

**Tests**: Included only where the constitution requires them (domain/business-rule logic:
`ReaderReducer`, sentence splitting) — not for platform-only code (TTS/notification wiring) that
can't be meaningfully unit tested.

## Format: `[ID] [P?] [Story] Description`

## Phase 1: Setup

- [X] T001 [P] Add `media3` version + `androidx-media3-session` alias to `gradle/libs.versions.toml`
- [X] T002 Create `:core:audio` module skeleton (`core/audio/build.gradle.kts`,
      `core/audio/src/main/AndroidManifest.xml`) and add `include(":core:audio")` to
      `settings.gradle.kts`
- [X] T003 [P] Add `implementation(project(":core:audio"))` to `app/build.gradle.kts`

**Checkpoint**: Module compiles empty; dependency graph in place.

---

## Phase 2: Foundational (Blocking Prerequisites)

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T004 [P] Create `NarrationState` sealed interface (`Idle`, `Playing`, `Paused`,
      `Unavailable`) in
      `core/domain/src/main/kotlin/com/example/nativeminds/domain/model/NarrationState.kt`
- [X] T005 [P] Create `NarrationUnavailableReason` enum (`ENGINE_MISSING`,
      `LANGUAGE_UNSUPPORTED`) in
      `core/domain/src/main/kotlin/com/example/nativeminds/domain/model/NarrationUnavailableReason.kt`
- [X] T006 Create `StoryNarrator` interface (`start`/`pause`/`resume`/`stop`/`state`) in
      `core/domain/src/main/kotlin/com/example/nativeminds/domain/narration/StoryNarrator.kt`
      (depends on T004)
- [X] T007 Create `ObserveNarrationUseCase` in
      `core/domain/src/main/kotlin/com/example/nativeminds/domain/usecase/ObserveNarrationUseCase.kt`
      (depends on T006)
- [X] T008 [P] Implement sentence-splitting utility with unit test in
      `core/audio/src/main/java/com/example/nativeminds/audio/SentenceSplitter.kt` and
      `core/audio/src/test/java/com/example/nativeminds/audio/SentenceSplitterTest.kt`
- [X] T009 Implement `TextToSpeechNarrator` (`StoryNarrator` impl: TTS init/language check,
      sentence-queue playback via T008, `ErrorReporter` call on `Unavailable`) in
      `core/audio/src/main/java/com/example/nativeminds/audio/TextToSpeechNarrator.kt` (depends
      on T006, T008)
- [X] T010 Implement `NarrationSessionService` (`MediaSessionService` hosting a `SimpleBasePlayer`
      wrapper around `TextToSpeechNarrator`, foreground notification, audio-focus pause) in
      `core/audio/src/main/java/com/example/nativeminds/audio/NarrationSessionService.kt`
      (depends on T009)
- [X] T011 Create `AudioModule` Hilt DI (`@Binds StoryNarrator`, `@Provides TextToSpeech`) in
      `core/audio/src/main/java/com/example/nativeminds/audio/di/AudioModule.kt` (depends on T009)
- [X] T012 Register `NarrationSessionService` and add
      `FOREGROUND_SERVICE_MEDIA_PLAYBACK`/`POST_NOTIFICATIONS` permissions in
      `app/src/main/AndroidManifest.xml` (depends on T010)

**Checkpoint**: Foundation ready — user story implementation can begin.

---

## Phase 3: User Story 1 - Listen to a story from the start (Priority: P1) 🎯 MVP

**Goal**: Tapping "Listen" starts on-device narration from the beginning, fully offline, and stops
cleanly at the end of the text.

**Independent Test**: Airplane mode on, open an unlocked story, tap "Listen" → narration audibly
starts within ~1s; let it play to the end → control returns to its initial state (spec SC-001).

- [X] T013 [US1] Add narration fields/intents (`NarrationStateChanged`, pill visual state) to
      `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderContract.kt`
- [X] T014 [US1] Fold `NarrationStateChanged` (Idle/Playing/end-of-story→Idle) into
      `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderReducer.kt`,
      with unit test additions in
      `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderReducerTest.kt`
- [X] T015 [US1] Wire `ReaderViewModel.kt` to inject `ObserveNarrationUseCase`/`StoryNarrator`,
      call `start()` with the entitlement-limited body text (FR-010) on `ListenClicked` while
      idle, and fold narrator state back in as `NarrationStateChanged` in
      `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderViewModel.kt`
- [X] T016 [US1] Update
      `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/ListenPill.kt`
      to render Idle/Playing visual states (icon + label swap) and update its `@ThemePreviews`
      matrix
- [X] T017 [US1] Wire `ReaderScreen.kt` to pass the new narration state/intents through to
      `ListenPill` in
      `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderScreen.kt`

**Checkpoint**: User Story 1 is fully functional and independently testable (offline start, plays
to completion).

---

## Phase 4: User Story 2 - Pause and resume without losing place (Priority: P1)

**Goal**: Pausing preserves the exact sentence position; resuming continues from there instead of
restarting.

**Independent Test**: Start narration, let part of it play, pause, wait, resume → audio continues
at/near the paused sentence rather than from the start (spec SC-002).

- [X] T018 [US2] Extend `ListenClicked` handling in `ReaderViewModel.kt` to call `pause()` while
      Playing and `resume()` while Paused (toggle behavior)
- [X] T019 [US2] Extend `ReaderReducer.kt` to map `Paused` to the pill's Resume visual state, with
      unit test additions in `ReaderReducerTest.kt`
- [X] T020 [US2] Add the Paused/Resume visual state to `ListenPill.kt` and its preview matrix
      (Idle/Playing/Paused together, per design-system rules)
- [X] T021 [US2] Verify/implement audio-focus interruption pause behavior (FR-012) in
      `core/audio/src/main/java/com/example/nativeminds/audio/NarrationSessionService.kt`

**Checkpoint**: User Stories 1 and 2 both work independently (start, pause, resume, survive an
interruption).

---

## Phase 5: User Story 3 - Leaving the screen resets playback (Priority: P2)

**Goal**: Navigating away from the detail screen stops narration and discards its position;
returning to the screen always starts fresh.

**Independent Test**: Start/pause narration, navigate back to the list, reopen the same story →
control shows initial "Listen" state; tapping starts from the beginning (spec SC-003).

- [X] T022 [US3] Call `StoryNarrator.stop()` from `ReaderViewModel.onCleared()` in
      `ReaderViewModel.kt`
- [X] T023 [US3] Guard `ReaderViewModel` init against a narrator already bound to a different
      `storyId` (defensive `stop()`) in `ReaderViewModel.kt`
- [X] T024 [US3] Unit test: a freshly created `ReaderUiState` always reflects `NarrationState.Idle`
      in `ReaderReducerTest.kt`

**Checkpoint**: All three user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T025 [P] Request the `POST_NOTIFICATIONS` runtime permission at narration start on
      Android 13+ (wherever the reader screen's permission flow belongs, `feature/reader` or `app`)
- [X] T026 Add `reader_pause`/`reader_resume`/`reader_audio_unavailable` string resources to
      `feature/reader/src/main/res/values/strings.xml`
- [X] T027 [P] Record the Media3 + on-device TTS decision in CLAUDE.md's "Audio" architecture line
      and in the README "Key Decisions" section (constitution decision-logging rule)
- [X] T028 [P] Log cut corners (single active narration session, no persisted/cross-session
      position, no in-app mini-player) in the README "Cut Corners / Assumptions" section
- [ ] T029 Run all six `quickstart.md` scenarios manually and confirm

---

## Dependencies & Execution Order

- **Setup (Phase 1)** → **Foundational (Phase 2)**: strictly blocking, no user story starts before
  T004–T012 are done.
- **US1 (Phase 3)**: first to implement — MVP. No dependency on US2/US3.
- **US2 (Phase 4)** and **US3 (Phase 5)**: independently testable per spec, but both edit the same
  files US1 touches (`ListenPill.kt`, `ReaderReducer.kt`, `ReaderViewModel.kt`), so implement them
  sequentially after US1 rather than in parallel to avoid merge conflicts.
- **Polish (Phase 6)**: after all desired stories are complete.

### Parallel Opportunities

- T001, T003 (Setup) — different files.
- T004, T005, T008 (Foundational) — different files, no dependency between them.
- T025, T027, T028 (Polish) — different files/concerns.

## Implementation Strategy

**MVP = Phase 1 + 2 + 3 (US1)**: offline narration that starts from the beginning and plays to
completion, with the "Listen" control reflecting real state. Ship/demo this before adding
pause/resume (US2) and the screen-reset guarantee (US3).
