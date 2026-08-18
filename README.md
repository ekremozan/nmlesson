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
| R8 minification — **enabled in a pre-prod security pass** | `release { optimization { enable = true } }`, previously `false` | A release build shipping fully unobfuscated, unshrunk bytecode is easy to decompile and inspect (class/method names, control flow) — turning R8 on is a standard, low-cost hardening step before any real distribution | Every release build now needs `keepRules/rules.keep` kept in sync with anything R8 can't infer on its own (e.g. `kotlinx.serialization` DTOs reflected into by name); verify with a release build + smoke test after any new serialized model is added |
| Window insets | Applied once at the app root in `MainActivity`, not per screen | Every screen otherwise repeats `statusBarsPadding()`/`navigationBarsPadding()`, and the one that forgets ships content under the status bar | A screen wanting edge-to-edge content (a full-bleed reader or cover image) has to opt out of the root padding rather than opt in |
| Home screen scrolling | Greeting, search field, category chips and the section heading sit outside the `LazyColumn`; only story cards scroll | Search and filtering stay reachable at any scroll position — on a browse screen, scrolling down is exactly when you decide to filter | The header costs vertical space permanently. A collapsing toolbar would recover it, at the cost of a scroll-linked animation that has to stay smooth while paging |
| Preview strategy | One `@Composable` per matrix: themes from a multipreview annotation (`@ThemePreviews` / `@ScreenThemePreviews`), states from a `PreviewParameterProvider` | Hand-writing light × dark × state as N copies of `@Preview` is boilerplate that drifts — a new state gets added to two of the four copies. Fixtures live in one file per feature so a UI-model change breaks one file, not six | Preview parameters make the tool's preview list longer and slower to render. Worth it: adding a state (loading, offline, premium-locked) is one line in the provider |
| Code comments | No `//` comments in the codebase; rationale lives here in this table instead, KDoc only on public declarations | Rationale in a comment is invisible to anyone reading the README and rots silently next to the code it explains. Naming and structure carry the *what*; the *why* belongs in a document that gets read during review | Someone reading a build file in isolation has to come here to learn why a dependency is `api`. Accepted: this table is the single place decisions are defended, which is also what the case study asks for |
| Design tokens | Ported the design system into a typed Compose token layer (`ui/theme/`) rather than styling per screen | One place to change a color or a text style; a palette regression is caught in `ThemePreview.kt` instead of on a device | More indirection for a small app. At 10× (multiple squads, more surfaces) this becomes a shared `:design-system` module with screenshot tests per component |
| Material 3 vs. custom | Material 3 scheme for standard roles + a small `NativeMindsColors` for what M3 has no slot for | Keeps M3 components (ripples, text fields, sheets) correct for free while still allowing brand roles the spec doesn't model | Two places to look up a color. The rule "standard → MaterialTheme, brand → NativeMindsTheme" is documented in CLAUDE.md to keep it unambiguous |
| Dynamic color | Disabled | The paper-and-terracotta ground and serif reading voice *are* the product's atmosphere; repainting them from wallpaper trades identity for a personalization win a reading app doesn't benefit from | Users who expect Material You theming don't get it |
| Error color | Mapped M3 `error` onto the deep terracotta ramp instead of adding a red | The design system has no red — warnings use terracotta, success uses sage. A stock red would read as a system dialog dropped into the app | Less "alarming" than a red; acceptable because the app has no destructive actions so far |
| Fonts | Bundled static instances of Google Sans Flex / Newsreader / Figtree (OFL) instead of downloadable fonts | The type is part of the brand — it has to be right on first launch and offline, which a provider round-trip can't guarantee. Static instances (not variable) because variable axes need API 26 and `minSdk` is 24 | ~400 KB of APK. At scale: ship only the weights in use (already done), and revisit variable fonts once `minSdk` ≥ 26 |
| Display font swap: Caprasimo → Google Sans Flex | Caprasimo has no `ş`/`Ş`/`ğ`/`Ğ`/`İ` glyphs in any released version (checked via `fontTools`, upstream and Google Fonts alike), so Turkish headings silently fell back to a mismatched system font for those letters. Replaced with Google Sans Flex's 36pt-optical-size Bold static instance, which has full Turkish coverage | A font that looks fine in an English-only design review can still fail the app's primary language; verify glyph coverage for every locale the app ships before bundling a display face | Google Sans Flex reads noticeably cleaner/more geometric than Caprasimo's slab-serif warmth — a deliberate brand trade-off, not a bug |
| Accent split | Two accents: `primary` (#C67139) for fills, `accentText` (#B2622D) for glyphs | The fill accent only reaches ~2.7:1 on the paper ground — it fails WCAG AA as text | One more token to reason about, in exchange for accessible accent text everywhere |
| **Reader feature** — full reasoning in [`specs/001-story-detail-reader/research.md`](specs/001-story-detail-reader/research.md), one line each below | | | |
| Navigation | `navigation-compose`, type-safe `@Serializable` routes, each feature owns its route | App had none yet; typed route gives free `SavedStateHandle` restoration | Two new deps for one screen — bet on the screens still to come |
| Story tap → reader | Plain callback, not a `HomeIntent` | Changes no state, so an intent would force a forbidden ViewModel branch | Reads as an exception to "every action is an intent" — see research.md R1 |
| Reader reducer returns effects | `Reduction(state, effects)`, unlike `HomeReducer`'s bare state | Only the reducer may decide intent → effect without the ViewModel branching | Two reducer shapes until Home needs its own effect — see research.md R2 |
| Story content storage | Separate `story_content` table, not columns on `stories`; DB 1→2 with a written migration | Keeps kilobyte bodies out of the paged list query | See research.md R3 |
| Premium preview rule | `ReaderAccess.Preview` has no field for the withheld text | A leak-proof type beats a flag a UI bug could ignore | See research.md R4 |
| Reader unlock card copy | The in-reader teaser (`UnlockCard`) that replaces the withheld text says "View plans" / "Planları gör", not the "Start 7 days free" trial copy the design mockup shows | The real Paywall has no free-trial mechanic behind it (mocked purchase only) — promising one in the teaser that leads straight to a "Subscribe now" screen would be a UI lie the user would immediately hit | See Cut Corners |
| Entitlement & observability | `EntitlementRepository`/`ErrorReporter` interfaces in `:core:domain`; entitlement backed by `SharedPreferences` (same shape as `LastSyncedLanguageStore`) so the mock purchase and its cancellation survive process death, `ErrorReporter` still logcat; analytics deferred entirely | Interface has to be right now, real backend is a later `@Binds` swap; `SharedPreferences` matches the app's existing convention for small persisted flags, no new dependency needed | See Cut Corners |
| Robolectric + `NetworkMonitor` interface | Test-only dependency; `NetworkMonitor` turned into an interface | `SavedStateHandle`/Room need a real `Bundle`/DB in tests; offline behavior needs a settable input | One more test dependency and binding |
| **Listen feature** — full reasoning in [`specs/003-listen-tts-playback/research.md`](specs/003-listen-tts-playback/research.md), one line each below | | | |
| Audio approach (TTS vs pre-generated) | Android's on-device `TextToSpeech`, not pre-generated/streamed audio files | Works fully offline with zero server cost and zero extra storage per story; a pre-generated-audio pipeline is a real production feature (TTS rendering job, CDN, cache) that isn't the point of this case study | Voice quality is the platform default, not a studio narration. At 10× scale, pre-rendering popular stories through a cloud TTS/voice-actor pipeline and caching the audio file would read as the natural upgrade, with `StoryNarrator` as the seam that swap goes behind |
| New `:core:audio` module | TTS + a foreground `MediaSessionService` is Android-platform code that doesn't belong in `:core:domain` (pure Kotlin) or `:core:data` (repositories/remote sources); `StoryNarrator` interface in domain, `TextToSpeechNarrator` impl here | Same shape as `:core:data`'s `DataModule` for repositories — `:feature:reader` only ever sees the interface | One more module and one more `@Binds` wiring than putting it directly in `:core:data` |
| Background playback | `androidx.media3:media3-session`'s `SimpleBasePlayer` wrapping `TextToSpeechNarrator`, hosted in a `MediaSessionService` | Gives the system media notification, its play/pause action, and (via `AudioManager` focus handling in `TextToSpeechNarrator`) pause-on-interruption for free instead of hand-rolled `NotificationCompat` | Position reported to the session is the sentence index, not real audio time — there is no seek bar, only play/pause |
| Whole story queued at once, progress read from `onStart` | Story text is split into sentences (`toStorySentences`) and **all** of them are handed to the engine up front (`QUEUE_FLUSH` then `QUEUE_ADD`), keyed by token-stamped utterance ids. Position comes from `onStart`/`onRangeStart`, not `onDone` | The first design fed one sentence and only queued the next when `onDone` arrived. Engines that never deliver `onDone` therefore fell silent after the opening sentence **with the position frozen at zero** — so pause/resume replayed the first line and looked exactly like restarting. Letting the engine own its queue makes playback independent of that callback, and `onStart` fires as speech actually happens | `onDone` is now only consulted to notice the story ended; a resume re-queues the remaining sentences rather than relying on what the engine still holds |
| Resume granularity | `onRangeStart` tracks the character offset reached inside the current sentence; a resume speaks `sentence.substring(offset)` and then the rest | Resuming at the start of a long sentence still sounds like starting over to a reader who paused near its end. The offset gives a genuinely "where I left off" resume | `onRangeStart` is API 26+; on 24–25 the offset stays 0, so narration resumes at the last sentence boundary — the documented fallback, not a silent one |
| Narration position in a pure `NarrationQueue` | The "where are we, what plays next" state machine is a plain-Kotlin class; `TextToSpeechNarrator` only carries its decisions out to the engine, audio focus and the service | Pause/resume correctness is the whole point of the feature and it was previously only provable on a device with a working speech engine. As a pure class it is 10 JVM tests, including "the completion the pause interrupted must not advance the queue" — the exact regression that made narration restart from the beginning | One more indirection between the interface and the engine |
| The session never owns the narration | `NarrationPlayer` flips a `released` flag in `handleRelease()` and ignores every later command; `TextToSpeechNarrator` only tears the service down once the queue is genuinely `Idle`, never on a pause | These two are the same bug from both ends. Stopping the engine to pause makes the platform deliver the interrupted sentence's completion; the queue rightly ignores it, but the narrator was treating "nothing to speak" as "narration finished" and stopping the service — whose teardown sent a `stop()` straight back into the narrator, resetting it to `Idle` so the next tap restarted the story. A control surface must not be able to end the thing it controls | Narration can now outlive a released session, so the service is started again on the next `start()`/`resume()` rather than assumed alive |
| Narrator is main-thread confined | Every `StoryNarrator` method and every `UtteranceProgressListener` callback is funnelled onto the main looper by a `Handler` | The engine delivers completions on a **binder thread**, so a finished sentence raced a tap over the same mutable queue — and a non-volatile `sentences` read from that thread had no guaranteed visibility of what the main thread wrote, which silently collapsed narration to `Idle` and made the next tap start over instead of resume. Single-threading is also what media3 requires of `NarrationPlayer` | Every call costs a possible `Handler.post`; irrelevant next to speech synthesis latency |
| Narration effects executed by the ViewModel, not the screen | `ReaderReducer` still returns `ReaderEffect` (now including `StartNarration`/`PauseNarration`/`ResumeNarration`), but `ReaderViewModel.onIntent` calls `StoryNarrator` directly for those three and only forwards `ReaderUiEffect.ShowAudioUnavailable` to the screen's effect channel | The *decision* of which narration action `ListenClicked` means still has to live in the reducer (it depends on current `NarrationState`) — only the one-shot UI effect belongs on the channel the screen collects. Same shape `PaywallViewModel` already uses for `PurchaseClicked` → `EntitlementRepository.setPremium` | Two effect sealed interfaces (`ReaderEffect`, `ReaderUiEffect`) instead of one, to keep the screen's `when` exhaustive over only what it should ever see |
| Sentence segmentation lives in `:core:domain` | `StorySentence(paragraphIndex, start, end)` + `toStorySentences()`/`sentenceTexts()` in `:core:domain`, replacing `:core:audio`'s `splitIntoSentences` | The narrator decides *what* is spoken and the reader draws *where* it is being spoken — both need the same segmentation, and `:feature:reader` cannot depend on `:core:audio`. Keeping character offsets instead of a flat list of trimmed strings is what gives the highlight something to anchor to; the old `flatMap`+`trim` threw away the only link back to the rendered paragraph | The domain layer now carries a piece of text processing that is arguably presentation. Accepted: it is pure Kotlin with no Android in it, and the alternative is two implementations of the same split drifting apart |
| Word highlight while narrating | `NarrationState.Playing/Paused` carry a nullable `SpokenRange`, published from `onRangeStart`; `ReaderUiState.narrationHighlight` resolves it to characters of a paragraph and `ReaderBody` paints it in `drawBehind` from the measured `TextLayoutResult` — screen 2a's amber wash, 3dp past the word on every side, gliding to the next word over 180ms | The reader had no way to see where narration was in the text. The highlight is drawn from the layout rather than as a `SpanStyle(background)`: the drop cap inflates its line far past the reading line height, and the platform places span backgrounds against that inflated line — in the opening paragraph they landed a third of a line low and painted over the very words they marked. Anchoring each run to its own baseline is exact on every line, and `drawBehind` is behind the glyphs by construction. The design animates the glide with a CSS `background` transition, which is a cross-fade; a mark that actually travels needs the geometry animated, and it only travels within a line — carrying it across a wrap would sweep it backwards over the whole column while the voice is already on the next word, so a wrap snaps; `null` means nothing is marked, and where the platform cannot report words at all (below API 26) the narrator publishes the sentence range itself. Letting the *reader* treat a missing range as "mark the sentence" also covered the engine's start-up latency, which flashed the sentence over the page on every start, resume and sentence boundary — a device-capability fallback belongs where the capability is known, not in the UI. It stays a computed property because both inputs are already in state — a stored field would only be a copy for the reducer to keep in step | The narrator now publishes several times a second, so `NarrationPlayer` narrows its collect to (story, playing, sentence) to stop the media notification rebuilding per word. Auto-scroll follows the *paragraph*, and stands down whenever the paragraph is already visible or the reader is dragging |
| Listening progress bar is word-based | The reader pill's bar and its "N min left" come from `ReaderUiState.listenPillProgress`: `narrationProgress` — words spoken / words in the story, as a `Float` the track animates over 300ms — whenever narration is playing or paused, and the scroll position otherwise. `progressPercent` stays scroll-derived and still feeds the paywall | Scrolling and listening are independent ways through a story; a bar that swapped between them jumped every time the reader read ahead of the voice. Words rather than sentences because sentence lengths vary wildly — one twelfth per sentence lurches, a word is small enough to move with the voice. The running per-sentence word totals are computed once in the mapper (`sentenceWordTotals`), so a word tick costs one `substring` of the current sentence rather than a walk of the story | The bar changes what it measures when narration starts and stops, so it can step as playback begins — the alternative, a bar that stays at zero for anyone who only reads, was worse. A paused story still counts as listening: the reader is returning to where the voice stopped. A story long enough that one word is worth less than a percent would tick invisibly without the fraction and the tween |
| Screen-scoped narration | `ObserveNarrationUseCase` maps the app-wide `StoryNarrator.state` through `NarrationState.forStory(storyId)`, collapsing any other story's state to `Idle` | One narrator for the whole app (single active session) means opening story B's screen while story A narrates in the background must not show B's pill as playing A's audio | If two reader screens are ever open at once (not currently possible in this nav graph), only the one matching the narrator's current story shows live state |
| AI feature design | _TODO_ | _TODO_ | _TODO_ |
| Analytics & crash reporting | Crash/error reporting shipped in the error-handling feature (`ErrorReporter` seam); analytics shipped separately — see the Firebase Analytics rows below | Covered by the "Observability" row above | — |
| **Error handling & Crashlytics** — full reasoning in [`specs/005-error-handling-crashlytics/research.md`](specs/005-error-handling-crashlytics/research.md), one line each below | | | |
| Crash reporting backend | Firebase Crashlytics, replacing `LogcatErrorReporter` | Requested explicitly; the existing `ErrorReporter` interface in `:core:domain` was already the single collection point every handled failure routes through, so wiring a real backend was a `@Binds` swap, not a new abstraction | At 10× a second backend (Sentry, a custom pipeline) is the same swap again — the interface, not the vendor, is what the rest of the app depends on |
| New `:core:crashreporting` module | Firebase BoM + Crashlytics live in their own module (same shape as `:core:audio`), not in `:core:data` | Kept `:core:data`'s dependency surface unchanged and isolated the one module that would need to change if the backend ever does; matches the requested "single, modular collection point" | One more module and one more `@Binds` wiring than putting it directly in `:core:data` |
| Uncaught crashes | No custom `Thread.UncaughtExceptionHandler` — relies entirely on Crashlytics' own default handler, installed automatically when the SDK initializes | Writing a second handler on top of the SDK's own risks the two racing or overriding each other; the SDK's mechanism is the "single collection point" already, at the JVM level | None — this is the standard integration path |
| Handled (non-fatal) errors | The same `FirebaseCrashlyticsErrorReporter.report()` that backs `ErrorReporter` also calls `recordException` for every caught failure (`log(context)` first as a breadcrumb) | One panel for both real crashes and caught errors was the explicit ask, rather than caught errors staying Logcat-only and invisible in production | Crashlytics' "Non-fatals" tab gets noisier as call sites grow; acceptable, it is still one panel instead of two blind spots |
| User identity in crash reports | `FirebaseCrashlytics.setUserId(...)` is never called; the SDK's own anonymous installation id is the only identifier attached | The project has no real auth/account system (entitlement is a device-local `SharedPreferences` flag, not an account), so there is no real user id to attach, and adding one would be a privacy liability for no debugging benefit | Once real accounts exist, a considered decision (not a default) about what identifier — if any — is safe to attach |
| Offline crash queuing | No custom retry/queue table — Crashlytics' SDK persists a report on-device and uploads on the next launch/connectivity | Reinventing what the SDK already guarantees would be pure risk for no gain | — |
| QA-only forced crash | A `BuildConfig.DEBUG`-gated "Test çökmesi tetikle" row in `:feature:settings` (`SettingsScreenContent`'s `onTestCrashClick`, a plain callback like a nav-out action, not a `SettingsIntent`) throws `RuntimeException("Test crash")` | The only way to prove SC-002 ("crashes reach Crashlytics automatically") without a code change every time is a repeatable trigger; gating it behind `BuildConfig.DEBUG` keeps it out of release builds entirely | A debug-only row is still one more branch in the screen composable — accepted because it's the only device-level way to verify the whole pipeline |
| Closing two silent `:core:audio` gaps found by auditing every catch/callback site after the Crashlytics wiring landed | `TextToSpeechNarrator`'s real error handling moved to the non-deprecated `UtteranceProgressListener.onError(utteranceId, errorCode)` overload (available since API 21, below `minSdk` 24), which reports via `errorReporter` with the error code and still advances the queue (mirrors `onDone`, since the engine calls exactly one of `onDone`/`onError` per utterance); the platform's deprecated single-arg `onError(utteranceId)` stays a required-but-empty override since the class leaves it `abstract`. `requestAudioFocus()` now checks the `AudioManager` return value and reports when focus is denied | Both were real, silent gaps against CLAUDE.md's "no silently swallowed exceptions" rule, sitting a few lines from call sites that already reported — an accidental omission, not a considered exception. Using the two-arg overload also drops the `@Suppress("DEPRECATION")` from anywhere near real logic — it now sits only on a one-line stub the platform forces us to keep | `onError` firing means the queue advances past a sentence the engine never actually spoke, so the reader can drift slightly ahead of what was heard; accepted over leaving the queue stuck waiting for an utterance that will never call `onDone` |
| **Paywall feature** — full reasoning in [`specs/002-paywall-screen/research.md`](specs/002-paywall-screen/research.md), one line each below | | | |
| Paywall UI | Full-screen `:feature:paywall` module (screens 4 + 8 of the design), replacing the reader's in-place `ModalBottomSheet` | The gate needed to be reachable from any premium surface, not owned by `:feature:reader`; a shared destination has to be its own module per the "no feature depends on another feature" rule | The old bottom-sheet row above no longer applies — superseded here rather than deleted, so the "why we changed it" stays visible |
| Reader → Paywall trigger | Plain callback (`onUnlockRequested`) resolved in `:app`'s nav graph, not a `ReaderIntent` | Same rule as "Story tap → reader": leaving the screen is the graph's concern, not local state | The reader no longer owns any unlock-sheet state at all — `isRestricted` is the only thing left |
| Granting the mock entitlement | `EntitlementRepository.setPremium(Boolean)` promoted onto the domain interface itself, called directly from `PaywallViewModel.onIntent` keyed on `PurchaseClicked` | `:feature:paywall` can only depend on `:core:domain`, so the write path has to live on the interface it already has; no use case was worth inventing for a flag write with no business rule | The one place a ViewModel does a repository side effect keyed on a specific intent rather than only forwarding effects — a deliberate, narrow exception, not a pattern to spread |
| Paywall hero "washed" covers | `alpha`, not the design's `blur(1.5px)` | `Modifier.blur` is a silent no-op below API 31 and `minSdk` is 24 — a third of the supported range would have simply lost the effect with no signal. Alpha reads the same everywhere | Slightly flatter than the comp. Revisit once `minSdk` ≥ 31 |
| Paywall bottom block | Plan cards + CTA + footer links pinned outside the scroll region (`weight(1f)` on the scrolling part, same shape as `HomeScreen.kt:153`) | The design's `margin-top:auto` footer only works because the comp is a fixed 844px canvas; on real devices a single scroll leaves the CTA floating mid-screen on tall phones and unreachable on short ones | The top region can get tight on very small screens — it scrolls, which is the right trade for keeping the purchase action always visible |
| Carrying story context across screens | `storyId`/`progressPercent` (and `plan`) travel as `@Serializable` nav-route arguments from `PaywallRoute` to `PurchaseSuccessRoute` | Matches the existing `ReaderRoute(storyId)` convention; two primitives don't justify a shared flow/ViewModel scope | Success screen re-fetches the story from `StoryRepository` rather than trusting stale reader state |
| Subscription / gating model | Single entitlement source of truth + a domain-side preview rule; purchase is fully mocked (no billing SDK, no network call) and says so on the success screen | Covered by the "Premium preview rule", "Entitlement", and "Granting the mock entitlement" rows | At 10× scale the mock purchase becomes a real Play Billing / App Store integration behind the same `EntitlementRepository.setPremium` call site |
| **Catalog seed (100 stories)** | | | |
| Cover image field | `Story.image: String` holds an opaque key (`"cover_01"`…`"cover_10"`), resolved to a drawable only in `:core:designsystem`'s `StoryCoverAssets` | `:core:model` has no Android dependency; a `@DrawableRes Int` on the domain model would have forced one. The key is a plain column, so it survives Room round-trips and mapping through every layer with no special casing | Swapping to remote cover art later means changing what the key resolves to (a URL instead of a resource id) without touching the domain model or the database schema |
| Cover art itself | 10 procedural vector drawables (`story_cover_01.xml`…`_10.xml`), API 24+ gradient `<vector>`s built from the existing Organic palette tokens | No Coil/image-loading dependency, no network fetch, no extra APK weight from bitmaps — offline-first holds for images the same way it already does for text | Visually abstract, not illustrative; see Cut Corners |
| 100-story generation | 20 hand-written pieces (`StorySeedBases.kt`), each published under 5 titles; `DummyStorySeed`/`DummyStoryContentSeed` fan that out by a flat index (`baseIndex * 5 + variantIndex`) and rotate category (`% 4`) and cover (`% 10`) on that index | 100 divides evenly by both 4 and 10, so the rotation alone guarantees exactly 25 stories per category and exactly 10 per cover with no separate bookkeeping table. A unit test (`DummyStorySeedTest`) pins both invariants plus id uniqueness | The same paragraphs appear under 5 different titles — an intentional trade for catalog size over content volume; see Cut Corners |
| **Story → Lesson repurposing** — short fiction replaced with subject/topic lesson content (Biyoloji, Tarih, Coğrafya, Kimya × 10 topics each) | | | |
| Rename scope | Full `Story*` → `Lesson*` rename across every module (model, database, data, audio, all UI feature modules), and `category` → `subject` everywhere it names the lesson's subject field | The old fiction-app naming would have read as a lie next to lesson content — "story", "category" and 10 generic cover keys all meant something different once the content became educational. Doing it as one mechanical pass (script-driven identifier rename + manual fixups) kept it reviewable as a rename, not entangled with the content change | 102 files touched; the size is why it's its own commit/PR tier ahead of any real content authoring |
| Database migration `3→4` | `MIGRATION_3_4` renames `stories`→`lessons`, `story_content`→`lesson_content`, `category`→`subject`, then `DELETE FROM lessons`/`lesson_content` | The catalog's *shape* changed (100 rotated fiction rows → 40 authored subject/topic rows), not just column names — there is no sane row-by-row mapping from the old catalog to the new one. `RoomLessonRepository.syncIfNeeded()` already reseeds on `count() == 0`, so clearing is what makes the next launch clean rather than half-migrated | Anyone who had the pre-rename build installed loses their local seed data on upgrade — acceptable for a demo app with no production installs, logged here rather than left silent |
| Image model | Kept a single `image: String` field on `Lesson`/`LessonEntity` (no new column), but it's now derived from `subject` at seed time — one of 4 fixed keys (`subject_biology` etc.), not an independently authored per-row value | Cheapest path to "one image per subject": no migration beyond the rename, no mapper changes beyond the seed. `SubjectImageAssets` (was `LessonCoverAssets`) shrank from a 10-key to a 4-key lookup | The field can in principle drift from `subject` since nothing enforces the derivation past the seed — a fully normalized version would compute the drawable key from `subject` in `SubjectImageAssets` directly and drop the column; revisit at 10× if lesson-level (not subject-level) art is ever wanted |
| Premium gating pattern | Per-subject: first 3 of each subject's 10 topics ship `isLocked = false`, the rest `true` — same `isLocked` + `ReaderAccess`/`FreePreviewRule` mechanism as before, just a different seed distribution | Reuses the existing, already-tested gating path unchanged; only the seed data's `isLocked` assignment needed to change | Locked/free ratio (3-of-10) is a seed-time constant, not a rule — moving it (e.g. per-subject override) means editing each `*Lessons.kt` file by hand |
| **Settings screen** | | | |
| New `:feature:settings` module + `ThemeRepository` | Same shape as `EntitlementRepository`: interface in `:core:domain`, in-memory `MutableStateFlow` impl (`MockThemeRepository`) bound in `:core:data`'s `DataModule` | The dark-theme toggle needed one source of truth reachable from both the settings screen (writer) and `MainActivity` (reader), and this is the pattern the codebase already uses for exactly that shape of problem | See Cut Corners — the value is not persisted (unlike `EntitlementRepository`, which now is) |
| Theme applied at the app root | `AppThemeViewModel` (in `:app`) collects `ThemeRepository.isDarkTheme()` and passes it into `NativeMindsTheme(darkTheme = …)` in `MainActivity`, replacing the `isSystemInDarkTheme()` default | `NativeMindsTheme` already took a `darkTheme` parameter — the toggle only needed a caller that reads it from somewhere other than the system setting | `MainActivity` now depends on a Hilt ViewModel outside any nav graph; `hiltViewModel()` resolves against the Activity's own `ViewModelStoreOwner`, which works but is a slightly unusual call site to spot in review |
| Full locale-qualified string resources (`values-tr/`) | Every module's `values/strings.xml` is now the English default (Settings and Quiz, previously Turkish-only, were translated to match), with a Turkish translation of the same keys added under `values-tr/` in `:app`, `:core:audio`, `:feature:home`, `:feature:reader`, `:feature:paywall`, `:feature:settings`, `:feature:quiz` | Content language already followed `Locale.getDefault()` via `ContentLanguageProvider`; the UI strings just hadn't been given the matching resource pair, so a system language switch changed the content but left screens fixed in whichever language their `strings.xml` happened to be authored in | Still no in-app language override — see the content-language row below; adding one (e.g. `AppCompatDelegate.setApplicationLocales`) would reuse the same `values-tr/` resources, just choose between them independent of the device setting |
| Paywall entry point from Settings | `PaywallRoute(lessonId = -1L, progressPercent = 0)` — a sentinel lesson id, since Settings has no lesson context to hand off | `PurchaseSuccessContract.lesson: Lesson? = null` was already null-safe (`PurchaseSuccessScreen.kt`'s `if (lesson != null)`), so an unresolvable id degrades to "no continue-reading card" instead of crashing — verified on-device | A generic "not tied to a lesson" paywall entry (nullable `lessonId` on the route) would be the honest fix once a second non-lesson entry point exists |
| Support/legal rows are visual only | "Bizi puanlayın", "Bize ulaşın", "Gizlilik politikası", "Kullanım koşulları" render with the design's icon/chevron but have no `onClick` | Explicitly out of scope for this pass — there is no store listing, support inbox, or legal copy to link to yet | Wiring them is additive: each becomes one `onClick` with no reducer/contract change |

| **Remote lesson content (Supabase)** — full reasoning in [`specs/004-remote-lesson-content/research.md`](specs/004-remote-lesson-content/research.md), one line each below | | | |
| Remote client library | `supabase-kt`'s `postgrest-kt` module over Ktor/OkHttp, pinned to 3.2.6/3.3.1 rather than latest (3.7.0/3.5.2) | Latest `supabase-kt` builds against Kotlin 2.4, which this project's Kotlin 2.2.10 compiler cannot read (`kotlin-stdlib` metadata version mismatch on first build attempt); 3.2.6 is the newest release still built against Kotlin 2.2.x | At 10× scale, bumping the project's own Kotlin version becomes the forcing function to also move to a current `supabase-kt`, rather than the other way around |
| Sync strategy | `RoomLessonRepository.syncIfNeeded()` fetches the full remote `lessons` table and replaces the local catalog with `dao.upsertAll()` + `dao.deleteMissing(ids)` inside one `withTransaction`, on every online sync — not just the first run | A transaction either fully commits or fully rolls back, so "a failed sync leaves the previous catalog untouched" is true by construction rather than by an extra recovery path. `deleteMissing` (not delete-all-then-insert) avoids cascading away `lesson_content` rows for lessons that are still present, just unchanged | At ~40 rows, fetching everything every sync is free. At 10× scale (hundreds of lessons, frequent syncs) this becomes an incremental diff keyed on an `updated_at` watermark instead of a full fetch |
| Lesson content is bulk-synced at splash, not lazily per-open | `RoomLessonRepository.syncIfNeeded()` now fetches `lessons` and every lesson's `lesson_content` in parallel (`coroutineScope { async { … } }`) and writes both inside the same `withTransaction` as the catalog sync, via the new `RemoteLessonDataSource.fetchAllContent(language)`. `fetchContent(id)` (the old per-lesson fetch) stays as a fallback for a lesson the bulk sync missed | On-device testing (`pm clear` → browse online → airplane mode) showed only 3 of 40 lessons were readable offline — exactly the ones that happened to have been opened while online, contradicting the "reading works fully offline" claim for every other lesson. At ~40 lessons × ~700 bytes (~28KB total), fetching every body on every sync is trivial bandwidth, so there was no real cost to closing this gap | If a content fetch fails mid-sync, the whole sync throws and the transaction never opens — the previous catalog and content are left untouched, same "fully commits or fully rolls back" guarantee as the lesson-metadata sync. A lesson added remotely after the last sync, or one missed by an interrupted sync, still falls back to the existing per-lesson `fetchContent`/`OfflineException` path the first time it's opened |
| Secrets | `SUPABASE_URL`/`SUPABASE_ANON_KEY` read from git-ignored `local.properties` into `core:data`'s `BuildConfig`, mirrored by an `android.defaults.buildfeatures` opt-in in that one module | The anon key is meant to ship inside client apps (protected by Postgres RLS, not secrecy), but there's no reason to hardcode it in a source file that's trivially greppable in the repo when the existing `local.properties`/`sdk.dir` pattern was already there to reuse | Anyone building the app from a fresh clone must add these two lines themselves — documented in `specs/004-remote-lesson-content/quickstart.md`, not discoverable from Gradle alone |
| Read-only access model | No end-user auth; the app reads with Supabase's public anon key, and RLS policies grant `select` only — no `insert`/`update`/`delete` from the client at all | The app has no login system and content is public read-only by design; a service-role key or write access from the client would be pure attack surface for zero benefit | Content is authored directly in Supabase's own SQL editor/table view; if a real editorial workflow is ever needed, that becomes an admin surface behind Supabase Auth, not a change to the read path |
| `minSdk` 24 vs. `supabase-kt`'s Android 26 floor | Enabled `isCoreLibraryDesugaringEnabled` + `coreLibraryDesugaring(libs.android.desugar.jdk.libs)` in `core:data` and `app`, rather than raising `minSdk` | `supabase-kt`'s own README states a minimum of API 26 without desugaring; raising `minSdk` was not on the table (a project-wide constraint), so desugaring is the standard, narrowly-scoped fix | One more Gradle dependency; no behavior change for API 24–25 devices since desugaring only backfills library classes, not new OS capability |

| **Firebase Analytics** — full reasoning in [`specs/006-firebase-analytics/research.md`](specs/006-firebase-analytics/research.md), one line each below | | | |
| New `:core:analytics` module | Mirrors `:core:crashreporting`'s shape exactly: a `sealed interface AnalyticsEvent` (11 cases) + single-method `AnalyticsReporter` interface in `:core:domain`, `FirebaseAnalyticsReporter` implementation here, bound in a Hilt `@Binds` module | A different Firebase SDK, lifecycle and domain sequel than crash reporting deserves its own modest module rather than overloading `:core:crashreporting`'s name; the closed event hierarchy makes a new event a compile error everywhere it isn't handled instead of a silently unmapped string | At 10× a second backend (Amplitude, a custom pipeline) is the same `@Binds` swap again — the interface, not the vendor, is what the rest of the app depends on |
| Screen-view tracking | Centralized in `:app`'s `NativeMindsNavHost` via `NavController.addOnDestinationChangedListener`, routed through a small `NavigationAnalyticsViewModel` (the nav host composable isn't itself Hilt-injectable) | The "a screen composable never receives a `NavController`" and "leaving a screen is a plain callback, not an intent" rules already in this codebase rule out asking every `:feature:*` module to report its own screen views; one listener in the one module that already sees every destination is the natural single point | Forward/back direction is inferred by comparing the new destination against a locally tracked shadow stack rather than a first-class Navigation Compose API — correct for this nav graph's shape, would need revisiting for a graph with duplicate destinations on the back stack |
| Paywall funnel via `onCleared()`, not a new intent | `PaywallViewModel` logs `PaywallShown` in `init` (using a `triggerSource` field added to `PaywallRoute`), `PaywallPurchaseClicked`+`SubscriptionStarted` in `onIntent(PurchaseClicked)`, and `PaywallDismissed` in `onCleared()` only when a `purchased` flag is still false | The purchased path already pops this screen before navigating away (see `onPurchased` in the nav host), so `onCleared()` fires in both the "bought" and "backed out" cases — a private flag set at purchase time is what tells them apart without adding a forbidden "screen closed" intent for a pure navigation event | One more piece of ViewModel-local mutable state outside the reducer's `_state`, justified the same way `PaywallViewModel`'s mock-purchase call already was: a decision the reducer cannot make because it depends on how the screen is left, not on any intent |
| `PurchaseDeclined` defined but unreachable | The event exists in `AnalyticsEvent` and `FirebaseAnalyticsReporter`'s mapping, but nothing calls it — the mock purchase flow (`PaywallIntent.PurchaseClicked` → `EntitlementRepository.setPremium(true)`) always succeeds | Sits in the sealed hierarchy ready for a real billing SDK's failure callback; forcing an artificial failure path just to exercise the event would have produced misleading analytics data for no real signal | See Cut Corners |
| Lesson-level parameters | `onLessonClick` widened from `(Long) -> Unit` to `(lessonId: Long, title: String, index: Int) -> Unit` so `LessonSelected` carries the list position and title without a second lookup | Both values already exist at the tap site inside `HomeScreen`'s `LazyColumn`; passing them through the existing plain callback is cheaper and more accurate than reconstructing them later from just an id | Every current and future caller of `onLessonClick` carries two more parameters than a bare id — acceptable, it's a plain callback, not a public API surface |

| **AI quiz feature (Gemini)** — full reasoning in [`specs/007-ai-quiz-generation/research.md`](specs/007-ai-quiz-generation/research.md), one line each below | | | |
| New `:feature:quiz` module, own nav route | `QuizRoute(lessonId)` opened from Reader's "Test" button via a plain `onTestRequested` callback (the same shape as `onUnlockRequested`), rather than an in-place overlay boolean in `ReaderUiState` | Keeps `:feature:reader` and `:feature:quiz` independent — neither depends on the other, matching the "`:feature:*` modules never depend on each other" rule — and `:app`'s nav host stays the one place that knows every destination | A second screen's worth of Hilt/Compose/nav boilerplate instead of one boolean; acceptable since the feature is a genuinely separate concern (AI generation, its own loading/error states) |
| Gemini client library — **reversed after an on-device crash** | Started with the official `com.google.ai.client.generativeai` Android SDK; **replaced** with a hand-rolled Ktor `HttpClient` REST call once real-device testing crashed the app | The SDK is compiled against Ktor 2.3.2; this project's Supabase dependency pins Ktor 3.3.1, and Gradle resolves one version of `ktor-client-core` project-wide. The SDK's transitive 2.3.2 request gets silently force-upgraded to 3.3.1, and `HttpTimeout` changed from a class to a top-level property between those major versions — `ClassNotFoundException` at the exact moment "Test" is tapped, invisible to `compileDebugKotlin` and every unit test, only visible by actually running the app | The very "hand-written DTOs" cost the SDK was chosen to avoid was paid anyway, just later and after a real crash instead of during code review — the lesson (see "How I Worked With AI") is that a passing build is not evidence a third-party SDK is runtime-compatible with the rest of the dependency graph |
| Gemini model name — **corrected after a live API call** | `gemini-2.5-flash` in the original plan; **`gemini-flash-latest`** in the shipped code | The exact model name was never verified against a live call during planning. On device, `gemini-2.5-flash` returned `404 — "no longer available to new users"` for this freshly-created API key. `curl`ing the `ListModels` endpoint and trying alternatives found `gemini-flash-latest` (an alias that tracks the current stable flash model, currently resolving to `gemini-3.7-flash`) actually works | An alias avoids repeating this exact failure mode when the underlying model is deprecated again, at the cost of the exact model version being outside this repo's control |
| Structured output over free-text parsing | `responseMimeType = "application/json"` + an explicit `Schema` (question, 4 options, correct index, explanation), validated again in `GeminiQuizPayloadDto.toDomain()` (`check()` on option count, index range, blank strings) | A model can return technically-valid JSON that still violates the app's invariants (3 options, an out-of-range index) — the schema narrows *how* it can fail, the mapper's own checks catch what the schema can't express | One more layer of validation code; worth it because a malformed quiz question reaching the UI would be a silent content bug, not a crash |
| No persistence for generated questions | Nothing is written to Room; each "Test" tap makes a fresh Gemini call, held only in `QuizViewModel`'s in-memory state | The feature was scoped, mid-planning, from an initial "5-question cached quiz" draft down to "one live-generated question per tap" once the actual design mockup (10a/10b/10c, "Soru 1 / 1") and the user's own description ("anlık olarak gemini'den alacak") made the real shape clear; adding a `QuizEntity`/DAO would have built infrastructure for a caching behavior nobody asked for | The quiz is unusable offline and costs one Gemini call every time it's opened — acceptable for a demo-scale case study; see Cut Corners |
| Gating: hide the button, then check again | The "Test" button only renders when `ReaderAccess` is `Full` (mirrors the design's own free-preview screen, which has no such button at all); `GenerateQuizUseCase` independently checks `EntitlementRepository.isPremium()` before touching `QuizRepository`, returning `QuizGenerationResult.Locked` with **zero** Gemini calls if not | UI-only gating is not a security boundary — trusting it alone would mean a stray deep link could reach a paid AI call for free. The use-case check is the one that actually matters; the hidden button is the honest UI reflection of it | Two gating checks to keep in sync instead of one, but they fail closed independently rather than compounding a single point of failure |
| API key management | `GEMINI_API_KEY` follows the exact `local.properties` → `BuildConfig` pattern already used for `SUPABASE_ANON_KEY`, rather than a new secrets tool | The project already has one working, git-ignored pattern for exactly this problem; introducing `secrets-gradle-plugin` for a second key would be unjustified complexity for the same outcome | The key still ships inside the compiled APK's `BuildConfig` — fine for a case study, not for a real release; see Cut Corners |
| Gemini key transport — **hardened in a pre-prod security pass** | Sent as the `x-goog-api-key` header instead of a `?key=...` query parameter | A query-string key rides along in any proxy or access log that records full URLs, even over TLS; the header carries the same credential without ending up in URL-shaped logs | None — the Gemini REST API accepts both forms, so this was a strict improvement over the original implementation |
| `Reduction(state, effects)` kept feature-local, not extracted | `:feature:quiz` copies `:feature:reader`'s `Reduction` shape locally rather than pulling it into a shared module, even though CLAUDE.md flags "extract at the second consumer" | Extracting a shared MVI module mid-feature was out of scope for what was planned in `tasks.md`; duplicating ~10 lines was the smaller, more reviewable change today | Named explicitly as follow-up work below — a third consumer should not get the same choice again |

| **Content language (TR/EN)** | | | |
| Language keyed by device locale, one language cached at a time | `ContentLanguageProvider` reads `Locale.getDefault()`; Supabase rows carry an added `language` column, each language's lessons independently keyed (no `lesson_group_id`); Room's schema is untouched — a language switch replaces the cached catalog rather than merging two languages into it | Matches the requirement ("client only passes a parameter") with no Room migration and no new UI; `dao.deleteMissing()` already purges the old language's rows via the existing sync transaction, and `LessonContentEntity`'s cascade FK cleans up their content for free | No Settings override and no shared reading-progress between a lesson's two language versions — both logged in Cut Corners |
| Startup gated on a system splash screen, not the Home empty state | `androidx.core.splashscreen` + `MainActivityViewModel.isReady`, held via `setKeepOnScreenCondition` until `EnsureContentLanguageUseCase` clears a stale-language cache **and** an initial `SyncLessonsUseCase` fetch either lands or hits a 4s `withTimeoutOrNull`; `HomeViewModel.pagedLessons` also gates on its own `contentVerified` state as a second, independent guard | The first version only waited on the language check, so a cold Room cache (first launch, or a sync still in flight) still showed `EmptyResultsState`'s "Connect to load lessons" for a moment after the splash lifted. Waiting for the fetch too means Home's first frame usually already has lessons to show; the timeout exists so a stalled or absent connection can never make the splash look frozen — that sync just continues in the background via `HomeViewModel`'s own | On a slow connection the splash can sit for up to 4s doing nothing visibly different from a hang; and every launch now makes two sync calls in quick succession (splash's, then Home's own on creation) — harmless at this catalog size, worth a shared "already synced this launch" guard if it ever isn't |

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

AI quiz feature (spec-driven pass, `specs/007-ai-quiz-generation/`):

- **Corrected mid-spec: a 5-question, cached quiz was the wrong shape.** `/speckit-specify` was run
  from a one-line description ("çoktan seçmeli soru cevaplama sayfası") with no design reference,
  and produced a reasonable-sounding but wrong default: 5 questions per quiz, persisted and reused
  across visits. When `/speckit-plan` was run with the actual Claude Design mockup (screens 2 and
  10 — a single "Soru 1 / 1" question, answered live) and the user's own description ("soruyu anlık
  olarak gemini'den alacak"), the mismatch was caught before any code was written: the spec was
  rewritten in place — one question per "Test" tap, generated fresh every time, no persistence —
  rather than building the originally-drafted five-question feature and discovering the gap later.
- **Rejected: trusting the hidden "Test" button as the gate.** The first pass toward gating would
  have stopped at "don't render the button for free users." Applied the same non-negotiable pattern
  already used for `ReaderAccess`/`EntitlementRepository` elsewhere in the app: `GenerateQuizUseCase`
  checks entitlement itself and returns `Locked` with zero Gemini calls, so a stray deep link can't
  reach a paid AI call just because the UI-only gate was bypassed.
- **A passing build turned out not to be enough — caught only by running on a real device.** The
  Gemini Android SDK's API surface (`GenerativeModel`, `Schema`, `generationConfig { ... }`) was
  written from memory, then checked with `./gradlew :core:data:compileDebugKotlin` rather than
  assumed correct — that part worked. What it didn't catch: the SDK is compiled against Ktor 2.3.2,
  this project's Supabase dependency forces Ktor 3.3.1 project-wide, and `HttpTimeout` changed from
  a class to a top-level property between those versions — a `ClassNotFoundException` that neither
  the compiler, lint, nor any unit test can see, because none of them load that class or instantiate
  a real `GenerativeModel`. Only tapping "Test" on an actual emulator surfaced it. Rewrote the data
  source around the plain Ktor `HttpClient` already used for Supabase instead — the same alternative
  rejected during planning as "unnecessary complexity," now the only option proven to actually work.
  Immediately after, the *first real* Gemini call returned `404` for the planned `gemini-2.5-flash`
  model ("no longer available to new users") — caught by `curl`ing the live API directly rather than
  trusting the model name written during planning, and fixed by switching to the `gemini-flash-latest`
  alias. Both fixes are recorded in `specs/007-ai-quiz-generation/research.md` R1/R2. Lesson: for a
  live third-party API, "it compiles and the tests pass" and "it works" are proven by two different
  actions — the first by a build, the second only by an actual network call from an actual device.

## ✂️ Cut Corners & Assumptions

<!-- Bilerek kısılan köşeler — anında buraya ekle, sona bırakma -->
- _TODO: örn. mock billing (gerçek Play Billing yerine), seed content, ..._
- **No release signing config.** `app/build.gradle.kts` has no `signingConfigs` block, so there is
  no keystore wired up for a real release build. R8 minification was enabled in the pre-prod
  security pass, but shipping a signed release artifact still needs a keystore + passwords supplied
  from outside this repo (never commit them) and a `signingConfigs.release` block added once they
  exist.
- **Entitlement persists locally, not against a real backend.** `SharedPreferencesEntitlementRepository`
  writes the flag to on-device `SharedPreferences`, so premium survives the app being closed and
  reopened. There is no server, no receipt, and no way to revoke access from outside this device —
  uninstalling the app or clearing its storage resets it to free. Replacing it is one `@Binds`.
- **The purchase is fully mocked.** The Paywall's "Subscribe now" button calls
  `EntitlementRepository.setPremium(true)` directly — no billing SDK, no payment credentials, no
  network call. It is a genuine state change (every gated surface sees it immediately), just not a
  real one; replacing it is one call site once real billing lands.
- **Canceling premium is instant and local.** Settings' "Cancel premium" button, after a confirm
  dialog, calls `EntitlementRepository.setPremium(false)` directly — no billing-provider
  cancellation request, no grace period, no refund logic; the demo goes straight from Premium to
  free.
- **Restore purchases, Terms, and Privacy are inert.** They match the design for fidelity, but
  Restore only shows a "no purchase found" message and Terms/Privacy do not open a document — there
  is no real store or legal copy to point at yet.
- **The Gemini API key ships inside the compiled APK.** `GEMINI_API_KEY` is read from git-ignored
  `local.properties` into `BuildConfig`, same as the Supabase keys — never committed, but still
  extractable from a release build by anyone who decompiles it. A production release needs a
  server-side proxy in front of Gemini so the key never reaches the device at all.
- **Generated quiz questions are never persisted.** Every "Test" tap makes a fresh Gemini call; there
  is no `QuizEntity`/DAO, no offline quiz, and no history of past attempts or scores. This was a
  deliberate scope decision (see Key Decisions), not an oversight — the design and the stated
  requirement were both for a single, live-generated question, not a cached quiz set.
- **No retry/backoff or rate limiting on Gemini calls.** Repeatedly tapping "Test" makes repeatedly
  new API calls with no debounce or cooldown; acceptable at demo scale, would need a minimum interval
  or an in-flight guard before any real traffic.
- **`QuizViewModel`'s Compose-driven flow is unit-tested at the reducer/use-case level, not with a
  full `QuizViewModelTest`** the way `ReaderViewModelTest` covers `ReaderViewModel` — the reducer
  (`QuizReducerTest`) and the use case (`GenerateQuizUseCaseTest`) between them cover every branch
  the ViewModel forwards, but the ViewModel's own `flatMapLatest`/effect-forwarding wiring has no
  dedicated coroutine test yet.
- **Listen now narrates real content** (superseded the "does not play anything" note above) —
  see the notes below for what's still cut in that feature.
- **Narration position is never persisted.** It lives only in the app-scoped `StoryNarrator`
  singleton for the current process; leaving the reader screen (or killing the app) discards it by
  design (spec requirement, not a shortcut) — there is no cross-session bookmark.
- **The media notification shows a generic title, not the story's.** `NarrationSessionService`
  doesn't know which story is playing beyond its id — passing the real title through would mean
  widening `StoryNarrator.start()` past what the domain contract needs. Acceptable for a
  single-story-at-a-time listen feature; would need the title wired through once multiple
  concurrent narrations are ever in scope.
- **Only one narration session exists app-wide.** Starting narration for a different story silently
  stops whatever was narrating before — there is no "switch and keep the old one queued" behavior,
  matching the spec's explicit single-active-session assumption.
- **Background/notification playback still needs a device pass.** The narration state machine
  (`NarrationQueue`) is unit-tested and `TextToSpeechNarrator` now actually starts
  `NarrationSessionService`, but this environment has no attached emulator/device to run
  `quickstart.md`'s scenarios 4 and 5 (background continuation, media notification, audio-focus
  interruption) against. The notification's appearance and media3's foreground promotion need a
  manual pass on a real device before shipping.
- **No in-app mini-player.** Leaving the reader screen while narration continues in the background
  is only visible through the system notification — there is no persistent in-app indicator
  elsewhere in the app, matching the feature spec's stated scope.
- **The reader's overflow menu is omitted, not drawn inert.** Font size and theme controls belong to
  a later feature; a spacer keeps the title optically centred until they exist.
- **Content language follows the device locale, with no in-app override.** `ContentLanguageProvider`
  reads `Locale.getDefault()` — English devices get English lessons, everything else gets Turkish;
  there is no Settings toggle to pick a language independent of the system one. Each language's rows
  live as ordinary, independently-keyed Supabase rows (no `lesson_group_id` linking a lesson to its
  translation), so switching languages replaces the cached catalog rather than merging into it —
  acceptable since only one language is ever read at a time, but it means there is no shared
  reading-progress concept across a lesson's two language versions.
- **A language switch clears the cache before it can refetch, even while offline.** `HomeViewModel`
  awaits `EnsureContentLanguageUseCase` before it ever reads from Room, specifically so a device
  language change can never flash the previous language's lessons for a frame. The cost: if the
  device language changes for the first time while offline, the user sees the "never synced" empty
  state rather than the (wrong-language) cached lessons, until connectivity returns — judged better
  than showing content in the wrong language, but worth knowing before filing it as a bug.
- **The 100-story catalog is 20 pieces of writing, not 100.** Each of the 20 base stories in
  `StorySeedBases.kt` is published under 5 different titles (and a rotating category/cover), so the
  same paragraphs appear multiple times in the library under different names. This was a deliberate
  choice to get catalog size, category spread, and paging/search behavior to demo realistically
  without hand-writing 100 distinct stories.
- **Cover art is procedural, not illustrated.** The 10 covers are code-generated gradient vector
  drawables (`core/designsystem/src/main/res/drawable/story_cover_0X.xml`), not AI-generated or
  hand-drawn illustrations — chosen to stay offline-first with zero image-loading dependency
  (no Coil) and near-zero APK weight. They read as abstract brand texture, not per-story art.
- **Analytics is now wired end-to-end** (superseded the "deferred entirely" note above) — see the
  Firebase Analytics rows in Key Decisions and [`specs/006-firebase-analytics/`](specs/006-firebase-analytics/)
  for the full design. Two corners specific to that feature, cut deliberately:
  - **`AnalyticsEvent.PurchaseDeclined` is defined but has no call site.** The mock purchase flow
    (`PaywallViewModel.onIntent(PurchaseClicked)` → `EntitlementRepository.setPremium(true)`)
    always succeeds, so there is no real failure to report yet. The event stays in the sealed
    hierarchy and `FirebaseAnalyticsReporter`'s mapping so a real billing SDK's decline/error
    callback is a one-line addition, not a new abstraction, once billing is real.
  - **`AnalyticsEvent.AiFeatureUsed` has no call site either.** The AI feature itself hasn't been
    built yet (see the "AI feature design" `_TODO_` row above) — only the event contract exists,
    proven by a single round-trip unit test
    (`core/domain/src/test/kotlin/.../observability/AnalyticsEventTest.kt`). Wiring it in is a
    one-line `analyticsReporter.log(...)` call once the AI feature lands.
  - **`quickstart.md`'s scenarios 1–3 were run against a real emulator with the real Firebase
    project** (Logcat's `FA-SVC` tag, verbose mode) — every event listed in
    [`contracts/analytics-events.md`](specs/006-firebase-analytics/contracts/analytics-events.md)
    was observed with correct parameters, in the correct order, including both paywall exits
    (purchased vs. dismissed-without-buying). That pass caught a real double-counting bug —
    `ReaderViewModel.onCleared()` logged `ListenStopped` twice (`PAUSED` then `SCREEN_LEFT`) for
    one pause-then-leave sequence — fixed by only reporting `SCREEN_LEFT` when narration was still
    `Playing`. Scenario 4 (offline queuing) and the DebugView console view (as opposed to raw
    Logcat) were not exercised — the Logcat pass already proves the events fire correctly; DebugView
    is a nice-to-have visual confirmation of the same data.
- **Crashlytics is wired to a real Firebase project (`nativeminds-68169`), not a mock.**
  `FirebaseCrashlyticsErrorReporter` (`:core:crashreporting`) and the Gradle wiring
  (`google-services`/`firebase-crashlytics` plugins) build clean end-to-end. `app/google-services.json`
  is git-ignored on purpose — it was supplied by the project owner from their own Firebase console,
  not fabricated or committed, so a fresh clone still needs that one file dropped into `app/` before
  `:app` builds (the same account-level step every Firebase-backed Android project requires). See
  [`specs/005-error-handling-crashlytics/quickstart.md`](specs/005-error-handling-crashlytics/quickstart.md)
  for the manual device/console verification steps (forcing a test crash, confirming it reaches the
  Crashlytics dashboard) that still need a real device and console access to complete.
- **Lesson content is no longer hand-seeded.** `DummyLessonSeed`/`DummyLessonContentSeed` and
  `FakeRemoteLessonDataSource` are deleted; `RoomLessonRepository` now syncs the `lessons` table
  from a real Supabase project via `SupabaseRemoteLessonDataSource`. The dummy catalog's content
  was preserved as `supabase/seed.sql`, a one-time script that seeds the remote project with the
  same 40 lessons the app used to ship hardcoded — see
  [`specs/004-remote-lesson-content/quickstart.md`](specs/004-remote-lesson-content/quickstart.md).
  **The actual Supabase project still needs to be created and seeded by hand** — that step needs a
  Supabase account and cannot be done by an automated change; until then `syncIfNeeded()` will fail
  against an unconfigured `SUPABASE_URL`/`SUPABASE_ANON_KEY`.
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
- Sync failures reach the user as a snackbar via `HomeEffect.ShowSyncError` **and** are reported
  through `ErrorReporter` — `SyncLessonsUseCase` now wraps `repository.syncIfNeeded()` in
  `runCatching { }.onFailure { errorReporter.report(...) }`, the same shape
  `RefreshLessonContentUseCase` already used. The offline-first contract is unchanged: a sync that
  cannot run (offline) is a silent no-op, and a sync that fails partway through never touches the
  previously-synced catalog.
- `StoryCard`'s `onClick` is still an empty lambda rather than a `HomeIntent.StoryClicked`. There is
  no navigation and no Reader screen for it to reach, and an intent whose reducer branch returns
  `this` and whose effect nobody consumes is dead code that reads like a feature.
- The visible snackbar has no `@Preview`. A `Snackbar` only appears in response to a `showSnackbar`
  call on a `SnackbarHostState`, which a static preview never makes; the preview would render an
  empty host and prove nothing. This is the "written reason" the preview rule asks for.
- `HomeUiState` is no longer shared with `WhileSubscribed(5_000)` — MVI needs state to survive
  without subscribers so reducer results are not lost, so it is a plain always-hot `MutableStateFlow`.
  The subject `Flow` is therefore collected for the ViewModel's whole life rather than stopping
  five seconds after the screen goes away. One Room query; acceptable now, worth revisiting if a
  screen ever observes something expensive.
- **Lesson bodies are still short placeholder text, not the ~500-line konu anlatımı target.** The
  Story→Lesson rename (Faz A) ships all 40 topics (Biyoloji/Tarih/Coğrafya/Kimya × 10) with real
  but short Turkish content (2-3 paragraphs each) so the whole pipeline — seeding, paging, search,
  reader, premium gating, TTS — can be verified end-to-end without waiting on ~20,000 lines of
  authored prose. Writing the real long-form content is deliberately deferred to separate follow-up
  passes (one per subject), reviewed topic-by-topic rather than as one large diff.
- **The 10 topic titles per subject are a first pass, not a vetted curriculum.** They were chosen
  to read as plausible lise-level topics for each subject but haven't been checked against an
  actual curriculum; expect them to be adjusted once real content is written.
- **The dark-theme preference is an in-memory mock, like entitlement.** `MockThemeRepository`
  holds a `MutableStateFlow(false)` and resets to light on process death — the project has no
  DataStore dependency yet and this is the only setting that would need one so far. Swapping in a
  persisted implementation is one `@Binds` change, same as `EntitlementRepository`.
- **The bottom tab bar from the Settings mockup (Home/Library/Ask AI/Settings) is not built.**
  Library and Ask AI don't exist as screens yet, so Settings is reached the same way every other
  screen is — a push from Home's profile icon — rather than through a persistent tab bar the rest
  of the app doesn't have.
- **The profile icon opening Settings needed a back affordance the mockup doesn't show** (it
  assumed the tab bar). A chevron-left button matching the Reader's back button was added so the
  screen is actually navigable without the tab bar.
- **Every seeded lesson claims `hasAudio = true`.** Narration is meant to work universally for
  lesson content, so nothing in the seed currently exercises the "no audio" reader/list state that
  the old fiction catalog demonstrated — worth adding back as an explicit test case if that state
  still needs coverage.
- **Content authoring has no in-app or admin UI.** Adding, editing, or removing a lesson happens
  directly in Supabase's own SQL editor/table view. Acceptable for a one-person case study; a real
  editorial workflow would need its own authoring surface, which is explicitly out of scope here.
- **Pull-to-refresh's spinner is tied to Paging's own `loadState.refresh`, not the sync's actual
  network duration.** Room's `PagingSource` auto-invalidates when `syncIfNeeded()` writes to the
  `lessons` table, so the spinner reflects "the list is reloading from the now-updated local data,"
  not "a network request is in flight." It disappears correctly either way; it just doesn't cover
  the earlier network-wait portion of a slow sync.
- **The never-synced empty state and the no-search-results empty state share one composable**
  (`EmptyResultsState(isFiltering = …)`) rather than being two separate screens. Chosen to reuse the
  existing icon/title/body layout exactly as the design system already draws it, at the cost of one
  conditional branch inside the composable instead of a second file.

## 🔭 What I'd Do Next / At 10× Scale

- _TODO_

## 🚀 Getting Started

```bash
./gradlew installDebug   # build & install on a connected device/emulator
./gradlew test           # unit tests
```

<!-- TODO: gerekli API key / local.properties ayarları varsa buraya -->
