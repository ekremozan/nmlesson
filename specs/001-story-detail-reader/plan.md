# Implementation Plan: Story Detail (Reader) With Premium Unlock Sheet

**Branch**: `001-story-detail-reader` | **Date**: 2026-08-13 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-story-detail-reader/spec.md`

## Summary

Add the app's second screen: a reading destination opened from a home story card. It renders the
design's Reader screens — full access (2a) for unlocked stories and subscribers, restricted (2b) for
premium stories opened without an entitlement, where the body fades after a 30% preview and a
premium unlock **bottom sheet** rises over it.

Technically this means four things the app does not have yet: a navigation host carrying a story id
from home to reader, story *content* (author + body) in the local store as a separate table from
list metadata, a single entitlement source of truth the gating decision reads, and an observability
seam so content failures reach error reporting and the paywall reaches analytics. The reader itself
follows the project's MVI shape, with one extension: its reducer returns state **and** effects, so
the ViewModel stays a pure dispatcher that never branches on an intent.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Java 11 toolchain

**Primary Dependencies**: Jetpack Compose (BOM 2026.02.01) + Material 3, Hilt 2.60.1, Room 2.8.4,
Paging 3.5.0, kotlinx.coroutines 1.10.2. **New**: `androidx.navigation:navigation-compose` and the
Kotlin serialization plugin + `kotlinx-serialization-json` (type-safe routes only) — both justified
in [research.md](research.md).

**Storage**: Room (`NativeMindsDatabase`), schema exported to `core/database/schemas`. This feature
takes it from version 1 to version 2 with a written migration.

**Testing**: JUnit4 + `kotlinx-coroutines-test` for domain/reducer/ViewModel units;
`androidx.room:room-testing` for the migration; Compose UI tests in `:app` `androidTest` for the
navigation and gating journeys.

**Target Platform**: Android, minSdk 24 / targetSdk 36, phone form factor

**Project Type**: Multi-module Android app (Clean Architecture + MVI)

**Performance Goals**: Header visible <1s from tap, body filled <2s when stored locally (SC-001);
no perceptible stutter scrolling a full-length story (SC-006)

**Constraints**: Fully readable offline for previously retrieved stories; no hardcoded colors,
sizes, text styles or UI strings; no `//` comments; no silently swallowed failures

**Scale/Scope**: One new feature module, one new screen plus a bottom sheet, one DB migration,
~20 seeded stories

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate (constitution) | Verdict | How this plan satisfies it |
|---|---|---|
| I. Defensible Decisions | PASS | Debatable choices are in [research.md](research.md); cut corners go to README. |
| II. Clean Architecture & Model Separation | PASS | `:feature:reader` → `:core:domain` only; entity/domain/UI stay three classes with extension-function mappers; every dependency is `@Inject` + `@Binds`/`@Provides`. |
| III. MVI With A Single Mutation Path | PASS | `ReaderContract.kt` + pure `ReaderUiState.reduce(intent)` as the only writer (see R2 in research.md). External data folds in as intents; effects use `Channel(BUFFERED)`. |
| IV. Offline-First With Visible Failures | PASS | Room is the source of truth; offline/error are typed states with retry, not exceptions; every failure reports through `ErrorReporter`; gating goes through one `EntitlementRepository`. |
| V. Design System Is The Only Source Of Style | PASS | Built from the design's Reader 2a/2b screens; new tokens added to `:core:designsystem`; every composable previewed in both themes. |

**Post-Phase-1 re-check**: PASS. The design in [data-model.md](data-model.md) and
[contracts/](contracts/) introduced no violation. One deliberate deviation is tracked below.

## Project Structure

### Documentation (this feature)

```text
specs/001-story-detail-reader/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── domain-contracts.md
│   ├── navigation.md
│   └── reader-mvi.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Created by /speckit-tasks, not here
```

### Source Code (repository root)

```text
core/model/src/main/kotlin/com/example/nativeminds/model/
├── Story.kt                                  # unchanged
└── StoryContent.kt                           # NEW domain payload: author + paragraphs

core/domain/src/main/kotlin/com/example/nativeminds/domain/
├── repository/
│   ├── StoryRepository.kt                    # + story(id), storyContent(id), refreshContent(id)
│   └── EntitlementRepository.kt              # NEW single source of truth for premium
├── observability/
│   ├── AnalyticsLogger.kt                    # NEW seam
│   └── ErrorReporter.kt                      # NEW seam
├── model/
│   └── ReaderAccess.kt                       # NEW Full / Preview gating result
└── usecase/
    ├── ObserveStoryDetailUseCase.kt          # NEW story + content + entitlement → ReaderAccess
    └── RefreshStoryContentUseCase.kt         # NEW pull content when absent

core/database/src/main/java/com/example/nativeminds/database/
├── StoryContentEntity.kt                     # NEW 1:1 table, body kept out of the list query
├── StoryContentDao.kt                        # NEW observe/upsert
├── StoryDao.kt                               # + observeStory(id)
├── NativeMindsDatabase.kt                    # version 2 + migration
└── di/DatabaseModule.kt                      # + StoryContentDao provider

core/data/src/main/java/com/example/nativeminds/data/
├── RoomStoryRepository.kt                    # + content reads and refresh
├── MockEntitlementRepository.kt              # NEW in-memory premium state
├── observability/LogcatAnalyticsLogger.kt    # NEW stand-in implementations
├── observability/LogcatErrorReporter.kt
├── local/DummyStorySeed.kt                   # + seeded bodies and authors
├── mapper/StoryContentMappers.kt             # NEW entity ↔ domain
└── di/DataModule.kt                          # + new @Binds

core/designsystem/src/main/java/com/example/nativeminds/designsystem/theme/
├── Type.kt, Spacing.kt, NativeMindsColors.kt # + reader-specific tokens

feature/reader/src/main/java/com/example/nativeminds/feature/reader/
├── navigation/ReaderRoute.kt                 # route + NavGraphBuilder extension
└── ui/
    ├── ReaderContract.kt                     # state + intents + effects
    ├── ReaderReducer.kt                      # pure, returns state + effects
    ├── ReaderViewModel.kt
    ├── ReaderScreen.kt                       # wrapper + stateless content + previews
    ├── components/                           # ReaderTopBar, ReaderBody, ListenPill,
    │                                         # PremiumUnlockSheet, ReaderUnavailableState
    ├── model/                                # ReaderStoryUiModel, ParagraphUiModel
    ├── mapper/ReaderMappers.kt
    └── preview/ReaderPreviewData.kt          # fixtures + PreviewParameterProviders

feature/home/.../ui/HomeScreen.kt             # story card click routed out via onStoryClick

app/src/main/java/com/example/nativeminds/
├── MainActivity.kt                           # hosts the nav graph instead of HomeScreen
└── navigation/NativeMindsNavHost.kt          # NEW composition root of navigation

Tests
core/domain/src/test/…                        # gating + preview-share rules
core/database/src/androidTest/…               # content DAO + migration 1→2
feature/reader/src/test/…                     # reducer (pure) + ViewModel with fakes
app/src/androidTest/…                         # home → reader navigation, full vs restricted
```

**Structure Decision**: The existing multi-module layout is kept exactly as the constitution
defines it; this feature adds one `:feature:reader` module and extends the core modules it needs.
Navigation is composed in `:app` (the only module allowed to know every feature), while each
feature owns its own route declaration, so `:app` never reaches into a screen's internals and no
feature module ever depends on another.

## Complexity Tracking

> Filled because the Constitution Check flags one deliberate deviation to justify.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| The reader's reducer returns `Reduction(state, effects)` rather than the plain `State` that `HomeReducer` returns | The reader must raise one-shot effects (subscribe unavailable, listen unavailable) *in response to intents*. The constitution forbids the ViewModel branching on an intent, so the only place that can decide "this intent produces that effect" and stay pure is the reducer. | Branching on the intent inside the ViewModel — directly forbidden by Principle III. Emitting effects from the composable — moves a business decision into the UI and makes it untestable. Leaving Home's signature and adding a second, hidden mutation path — reintroduces exactly the dual-writer problem Principle III exists to prevent. |
| Story content lives in a second table rather than extra columns on `stories` | The list query is paged and runs on every keystroke; dragging multi-kilobyte bodies through it would defeat the paging work already done (Principle IV, "stays fast at scale"). | Adding `author`/`body` columns to `stories` — every `PagingSource` page would then load full bodies for rows the user only sees as a card. |

Both are recorded in README "Key Decisions" when implemented, per Principle I.
