# Implementation Plan: Remote Lesson Content

**Branch**: `004-remote-lesson-content` | **Date**: 2026-08-15 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/004-remote-lesson-content/spec.md`

## Summary

Replace the hardcoded dummy lesson seed with lesson content (subjects, titles, teasers, bodies)
fetched from a Supabase Postgres backend. `RoomLessonRepository` already treats Room as the single
source of truth and calls a `RemoteLessonDataSource` interface to sync into it — this plan adds a
`SupabaseRemoteLessonDataSource` implementation of that same interface (using the official
`supabase-kt` Postgrest client over Ktor/OkHttp), swaps it in for `FakeRemoteLessonDataSource` in
`DataModule`, and changes `syncIfNeeded()` from "seed once if empty" to "replace the local catalog
with the current remote state inside one transaction" so adds/edits/removals on the Supabase side
propagate to installed apps. The existing dummy seed data becomes the one-time payload uploaded into
Supabase's tables so the remote catalog starts out equivalent to what ships today.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (existing project toolchain, unchanged)

**Primary Dependencies**: `io.github.jan-tennert.supabase:postgrest-kt` (official Supabase Kotlin
Postgrest client) + `io.ktor:ktor-client-okhttp` as its HTTP engine on Android; `kotlinx-serialization-json`
(already a project dependency) for the wire DTOs

**Storage**: Room (`:core:database`, unchanged) stays the single local source of truth read by every
feature module; Supabase Postgres (two tables: `lessons`, `lesson_content`) is the new remote source
`RoomLessonRepository` syncs from

**Testing**: JUnit4 + the project's existing fake-repository/fake-data-source pattern
(`RoomLessonRepositoryTest`, `FakeLessonRepository`); new unit tests cover the DTO→domain mappers and
the replace-in-transaction sync behavior (add/update/remove, and "sync fails → nothing changes")

**Target Platform**: Android, `minSdk` 24 / `compileSdk` 36–37 (existing, unchanged)

**Project Type**: Mobile app, existing Gradle multi-module layout — no new Gradle modules needed, all
changes land inside `:core:data` plus one new intent/effect in `:feature:home`

**Performance Goals**: Full catalog sync (today: 4 subjects × 10 lessons = 40 lessons + 40 content
rows) completes in a few seconds on a normal mobile connection and never blocks the UI thread (runs
on `@IoDispatcher`, matching the existing `syncIfNeeded()` dispatch)

**Constraints**: Offline-first (Room remains the only thing feature modules read from); remote access
is anonymous/public-read only — no end-user login exists or is introduced; the Supabase URL and anon
key must not be committed to version control in plaintext

**Scale/Scope**: Today's catalog is ~40 lessons; the chosen sync strategy (full replace inside one
Room transaction) is deliberately simple at this size — see Complexity Tracking / research.md for
what would change at 10× scale

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Check | Status |
|---|---|---|
| I. Defensible Decisions | New dependency (`supabase-kt` + Ktor OkHttp) is added to `gradle/libs.versions.toml` and justified. Three non-obvious choices need a README "Key Decisions" entry: (a) `supabase-kt` Postgrest client vs. hand-rolled REST calls, (b) full-replace-in-transaction sync vs. incremental diffing, (c) anon-key/RLS public-read model vs. adding auth. The retirement of `FakeRemoteLessonDataSource` from the production DI graph is a cut corner removal, not a new one — logged as resolved, not as a new corner. | PASS (pending README updates in implementation) |
| II. Clean Architecture & Model Separation | `RemoteLessonDataSource` interface (already in `:core:data`) is unchanged; only a new `SupabaseRemoteLessonDataSource` implementation is added, alongside `LessonDto`/`LessonContentDto` (`@Serializable`, own classes, never passed to domain) and `toDomain()` extension mappers. `:core:domain` and `:feature:*` see no new types. `SupabaseClient` is a type the project doesn't own → provided via `@Provides`, not `@Binds`. | PASS |
| III. MVI With A Single Mutation Path | No existing `HomeIntent`/`HomeUiState` contract is broken. One addition: a `HomeIntent.RefreshRequested` intent whose reduction increments a `syncToken: Int` in state (the same "retry is a token bump" pattern CLAUDE.md already prescribes), read by a `distinctUntilChanged()`'d flow that re-invokes `SyncLessonsUseCase`. The reducer stays pure; the ViewModel still only applies reductions and forwards effects. | PASS |
| IV. Offline-First With Visible Failures | Room stays the only read path. `syncIfNeeded()`'s replace runs inside `database.withTransaction { }` (the pattern already used for seeding), so a failure partway through leaves the previous local catalog completely intact — satisfying FR-004/SC-004 by construction, not by extra error-recovery code. Every sync failure still reaches `ErrorReporter` and the existing `HomeEffect.ShowSyncError` path; nothing is newly swallowed. Gating is untouched — `EntitlementRepository`/`ReaderAccess` don't change. | PASS |
| V. Design System Is The Only Source Of Style | The only UI-visible change is distinguishing "no results for this filter" (existing empty state) from "no content has ever synced yet" (new copy/state on the same empty-state composable). No new screen, no new colors/sizes — reuses `NativeMindsTheme` tokens already in `HomeScreen.kt`. Both states get a preview via the existing `PreviewParameterProvider`. | PASS |

No violations — Complexity Tracking table is empty.

*Re-checked after Phase 1 design (data-model.md, contracts/, quickstart.md): the concrete file list
in Project Structure below matches every row in this table — no new violation was introduced by the
detailed design.*

## Project Structure

### Documentation (this feature)

```text
specs/004-remote-lesson-content/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md         # Phase 1 output (/speckit-plan command)
├── quickstart.md         # Phase 1 output (/speckit-plan command)
├── contracts/            # Phase 1 output (/speckit-plan command)
│   └── supabase-schema.md
└── tasks.md              # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
core/data/src/main/java/com/example/nativeminds/data/
├── remote/
│   ├── RemoteLessonDataSource.kt          # unchanged interface
│   ├── FakeRemoteLessonDataSource.kt      # kept, moved to test sources only
│   ├── SupabaseRemoteLessonDataSource.kt  # NEW: postgrest-kt implementation
│   ├── dto/
│   │   ├── LessonDto.kt                   # NEW: @Serializable wire model
│   │   └── LessonContentDto.kt            # NEW: @Serializable wire model
│   └── mapper/
│       └── LessonDtoMappers.kt            # NEW: LessonDto.toDomain(), LessonContentDto.toDomain()
├── di/
│   ├── DataModule.kt                      # CHANGED: bind SupabaseRemoteLessonDataSource
│   └── NetworkModule.kt                   # NEW: @Provides SupabaseClient
└── RoomLessonRepository.kt                # CHANGED: syncIfNeeded() becomes replace-in-transaction

core/data/src/main/java/com/example/nativeminds/data/local/
└── DummyLessonSeed.kt / DummyLessonContentSeed.kt  # CHANGED: retired as prod data source,
                                                      # repurposed as the source for the one-time
                                                      # Supabase seed script (see quickstart.md)

feature/home/src/main/java/com/example/nativeminds/feature/home/ui/
├── HomeContract.kt      # CHANGED: + HomeIntent.RefreshRequested, + syncToken, + empty-vs-never-synced
├── HomeReducer.kt        # CHANGED: handles RefreshRequested
└── HomeViewModel.kt      # CHANGED: derives sync trigger from syncToken instead of only `init`

gradle/libs.versions.toml # CHANGED: + supabase-kt (postgrest, ktor-client-okhttp) versions/libraries

supabase/
└── seed.sql               # NEW: one-time INSERT script populating `lessons` + `lesson_content`
                            # from the current dummy catalog (run once via Supabase's SQL editor)
```

**Structure Decision**: Everything lives inside the existing `:core:data` module (remote source,
DTOs, mappers, DI) plus a small, additive change to `:feature:home`'s contract for manual refresh.
No new Gradle module is needed — this is exactly the seam `RemoteLessonDataSource` was already cut
along, per `FakeRemoteLessonDataSource`'s own doc comment ("swapping this for a real implementation
later changes nothing on `RoomLessonRepository`'s side").

## Complexity Tracking

*No violations — table intentionally empty.*
