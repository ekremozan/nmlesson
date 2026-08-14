# Data Model: Paywall & Purchase Success Screens

No new persisted entities. Everything below is either an existing domain type reused as-is, or
transient UI/nav state.

## Reused (unchanged)

- **`EntitlementRepository`** (`:core:domain`) — gains one interface method:
  `fun setPremium(value: Boolean)`. Still the single source of truth every gating check reads.
- **`Story`** (`:core:model`) — read via `StoryRepository.story(id)` to render the Success screen's
  resume card (title, cover).

## New nav arguments (not persisted)

- **`PaywallRoute(storyId: Long, progressPercent: Int)`** — which story/position triggered the
  gate; `progressPercent` is carried through untouched so Success can show it.
- **`PurchaseSuccessRoute(storyId: Long, progressPercent: Int, plan: PurchasePlan)`** — same story
  reference plus which plan was purchased, for the "you're premium now — {plan}" line.

## New transient UI state

- **`PurchasePlan`** (enum, `:feature:paywall`): `MONTHLY`, `YEARLY`. Drives which plan card is
  highlighted and what the CTA label/price show. Not persisted — resets to `MONTHLY` (the design's
  default) each time the Paywall screen opens.
- **`PaywallUiState`**: `{ selectedPlan: PurchasePlan, isRestoreMessageVisible: Boolean }`.
  `isRestoreMessageVisible` is a one-shot effect in practice (Restore purchases → snackbar), listed
  here only if the reducer needs it as state rather than an effect; default to effect per
  Constitution III (one-shot → `ReaderEffect`-style channel), so no field is actually needed —
  `PaywallUiState` is just `{ selectedPlan: PurchasePlan }`.
- **`PurchaseSuccessUiState`**: `{ storyId: Long, progressPercent: Int, plan: PurchasePlan, story: Story? }`
  — `story` starts `null` (loading) and folds in via an intent once
  `StoryRepository.story(storyId)` emits, mirroring Reader's `DetailChanged` pattern.

## State transitions

- Paywall: `selectedPlan` flips between `MONTHLY`/`YEARLY` on plan-card tap; purchase CTA tap is a
  plain callback out (not a state change) that triggers `EntitlementRepository.setPremium(true)`
  and navigation to Success — the paywall itself never represents "purchased" as a state.
- Success: `story` goes `null` → `Story` once the repository emits; no further transitions besides
  the story catalog dropping the row (handled by the existing `null`-means-gone convention already
  used by `StoryRepository.story`).
