# NativeMinds — Bite-Sized Story App (Mobile Case Study)

> A short-story reading & listening app: browse and search stories, read or listen, free users get a taste, subscribers unlock everything — including an AI-powered feature.

<!-- TODO: 1-2 cümlelik final ürün tanımı + ekran görüntüleri / GIF -->

## 📱 Live Demo

- **APK / Store link:** _TODO (deploy edilince eklenecek)_
- **Demo video (2–5 min):** _TODO_
- **Architecture diagram (Miro/Excalidraw):** _TODO link_

## ✨ Features

<!-- Tamamlandıkça işaretle -->
- [x] Story list — browse & search
- [x] Story reader (read experience) — full text, drop cap, reading progress, offline/error states
- [ ] Listen mode (audio / TTS) — the control is drawn and says it is not available yet
- [x] Free vs Premium gating — 30% preview + full-screen Paywall/Purchase-Success flow (mock purchase)
- [ ] AI feature: _TODO — hangi AI özelliği, neden_
- [x] Offline support (cached stories readable without network)
- [ ] Analytics (key funnel events) — deferred; see Cut Corners
- [x] Crash / error reporting — every failure routed through an `ErrorReporter` seam

## 🏗 Architecture

<!-- Diagram buraya embed edilecek + 1 paragraf özet -->

**Stack:** Kotlin, Jetpack Compose (Material 3), Room, Paging 3, Hilt _TODO: Media3, Firebase_

**Shape:** Clean Architecture + MVI with unidirectional data flow, offline-first (local DB as
source of truth), dependencies resolved by Hilt at compile time.

**Modules** — the arrows only ever point inwards, towards the domain:

```
:app             composition root — owns NativeMindsNavHost, the only module that knows
                 which implementations and which destinations exist
  ├── :feature:home     Compose screens + MVI ViewModels → :core:domain, :core:designsystem
  ├── :feature:reader   Reader screen, its own route declaration
  ├── :feature:paywall  Paywall + Purchase Success screens, mock purchase, its own routes
  ├── :core:data        RoomStoryRepository, entitlement, observability, @Binds
  └── :core:database    Room entities, DAOs, migrations, DatabaseModule

:core:domain     repository contracts + use cases        (pure Kotlin, no Android)
:core:model      Story, StoryContent                     (pure Kotlin)
:core:common     dispatcher qualifiers, @ApplicationScope
:core:designsystem  theme tokens, shared components
```

The point of the split: `:feature:home` depends on `:core:domain`, never on `:core:data`. It knows
the `StoryRepository` contract and nothing about Room, so the data layer can be replaced without
recompiling any feature. `:app` is the single place where contract meets implementation.

<!-- TODO: Miro/Excalidraw diyagramı, veri akışı -->

## 🧠 Key Decisions & Reasoning

<!-- Her önemli karar için: ne seçtim, neden, trade-off ne, 10× ölçekte ne değişirdi -->

| Decision | What I chose | Why | Trade-off / at 10× scale |
|---|---|---|---|
| Platform & UI | Native Android, Compose | _TODO_ | _TODO_ |
| Architecture pattern | **MVI**, not MVVM: one `onIntent(HomeIntent)` entry point, a pure `HomeUiState.reduce(intent)` as the only writer of state, and a one-shot `HomeEffect` channel alongside it | MVVM's N-public-functions-per-ViewModel shape does not survive the screens still to come — Reader, Audio player, Paywall and Ask-AI all carry navigation, snackbars and error states, so the choice was to discover the same event plumbing four times or to define it once. The reducer is the concrete win today: it is pure and Android-free, so the screen's whole behaviour is verifiable without `Dispatchers.setMain`, a fake repository, or a coroutine test scope. The effect channel is the other: the app had no way at all to say "something failed" before it | Honestly a net negative at one screen — a sealed interface and a `when` branch per tap buys little when there are four taps. The bet is on the roadmap, not on today. At 10× the reducer stops being one `when` and splits per concern (filtering, playback, entitlement), and the intent stream becomes the natural place to hang analytics and a crash-time breadcrumb trail, which per-method callbacks cannot offer |
| MVI contract placement | Feature-local (`HomeContract.kt` in `:feature:home`), **not** a shared `BaseViewModel` in a core module | A shared generic base would have to live somewhere: `:core:common` would push `androidx.lifecycle` onto `:core:data`'s compile classpath, and a new `:core:mvi` module is real infrastructure to justify with one consumer. There is also a design cost — `pagedStories` deliberately sits *outside* the state (a `PagingData` snapshot has no business inside an equatable state object), and a generic `MviViewModel<S, I, E>` invites forcing it in | Screen two and three will repeat ~15 lines of channel and reducer wiring. That repetition is the signal to extract, and by then there will be three real call sites shaping the abstraction instead of one imagined one |
| State shape | `HomeUiState` holds only irreducible facts (`query`, `selectedCategory`, `categories`); `chips`, `suggestions` and `isFiltering` are computed properties | The reducer cannot leave a derived field stale if there is no derived field to update — chip selection drifting from `selectedCategory` becomes unrepresentable rather than merely tested-for. It also keeps `equals` meaningful: two states are equal when the facts match | The computed lists are new instances on every read, so Compose cannot skip a recomposition of the chip row on an unrelated state change. Measured against a five-item row this is noise; if a screen ever derives something expensive, that one moves back into the reducer as an eagerly-computed field |
| One-shot events | `Channel(BUFFERED)` + `receiveAsFlow()`, not `MutableSharedFlow` | A `SharedFlow` with no replay drops events emitted while the screen is stopped — `tryEmit` returns `true` and the snackbar silently never happens, which is exactly the failure mode the project's "no silently swallowed" rule exists to prevent. A channel buffers until the collector returns, and its single-consumer semantics make double navigation from two collectors impossible | Round-robin delivery if a second collector ever subscribes. With one screen and one collector that is a guarantee, not a limitation. No replay also means a snackbar is not re-shown across a configuration change, which is the behaviour we want |
| Dependency injection | **Hilt**, not Koin or hand-rolled providers | The graph is verified at **compile time**. In a multi-module project the realistic failure is forgetting to register a new feature's bindings — with Hilt that doesn't compile; with Koin's runtime service locator it compiles fine and throws `NoBeanDefFoundException` when the user opens the screen. That matches the project rule that failures must be visible. KSP was already in the build for Room, so the marginal cost was small, and Hilt brings `hiltViewModel()`, `SavedStateHandle` and WorkManager integration for free | Steeper learning curve and an extra KSP round per module. Koin would have won on KMP support — irrelevant here, the app is Android-only. At 10× the single `SingletonComponent` stops being enough: long-lived, expensive dependencies (player, AI client) move to their own scopes, and feature modules get `@InstallIn(ViewModelComponent::class)` bindings so they don't all inflate the app component |
| Layering | `StoryRepository` contract and use cases live in a pure-Kotlin `:core:domain`; `:core:data` implements it; `:app` is the only module that wires the two together | Makes the dependency arrow point inwards for real rather than on a diagram: `:feature:home` cannot reference Room or `RoomStoryRepository` even by accident, because they are not on its compile classpath. Use cases also give premium gating a home that is neither the ViewModel nor the DAO | Two extra modules and a thin use-case layer that is nearly pass-through today. At 10× that thinness is the point — gating, entitlement checks and AI pre/post-processing land in the use case without any feature module changing |
| Category chips | Derived from the stories table (`GROUP BY category ORDER BY COUNT(*) DESC`) instead of a hardcoded list in the ViewModel | A second, hand-maintained list of categories is a copy that drifts: a sync introducing a new category would leave it invisible, and one emptying out would leave a chip that returns nothing. The same ordered query feeds the empty state's suggestions, so "suggested" means "most content" rather than an arbitrary pick | Chip order can shift as content changes, which costs some muscle memory. At 10× the `GROUP BY` over the whole table stops being free — it becomes a maintained `categories` table (or a curated, server-supplied order, which is what an editorial team would want anyway) |
| "All" chip | Modelled as `category = null` with the label in a string resource, not as the string `"All"` | The literal was doing two jobs — visible UI text and the "no filter" sentinel — so it was both unlocalizable and spread as `category != "All"` comparisons through the ViewModel | `ChipUiModel` and two callback signatures take a nullable, which reads slightly heavier than a plain `String` |
| Dispatchers | Injected via `@IoDispatcher` / `@DefaultDispatcher` qualifiers instead of calling `Dispatchers.IO` inline | Repository tests can bind a `TestDispatcher` and control the clock; without it, `withContext(Dispatchers.IO)` is untestable by construction | One more indirection for a two-line function. Pays off as soon as sync gets retry/backoff logic worth testing |
| Module visibility (`api` vs `implementation`) | `api` only where a type appears in a consumer's own signatures: `:core:model` and `paging-common` from `:core:domain`, the dispatcher qualifiers from `:core:common`, the domain contracts from `:core:data`. Everything else is `implementation` | `api` is a promise to leak a type upward, so it's spent deliberately rather than by default. Room is the case that matters: it stays an `implementation` detail of `:core:database`, which is why no feature module can touch an entity even by accident. The one deliberate exception is `:app` depending on `:core:database` — Hilt's generated component has to be able to name `StoryDao`. Using `api` on `:core:data` instead would have leaked Room to every consumer, which is precisely what the layering exists to prevent | Adding a genuinely shared type means touching two build files instead of one. At 10× this is what keeps module build times from collapsing into one big recompile |
| Domain module purity | `:core:domain` takes `paging-common` (not `paging-runtime`) and `javax.inject` + plain `dagger` (never `hilt-android`) | The domain layer must compile as pure JVM — that constraint is what makes "no Android in domain" enforceable by the compiler instead of by review. `PagingData`/`PagingSource` live in the pure-JVM paging artifact, so paging contracts can sit in domain without dragging Android in | The pure-JVM subset is smaller and occasionally forces a workaround. See "Cut Corners" for the `dagger` dependency trade-off |
| compileSdk vs targetSdk | `compileSdk` 37, `targetSdk` 36 | AndroidX (core 1.19, lifecycle 2.11) requires compiling against API 37. Compiling against newer APIs is a separate decision from opting into the new runtime behavior — the latter changes how the app behaves in users' hands and deserves its own testing pass | The app doesn't get API 37's behavioral improvements until the target is raised. Deliberate: it's a decision to make with a test matrix, not a version-bump side effect |
| Window insets | Applied once at the app root in `MainActivity`, not per screen | Every screen otherwise repeats `statusBarsPadding()`/`navigationBarsPadding()`, and the one that forgets ships content under the status bar | A screen wanting edge-to-edge content (a full-bleed reader or cover image) has to opt out of the root padding rather than opt in |
| Home screen scrolling | Greeting, search field, category chips and the section heading sit outside the `LazyColumn`; only story cards scroll | Search and filtering stay reachable at any scroll position — on a browse screen, scrolling down is exactly when you decide to filter | The header costs vertical space permanently. A collapsing toolbar would recover it, at the cost of a scroll-linked animation that has to stay smooth while paging |
| Preview strategy | One `@Composable` per matrix: themes from a multipreview annotation (`@ThemePreviews` / `@ScreenThemePreviews`), states from a `PreviewParameterProvider` | Hand-writing light × dark × state as N copies of `@Preview` is boilerplate that drifts — a new state gets added to two of the four copies. Fixtures live in one file per feature so a UI-model change breaks one file, not six | Preview parameters make the tool's preview list longer and slower to render. Worth it: adding a state (loading, offline, premium-locked) is one line in the provider |
| Code comments | No `//` comments in the codebase; rationale lives here in this table instead, KDoc only on public declarations | Rationale in a comment is invisible to anyone reading the README and rots silently next to the code it explains. Naming and structure carry the *what*; the *why* belongs in a document that gets read during review | Someone reading a build file in isolation has to come here to learn why a dependency is `api`. Accepted: this table is the single place decisions are defended, which is also what the case study asks for |
| Design tokens | Ported the design system into a typed Compose token layer (`ui/theme/`) rather than styling per screen | One place to change a color or a text style; a palette regression is caught in `ThemePreview.kt` instead of on a device | More indirection for a small app. At 10× (multiple squads, more surfaces) this becomes a shared `:design-system` module with screenshot tests per component |
| Material 3 vs. custom | Material 3 scheme for standard roles + a small `NativeMindsColors` for what M3 has no slot for | Keeps M3 components (ripples, text fields, sheets) correct for free while still allowing brand roles the spec doesn't model | Two places to look up a color. The rule "standard → MaterialTheme, brand → NativeMindsTheme" is documented in CLAUDE.md to keep it unambiguous |
| Dynamic color | Disabled | The paper-and-terracotta ground and serif reading voice *are* the product's atmosphere; repainting them from wallpaper trades identity for a personalization win a reading app doesn't benefit from | Users who expect Material You theming don't get it |
| Error color | Mapped M3 `error` onto the deep terracotta ramp instead of adding a red | The design system has no red — warnings use terracotta, success uses sage. A stock red would read as a system dialog dropped into the app | Less "alarming" than a red; acceptable because the app has no destructive actions so far |
| Fonts | Bundled static instances of Caprasimo / Newsreader / Figtree (OFL) instead of downloadable fonts | The type is part of the brand — it has to be right on first launch and offline, which a provider round-trip can't guarantee. Static instances (not variable) because variable axes need API 26 and `minSdk` is 24 | ~400 KB of APK. At scale: ship only the weights in use (already done), and revisit variable fonts once `minSdk` ≥ 26 |
| Accent split | Two accents: `primary` (#C67139) for fills, `accentText` (#B2622D) for glyphs | The fill accent only reaches ~2.7:1 on the paper ground — it fails WCAG AA as text | One more token to reason about, in exchange for accessible accent text everywhere |
| **Reader feature** — full reasoning in [`specs/001-story-detail-reader/research.md`](specs/001-story-detail-reader/research.md), one line each below | | | |
| Navigation | `navigation-compose`, type-safe `@Serializable` routes, each feature owns its route | App had none yet; typed route gives free `SavedStateHandle` restoration | Two new deps for one screen — bet on the screens still to come |
| Story tap → reader | Plain callback, not a `HomeIntent` | Changes no state, so an intent would force a forbidden ViewModel branch | Reads as an exception to "every action is an intent" — see research.md R1 |
| Reader reducer returns effects | `Reduction(state, effects)`, unlike `HomeReducer`'s bare state | Only the reducer may decide intent → effect without the ViewModel branching | Two reducer shapes until Home needs its own effect — see research.md R2 |
| Story content storage | Separate `story_content` table, not columns on `stories`; DB 1→2 with a written migration | Keeps kilobyte bodies out of the paged list query | See research.md R3 |
| Premium preview rule | `ReaderAccess.Preview` has no field for the withheld text | A leak-proof type beats a flag a UI bug could ignore | See research.md R4 |
| Entitlement & observability | `EntitlementRepository`/`ErrorReporter` interfaces in `:core:domain`, mock/logcat behind them; analytics deferred entirely | Interface has to be right now, real backend is a later `@Binds` swap | See Cut Corners |
| Robolectric + `NetworkMonitor` interface | Test-only dependency; `NetworkMonitor` turned into an interface | `SavedStateHandle`/Room need a real `Bundle`/DB in tests; offline behavior needs a settable input | One more test dependency and binding |
| Audio approach (TTS vs pre-generated) | _TODO_ | _TODO_ | _TODO_ |
| AI feature design | _TODO_ | _TODO_ | _TODO_ |
| Analytics & crash reporting | Crash/error reporting shipped this feature (`ErrorReporter` seam); analytics deferred | Covered by the "Observability" row above | — |
| **Paywall feature** — full reasoning in [`specs/002-paywall-screen/research.md`](specs/002-paywall-screen/research.md), one line each below | | | |
| Paywall UI | Full-screen `:feature:paywall` module (screens 4 + 8 of the design), replacing the reader's in-place `ModalBottomSheet` | The gate needed to be reachable from any premium surface, not owned by `:feature:reader`; a shared destination has to be its own module per the "no feature depends on another feature" rule | The old bottom-sheet row above no longer applies — superseded here rather than deleted, so the "why we changed it" stays visible |
| Reader → Paywall trigger | Plain callback (`onUnlockRequested`) resolved in `:app`'s nav graph, not a `ReaderIntent` | Same rule as "Story tap → reader": leaving the screen is the graph's concern, not local state | The reader no longer owns any unlock-sheet state at all — `isRestricted` is the only thing left |
| Granting the mock entitlement | `EntitlementRepository.setPremium(Boolean)` promoted onto the domain interface itself, called directly from `PaywallViewModel.onIntent` keyed on `PurchaseClicked` | `:feature:paywall` can only depend on `:core:domain`, so the write path has to live on the interface it already has; no use case was worth inventing for a flag write with no business rule | The one place a ViewModel does a repository side effect keyed on a specific intent rather than only forwarding effects — a deliberate, narrow exception, not a pattern to spread |
| Paywall hero "washed" covers | `alpha`, not the design's `blur(1.5px)` | `Modifier.blur` is a silent no-op below API 31 and `minSdk` is 24 — a third of the supported range would have simply lost the effect with no signal. Alpha reads the same everywhere | Slightly flatter than the comp. Revisit once `minSdk` ≥ 31 |
| Paywall bottom block | Plan cards + CTA + footer links pinned outside the scroll region (`weight(1f)` on the scrolling part, same shape as `HomeScreen.kt:153`) | The design's `margin-top:auto` footer only works because the comp is a fixed 844px canvas; on real devices a single scroll leaves the CTA floating mid-screen on tall phones and unreachable on short ones | The top region can get tight on very small screens — it scrolls, which is the right trade for keeping the purchase action always visible |
| Carrying story context across screens | `storyId`/`progressPercent` (and `plan`) travel as `@Serializable` nav-route arguments from `PaywallRoute` to `PurchaseSuccessRoute` | Matches the existing `ReaderRoute(storyId)` convention; two primitives don't justify a shared flow/ViewModel scope | Success screen re-fetches the story from `StoryRepository` rather than trusting stale reader state |
| Subscription / gating model | Single entitlement source of truth + a domain-side preview rule; purchase is fully mocked (no billing SDK, no network call) and says so on the success screen | Covered by the "Premium preview rule", "Entitlement", and "Granting the mock entitlement" rows | At 10× scale the mock purchase becomes a real Play Billing / App Store integration behind the same `EntitlementRepository.setPremium` call site |

## 🤖 How I Worked With AI

<!-- Case Part 2: AI'ı nasıl yönlendirdim -->

### Setup
<!-- TODO: Claude Code, CLAUDE.md kuralları, custom command/agent/MCP vs. kısaca -->

### How I framed & steered
<!-- TODO: problemi nasıl çerçeveledim, ne istedim, ilk cevap yanlışken nasıl iterate ettim — 2-3 somut örnek -->

### Where I overrode the AI
<!-- TODO: AI çıktısını reddedip kendi yolumla yaptığım yerler — somut örnekler -->

Reader feature (spec-driven pass, `specs/001-story-detail-reader/`):

- **Rejected: "story tapped" as an MVI intent.** The obvious reading of the project's own rule
  ("every user action is an intent") produced a design with an identity reducer and a `when` in the
  ViewModel just to emit a navigation effect — which the *next* rule in the same document forbids.
  Held both rules against each other, decided navigation out of a screen is the graph's concern, and
  wrote the reasoning down rather than letting the exception look like sloppiness.
- **Rejected: branching on intents in the ViewModel to raise effects.** The reader genuinely needs
  intent-driven effects. Instead of the shortcut, changed the reducer to return
  `Reduction(state, effects)` so the pure function keeps owning every consequence of an intent.
- **Rejected: "full text + isTruncated flag" for the paywall.** That shape lets a UI bug leak paid
  content. Replaced with a sealed `ReaderAccess.Preview` that has nowhere to put the withheld
  paragraphs, and added an instrumented test asserting the withheld text is not in the tree at all.
- **Rejected: an inert overflow button** in the reader's top bar, copied from the design. A control
  that answers a tap with nothing contradicts the project's "failures must be visible" instinct;
  omitted it and kept the layout balanced with a spacer.
- **Caught by running it, not by reading it:** the first build put the floating listen pill over
  live text with no scrim, and the progress bar read 57% before any scrolling (the formula counted
  visible items instead of scroll position). Both were only visible in a screenshot on an emulator —
  a reminder that "the tests pass" and "the screen is right" are different claims.
- **Corrected after the fact: analytics was added without asking.** CLAUDE.md's constitutional
  observability requirement ("log key funnel events: content viewed... paywall shown...") was read
  as license to bake `content_viewed`/`paywall_shown` logging straight into the spec's functional
  requirements, without surfacing it as a choice the way the two genuine `[NEEDS CLARIFICATION]`
  items were. Removed on request; crash/error reporting was kept because it maps to a stricter,
  unambiguous project rule ("no silently swallowed exceptions") rather than an additive one.
  Lesson: a constitutional *requirement* still has scope decisions inside it (which events, when)
  that are the user's to make, not mine to assume.

## ✂️ Cut Corners & Assumptions

<!-- Bilerek kısılan köşeler — anında buraya ekle, sona bırakma -->
- _TODO: örn. mock billing (gerçek Play Billing yerine), seed content, ..._
- **Entitlement is an in-memory mock.** `MockEntitlementRepository` holds a `MutableStateFlow(false)`
  and resets with the process. Nothing in the app can grant a subscription yet, so there is nothing
  to persist; the demo drives it directly. Replacing it is one `@Binds`.
- **The purchase is fully mocked.** The Paywall's "Subscribe now" button calls
  `EntitlementRepository.setPremium(true)` directly — no billing SDK, no payment credentials, no
  network call. It is a genuine state change (every gated surface sees it immediately), just not a
  real one; replacing it is one call site once real billing lands.
- **Restore purchases, Terms, and Privacy are inert.** They match the design for fidelity, but
  Restore only shows a "no purchase found" message and Terms/Privacy do not open a document — there
  is no real store or legal copy to point at yet.
- **The listen control does not play anything.** The pill and its progress bar are drawn as
  designed, the progress tracks the reader's position *in the text*, and a tap says playback is not
  available yet. Audio is its own feature.
- **The reader's overflow menu is omitted, not drawn inert.** Font size and theme controls belong to
  a later feature; a spacer keeps the title optically centred until they exist.
- **Analytics is deferred entirely.** No funnel events (`content_viewed`, `paywall_shown`, etc.) are
  recorded yet — cut to keep this feature's scope to what was asked for. Planned as a follow-up: the
  domain-level `AnalyticsLogger` seam pattern already exists in this codebase's design (mirrors
  `ErrorReporter`) and is the shape to reintroduce it in.
- **Crash/error reporting is logcat-only.** `LogcatErrorReporter` implements the real interface and
  every call site reports through it, but nothing leaves the device yet. Firebase is a separate
  build-and-account step.
- **Story text is hand-seeded.** `DummyStoryContentSeed` stands in for a catalog, and
  `FakeRemoteStoryDataSource.fetchContent` serves the same text back as if it were a fetch.
- **The closing line says "Next in Fiction" without naming the next story.** The design names it;
  resolving an actual next story needs an ordering rule the app does not have yet.
- `GetPagedStoriesUseCase` / `SyncStoriesUseCase` are currently pass-throughs to the repository.
  They exist as the seam premium gating will occupy, not because they add logic today.
- The whole graph lives in Hilt's `SingletonComponent`. Correct while every dependency is cheap and
  process-lived; the audio player and AI client will need narrower scopes.
- On a first launch the filter row shows only "All" for the instant between the screen appearing
  and the seed landing in Room — the chips follow the database, and it is briefly empty.
- `:core:domain` depends on `com.google.dagger:dagger` (pure JVM) so its `@Inject` factories are
  generated locally. `javax.inject` alone would have been purer, at the cost of moving factory
  generation into `:app` and a compiler warning per class.
- Sync failures now reach the user as a snackbar via `HomeEffect.ShowSyncError`; the repository no
  longer swallows them. The offline-first contract is unchanged — the seeded or previously synced
  database stays the source of truth and the screen keeps working. What is still missing is the
  other half of the rule: the exception is not reported anywhere, because crash reporting is not
  wired up yet. `HomeViewModel`'s `runCatching` is where that call goes.
- `StoryCard`'s `onClick` is still an empty lambda rather than a `HomeIntent.StoryClicked`. There is
  no navigation and no Reader screen for it to reach, and an intent whose reducer branch returns
  `this` and whose effect nobody consumes is dead code that reads like a feature.
- The visible snackbar has no `@Preview`. A `Snackbar` only appears in response to a `showSnackbar`
  call on a `SnackbarHostState`, which a static preview never makes; the preview would render an
  empty host and prove nothing. This is the "written reason" the preview rule asks for.
- `HomeUiState` is no longer shared with `WhileSubscribed(5_000)` — MVI needs state to survive
  without subscribers so reducer results are not lost, so it is a plain always-hot `MutableStateFlow`.
  The category `Flow` is therefore collected for the ViewModel's whole life rather than stopping
  five seconds after the screen goes away. One Room query; acceptable now, worth revisiting if a
  screen ever observes something expensive.

## 🔭 What I'd Do Next / At 10× Scale

- _TODO_

## 🚀 Getting Started

```bash
./gradlew installDebug   # build & install on a connected device/emulator
./gradlew test           # unit tests
```

<!-- TODO: gerekli API key / local.properties ayarları varsa buraya -->
