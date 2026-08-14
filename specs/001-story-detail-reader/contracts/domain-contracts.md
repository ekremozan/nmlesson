# Contract: Domain Interfaces

The contracts `:feature:reader` is allowed to see. All live in `:core:domain`, are implemented in
`:core:data`, and are bound there with `@Binds`. No feature module ever names an implementation.

## `StoryRepository` (extended)

Existing members (`pagedStories`, `categories`, `syncIfNeeded`) are unchanged. Added:

```text
fun story(id: Long): Flow<Story?>
    Emits the story row, then again on every change. null means "no such story locally".

fun storyContent(id: Long): Flow<StoryContent?>
    Emits the stored content, then again once a refresh writes it. null means "not stored yet".

suspend fun refreshContent(id: Long)
    Pulls content from the remote source and writes it to the local store.
    Throws when offline or when the fetch fails — the caller turns that into an
    UnavailableReason rather than swallowing it.
```

**Guarantees**: both flows are cold, backed by Room, and safe to collect from the main thread; the
suspend function does its work on the IO dispatcher inside the implementation. Reads never touch
the network — offline reading is a property of the read path, not a fallback branch.

## `EntitlementRepository` (new)

```text
fun isPremium(): Flow<Boolean>
    The single source of truth for premium status. Emits the current value immediately
    and again on every change.
```

**Guarantees**: one binding, application-scoped, and the only place any gating decision reads from.
The mock implementation defaults to `false`. A test or a demo drives it directly; no other code
path writes it in this feature.

## `AnalyticsLogger` (new)

```text
fun logContentViewed(storyId: Long, isRestricted: Boolean)
fun logPaywallShown(storyId: Long)
```

**Guarantees**: never throws — a failing logger must not break reading. Implementations are
side-effect only and return nothing.

## `ErrorReporter` (new)

```text
fun report(throwable: Throwable, context: String)
```

**Guarantees**: called for every content-resolution failure, in addition to (never instead of)
surfacing the failure on screen. Never throws.

## Use cases

```text
ObserveStoryDetailUseCase(storyId: Long): Flow<ReaderDetail>
    Combines story(id), storyContent(id) and isPremium() into one stream.
    - story null                       → Unavailable(STORY_MISSING)
    - content null                     → Loading, and the caller refreshes
    - story not locked, or premium     → Available(Full(...))
    - locked and not premium           → Available(Preview(...)) with the 30% rule applied here

RefreshStoryContentUseCase(storyId: Long): Result<Unit>
    Calls refreshContent and converts the failure into a typed reason. Reports every
    failure through ErrorReporter before returning it.
```

**The gating rule lives here, not in the ViewModel and not in the UI** — that is what makes it
unit-testable without a device and what stops a second screen from re-implementing it differently.

### Test obligations for this contract

- Unlocked story + non-premium reader → `Full`.
- Locked story + premium reader → `Full`.
- Locked story + non-premium reader → `Preview`, whose paragraphs are a strict prefix of the body
  and never the whole of it.
- Preview share: the returned paragraphs are the shortest whole-paragraph prefix reaching 30% of
  the body's characters, and at least one paragraph even for a single-paragraph story.
- Entitlement flipping `false → true` while collecting re-emits as `Full` without resubscribing.
- Missing story row → `Unavailable(STORY_MISSING)`.
- `RefreshStoryContentUseCase` reports through `ErrorReporter` on every failure path.
