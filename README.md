# NativeMinds — Bite-Sized Story App (Mobile Case Study)

> A short-story reading & listening app: browse and search stories, read or listen, free users get a taste, subscribers unlock everything — including an AI-powered feature.

<!-- TODO: 1-2 cümlelik final ürün tanımı + ekran görüntüleri / GIF -->

## 📱 Live Demo

- **APK / Store link:** _TODO (deploy edilince eklenecek)_
- **Demo video (2–5 min):** _TODO_
- **Architecture diagram (Miro/Excalidraw):** _TODO link_

## ✨ Features

<!-- Tamamlandıkça işaretle -->
- [ ] Story list — browse & search
- [ ] Story reader (read experience)
- [ ] Listen mode (audio / TTS)
- [ ] Free vs Premium gating (paywall)
- [ ] AI feature: _TODO — hangi AI özelliği, neden_
- [ ] Offline support (cached stories readable without network)
- [ ] Analytics (key funnel events)
- [ ] Crash / error reporting

## 🏗 Architecture

<!-- Diagram buraya embed edilecek + 1 paragraf özet -->

**Stack:** Kotlin, Jetpack Compose (Material 3), Room, Paging 3, Hilt _TODO: Media3, Firebase_

**Shape:** Clean Architecture + MVVM with unidirectional data flow, offline-first (local DB as
source of truth), dependencies resolved by Hilt at compile time.

**Modules** — the arrows only ever point inwards, towards the domain:

```
:app             composition root — the only module that knows which implementations exist
  ├── :feature:home     Compose screens + ViewModels     → :core:domain, :core:designsystem
  ├── :core:data        RoomStoryRepository, NetworkMonitor, @Binds  → :core:domain, :core:database
  └── :core:database    Room entities, DAO, DatabaseModule

:core:domain     StoryRepository contract + use cases    (pure Kotlin, no Android)
:core:model      Story                                   (pure Kotlin)
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
| Architecture pattern | _TODO_ | _TODO_ | _TODO_ |
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
| Data / offline strategy | _TODO_ | _TODO_ | _TODO_ |
| Audio approach (TTS vs pre-generated) | _TODO_ | _TODO_ | _TODO_ |
| Subscription / gating model | _TODO_ | _TODO_ | _TODO_ |
| AI feature design | _TODO_ | _TODO_ | _TODO_ |
| Analytics & crash reporting | _TODO_ | _TODO_ | _TODO_ |

## 🤖 How I Worked With AI

<!-- Case Part 2: AI'ı nasıl yönlendirdim -->

### Setup
<!-- TODO: Claude Code, CLAUDE.md kuralları, custom command/agent/MCP vs. kısaca -->

### How I framed & steered
<!-- TODO: problemi nasıl çerçeveledim, ne istedim, ilk cevap yanlışken nasıl iterate ettim — 2-3 somut örnek -->

### Where I overrode the AI
<!-- TODO: AI çıktısını reddedip kendi yolumla yaptığım yerler — somut örnekler -->

## ✂️ Cut Corners & Assumptions

<!-- Bilerek kısılan köşeler — anında buraya ekle, sona bırakma -->
- _TODO: örn. mock billing (gerçek Play Billing yerine), seed content, ..._
- `GetPagedStoriesUseCase` / `SyncStoriesUseCase` are currently pass-throughs to the repository.
  They exist as the seam premium gating will occupy, not because they add logic today.
- The whole graph lives in Hilt's `SingletonComponent`. Correct while every dependency is cheap and
  process-lived; the audio player and AI client will need narrower scopes.
- On a first launch the filter row shows only "All" for the instant between the screen appearing
  and the seed landing in Room — the chips follow the database, and it is briefly empty.
- `:core:domain` depends on `com.google.dagger:dagger` (pure JVM) so its `@Inject` factories are
  generated locally. `javax.inject` alone would have been purer, at the cost of moving factory
  generation into `:app` and a compiler warning per class.
- **Sync failures are swallowed on purpose** — the one place the "failures must be visible" rule is
  knowingly bent. A failed refresh leaves the database (seeded or previously synced) as the source
  of truth so the UI keeps working offline, which is the offline-first contract. It is a cut corner
  only until crash reporting is wired up: `RoomStoryRepository`'s catch is where the exception gets
  reported, and until then a sync that never succeeds is indistinguishable from one that has
  nothing new. No user-facing "couldn't refresh" signal exists yet either.

## 🔭 What I'd Do Next / At 10× Scale

- _TODO_

## 🚀 Getting Started

```bash
./gradlew installDebug   # build & install on a connected device/emulator
./gradlew test           # unit tests
```

<!-- TODO: gerekli API key / local.properties ayarları varsa buraya -->
