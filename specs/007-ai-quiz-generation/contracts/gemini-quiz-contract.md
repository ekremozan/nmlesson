# Sözleşme: Gemini Soru Üretimi

Bu özellik dışa açık bir HTTP/CLI API'si sunmaz (mobil uygulama); buradaki "sözleşme", uygulamanın
dış dünyaya (Gemini API) yaptığı isteğin ve `:core:domain`'in kendi içindeki repository
arayüzünün şeklidir.

## 1. Domain repository sözleşmesi — `core/domain/.../repository/QuizRepository.kt`

```kotlin
interface QuizRepository {
    suspend fun generateQuestion(storyTitle: String, storyBody: String): Result<QuizQuestion>
}
```

- Girdi: hikaye başlığı + tam gövde metni (düz metin, HTML/markdown yok).
- Çıktı: `Result.success(QuizQuestion)` veya `Result.failure(throwable)` — asla `throw` etmez,
  asla sessizce `null` dönmez.
- Çağıran (`GenerateQuizUseCase`) hata durumunda `ErrorReporter.report(throwable, context)` çağırır
  ve kullanıcıya `QuizGenerationResult.Failed` olarak yansıtır.

## 2. Use case sözleşmesi — `core/domain/.../usecase/GenerateQuizUseCase.kt`

```kotlin
class GenerateQuizUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val entitlementRepository: EntitlementRepository,
    private val quizRepository: QuizRepository,
    private val errorReporter: ErrorReporter,
) {
    suspend operator fun invoke(lessonId: Long): QuizGenerationResult
}
```

Akış:
1. `entitlementRepository.isPremium()` ilk değeri `false` ise → `QuizGenerationResult.Locked`
   döner, `quizRepository` hiç çağrılmaz.
2. `lessonRepository`'den hikaye başlığı + gövdesi okunur (yerel Room'dan; zaten indirilmiş
   olmalı, çünkü kullanıcı hâlihazırda o hikayeyi okuyor).
3. `quizRepository.generateQuestion(title, body)` çağrılır.
4. Başarılıysa `QuizGenerationResult.Success(question)`; başarısızsa hata raporlanır ve
   `QuizGenerationResult.Failed(throwable)` döner.

## 3. Gemini API isteği (istemciden doğrudan)

**DÜZELTME (gerçek cihaz testinden sonra)**: `GenerativeModel` (Gemini Android SDK) planı terk
edildi — SDK, Ktor 2.x'e göre derlenmiş ve bu projenin Ktor 3.3.1'iyle çalışma zamanında
çakışıyor (`ClassNotFoundException: io.ktor.client.plugins.HttpTimeout`; bkz.
[research.md](research.md) R1). Bunun yerine düz bir Ktor `HttpClient` ile
`POST https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key={apiKey}`
çağrılıyor. Model adı da `gemini-2.5-flash` → `gemini-flash-latest` olarak düzeltildi
(eski model yeni API anahtarlarına kapatılmış — bkz. research.md R2).

**Request body** (`GeminiGenerateContentRequestDto`):
```json
{
  "contents": [{"parts": [{"text": "<prompt>"}]}],
  "generationConfig": {
    "responseMimeType": "application/json",
    "responseSchema": {
      "type": "OBJECT",
      "properties": {
        "question": {"type": "STRING"},
        "options": {"type": "ARRAY", "items": {"type": "STRING"}},
        "correctOptionIndex": {"type": "INTEGER"},
        "explanation": {"type": "STRING"}
      },
      "required": ["question", "options", "correctOptionIndex", "explanation"]
    }
  }
}
```

**İstek içeriği** (tek kullanıcı mesajı, bkz. [research.md](research.md) R7):

```
Hikaye: "{storyTitle}"

{storyBody}

---
Bu hikayeye dayalı, tam olarak 4 şıklı ve tek doğru cevaplı bir okuma anlama sorusu üret.
Doğru cevabın kısa (1-2 cümlelik) bir açıklamasını da ver. Yanıtı yalnızca verilen şemaya
uygun JSON olarak döndür. Soru ve şıkları Türkçe yaz.
```

**Yanıt zarfı**: Gerçek REST yanıtı `{"candidates": [{"content": {"parts": [{"text": "<json string>"}]}}]}`
şeklinde gelir (`GeminiGenerateContentResponseDto`); asıl quiz JSON'ı bu `text` alanının içinde
bir string olarak taşınır ve ayrıca `GeminiQuizPayloadDto.serializer()` ile ayrıştırılır.

**O iç JSON'ın beklenen şekli** (`GeminiQuizPayloadDto` ile eşleşir):

```json
{
  "question": "Bekçinin el yazısı hangi olayla birlikte değişti?",
  "options": [
    "Fenerin elektriğe geçirilmesiyle",
    "Işığın dönüştürülüp saat mekanizmasının indirilmesiyle",
    "Yeni bir bekçinin göreve başlamasıyla",
    "Fırtınada fenerin hasar görmesiyle"
  ],
  "correctOptionIndex": 1,
  "explanation": "Metne göre el yazısı iki kez değişti; ilki 1931'de ışığın dönüştürülüp saat mekanizmasının parça parça aşağı indirildiği yıldı."
}
```

**Hata durumları** (`GeminiRemoteQuizDataSource` içinde `Result.failure` olarak yansıtılır):
- Ağ hatası (`IOException` ailesi) → olduğu gibi sarmalanır.
- Gemini API hatası (kota, geçersiz anahtar, güvenlik filtresi engeli) → SDK'nın attığı istisna
  sarmalanır.
- JSON şemaya uysa bile `options.size != 4` veya `correctOptionIndex !in 0..3` veya boş
  `question`/`explanation` → mapper `IllegalStateException` fırlatır, `Result.failure` olarak
  yansır (bkz. [data-model.md](data-model.md) doğrulama kuralları).

## 4. UI intent sözleşmesi — `feature/quiz/.../ui/QuizContract.kt`

```kotlin
sealed interface QuizIntent {
    data class LessonIdChanged(val lessonId: Long) : QuizIntent
    data class QuestionLoaded(val result: QuizGenerationResult) : QuizIntent
    data class OptionSelected(val optionId: String) : QuizIntent
    data object RetryRequested : QuizIntent
}
```

- `LessonIdChanged` ekran ilk açıldığında (nav argümanından) veya `RetryRequested` sonrası
  `retryToken` arttığında `distinctUntilChanged()` ile tetiklenir, `GenerateQuizUseCase`'i
  çağırır, sonucu `QuestionLoaded` intent'i olarak reducer'a geri besler (mevcut Reader
  deseniyle birebir aynı: "async work is triggered by state, not an intent handler").
- `OptionSelected`, yalnızca `Ready(revealed=false)` durumundayken state'i değiştirir.
