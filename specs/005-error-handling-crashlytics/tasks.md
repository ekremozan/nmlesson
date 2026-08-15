---

description: "Task list template for feature implementation"
---

# Tasks: API ve Çökme Hatalarında Best-Practice Hata Yönetimi ve Crashlytics Entegrasyonu

**Input**: Design documents from `/specs/005-error-handling-crashlytics/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/error-reporter-contract.md](contracts/error-reporter-contract.md),
[quickstart.md](quickstart.md)

**Tests**: Spesifikasyon açıkça test istemiyor; yine de constitution "domain/repository iş
kuralları test edilir" gerektirdiği için US1 ve US3'te hedefli birim testleri dahil edildi.
`FirebaseCrashlyticsErrorReporter`'ın kendisi (mevcut `LogcatErrorReporter` emsaliyle tutarlı
şekilde) birim testsiz bırakıldı — bkz. research.md #8.

**Organization**: Görevler spec.md'deki kullanıcı hikayelerine (US1/US2/US3) göre gruplanmıştır.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Paralel çalıştırılabilir (farklı dosyalar, bağımlılık yok)
- **[Story]**: Görevin ait olduğu kullanıcı hikayesi (US1, US2, US3)
- Her görevde tam dosya yolu belirtilir

## Path Conventions

Bu proje çok modüllü bir Android/Gradle projesidir (bkz. plan.md "Project Structure"). Aşağıdaki
yollar gerçek modül yapısını yansıtır: `core/crashreporting/`, `core/data/`, `app/`,
`feature/home/`, `feature/reader/`, `feature/settings/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Firebase/Crashlytics bağımlılıklarının ve yeni modülün proje iskelesini kurmak

- [X] T001 Bir Firebase projesi oluşturup `com.example.nativeminds` uygulama kimliğiyle eşleşen
      `google-services.json` dosyasını `app/google-services.json` konumuna yerleştir (bkz.
      research.md #10) — kullanıcı tarafından sağlandı (proje `nativeminds-68169`), yerleştirildi
- [X] T002 [P] `gradle/libs.versions.toml` içine `firebase-bom` versiyonunu, `google-services` ve
      `firebase-crashlytics` plugin alias'larını ve `firebase-crashlytics` kütüphane alias'ını ekle
- [X] T003 [P] Root `build.gradle.kts` içine `google-services` ve `firebase-crashlytics`
      eklentilerini mevcut desendeki gibi `apply false` olarak ekle
- [X] T004 `settings.gradle.kts` içine `include(":core:crashreporting")` satırını ekle

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: `ErrorReporter`'ın gerçek Crashlytics implementasyonuna bağlanması — hem US2 hem US3
bu tek bağlamaya dayanır (bkz. contracts/error-reporter-contract.md)

**⚠️ CRITICAL**: Bu faz tamamlanmadan hiçbir kullanıcı hikayesi doğrulanamaz

- [X] T005 `core/crashreporting/build.gradle.kts` oluştur — `:core:audio` ile aynı desende Android
      library modülü, `:core:domain`'e bağımlılık, Firebase BoM (`implementation(platform(...))`) +
      `firebase-crashlytics` kütüphanesi, Hilt + KSP
- [X] T006 [P] `core/crashreporting/src/main/java/com/example/nativeminds/crashreporting/FirebaseCrashlyticsErrorReporter.kt`
      oluştur — `ErrorReporter`'ı implement eder; `report(throwable, context)` içinde önce
      `FirebaseCrashlytics.log(context)` sonra `recordException(throwable)` çağırır; `setUserId`
      hiçbir yerde çağrılmaz (spec FR-007)
- [X] T007 [P] `core/crashreporting/src/main/java/com/example/nativeminds/crashreporting/di/CrashReportingModule.kt`
      oluştur — `@Provides` ile `FirebaseCrashlytics.getInstance()`, `@Binds` ile
      `ErrorReporter` → `FirebaseCrashlyticsErrorReporter`
- [X] T008 `core/data/src/main/java/com/example/nativeminds/data/di/DataModule.kt` içinden
      `errorReporter` `@Binds` fonksiyonunu ve `LogcatErrorReporter` import'unu kaldır
- [X] T009 `core/data/src/main/java/com/example/nativeminds/data/observability/LogcatErrorReporter.kt`
      dosyasını sil (yerini `:core:crashreporting` alıyor)
- [X] T010 `app/build.gradle.kts` içine `google-services` ve `firebase-crashlytics` eklentilerini
      uygula, `implementation(project(":core:crashreporting"))` bağımlılığını ekle
- [X] T011 `./gradlew assembleDebug` çalıştırıp yeni modülün ve `@Binds` değişikliğinin hatasız
      derlendiğini doğrula — T001 tamamlandıktan sonra `:app:assembleDebug` dahil tüm proje hatasız
      derleniyor (`BUILD SUCCESSFUL`)

**Checkpoint**: Foundation hazır — US1, US2, US3 artık bağımsız şekilde ilerleyebilir

---

## Phase 3: User Story 1 - Ağ/API hatası alan kullanıcı ne olduğunu anlar ve devam edebilir (Priority: P1) 🎯 MVP

**Goal**: Kullanıcı bir ağ/API hatası aldığında teknik olmayan, anlaşılır bir mesaj ve (uygun
olduğunda) yeniden deneme seçeneği görür; önbellekteki içerik erişilebilir kalır

**Independent Test**: Ağ bağlantısını kapatıp Home'da senkronizasyon ve Reader'da içerik açma
tetiklenerek; anlaşılır mesajın, retry seçeneğinin ve önbellek erişiminin çalıştığı, Crashlytics
kurulumundan bağımsız olarak doğrulanabilir

### Implementation for User Story 1

- [X] T012 [P] [US1] `feature/home/src/main/res/values/strings.xml` içindeki senkronizasyon hata
      mesajının (`ShowSyncError` efektine bağlı string) teknik olmayan, anlaşılır bir dille
      yazıldığını gözden geçir/gerekirse güncelle — gözden geçirildi, `home_sync_error` zaten
      anlaşılır ve önbelleği işaret ediyor, değişiklik gerekmedi
- [X] T013 [P] [US1] `feature/reader/src/main/res/values/strings.xml` içindeki hata/offline
      mesajlarının (`ReaderUnavailableState`) tutarlı ve anlaşılır olduğunu gözden geçir/gerekirse
      güncelle — gözden geçirildi, `reader_offline_*`/`reader_error_*`/`reader_retry` zaten
      anlaşılır ve retry seçeneği sunuyor, değişiklik gerekmedi
- [X] T014 [US1] `feature/home/src/test/java/com/example/nativeminds/feature/home/ui/HomeViewModelTest.kt`
      içine, art arda başarısız olan senkronizasyonların `ShowSyncError` efektini tekrar tekrar
      göndermediğini (spam yapmadığını) doğrulayan bir test ekle (spec FR-009) — eklendi ve geçti
- [ ] T015 [US1] **BEKLİYOR (cihaz/emülatör gerekli)** — [quickstart.md](quickstart.md) "4.
      Yakalanmış hata → non-fatal" adımının Home/Reader kısmını (mesaj metni ve retry davranışı,
      offline'da önbellek erişimi) manuel doğrula; bu ortamda Android cihaz/emülatör yok (`adb` yok)

**Checkpoint**: US1 bağımsız olarak test edilebilir ve teslim edilebilir durumda

---

## Phase 4: User Story 2 - Beklenmedik çökmeler otomatik olarak raporlanır (Priority: P1)

**Goal**: Uygulama beklenmedik şekilde çöktüğünde olay otomatik olarak Firebase Crashlytics'e
iletilir; geliştirici bunu bağlamıyla birlikte inceleyebilir

**Independent Test**: Debug derlemede kontrollü bir test çökmesi tetiklenip Firebase konsolunda
raporun (bağlantı olsun/olmasın, gecikmeli de olsa) göründüğü doğrulanarak, US1'den bağımsız test
edilebilir

### Implementation for User Story 2

- [X] T016 [US2] `feature/settings/src/main/java/com/example/nativeminds/feature/settings/ui/SettingsScreen.kt`
      (gerekirse `SettingsContract.kt`/`SettingsReducer.kt`) içine yalnızca `BuildConfig.DEBUG`
      derlemelerinde görünen bir "Test Crash" aksiyonu ekle — dokunulduğunda
      `throw RuntimeException("Test crash")` fırlatır; SC-002'yi kod değişikliği olmadan doğrulamanın
      tek yolu budur, bu yüzden debug-only olarak kalıcı bir QA affordance'ıdır (README Key
      Decisions'ta belgelenecek) — eklendi (`buildConfig = true` + `DebugTestCrashRow`, plain
      callback olarak `SettingsScreenContent`'e bağlandı, intent/reducer'a dokunmadı)
- [X] T017 [US2] [quickstart.md](quickstart.md) "2. Gerçek çökme senaryosu" adımını çalıştırıp
      Firebase konsolu → Crashes sekmesinde raporun göründüğünü doğrula — emülatörde (Pixel_10)
      "Test çökmesi tetikle" tetiklendi, `RuntimeException: Test crash` ile süreç öldü
      (`SettingsScreen.kt:70`), Android süreci arka planda otomatik yeniden başlatıp Crashlytics
      SDK'sı raporu yükledi; logcat'te `crashlyticsreports-pa.googleapis.com/.../batchlog` isteğine
      **Status Code: 200** doğrulandı — konsol ekranı kullanıcı hesabı gerektirdiği için ayrıca
      görsel olarak teyit edilmedi
- [ ] T018 [US2] **BEKLİYOR (cihaz/emülatör + Firebase konsolu erişimi gerekli)** —
      [quickstart.md](quickstart.md) "3. Çevrimdışı çökme kuyruklama" adımını çalıştırıp raporun
      bağlantı geri geldiğinde iletildiğini doğrula (spec FR-008)

**Checkpoint**: US1 ve US2 birlikte, birbirinden bağımsız çalışır durumda

---

## Phase 5: User Story 3 - Yakalanmış hatalar sessizce kaybolmaz (Priority: P2)

**Goal**: Kod içinde yakalanan (kullanıcıyı çökertmeyen) her hata, bağlam bilgisiyle birlikte
Crashlytics'te "non-fatal" olarak görünür kalır, PII sızdırmaz

**Independent Test**: Çevrimdışıyken Home/Reader'da bir hata tetiklenip, kullanıcının çökme
yaşamadığı ama Firebase konsolu → Non-fatals sekmesinde bu hatanın bağlamıyla (kullanıcı kimliği
olmadan) göründüğü doğrulanarak, US1/US2'den bağımsız test edilebilir

### Implementation for User Story 3

- [X] T019 [P] [US3] `core/domain/src/main/kotlin/com/example/nativeminds/domain/usecase/RefreshLessonContentUseCase.kt`
      ve `SyncLessonsUseCase.kt` içindeki `errorReporter.report(...)` çağrılarının `context`
      string'lerinin PII içermediğini gözden geçir (spec FR-007) — gözden geçirildi
      (`"refreshContent(lessonId=$lessonId)"`, `"syncIfNeeded"`), her ikisi de içerik kimliği/işlem
      adı taşıyor, kullanıcı verisi yok
- [X] T020 [P] [US3] `core/audio/src/main/java/com/example/nativeminds/audio/TextToSpeechNarrator.kt`
      içindeki `errorReporter.report(...)` çağrısının `context` string'inin PII içermediğini gözden
      geçir — gözden geçirildi (TTS durum kodu / cihaz locale'i), kullanıcı verisi yok
- [ ] T021 [US3] **BEKLİYOR (cihaz/emülatör + Firebase konsolu erişimi gerekli)** —
      [quickstart.md](quickstart.md) "4. Yakalanmış hata → non-fatal" ve "5. PII kontrolü"
      adımlarını çalıştırıp Firebase konsolu → Non-fatals sekmesinde T019/T020'deki üç çağrı
      noktasının bağlamıyla birlikte göründüğünü ve kullanıcı kimliği alanının boş/anonim olduğunu
      doğrula

**Checkpoint**: US1, US2, US3 hepsi bağımsız şekilde çalışır ve doğrulanmış durumda

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Belgeleme ve genel regresyon doğrulaması

- [X] T022 [P] `README.md` "Key Decisions" bölümüne Firebase Crashlytics seçimini ve yeni
      `:core:crashreporting` modülü kararını (ne seçildi, neden, trade-off, 10× ölçekte ne değişir)
      ekle
- [X] T023 [P] `README.md` "Cut Corners / Assumptions" bölümüne `google-services.json`'un gerçek bir
      Firebase projesinden manuel olarak sağlanması gerektiğini ve debug-only "Test Crash"
      affordance'ının (T016) amacını ekle
- [X] T024 `./gradlew test` ve `./gradlew lint` çalıştırıp mevcut `RecordingErrorReporter`/
      `NoOpErrorReporter`/`SilentErrorReporter` tabanlı testlerin hâlâ geçtiğini doğrula — T001
      tamamlandıktan sonra `:app` dahil **tüm modüllerde** `./gradlew test` ve `./gradlew lint`
      başarılı; bu geçişte `HomeViewModelTest`'e yeni bir test eklendi ve
      `SettingsScreenContent`'teki parametre sırası bir lint uyarısını (Modifier ilk opsiyonel
      parametre olmalı) düzeltmek için değiştirildi
- [ ] T025 [US1'e bağlı, T001'den bağımsız] [quickstart.md](quickstart.md) "7. Tema/önizleme
      kontrolü" adımını çalıştırıp Home/Reader hata bileşenlerini açık ve koyu temada gözden geçir
      — **BEKLİYOR**: bu ortamda cihaz/emülatör önizleme render'ı çalıştırılamadı (bkz. Completion
      Report)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Bağımsız, hemen başlanabilir — ancak T001 (gerçek Firebase projesi) manuel
  bir ön koşuldur ve T010/T011'i (Foundational) bloklar
- **Foundational (Phase 2)**: Setup'ın tamamlanmasına bağlı — TÜM kullanıcı hikayelerini bloklar
- **User Stories (Phase 3-5)**: Hepsi Foundational'ın tamamlanmasına bağlı; kendi aralarında
  bağımsızdır, öncelik sırasına göre (P1 → P1 → P2) veya paralel ilerletilebilir
- **Polish (Phase 6)**: Teslim edilmek istenen tüm kullanıcı hikayelerinin tamamlanmasına bağlı

### User Story Dependencies

- **US1 (P1)**: Foundational sonrası başlar — US2/US3'e bağımlı değil
- **US2 (P1)**: Foundational sonrası başlar — US1'e bağımlı değil
- **US3 (P2)**: Foundational sonrası başlar — US1/US2'ye bağımlı değil (aynı Foundational binding'i
  paylaşır ama ayrı doğrulanabilir)

### Parallel Opportunities

- T002, T003 paralel çalıştırılabilir (Setup)
- T006, T007 paralel çalıştırılabilir (Foundational, farklı dosyalar)
- Foundational tamamlandıktan sonra US1/US2/US3 fazları paralel ilerletilebilir (farklı geliştiriciler
  ile)
- T012, T013 paralel (US1); T019, T020 paralel (US3); T022, T023 paralel (Polish)

---

## Parallel Example: Foundational

```bash
Task: "core/crashreporting/.../FirebaseCrashlyticsErrorReporter.kt oluştur"
Task: "core/crashreporting/.../di/CrashReportingModule.kt oluştur"
```

## Parallel Example: User Story 3

```bash
Task: "RefreshLessonContentUseCase.kt ve SyncLessonsUseCase.kt context string'lerini gözden geçir"
Task: "TextToSpeechNarrator.kt context string'ini gözden geçir"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 (Setup) + Phase 2 (Foundational) tamamlanır — bu ikisi olmadan hiçbir hikaye
   doğrulanamaz, çünkü Crashlytics binding'i US2/US3'ün temelidir ve US1'in mesaj/retry davranışı
   zaten mevcut kod üzerine kurulu
2. Phase 3 (US1) tamamlanır
3. **DURUP DOĞRULA**: US1'i bağımsız test et (ağ hatası mesajı + retry + offline cache)
4. Hazırsa teslim/demo

### Incremental Delivery

1. Setup + Foundational → temel hazır (Crashlytics gerçekten bağlı)
2. US1 eklenir → bağımsız test edilir → teslim (MVP!)
3. US2 eklenir → bağımsız test edilir → teslim (artık çökmeler görünür)
4. US3 eklenir → bağımsız test edilir → teslim (artık yakalanmış hatalar da görünür)
5. Polish ile README/regresyon tamamlanır

## Notes

- [P] görevler = farklı dosyalar, bağımlılık yok
- [Story] etiketi görevi ilgili kullanıcı hikayesine bağlar
- T016'daki debug-only "Test Crash" affordance'ı, spec'in "her çökme otomatik raporlanır"
  başarı kriterini (SC-002) doğrulamanın pratik yoludur; production derlemede görünmez
  (`BuildConfig.DEBUG` koruması)
- Her görev tamamlandıktan sonra commit atılması önerilir
- Her checkpoint'te hikayenin bağımsız çalıştığı doğrulanmalı
