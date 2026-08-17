# Hızlı Başlangıç: AI ile Üretilmiş Çoktan Seçmeli Test

Bu doküman, özellik uygulandıktan sonra uçtan uca doğrulama için izlenecek adımları anlatır.
Uygulama detayları için [data-model.md](data-model.md) ve
[contracts/gemini-quiz-contract.md](contracts/gemini-quiz-contract.md)'a bakınız.

## Ön koşullar

1. `local.properties` dosyasına bir Gemini API anahtarı eklenmiş olmalı:
   ```
   GEMINI_API_KEY=AIza...
   ```
   (Anahtar [Google AI Studio](https://aistudio.google.com/apikey)'dan alınır; repo'ya asla
   commit edilmez — bkz. [research.md](research.md) R3.)
2. Cihaz/emülatörde aktif bir internet bağlantısı olmalı.
3. Uygulamada premium (mock abonelik) aktif bir hesapla giriş yapılmış olmalı (`EntitlementRepository`
   mevcut mock akışıyla).

## Senaryo 1 — Mutlu yol (P1)

```bash
./gradlew installDebug
```

1. Uygulamayı aç, herhangi bir hikayeyi aç (Reader ekranı).
2. Üst çubuktaki **Test** butonuna dokun.
3. **Beklenen**: 10 saniye içinde yeni bir ekran açılır; "AI ile hazırlandı" rozeti, "Soru 1 / 1"
   etiketi, bir soru metni ve 4 şık görünür.
4. Doğru olduğunu düşündüğün şıkka dokun.
5. **Beklenen** (doğruysa): şık yeşil/"Doğru" vurgusuyla işaretlenir, altında kısa bir açıklama ve
   "Okumaya dön" butonu belirir.
6. **Beklenen** (yanlışsa): seçtiğin şık "yanlış" vurgusuyla, doğru şık ayrıca işaretli biçimde
   gösterilir; aynı açıklama ve "Okumaya dön" butonu belirir.
7. "Okumaya dön"a dokun → Reader ekranına, kaldığın yerden dönersin.

## Senaryo 2 — Premium olmayan kullanıcı (P2)

1. Mock abonelik durumunu premium-değil yap.
2. Bir hikayeyi aç.
3. **Beklenen**: Free preview ekranında (paywall kartı) zaten "Test" butonu görünmez; kart
   üzerinden premium'a yönlendirme akışı çalışır.
4. (Savunma kontrolü) Eğer `QuizRoute`'a doğrudan bir deep link/nav ile ulaşılırsa: **Beklenen**
   ekran anında `Locked` durumuna düşer, Gemini'ye hiçbir istek atılmaz (network log'da istek
   görülmemeli) ve kullanıcı paywall'a yönlendirilir.

## Senaryo 3 — Hata ve yeniden deneme

1. Cihazda uçak modunu aç.
2. Bir hikayede **Test**'e dokun.
3. **Beklenen**: 10 saniye içinde anlaşılır bir hata mesajı ve "Yeniden dene" eylemi görünür;
   uygulama çökmez, hata Crashlytics'e raporlanır (debug ortamında Logcat'te
   `FirebaseCrashlyticsErrorReporter` log'u görülebilir).
4. Uçak modunu kapat, "Yeniden dene"ye dokun.
5. **Beklenen**: Yeni bir Gemini isteği atılır ve soru normal şekilde yüklenir.

## Birim testleri

```bash
./gradlew :core:domain:test --tests "*GenerateQuizUseCase*"
./gradlew :feature:quiz:test --tests "*QuizReducer*"
./gradlew test
./gradlew lint
```

Beklenen kapsam (bkz. [tasks.md](tasks.md) — `/speckit-tasks` ile üretilecek):
- `GenerateQuizUseCase`: kilitli/başarılı/hatalı üç dal.
- `QuizReducer`: `Loading→Ready`, `Ready→Ready(revealed)`, `revealed` sonrası `OptionSelected`'ın
  yok sayılması, `Error→Loading` (retry).
- `GeminiQuizPayloadDto.toDomain()` mapper: geçerli payload, `options.size != 4`,
  `correctOptionIndex` aralık dışı, boş `question`/`explanation` — dört durumun da doğru
  şekilde başarısız/başarılı olduğu.
