---

description: "Task list for Paywall & Purchase Success Screens"
---

# Tasks: Paywall & Purchase Success Screens

**Input**: Design documents from `/specs/002-paywall-screen/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/paywall-navigation.md, quickstart.md

**Tests**: Included — Constitution III requires unit tests for reducer/business logic; existing `:feature:reader` convention (reducer + ViewModel tests) is followed for the new module.

**Organization**: Tasks are grouped by user story (US1/US2/US3 map to spec.md priorities P1/P1/P3).

## Format: `[ID] [P?] [Story] Description`

## Phase 1: Setup

- [X] T001 Add `include(":feature:paywall")` to `settings.gradle.kts`
- [X] T002 Create `feature/paywall/build.gradle.kts` (copy `:feature:reader`'s plugin/dependency shape: compose, hilt, navigation, serialization, robolectric test deps)
- [X] T003 ~~Create `feature/paywall/src/main/AndroidManifest.xml`~~ — skipped: no sibling feature module has one, AGP derives it from `namespace`
- [X] T004 [P] Add `implementation(project(":feature:paywall"))` to `app/build.gradle.kts`

**Checkpoint**: module compiles empty, included in the build.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: shared contract surface every user story writes to or reads from.

- [X] T005 [P] Add `fun setPremium(value: Boolean)` to `core/domain/src/main/kotlin/com/example/nativeminds/domain/repository/EntitlementRepository.kt`
- [X] T006 [P] Add `override` to the matching method in `core/domain/src/test/kotlin/com/example/nativeminds/domain/FakeEntitlementRepository.kt`
- [X] T007 [P] Add `override` to the matching method in `core/data/src/main/java/com/example/nativeminds/data/MockEntitlementRepository.kt`
- [X] T008 [P] Create `PurchasePlan` enum (`MONTHLY`, `YEARLY`) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PurchasePlan.kt`
- [X] T009 [P] Create `PaywallRoute(storyId: Long, progressPercent: Int)` in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/navigation/PaywallRoute.kt`
- [X] T010 [P] Create `PurchaseSuccessRoute(storyId: Long, progressPercent: Int, plan: PurchasePlan)` in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/navigation/PurchaseSuccessRoute.kt`
- [X] T011 Create `feature/paywall/src/main/res/values/strings.xml` with the screen-4 and screen-8 copy from the design doc (headline, benefits, plan labels, CTA, restore/terms/privacy, success headline/body/recap/resume-card/actions)

**Checkpoint**: shared types/strings exist; every user story phase below can compile against them.

---

## Phase 3: User Story 1 - See the paywall when a premium story is gated (Priority: P1) 🎯 MVP

**Goal**: non-subscriber tapping the reader's unlock CTA lands on a full-screen Paywall (design screen 4) with selectable Monthly/Yearly plans, replacing the old in-place sheet.

**Independent Test**: open a locked story as a non-subscriber, tap the unlock CTA, confirm the Paywall screen opens with both plans and a working close control back to the reader.

### Implementation for User Story 1

- [X] T012 [P] [US1] Create `PaywallContract.kt` (`PaywallUiState{selectedPlan}`, `PaywallIntent.PlanSelected`, close as plain callback) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallContract.kt`
- [X] T013 [US1] Create `PaywallReducer.kt` handling `PlanSelected` in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallReducer.kt` (depends on T012)
- [X] T014 [P] [US1] Create `PaywallReducerTest.kt` covering plan selection in `feature/paywall/src/test/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallReducerTest.kt` (depends on T013)
- [X] T015 [US1] Create `PaywallViewModel.kt` (Hilt, single `onIntent`) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallViewModel.kt` (depends on T013)
- [X] T016 [US1] Create `PaywallScreen.kt` (screen 4: hero, headline, benefits list, Monthly/Yearly cards, CTA, restore/terms/privacy row, `@ScreenThemePreviews`) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallScreen.kt` (depends on T015)
- [X] T017 [P] [US1] Create `PaywallPreviewData.kt` fixtures in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/preview/PaywallPreviewData.kt` (depends on T012)
- [X] T018 [US1] Create `PaywallNavigation.kt` (`NavGraphBuilder.paywallScreen(onClose, onPurchased)`) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/navigation/PaywallNavigation.kt` (depends on T016, T009)
- [X] T019 [US1] Trim `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderContract.kt`: remove `isUnlockSheetVisible`, `hasDismissedUnlockSheet`, `UnlockSheetRequested`, `UnlockSheetDismissed`, `SubscribeClicked`, `ShowSubscriptionUnavailable`; `showUnlockAffordance` becomes `isRestricted`
- [X] T020 [US1] Trim matching branches in `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderReducer.kt` (depends on T019)
- [X] T021 [US1] Update `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderScreen.kt`: remove `PremiumUnlockSheet`/sheet state, add `onUnlockRequested: (storyId: Long, progressPercent: Int) -> Unit` plain callback wired to the footer CTA (depends on T020)
- [X] T022 [US1] Delete `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/PremiumUnlockSheet.kt` (depends on T021)
- [X] T023 [US1] Update `feature/reader/src/main/java/com/example/nativeminds/feature/reader/navigation/ReaderRoute.kt`: `readerScreen()` gains `onUnlockRequested` and forwards it (depends on T021)
- [X] T024 [US1] Remove now-unused `reader_unlock_*`/`reader_subscription_unavailable` entries from `feature/reader/src/main/res/values/strings.xml` (depends on T022)
- [X] T025 [P] [US1] Update `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderReducerTest.kt` to drop sheet-related cases (depends on T020)
- [X] T026 [P] [US1] Update `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderViewModelTest.kt` to drop sheet-related cases (depends on T021)
- [X] T027 [US1] Wire `readerScreen(onUnlockRequested = { storyId, progress -> navController.navigate(PaywallRoute(storyId, progress)) })` and `paywallScreen(onClose = navController::navigateUp, ...)` in `app/src/main/java/com/example/nativeminds/navigation/NativeMindsNavHost.kt` (depends on T018, T023)

**Checkpoint**: locked story → unlock CTA → Paywall screen → close returns to reader, fully working end-to-end.

---

## Phase 4: User Story 2 - Confirm the purchase and land on success (Priority: P1)

**Goal**: tapping the Paywall CTA grants mock premium entitlement and opens the Purchase Success screen (design screen 8), which resumes the triggering story fully unlocked.

**Independent Test**: from the Paywall, tap the CTA; confirm entitlement flips to premium immediately, the Success screen names the plan and shows the originating story, and "Continue reading" returns to that story unlocked.

### Implementation for User Story 2

- [X] T028 [P] [US2] Create `PurchaseSuccessContract.kt` (`PurchaseSuccessUiState{storyId, progressPercent, plan, story}`, `DetailChanged` intent) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessContract.kt`
- [X] T029 [US2] Create `PurchaseSuccessReducer.kt` in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessReducer.kt` (depends on T028)
- [X] T030 [P] [US2] Create `PurchaseSuccessReducerTest.kt` in `feature/paywall/src/test/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessReducerTest.kt` (depends on T029)
- [X] T031 [US2] Create `PurchaseSuccessViewModel.kt` (Hilt, folds `StoryRepository.story(storyId)` in as `DetailChanged`) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessViewModel.kt` (depends on T029)
- [X] T032 [US2] Create `PurchaseSuccessScreen.kt` (screen 8: confirmation state, benefits recap, resume card, "Continue reading"/"Explore library" actions, `@ScreenThemePreviews`) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/success/PurchaseSuccessScreen.kt` (depends on T031)
- [X] T033 [P] [US2] Create `PurchaseSuccessPreviewData.kt` fixtures in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/preview/PurchaseSuccessPreviewData.kt` (depends on T028)
- [X] T034 [US2] Create `PurchaseSuccessNavigation.kt` (`NavGraphBuilder.purchaseSuccessScreen(onContinueReading, onExploreLibrary)`) in `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/navigation/PurchaseSuccessNavigation.kt` (depends on T032, T010)
- [X] T035 [US2] Add purchase handling to `PaywallContract.kt`/`PaywallReducer.kt`/`PaywallViewModel.kt`: CTA tap calls `EntitlementRepository.setPremium(true)` then raises `onPurchased(storyId, progressPercent, selectedPlan)` (depends on T013, T015, T005)
- [X] T036 [US2] Wire `paywallScreen(onPurchased = { storyId, progress, plan -> navController.navigate(PurchaseSuccessRoute(storyId, progress, plan)) { ... } })` and `purchaseSuccessScreen(onContinueReading, onExploreLibrary)` in `app/src/main/java/com/example/nativeminds/navigation/NativeMindsNavHost.kt` (depends on T027, T034, T035)
- [X] T037 [US2] Update `app/src/androidTest/java/com/example/nativeminds/ReaderJourneyTest.kt` for the new unlock → paywall → success journey (depends on T036)

**Checkpoint**: full purchase loop works — unlock → paywall → purchase → success → resumed, unlocked story.

---

## Phase 5: User Story 3 - Restore purchases affordance is present but inert (Priority: P3)

**Goal**: tapping "Restore purchases" on the Paywall gives visible feedback without granting entitlement.

**Independent Test**: tap "Restore purchases" on the Paywall; confirm a "no purchase found" message appears and entitlement is unchanged.

### Implementation for User Story 3

- [X] T038 [US3] Add `RestorePurchasesClicked` intent and a one-shot effect to `PaywallContract.kt` (depends on T012)
- [X] T039 [US3] Handle `RestorePurchasesClicked` in `PaywallReducer.kt` by raising the effect, no state/entitlement change (depends on T038, T013)
- [X] T040 [US3] Wire the "Restore purchases" tap and effect-channel snackbar in `PaywallScreen.kt` (depends on T016, T039)
- [X] T041 [P] [US3] Add the "no purchase found" string to `feature/paywall/src/main/res/values/strings.xml` (depends on T011)
- [X] T042 [US3] Add a restore-purchases case to `PaywallReducerTest.kt` (depends on T039, same file as T014)

**Checkpoint**: all three user stories independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T043 [P] Add README "Key Decisions" (paywall as its own nav-reached module) and "Cut Corners / Assumptions" (mock purchase, inert Restore/Terms/Privacy) entries in `README.md`
- [ ] T044 ~~Run the manual walkthrough in `specs/002-paywall-screen/quickstart.md` end-to-end on device/emulator~~ — blocked: no Android emulator/`adb` available in this environment; compile + unit tests + lint verified instead
- [X] T045 [P] Run `./gradlew :feature:paywall:test :feature:reader:test :core:domain:test :core:data:test :feature:paywall:lintDebug :feature:reader:lintDebug :app:lintDebug` — all green

---

## Dependencies & Execution Order

- **Setup (Phase 1)** → **Foundational (Phase 2)**: blocks everything below.
- **US1 (Phase 3)**: MVP; depends only on Foundational.
- **US2 (Phase 4)**: depends on Foundational and on US1's `PaywallContract`/`PaywallReducer`/`PaywallViewModel`/`PaywallScreen` and nav wiring (T012–T018, T027) being in place, since it extends the same Paywall files (T035) and chains off T027's nav host edit (T036).
- **US3 (Phase 5)**: depends on US1's Paywall files (T012, T013, T016) the same way.
- **Polish (Phase 6)**: depends on all desired stories being complete.

### Parallel Opportunities

- Setup: T004 alongside T001–T003.
- Foundational: T005–T010 are all different files and can run in parallel; T011 after T008–T010 exist conceptually but has no code dependency, so it can also run in parallel.
- US1: T012 and T017 in parallel; T014, T025, T026 in parallel once their respective dependencies land.
- US2: T028, T030, T033 in parallel with each other.
- US3: T041 in parallel with T038–T040.
- Polish: T043 and T045 in parallel.

---

## Implementation Strategy

### MVP First

1. Phase 1 Setup → Phase 2 Foundational → Phase 3 (US1).
2. **STOP and VALIDATE**: locked story → Paywall → close, working independently of any purchase logic.

### Incremental Delivery

1. Setup + Foundational → Foundation ready.
2. US1 → Paywall reachable and closable (MVP demo of the screen itself).
3. US2 → full purchase loop, the feature's actual value.
4. US3 → Restore purchases polish, lowest priority, first to cut if out of time.
5. Polish → README documentation gates (Constitution I) and test/lint pass.
