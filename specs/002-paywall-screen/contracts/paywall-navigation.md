# Contract: Paywall Navigation

The app has no external API; the "contract" is the nav-graph boundary between modules.

## Routes (`:feature:paywall`)

```kotlin
@Serializable
data class PaywallRoute(val storyId: Long, val progressPercent: Int)

@Serializable
data class PurchaseSuccessRoute(val storyId: Long, val progressPercent: Int, val plan: PurchasePlan)

fun NavGraphBuilder.paywallScreen(
    onClose: () -> Unit,
    onPurchased: (storyId: Long, progressPercent: Int, plan: PurchasePlan) -> Unit,
)

fun NavGraphBuilder.purchaseSuccessScreen(
    onContinueReading: (storyId: Long) -> Unit,
    onExploreLibrary: () -> Unit,
)
```

## `:feature:reader` change

```kotlin
fun NavGraphBuilder.readerScreen(
    onBack: () -> Unit,
    onUnlockRequested: (storyId: Long, progressPercent: Int) -> Unit,
)
```

## `:core:domain` change

```kotlin
interface EntitlementRepository {
    fun isPremium(): Flow<Boolean>
    fun setPremium(value: Boolean)
}
```

## Wiring in `:app` (`NativeMindsNavHost`)

- `readerScreen(onBack, onUnlockRequested = { storyId, progress -> navController.navigate(PaywallRoute(storyId, progress)) })`
- `paywallScreen(onClose = navController::navigateUp, onPurchased = { storyId, progress, plan -> navController.navigate(PurchaseSuccessRoute(storyId, progress, plan)) { popUpTo<ReaderRoute> { inclusive = false } } })`
- `purchaseSuccessScreen(onContinueReading = { storyId -> navController.navigate(ReaderRoute(storyId)) { popUpTo<HomeRoute> } }, onExploreLibrary = { navController.navigate(HomeRoute) { popUpTo<HomeRoute> { inclusive = true } } })`
