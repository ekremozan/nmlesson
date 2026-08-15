# Araştırma: Firebase Analytics Entegrasyonu

## 1. Modül yerleşimi: yeni `:core:analytics` mi, mevcut `:core:crashreporting`'i mi genişletme?

- **Karar**: Yeni bir `:core:analytics` modülü eklenecek; `:core:crashreporting`'e dokunulmayacak.
- **Gerekçe**: `:core:crashreporting`, `ErrorReporter` arayüzünü barındıran ve tek sorumluluğu hata
  raporlama olan bir modül. Analiz olayları farklı bir Firebase SDK'sı (`firebase-analytics`),
  farklı bir yaşam döngüsü ve farklı bir domain sözleşmesi (`AnalyticsReporter`) gerektirir. Aynı
  modülde ikisini birleştirmek, "her modül tek bir Firebase yeteneğini sarmalar" simetrisini bozar
  ve `:core:crashreporting`'in adını yanıltıcı hale getirir. Yeni modül, mevcut
  `:core:crashreporting`'in `build.gradle.kts` + `di/` + tek implementasyon dosyası kalıbını birebir
  kopyalar — yani sıfırdan bir desen icat edilmiyor.
- **Değerlendirilen alternatifler**: (a) `:core:crashreporting`'i `:core:observability` olarak
  yeniden adlandırıp ikisini birden barındırmak — reddedildi, gereksiz bir taşıma/yeniden adlandırma
  riski taşıyor ve mevcut çalışan modülü bu özellik kapsamı dışında değiştiriyor. (b) Analiz
  mantığını doğrudan `:core:data`'ya koymak — reddedildi, `:core:data` repository'lerin yeridir,
  Firebase SDK bağımlılığı barındırmaz.

## 2. Olay modeli: `sealed class AnalyticsEvent` mi, stringly-typed `logEvent(name, params)` mi?

- **Karar**: `:core:domain`'de kapalı bir `AnalyticsEvent` hiyerarşisi (`sealed interface`) ve
  `AnalyticsReporter.log(event: AnalyticsEvent)` tek metodu.
- **Gerekçe**: `ErrorReporter.report(throwable, context)` da aynı şekilde tipli ve tek metotludur.
  Stringly-typed bir `logEvent(name: String, params: Map<String, Any>)` yaklaşımı, her çağrı
  noktasında yazım hatası ve eksik/yanlış parametre riski taşır ve derleme zamanında yakalanmaz.
  Kapalı hiyerarşi, hangi olayların var olduğunu tek dosyada listeler (bu spesifikasyonun FR-001
  ilâ FR-012'sinin doğrudan karşılığıdır) ve her olay kendi zorunlu parametrelerini constructor'da
  taşır. Firebase'e eşleme (`event adı + Bundle`) yalnızca `:core:analytics` içindeki tek bir
  `when` bloğunda yapılır — model ayrımı ilkesiyle (constitution madde II) uyumludur.
- **Değerlendirilen alternatifler**: Stringly-typed genel API — reddedildi, yukarıdaki risklerle.
  Her olay için ayrı bir arayüz metodu (`logScreenView(...)`, `logLessonSelected(...)`, ...) —
  reddedildi; `AnalyticsReporter`'ı her yeni olayda büyüyen geniş bir arayüze çeviriyor, oysa kapalı
  hiyerarşi + tek `log()` metodu Genişlet/Kapat prensibine daha uygun (yeni olay = yeni `sealed`
  alt tip, arayüz değişmez).

## 3. Ekran görüntüleme (screen view) olaylarının tetiklenme noktası

- **Karar**: `NativeMindsNavHost` içinde `NavController.currentBackStackEntryFlow` (veya
  `addOnDestinationChangedListener`) dinlenerek merkezi olarak tetiklenir; `:app` modülü zaten
  somut implementasyonları (`AnalyticsReporter`'ın Hilt'ten enjekte edilmiş hâli) görebilen tek
  modül olduğundan bu, mimari kuralları ihlal etmez.
- **Gerekçe**: Constitution'a göre "bir ekran composable'ı asla `NavController` almaz" ve
  "dışarı gezinme bir intent değil, düz bir callback'tir". Ekran görüntüleme olayını her
  `:feature:*` modülüne bir intent olarak eklemek hem bu kuralı ihlal eder hem de her yeni ekranda
  tekrar eden kod üretir. `NavController`'ın hedef değişikliklerini zaten tek bir yerden (`:app`)
  gözlemleyebiliyor olması, ekran adı + önceki ekran + gezinme yönü (ileri/geri) bilgisinin doğal
  olarak tek noktada toplanmasını sağlar (FR-001/FR-002).
- **Değerlendirilen alternatifler**: Her `…ScreenContent`'in `LaunchedEffect(Unit)` içinde kendi
  "görüntülendi" intent'ini göndermesi — reddedildi; hem tekrarlıyor hem de "önceki ekran" bilgisini
  bir feature modülünün bilmesi mümkün değil (başka bir feature'a bağımlı olmadan). `NavController`'ı
  her ekrana enjekte etmek — açıkça yasak.

## 4. "Ders seçildi" olayının parametreleri (kimlik + başlık + liste konumu)

- **Karar**: `HomeRoute.homeScreen(...)` ve `HomeScreen`'deki `onLessonClick` imzası
  `(Long) -> Unit`'ten `(lessonId: Long, title: String, index: Int) -> Unit`'e genişletilir. Bu bir
  intent değil, düz bir callback imza değişikliğidir (mimari kural yalnızca callback'in intent
  olmamasını şart koşar, imzasını sabitlemez); tıklama anında `lesson` öğesi ve `index` zaten
  `LazyColumn`'daki `items(...) { index -> ... }` kapsamında mevcuttur (bkz.
  `HomeScreen.kt:183-187`).
- **Gerekçe**: FR-003, ders kimliği + başlık + liste konumunun birlikte raporlanmasını istiyor.
  Bu veriler yalnızca tıklama anında, composable içinde mevcut; ViewModel'e taşımak (ör.
  `HomeIntent.LessonSelected`) gereksiz bir state mutasyonu olur (ders seçimi ekranın kendi state'ini
  değiştirmiyor, yalnızca gezinme tetikliyor) — bu yüzden intent değil, zenginleştirilmiş bir
  callback tercih edildi.
- **Değerlendirilen alternatifler**: `HomeIntent.LessonSelected` eklemek — reddedildi, MVI kuralı
  "her kullanıcı eylemi bir intent'tir" der ama bu eylem hiçbir state'i değiştirmiyor; var olan
  "gezinme bir intent değildir" istisnasına daha çok uyuyor. Yalnızca `lessonId` ile geçip başlığı
  reader tarafında yeniden çözmek — reddedildi, gereksiz ikinci bir sorgu ve olayın "hangi ders
  listede hangi sırada seçildi" bilgisini kaybeder.

## 5. Paywall satın alma hunisindeki "vazgeçildi" (declined) durumunun mevcut sahte akışla uyumu

- **Karar**: `AnalyticsEvent.PurchaseDeclined` sözleşmede tanımlanır ve `AnalyticsReporter`
  arayüzünde yer alır, ancak mevcut sahte (mock) satın alma akışı (`PaywallIntent.PurchaseClicked`
  → `entitlementRepository.setPremium(true)` her zaman başarılı) bu olayı **tetikleyecek gerçek bir
  başarısızlık yolu içermiyor**. Bu bilinçli bir eksik olarak README "Cut Corners / Assumptions"
  bölümüne, uygulama sırasında kaydedilecek.
- **Gerekçe**: CLAUDE.md'de "gerçek ödemeler kapsam dışı, sahte/sandbox abonelik akışı kabul
  edilebilir" deniyor; sahte akış zaten koşulsuz başarılı. Olayı sözleşmede tanımlamak (gerçek bir
  ödeme SDK'sı eklendiğinde tak-çalıştır kullanılabilir olması için) FR-010'u karşılar; olayı
  zorla tetikleyecek yapay bir "rastgele başarısızlık" eklemek ise gerçek kullanıcı davranışını
  yansıtmayan, test ortamında yanıltıcı analiz verisi üretecek bir kısayol olurdu — bu daha kötü bir
  seçim.
- **Değerlendirilen alternatifler**: `RestorePurchasesClicked` → `ShowNoPurchaseFound` yolunu
  "vazgeçildi" olarak işaretlemek — reddedildi, semantik olarak farklı bir eylem (yeni satın alma
  değil, geri yükleme). Yapay/rastgele başarısızlık enjekte etmek — reddedildi, yukarıdaki gerekçe.

## 6. Çevrimdışı olay kuyruklama (FR-016/SC-003)

- **Karar**: Ayrı bir yerel depolama/kuyruk yazılmayacak; Firebase Analytics (GA4 for Firebase)
  SDK'sı olayları cihazda otomatik olarak tamponlayıp bağlantı geldiğinde toplu gönderir
  (varsayılan davranış, ek yapılandırma gerekmez).
  `Firebase.analytics.setAnalyticsCollectionEnabled(true)` varsayılan olarak açıktır ve
  offline/online geçişini SDK yönetir.
- **Gerekçe**: SDK'nın belgelenen davranışı zaten SC-003'ü karşılıyor; kendi kuyruğumuzu yazmak
  hem gereksiz karmaşıklık hem de "sahte bir kalıcılık katmanı" riski taşır (constitution madde I:
  gereksiz soyutlama eklenmez).
- **Değerlendirilen alternatifler**: Room tablosunda özel bir olay kuyruğu tutup `NetworkMonitor`
  ile senkronize etmek — reddedildi, SDK zaten bunu yapıyor; bu, "üç benzer satır bir soyutlamadan
  iyidir" ilkesinin tersi yönde gereksiz bir mühendislik olurdu.

## 7. Hata yönetimi (FR-014/FR-015)

- **Karar**: `FirebaseAnalyticsReporter.log(event)` içindeki her çağrı `runCatching` ile sarılır;
  başarısızlık `ErrorReporter.report(throwable, context = "analytics:<eventName>")` üzerinden
  crash raporlamaya iletilir, kullanıcıya hiçbir şey gösterilmez ve akış kesilmez.
- **Gerekçe**: Constitution madde IV ("Failures must be visible... to crash reporting always") ve
  `ErrorReporter`'ın zaten "Implementations must never throw" sözleşmesiyle birebir tutarlı.
- **Değerlendirilen alternatifler**: Hataları tamamen yutmak — reddedildi, "sessizce yutulan
  istisna yok" kuralını ihlal eder. Kullanıcıya hata göstermek — reddedildi, analiz kaydı kullanıcı
  akışını etkilememeli (FR-014).

## Çözülen NEEDS CLARIFICATION

Spesifikasyonda hiç `[NEEDS CLARIFICATION]` işareti kalmamıştı; bu bölüm yalnızca planlama
sırasında ortaya çıkan tasarım kararlarını belgeler.
