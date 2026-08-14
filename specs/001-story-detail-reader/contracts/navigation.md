# Contract: Navigation

## Graph

`:app` owns `NativeMindsNavHost`, the only place that knows both destinations exist. Each feature
module declares its own route and a `NavGraphBuilder` extension; neither feature module depends on
the other.

```text
NativeMindsNavHost(navController)
  startDestination = HomeRoute
  homeScreen(onStoryClick = { id -> navController.navigate(ReaderRoute(id)) })
  readerScreen(onBack = navController::navigateUp)
```

`MainActivity` hosts the graph instead of calling `HomeScreen` directly, keeping the edge-to-edge
and theme wrapping it already applies.

## Routes

```text
@Serializable data object HomeRoute
@Serializable data class ReaderRoute(val storyId: Long)
```

`storyId` is the only argument. The reader reads it from `SavedStateHandle` inside its ViewModel,
which is what makes FR-002 (surviving process recreation) fall out of the platform rather than
being hand-rolled.

## Screen entry points

```text
fun NavGraphBuilder.homeScreen(onStoryClick: (Long) -> Unit)
fun NavGraphBuilder.readerScreen(onBack: () -> Unit)
```

**Guarantees**:

- A feature's composable never receives a `NavController`; it receives plain lambdas, so it stays
  previewable and testable.
- `onStoryClick` is a callback rather than a `HomeIntent` — the reasoning is in
  [research.md](../research.md) R2.
- Back from the reader returns to Home with its query, category and scroll position intact, because
  Home's entry stays on the back stack and its `HomeViewModel` is retained (FR-001).

### Test obligations for this contract

- Tapping a seeded story card on Home shows that story's title in the reader header.
- Pressing back returns to Home with a previously typed query still in the search field.
- Launching the reader route directly with a `storyId` that has no local row shows the
  "story unavailable" state rather than crashing or hanging.
