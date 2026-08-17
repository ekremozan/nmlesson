# Uygulama Planı: AI ile Üretilmiş Çoktan Seçmeli Test (Gemini)

**Dal**: `007-ai-quiz-generation` | **Tarih**: 2026-08-16 | **Spec**: [spec.md](spec.md)

**Girdi**: `specs/007-ai-quiz-generation/spec.md` içindeki özellik spesifikasyonu

> **Düzeltme notu (2026-08-16, gerçek cihaz testinden sonra)**: Bu plan `com.google.ai.client.generativeai`
> SDK'sını ve `gemini-2.5-flash` modelini öngörüyordu. İkisi de gerçek cihazda çalışmadığı için
> düzeltildi — sırasıyla düz bir Ktor `HttpClient` REST çağrısı ve `gemini-flash-latest` ile.
> Tam gerekçe için [research.md](research.md) R1/R2. Aşağıdaki metin planlama anındaki hâliyle
> (tarihsel kayıt olarak) bırakıldı.

## Özet

Hikaye okuma ekranına ("Reader") bir "Test" butonu eklenir. Butona basıldığında yeni bir ekrana
(`:feature:quiz`) geçilir; bu ekran, o hikayenin tüm metnini Gemini API'sine (istemciden doğrudan,
`generativeai` Android SDK'sı ile) göndererek anlık olarak 4 şıklı, tek doğru cevaplı bir soru ve
doğru cevabın kısa açıklamasını üretir. Kullanıcı bir şık seçtiğinde doğru/yanlış görsel hâli ve
açıklama gösterilir. Özellik yalnızca premium kullanıcılar için etkindir; soru hiçbir yerde kalıcı
olarak saklanmaz — her "Test" tetiklemesi yeni bir AI çağrısıdır.

## Teknik Bağlam

**Dil/Sürüm**: Kotlin 2.2.10, JVM target 11, Jetpack Compose (Compose BOM), Material 3

**Ana Bağımlılıklar**: Hilt (DI), `com.google.ai.client.generativeai` (Gemini Android SDK, yeni),
kotlinx.serialization (mevcut), mevcut `:core:domain` / `:core:data` / `:core:database` katman
kalıpları

**Depolama**: Yok — üretilen soru kalıcı olarak saklanmaz, yalnızca ekran ömrü boyunca ViewModel
state'inde tutulur (bkz. [research.md](research.md) R4)

**Test**: JUnit + `kotlinx-coroutines-test` (use case ve reducer birim testleri), az sayıda Compose
UI testi (`QuizScreen` durumları)

**Hedef Platform**: Android, `minSdk` 24 / `compileSdk`-`targetSdk` 36

**Proje Türü**: Mobile-app (çok modüllü Android — bkz. Proje Yapısı)

**Performans Hedefleri**: SC-001 — normal ağ koşullarında soru ekranı 10 saniye içinde dolu olmalı

**Kısıtlar**: Gemini API anahtarı repo'ya asla girmemeli (mevcut Supabase anahtarı deseniyle aynı
şekilde `local.properties` → `BuildConfig`); özellik ağ bağlantısı gerektirir ve bunu açıkça
belirtir; mevcut MVI/Hilt/Clean Architecture kurallarına ve "kod içinde `//` yorum yok" kuralına
tam uyum

**Ölçek/Kapsam**: Tek yeni özellik modülü (`:feature:quiz`), `:core:domain` ve `:core:data`'da
küçük eklemeler, `:app`'te bir yeni nav rotası kablolaması; `:core:database` değişmez (kalıcılık
yok)

## Anayasa Kontrolü

*KAPI: Phase 0 araştırmasından önce geçmeli. Phase 1 tasarımından sonra yeniden değerlendirilir.*

| İlke | Durum | Not |
|---|---|---|
| I. Savunulabilir Kararlar | GEÇTİ | Gemini SDK seçimi, kalıcılık yok kararı ve gating yaklaşımı `research.md`'de gerekçelendirildi; README "Key Decisions"a taşınacak |
| II. Clean Architecture & Model Ayrımı | GEÇTİ | DTO (`GeminiQuizPayloadDto`) → domain (`QuizQuestion`) → UI (`QuizQuestionUiModel`) ayrımı korunuyor; `:feature:quiz` yalnızca `:core:domain`'e bağımlı |
| III. MVI Tek Mutasyon Yolu | GEÇTİ | `QuizContract`/`QuizReducer` deseni `ReaderContract`/`ReaderReducer` ile birebir aynı şekilde uygulanacak |
| IV. Offline-First & Görünür Hatalar | KISMİ — GEREKÇELİ | Bu AI özelliği kasıtlı olarak ağ gerektirir (spec Varsayımlar); bu, hikaye metni/sesinin offline okunabilirliğini etkilemez. Hatalar asla yutulmaz: her başarısızlık `ErrorReporter`'a raporlanır ve kullanıcıya yeniden deneme eylemiyle gösterilir |
| V. Tasarım Sistemi Tek Stil Kaynağı | GEÇTİ | `QuizScreen`, Claude Design projesindeki 10a/10b/10c ekranlarını birebir referans alır; sabit renk/boyut yok, `NativeMindsTheme` token'ları kullanılır |
| Teknoloji Kısıtları | GEÇTİ | Yeni bağımlılık (`generativeai`) `gradle/libs.versions.toml`'a eklenip gerekçelendirilecek; `minSdk`/`compileSdk` değişmiyor |

Phase 1 sonrası yeniden değerlendirme: [Phase 1 Sonrası Anayasa Kontrolü](#phase-1-sonrası-anayasa-kontrolü) bölümüne bakınız.

## Proje Yapısı

### Dokümantasyon (bu özellik)

```text
specs/007-ai-quiz-generation/
├── plan.md              # Bu dosya (/speckit-plan çıktısı)
├── research.md          # Phase 0 çıktısı
├── data-model.md         # Phase 1 çıktısı
├── quickstart.md         # Phase 1 çıktısı
├── contracts/             # Phase 1 çıktısı
│   └── gemini-quiz-contract.md
└── tasks.md              # Phase 2 çıktısı (/speckit-tasks — bu komutla oluşturulmaz)
```

### Kaynak Kod (repo kökü)

```text
core/domain/src/main/kotlin/com/example/nativeminds/domain/
├── model/
│   └── QuizQuestion.kt                 # YENİ — domain modeli (QuizQuestion, QuizOption)
├── repository/
│   └── QuizRepository.kt               # YENİ — arayüz: suspend fun generateQuestion(...)
└── usecase/
    └── GenerateQuizUseCase.kt          # YENİ — entitlement + repository orkestrasyonu

core/data/src/main/java/com/example/nativeminds/data/
├── remote/quiz/
│   ├── GeminiQuizDataSource.kt         # YENİ — arayüz
│   ├── GeminiRemoteQuizDataSource.kt   # YENİ — impl (generativeai SDK)
│   └── dto/GeminiQuizPayloadDto.kt     # YENİ — DTO + toDomain() mapper
├── repository/
│   └── QuizRepositoryImpl.kt           # YENİ
└── di/
    ├── NetworkModule.kt                # DEĞİŞTİ — GenerativeModel @Provides + GEMINI_API_KEY
    └── RepositoryModule.kt             # DEĞİŞTİ — QuizRepository @Binds

feature/quiz/                            # YENİ MODÜL
└── src/main/java/com/example/nativeminds/feature/quiz/
    ├── ui/
    │   ├── QuizContract.kt
    │   ├── QuizReducer.kt
    │   ├── QuizViewModel.kt
    │   ├── QuizScreen.kt
    │   ├── mapper/QuizMappers.kt
    │   └── preview/QuizPreviewData.kt
    └── navigation/
        └── QuizRoute.kt                # QuizRoute(lessonId), NavGraphBuilder.quizScreen(...)

feature/reader/src/main/java/com/example/nativeminds/feature/reader/
├── ui/
│   ├── ReaderContract.kt               # DEĞİŞTİ — showTestAction: Boolean eklendi (türetilmiş)
│   ├── ReaderScreen.kt                 # DEĞİŞTİ — "Test" butonu (yalnızca ReaderAccess.Full)
│   └── components/ReaderTopBar.kt      # DEĞİŞTİ — Test butonu eklendi
└── navigation/ReaderRoute.kt           # DEĞİŞTİ — onTestRequested: (Long) -> Unit callback'i

app/src/main/java/com/example/nativeminds/
└── NativeMindsNavHost.kt               # DEĞİŞTİ — quizScreen(...) rotası kablolanır

gradle/libs.versions.toml               # DEĞİŞTİ — generativeai versiyonu + alias
settings.gradle.kts                     # DEĞİŞTİ — include(":feature:quiz")
```

**Yapı Kararı**: Kullanıcı tercihiyle (`/speckit-plan` görüşmesi) quiz, Reader ekranı içinde bir
overlay değil, ayrı bir navigasyon rotası olarak yeni bir `:feature:quiz` modülünde uygulanır.
Bu, mevcut `:feature:reader` / `:app` navigasyon kalıbını (`ReaderRoute` → `onUnlockRequested`
benzeri bir `onTestRequested` callback'i, `:app`'in rotayı kablolaması) birebir izler ve
`:feature:*` modüllerinin birbirine bağımlı olmaması kuralını korur.

## Phase 1 Sonrası Anayasa Kontrolü

Phase 1 tasarımı (`data-model.md`, `contracts/gemini-quiz-contract.md`) tamamlandıktan sonra
gözden geçirildi: yukarıdaki tablodaki değerlendirme değişmedi. `QuizRepositoryImpl` tüm
network/parse hatalarını domain katmanına `Result` ile taşıyor, `GenerateQuizUseCase` bunları
`runCatching` + `errorReporter.report(...)` ile sarmalıyor (mevcut `RefreshLessonContentUseCase`
kalıbıyla birebir aynı) — İlke IV'ün "hata asla yutulmaz" gereği karşılanıyor. Yeni bir anayasa
ihlali yok; Karmaşıklık Takibi tablosu gerekmiyor.

## Karmaşıklık Takibi

*Doldurulmadı — Anayasa Kontrolü'nde gerekçelendirilmesi gereken bir ihlal yok.*
