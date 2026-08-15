# Quickstart: Hata Yönetimi ve Crashlytics Doğrulaması

Bu doküman, özelliğin uçtan uca çalıştığını manuel olarak doğrulamak içindir. Kod detayları için
[data-model.md](data-model.md), [contracts/error-reporter-contract.md](contracts/error-reporter-contract.md)
ve [research.md](research.md)'ye bakın.

## Ön koşullar

1. Gerçek bir Firebase projesi oluşturulmuş ve `com.example.nativeminds` uygulama kimliğiyle
   eşleştirilmiş `google-services.json` dosyası `app/` dizinine yerleştirilmiş olmalı (bkz.
   research.md #10 — bu adım bu planın kapsamı dışındadır, geliştirici tarafından yapılır).
2. Cihaz/emülatörde internet bağlantısı kontrol edilebilir olmalı (uçak modu açıp kapatabilme).

## 1. Build doğrulaması

```bash
./gradlew assembleDebug
```

Beklenen: Yeni `:core:crashreporting` modülü ve güncellenmiş Hilt binding'iyle birlikte hatasız
derlenir.

## 2. Gerçek çökme senaryosu (fatal)

1. Uygulamayı debug modda çalıştırın.
2. Geçici olarak (yalnızca doğrulama için, commit edilmez) herhangi bir ekranda bir butona
   `throw RuntimeException("Test crash")` ekleyip tetikleyin.
3. Uygulama kapanır. Uygulamayı tekrar açın (Crashlytics raporu genelde bir sonraki açılışta yükler).
4. Firebase konsolu → Crashlytics → Crashes sekmesinde, birkaç dakika içinde bu çökmenin göründüğünü
   doğrulayın.

**Beklenen sonuç**: SC-002 — çökme, ek kod yazılmadan otomatik olarak raporlanır.

## 3. Çevrimdışı çökme kuyruklama (FR-008)

1. Cihazı uçak moduna alın.
2. Adım 2'deki test çökmesini tekrar tetikleyin.
3. Uygulamayı yeniden açın (hâlâ çevrimdışı) — rapor gönderilmemiş olmalı.
4. Uçak modunu kapatın, uygulamayı tekrar açın.
5. Firebase konsolunda raporun bir süre sonra göründüğünü doğrulayın.

**Beklenen sonuç**: Rapor kaybolmaz, bağlantı geldiğinde iletilir.

## 4. Yakalanmış hata → non-fatal (FR-004, FR-010)

1. Cihazı uçak moduna alın.
2. Home ekranında aşağı çekip yenileyin (`PullToRefreshBox`) — senkronizasyon başarısız olmalı.
3. `HomeEffect.ShowSyncError` snackbar'ının anlaşılır bir mesajla göründüğünü doğrulayın (FR-001).
4. Reader ekranında bir dersi açıp `RetryRequested` akışını tetikleyin; aynı şekilde anlaşılır bir
   hata durumu ve yeniden dene seçeneği görün (FR-002).
5. Bağlantıyı geri açın, Firebase konsolu → Crashlytics → Non-fatals sekmesinde bu hataların
   `context` bilgisiyle (ör. `"SyncLessonsUseCase"`) birlikte göründüğünü doğrulayın.

**Beklenen sonuç**: SC-001, SC-003 — kullanıcı deneyimi bozulmaz, hata görünürlüğü kaybolmaz.

## 5. PII kontrolü (FR-007)

Firebase konsolunda 4. adımdaki non-fatal olayları açıp, kullanıcı kimliği alanının boş/anonim
olduğunu ve `context` metninde kullanıcıya özel hiçbir veri (e-posta, arama sorgusu, ad) olmadığını
doğrulayın.

## 6. Regresyon

```bash
./gradlew test
./gradlew lint
```

Beklenen: Mevcut `RecordingErrorReporter`/`NoOpErrorReporter`/`SilentErrorReporter` tabanlı testler
değişmeden geçer (arayüz ve çağıranlar değişmedi, sadece `@Binds` hedefi değişti).

## 7. Tema/önizleme kontrolü

Home ve Reader'daki hata/retry bileşenlerinin ilgili `@Preview`'larını hem açık hem koyu temada
görsel olarak kontrol edin — bu plan hiçbir tokenı değiştirmiyor, sadece mesaj metinlerinin tutarlı
olduğu doğrulanıyor.
