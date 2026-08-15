# Sözleşme: `ErrorReporter` — Modüller Arası Tek Toplama Noktası

Bu, uygulamanın herhangi bir yerindeki (feature modülü, use case, repository, `:core:audio`) hata
yönetimi kodunun uyması gereken tek iç sözleşmedir. Kullanıcının "nerede olursa olsun tek bir yerden
collect etme" isteğinin somutlaştığı yer burasıdır.

## Arayüz

Konum: `core/domain/src/main/kotlin/com/example/nativeminds/domain/observability/ErrorReporter.kt`
(bu plan kapsamında **değişmez**).

```kotlin
interface ErrorReporter {
    fun report(throwable: Throwable, context: String)
}
```

## Davranış sözleşmesi

1. **Her yakalanan hata buradan geçer.** Bir `catch`/`runCatching.onFailure` bloğu kullanıcıya bir
   hata durumu gösteriyorsa, aynı blok `ErrorReporter.report(...)` çağırmadan bitemez (spec FR-004).
2. **Raporlama, kullanıcıya göstermenin yerini almaz, ona ek olarak yapılır.** Bir hata hem ekrana
   hem raporlamaya gider; sadece birine değil.
3. **Implementasyon asla fırlatmaz.** `report()` içinde oluşan herhangi bir ikincil hata (ör. ağ
   yokken raporlama servisine ulaşılamaması), çağıranı etkilememelidir — bu implementasyonun
   sorumluluğudur (Firebase Crashlytics SDK'sı bu garantiyi zaten sağlar, kendi içinde kuyruklar).
4. **`context` insan-okunur ve PII içermez.** Hangi işlem/ekranda oluştuğunu söyler (ör. sınıf/use
   case adı), asla kullanıcı girdisi taşımaz (spec FR-007).
5. **Tek implementasyon, iki hedef.** Bu plan kapsamında tek bir `FirebaseCrashlyticsErrorReporter`
   (`:core:crashreporting`), hem yakalanmış hataları (non-fatal) hem de — SDK'nın kendi otomatik
   handler'ı aracılığıyla, bu arayüzün dışında — gerçek çökmeleri aynı Firebase Crashlytics projesine
   yönlendirir. Böylece geliştirici tek bir panelden bakar.

## Çağıran taraflar (bu plan kapsamında değişmeyen mevcut kullanım)

| Çağıran | Konum | Not |
|---|---|---|
| `RefreshLessonContentUseCase` | `:core:domain` | `runCatching` + `.onFailure { errorReporter.report(...) }` |
| `SyncLessonsUseCase` | `:core:domain` | Aynı desen |
| `TextToSpeechNarrator` | `:core:audio` | `startService` çağrısını sarar |

Yeni çağıranlar eklenirse (ör. gelecekte yeni bir uzak veri kaynağı), aynı sözleşmeye uymalıdır.

## Binding (bu planda değişen tek satır)

Önce: `core/data/.../di/DataModule.kt` içinde `LogcatErrorReporter` → `ErrorReporter`.

Sonra: `core/crashreporting/.../di/CrashReportingModule.kt` içinde
`FirebaseCrashlyticsErrorReporter` → `ErrorReporter`.

Arayüzün kendisi ve tüm çağıranlar değişmediği için bu, mevcut testleri (fake `ErrorReporter`
implementasyonlarını kullanan) etkilemez.
