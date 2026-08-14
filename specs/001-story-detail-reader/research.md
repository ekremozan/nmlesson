# Phase 0 Research: Story Detail (Reader) With Premium Unlock Sheet

Full treatment only for calls that were actually debatable. Everything else is one line — the
reasoning is either obvious or already established elsewhere in the codebase.

## Debatable calls

### R1. Story tap → reader is a plain callback, not a `HomeIntent`

**Decision**: `onStoryClick: (Long) -> Unit`, supplied by the nav graph, not a `HomeIntent`.

**Why**: The action changes no state. Making it an intent means an identity reduction plus a
branch in the ViewModel purely to emit a navigation effect — the shape Principle III forbids.
Navigating out of a screen is the graph's concern, not the screen's state.

**Trade-off**: Reads as an exception to "every user action is an intent"; it isn't one, because no
state is involved. If Home ever needs to *record* an open, that becomes a real intent alongside
the callback.

### R2. Reducer returns effects, not just state

**Decision**: `ReaderReducer` returns `Reduction(state, effects)`. `HomeReducer` still returns a
bare state.

**Why**: The reader raises one-shot effects (subscribe/listen "not available yet") in direct
response to intents. Something has to map intent → effect; the ViewModel may not branch, so the
reducer — already the one pure decision point — is the only place that can do it without breaking
Principle III.

**Trade-off**: Two reducer shapes in the codebase until Home needs its first intent-driven effect.
`Reduction` stays local to `:feature:reader`; extract to a shared module at the second consumer.

### R3. Story content in its own table, not extra columns on `stories`

**Decision**: `story_content` (1:1, FK cascade), not `author`/`body` columns on `stories`.

**Why**: The story list is paged and re-queries on every keystroke; a body measured in kilobytes
riding along would defeat the paging already in place.

**Trade-off**: One join instead of one table. DB version 1 → 2 with a written migration, not a
destructive fallback — a dropped library on upgrade is invisible in dev and unacceptable in prod.

### R4. Premium preview rule returns a sealed result, not a flag beside the text

**Decision**: `ReaderAccess.Preview` carries only the paragraphs the reader is allowed to see —
there is no field on it for the rest of the story.

**Why**: A "full text + isTruncated" shape lets a UI bug leak paid content; a type that has nowhere
to put the withheld text cannot leak it by construction. Computed in `:core:domain` (whole
paragraphs, ~30% of characters) so it is provable in a plain JVM test.

**Trade-off**: A story whose first paragraph exceeds the share gives away a little more — bounded
by one paragraph, the better trade against a mid-word cut.

### R5. Paywall delivered as a bottom sheet, not the anchored card the design draws

**Decision**: `ModalBottomSheet`, per the user's explicit ask.

**Why**: A sheet is what the interaction actually is — arrives over the story, dismissible, can be
brought back. As a fixed card it would either sit there permanently over text the reader is
allowed to read, or need a dismissal affordance invented from nothing. The design's own follow-up
note ("make the paywall a bottom sheet instead of a card") anticipates this.

## One-line calls

- **Navigation**: `navigation-compose` + type-safe `@Serializable` routes — the app had none yet
  and `SavedStateHandle` restoration comes free with it; each feature owns its own route.
- **Async loading**: state-driven (`LoadKey(storyId, retryToken)` → `distinctUntilChanged` →
  `flatMapLatest`), matching the pattern `HomeViewModel` already uses for paging; retry is just
  `retryToken + 1`.
- **Entitlement**: one `EntitlementRepository.isPremium(): Flow<Boolean>` in `:core:domain`, mock
  `MutableStateFlow` in `:core:data` — the interface is what has to be right now, the mock behind
  it is a single `@Binds` away from real billing.
- **Body rendering**: `LazyColumn` of paragraphs (not one long `Text`) — stays smooth for a
  full-length story and restores scroll position across configuration changes for free.
- **Subscribe / listen actions**: drawn as designed, answer a tap with "not available yet" rather
  than being silently inert or wired to a flow this feature doesn't build (see spec FR-016/017).
- **Error reporting**: `ErrorReporter` domain interface, logcat implementation — the project rule
  against silently swallowed exceptions is unconditional; the real backend is a follow-up.
