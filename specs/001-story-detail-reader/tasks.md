---

description: "Task list for Story Detail (Reader) With Premium Unlock Sheet"
---

# Tasks: Story Detail (Reader) With Premium Unlock Sheet

**Input**: Design documents from `/specs/001-story-detail-reader/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/)

**Tests**: Included. Not optional here — the constitution requires unit tests for domain and
repository logic carrying business rules (gating, offline fallback) plus a small number of Compose
tests, and each contract file lists its own test obligations.

**Organization**: Tasks are grouped by user story so each story can be implemented, tested and
demonstrated on its own.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Every task names the exact file path it touches

## Path Conventions

Multi-module Android project. Module roots: `app/`, `core/{model,domain,data,database,common,designsystem}/`,
`feature/{home,reader}/`. Kotlin sources live under `src/main/java/…` except `:core:model` and
`:core:domain`, which use `src/main/kotlin/…`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Get the new module and its dependencies in place before any code is written

- [X] T001 Add `navigation-compose`, the Kotlin serialization plugin and `kotlinx-serialization-json` to `gradle/libs.versions.toml`, pinning each to the current stable version (do not guess — check, then verify with a build)
- [X] T002 Create the `:feature:reader` module: `feature/reader/build.gradle.kts` mirroring `feature/home/build.gradle.kts` (android library + compose + ksp + hilt, deps on `:core:designsystem`, `:core:model`, `:core:domain`), `feature/reader/src/main/AndroidManifest.xml`, and register it in `settings.gradle.kts`
- [X] T003 [P] Add the serialization plugin and navigation dependency to `app/build.gradle.kts` and `feature/home/build.gradle.kts`, and add `implementation(project(":feature:reader"))` to `app/build.gradle.kts`
- [X] T004 [P] Add reader-specific design tokens to `core/designsystem/src/main/java/com/example/nativeminds/designsystem/theme/Type.kt`, `Spacing.kt` and `NativeMindsColors.kt` (reading measure, drop cap, paragraph spacing, body-fade height, unlock-sheet surface) so no composable inlines a value later
- [X] T005 Run `./gradlew assembleDebug` to confirm the new module and dependencies resolve before any feature code exists

**Checkpoint**: The project builds with an empty `:feature:reader` module on the graph

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Storage, domain contracts and their implementations that every user story needs

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database

- [X] T006 [P] Create `core/database/src/main/java/com/example/nativeminds/database/StoryContentEntity.kt` — table `story_content`, `storyId` primary key, `author`, `body`, foreign key to `stories` with `ON DELETE CASCADE` and an index, per [data-model.md](data-model.md)
- [X] T007 [P] Create `core/database/src/main/java/com/example/nativeminds/database/StoryContentDao.kt` with `observeContent(storyId): Flow<StoryContentEntity?>`, `upsert`, `upsertAll`
- [X] T008 Add `observeStory(id: Long): Flow<StoryEntity?>` to `core/database/src/main/java/com/example/nativeminds/database/StoryDao.kt`
- [X] T009 Bump `NativeMindsDatabase` to version 2 in `core/database/src/main/java/com/example/nativeminds/database/NativeMindsDatabase.kt`, register `StoryContentEntity` and `storyContentDao()`, and write the explicit `MIGRATION_1_2`
- [X] T010 Wire the migration and the new DAO in `core/database/src/main/java/com/example/nativeminds/database/di/DatabaseModule.kt`, and commit the regenerated schema under `core/database/schemas/`
- [X] T011 [P] Add `androidx.room:room-testing` to `core/database/build.gradle.kts` `androidTestImplementation` and write the 1→2 migration test in `core/database/src/androidTest/java/com/example/nativeminds/database/MigrationTest.kt`
- [X] T012 [P] Write `core/database/src/androidTest/java/com/example/nativeminds/database/StoryContentDaoTest.kt` proving `observeContent` emits `null` then re-emits after an upsert

### Domain contracts

- [X] T013 [P] Create `core/model/src/main/kotlin/com/example/nativeminds/model/StoryContent.kt` (`storyId`, `author`, `paragraphs`)
- [X] T014 [P] Create `core/domain/src/main/kotlin/com/example/nativeminds/domain/repository/EntitlementRepository.kt` with `isPremium(): Flow<Boolean>`
- [X] T015 [P] Create `core/domain/src/main/kotlin/com/example/nativeminds/domain/observability/AnalyticsLogger.kt` and `ErrorReporter.kt` per [contracts/domain-contracts.md](contracts/domain-contracts.md)
- [X] T016 [P] Create `core/domain/src/main/kotlin/com/example/nativeminds/domain/model/ReaderAccess.kt` (`Full`, `Preview`) and `ReaderDetail.kt` (`Loading`, `Available`, `Unavailable(UnavailableReason)`)
- [X] T017 Extend `core/domain/src/main/kotlin/com/example/nativeminds/domain/repository/StoryRepository.kt` with `story(id)`, `storyContent(id)` and `suspend refreshContent(id)`, documenting in KDoc that `refreshContent` throws rather than swallowing

### Data implementations

- [X] T018 [P] Create `core/data/src/main/java/com/example/nativeminds/data/mapper/StoryContentMappers.kt` — `StoryContentEntity.toDomain()` splitting the body on blank lines, trimming and dropping empties, and `StoryContent.toEntity()`
- [X] T019 Implement `story`, `storyContent` and `refreshContent` in `core/data/src/main/java/com/example/nativeminds/data/RoomStoryRepository.kt`, keeping reads off the network and the suspend work on `@IoDispatcher`
- [X] T020 [P] Add `fetchContent(storyId): StoryContent` to `core/data/src/main/java/com/example/nativeminds/data/remote/RemoteStoryDataSource.kt` and implement it in `FakeRemoteStoryDataSource.kt`, throwing when the story is unknown
- [X] T021 [P] Create `core/data/src/main/java/com/example/nativeminds/data/MockEntitlementRepository.kt` — `@Singleton`, `MutableStateFlow(false)`, per [research.md](research.md) R6
- [X] T022 [P] Create `core/data/src/main/java/com/example/nativeminds/data/observability/LogcatAnalyticsLogger.kt` and `LogcatErrorReporter.kt`, neither of which may throw
- [X] T023 Bind `EntitlementRepository`, `AnalyticsLogger` and `ErrorReporter` in `core/data/src/main/java/com/example/nativeminds/data/di/DataModule.kt`
- [X] T024 Add an author and a full body to every story in `core/data/src/main/java/com/example/nativeminds/data/local/DummyStorySeed.kt`, including the design's two verbatim stories, and write both tables in one transaction from `RoomStoryRepository.syncIfNeeded()`

### Navigation shell

- [X] T025 [P] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/navigation/ReaderRoute.kt` — `@Serializable data class ReaderRoute(val storyId: Long)` plus `fun NavGraphBuilder.readerScreen(onBack: () -> Unit)`
- [X] T026 [P] Create `feature/home/src/main/java/com/example/nativeminds/feature/home/navigation/HomeRoute.kt` — `@Serializable data object HomeRoute` plus `fun NavGraphBuilder.homeScreen(onStoryClick: (Long) -> Unit)`
- [X] T027 Add `onStoryClick: (Long) -> Unit` to `HomeScreen` and `HomeScreenContent` in `feature/home/src/main/java/com/example/nativeminds/feature/home/ui/HomeScreen.kt` and pass it to each `StoryCard` — a plain callback, not a `HomeIntent` (see [research.md](research.md) R2)
- [X] T028 Create `app/src/main/java/com/example/nativeminds/navigation/NativeMindsNavHost.kt` and switch `app/src/main/java/com/example/nativeminds/MainActivity.kt` to host it instead of calling `HomeScreen` directly, keeping the existing theme and edge-to-edge wrapping

**Checkpoint**: Content is stored and readable through the domain contracts; the graph has two
destinations and the app still runs

---

## Phase 3: User Story 1 - Open a free story and read it (Priority: P1) 🎯 MVP

**Goal**: Tapping a story card opens a reading screen filled with that story's real content, and
back returns to an unchanged Home.

**Independent Test**: Tap any unlocked story on Home, confirm the reader shows its category,
minutes, title, author, cover and full body, scroll to the end, press back and confirm Home's
query, category chip and scroll position survived.

### Tests for User Story 1

- [X] T029 [P] [US1] Write `core/domain/src/test/kotlin/com/example/nativeminds/domain/usecase/ObserveStoryDetailUseCaseTest.kt` covering the full-access path and `Unavailable(STORY_MISSING)` per [contracts/domain-contracts.md](contracts/domain-contracts.md)
- [X] T030 [P] [US1] Write `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderReducerTest.kt` for `DetailChanged`, `ScrollProgressChanged` clamping, and `ListenClicked` returning exactly `ShowAudioUnavailable` with state untouched
- [X] T031 [P] [US1] Write `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderViewModelTest.kt` proving detail emissions reach state only through `onIntent` and that the ViewModel contains no `when (intent)`

### Implementation for User Story 1

- [X] T032 [P] [US1] Create `core/domain/src/main/kotlin/com/example/nativeminds/domain/usecase/ObserveStoryDetailUseCase.kt` emitting `Loading`, `Available(Full)` and `Unavailable` — entitlement is not consulted yet, so every story reads in full
- [X] T033 [P] [US1] Create `core/domain/src/main/kotlin/com/example/nativeminds/domain/usecase/RefreshStoryContentUseCase.kt`, reporting every failure through `ErrorReporter` before returning it
- [X] T034 [P] [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/model/ReaderStoryUiModel.kt` and `ReaderBodyUiModel.kt`
- [X] T035 [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/mapper/ReaderMappers.kt` — `ReaderAccess.toUiModel()` and the minutes/category formatting, as extension functions
- [X] T036 [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderContract.kt` — `ReaderUiState` (irreducible fields only, computed properties for the rest), `ReaderIntent`, `ReaderEffect`, `Reduction`, per [contracts/reader-mvi.md](contracts/reader-mvi.md)
- [X] T037 [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderReducer.kt` — a pure top-level `ReaderUiState.reduce(intent): Reduction`, the only writer of state
- [X] T038 [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderViewModel.kt` — `storyId` from `SavedStateHandle`, the state-driven load key with `distinctUntilChanged().flatMapLatest`, detail folded back in as an intent, effects on a `Channel(BUFFERED)`, and a branch-free `onIntent`
- [X] T039 [P] [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/preview/ReaderPreviewData.kt` — fixtures and the `PreviewParameterProvider`s for every reader state, with no ViewModel or database dependency
- [X] T040 [P] [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/ReaderTopBar.kt` (back, truncating title, overflow) with its `@ThemePreviews` preview at the bottom of the file
- [X] T041 [P] [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/ReaderBody.kt` — a `LazyColumn` of paragraphs keyed by index, drop cap on the first paragraph, header/cover/footer as items, with previews
- [X] T042 [P] [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/ListenPill.kt` — the footer control with progress and remaining labels, plain callbacks, with previews
- [X] T043 [US1] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderScreen.kt` — the Hilt wrapper collecting state and effects with `flowWithLifecycle`, the stateless `ReaderScreenContent(state, onIntent, onBack)`, and `@ScreenThemePreviews` previews at the bottom of the file
- [X] T044 [US1] Feed reading progress from the list state into `ScrollProgressChanged` via `snapshotFlow`, mapped to whole percent and `distinctUntilChanged()` before it becomes an intent
- [X] T045 [P] [US1] Add every reader string to `feature/reader/src/main/res/values/strings.xml` — no literal in a composable
- [X] T046 [US1] Log `content_viewed` through `AnalyticsLogger` from the detail flow in `ReaderViewModel`, keyed so a re-emission cannot double-count one open
- [X] T047 [US1] Extend `app/src/androidTest/java/com/example/nativeminds/HomeGraphTest.kt` (or add `ReaderNavigationTest.kt` beside it) with the journeys in [contracts/navigation.md](contracts/navigation.md): card tap shows the title in the reader, and back restores a typed query

**Checkpoint**: US1 is a complete, demonstrable slice — every story opens and reads in full

---

## Phase 4: User Story 2 - Hit the premium wall on a locked story (Priority: P2)

**Goal**: A premium story opened without an entitlement shows a bounded preview that fades out,
with the unlock bottom sheet over it; a subscriber sees the same story in full.

**Independent Test**: Open the seeded premium story as a free user — the preview is readable, the
rest is unreachable, and the sheet appears with its benefits and call to action. Flip the
entitlement source and reopen — the full body renders with no sheet.

### Tests for User Story 2

- [X] T048 [P] [US2] Extend `core/domain/src/test/kotlin/com/example/nativeminds/domain/usecase/ObserveStoryDetailUseCaseTest.kt` with the gating matrix from [contracts/domain-contracts.md](contracts/domain-contracts.md): locked+free → `Preview`, locked+premium → `Full`, unlocked+free → `Full`, and an entitlement flip re-emitting without resubscribing
- [X] T049 [P] [US2] Write `core/domain/src/test/kotlin/com/example/nativeminds/domain/usecase/PreviewShareTest.kt` asserting the preview is the shortest whole-paragraph prefix reaching 30% of the body's characters, is never the whole body, and is at least one paragraph for a single-paragraph story
- [X] T050 [P] [US2] Extend `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderReducerTest.kt`: a restricted `DetailChanged` opens the sheet, a second one does not reopen a dismissed sheet, `SubscribeClicked` returns exactly `ShowSubscriptionUnavailable` with state untouched, and a `Full` detail clears the restricted state

### Implementation for User Story 2

- [X] T051 [US2] Add the preview rule to `core/domain/src/main/kotlin/com/example/nativeminds/domain/usecase/ObserveStoryDetailUseCase.kt` — combine `EntitlementRepository.isPremium()`, and return `ReaderAccess.Preview` with the computed paragraphs and `freeSharePercent` so the withheld text never leaves the domain layer
- [X] T052 [P] [US2] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/PremiumUnlockSheet.kt` — a Material 3 `ModalBottomSheet` with the lock medallion, heading, free-share line, three benefit rows, primary action, price and cancellation line, plain callbacks, and `@ThemePreviews` previews at the bottom of the file
- [X] T053 [US2] Add the PREMIUM badge to the reader header and the body fade for the restricted case in `ReaderBody.kt`, driven by `ReaderBodyUiModel.isTruncated` — the composable never receives the withheld paragraphs
- [X] T054 [US2] Present, dismiss and re-present the sheet from `ReaderScreenContent` in `ReaderScreen.kt`, including the persistent unlock control that remains after dismissal (FR-009)
- [X] T055 [P] [US2] Add the unlock-sheet strings to `feature/reader/src/main/res/values/strings.xml`, including the "subscription not available yet" message
- [X] T056 [US2] Log `paywall_shown` through `AnalyticsLogger` when a restricted detail first arrives, and add the restricted flag to `content_viewed`
- [X] T057 [US2] Extend the restricted-state previews in `ReaderPreviewData.kt` and `ReaderScreen.kt` so the light/dark pair renders both access states from one `PreviewParameterProvider`
- [X] T058 [US2] Add a Compose test in `app/src/androidTest/java/com/example/nativeminds/ReaderGatingTest.kt` covering the free path (preview + sheet + "not available yet" on subscribe) and the subscriber path (full body, no sheet) by driving the entitlement source directly

**Checkpoint**: US1 and US2 both work independently; gating is provable without a device for the
rules and with one for the screen

---

## Phase 5: User Story 3 - Read what is already downloaded, with no network (Priority: P3)

**Goal**: Stored stories read fully offline; a story with no stored content shows an offline
message with retry instead of a blank page.

**Independent Test**: Open a story with network, disable the network, reopen it and confirm it
renders; open a story whose content was never stored and confirm the offline state with retry, then
restore the network and confirm retry fills the body.

### Tests for User Story 3

- [X] T059 [P] [US3] Write `core/data/src/test/java/com/example/nativeminds/data/RoomStoryRepositoryContentTest.kt` proving `storyContent` reads never touch the network and that `refreshContent` throws when offline rather than returning empty content
- [X] T060 [P] [US3] Extend `core/domain/src/test/kotlin/com/example/nativeminds/domain/usecase/ObserveStoryDetailUseCaseTest.kt` with absent-content-while-offline → `Unavailable(OFFLINE)` and refresh failure → `Unavailable(ERROR)`, each reported through a fake `ErrorReporter`
- [X] T061 [P] [US3] Extend `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderReducerTest.kt` with `RetryRequested` returning to `Loading` and incrementing `retryToken`, and `ReaderViewModelTest.kt` with retry resubscribing the detail flow exactly once

### Implementation for User Story 3

- [X] T062 [US3] Map absent content to `OFFLINE` / `ERROR` / `STORY_MISSING` in `ObserveStoryDetailUseCase.kt` and trigger `RefreshStoryContentUseCase` when content is missing, consulting `NetworkMonitor` through the data layer rather than from the feature module
- [X] T063 [P] [US3] Create `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/ReaderUnavailableState.kt` — one composable rendering the offline, error and story-missing cases with a retry control, with previews for all three
- [X] T064 [US3] Render the unavailable state from `ReaderScreenContent` and wire retry to `ReaderIntent.RetryRequested` in `ReaderScreen.kt`
- [X] T065 [P] [US3] Add the offline, error and story-missing strings to `feature/reader/src/main/res/values/strings.xml`
- [X] T066 [US3] Confirm every content-resolution failure reaches `ErrorReporter` in addition to the screen — no `runCatching` that ends in a bare fallback anywhere on this path

**Checkpoint**: All three stories are independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T067 [P] Record the Key Decisions from [research.md](research.md) in `README.md`: the reducer's `Reduction` return, navigation as a callback rather than an intent, the separate content table, the domain-side preview rule, and the two new dependencies
- [X] T068 [P] Record the Cut Corners in `README.md`: in-memory mock entitlement, logcat-only observability, and the deliberately unavailable subscribe and listen actions
- [X] T069 [P] Add a "How I Worked With AI" note in `README.md` covering the corrections this feature drove — navigation-as-intent rejected, and the reducer signature chosen over a branching ViewModel
- [X] T070 Update the Architecture section of `CLAUDE.md` with the `:feature:reader` module, the navigation ownership split, and the `Reduction` pattern, so the living section stays current
- [X] T071 Verify every new composable has a preview and that no `@Preview` matrix is hand-written — themes from `@ThemePreviews`/`@ScreenThemePreviews`, states from a `PreviewParameterProvider`
- [X] T072 Grep the feature's sources for `//` comments, hardcoded colors, `dp`/`sp` literals and string literals in composables, and remove every hit
- [X] T073 Run `./gradlew test lint` and fix everything it reports
- [X] T074 Run `./gradlew :core:database:connectedDebugAndroidTest :app:connectedDebugAndroidTest`
- [X] T075 Walk through [quickstart.md](quickstart.md) section 4 on a device and confirm every row

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Needs Setup — **blocks every user story**
- **US1 (Phase 3)**: Needs Foundational
- **US2 (Phase 4)**: Needs Foundational; extends US1's use case and screen, so in practice it
  follows US1 rather than running beside it
- **US3 (Phase 5)**: Needs Foundational; touches the same use case and screen as US1/US2
- **Polish (Phase 6)**: Needs the stories you intend to ship

### User Story Dependencies

- **US1 (P1)**: Independent once Foundational is done. This is the MVP.
- **US2 (P2)**: Independently *testable* — its gating rules are pure domain tests — but it edits
  `ObserveStoryDetailUseCase`, `ReaderBody` and `ReaderScreen`, which US1 creates. Sequence it
  after US1 unless you are willing to merge the same files twice.
- **US3 (P3)**: Same situation — independently testable, but it edits the same use case and screen.

### Within Each User Story

- Tests before the implementation they describe
- Domain models before use cases; use cases before the ViewModel; the ViewModel before the screen
- Contract → reducer → ViewModel → screen, never the other way round
- Strings and previews land with the composable that needs them, not afterwards

### Parallel Opportunities

- Setup: T003 and T004 in parallel after T002
- Foundational: the three groups (database T006–T012, domain contracts T013–T016, navigation shell
  T025–T026) are independent; within them every `[P]` task touches a different file. T017 gates
  T019; T019 gates T024
- US1: T029–T031 in parallel; T032–T034 in parallel; T039–T042 in parallel; T045 any time
- US2: T048–T050 in parallel; T052 and T055 in parallel with T051
- US3: T059–T061 in parallel; T063 and T065 in parallel with T062
- Polish: T067–T069 in parallel (different README sections — merge carefully)

---

## Parallel Example: User Story 1

```text
After Foundational completes, start these three together:
  T029  ObserveStoryDetailUseCaseTest      (:core:domain)
  T030  ReaderReducerTest                  (:feature:reader)
  T031  ReaderViewModelTest                (:feature:reader)

Then these three together:
  T032  ObserveStoryDetailUseCase
  T033  RefreshStoryContentUseCase
  T034  Reader UI models

Then the components, once the contract (T036) exists:
  T040  ReaderTopBar
  T041  ReaderBody
  T042  ListenPill
  T039  ReaderPreviewData
```

---

## Implementation Strategy

**MVP = Phase 1 + Phase 2 + Phase 3 (US1)**. That already delivers a working second screen: tap a
card, read the whole story, come back to an unchanged Home — the case study's "stories can be read"
criterion, end to end, with content served from Room.

**Increment 2 = Phase 4 (US2)** turns the screen into the gating demonstration: preview, fade,
unlock sheet, and the honest "not available yet" answer on the subscribe action.

**Increment 3 = Phase 5 (US3)** makes the offline promise visible rather than incidental.

Ship each increment as its own commit set, and run `./gradlew test lint` at every checkpoint — a
phase is not done while the previous phase's tests are red.
