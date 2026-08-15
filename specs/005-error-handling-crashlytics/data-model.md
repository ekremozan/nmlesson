# Data Model: API ve Çökme Hatalarında Best-Practice Hata Yönetimi ve Crashlytics Entegrasyonu

Bu özellik kalıcı bir yerel veri modeli (Room entity) eklemez — çökme/hata kuyruklama, Firebase
Crashlytics SDK'sı tarafından cihaz üzerinde kendi içinde yönetilir (bkz. research.md #6). Aşağıdaki
iki kavramsal varlık, sadece `ErrorReporter` arayüzünden geçen verinin şeklini belgelemek için
tanımlanmıştır; ikisi de kod içinde ayrı bir sınıf/tablo olarak var olmaz.

## Hata Olayı (Handled Error Event)

Uygulama içinde yakalanan, kullanıcıyı çökertmeyen bir hatayı temsil eder. `ErrorReporter.report()`
çağrısının parametreleriyle birebir örtüşür.

| Alan | Tür | Açıklama |
|---|---|---|
| `throwable` | `Throwable` | Yakalanan istisna; orijinal stack trace korunur |
| `context` | `String` | Hatanın oluştuğu işlem/ekranın insan-okunur adı (ör. `"SyncLessonsUseCase"`) — asla kullanıcı girdisi veya kişisel veri içermez |

**Validation/kurallar**:
- `context` boş olamaz (çağıran taraf her zaman anlamlı bir tanımlayıcı geçirmelidir).
- `throwable` asla `null` olamaz (Kotlin tip sisteminde zaten garanti).
- Bu olay, `ErrorReporter` implementasyonunda **asla** yeni bir istisna fırlatmamalıdır (mevcut
  KDoc kısıtı: "Implementations must never throw").

**Hedef**: Firebase Crashlytics "Non-fatals" — `log(context)` + `recordException(throwable)`.

## Çökme Raporu (Crash Report)

Uygulamanın beklenmedik şekilde sonlandığı bir olayı temsil eder. Bu, uygulama kodu tarafından
oluşturulmaz — Firebase Crashlytics SDK'sının otomatik `UncaughtExceptionHandler`'ı tarafından
yakalanır (bkz. research.md #3).

| Alan | Tür | Açıklama | Kaynak |
|---|---|---|---|
| Stack trace | — | Çökmeye neden olan istisna zinciri | SDK otomatik |
| Cihaz/uygulama bağlamı | — | Cihaz modeli, OS sürümü, uygulama sürüm kodu | SDK otomatik |
| Zaman damgası | — | Çökme anı | SDK otomatik |
| Kurulum kimliği | — | SDK'nın kendi ürettiği anonim installation ID; **kullanıcıyı tanımlamaz** | SDK otomatik |

**Validation/kurallar**:
- Uygulama kodu, `FirebaseCrashlytics.setUserId(...)` çağırmaz (spec FR-007, research.md #5).
- Uygulama kodu, hiçbir özel `UncaughtExceptionHandler` kaydetmez (research.md #3).

**Hedef**: Firebase Crashlytics "Crashes" (fatals).
