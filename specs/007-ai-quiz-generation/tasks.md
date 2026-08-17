# Görevler: AI ile Üretilmiş Çoktan Seçmeli Test (Gemini)

**Girdi**: `specs/007-ai-quiz-generation/` altındaki tasarım dokümanları (`plan.md`, `spec.md`,
`research.md`, `data-model.md`, `contracts/gemini-quiz-contract.md`, `quickstart.md`)

**Ön koşullar**: plan.md (zorunlu), spec.md (zorunlu), research.md, data-model.md, contracts/

**Testler**: Anayasa'nın "Development Workflow & Quality Gates" bölümü, iş kuralı taşıyan
domain/repository mantığı için birim testini ZORUNLU kılıyor (gating, mapper doğrulama, reducer).
Bu yüzden aşağıda test görevleri dahildir — atlanmaz.

**Organizasyon**: Görevler spec.md'deki kullanıcı hikayelerine göre gruplanmıştır (US1, US2).

## Format: `[ID] [P?] [Story] Açıklama`

- **[P]**: Paralel çalıştırılabilir (farklı dosyalar, birbirine bağımlı değil)
- **[Story]**: Görevin ait olduğu kullanıcı hikayesi (US1, US2)

---

## Gemini API Kurulumu — Senin Sağlaman Gerekenler

Bu bölümdeki adımlar bir LLM/geliştirici tarafından otomatik yapılamaz; sen (proje sahibi)
yapman gerekir. Aşağıdaki görev listesindeki **T003** bu adımlara bağımlıdır.

1. **API anahtarı al**: [Google AI Studio](https://aistudio.google.com/apikey)'dan ücretsiz bir
   Gemini API anahtarı oluştur (Google hesabınla giriş yeterli, ayrı bir Cloud projesi/kredi kartı
   gerekmez — ücretsiz kota yeterlidir).
2. **Anahtarı yerel olarak ekle**: Repo kökündeki `local.properties` dosyasına (bu dosya
   `.gitignore`'da, commit'lenmez) şu satırı ekle:
   ```
   GEMINI_API_KEY=AIza...senin_anahtarın...
   ```
3. **Model erişimini teyit et**: Plan `gemini-2.5-flash` modelini kullanıyor (research.md R2);
   yeni oluşturulan API anahtarları bu modele varsayılan olarak erişebilir, ekstra bir izin
   istemene gerek yok.
4. **Ücretsiz kota sınırlarını bil**: Geliştirme/demo sırasında (case study kapsamı) ücretsiz
   kota fazlasıyla yeterlidir; production'a çıkarsan kota/faturalandırma ayarlarını AI Studio
   üzerinden gözden geçirmen gerekir (bu README "Cut Corners / Assumptions"a not düşülecek — T032).
5. **CI/paylaşılan build ortamı** (varsa): Eğer bir CI sistemi debug APK üretecekse,
   `GEMINI_API_KEY`'i CI'ın secret/environment değişkeni mekanizmasıyla enjekte etmen gerekir —
   case study kapsamında yalnızca yerel geliştirme yeterli olduğundan bu adım opsiyoneldir.

Sen bu adımları tamamlamadan T003 sonrası hiçbir görev (Gemini'ye gerçek istek atan hiçbir kod)
cihazında çalışmaz; derleme/build yine de başarılı olur (anahtar boşsa `GenerativeModel` yalnızca
çağrı anında hata verir, bu da T008/US1 test senaryolarıyla yakalanır).

---

## Phase 1: Kurulum (Paylaşılan Altyapı)

**Amaç**: Yeni modül ve bağımlılıkların iskeletini kurmak

- [X] T001 `gradle/libs.versions.toml`'a Gemini Android SDK için versiyon + alias ekle
  (`com.google.ai.client.generativeai:generativeai`) — bkz. research.md R1
- [X] T002 [P] `:feature:quiz` modülünü `settings.gradle.kts`'e ekle ve
  `feature/quiz/build.gradle.kts`'i oluştur (bağımlılıklar: `:core:domain`,
  `:core:designsystem`, Compose, Hilt — `:feature:reader`'ın `build.gradle.kts`'iyle aynı şablon)
- [X] T003 `core/data/build.gradle.kts`'e `GEMINI_API_KEY` için `buildConfigField` ekle
  (Supabase anahtarlarıyla birebir aynı `local.properties` okuma deseni — bkz. research.md R3).
  **Bağımlı**: yukarıdaki "Gemini API Kurulumu" adım 1-2'nin senin tarafından tamamlanmış olması

---

## Phase 2: Temel Altyapı (Engelleyici Ön Koşullar)

**Amaç**: Her iki kullanıcı hikayesinin de üzerine kurulacağı ortak domain/data katmanı

**⚠️ KRİTİK**: Bu faz tamamlanmadan hiçbir kullanıcı hikayesi görevi başlayamaz

- [X] T004 [P] Domain modelleri `QuizQuestion`/`QuizOption`'ı
  `core/domain/src/main/kotlin/com/example/nativeminds/domain/model/QuizQuestion.kt`'a ekle
  (bkz. data-model.md)
- [X] T005 [P] `QuizRepository` arayüzünü
  `core/domain/src/main/kotlin/com/example/nativeminds/domain/repository/QuizRepository.kt`'a
  ekle (bkz. contracts/gemini-quiz-contract.md §1)
- [X] T006 [P] `QuizGenerationResult` sealed interface'ini (`Success`/`Locked`/`Failed`)
  `core/domain/src/main/kotlin/com/example/nativeminds/domain/usecase/GenerateQuizUseCase.kt`
  içinde tanımla (T012'de aynı dosyaya use case eklenecek)
- [X] T007 `GeminiQuizPayloadDto` + `toDomain(storyTitle): QuizQuestion` mapper'ını
  `core/data/src/main/java/com/example/nativeminds/data/remote/quiz/dto/GeminiQuizPayloadDto.kt`'a
  ekle; doğrulama kurallarını uygula (options.size==4, correctOptionIndex 0..3, boş alan yok —
  bkz. data-model.md) (bağımlı: T004)
- [X] T008 `GeminiQuizDataSource` arayüzü + `GeminiRemoteQuizDataSource` impl'ini
  `core/data/src/main/java/com/example/nativeminds/data/remote/quiz/` altına ekle
  (`GenerativeModel` çağrısı, `responseMimeType=application/json` + `responseSchema`, prompt
  research.md R7'deki metin) (bağımlı: T003, T007)
- [X] T009 `QuizRepositoryImpl`'i
  `core/data/src/main/java/com/example/nativeminds/data/repository/QuizRepositoryImpl.kt`'a
  ekle (bağımlı: T005, T008)
- [X] T010 `core/data/.../di/NetworkModule.kt`'a `GenerativeModel` için `@Provides` ekle
  (`BuildConfig.GEMINI_API_KEY`, model adı `gemini-2.5-flash`) (bağımlı: T003)
- [X] T011 `core/data/.../di/RepositoryModule.kt`'a `QuizRepository → QuizRepositoryImpl`
  için `@Binds` ekle (bağımlı: T009)
- [X] T012 `GenerateQuizUseCase`'i
  `core/domain/src/main/kotlin/com/example/nativeminds/domain/usecase/GenerateQuizUseCase.kt`'a
  ekle (`LessonRepository` + `EntitlementRepository` + `QuizRepository` + `ErrorReporter`
  orkestrasyonu — bkz. contracts/gemini-quiz-contract.md §2) (bağımlı: T004, T005, T006)
- [X] T013 [P] `AnalyticsEvent`'e `QuizRequested`/`QuizAnswered` durumlarını ekle ve
  `core/analytics/.../FirebaseAnalyticsReporter.kt`'taki `toFirebaseEvent()`'e karşılık gelen
  dalları ekle; `AiFeatureUsed(lessonId, featureName="quiz")` olayının tetiklendiği yeri not al
  (bkz. research.md R6)
- [X] T014 [P] `QuizRoute` iskeletini (`@Serializable data class QuizRoute(val lessonId: Long)`
  + boş `NavGraphBuilder.quizScreen(...)` gövdesi)
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/navigation/QuizRoute.kt`'a
  ekle

**Kontrol noktası**: Temel altyapı hazır — kullanıcı hikayesi görevleri şimdi başlayabilir

---

## Phase 3: Kullanıcı Hikayesi 1 - Hikaye içeriğinden anlık soru üretme ve cevaplama (Öncelik: P1) 🎯 MVP

**Hedef**: Premium kullanıcı "Test"e basar, Gemini'den anlık 1 soru + 4 şık üretilir, şık
seçince doğru/yanlış görsel hâli ve kısa açıklama gösterilir, "Okumaya dön" ile Reader'a döner.

**Bağımsız Test**: Premium hesapla bir hikaye açılıp "Test"e basılır; hem doğru hem yanlış şık
seçilerek iki ayrı denemede doğru görsel hâlin ve açıklamanın göründüğü, "Okumaya dön"ün Reader'a
döndürdüğü doğrulanır (quickstart.md Senaryo 1).

### Testler for Kullanıcı Hikayesi 1

> **NOT: Bu testleri önce yaz, implementasyondan önce FAIL ettiklerini doğrula**

- [X] T015 [P] [US1] `GenerateQuizUseCase` başarılı yol testi (premium=true, Gemini başarılı
  yanıt → `Success`)
  `core/domain/src/test/kotlin/com/example/nativeminds/domain/usecase/GenerateQuizUseCaseTest.kt`
- [X] T016 [P] [US1] `GeminiQuizPayloadDto.toDomain()` mapper testi — geçerli payload +
  `options.size != 4` + `correctOptionIndex` aralık dışı + boş `question`/`explanation` (4 durum)
  `core/data/src/test/java/com/example/nativeminds/data/remote/quiz/dto/GeminiQuizPayloadDtoMapperTest.kt`
- [X] T017 [P] [US1] `QuizReducer` durum geçişi testi — `Loading→Ready`,
  `Ready→Ready(revealed=true)`, `revealed` sonrası `OptionSelected`'ın yok sayılması,
  `Error→Loading` (retry)
  `feature/quiz/src/test/java/com/example/nativeminds/feature/quiz/ui/QuizReducerTest.kt`

### Implementasyon for Kullanıcı Hikayesi 1

- [X] T018 [P] [US1] `QuizContract.kt`'ı (state/intent/effect, `QuizQuestionUiModel`,
  `QuizOptionUiModel`, `QuizOptionVisualState`) oluştur —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/ui/QuizContract.kt`
  (bkz. data-model.md, contracts/gemini-quiz-contract.md §4)
- [X] T019 [US1] `QuizReducer.kt`'ı (saf top-level extension function) oluştur —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/ui/QuizReducer.kt`
  (bağımlı: T018)
- [X] T020 [P] [US1] `QuizMappers.kt`'ı (`QuizQuestion.toUiModel(...)`) oluştur —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/ui/mapper/QuizMappers.kt`
  (bağımlı: T018)
- [X] T021 [US1] `QuizViewModel.kt`'ı (Hilt, tek `onIntent()`, `GenerateQuizUseCase` +
  `AnalyticsReporter` enjeksiyonu, state anahtarından (`lessonId`/`retryToken`) tetiklenen
  `flatMapLatest` yükleme) oluştur —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/ui/QuizViewModel.kt`
  (bağımlı: T012, T019)
- [X] T022 [US1] `QuizScreen.kt`'ı (stateful `QuizScreen` + stateless `QuizScreenContent`;
  tasarımdaki 10a/10b/10c görsel durumları; `@ScreenThemePreviews`) oluştur —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/ui/QuizScreen.kt`
  (bağımlı: T020)
- [X] T023 [P] [US1] `QuizPreviewData.kt`'ı (sabit veri + `PreviewParameterProvider`, ViewModel/DB
  bağımlılığı yok) oluştur —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/ui/preview/QuizPreviewData.kt`
- [X] T024 [US1] `QuizRoute.kt`'taki `quizScreen(...)` gövdesini `hiltViewModel()` ve
  `QuizScreen` ile doldur (`onBack` callback'i "Okumaya dön" eylemine bağlanır) —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/navigation/QuizRoute.kt`
  (bağımlı: T021, T022, T014)
- [X] T025 [US1] `ReaderTopBar.kt`'a "Test" butonunu ekle (yalnızca `ReaderAccess.Full` iken
  görünür) ve `onTestRequested: (Long) -> Unit` callback'ini `ReaderScreen.kt` →
  `ReaderRoute.kt` → `readerScreen(...)` imzasına kadar ilet —
  `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/ReaderTopBar.kt`,
  `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/ReaderScreen.kt`,
  `feature/reader/src/main/java/com/example/nativeminds/feature/reader/navigation/ReaderRoute.kt`
- [X] T026 [US1] `app/src/main/java/com/example/nativeminds/NativeMindsNavHost.kt`'a
  `quizScreen(onBack = { navController.popBackStack() })` rotasını ekle ve
  `readerScreen(..., onTestRequested = { lessonId -> navController.navigate(QuizRoute(lessonId)) })`
  şeklinde kabla (bağımlı: T024, T025)

**Kontrol noktası**: Kullanıcı Hikayesi 1 uçtan uca çalışır ve bağımsız test edilebilir
(quickstart.md Senaryo 1 ve 3)

---

## Phase 4: Kullanıcı Hikayesi 2 - Premium olmayan kullanıcı için erişim engeli (Öncelik: P2)

**Hedef**: Premium olmayan kullanıcı "Test"e hiçbir şekilde erişemez ve Gemini'ye asla istek
atılmaz; olası bir doğrudan erişimde bile `GenerateQuizUseCase` kilitli sonucu döner.

**Bağımsız Test**: Premium olmayan bir hesapla hikaye açılır; Reader'da "Test" butonu görünmez.
`QuizRoute`'a doğrudan ulaşılsa bile ekran `Locked` durumuna düşer ve ağ isteği atılmadığı
doğrulanır (quickstart.md Senaryo 2).

### Testler for Kullanıcı Hikayesi 2

- [X] T027 [US2] `GenerateQuizUseCase` kilitli yol testi — genişletir: T015'in oluşturduğu
  `GenerateQuizUseCaseTest.kt`'e `premium=false` durumunda `QuizRepository`'nin hiç
  çağrılmadığını ve sonucun `Locked` olduğunu doğrulayan test ekle (bağımlı: T015 — aynı dosya,
  paralel değil)

### Implementasyon for Kullanıcı Hikayesi 2

- [X] T028 [US2] `QuizViewModel`'in `Locked` sonucunu `QuizUiState.Locked`'a çevirdiğini ve
  paywall'a yönlendiren bir `QuizEffect`i tetiklediğini doğrula/tamamla —
  `feature/quiz/src/main/java/com/example/nativeminds/feature/quiz/ui/QuizViewModel.kt`
  (bağımlı: T021, T012)
- [X] T029 [US2] `ReaderTopBar` önizlemelerinde `ReaderAccess.Preview` durumunda "Test"
  butonunun render edilmediğini doğrulayan bir Compose önizleme/durum ekle —
  `feature/reader/src/main/java/com/example/nativeminds/feature/reader/ui/components/ReaderTopBar.kt`
  (bağımlı: T025)
- [X] T030 [P] [US2] `QuizViewModel` için sahte (fake) `GenerateQuizUseCase` ile `Locked`
  sonucunda hiçbir `QuizRepository`/ağ çağrısının yapılmadığını doğrulayan birim testi ekle —
  `feature/quiz/src/test/java/com/example/nativeminds/feature/quiz/ui/QuizViewModelTest.kt`

**Kontrol noktası**: Kullanıcı Hikayesi 1 VE 2 birbirinden bağımsız olarak çalışır

---

## Phase 5: Cilalama & Çapraz Kesişen Konular

**Amaç**: Anayasa'nın "Defensible Decisions" ve dokümantasyon gereklerini kapatmak

- [X] T031 [P] README "Key Decisions"a Gemini SDK seçimi, yapılandırılmış çıktı (responseSchema),
  gating yaklaşımı ve kalıcılık-yok kararını research.md'den özetleyerek ekle
- [X] T032 [P] README "Cut Corners / Assumptions"a şunları ekle: API anahtarı APK içinde
  BuildConfig sabiti olarak taşınıyor (production'da proxy arkasına alınmalı), quiz sorusu
  kalıcı olarak saklanmıyor, tek soru üretiliyor (çok soruluk quiz yok)
- [X] T033 [P] README "How I Worked With AI"a bu özelliğin planlama sürecini not al: ilk
  spec taslağının "5 sorulu quiz" varsayımının tasarım referansıyla (10a/10b/10c, "Soru 1/1")
  çeliştiğinin fark edilip tek-sorulu anlık akışa düzeltildiği
- [X] T034 quickstart.md'deki 3 senaryoyu (mutlu yol, gating, hata/retry) gerçek cihaz/emülatörde
  manuel olarak çalıştır ve doğrula — 2026-08-16, Pixel 10 (AVD), gerçek `GEMINI_API_KEY` ile
- [X] T035 `./gradlew test` ve `./gradlew lint`'i tüm etkilenen modüller için çalıştır, ikisinin
  de geçtiğini doğrula

---

## Düzeltme Notu (2026-08-16, gerçek cihaz testinden sonra)

Bu bölümdeki görevler (T001, T003, T008, T010) plan aşamasında `com.google.ai.client.generativeai`
SDK'sını ve `gemini-2.5-flash` modelini referans alıyordu. Gerçek cihazda "Test" ilk denendiğinde
uygulama çöktü (`ClassNotFoundException: io.ktor.client.plugins.HttpTimeout` — SDK Ktor 2.x'e göre
derlenmiş, proje Ktor 3.3.1 kullanıyor) ve düzeltilip yeniden denendiğinde bu kez Gemini
`gemini-2.5-flash`'ın yeni API anahtarlarına kapatıldığını bildirdi (`404 NOT_FOUND`). Her ikisi de
düzeltildi:
- SDK yerine düz Ktor `HttpClient` ile REST çağrısı (`GeminiRemoteQuizDataSource`,
  `GeminiGenerateContentDto` ailesi)
- Model adı `gemini-flash-latest`'e güncellendi

Tam gerekçe için [research.md](research.md) R1/R2'ye bakınız. Görev metinleri tarihsel kayıt
olarak olduğu gibi bırakıldı; gerçek implementasyon bu nottaki hâldir.

---

## Bağımlılıklar & Yürütme Sırası

### Faz Bağımlılıkları

- **Kurulum (Faz 1)**: Bağımsız — hemen başlar (T003 hariç, senin API anahtarı adımına bağımlı)
- **Temel Altyapı (Faz 2)**: Kurulum'un tamamlanmasına bağımlı — TÜM kullanıcı hikayelerini
  ENGELLER
- **Kullanıcı Hikayeleri (Faz 3+)**: Hepsi Temel Altyapı'nın tamamlanmasına bağımlı
  - US1 ve US2 paralel ilerleyebilir, ama US2'nin testi (T027) US1'in test dosyasını (T015)
    genişlettiği için US1'den sonra yapılması pratik açıdan daha kolaydır
- **Cilalama (Son Faz)**: İstenen tüm kullanıcı hikayelerinin tamamlanmasına bağımlı

### Kullanıcı Hikayesi Bağımlılıkları

- **US1 (P1)**: Temel Altyapı sonrası başlayabilir — başka hikayeye bağımlı değil
- **US2 (P2)**: Temel Altyapı sonrası başlayabilir; T027 pratikte T015'in üzerine yazıldığı için
  US1'in test dosyasının var olmasını gerektirir, ama US1'in UI implementasyonunun bitmiş
  olmasını gerektirmez

### Her Kullanıcı Hikayesi İçinde

- Testler implementasyondan önce yazılır ve FAIL ettiği doğrulanır
- Domain modelleri/sözleşmeler → repository/use case → ViewModel/reducer → Screen → navigasyon
  kablolaması sırası izlenir

### Paralel Fırsatlar

- Faz 1: T002 diğerlerinden bağımsız paralel çalışabilir
- Faz 2: T004, T005, T006, T013, T014 paralel çalışabilir; T007→T008→T009→T011 sıralı bir zincir
- Faz 3: T015, T016, T017 paralel; T018, T020, T023 paralel
- Faz 4: T030 paralel; T027 (T015'i genişlettiği için) ve T028/T029 sıralı

---

## Paralel Örnek: Kullanıcı Hikayesi 1

```bash
# US1 testlerini birlikte başlat:
Task: "GenerateQuizUseCase başarılı yol testi — core/domain/src/test/.../GenerateQuizUseCaseTest.kt"
Task: "GeminiQuizPayloadDto.toDomain() mapper testi — core/data/src/test/.../GeminiQuizPayloadDtoMapperTest.kt"
Task: "QuizReducer durum geçişi testi — feature/quiz/src/test/.../QuizReducerTest.kt"

# US1 model/kontrat dosyalarını birlikte başlat:
Task: "QuizContract.kt — feature/quiz/.../ui/QuizContract.kt"
Task: "QuizMappers.kt — feature/quiz/.../ui/mapper/QuizMappers.kt"
Task: "QuizPreviewData.kt — feature/quiz/.../ui/preview/QuizPreviewData.kt"
```

---

## Uygulama Stratejisi

### Önce MVP (Yalnızca Kullanıcı Hikayesi 1)

1. Faz 1: Kurulum'u tamamla (senin Gemini API anahtarı adımların dahil)
2. Faz 2: Temel Altyapı'yı tamamla (KRİTİK — tüm hikayeleri engeller)
3. Faz 3: Kullanıcı Hikayesi 1'i tamamla
4. **DUR ve DOĞRULA**: quickstart.md Senaryo 1 ve 3'ü bağımsız olarak test et
5. Hazırsa demo/teslim et

### Artımlı Teslimat

1. Kurulum + Temel Altyapı → Temel hazır
2. US1 ekle → bağımsız test et → MVP demo
3. US2 ekle → bağımsız test et → tam gating demo
4. Cilalama fazını tamamla → README ve doğrulama güncel

---

## Notlar

- [P] görevler = farklı dosyalar, birbirine bağımlı değil
- [Story] etiketi görevi ilgili kullanıcı hikayesine izlenebilir kılar
- Her kullanıcı hikayesi bağımsız olarak tamamlanabilir ve test edilebilir olmalı
- İmplementasyondan önce testlerin FAIL ettiğini doğrula
- Her görevden veya mantıksal gruptan sonra commit at
- Bağımsızlığı doğrulamak için her kontrol noktasında dur
