# Quickstart: Validating the Reader

How to prove this feature works end to end. Run from the repository root.

## Prerequisites

- Android Studio's bundled JBR exported as `JAVA_HOME` before any Gradle call.
- A connected device or a running emulator for the instrumented checks.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

## 1. Static and unit checks

```bash
./gradlew test lint
```

Expected: green. The suites that matter here are the domain gating tests
(`:core:domain`, see [contracts/domain-contracts.md](contracts/domain-contracts.md)) and the reader
reducer/ViewModel tests (`:feature:reader`, see [contracts/reader-mvi.md](contracts/reader-mvi.md)).

Run only the gating rules while iterating:

```bash
./gradlew :core:domain:test --tests "*ObserveStoryDetailUseCaseTest"
```

## 2. Database migration and DAO

```bash
./gradlew :core:database:connectedDebugAndroidTest
```

Expected: the 1 → 2 migration test opens a version-1 database, migrates, and reads back a
`story_content` row; the content DAO test proves the flow re-emits after an upsert. A missing
exported schema under `core/database/schemas` fails this step.

## 3. Navigation and gating journeys

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected: the existing `HomeGraphTest` still passes, plus the new journeys from
[contracts/navigation.md](contracts/navigation.md) — tap a card, land on the reader, press back and
find Home unchanged.

## 4. Manual walkthrough

```bash
./gradlew installDebug
```

| Step | Expected |
|---|---|
| Tap "The Lighthouse Keeper's Last Letter" | Reader opens with category, minutes, title, author, cover, and the full body; scrolling reaches the last paragraph and the "next in category" line |
| Scroll the body | The footer pill's progress advances; the percentage tracks the position |
| Tap the pill | A message says audio is not available yet — nothing else happens (FR-017) |
| Press back | Home returns with the previous query, category chip and scroll position (FR-001) |
| Type a query, open a result, press back | The query is still in the search field |
| Tap "The Cartographer of Missing Islands" | PREMIUM badge in the header, roughly the first third of the body readable, the text fading out, and the unlock bottom sheet presented (FR-007, FR-008) |
| Try to scroll past the fade or select the hidden text | Impossible — the withheld paragraphs are not on screen at all |
| Tap "Start 7 days free" | A message says subscription is not available yet; the story stays locked (FR-016) |
| Dismiss the sheet | The faded preview remains, and a control to bring the sheet back stays visible (FR-009) |
| Enable airplane mode, reopen a story read earlier | It renders fully, with no error (FR-015) |
| Open a story whose content was never stored, offline | The offline state with a retry control appears instead of a blank body (FR-004) |
| Turn the network back on and tap retry | The body loads and replaces the message |
| Switch the system theme while reading | Both light and dark render correctly and the reading position is kept (FR-013) |

To see the subscriber path, drive the entitlement source directly — there is no in-app way to
subscribe in this feature by design (research R6, R10).

## 5. Design verification

Open `ReaderScreen.kt` and `PremiumUnlockSheet.kt` in Android Studio's preview pane. Every
composable has one, and the screen-level previews render the light/dark pair for each state
(full access, restricted, loading, offline, error) from a `PreviewParameterProvider`. Compare
against the design source's Reader 2a/2b screens before calling the screen done.
