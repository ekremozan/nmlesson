# Phase 1 Data Model: Remote Lesson Content

This feature adds a remote-side schema and one new wire-model layer on top of the domain/local
models that already exist (`core/model/.../Lesson.kt`, `LessonContent.kt`,
`core/database/.../LessonEntity.kt`, `LessonContentEntity.kt`). Domain and Room shapes are unchanged
— see `contracts/supabase-schema.md` for the exact remote table definitions this maps from.

## Remote (Supabase Postgres) — source of truth for content authoring

### `lessons`

| Column | Type | Notes |
|---|---|---|
| `id` | `bigint` (PK) | Matches the domain/local `Lesson.id` |
| `subject` | `text` | One of the four existing subject names (`Biyoloji`, `Tarih`, `Coğrafya`, `Kimya` — matches `Lesson.subject` exactly) |
| `title` | `text` | Required, non-blank |
| `teaser` | `text` | Required, non-blank |
| `minutes` | `int` | Reading/listening time estimate |
| `has_audio` | `boolean` | Whether narration is available |
| `is_locked` | `boolean` | Premium gating flag — gating *rule* stays in the domain use case, this only carries the raw flag |
| `image` | `text` | Subject illustration key (e.g. `"subject_biology"`) — see FR-010, values stay within the four bundled illustrations |

### `lesson_content`

| Column | Type | Notes |
|---|---|---|
| `lesson_id` | `bigint` (PK, FK → `lessons.id`, `ON DELETE CASCADE`) | Mirrors `LessonContentEntity`'s foreign key |
| `author` | `text` | |
| `body` | `text` | Whole lesson text with a blank line between paragraphs — same on-the-wire shape `LessonContentEntity.body` already stores locally, so the mapper only needs to pass it through unchanged |

No `updated_at`/watermark column is introduced in this iteration — the sync strategy is full-replace
(see research.md §4), which needs no change-tracking column at this scale. Adding one is the
concrete "10× scale" change to note in README Key Decisions.

## Wire DTOs — `:core:data` (`remote/dto/`)

New, `@Serializable`, never seen outside `:core:data`:

```kotlin
@Serializable
data class LessonDto(
    val id: Long,
    val subject: String,
    val title: String,
    val teaser: String,
    val minutes: Int,
    @SerialName("has_audio") val hasAudio: Boolean,
    @SerialName("is_locked") val isLocked: Boolean,
    val image: String,
)

@Serializable
data class LessonContentDto(
    @SerialName("lesson_id") val lessonId: Long,
    val author: String,
    val body: String,
)
```

### Mappers (`remote/mapper/LessonDtoMappers.kt`, extension functions)

- `LessonDto.toDomain(): Lesson`
- `LessonContentDto.toDomain(): LessonContent` — splits `body` into `paragraphs` the same way the
  existing `LessonContentEntity.toDomain()` mapper already does (reused, not duplicated)

## Local (Room) — unchanged

`LessonEntity` and `LessonContentEntity` keep their current shape exactly. `RoomLessonRepository`'s
mappers (`Lesson.toEntity()`, `LessonContent.toEntity()`) are reused unchanged — the DTO layer only
adds a new *source* of domain models, not a new local shape.

## `HomeUiState` addition (`:feature:home`)

| Field | Type | Purpose |
|---|---|---|
| `syncToken` | `Int` (default `0`) | Bumped by `HomeIntent.RefreshRequested`'s reduction; a derived flow watches it (`distinctUntilChanged()`) to re-invoke `SyncLessonsUseCase`, per research.md §5 |

`isFiltering` (existing computed property) is reused, unchanged, to select between the "no results
for this search" and "nothing synced yet" empty-state copy (research.md §6) — no new state field
needed for that distinction beyond the existing paging `LoadState` + `isFiltering`.

## State / entity relationships

```
Supabase.lessons (1) ──< Supabase.lesson_content (1)   [ON DELETE CASCADE, by lesson_id]
        │ sync (replace-in-transaction)
        ▼
Room.lessons (1) ──< Room.lesson_content (1)            [existing FK, unchanged]
        │ read-only
        ▼
:core:domain Lesson / LessonContent                       [unchanged domain models]
        │ existing mappers
        ▼
:feature:home / :feature:reader UI models                 [unchanged]
```
