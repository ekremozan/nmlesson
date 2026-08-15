# Research: API ve Çökme Hatalarında Best-Practice Hata Yönetimi ve Crashlytics Entegrasyonu

## 1. Merkezi toplama noktası: yeni soyutlama değil, mevcut seam

**Decision**: Kullanıcının istediği "nerede olursa olsun crash olduğunda tek bir yerden collect etme"
yapısı, yeni bir katman eklenmeden mevcut `ErrorReporter` arayüzü (`:core:domain`) üzerinden
karşılanacak.

**Rationale**: Kod tabanı araştırması, tüm yakalanmış hataların bugün zaten tek bir noktadan
geçtiğini doğruladı: `RefreshLessonContentUseCase.kt:18-19`, `SyncLessonsUseCase.kt:19-20` ve
`TextToSpeechNarrator.kt:210-212` — üçü de `ErrorReporter.report(throwable, context)` çağırıyor.
Bunun üstüne ikinci bir "collector" katmanı eklemek gereksiz soyutlama olur (Constitution Principle
I: "Complexity that is not justified is removed rather than documented").

**Alternatives considered**: Yeni bir `CrashCollector`/`ErrorBus` sınıfı — reddedildi, mevcut
arayüzün yaptığı işi tekrar eder.

## 2. Implementasyonun yeri: yeni `:core:crashreporting` modülü

**Decision**: Firebase Crashlytics implementasyonu, `:core:data` içine değil, `:core:audio`
modülüyle birebir aynı desende yeni bir `:core:crashreporting` modülüne konur.

**Rationale**: Kullanıcı açıkça "modüler bir yapıda olacak" dedi. `:core:data` zaten repository'ler,
`NetworkMonitor` ve senkronizasyon mantığını taşıyan geniş sorumluluklu bir modül; ona üçüncü bir
büyük bağımlılık (Firebase BoM) daha eklemek modülün sorumluluğunu bulanıklaştırır. `:core:audio`
tam olarak bu deseni zaten kanıtlıyor: somut bir platform teknolojisini (`TextToSpeech`/Media3) bir
`:core:domain` arayüzüne (`StoryNarrator`) `@Binds` ile bağlayan, kendi başına duran bir modül.
Crashlytics için aynı şekli kullanmak, ileride raporlama backend'i değiştirilmek istenirse (ör.
Sentry) tek bir modülün değişmesi anlamına gelir ve `:core:data`'yı Firebase'den tamamen bağımsız
tutar.

**Alternatives considered**: `LogcatErrorReporter` gibi doğrudan `:core:data` içine eklemek — daha
az modül ama kullanıcının modülerlik isteğiyle çelişiyor ve `:core:data`'nın sorumluluğunu büyütüyor.

## 3. Gerçek çökmeler: SDK'nın kendi otomatik handler'ı, elle yazılmış handler yok

**Decision**: `Thread.setDefaultUncaughtExceptionHandler` ile özel bir handler YAZILMAYACAK.
Firebase Crashlytics SDK'sı başlatıldığında (Hilt `Application` sınıfı ayağa kalktığında, SDK'nın
kendi `ContentProvider` init'i ile) zaten JVM'in `Thread.UncaughtExceptionHandler`'ını sarmalayıp
kendi merkezi toplama noktasını kurar.

**Rationale**: Bunun üzerine ikinci bir handler yazmak hem "tek yerden toplama" ilkesini ihlal eder
hem de iki handler'ın birbirini override etmesi/yarışması riski taşır. Ek olarak SDK, native/ANR
çökmeleri de kapsayan, üretimde kanıtlanmış bir mekanizma sağlıyor — bunu elle yeniden icat etmenin
hiçbir kazancı yok.

**Alternatives considered**: Özel `UncaughtExceptionHandler` + manuel `recordException` çağrısı —
reddedildi.

## 4. Yakalanmış hatalar da non-fatal olarak Crashlytics'e

**Decision**: `FirebaseCrashlyticsErrorReporter.report(throwable, context)`,
`FirebaseCrashlytics.getInstance().log(context)` ile bağlam bilgisini breadcrumb olarak ekler,
ardından `recordException(throwable)` çağırır — böylece bu olay Crashlytics panelinde "Non-fatals"
altında, hangi işlemde oluştuğu bilgisiyle birlikte görünür.

**Rationale**: `/speckit-specify` sırasında kullanıcı bu seçeneği ("yakalanmış hatalar da non-fatal
olarak Crashlytics'e") açıkça onayladı (spec FR-010). Tek panelde hem çökmeler hem yönetilmiş
hatalar görünür olur; ayrı bir görünürlük kanalı tutmanın (ör. sadece Logcat) getirisi yok.

**Alternatives considered**: Yakalanmış hataları yalnızca Logcat'te bırakmak — reddedildi (kullanıcı
tercihiyle çelişiyor).

## 5. PII: kullanıcı kimliği set edilmiyor

**Decision**: `FirebaseCrashlytics.setUserId(...)` çağrılmayacak; SDK'nın kendi anonim kurulum
(installation) ID'si yeterli kabul edilecek. Context string'leri yalnızca ekran/işlem adı gibi
teknik bağlam taşır, hiçbir zaman kullanıcı girdisi (e-posta, ad, arama sorgusu metni) içermez.

**Rationale**: Projede gerçek bir kullanıcı hesap/auth sistemi yok (`MockEntitlementRepository`
bellekte tutulan sahte bir abonelik durumu); ekstra bir kimlik alanı eklemek gereksiz ve spec FR-007
ile çelişen bir gizlilik riski taşır.

**Alternatives considered**: Anonim ama kalıcı bir cihaz UUID'si set etmek — reddedildi, SDK'nın
kendi installation ID'si zaten bu ihtiyacı karşılıyor, tekrar icat etmeye gerek yok.

## 6. Çevrimdışı kuyruklama: SDK'ya bırakılıyor, Room şeması değişmiyor

**Decision**: Spec FR-008'deki "bağlantı yokken oluşan çökme raporları cihazda tutulup bağlantı
gelince gönderilsin" gereksinimi, Firebase Crashlytics SDK'sının kendi yerleşik davranışıyla
karşılanır — SDK, raporları cihazda otomatik olarak saklar ve bir sonraki uygulama açılışında (veya
bağlantı geldiğinde) gönderir. Bunun için Room'da yeni bir tablo veya özel bir kuyruklama mekanizması
YAZILMAYACAK.

**Rationale**: Bu davranış SDK'nın temel özelliğidir; yeniden inşa etmek gereksiz karmaşıklık ve
hata riski ekler.

## 7. UX: Retry ve hata mesajları — mevcut desenler yeniden kullanılıyor

**Decision**: FR-001/FR-002/FR-003/FR-009 için yeni bir UI deseni icat edilmeyecek; mevcut, zaten
best-practice olan iki desen esas alınacak:
- **Home**: `PullToRefreshBox` + `HomeIntent.RefreshRequested` + `HomeEffect.ShowSyncError`
  (`HomeScreen.kt:109-111`, `HomeContract.kt:53`, `HomeViewModel.kt:67`).
- **Reader**: `ReaderIntent.RetryRequested` → `retryToken` artışı → içerik akışının yeniden
  abone olması (`ReaderContract.kt:43,161`, `ReaderReducer.kt:21-22`), `ReaderUnavailableState`
  bileşeni ile çevrimdışı/hata ayrımı.

Bu planın kapsamındaki tek değişiklik: (a) bu ekranlardaki hata mesajı string kaynaklarının teknik
olmayan, tutarlı bir dille yazıldığının doğrulanması, (b) `ShowSyncError`/`RetryRequested` sonrası
aynı hatanın arka arkaya defalarca kullanıcıya gösterilmediğinin (spam yapmadığının) doğrulanması —
her ikisi de mevcut effect-channel deseni zaten tek seferlik (one-shot) olduğu için doğal olarak
sağlanıyor.

**Rationale**: Constitution Principle III (MVI, tek mutasyon yolu) ve Principle I (gereksiz
karmaşıklık yok) — çalışan, test edilmiş bir deseni tekrar icat etmemek.

**Alternatives considered**: Yeni bir ortak "ErrorBanner" bileşeni / merkezi hata state'i —
reddedildi, mevcut per-feature efekt/retry deseni zaten spec gereksinimlerini karşılıyor ve MVI
sınırlarını (feature modülleri birbirine bağımlı olamaz) ihlal etmiyor.

## 8. Test stratejisi

**Decision**: `FirebaseCrashlyticsErrorReporter` için ayrı bir birim testi YAZILMAYACAK; bu, mevcut
`LogcatErrorReporter`'ın da test edilmediği emsalle tutarlıdır (araştırma: "No dedicated
LogcatErrorReporter test"). Doğrulama, quickstart.md'deki manuel Firebase konsolu kontrolüyle
yapılır. Buna karşılık, `ErrorReporter`'ı çağıran iş mantığı (use case'ler, ViewModel'lar) zaten
`RecordingErrorReporter`/`NoOpErrorReporter`/`SilentErrorReporter` fake'leriyle test ediliyor ve bu
testler değişmeden kalır — çünkü `ErrorReporter` arayüzü ve çağrı noktaları değişmiyor, sadece
`@Binds` hedefi değişiyor.

**Rationale**: `FirebaseCrashlytics` üçüncü taraf, `final` bir sınıf; sahte (fake) bir sürümünü
yazmak SDK'nın gerçek davranışını test etmeyeceği için düşük değerli bir test olurdu. Constitution,
"business rules carrying" domain/repository kodunu test etmeyi zorunlu kılıyor — ince bir SDK
adaptörü bu kapsamın dışında.

**Alternatives considered**: `FirebaseCrashlytics`'i saran ek bir arayüz + Robolectric testi —
reddedildi, katma değeri düşük, ekstra bakım yükü yüksek.

## 9. Gradle/bağımlılık kurulumu

**Decision**: `gradle/libs.versions.toml`'a şunlar eklenir: `firebase-bom` (version), `google-services`
(plugin), `firebase-crashlytics` (plugin) ve `firebase-crashlytics` (kütüphane, BoM üzerinden sürüm
alır). Root `build.gradle.kts`'e her iki eklenti `apply false` olarak eklenir (mevcut desen); `app/
build.gradle.kts` bunları uygular ve `:core:crashreporting`'e bağımlılık ekler; `core/crashreporting/
build.gradle.kts` Firebase BoM'u `implementation(platform(...))` ile alıp `firebase-crashlytics`
kütüphanesini kullanır.

**Rationale**: Constitution Principle I: "New dependencies MUST be declared in
`gradle/libs.versions.toml`... and justified in the same commit." README "Key Decisions" bölümünde
bu bağımlılık seti tek bir karar olarak gerekçelendirilecek.

## 10. `google-services.json` — kapsam dışı bir kurulum adımı

**Decision**: Gerçek bir Firebase projesi oluşturmak ve `app/google-services.json` dosyasını o
projeden indirip yerleştirmek, bu planın (kod değişikliği) kapsamı dışındadır; geliştirici tarafından
Firebase konsolundan elle yapılacak bir adımdır. Repoya sahte/örnek bir dosya konulmaz.

**Rationale**: Bu bir hesap/altyapı kurulum adımı, kod değişikliği değil; gerçek proje kimlik
bilgilerini (API anahtarı, proje numarası) içeren bir dosyayı yapay olarak üretmek anlamsız ve
yanıltıcı olurdu. README "Cut Corners / Assumptions" bölümünde bu adımın geliştirici tarafından
tamamlanması gerektiği açıkça belirtilecek.

**Alternatives considered**: Sahte/placeholder `google-services.json` commit etmek — reddedildi,
build'i yanıltıcı şekilde "çalışıyormuş gibi" gösterir ama gerçek bir Firebase projesine
bağlanmaz.
