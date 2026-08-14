# Research: Paywall & Purchase Success Screens

No NEEDS CLARIFICATION markers were left in the spec. Decisions below resolve the technical
unknowns the plan needed.

## Module boundary for the paywall

- **Decision**: New top-level `:feature:paywall` module, not code added inside `:feature:reader`.
- **Rationale**: Constitution II forbids `:feature:*` modules depending on each other; the spec
  requires the paywall to be reachable from "every place that gates content," so it must be a
  sibling `:app`-composed destination, not owned by reader.
- **Alternatives considered**: Put Paywall/Success composables inside `:feature:reader` — rejected,
  breaks the module-per-destination convention and would force a future gated surface (e.g. AI
  feature) to depend on `:feature:reader` just to reach the paywall.

## Triggering navigation from Reader

- **Decision**: Reader's unlock CTA becomes a plain callback (`onUnlockRequested`) passed into
  `readerScreen()`, resolved in `:app`'s nav graph — not a `ReaderIntent`.
- **Rationale**: Constitution III: "Navigating out of a screen is a plain callback supplied by the
  nav graph, not an intent." The old `SubscribeClicked` intent existed only because the sheet was
  in-place; once the destination is a real screen, the same rule that already governs `onBack`
  applies here.
- **Alternatives considered**: Keep an intent that raises a `ReaderEffect` carrying a navigation
  request — rejected as unnecessary indirection once the target is an external screen rather than
  reader-owned UI.

## Granting the mock entitlement

- **Decision**: Add `fun setPremium(value: Boolean)` to the `EntitlementRepository` domain
  interface itself; `MockEntitlementRepository` and the test `FakeEntitlementRepository` both
  already have a method with this exact signature, so this is a promotion to the interface, not new
  behavior.
- **Rationale**: Constitution IV requires a single entitlement source of truth; `:feature:paywall`
  cannot depend on `:core:data` to reach the concrete mock, so the write path must live on the
  interface it already depends on. No new use case is warranted — this is a flag write with no
  business rule attached (the *read*-side rule already lives in `ObserveStoryDetailUseCase`).
- **Alternatives considered**: A `GrantPremiumUseCase` wrapper — rejected as an unjustified
  abstraction over a one-line repository call (constitution I: no premature abstraction).

## Carrying "what story to resume" from Reader through Paywall to Success

- **Decision**: `storyId` and the reader's `progressPercent` at trigger time travel as
  `@Serializable` nav-route arguments (`PaywallRoute(storyId, progressPercent)` →
  `PurchaseSuccessRoute(storyId, progressPercent)`), matching the existing `ReaderRoute(storyId)`
  pattern. Success screen re-fetches the story's title/cover from `StoryRepository.story(id)` for
  display; it does not re-derive progress (that lives only in the reader's transient scroll state).
- **Rationale**: Same typed-argument convention already used by `ReaderRoute`; avoids inventing a
  cross-screen shared state holder for two values.
- **Alternatives considered**: A shared "purchase flow" ViewModel scoped to a nav sub-graph —
  rejected as unneeded complexity for two primitive values.

## Back-stack shape after purchase

- **Decision**: Success screen's "Continue reading" pops back to the originating `ReaderRoute`
  instance (or navigates a fresh one with `popUpTo` if it's gone) rather than pushing a second
  reader instance on top of Paywall; "Explore library" pops to `HomeRoute`.
- **Rationale**: Matches spec edge case (removed story falls back gracefully) and avoids a
  back-stack where pressing back from the unlocked story returns to the paywall/success screens.
- **Alternatives considered**: Leave Paywall/Success on the back stack — rejected, would let the
  reader navigate back into a screen that no longer reflects a decision to make.
