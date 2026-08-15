# Contract: Supabase Schema & Access

This is the interface the app depends on: two tables, their columns (see `data-model.md` for types),
and the access policy that makes anonymous read-only access safe. Anything on the Supabase side can
change freely as long as this contract holds; anything that changes this contract is a breaking
change to `SupabaseRemoteLessonDataSource`.

## Tables

### `lessons`

```sql
create table public.lessons (
  id         bigint primary key,
  subject    text not null,
  title      text not null,
  teaser     text not null,
  minutes    integer not null,
  has_audio  boolean not null default true,
  is_locked  boolean not null default false,
  image      text not null
);
```

### `lesson_content`

```sql
create table public.lesson_content (
  lesson_id  bigint primary key references public.lessons(id) on delete cascade,
  author     text not null,
  body       text not null
);
```

## Row Level Security (mandatory)

Both tables MUST have RLS enabled with a public-read-only policy. Without this, the `anon` key would
either be unable to read anything (RLS on, no policy) or Supabase would refuse to enable useful
protection later. No `INSERT`/`UPDATE`/`DELETE` policy is created for the `anon` role — content
changes happen only through the Supabase dashboard/SQL editor, authenticated as the project owner,
which bypasses RLS by default.

```sql
alter table public.lessons enable row level security;
alter table public.lesson_content enable row level security;

create policy "Public read access" on public.lessons
  for select using (true);

create policy "Public read access" on public.lesson_content
  for select using (true);
```

## Client access contract

- **Auth**: the Supabase **anon public key** + project URL, sent as the standard Supabase
  `apikey`/`Authorization` headers (handled by the `supabase-kt` `Postgrest` client — the app code
  never constructs these headers by hand).
- **Read `lessons`**: `postgrest["lessons"].select()` → `List<LessonDto>` (see `data-model.md`).
  No filtering/pagination is done server-side for this feature — the full catalog is fetched and
  Room's existing `Pager`/`PagingSource` continues to do all list paging locally, unchanged.
- **Read `lesson_content`**: `postgrest["lesson_content"].select()` → `List<LessonContentDto>`,
  fetched in the same sync pass as `lessons` (both tables are small; no per-lesson lazy fetch is
  needed the way the old `fetchContent(lessonId)` simulated one).
- **Writes**: none. The app never calls `insert`/`update`/`delete` against Supabase — this contract
  is read-only by design (see research.md §2).

## Compatibility

`RemoteLessonDataSource` (`:core:data`) keeps its existing two-method shape:

```kotlin
interface RemoteLessonDataSource {
    suspend fun fetchLessons(): List<Lesson>
    suspend fun fetchContent(lessonId: Long): LessonContent
}
```

`SupabaseRemoteLessonDataSource.fetchLessons()` maps every row of `lessons` via `LessonDto.toDomain()`.
`fetchContent(lessonId)` filters the `lesson_content` fetch to the requested row (or throws
`IOException` if absent, matching the current fake's behavior) — kept for interface compatibility
with `RoomLessonRepository.refreshContent(id)`, which is unaffected by this feature.
