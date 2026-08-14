# Data Model: Listen To Story

No persisted entities — narration state is in-memory only for the current screen visit (per spec
Assumptions). Only new in-memory/domain types:

## `NarrationState` (`:core:domain`, sealed)

| Variant | Fields | Meaning |
|---|---|---|
| `Idle` | — | Nothing narrated yet, or reset (screen re-entered) |
| `Playing` | `sentenceIndex: Int`, `totalSentences: Int` | Currently speaking `sentenceIndex` |
| `Paused` | `sentenceIndex: Int`, `totalSentences: Int` | Paused at `sentenceIndex`, resumable |
| `Unavailable` | `reason: NarrationUnavailableReason` | No usable TTS engine/voice for this content |

`sentenceIndex` / `totalSentences` are what `ReaderReducer` derives the `ListenPill`'s
Listen/Pause/Resume visual state and (optionally) narration progress from — no new persisted field.

## `NarrationUnavailableReason` (`:core:domain`, enum)

`ENGINE_MISSING`, `LANGUAGE_UNSUPPORTED` — enough to pick a specific string resource in FR-011's
user-facing message without leaking platform error codes into the domain layer.

## `StoryNarrator` (`:core:domain`, interface)

- `fun start(storyId: Long, text: String)` — begins from sentence 0
- `fun pause()`
- `fun resume()`
- `fun stop()` — used on screen exit (FR-007) and on reaching the end (FR-009)
- `val state: Flow<NarrationState>`

One narrator instance is app-scoped (`@ApplicationScope`/singleton) since only one narration session
exists at a time (spec Assumptions); `ReaderViewModel` observes `state` and calls `stop()` from
`onCleared()`, and also calls `stop()` when it detects `storyId` differs from the narrator's current
story (defensive, in case another screen instance is still bound).
