# Hızlı Başlangıç: Firebase Analytics Doğrulaması

Bu, özelliğin uçtan uca çalıştığını kanıtlayan çalıştırılabilir doğrulama senaryolarını içerir.
Olay/parametre ayrıntıları için [contracts/analytics-events.md](contracts/analytics-events.md) ve
[data-model.md](data-model.md)'ye bakın.

## Ön koşullar

- `google-services.json` projede tanımlı (mevcut Crashlytics kurulumu zaten Firebase projesine
  bağlı olduğundan ek bir konsol adımı gerekmez — Analytics aynı Firebase projesinin bir parçası).
- Fiziksel cihaz veya emülatör, Google Play Hizmetleri kurulu.
- Android Studio > Logcat, filtre: `FA` (Firebase Analytics'in kendi debug etiketleri) veya
  `adb shell setprop debug.firebase.analytics.app com.example.nativeminds` ile debug modu açılmış.

## Kurulum

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:installDebug
adb shell setprop debug.firebase.analytics.app com.example.nativeminds
adb logcat -s FA FA-SVC
```

## Senaryo 1 — Ekran akışı (FR-001, FR-002)

1. Uygulamayı aç → Logcat'te `screen_view` (`screen_name=home`) görünmeli.
2. Bir ders kartına dokun → `screen_view` (`screen_name=reader`, `previous_screen_name=home`).
3. Sistem geri tuşuna bas → `screen_view` (`screen_name=home`, `source=back`).

**Beklenen sonuç**: Her gezinme adımı ayrı bir `screen_view` olayı üretir, önceki ekran alanı
doğru doldurulur.

## Senaryo 2 — Ders seçimi ve dinleme (FR-003 – FR-006)

1. Ana ekranda bir derse dokun → `lesson_selected` (`lesson_id`, `lesson_title`, `list_index`).
2. Okuma ekranı açılınca → `content_viewed` (`lesson_id`, `access_level`).
3. "Dinle" butonuna dokun → `listen_started` (`lesson_id`).
4. Dinlerken duraklat → `listen_stopped` (`reason=paused`, `progress_percent=<0-100>`).
5. Ekrandan geri çık (dinleme sürerken) → `listen_stopped` (`reason=screen_left`).

**Beklenen sonuç**: Beş olay da doğru `lesson_id` ile ve reader `:core:audio` narratörünün gerçek
duraklama/ekrandan çıkma anlarıyla eşleşerek görünür.

## Senaryo 3 — Paywall hunisi (FR-007 – FR-011)

1. Kilitli/önizleme bir dersten "kilidini aç"a dokun → `paywall_shown`
   (`trigger_source=reader_unlock`).
2. Bir plan seçip satın alma butonuna dokun → `paywall_purchase_clicked` (`plan`).
3. Sahte satın alma tamamlanır (anında) → `subscription_started` (`plan`).
4. Ayrı bir denemede: paywall'ı satın almadan kapat (X / geri) → `paywall_dismissed`.

**Beklenen sonuç**: (2) ve (3) arka arkaya, (4) ise (2) hiç tetiklenmeden gelir; ikisi karışmaz.
`purchase_declined` bu sürümde tetiklenmez (bkz. research.md #5) — bu beklenen bir durumdur, hata
değildir.

## Senaryo 4 — Hata dayanıklılığı (FR-014, FR-015)

1. Cihazda uçak modunu aç, ders içeriğini görüntüle ve dinlemeyi başlat.
2. Uygulama çökmemeli, ekran normal çalışmaya devam etmeli.
3. Uçak modunu kapat → olaylar SDK'nın kendi tamponundan otomatik olarak Firebase'e ulaşmalı
   (DebugView konsolunda birkaç dakika içinde görünür).

**Beklenen sonuç**: Çevrimdışı sırasında hiçbir kullanıcı akışı kesilmez; bağlantı dönünce veri
kaybı olmaz (SC-002, SC-003).

## Senaryo 5 — Birim testleri (geliştirme sırasında, cihaz gerektirmez)

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :core:domain:test :feature:paywall:test :feature:reader:test :feature:home:test
```

Sahte (`Recording`) bir `AnalyticsReporter` ile: `PaywallViewModel.onIntent(PurchaseClicked)`
çağrısının tam olarak bir `PaywallPurchaseClicked` ve ardından bir `SubscriptionStarted` olayı
kaydettiğini; `ReaderViewModel`'in dinleme başlat/durdur efektlerinin sırasıyla `ListenStarted` /
`ListenStopped` ürettiğini doğrular.
