# Veri Modeli: AI ile Üretilmiş Çoktan Seçmeli Test

Üç katmanlı model ayrımı (anayasa İlke II) korunur: DTO (Gemini yanıtı) → domain → UI. Kalıcılık
katmanı yoktur (bkz. [research.md](research.md) R4) — bu yüzden bir Room entity/DAO tanımlanmaz.

## DTO katmanı — `core/data/.../remote/quiz/dto/GeminiQuizPayloadDto.kt`

Gemini'nin `responseSchema`'ya uyan JSON yanıtının kotlinx.serialization karşılığı:

```kotlin
@Serializable
data class GeminiQuizPayloadDto(
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
)
```

**Doğrulama kuralları** (mapper'da uygulanır, ayrı bir validasyon kütüphanesi eklenmez):
- `options.size == 4` değilse mapper başarısız olur (`IllegalStateException` → repository'de
  `Result.failure`).
- `correctOptionIndex` `0..3` aralığında değilse aynı şekilde başarısız olur.
- `question` veya `explanation` boşsa aynı şekilde başarısız olur.

## Domain katmanı — `core/domain/.../model/QuizQuestion.kt`

```kotlin
data class QuizQuestion(
    val questionText: String,
    val storyTitle: String,
    val options: List<QuizOption>,
    val correctOptionId: String,
    val explanation: String,
)

data class QuizOption(
    val id: String,
    val text: String,
)
```

- `options` her zaman 4 eleman içerir; `id` değerleri `"A"`, `"B"`, `"C"`, `"D"` (tasarımdaki
  şık rozetleriyle birebir).
- `correctOptionId`, `options` listesindeki bir `id`'ye karşılık gelmelidir (mapper garanti eder).
- `GeminiQuizPayloadDto.toDomain(storyTitle: String): QuizQuestion` extension function'ı
  dönüşümü yapar; `correctOptionIndex` → harf `id`'ye çevrilir.

### Kullanım sonucu — `core/domain/.../usecase/GenerateQuizUseCase.kt`

```kotlin
sealed interface QuizGenerationResult {
    data class Success(val question: QuizQuestion) : QuizGenerationResult
    data object Locked : QuizGenerationResult
    data class Failed(val throwable: Throwable) : QuizGenerationResult
}
```

Anayasa İlke IV'ün gating gereğiyle uyumlu: kilitli durum, içeriği (soruyu) yapısal olarak
elinde tutamaz — `Locked` hiçbir `QuizQuestion` taşımaz.

## UI katmanı — `feature/quiz/.../ui/QuizContract.kt`

```kotlin
data class QuizQuestionUiModel(
    val questionText: String,
    val storyTitle: String,
    val options: List<QuizOptionUiModel>,
    val explanation: String,
)

data class QuizOptionUiModel(
    val id: String,
    val letter: String,
    val text: String,
    val visualState: QuizOptionVisualState,
)

enum class QuizOptionVisualState { UNSELECTED, SELECTED, CORRECT_REVEALED, INCORRECT_REVEALED }
```

`QuizQuestion.toUiModel(selectedOptionId: String?, revealed: Boolean): QuizQuestionUiModel`
extension function'ı, tasarımdaki üç görsel duruma (10a cevaplanmamış, 10b doğru, 10c yanlış)
karşılık gelen `visualState` değerlerini hesaplar.

## Durum makinesi (ekran içi)

```
Loading ──(Gemini yanıtı)──▶ Ready(question, selectedOptionId=null, revealed=false)
Loading ──(hata)───────────▶ Error(message)
Loading ──(kilitli)────────▶ Locked
Ready(revealed=false) ──(OptionSelected)──▶ Ready(revealed=true, selectedOptionId=seçilen)
Error ──(RetryRequested)───▶ Loading
```

`Ready(revealed=true)` durumuna ulaşıldıktan sonra başka bir `OptionSelected` intent'i kabul
edilmez (FR kabul senaryosu: kullanıcı cevabını değiştiremez) — reducer bu durumda state'i
değiştirmeden döner.
