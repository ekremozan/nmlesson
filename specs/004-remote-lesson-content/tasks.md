---

description: "Task list for Remote Lesson Content"
---

# Tasks: Remote Lesson Content

**Input**: Design documents from `/specs/004-remote-lesson-content/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/supabase-schema.md](./contracts/supabase-schema.md), [quickstart.md](./quickstart.md)

**Tests**: Included — the project constitution (Principle IV, Development Workflow gates) mandates
unit tests for domain/repository logic carrying business rules (sync, offline fallback), so these
are not optional here even though the spec didn't explicitly request TDD.

**Organization**: Tasks are grouped by user story (spec.md priorities) so each can be implemented,
tested, and demoed independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no unmet dependencies)
- **[Story]**: US1 / US2 / US3, mapping to spec.md's three user stories
- Every task names its exact file path

## Phase 1: Setup

**Purpose**: Get a real Supabase project + build wiring in place before any code depends on it.

- [X] T001 [P] Add `supabase-kt` (`postgrest`) and `ktor-client-okhttp` version + library entries to `gradle/libs.versions.toml`, per [research.md](./research.md) §1 — pinned to supabase-kt 3.2.6 / ktor 3.3.1 (built against Kotlin 2.2.x) instead of latest, since latest supabase-kt requires Kotlin 2.4 and this project is pinned to 2.2.10
- [X] T002 [P] Enable `buildFeatures.buildConfig = true` in `core/data/build.gradle.kts` and add `SUPABASE_URL` / `SUPABASE_ANON_KEY` `buildConfigField`s read from the git-ignored `local.properties` (same pattern the root project already uses for `sdk.dir`), per [research.md](./research.md) §3
- [X] T003 Supabase project created, `supabase/schema.sql` and `supabase/seed.sql` run (verified via REST: `lessons` and `lesson_content` both report 40/40 rows), `SUPABASE_URL`/`SUPABASE_ANON_KEY` (publishable key — confirmed compatible, `supabase-kt` treats the key as an opaque header value with no client-side format validation) added to local `local.properties`
- [X] T004 [P] Create `supabase/seed.sql` (and `supabase/schema.sql`), generating one `insert` per row from `DummyLessonSeed.lessons` and `DummyLessonContentSeed.content` via a throwaway JVM test (run once, then removed) — running it against the live project is part of T003

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The actual remote-fetch-and-replace sync engine. Every user story depends on this.

**⚠️ CRITICAL**: No user story task can be verified until this phase is complete.

- [X] T005 [P] Extract the existing paragraph-splitting logic in `core/data/src/main/java/com/example/nativeminds/data/mapper/LessonContentMappers.kt` into a small internal helper so it can be reused by the new DTO mapper, per [data-model.md](./data-model.md)
- [X] T006 [P] Create `LessonDto` (`@Serializable`) in `core/data/src/main/java/com/example/nativeminds/data/remote/dto/LessonDto.kt`, per [data-model.md](./data-model.md)
- [X] T007 [P] Create `LessonContentDto` (`@Serializable`) in `core/data/src/main/java/com/example/nativeminds/data/remote/dto/LessonContentDto.kt`, per [data-model.md](./data-model.md)
- [X] T008 Create `LessonDto.toDomain()` and `LessonContentDto.toDomain()` extension mappers (reusing T005's helper) in `core/data/src/main/java/com/example/nativeminds/data/remote/mapper/LessonDtoMappers.kt` (depends on: T005, T006, T007)
- [X] T009 Create `NetworkModule.kt` in `core/data/src/main/java/com/example/nativeminds/data/di/NetworkModule.kt`, `@Provides`-ing a singleton `SupabaseClient` (with the `Postgrest` plugin installed) built from `BuildConfig.SUPABASE_URL`/`BuildConfig.SUPABASE_ANON_KEY` (depends on: T002)
- [X] T010 Create `SupabaseRemoteLessonDataSource` implementing `RemoteLessonDataSource` (`core/data/src/main/java/com/example/nativeminds/data/remote/RemoteLessonDataSource.kt`) in `core/data/src/main/java/com/example/nativeminds/data/remote/SupabaseRemoteLessonDataSource.kt`, per [contracts/supabase-schema.md](./contracts/supabase-schema.md) (depends on: T008, T009) — kept `fetchContent(id)` lazy/per-lesson exactly as the original interface already did, per research.md §7's own hedge; only `fetchLessons()` feeds the bulk sync
- [X] T011 [P] Add `deleteMissing(ids: List<Long>)` to `core/database/src/main/java/com/example/nativeminds/database/LessonDao.kt` — **deviates from the plan's `deleteAll()`**: a full delete-then-insert would cascade-delete every already-cached `lesson_content` row on *every* sync, including for lessons that didn't change, defeating offline reading; `deleteMissing` only removes rows actually gone from the remote catalog
- [X] T012 Change `RoomLessonRepository.syncIfNeeded()` in `core/data/src/main/java/com/example/nativeminds/data/RoomLessonRepository.kt` to: no-op silently when `NetworkMonitor.isOnline()` is false; otherwise fetch remote lessons, fail loudly if the result is empty (refuses to wipe a working local catalog because of a suspicious empty response), then replace the local catalog inside one `database.withTransaction { dao.upsertAll(...); dao.deleteMissing(ids) }` (FR-003/FR-004). Error reporting moved to `SyncLessonsUseCase` (domain layer), matching `RefreshLessonContentUseCase`'s existing shape, rather than injecting `ErrorReporter` into `:core:data` (depends on: T010, T011)
- [X] T013 Delete `core/data/src/main/java/com/example/nativeminds/data/remote/FakeRemoteLessonDataSource.kt` (no longer referenced once T012 lands — confirmed with a repo-wide search) (depends on: T012). Also deleted the now-fully-unused dummy seed files (`DummyLessonSeed.kt`, `DummyLessonContentSeed.kt`, `LessonSeedCatalog.kt`, `LessonSeedTopic.kt`, the four `*Lessons.kt` subject files, and `DummyLessonSeedTest.kt`) — their content was already captured in `supabase/seed.sql` (T004) before deletion, so nothing was lost, and the constitution's "no dead code" rule ruled out leaving ~700 unreferenced lines behind
- [X] T014 Update `core/data/src/main/java/com/example/nativeminds/data/di/DataModule.kt` to bind `SupabaseRemoteLessonDataSource` in place of `FakeRemoteLessonDataSource` (depends on: T010, T013)
- [X] T015 [P] Unit test `LessonDto.toDomain()` / `LessonContentDto.toDomain()` in `core/data/src/test/java/com/example/nativeminds/data/remote/mapper/LessonDtoMappersTest.kt` (depends on: T008)
- [X] T016 [P] Extend `core/data/src/test/java/com/example/nativeminds/data/RoomLessonRepositoryTest.kt` with cases for `syncIfNeeded()`: remote adds/edits/removes are reflected after a successful sync, an offline sync is a silent no-op, an empty remote response fails rather than clearing the catalog, and a failed sync leaves the previously-synced catalog byte-for-byte unchanged (depends on: T012). Also added `SyncLessonsUseCaseTest` in `:core:domain` covering the new error-reporting wrap

**Checkpoint**: The app can now fetch the real catalog from Supabase and store it correctly. User story work can begin.

---

## Phase 3: User Story 1 - Learner sees real, remotely-sourced lessons (Priority: P1) 🎯 MVP

**Goal**: Subjects, titles, teasers, and lesson bodies shown in the app come from the Supabase
catalog set up in Phase 1/2, not from bundled sample data.

**Independent Test**: With the dummy seed removed (Phase 2) and the device online, fresh-install the
app and confirm every subject/lesson/content value shown matches the Supabase tables, not the old
hardcoded values.

- [X] T017 [US1] Add a distinct "connect to load lessons" empty-state string pair (title + body) alongside the existing search-empty strings in `feature/home/src/main/res/values/strings.xml`, per [research.md](./research.md) §6
- [X] T018 [US1] `EmptyResultsState` (in `feature/home/.../ui/components/EmptyResultsState.kt`) now takes an `isFiltering` parameter — `true` keeps the existing search-empty copy/suggestions/clear-search link, `false` shows the never-synced copy with the query-specific affordances omitted entirely (not just emptied); `HomeScreen.kt` passes `state.isFiltering` through (depends on: T017)
- [X] T019 [P] [US1] Added `NeverSyncedEmptyStatePreview` next to `EmptyResultsStatePreview` in `EmptyResultsState.kt`, and a third `HomePreviewCase` (no query, no subjects, empty lessons) in `feature/home/.../ui/preview/HomePreviewData.kt` (depends on: T018)
- [X] T020 [US1] Validated on a live emulator against the real Supabase project: fresh install online shows all 40 lessons with real titles/subjects matching the Supabase tables exactly; fresh install offline shows the "Connect to load lessons" empty state (not blank/broken), and recovers to the full catalog once online + refreshed

**Checkpoint**: User Story 1 is independently functional and demoable — this is the MVP slice.

---

## Phase 4: User Story 2 - Lessons keep working fully offline after the first sync (Priority: P1)

**Goal**: Once synced, the full catalog stays browsable, searchable, and readable with no network,
exactly as before — confirming the remote migration didn't regress the offline-first guarantee.

**Independent Test**: Launch online once to sync, force-quit, enable airplane mode, relaunch, and
confirm the previously-synced catalog and lesson bodies are all still fully readable.

- [X] T021 [P] [US2] `readingALessonNeverReachesTheNetwork` in `RoomLessonRepositoryTest.kt` proves reads never call the remote; `syncingWhileOfflineIsANoOp` proves an offline sync touches neither Room nor the network — together the regression coverage this task asked for (depends on: T012, T016)
- [X] T022 [US2] Validated on a live emulator: synced online once, opened a lesson (caching its content), force-stopped, disabled wifi+data (confirmed via a failed ping, not just the `airplane_mode_on` setting — that broadcast is permission-denied on this emulator image and does not actually cut connectivity on its own), relaunched — all 40 lessons and the previously-opened lesson's full text were still there with zero network calls

**Checkpoint**: User Stories 1 and 2 both independently verified — offline guarantee holds.

---

## Phase 5: User Story 3 - Content updates reach existing users without an app update (Priority: P2)

**Goal**: A lesson added, edited, or removed in Supabase is reflected in an already-installed app on
its next sync, and the learner can trigger that sync manually instead of only via app relaunch.

**Independent Test**: Edit/add/remove a lesson directly in Supabase, then trigger a sync (relaunch or
manual refresh) on a device that already has synced content, and confirm the change appears without
reinstalling the app.

- [X] T023 [P] [US3] Add `HomeIntent.RefreshRequested` and a `syncToken: Int = 0` field to `HomeUiState` in `feature/home/src/main/java/com/example/nativeminds/feature/home/ui/HomeContract.kt`, per [research.md](./research.md) §5
- [X] T024 [US3] Handle `RefreshRequested` in `feature/home/src/main/java/com/example/nativeminds/feature/home/ui/HomeReducer.kt` — the reduction only increments `syncToken` (depends on: T023)
- [X] T025 [US3] `HomeViewModel.kt`'s `init` now derives a `_state.map { it.syncToken }.distinctUntilChanged()` flow that calls `syncLessons()` (now `Result`-returning) and sends `HomeEffect.ShowSyncError` `.onFailure`, replacing the old one-shot `launch { runCatching { ... } }` (depends on: T024, T012). `SyncLessonsUseCase` itself gained an `ErrorReporter` dependency and now returns `Result<Unit>`, matching `RefreshLessonContentUseCase`'s existing shape
- [X] T026 [US3] Wrapped the scrollable column in `HomeScreenContent` with Material3's `PullToRefreshBox` (`androidx.compose.material3.pulltorefresh` — not `androidx.compose.material3` directly, contrary to the plan's assumption), `isRefreshing` tied to `lessons.loadState.refresh is LoadState.Loading` (a real signal — Room's `PagingSource` auto-invalidates when sync writes to the table — rather than a fabricated timer), `onRefresh` dispatching `HomeIntent.RefreshRequested` (depends on: T025)
- [X] T027 [P] [US3] `HomeReducerTest.kt` (new file) covers the `RefreshRequested` reduction — only `syncToken` changes (depends on: T024). `HomeViewModelTest.kt` also gained two tests: refresh triggers another sync call, and a failing refresh surfaces `ShowSyncError` too
- [X] T028 [US3] Validated on a live emulator: ran `update public.lessons set title = '... (Güncellendi)' where id = 1;` directly in Supabase's SQL editor, pulled to refresh in-app, the new title appeared with no reinstall. Separately, with the device offline, a pull-to-refresh left the catalog (including the just-verified title edit) completely unchanged — no crash, no partial state

**Checkpoint**: All three user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Constitution-mandated documentation and final verification across all stories.

- [X] T029 [P] Added a 6-row "Remote lesson content (Supabase)" section to README's Key Decisions table: client library pin (and why not latest), sync/replace strategy, lazy content-fetch retained, secrets handling, read-only access model, and the `minSdk` 24 vs. desugaring trade-off
- [X] T030 [P] Rewrote the stale "Story text is hand-seeded" Cut Corners entry to describe the actual current state (dummy seed deleted, `supabase/seed.sql` preserves its content, live project still needs manual creation — T003), corrected the now-false "sync failures are not reported anywhere" entry, and added three new entries (no admin authoring UI, pull-to-refresh spinner semantics, shared empty-state composable)
- [X] T031 `./gradlew test` (all modules) and `./gradlew lint` both pass with zero errors; `./gradlew :app:assembleDebug` also verified to confirm the `minSdk` 24 + desugaring fix actually dexes successfully, not just compiles
- [X] T032 All 5 quickstart.md §3 scenarios verified end-to-end on a live emulator against the real Supabase project (see T020/T022/T028) — remote lesson content is fully working, not just unit-tested

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately. T003 (Supabase project) blocks nothing in Phase 1 itself but blocks every later phase's *runtime* verification (not the code tasks).
- **Foundational (Phase 2)**: Depends on Phase 1 (T002 for BuildConfig, T003 for a live project to point at) — BLOCKS all user stories.
- **User Stories (Phase 3–5)**: All depend on Phase 2 completion (T014 specifically — the real data source must be bound). US1/US2/US3 are otherwise independent of each other.
- **Polish (Phase 6)**: Depends on all three user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Depends only on Foundational (Phase 2).
- **User Story 2 (P1)**: Depends only on Foundational (Phase 2) — independent of US1's UI change, though both are typically validated together since they share the same sync engine.
- **User Story 3 (P2)**: Depends only on Foundational (Phase 2) — touches `HomeContract`/`HomeReducer`/`HomeViewModel`/`HomeScreen`, disjoint files from US1's empty-state change until T018/T025 both land in `HomeScreen.kt`/`HomeViewModel.kt` (sequence those two if worked in parallel by different people).

### Parallel Opportunities

- T001, T002, T004 (Phase 1) — different files, run together.
- T005, T006, T007 (Phase 2) — different files, run together; T008 waits on all three.
- T011 can run in parallel with T005–T009 (different module).
- T015, T016 can run in parallel with each other once T012 lands.
- Once Phase 2's checkpoint (T014) is reached, US1, US2, and US3 can be staffed in parallel — see file overlap note above for US1/US3.

---

## Parallel Example: Phase 2 kickoff

```bash
Task: "Extract paragraph-splitting helper in core/data/.../mapper/LessonContentMappers.kt"
Task: "Create LessonDto in core/data/.../remote/dto/LessonDto.kt"
Task: "Create LessonContentDto in core/data/.../remote/dto/LessonContentDto.kt"
Task: "Add LessonDao.deleteAll() in core/database/.../LessonDao.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Phase 1: Setup (real Supabase project + build wiring)
2. Phase 2: Foundational (sync engine) — **critical path, cannot be skipped or parallelized away**
3. Phase 3: User Story 1
4. **STOP and VALIDATE**: run quickstart.md §3 steps 1 and 5 independently
5. This alone satisfies FR-001/FR-002/FR-009 and SC-001 — a demoable MVP

### Incremental Delivery

1. Setup + Foundational → real remote sync engine working, verified by unit tests (T015, T016)
2. + User Story 1 → demo: real content, not fake (MVP)
3. + User Story 2 → demo: offline still works exactly as before
4. + User Story 3 → demo: edit content remotely, watch it show up without a new build
5. + Polish → README decisions recorded, full test/lint pass, full quickstart re-validated

### Suggested Solo Order

Given this is a single-developer case-study project (not a staffed team), work Phases 1 → 2 → 3 → 4
→ 5 → 6 sequentially in that order — the "parallel" markers above are for tasks safe to reorder
within a phase, not a suggestion to context-switch across phases.
