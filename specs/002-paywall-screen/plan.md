# Implementation Plan: Paywall & Purchase Success Screens

**Branch**: `002-paywall-screen` | **Date**: 2026-08-14 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-paywall-screen/spec.md`

## Summary

New `:feature:paywall` module with two screens (Paywall = design screen 4, Purchase Success = design
screen 8), reached only via navigation. Reader's existing `PremiumUnlockSheet` in-place sheet is
removed; its trigger becomes a plain nav callback to the Paywall route. Purchase is mocked: tapping
the CTA calls a new `EntitlementRepository.setPremium(true)` and navigates to Success, which resumes
the triggering story fully unlocked.

## Technical Context

**Language/Version**: Kotlin 2.x, Jetpack Compose (existing project toolchain)

**Primary Dependencies**: Navigation-Compose (`@Serializable` routes), Hilt, existing
`EntitlementRepository`/`StoryRepository` from `:core:domain`

**Storage**: N/A (entitlement stays in-memory via `MockEntitlementRepository`, unchanged mechanism)

**Testing**: JUnit + Robolectric for reducers/ViewModels (matches `:feature:reader`), Compose
previews for both themes

**Target Platform**: Android (minSdk 24, compileSdk 36/37)

**Project Type**: Mobile app, multi-module Gradle

**Performance Goals**: N/A beyond existing app defaults

**Constraints**: No real billing SDK or network call (mock purchase, per Assumptions in spec)

**Scale/Scope**: 2 new screens, 1 new feature module, small edits to `:feature:reader`,
`:core:domain`, `:core:data`, `:app`

## Constitution Check

- **II. Clean Architecture**: New `:feature:paywall` depends only on `:core:domain` +
  `:core:designsystem`/`:core:model`, never on `:feature:reader` or `:core:data`. `:app` wires the
  nav graph and is the only module that knows both routes exist. PASS.
- **III. MVI**: Paywall screen gets its own `PaywallContract`/`PaywallReducer` (plan selection,
  purchase intent). Success screen gets its own `PurchaseSuccessContract`/`PurchaseSuccessReducer`
  (folds in the resumed story via an intent, same as Reader's `DetailChanged` pattern). Navigating
  out (close, continue reading, explore library) stays a plain callback, not an intent. PASS.
- **IV. Offline-first / single entitlement source**: purchase writes through the existing
  `EntitlementRepository`, the only place gating reads from — no new `isPremium` check is added
  anywhere. PASS.
- **V. Design system**: screens re-implement screens 4 and 8 from the already-read `NativeMinds Home.dc.html`
  design doc, token-only styling, both themes previewed. PASS.

No violations; Complexity Tracking section not needed.

## Project Structure

### Documentation (this feature)

```text
specs/002-paywall-screen/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── paywall-navigation.md
└── tasks.md   # /speckit-tasks, not this command
```

### Source Code (repository root)

**Structure Decision**: existing Gradle multi-module layout; one new module (`:feature:paywall`),
edits to `:feature:reader`, `:core:domain`, `:core:data`, `:app`.

## Files That Will Change

**New module `:feature:paywall`**
- `feature/paywall/build.gradle.kts`
- `feature/paywall/src/main/AndroidManifest.xml`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallContract.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallReducer.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallViewModel.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallScreen.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessContract.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessReducer.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessViewModel.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessScreen.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/preview/PaywallPreviewData.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/navigation/PaywallRoute.kt`
- `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/navigation/PurchaseSuccessRoute.kt`
- `feature/paywall/src/main/res/values/strings.xml`
- `feature/paywall/src/test/.../ui/paywall/PaywallReducerTest.kt`
- `feature/paywall/src/test/.../ui/success/PurchaseSuccessReducerTest.kt`

**`:core:domain`**
- `core/domain/src/main/kotlin/com/example/nativeminds/domain/repository/EntitlementRepository.kt` — add `fun setPremium(value: Boolean)`
- `core/domain/src/test/kotlin/com/example/nativeminds/domain/FakeEntitlementRepository.kt` — `override` the method

**`:core:data`**
- `core/data/src/main/java/com/example/nativeminds/data/MockEntitlementRepository.kt` — `override` the method (behavior unchanged)

**`:feature:reader`** (remove in-place sheet, replace with nav trigger)
- `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderContract.kt` — drop `isUnlockSheetVisible`/`hasDismissedUnlockSheet`/`UnlockSheetRequested`/`UnlockSheetDismissed`/`SubscribeClicked`/`ShowSubscriptionUnavailable`; `showUnlockAffordance` becomes `isRestricted`
- `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderReducer.kt` — drop matching branches
- `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderScreen.kt` — remove `PremiumUnlockSheet`/sheet state; add `onUnlockRequested: (storyId: Long, progressPercent: Int) -> Unit` plain callback wired to the footer CTA
- `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/PremiumUnlockSheet.kt` — deleted
- `feature/reader/src/main/java/com/example/nativeminds/feature/reader/navigation/ReaderRoute.kt` — `readerScreen()` gains the `onUnlockRequested` param
- `feature/reader/src/main/res/values/strings.xml` — drop now-unused `reader_unlock_*`/`reader_subscription_unavailable` strings
- `feature/reader/src/test/.../ReaderReducerTest.kt`, `ReaderViewModelTest.kt` — drop sheet-related cases

**`:app`**
- `app/build.gradle.kts` — `implementation(project(":feature:paywall"))`
- `app/src/main/java/com/example/nativeminds/navigation/NativeMindsNavHost.kt` — wire `readerScreen(onUnlockRequested = ...)`, add `paywallScreen(...)`, `purchaseSuccessScreen(...)`
- `app/src/androidTest/java/com/example/nativeminds/ReaderJourneyTest.kt` — update for the removed sheet if it asserts on it

**Root**
- `settings.gradle.kts` — `include(":feature:paywall")`
- `README.md` — Key Decisions entry (paywall as its own module + nav-triggered) and Cut Corners entry (mock purchase, Restore purchases inert) per constitution Principle I

## Complexity Tracking

None — no constitution violations.
