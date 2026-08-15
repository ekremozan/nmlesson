---

description: "Task list template for feature implementation"
---

# Görevler: Firebase Analytics Entegrasyonu

**Girdi**: `/specs/006-firebase-analytics/` altındaki tasarım dokümanları

**Ön koşullar**: plan.md, spec.md, research.md, data-model.md, contracts/analytics-events.md, quickstart.md

**Testler**: Bu özellik CLAUDE.md/anayasa gereği ("domain/repository mantığı taşıyan kod için birim
testi yazılmalı") test görevleri içerir — analiz kaydı doğrudan iş kuralı değil ama huninin doğru
sırayla tetiklendiği (paywall, dinleme) gözlemlenebilirlik davranışı olduğundan test edilir.

**Organizasyon**: Görevler spec.md'deki kullanıcı hikayelerine göre gruplanmıştır (US1–US4).

## Format: `[ID] [P?] [Story] Açıklama`

- **[P]**: Paralel çalıştırılabilir (farklı dosyalar, tamamlanmamış göreve bağımlılık yok)
- **[Story]**: Bu görevin ait olduğu kullanıcı hikayesi (US1, US2, US3, US4)

## Yol Kuralları

Bu, çok modüllü bir Android (Kotlin + Jetpack Compose) projesidir. Yollar plan.md'deki
"Kaynak Kod" bölümünde belirtilen gerçek modül yapısına göre verilmiştir.

---

## Faz 1: Kurulum (Paylaşılan Altyapı)

**Amaç**: `:core:analytics` modülünün iskeletini ve bağımlılık grafiğini hazırlamak.

- [X] T001 `gradle/libs.versions.toml`'a `firebase-analytics` kütüphane takma adını ekle
      (`firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }`,
      `firebase-crashlytics` satırının hemen altına)
- [X] T002 [P] `core/analytics/build.gradle.kts`'i `core/crashreporting/build.gradle.kts`'in
      birebir kopyası olarak oluştur (namespace `com.example.nativeminds.analytics`,
      `libs.firebase.bom` + `libs.firebase.analytics` bağımlılığı, `api(project(":core:domain"))`,
      Hilt eklentisi)
- [X] T003 `settings.gradle.kts`'e `include(":core:analytics")` ekle ve
      `app/build.gradle.kts`'e `implementation(project(":core:analytics"))` ekle
      (`:core:crashreporting` satırının yanına)

**Kontrol noktası**: `./gradlew :core:analytics:assemble` boş modülle derlenir.

---

## Faz 2: Temel (Engelleyici Ön Koşullar)

**Amaç**: Tüm kullanıcı hikayelerinin üzerine kurulacağı `AnalyticsEvent`/`AnalyticsReporter`
sözleşmesi ve Firebase implementasyonu.

**⚠️ KRİTİK**: Bu faz tamamlanmadan hiçbir kullanıcı hikayesi görevi başlayamaz.

- [X] T004 [P] `core/domain/src/main/kotlin/com/example/nativeminds/domain/observability/AnalyticsEvent.kt`
      dosyasını oluştur: `sealed interface AnalyticsEvent` (11 alt tip:
      `ScreenViewed`, `LessonSelected`, `ContentViewed`, `ListenStarted`, `ListenStopped`,
      `PaywallShown`, `PaywallPurchaseClicked`, `SubscriptionStarted`, `PurchaseDeclined`,
      `PaywallDismissed`, `AiFeatureUsed`) ve `enum class NavigationSource { FORWARD, BACK }`,
      `enum class AccessLevel { FULL, PREVIEW }`, `enum class ListenStopReason { PAUSED, COMPLETED, SCREEN_LEFT }`
      — tam alan listesi için [contracts/analytics-events.md](contracts/analytics-events.md) §1
- [X] T005 [P] `core/domain/src/main/kotlin/com/example/nativeminds/domain/observability/AnalyticsReporter.kt`
      dosyasını oluştur: `interface AnalyticsReporter { fun log(event: AnalyticsEvent) }`,
      `ErrorReporter.kt`'deki KDoc üslubuyla ("implementasyonlar asla fırlatmaz")
- [X] T006 [P] `core/domain/src/test/kotlin/com/example/nativeminds/domain/RecordingAnalyticsReporter.kt`
      dosyasını `RecordingErrorReporter.kt`'nin birebir eşi olarak oluştur:
      `class RecordingAnalyticsReporter : AnalyticsReporter { val logged = mutableListOf<AnalyticsEvent>(); override fun log(event: AnalyticsEvent) { logged += event } }`
- [X] T007 `core/analytics/src/main/java/com/example/nativeminds/analytics/FirebaseAnalyticsReporter.kt`
      dosyasını oluştur: `AnalyticsReporter`'ı implemente eden `@Singleton` sınıf,
      `FirebaseAnalytics` enjekte edilir, `log(event)` içinde kapalı (exhaustive, `else` dalsız)
      bir `when (event)` ile her alt tipi [contracts/analytics-events.md](contracts/analytics-events.md)
      §2'deki Firebase olay adı/parametrelerine eşler; tüm gövde `runCatching { ... }.onFailure { errorReporter.report(it, "analytics:${event::class.simpleName}") }`
      ile sarılır (`ErrorReporter` de constructor'a enjekte edilir) — FR-014/FR-015
- [X] T008 `core/analytics/src/main/java/com/example/nativeminds/analytics/di/AnalyticsModule.kt`
      dosyasını `CrashReportingModule.kt`'in eşi olarak oluştur: `@Binds` ile
      `FirebaseAnalyticsReporter -> AnalyticsReporter`, `@Provides` ile
      `FirebaseAnalytics.getInstance()`

**Kontrol noktası**: `:core:domain` ve `:core:analytics` derlenir; `RecordingAnalyticsReporter`
birim testlerinde kullanılabilir hale gelir.

---

## Faz 3: Kullanıcı Hikayesi 1 - Uygulama içi gezinmenin izlenmesi (Öncelik: P1) 🎯 MVP

**Hedef**: Her ekran geçişinde (ileri ve geri) doğru önceki ekran bilgisiyle `screen_view` olayı.

**Bağımsız Test**: Uygulamayı aç, ana ekran → okuma → paywall'a git, sonra geri dön; Logcat'te
(`quickstart.md` Senaryo 1) dört doğru sıralı `screen_view` olayı görünür.

- [X] T009 [P] [US1] `app/src/main/java/com/example/nativeminds/navigation/NavigationAnalyticsViewModel.kt`
      dosyasını oluştur: `@HiltViewModel class NavigationAnalyticsViewModel @Inject constructor(private val analyticsReporter: AnalyticsReporter) : ViewModel()`,
      tek bir `fun trackScreenView(screenName: String, previousScreenName: String?, source: NavigationSource)`
      metodu `analyticsReporter.log(AnalyticsEvent.ScreenViewed(...))` çağırır
      (NavHost composable'ının kendisi Hilt'e bağlı olmadığından DI erişimi bu küçük ViewModel
      üzerinden sağlanır — bkz. research.md §3)
- [X] T010 [US1] `app/src/main/java/com/example/nativeminds/navigation/NativeMindsNavHost.kt`'i
      güncelle: `hiltViewModel<NavigationAnalyticsViewModel>()` al, `navController`'a
      `DisposableEffect` içinde `addOnDestinationChangedListener` ekle; hedefin adını
      (`HomeRoute`→`"home"`, `ReaderRoute`→`"reader"`, `PaywallRoute`→`"paywall"`,
      `PurchaseSuccessRoute`→`"purchase_success"`, `SettingsRoute`→`"settings"`) ve önceki hedefi
      çözüp `trackScreenView(...)` çağır; yön (`FORWARD`/`BACK`), yeni hedefin geri yığınında
      zaten var olup olmadığına (`navController.previousBackStackEntry` ile karşılaştırarak)
      bakılarak belirlenir (FR-001, FR-002)
- [X] T011 [P] [US1] `app/src/test/java/com/example/nativeminds/navigation/NavigationAnalyticsViewModelTest.kt`
      dosyasını oluştur: `RecordingAnalyticsReporter` ile `trackScreenView(...)`'ün tam olarak bir
      `AnalyticsEvent.ScreenViewed` kaydettiğini ve alanların doğru eşlendiğini doğrula

**Kontrol noktası**: US1 bağımsız olarak çalışır ve test edilebilir — diğer hikayeler olmadan da
uygulama içindeki tüm gezinme Firebase'e raporlanır.

---

## Faz 4: Kullanıcı Hikayesi 2 - Ders seçimi ve dinleme kullanımının izlenmesi (Öncelik: P1)

**Hedef**: Ders seçimi, içerik görüntüleme, dinleme başlama/durma; hepsi ders kimliğiyle.

**Bağımsız Test**: Ana ekrandan bir ders seç, aç, dinlemeyi başlat, duraklat, ekrandan çık;
`quickstart.md` Senaryo 2'deki beş olayın hepsi doğru `lesson_id` ile görünür.

- [X] T012 [P] [US2] `feature/home/src/main/java/com/example/nativeminds/feature/home/ui/HomeScreen.kt`
      ve `feature/home/src/main/java/com/example/nativeminds/feature/home/navigation/HomeRoute.kt`'deki
      `onLessonClick` imzasını `(Long) -> Unit`'ten `(lessonId: Long, title: String, index: Int) -> Unit`'e
      genişlet; çağrı noktası `HomeScreen.kt:187`'deki `items(count = lessons.itemCount) { index -> ... onClick = { onLessonClick(lesson.id) } }`
      bloğunda `index` ve `lesson.title` zaten mevcut; dosyanın altındaki `@Preview`
      fonksiyonlarındaki `onLessonClick = {}` lambda'larını yeni imzaya güncelle
      (bkz. research.md §4)
- [X] T013 [US2] `app/src/main/java/com/example/nativeminds/navigation/NativeMindsNavHost.kt`'deki
      `homeScreen(onLessonClick = { lessonId -> ... })` geri çağrısını yeni imzaya uyarlayıp
      `navController.navigate(...)`'den önce
      `analyticsReporter.log(AnalyticsEvent.LessonSelected(lessonId, title, index))` çağır
      (T010'daki `NavigationAnalyticsViewModel`'e enjekte edilen `analyticsReporter`'ı yeniden
      kullan, aynı dosya — T010'dan sonra yapılır)
- [X] T014 [US2] `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderViewModel.kt`'e
      `private val analyticsReporter: AnalyticsReporter` constructor parametresi ekle; `init`
      bloğunda `_state.map { it.readyContent != null }.distinctUntilChanged().filter { it }`
      akışına bir `onEach` ekleyerek içerik ilk kez `Ready` olduğunda tam olarak bir kez
      `AnalyticsEvent.ContentViewed(lessonId, accessLevel = if (isRestricted) PREVIEW else FULL)`
      kaydet (FR-004)
- [X] T015 [US2] Aynı dosyada (`ReaderViewModel.kt`) `onIntent`'in efekt işleme `when` bloğuna:
      `StartNarration`/`ResumeNarration` dallarına `analyticsReporter.log(AnalyticsEvent.ListenStarted(lessonId))`,
      `PauseNarration` dalına `analyticsReporter.log(AnalyticsEvent.ListenStopped(lessonId, PAUSED, progressPercent))`
      ekle; `override fun onCleared()` içinde narration aktifse (Playing/Paused)
      `analyticsReporter.log(AnalyticsEvent.ListenStopped(lessonId, SCREEN_LEFT, progressPercent))`
      kaydet (FR-005, FR-006)
- [X] T016 [P] [US2] `feature/reader/src/test/java/com/example/nativeminds/feature/reader/ui/ReaderViewModelTest.kt`'e
      `RecordingAnalyticsReporter` kullanan üç yeni test ekle: içerik `Ready` olunca tek bir
      `ContentViewed`; `ListenClicked` sonrası `ListenStarted`; tekrar `ListenClicked` (duraklat)
      sonrası doğru `progressPercent`'li `ListenStopped(PAUSED)`

**Kontrol noktası**: US1 + US2 birlikte, tam bir "ana ekrandan derse gir, dinle, çık" yolculuğunu
uçtan uca raporlar.

---

## Faz 5: Kullanıcı Hikayesi 3 - Paywall'dan satın almaya kadar olan huni (Öncelik: P2)

**Hedef**: Paywall gösterimi, satın alma tıklaması, abonelik başlangıcı, paywall'dan geri gitme —
dört çıkışın hepsi ayrı ayrı ve doğru sırada.

**Bağımsız Test**: `quickstart.md` Senaryo 3 — kilitli içerikten paywall'ı aç, satın al; ayrı bir
denemede satın almadan kapat. İki akış birbirine karışmadan raporlanır.

- [X] T017 [US3] `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/navigation/PaywallRoute.kt`
      ve `PaywallNavigation.kt`'deki `@Serializable data class PaywallRoute` tanımına
      `val triggerSource: String` alanı ekle
- [X] T018 [US3] `app/src/main/java/com/example/nativeminds/navigation/NativeMindsNavHost.kt`'deki
      iki `PaywallRoute(...)` çağrısını güncelle: `readerScreen`'in `onUnlockRequested`'ı
      `triggerSource = "reader_unlock"`, `settingsScreen`'in `onPremiumClick`'i
      `triggerSource = "settings_premium"` geçsin (T013'ten sonra, aynı dosya)
- [X] T019 [US3] `feature/paywall/src/main/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallViewModel.kt`'e
      `private val analyticsReporter: AnalyticsReporter` ekle; `init` bloğunda
      `analyticsReporter.log(AnalyticsEvent.PaywallShown(route.lessonId, route.triggerSource))`
      kaydet; `onIntent`'te `PaywallIntent.PurchaseClicked` işlenirken sırasıyla
      `PaywallPurchaseClicked(lessonId, selectedPlan)` ve (sahte satın alma her zaman başarılı
      olduğundan) `SubscriptionStarted(lessonId, selectedPlan)` kaydet ve `private var purchased = true`
      yap; `override fun onCleared()` ekleyip `purchased` false ise
      `analyticsReporter.log(AnalyticsEvent.PaywallDismissed(lessonId))` kaydet
      (FR-007, FR-008, FR-009, FR-011 — bkz. research.md §5 için `PurchaseDeclined`'ın bu akışta
      neden tetiklenmediği)
- [X] T020 [P] [US3] `feature/paywall/src/test/java/com/example/nativeminds/feature/paywall/ui/paywall/PaywallViewModelTest.kt`
      dosyasını (yeni) oluştur: `RecordingAnalyticsReporter` ile (a) ViewModel oluşturulunca tek
      bir `PaywallShown`, (b) `PurchaseClicked` sonrası sırasıyla `PaywallPurchaseClicked` +
      `SubscriptionStarted`, (c) satın alma olmadan `onCleared()` çağrılınca `PaywallDismissed`,
      (d) satın alma sonrası `onCleared()` çağrılınca `PaywallDismissed` KAYDEDİLMEDİĞİNİ doğrula

**Kontrol noktası**: US1 + US2 + US3 birlikte tam dönüşüm huninin uçtan uca raporlandığını
kanıtlar.

---

## Faz 6: Kullanıcı Hikayesi 4 - AI özelliği kullanımının izlenmesi (Öncelik: P3)

**Hedef**: `AiFeatureUsed` olayının sözleşmesi çalışır durumda ve gelecekteki AI özelliği
tarafından tek satırla kullanılabilir (bu proje kapsamında AI özelliğinin kendisi henüz yok —
CLAUDE.md: "AI feature: karar verildiğinde kaydedilecek").

**Bağımsız Test**: Birim testinde `RecordingAnalyticsReporter.log(AnalyticsEvent.AiFeatureUsed(...))`
çağrısının derlenip doğru kaydedildiğini doğrulamak.

- [X] T021 [P] [US4] `core/domain/src/test/kotlin/com/example/nativeminds/domain/observability/AnalyticsEventTest.kt`
      dosyasını oluştur: `RecordingAnalyticsReporter` ile `AnalyticsEvent.AiFeatureUsed(lessonId = 1L, featureName = "story_summary")`
      kaydının `logged` listesine doğru şekilde düştüğünü doğrulayan tek bir sanity testi
      (gerçek çağrı noktası, AI özelliği tasarlandığında ayrı bir spec'te eklenecek)

**Kontrol noktası**: `AnalyticsEvent.AiFeatureUsed` sözleşmesi kanıtlanmış durumda; gerçek
enstrümantasyon AI özelliğiyle birlikte gelecek.

---

## Faz 7: Cilalama ve Kesişen Konular

**Amaç**: Anayasa'nın gerektirdiği dokümantasyon ve son doğrulama.

- [X] T022 [P] `README.md`'nin "Key Decisions" bölümüne şu kararı ekle: `:core:analytics` modülü +
      kapalı `AnalyticsEvent` hiyerarşisi + `:app`'te merkezi ekran izleme (research.md §1-§3
      özeti, ne seçildi/neden/trade-off/10× ölçekte ne değişir)
- [X] T023 [P] `README.md`'nin "Cut Corners / Assumptions" bölümüne iki madde ekle: (1)
      `PurchaseDeclined` olayı tanımlı ama sahte satın alma akışı koşulsuz başarılı olduğundan şu an
      tetiklenmiyor (research.md §5); (2) `AiFeatureUsed` olayının gerçek çağrı noktası, AI özelliği
      henüz uygulanmadığından yok
- [X] T024 [quickstart.md](quickstart.md)'deki senaryoları bir emülatörde (Pixel 10 AVD) gerçek
      Firebase projesine karşı çalıştırıp Logcat (`FA-SVC`) çıktısıyla doğrula — Senaryo 1 (gezinme,
      FORWARD/BACK), Senaryo 2 (ders seçimi, içerik/dinleme başlama-durma), Senaryo 3 (paywall hunisi:
      gösterildi/tıklandı/satın alındı/geri gidildi) tümü canlı test edildi ve doğru çalıştı. Bu süreçte
      gerçek bir hata bulundu ve düzeltildi: `ReaderViewModel.onCleared()` duraklatılmış bir dinlemede
      `ListenStopped`'ı iki kez (PAUSED + SCREEN_LEFT) logluyordu — artık yalnızca hâlâ `Playing`
      durumundaysa `SCREEN_LEFT` loglanıyor. Senaryo 4 (çevrimdışı) ve Senaryo 5 (birim testleri, zaten
      T001-T023'te çalıştırılmıştı) ayrıca doğrulanmadı
- [X] T025 `JAVA_HOME` Android Studio JBR'a ayarlanmış şekilde
      `./gradlew :core:domain:test :core:analytics:test :feature:home:test :feature:reader:test :feature:paywall:test :app:test`
      çalıştır, hepsinin geçtiğini doğrula — hepsi geçti; ayrıca `:app:assembleDebug` ile tam APK
      derlemesi de doğrulandı

---

## Bağımlılıklar ve Yürütme Sırası

### Faz Bağımlılıkları

- **Kurulum (Faz 1)**: Bağımlılık yok — hemen başlanabilir
- **Temel (Faz 2)**: Kurulumun tamamlanmasına bağlı — TÜM kullanıcı hikayelerini engeller
- **Kullanıcı Hikayeleri (Faz 3-6)**: Hepsi Temel fazın tamamlanmasına bağlı
  - US1 (Faz 3) diğer hikayelerden bağımsızdır
  - US2 (Faz 4), T010/T013'te US1'in dokunduğu `NativeMindsNavHost.kt` dosyasını paylaştığı için
    US1'den SONRA yapılmalıdır (aynı dosyada art arda düzenleme — paralel değil)
  - US3 (Faz 5), aynı sebeple US2'den sonra gelir (T018, T013'ün üzerine inşa edilir)
  - US4 (Faz 6) diğer hikayelerden tamamen bağımsızdır, herhangi bir noktada yapılabilir
- **Cilalama (Faz 7)**: İstenen tüm kullanıcı hikayelerinin tamamlanmasına bağlıdır

### Kullanıcı Hikayesi Bağımlılıkları

- **US1 (P1)**: Yalnızca Faz 2'ye bağlı — bağımsız
- **US2 (P1)**: Faz 2'ye bağlı; `NativeMindsNavHost.kt` üzerinde US1 ile sıralı (dosya paylaşımı,
  mantıksal bağımlılık yok)
- **US3 (P2)**: Faz 2'ye bağlı; `NativeMindsNavHost.kt` üzerinde US2 ile sıralı
- **US4 (P3)**: Yalnızca Faz 2'ye bağlı — tamamen bağımsız, paralel yapılabilir

### Paralel Fırsatlar

- T002 (Faz 1) diğer kurulum görevleriyle paralel
- T004, T005, T006 (Faz 2) farklı dosyalar — paralel
- T009 ve T011 (US1) paralel
- T012 (US2) farklı bir dosya seti olduğundan T009-T011 ile paralel başlanabilir, ancak T013
  `NativeMindsNavHost.kt` üzerinden T010'a bağımlı olduğundan o adım sıralı kalmalı
- T016 (US2 testleri) T014/T015 tamamlanınca T020 (US3 testleri) ile paralel
- Faz 6 (US4) herhangi bir noktada, diğer tüm fazlarla paralel yapılabilir
- T022, T023 (Faz 7) paralel

---

## Paralel Örnek: Temel Faz

```bash
# T004, T005, T006'yı birlikte başlat (farklı dosyalar, aynı modül):
Task: "AnalyticsEvent.kt sealed hiyerarşisini oluştur (core/domain)"
Task: "AnalyticsReporter.kt arayüzünü oluştur (core/domain)"
Task: "RecordingAnalyticsReporter.kt test sahtesini oluştur (core/domain/src/test)"
```

---

## Uygulama Stratejisi

### Önce MVP (Yalnızca Kullanıcı Hikayesi 1)

1. Faz 1: Kurulum'u tamamla
2. Faz 2: Temel'i tamamla (KRİTİK — tüm hikayeleri engeller)
3. Faz 3: US1'i tamamla
4. **DUR ve DOĞRULA**: `quickstart.md` Senaryo 1'i bağımsız olarak test et
5. Bu noktada uygulama, kullanıcıların uygulama içinde nasıl gezindiğini raporluyor olacak

### Artımlı Teslimat

1. Kurulum + Temel → Temel hazır
2. US1 ekle → Bağımsız test et → Gezinme verisi akıyor (MVP!)
3. US2 ekle → Bağımsız test et → Ders/dinleme verisi akıyor
4. US3 ekle → Bağımsız test et → Paywall hunisi tam raporlanıyor
5. US4 ekle → AI özelliği sözleşmesi hazır (gerçek özellik ayrı bir spec'te gelecek)
6. Her hikaye bir öncekini bozmadan değer ekler

---

## Notlar

- [P] görevler = farklı dosyalar, tamamlanmamış bir göreve bağımlılık yok
- [Story] etiketi görevi ilgili kullanıcı hikayesine bağlar (izlenebilirlik için)
- US2 ve US3, `NativeMindsNavHost.kt` üzerinde US1 ile dosya paylaştığı için "bağımsız test
  edilebilir" olsalar da yürütme sırası dosya çakışmasını önlemek amacıyla sıralıdır
- Her görevden sonra veya mantıksal bir grup tamamlandığında commit at
- Her kontrol noktasında durup hikayeyi bağımsız olarak doğrula
- Kaçının: belirsiz görevler, aynı dosyada çakışan paralel düzenlemeler, hikaye bağımsızlığını
  bozan çapraz bağımlılıklar
