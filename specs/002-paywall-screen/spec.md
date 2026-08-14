# Feature Specification: Paywall & Purchase Success Screens

**Feature Branch**: `002-paywall-screen`

**Created**: 2026-08-14

**Status**: Draft

**Input**: User description: "Uygulamada bir paywall ekranı eklenecek. okuma kısmında paywall isteyen yerlerde her zaman bu ekrana yönlendirecek. tasarımda 4. ekran var paywall ekranı. onu implement et. sonra 8. ekran olarak success ekranı gelecek. burada ödeme vs olmayacak sanki ödeme olmuş gibi artık premium olarak devam edecek kişi."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - See the paywall when a premium story is gated (Priority: P1)

A free reader opens a story that requires a subscription. Instead of the current in-place unlock affordance, the app takes them to a full-screen paywall that explains what premium unlocks and lets them choose a plan.

**Why this priority**: This is the core of the feature — without it there is nowhere to route the existing gating logic, and the case study's premium-gating requirement stays unproven.

**Independent Test**: Open a locked story as a non-subscriber and trigger the existing unlock action; confirm the paywall screen appears with both plan options and a primary call-to-action, and confirm it appears every time an unlock is requested during that session, not just the first.

**Acceptance Scenarios**:

1. **Given** a non-subscriber is reading a premium story preview, **When** they tap the unlock action, **Then** the app navigates to the paywall screen showing the benefits list, a Monthly plan, a Yearly plan (marked as best value), and a primary purchase button.
2. **Given** the paywall is open with Monthly selected by default, **When** the reader taps the Yearly plan card, **Then** Yearly becomes the visibly selected option and the primary button's label/price reflects that choice.
3. **Given** the reader is on the paywall screen, **When** they tap the close control, **Then** they return to exactly where they were in the reader, with no change to their entitlement.
4. **Given** a non-subscriber encounters a second locked story later in the same session, **When** they trigger the unlock action again, **Then** the paywall screen opens again (it is not a one-time interstitial).

---

### User Story 2 - Confirm the purchase and land on success (Priority: P1)

After picking a plan and tapping the purchase button, the reader sees a confirmation screen telling them they're premium now, and a way back into the story they came from.

**Why this priority**: Closes the loop the paywall opens — without a success state the purchase action is a dead end and the entitlement change is invisible to the user.

**Independent Test**: From the paywall, tap the purchase button and confirm the app grants premium entitlement immediately, shows the success screen with a summary of what was unlocked and the plan chosen, and offers a way to resume the story that triggered the paywall.

**Acceptance Scenarios**:

1. **Given** the reader has selected a plan on the paywall, **When** they tap the primary purchase button, **Then** no real payment flow runs, the app marks the account as premium, and the success screen is shown.
2. **Given** the success screen is shown, **When** it renders, **Then** it names the plan just chosen and shows the story the reader was reading before the paywall interrupted them.
3. **Given** the reader is on the success screen, **When** they tap the primary action, **Then** they return to that story and it now renders as fully unlocked (no preview truncation).
4. **Given** the purchase has completed, **When** the reader navigates anywhere else in the app afterwards, **Then** every premium gate treats them as a subscriber without needing to reopen the paywall.

---

### User Story 3 - Restore purchases affordance is present but inert (Priority: P3)

The paywall shows "Restore purchases" alongside Terms/Privacy, matching the design, even though there is no real store to restore from in this mock flow.

**Why this priority**: Visual/spec completeness for the design reference; not required for the gating loop to work end-to-end, so it is the first thing to drop if time is short.

**Independent Test**: Tap "Restore purchases" on the paywall and confirm the app gives a clear, non-crashing response (e.g., a message that there is nothing to restore) rather than silently doing nothing or granting entitlement.

**Acceptance Scenarios**:

1. **Given** the reader is on the paywall, **When** they tap "Restore purchases", **Then** the app shows a message that no prior purchase was found and does not change entitlement.

---

### Edge Cases

- Reader backgrounds the app while the paywall is open, then returns: paywall state (selected plan) is preserved for the current screen instance, or at minimum resets to the default plan without crashing.
- Reader taps the purchase button twice quickly: entitlement is granted once and only one navigation to the success screen occurs.
- Reader reaches the paywall from a story that is later removed from the catalog before they finish purchasing: success screen's "resume story" affordance degrades gracefully (e.g., falls back to the library) instead of crashing.
- Reader is already premium somehow and still lands on a gated flow: the paywall trigger should not fire for already-entitled readers, per existing gating rules.
- Device is offline when the reader taps purchase: since this is a mock/local entitlement grant (no network call), the purchase still succeeds and the success screen still appears.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST provide a dedicated, full-screen Paywall screen matching the reference design's screen 4 (hero visual, "Unlock every story" headline, benefits list, Monthly/Yearly plan cards, primary CTA button, Restore purchases/Terms/Privacy row).
- **FR-002**: Every place in the reading experience that currently gates content behind a premium check MUST navigate to the Paywall screen when a non-subscriber tries to access that content, replacing the current in-place unlock sheet as the sole entry point into the purchase flow.
- **FR-003**: The Paywall screen MUST let the reader pick between a Monthly and a Yearly plan, with Yearly visually marked as the better-value option, and the primary CTA MUST reflect whichever plan is currently selected.
- **FR-004**: The Paywall screen MUST offer a close/back control that returns the reader to their prior screen without altering their entitlement.
- **FR-005**: Tapping the primary CTA on the Paywall screen MUST grant the reader a premium entitlement immediately, with no real payment processor or network call involved (mock/sandbox purchase, per project scope).
- **FR-006**: After a successful mock purchase, the app MUST navigate to a dedicated Purchase Success screen matching the reference design's screen 8 (confirmation state, benefits recap, "what you were reading" resume card, primary "continue reading" action, secondary "explore library" action).
- **FR-007**: The Purchase Success screen's primary action MUST return the reader to the specific story they were reading when the paywall was triggered, now rendered with full (unlocked) access.
- **FR-008**: Granting entitlement via the mock purchase MUST update the single source of truth used by all premium gating checks, so every other gated surface in the app immediately reflects the new premium status without requiring an app restart.
- **FR-009**: The Paywall screen's "Restore purchases" control MUST give the reader visible feedback (e.g., a message) rather than doing nothing, and MUST NOT grant entitlement in this mock flow.
- **FR-010**: Repeated triggers of the gate (e.g., opening a second locked story after dismissing the paywall once) MUST reopen the Paywall screen each time until the reader is premium.

### Key Entities

- **Entitlement state**: Whether the current user is a premium subscriber; already exists as the app's single source of truth and is read by all gating checks. This feature's purchase action is a new writer to that same state.
- **Paywall trigger context**: The story (and reading position, if available) the reader was viewing when the paywall opened, carried through the paywall and success screens so the success screen can offer to resume it.
- **Plan selection**: Which of the two offered plans (Monthly, Yearly) is currently highlighted on the Paywall screen; a transient UI selection, not persisted.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of premium-gated reading interactions route through the Paywall screen — no surface in the app still shows the old in-place unlock sheet after this feature ships.
- **SC-002**: A reader can go from tapping an unlock action to landing back in their unlocked story in 3 taps or fewer (unlock → select plan (optional, default preselected) → purchase → continue reading).
- **SC-003**: Immediately after the mock purchase completes, 100% of premium checks elsewhere in the running app reflect the new entitlement with no stale "locked" state observed.
- **SC-004**: The Paywall and Purchase Success screens visually match the reference design (screens 4 and 8) in both light and dark themes, per the project's design-system rules.

## Assumptions

- "Paywall isteyen yerler" (places that request the paywall) refers to the existing premium-gating trigger already wired into the reader (currently surfaced as an in-place unlock sheet); this feature redirects that same trigger to the new full-screen Paywall instead of adding new gates.
- The purchase is fully mocked: no real billing SDK, no payment credentials, no network call — tapping the CTA is equivalent to the app locally flipping the existing entitlement flag to premium, consistent with the project's documented "mock billing" cut corner.
- "Restore purchases", "Terms", and "Privacy" are shown for design fidelity but are out of scope to implement as real flows in this feature; Restore gives a simple no-purchases-found response, and Terms/Privacy are not required to open real documents.
- Only one subscription tier ("premium") exists; Monthly and Yearly are pricing variants of the same entitlement, not different feature sets.
- The Paywall can be triggered from more than one place over time (any premium-gated reading surface), so it is implemented as a shared, reusable screen/route rather than something owned by a single feature module's story detail flow.
