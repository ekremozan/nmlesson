# Uygulama Planı: Firebase Analytics Entegrasyonu

**Dal**: `006-firebase-analytics` | **Tarih**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Girdi**: `specs/006-firebase-analytics/spec.md` içindeki özellik spesifikasyonu

## Özet

Kullanıcının uygulama içinde nereden girip nasıl gezindiğini (ekran akışı), hangi dersi seçip
dinlediğini/durdurduğunu ve paywall'dan satın almaya kadar olan huniyi (gösterildi → tıklandı →
satın alındı / vazgeçildi / geri gidildi), parametreli olaylarla Firebase Analytics'e raporlamak.
Teknik yaklaşım: `:core:crashreporting`/`ErrorReporter` ile aynı kalıpta yeni bir `:core:analytics`
modülü ve `:core:domain`'de `AnalyticsReporter` arayüzü + kapalı (`sealed`) `AnalyticsEvent` tipi.
Ekran görüntüleme olayları `:app`'teki `NativeMindsNavHost`'ta `NavController` geri çağrılarından
merkezi olarak; ders/dinleme ve paywall olayları ilgili ViewModel'lerin **efekt yürütme**
noktalarından (reducer'ın kendisi değil) tetiklenir — tıpkı `PaywallViewModel`'in
`entitlementRepository.setPremium(true)` çağrısı gibi.

## Teknik Bağlam

**Dil/Sürüm**: Kotlin (proje genelinde), Java 11 hedef uyumluluk

**Birincil Bağımlılıklar**: Firebase Analytics KTX (`com.google.firebase:firebase-analytics`),
mevcut `firebase-bom` (34.9.0) üzerinden; Hilt (DI); Jetpack Navigation Compose (`NavController`
geri çağrıları için, zaten bağımlılık)

**Depolama**: Yok (analiz olayları için ayrı bir yerel depolama eklenmez — çevrimdışı kuyruklama
Firebase Analytics SDK'sının kendi disk tamponu ile karşılanır, bkz. research.md)

**Test**: JUnit + kotlin.test (mevcut `:core:domain` test kurulumu) — `AnalyticsEvent` eşlemesi ve
reducer/ViewModel tetikleme noktaları için sahte (fake) `AnalyticsReporter` ile birim testleri

**Hedef Platform**: Android (minSdk 24, compileSdk/targetSdk 36)

**Proje Türü**: Mobil uygulama (çok modüllü Gradle projesi)

**Performans Hedefleri**: Olay kaydı ana iş parçacığını bloklamamalı; SDK'nın kendi asenkron/best
effort davranışına güvenilir (özel bir performans bütçesi gerekmez)

**Kısıtlar**: Olay parametrelerinde PII yok; her özellik modülü Firebase SDK'sına doğrudan
bağımlı olmamalı (yalnızca `:core:domain`'deki `AnalyticsReporter` arayüzüne bağımlı olmalı);
mevcut MVI/Clean Architecture kurallarına (bkz. constitution) uyulmalı

**Kapsam/Ölçek**: 5 ekran (Home, Reader, Paywall, PurchaseSuccess, Settings), ~11 farklı olay tipi

## Anayasa Kontrolü

*KAPI: Faz 0 araştırmasından önce geçilmeli. Faz 1 tasarımından sonra yeniden kontrol edilir.*

| İlke | Bu özellik için değerlendirme | Sonuç |
|------|-------------------------------|-------|
| I. Savunulabilir Kararlar | `AnalyticsReporter` soyutlaması ve `:core:analytics` modülü, tam olarak `ErrorReporter`/`:core:crashreporting` deseninin tekrarıdır — yeni bir desen icat edilmiyor. Firebase BOM zaten bağımlılık grafiğinde (`firebase-crashlytics` için); yeni eklenen tek `libs.` girdisi `firebase-analytics`. | Geçti |
| II. Clean Architecture & Model Ayrımı | `AnalyticsEvent` (domain modeli) `:core:domain`'de tanımlanır; Firebase `Bundle`/parametre eşlemesi yalnızca `:core:analytics` içindeki implementasyonda yapılır. Hiçbir `:feature:*` modülü Firebase SDK'sına bağımlı olmaz. | Geçti |
| III. MVI Tek Mutasyon Yolu | Analiz kaydı **state yazmaz**; bu yüzden reducer'ın işi değildir. `ErrorReporter` çağrılarıyla aynı şekilde, ViewModel'in efekt yürütme noktalarında (reducer'ın döndürdüğü efektlerin işlenmesi sırasında) yan etki olarak tetiklenir. Reducer hâlâ *hangi* efektin üretileceğine karar veren tek yerdir; ViewModel yalnızca o efekti Firebase çağrısına çevirir. Gezinme olayları (ekran görüntülendi) zaten intent olmayan `NavController` geri çağrılarından geldiği için MVI sınırının dışındadır — mevcut "gezinme bir intent değildir" kuralıyla tutarlı. | Geçti |
| IV. Offline-First & Görünür Hatalar | Firebase Analytics SDK'sı olayları yerelde tamponlayıp bağlantı geldiğinde gönderir (bkz. research.md) — FR-016/SC-003 bunun üzerine kuruludur, ekstra depolama gerekmez. Analiz çağrısı bir istisna fırlatırsa `ErrorReporter`'a iletilir, asla sessizce yutulmaz veya kullanıcı akışını kesmez (FR-014/FR-015). | Geçti |
| V. Tasarım Sistemi | Bu özellik yeni bir UI yüzeyi eklemiyor (yalnızca mevcut ekranlara enstrümantasyon), bu yüzden bu ilke uygulanabilir değil. | Uygulanamaz |

Anayasa ihlali yok; Karmaşıklık İzleme bölümü boş bırakıldı.

**Faz 1 sonrası yeniden kontrol**: `research.md` ve `data-model.md`'deki tasarım (kapalı
`AnalyticsEvent` hiyerarşisi, tek `AnalyticsReporter.log()` metodu, gezinme olaylarının `:app`'te
merkezi tetiklenmesi, ders/dinleme/paywall olaylarının ViewModel efekt yürütme noktalarında
tetiklenmesi) yukarıdaki değerlendirmeyi değiştirmiyor — tüm ilkeler hâlâ geçiyor.

## Proje Yapısı

### Dokümantasyon (bu özellik)

```text
specs/006-firebase-analytics/
├── plan.md              # Bu dosya (/speckit-plan komutu çıktısı)
├── research.md          # Faz 0 çıktısı (/speckit-plan komutu)
├── data-model.md         # Faz 1 çıktısı
├── contracts/
│   └── analytics-events.md   # Faz 1 çıktısı — olay/parametre sözleşmesi
├── quickstart.md         # Faz 1 çıktısı
└── tasks.md              # Faz 2 çıktısı (/speckit-tasks komutu — bu komutla oluşturulmaz)
```

### Kaynak Kod (repo kökü)

```text
core/
├── domain/
│   └── src/main/kotlin/com/example/nativeminds/domain/
│       └── observability/
│           ├── ErrorReporter.kt              # mevcut
│           ├── AnalyticsEvent.kt             # YENİ — sealed olay modeli
│           └── AnalyticsReporter.kt          # YENİ — arayüz
├── analytics/                                # YENİ modül, :core:crashreporting'in eşi
│   ├── build.gradle.kts
│   └── src/main/java/com/example/nativeminds/analytics/
│       ├── FirebaseAnalyticsReporter.kt      # AnalyticsEvent -> Firebase logEvent eşlemesi
│       └── di/AnalyticsModule.kt
└── crashreporting/                            # mevcut, değişmez

app/
└── src/main/java/com/example/nativeminds/navigation/
    └── NativeMindsNavHost.kt                 # ekran görüntülendi olayları buradan tetiklenir

feature/
├── home/  ...ui/HomeViewModel.kt             # ders seçildi (nav geri çağrısı üzerinden değil,
│                                               # doğrudan tıklama noktasında; bkz. research.md)
├── reader/ ...ui/ReaderViewModel.kt          # içerik görüntülendi, dinleme başladı/durduruldu
├── paywall/ ...ui/paywall/PaywallViewModel.kt # paywall tıklandı, abonelik başlatıldı, vazgeçildi
└── settings/ (değişmez — premium tıklaması zaten paywall'a yönlendiriyor)
```

**Yapı Kararı**: Mevcut çok modüllü yapı korunur. Tek yeni modül `:core:analytics`, mevcut
`:core:crashreporting`'in birebir eşi olarak eklenir ve `settings.gradle.kts`'e dahil edilir.
`AnalyticsEvent`/`AnalyticsReporter` `:core:domain`'e eklenir çünkü bunlar saf Kotlin sözleşmesidir
ve her `:feature:*` modülünün zaten erişebildiği tek katmandır.

## Karmaşıklık İzleme

*Anayasa Kontrolü'nde ihlal yok — bu bölüm boş bırakılmıştır.*
