# Sözleşme: Analiz Olayları (`AnalyticsReporter` / `AnalyticsEvent`)

Bu, `:core:domain`'in geri kalan modüllere sunduğu tek "dış arayüz"dür (bu proje bir kütüphane/API
sunmadığı için, sözleşme dış sistemlere değil iç modüller arası sınıra aittir — Firebase Analytics
tarafına giden Bundle formatı bu dosyanın ikinci bölümünde belgelenir).

## 1. Domain sözleşmesi (her `:feature:*` ve `:app`'in gördüğü yüzey)

```kotlin
package com.example.nativeminds.domain.observability

interface AnalyticsReporter {
    fun log(event: AnalyticsEvent)
}

sealed interface AnalyticsEvent {
    data class ScreenViewed(
        val screenName: String,
        val previousScreenName: String?,
        val source: NavigationSource,
    ) : AnalyticsEvent

    data class LessonSelected(
        val lessonId: Long,
        val lessonTitle: String,
        val listIndex: Int,
    ) : AnalyticsEvent

    data class ContentViewed(
        val lessonId: Long,
        val accessLevel: AccessLevel,
    ) : AnalyticsEvent

    data class ListenStarted(val lessonId: Long) : AnalyticsEvent

    data class ListenStopped(
        val lessonId: Long,
        val reason: ListenStopReason,
        val progressPercent: Int,
    ) : AnalyticsEvent

    data class PaywallShown(
        val lessonId: Long,
        val triggerSource: String,
    ) : AnalyticsEvent

    data class PaywallPurchaseClicked(
        val lessonId: Long,
        val plan: String,
    ) : AnalyticsEvent

    data class SubscriptionStarted(
        val lessonId: Long,
        val plan: String,
    ) : AnalyticsEvent

    data class PurchaseDeclined(
        val lessonId: Long,
        val reason: String,
    ) : AnalyticsEvent

    data class PaywallDismissed(val lessonId: Long) : AnalyticsEvent

    data class RestorePurchasesClicked(val lessonId: Long) : AnalyticsEvent

    data class LessonsFiltered(
        val query: String?,
        val subject: String?,
    ) : AnalyticsEvent

    data class AiFeatureUsed(
        val lessonId: Long?,
        val featureName: String,
    ) : AnalyticsEvent
}

enum class NavigationSource { FORWARD, BACK }
enum class AccessLevel { FULL, PREVIEW }
enum class ListenStopReason { PAUSED, COMPLETED, SCREEN_LEFT }
```

**Kurallar**:

- Bir çağıran `AnalyticsReporter.log(...)` çağrısını asla `try/catch` ile sarmak zorunda değildir —
  implementasyon (`FirebaseAnalyticsReporter`) hiçbir zaman fırlatmaz (bkz. research.md #7).
- Yeni bir olay eklemek, `AnalyticsEvent`'e yeni bir `data class` eklemek ve
  `FirebaseAnalyticsReporter`'daki `when` bloğuna bir dal eklemekten ibarettir; `AnalyticsReporter`
  arayüzü değişmez.

## 2. Firebase eşlemesi (`:core:analytics` içinde, dış sisteme giden format)

| `AnalyticsEvent` alt tipi | Firebase olay adı | Firebase parametreleri |
|---|---|---|
| `ScreenViewed` | `screen_view` (Firebase'in standart olayı) | `screen_name`, `previous_screen_name` (null ise atlanır), `source` |
| `LessonSelected` | `lesson_selected` | `lesson_id`, `lesson_title`, `list_index` |
| `ContentViewed` | `content_viewed` | `lesson_id`, `access_level` |
| `ListenStarted` | `listen_started` | `lesson_id` |
| `ListenStopped` | `listen_stopped` | `lesson_id`, `reason`, `progress_percent` |
| `PaywallShown` | `paywall_shown` | `lesson_id`, `trigger_source` |
| `PaywallPurchaseClicked` | `paywall_purchase_clicked` | `lesson_id`, `plan` |
| `SubscriptionStarted` | `subscription_started` | `lesson_id`, `plan` |
| `PurchaseDeclined` | `purchase_declined` | `lesson_id`, `reason` |
| `PaywallDismissed` | `paywall_dismissed` | `lesson_id` |
| `RestorePurchasesClicked` | `restore_purchases_clicked` | `lesson_id` |
| `LessonsFiltered` | `lessons_filtered` | `query` (varsa), `subject` (varsa) |
| `AiFeatureUsed` | `ai_feature_used` | `lesson_id` (varsa), `feature_name` |

Olay/parametre adları Firebase'in kısıtlarına uyar (küçük harf, alt çizgi, ≤40 karakter olay adı,
≤24 karakter parametre adı, ≤100 karakter string değer).

## 3. Geriye dönük uyumluluk

Bu sözleşme yeni bir özelliktir; geriye dönük uyumluluk kaygısı yoktur. İleride yeni bir olay
eklendiğinde bu dosya güncellenmeli ve `FirebaseAnalyticsReporter`'daki `when` bloğu `else` dalı
İÇERMEMELİDİR — derleyicinin kapalılığı (exhaustiveness) zorlaması, yeni bir `AnalyticsEvent` alt
tipinin eşlemesiz kalmasını derleme zamanında engeller.
