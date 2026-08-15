# Quickstart: Remote Lesson Content

This is the one-time setup the project owner does on the Supabase side, plus how to validate the
feature end-to-end once it's implemented. No end user ever signs up for anything — only the
developer needs a Supabase account, to manage content.

## 1. One-time Supabase account & project setup

1. Create a free account at supabase.com (GitHub sign-in is enough — no separate password to
   manage).
2. Create a new project (pick any name, e.g. `nativeminds`, and a database password — the password
   is only used for direct Postgres access, not by the app).
3. In the project's **Settings → API** page, copy two values:
   - **Project URL** (e.g. `https://xxxxxxxx.supabase.co`)
   - **`anon` `public` API key** (a long JWT-looking string) — this is the key the app ships with;
     it is *not* the `service_role` key, which must never be copied into the app.
4. In the SQL editor, run [`supabase/schema.sql`](../../supabase/schema.sql) once, to create
   `lessons` and `lesson_content` with public-read-only access (mirrors
   [`contracts/supabase-schema.md`](./contracts/supabase-schema.md)).
5. Run [`supabase/seed.sql`](../../supabase/seed.sql) once, to populate both tables with today's 40
   lessons (generated from `DummyLessonSeed`/`DummyLessonContentSeed`) so the remote catalog starts
   out equal to what the app already ships.

That's the entire "üyelik/token" side: one developer account, one project, one URL + one public key.
No per-user tokens, no OAuth flow, nothing the end learner ever sees or does.

## 2. Wire the key into the app (developer machine)

Add two lines to the git-ignored `local.properties` at the repo root:

```properties
SUPABASE_URL=https://xxxxxxxx.supabase.co
SUPABASE_ANON_KEY=<the anon public key>
```

These are read into `BuildConfig.SUPABASE_URL` / `BuildConfig.SUPABASE_ANON_KEY` at build time (see
`research.md` §3) — nothing else in the app reads them directly.

## 3. Validate the feature end-to-end

Prerequisite: steps 1–2 done, `SupabaseRemoteLessonDataSource` wired into `DataModule` (implementation
phase).

```bash
./gradlew :app:installDebug
```

1. **First launch, online** — open the app with network on. Confirm the Home lesson list matches
   what's in the Supabase `lessons` table, not the old hardcoded values (User Story 1).
2. **Offline replay** — force-quit, enable airplane mode, relaunch. Confirm the same lessons are
   still browsable, searchable, and their content still opens and reads normally (User Story 2).
3. **Remote edit propagation** — with the device back online, edit one lesson's `title` directly in
   Supabase's table editor (or `update public.lessons set title = '...' where id = 1;` in the SQL
   editor), then trigger the app's refresh (relaunch, or the in-app refresh control). Confirm the new
   title appears without reinstalling the app (User Story 3).
4. **Failed-sync safety** — with the device online and content already synced once, turn on airplane
   mode mid-session and trigger a manual refresh. Confirm the app shows a sync-failed indication
   (`HomeEffect.ShowSyncError`) while every previously synced lesson remains exactly as it was — no
   partial or emptied catalog (FR-004/SC-004).
5. **True first-launch-offline** — uninstall the app, enable airplane mode, install fresh, and
   launch. Confirm the empty state reads as "connect to load lessons" rather than an unexplained
   blank list (FR-006/SC-005).

## 4. Automated checks

```bash
./gradlew :core:data:test    # DTO mappers + replace-in-transaction sync behavior
./gradlew :feature:home:test # RefreshRequested intent / syncToken reduction
./gradlew test                # full unit test suite
./gradlew lint
```
