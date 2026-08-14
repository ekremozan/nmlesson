# Quickstart: Paywall & Purchase Success

## Prerequisites

- Branch `002-paywall-screen` checked out, `JAVA_HOME` pointed at Android Studio's JBR.

## Validate

```bash
./gradlew :feature:paywall:test :feature:reader:test :core:domain:test
./gradlew installDebug
```

1. Launch the app, open any story that is premium-locked (`Story.isLocked == true`) as a
   non-subscriber.
2. Tap the unlock CTA at the bottom of the reader → Paywall screen (design screen 4) opens,
   Monthly preselected.
3. Tap the Yearly card → selection and CTA price update.
4. Tap the primary CTA → Purchase Success screen (design screen 8) opens immediately, no network
   call; it names the plan and shows the story just left.
5. Tap "Continue reading" → back in the same story, now fully unlocked (no preview truncation, no
   unlock CTA).
6. Open a second locked story → unlock CTA still routes to Paywall on tap for parity, but per
   FR-010 confirm gating itself now reports full access everywhere (i.e. this story wasn't locked
   to begin with, since the reader is premium).
7. Restart the reducer-only check: on the Paywall, tap "Restore purchases" → a message appears,
   entitlement is untouched (verify via a locked story elsewhere if not yet premium).

Refer to [contracts/paywall-navigation.md](contracts/paywall-navigation.md) for the exact
route/callback shapes and [data-model.md](data-model.md) for state shapes.
