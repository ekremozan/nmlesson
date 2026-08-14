# Contract: `StoryNarrator` (`:core:domain`)

The only interface `:feature:reader` is allowed to depend on for narration.

```kotlin
interface StoryNarrator {
    fun start(storyId: Long, text: String)
    fun pause()
    fun resume()
    fun stop()
    val state: Flow<NarrationState>
}
```

## Rules

- `start` while already `Playing`/`Paused` for a **different** `storyId` implicitly stops the prior
  session first (single active session, per spec Assumptions).
- `pause()` when not `Playing` is a no-op.
- `resume()` when not `Paused` is a no-op.
- `stop()` always transitions to `Idle` and discards `sentenceIndex` — this is the only way position
  is lost (screen exit, story finished, process death makes this moot since state is in-memory).
- Reaching the last sentence transitions `Playing → Idle` directly (not `Paused`), per FR-009.
- If TTS init fails or the story's language has no installed voice, `start()` transitions to
  `Unavailable` instead of `Playing`, and the implementation MUST also invoke `ErrorReporter`.
- `state` is a hot flow (`StateFlow`-shaped); the current value reflects reality immediately after
  any of the above calls return, callers don't need to guess in-flight state.
