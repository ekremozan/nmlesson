# Veri Modeli: Firebase Analytics Entegrasyonu

Bu özellik kalıcı bir veritabanı şeması eklemez (bkz. research.md #6). Tek "veri modeli", olayların
kendisidir: `:core:domain`'de tanımlanan kapalı `AnalyticsEvent` hiyerarşisi.

## `AnalyticsEvent` (sealed interface, `:core:domain/observability/AnalyticsEvent.kt`)

| Olay (alt tip) | Karşılık geldiği FR | Parametreler | Not |
|---|---|---|---|
| `ScreenViewed` | FR-001, FR-002 | `screenName: String`, `previousScreenName: String?`, `source: NavigationSource` (`FORWARD`/`BACK`) | Her ekran geçişinde `:app`'ten tetiklenir |
| `LessonSelected` | FR-003 | `lessonId: Long`, `lessonTitle: String`, `listIndex: Int` | Ana ekrandaki ders listesinden tıklama anında |
| `ContentViewed` | FR-004 | `lessonId: Long`, `accessLevel: AccessLevel` (`FULL`/`PREVIEW`) | Reader içeriği `Ready` durumuna geçtiğinde |
| `ListenStarted` | FR-005 | `lessonId: Long` | `ReaderEffect.StartNarration`/`ResumeNarration` yürütüldüğünde |
| `ListenStopped` | FR-006 | `lessonId: Long`, `reason: ListenStopReason` (`PAUSED`/`COMPLETED`/`SCREEN_LEFT`), `progressPercent: Int` | `PauseNarration` yürütüldüğünde veya reader ekranından ayrılırken |
| `PaywallShown` | FR-007 | `lessonId: Long`, `triggerSource: String` (ör. `"reader_unlock"`, `"settings_premium"`) | `PaywallRoute`'a girişte |
| `PaywallPurchaseClicked` | FR-008 | `lessonId: Long`, `plan: String` | `PaywallIntent.PurchaseClicked` işlenirken |
| `SubscriptionStarted` | FR-009 | `lessonId: Long`, `plan: String` | Sahte satın alma başarıyla tamamlandığında (bugün: her zaman) |
| `PurchaseDeclined` | FR-010 | `lessonId: Long`, `reason: String` | Sözleşmede tanımlı; bugünkü sahte akışta tetiklenmiyor (bkz. research.md #5) |
| `PaywallDismissed` | FR-011 | `lessonId: Long` | `paywallScreen(onClose = ...)` tetiklendiğinde, satın alma tıklanmadan |
| `RestorePurchasesClicked` | (genişletme — canlı cihaz testinde bulunan boşluk) | `lessonId: Long` | `PaywallIntent.RestorePurchasesClicked` işlenirken |
| `LessonsFiltered` | (genişletme — canlı cihaz testinde bulunan boşluk) | `query: String?`, `subject: String?` | Ana ekranda arama metni veya konu filtresi 600ms durağanlaştıktan sonra; en az biri dolu olmalı |
| `AiFeatureUsed` | FR-012 | `lessonId: Long?`, `featureName: String` | AI özelliği eklendiğinde kullanılacak (bu özellik kapsamında AI özelliğinin kendisi yok — CLAUDE.md'de "AI feature: karar bekleniyor" notu var; bu olay tipi ileriye dönük tanımlanır) |

`ListenStopped`'ın `reason=COMPLETED` dalı da bu genişletmede gerçek bir çağrı noktası kazandı:
`ReaderViewModel.onIntent`, `NarrationStateChanged` intent'i `Playing → Idle` geçişini taşıdığında
(kullanıcı duraklatmadan doğal bitiş) `%100` ilerlemeyle bu olayı kaydeder. Ayrıca aynı durmanın iki
kez raporlanmasını önlemek için `onCleared()` artık yalnızca narrasyon hâlâ `Playing` iken
`SCREEN_LEFT` loglar — `Paused` durumda zaten bir `PAUSED` olayı kaydedilmiştir.

Tüm alt tipler `data class`; hiçbiri PII taşımaz (yalnızca sayısal/dahili kimlikler ve önceden
tanımlı enum/string sabitleri, bkz. FR-017).

## `AnalyticsReporter` (arayüz, `:core:domain/observability/AnalyticsReporter.kt`)

```kotlin
interface AnalyticsReporter {
    fun log(event: AnalyticsEvent)
}
```

`ErrorReporter` ile aynı sözleşme şekli: tek metot, "implementasyonlar asla fırlatmaz" kuralı
(bkz. research.md #7).

## Durum geçişleri

Bu özellik state tutmaz (`AnalyticsEvent`ler tek seferlik/fire-and-forget'tir); bu yüzden ayrı bir
durum makinesi yoktur. Tek sıralı akış paywall hunisidir ve doğası gereği doğrusaldır:

```text
ScreenViewed(paywall) → PaywallShown
                            │
                ┌───────────┼────────────┐
                ▼                        ▼
   PaywallPurchaseClicked        PaywallDismissed (satın alma tıklanmadan)
                │
      ┌─────────┴─────────┐
      ▼                   ▼
SubscriptionStarted   PurchaseDeclined  (bugün ulaşılamaz, bkz. research.md #5)
```

## Gezinme bağlamı (`NavigationSource`)

`sealed`/`enum class NavigationSource { FORWARD, BACK }` — `NavController.currentBackStackEntryFlow`
ile mevcut ve önceki `NavBackStackEntry`'nin `destination.route`'undan türetilir; `:app` dışına
sızmaz (yalnızca `ScreenViewed.source` alanını doldurmak için kullanılır).
