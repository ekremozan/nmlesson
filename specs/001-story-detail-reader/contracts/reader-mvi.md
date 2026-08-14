# Contract: Reader MVI

The screen's own contract, per Constitution Principle III. `ReaderContract.kt` holds the state,
intents and effects; `ReaderReducer.kt` holds the pure reducer; nothing else writes state.

## Intents

Every user action and every arrival of outside data enters through `onIntent`.

| Intent | Raised by | Effect on state |
|---|---|---|
| `DetailChanged(detail: ReaderDetail)` | the domain flow, folded back in | replaces `content`, sets `isPremiumContent`, opens the sheet the first time a restricted detail arrives |
| `RetryRequested` | the unavailable state's retry control | `content = Loading`, `retryToken + 1` |
| `ScrollProgressChanged(percent: Int)` | `snapshotFlow` over the list state | `progressPercent` |
| `UnlockSheetDismissed` | sheet dismissal | `isUnlockSheetVisible = false` |
| `UnlockSheetRequested` | the persistent unlock control (FR-009) | `isUnlockSheetVisible = true` |
| `SubscribeClicked` | the sheet's primary action | none — emits `ShowSubscriptionUnavailable` |
| `ListenClicked` | the footer pill | none — emits `ShowAudioUnavailable` |

## Effects

`Channel(BUFFERED)` + `receiveAsFlow()`, collected in the Hilt wrapper composable with
`flowWithLifecycle`. Never a `MutableSharedFlow`.

- `ShowSubscriptionUnavailable`
- `ShowAudioUnavailable`

## Reducer

```text
fun ReaderUiState.reduce(intent: ReaderIntent): Reduction
data class Reduction(val state: ReaderUiState, val effects: List<ReaderEffect> = emptyList())
```

Pure, top-level, no Android, no coroutines, no ViewModel. It is the only writer of state, and the
only place that decides which intent produces which effect (research R3).

## ViewModel

```text
class ReaderViewModel(savedStateHandle, observeStoryDetail, refreshStoryContent, analytics)

  storyId  = savedStateHandle.toRoute<ReaderRoute>().storyId

  init:
    state.map { LoadKey(storyId, it.retryToken) }
         .distinctUntilChanged()
         .flatMapLatest { observeStoryDetail(storyId) }
         .onEach { onIntent(DetailChanged(it)) }
         .launchIn(viewModelScope)

  fun onIntent(intent) =
    reduce, assign state, forward effects   // no branching, no second mutation path
```

**Guarantees**:

- No `when (intent)` anywhere in the ViewModel.
- No public mutable state flow; exactly one `onIntent`.
- No `PagingData` in this state.
- Analytics (`content_viewed`, `paywall_shown`) is logged from the flow that folds detail in, keyed
  so a recomposition or a re-emission cannot double-count a single open.

## UI boundary

`ReaderScreen` (Hilt wrapper, exempt from previews) collects state and effects and delegates to the
stateless `ReaderScreenContent(state, onIntent, onBack)`. Components below it —
`ReaderTopBar`, `ReaderBody`, `ListenPill`, `PremiumUnlockSheet`, `ReaderUnavailableState` — take
plain callbacks; only `ReaderScreenContent` turns those into intents.

### Test obligations for this contract

Reducer, as pure unit tests:

- `DetailChanged` with a restricted detail opens the sheet; a second `DetailChanged` while the
  reader has already dismissed it does not reopen it.
- `RetryRequested` increments `retryToken` and returns to `Loading`.
- `SubscribeClicked` leaves state untouched and returns exactly `ShowSubscriptionUnavailable`.
- `ListenClicked` leaves state untouched and returns exactly `ShowAudioUnavailable`.
- `ScrollProgressChanged` is clamped to 0–100.
- An entitlement change arriving as `DetailChanged(Full)` clears the restricted state and hides the
  sheet without any other intent.

ViewModel, with fakes:

- A detail emission reaches state through `onIntent` and nowhere else.
- Retry re-subscribes the detail flow exactly once per retry.
- Effects survive being raised while the screen is stopped (the `Channel` guarantee).
