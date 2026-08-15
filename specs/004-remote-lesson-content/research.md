# Phase 0 Research: Remote Lesson Content

## 1. Supabase client library

**Decision**: Use the official Kotlin Multiplatform SDK, `io.github.jan-tennert.supabase`, with just
its `Postgrest` module, on the `Ktor OkHttp` engine (Android-native HTTP stack, no separate networking
library needed).

**Rationale**: The app already depends on `kotlinx-serialization-json`, which `supabase-kt` builds
on directly — DTOs need no adapter layer. Postgrest gives a typed query builder
(`postgrest["lessons"].select()`) instead of hand-building REST URLs and headers, and the same
dependency scales to Storage/Realtime later without adding another library if the product grows.
It's a single well-known, actively maintained dependency, matching the constitution's "prefer fewer,
well-known libraries" rule.

**Alternatives considered**:
- *Raw Retrofit against PostgREST's REST endpoint*: works, but reimplements filtering/query-building
  that Postgrest already solves, and requires hand-writing the `apikey`/`Authorization` header
  plumbing Supabase's SDK already does. Rejected — more code for no behavioral difference.
- *Ktor client calling PostgREST directly without the Supabase SDK*: same rejection reasoning, one
  layer thinner than Retrofit but still reinventing the SDK.

## 2. Authentication / access model

**Decision**: No end-user authentication. The app calls Supabase with the project's public **anon
key**, and a Postgres **Row Level Security (RLS)** policy on both tables grants `SELECT` to the
`anon` role only — no `INSERT`/`UPDATE`/`DELETE` from the client, ever.

**Rationale**: The app has no login system today (per spec Assumptions, content is public-read,
consistent with the app's current no-login model). The anon key is designed by Supabase to be
embedded in client apps; it is not a secret in the way a service-role key is, but it is still not
committed in plaintext (see §3) — treating it carelessly would be sloppy even if not strictly unsafe,
and RLS is what actually keeps the tables safe if the key ever leaks. The developer's own Supabase
account/project is what needs creating — end users never sign up for anything (this directly answers
"üyelik açma / token" — see quickstart.md for the exact one-time steps).

**Alternatives considered**:
- *Service-role key embedded in the app*: rejected outright — that key bypasses RLS entirely and
  must never ship in a client binary.
- *Supabase Auth (anonymous or real accounts) for end users*: unnecessary complexity for read-only
  public content; would only be justified if per-user data (progress, favorites) is added later.

## 3. Secret handling

**Decision**: `SUPABASE_URL` and `SUPABASE_ANON_KEY` live in the developer's local, git-ignored
`local.properties`, are read into `BuildConfig` fields at build time (the existing Android
convention this project already uses for the SDK path), and are referenced from
`SupabaseRemoteLessonDataSource`/`NetworkModule` only through `BuildConfig`, never as a literal.

**Rationale**: `local.properties` is already git-ignored in this repo; reusing it costs nothing and
keeps the key out of version control and out of the APK's readable source, consistent with "New
dependencies ... justified" and general defensible-decision hygiene. This is a README "Key Decisions"
entry, not a "Cut Corner" — it's the correct level of care for a public-but-not-throwaway key.

**Alternatives considered**:
- *Hardcoding the key in `NetworkModule.kt`*: rejected — trivially grep-able in the repo even though
  the key is meant to be public-facing; no reason not to do this properly.
- *A secrets-manager / remote config service*: over-engineered for a case-study project with one
  environment.

## 4. Sync strategy: full replace vs. incremental diff

**Decision**: On every successful sync, fetch the complete current `lessons` + `lesson_content`
tables from Supabase and replace the entire local Room catalog with them inside one
`database.withTransaction { }` block (delete-all-then-insert-all, or an upsert-then-delete-missing
pass — implementation detail for `tasks.md`), mirroring the transactional pattern
`RoomLessonRepository` already uses for its dummy seed.

**Rationale**: This satisfies FR-003 (adds/edits/removals propagate) and FR-004 (a failed sync
leaves the previous catalog untouched) *by construction* — a transaction either fully commits or
fully rolls back, so there is no separate "undo a partial sync" code path to get wrong. At today's
scale (~40 rows in each table) fetching everything on every sync is trivial bandwidth and CPU cost.

**Alternatives considered**:
- *Incremental diffing (fetch only rows changed since last sync, delete only rows no longer
  present)*: the correct approach once the catalog is large (hundreds+ lessons) or sync frequency is
  high — needs a `updated_at` column and a stored "last synced at" watermark. Rejected for now as
  unjustified complexity at this scale; this is the concrete "what would change at 10× scale" note
  for the README Key Decisions entry.
- *Merge/upsert without ever deleting*: rejected — it cannot satisfy FR-003's "a lesson removed
  remotely disappears from the app" requirement.

## 5. Triggering sync (manual refresh)

**Decision**: Keep the existing automatic sync-on-launch (`HomeViewModel.init` already calls
`SyncLessonsUseCase`), and add one explicit manual-refresh entry point: a `HomeIntent.RefreshRequested`
intent whose only reduction increments a `syncToken: Int` in `HomeUiState`, following the exact
"retry is a token bump" shape CLAUDE.md already prescribes for this codebase. A `distinctUntilChanged()`
flow derived from `syncToken` re-invokes `SyncLessonsUseCase`.

**Rationale**: The spec's Assumptions commit to a manual refresh existing (not just app-restart);
the codebase already has a named pattern for exactly this ("Retry" is an intent whose only reduction
increments a token in state), so this introduces zero new architectural shape — it reuses one that
already exists elsewhere in the app.

**Alternatives considered**:
- *Rely on app-relaunch only, no in-screen refresh control*: simpler, but conflicts with the spec's
  own Assumptions and gives the learner no way to pull fresh content without leaving the screen.

## 6. Distinguishing "no results for this search" from "nothing has ever synced"

**Decision**: `HomeScreen`'s existing empty-state composable (driven by
`lessons.itemCount == 0 && lessons.loadState.refresh is LoadState.NotLoading`) gains a second copy
variant selected by `HomeUiState.isFiltering` (already computed) — when `false` **and** the catalog
is empty, the message reads as "connect to load lessons" rather than the current "couldn't find a
lesson for '%1$s'" search-empty copy.

**Rationale**: FR-006/SC-005 require a clear, non-broken-looking state on a true first-launch-offline
case; the paging empty-state mechanism that already exists is the correct, minimal place to add this
— no new screen or design-system token is needed, only a second string resource and a state check.

**Alternatives considered**:
- *A dedicated full-screen "no content" state separate from the search-empty state*: more visual
  weight than the case warrants; the existing empty-state slot already sits in the right place on
  screen.

## 7. Retiring the dummy seed as a production fallback

**Decision**: `FakeRemoteLessonDataSource` stops being bound in `DataModule` (production DI graph)
and moves to test sources, where it continues to serve as the test double for
`RoomLessonRepositoryTest` and similar. `DummyLessonSeed`/`DummyLessonContentSeed` stop being read by
`RoomLessonRepository` at runtime; their content becomes the input for the one-time `supabase/seed.sql`
script (FR-009) instead.

**Rationale**: FR-008 requires the fake content not to silently coexist with real remote content in
production. Keeping the fake as a *test* double (rather than deleting it) preserves the existing test
suite's ability to test `RoomLessonRepository` without a network dependency — deleting it would cost
test coverage for no benefit.

**Alternatives considered**:
- *Delete `FakeRemoteLessonDataSource` entirely*: would force every repository test onto a real or
  mocked network client; rejected as unnecessary churn.
