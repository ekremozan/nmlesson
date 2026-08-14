# Phase 1 Data Model: Story Detail (Reader) With Premium Unlock Sheet

Three separate model families, as Constitution Principle II requires: Room entities, domain models,
UI models. Conversion is always an extension function, and no entity ever reaches a composable.

## Data layer (Room)

### `StoryEntity` — unchanged

Existing table `stories` (`id`, `category`, `title`, `teaser`, `minutes`, `hasAudio`, `isLocked`).
`isLocked` remains the per-story premium marker; this feature adds no columns to it.

### `StoryContentEntity` — new

Table `story_content`, one row per story, holding only what the reader needs and the list does not.

| Field | Type | Notes |
|---|---|---|
| `storyId` | `Long` | Primary key; matches `StoryEntity.id`. Foreign key to `stories` with `ON DELETE CASCADE`, indexed. |
| `author` | `String` | Display name shown under the title. Non-null; empty is not valid content. |
| `body` | `String` | Whole story text; paragraphs separated by a blank line (see research R5). Non-null and non-blank. |

**Validation**: a row with a blank `body` is treated as absent — the reader shows the unavailable
state rather than an empty page (spec edge case).

**Migration 1 → 2**: creates `story_content` and its index. `stories` is untouched, so existing
installs keep their rows and simply have no content until the seed or a refresh fills it. The
migration is written by hand, exported to `core/database/schemas`, and covered by a
`MigrationTestHelper` test.

### DAO additions

- `StoryDao.observeStory(id: Long): Flow<StoryEntity?>` — header data for the reader.
- `StoryContentDao.observeContent(storyId: Long): Flow<StoryContentEntity?>`
- `StoryContentDao.upsert(content: StoryContentEntity)`
- `StoryContentDao.upsertAll(content: List<StoryContentEntity>)` — used by the seed.

`Flow` in both cases so a refresh that writes content updates an open reader with no manual
re-read, which is also what makes the retry path (FR-004) work without a second code path.

## Domain layer

### `StoryContent` — new (`:core:model`)

| Field | Type | Notes |
|---|---|---|
| `storyId` | `Long` | Identity of the story it belongs to. |
| `author` | `String` | |
| `paragraphs` | `List<String>` | Ordered, non-empty; trimmed, blanks dropped by the mapper. |

Relationship: exactly one `StoryContent` per `Story`, resolved by id. `Story` stays as it is —
the list and the reader header share it.

### `ReaderAccess` — new (`:core:domain`)

The gating result, a sealed hierarchy so the restricted branch structurally cannot carry the text
it is meant to withhold (FR-007).

- `ReaderAccess.Full(story: Story, content: StoryContent)` — unlocked story, or premium story with
  entitlement.
- `ReaderAccess.Preview(story: Story, author: String, paragraphs: List<String>, freeSharePercent: Int)`
  — premium story without entitlement. `paragraphs` is the computed 30% prefix and never the whole
  body; `freeSharePercent` is what the unlock sheet quotes.

### `ReaderDetail` — new (`:core:domain`)

What `ObserveStoryDetailUseCase` emits, so every failure mode is a value rather than an exception
crossing layers:

- `ReaderDetail.Loading`
- `ReaderDetail.Available(access: ReaderAccess)`
- `ReaderDetail.Unavailable(reason: UnavailableReason)` where `UnavailableReason` is
  `OFFLINE`, `ERROR`, or `STORY_MISSING`.

**State transitions**: `Loading → Available` when both story and content resolve;
`Loading → Unavailable` when the story row is gone (`STORY_MISSING`), content is absent with no
network (`OFFLINE`), or a refresh fails (`ERROR`). `Unavailable → Loading → Available` on retry.
`Available` re-emits on an entitlement change, moving between `Full` and `Preview` in place.

### `Entitlement`

Represented as a plain `Boolean` behind `EntitlementRepository.isPremium(): Flow<Boolean>`. It is
deliberately not a model class — there is exactly one fact to carry today, and inventing a wrapper
would imply structure the feature does not have.

### Reading position

Not persisted and not a domain model. It exists only as `progressPercent: Int` in the reader's UI
state, derived from the list scroll position; the list itself restores the offset across
configuration changes.

## UI layer (`:feature:reader`)

### `ReaderStoryUiModel`

Pre-formatted header data: `title`, `author`, `category`, `minutesLabel` (already a string, as in
`StoryUiModel`), `isPremium`, `hasAudio`.

### `ReaderBodyUiModel`

`paragraphs: List<String>` plus `isTruncated: Boolean` — the one flag the body composable needs to
decide whether to draw the fade. It never receives the withheld text.

### `ReaderUiState`

Irreducible facts only (Principle III); everything else is a computed property.

| Field | Type | Notes |
|---|---|---|
| `storyId` | `Long` | From the route via `SavedStateHandle`. |
| `content` | `ReaderContentUiState` | `Loading` / `Ready(story, body)` / `Unavailable(reason)`. |
| `isPremiumContent` | `Boolean` | Whether this story is gated at all. |
| `isUnlockSheetVisible` | `Boolean` | Starts `true` when restricted; the reader may dismiss it. |
| `progressPercent` | `Int` | 0–100, whole percent. |
| `retryToken` | `Int` | Incremented by a retry intent; drives the reload (research R4). |

Computed: `isRestricted` (content is `Ready` and its body `isTruncated`), `showUnlockAffordance`
(restricted and the sheet is hidden), `remainingLabel` source values for the pill.

**Mappers**: `StoryContentEntity.toDomain()`, `StoryContent.toEntity()`, `ReaderAccess.toUiModel()`
— all extension functions, in `mapper/` files in the module that owns the target type.

## Seed data

`DummyStorySeed` gains an author and a body for every story it already defines, including the two
the design shows verbatim (the lighthouse keeper story as full access, the cartographer story as
the restricted example). Seeding writes both tables in the same transaction, so a story is never
present without its content on a fresh install.
