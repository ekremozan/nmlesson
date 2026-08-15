# Implementation Plan: API ve Çökme Hatalarında Best-Practice Hata Yönetimi ve Crashlytics Entegrasyonu

**Branch**: `005-error-handling-crashlytics` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-error-handling-crashlytics/spec.md`

## Summary

Uygulamadaki tüm ağ/API istekleri ve çökmeye açık noktalar için tutarlı, kullanıcı dostu bir hata
yönetimi kurulacak ve **tek bir merkezi toplama noktasından** (mevcut `ErrorReporter` arayüzü,
`:core:domain`) hem yakalanmış hatalar hem de gerçek çökmeler Firebase Crashlytics'e akacak. Teknik
yaklaşım: yeni bir soyutlama icat etmek yerine zaten kurulu olan `ErrorReporter` seam'ini gerçek bir
backend'e bağlamak — bunu, mevcut `:core:audio` modülüyle aynı desende yeni, odaklı bir
`:core:crashreporting` modülünde yaparak Firebase bağımlılığını `:core:data`'dan izole etmek ve
kullanıcının istediği modülerliği sağlamak.

## Technical Context

**Language/Version**: Kotlin (proje genelinde kullanılan sürüm), JVM target 11

**Primary Dependencies**: Firebase BoM + Firebase Crashlytics SDK, Google Services Gradle eklentisi,
Firebase Crashlytics Gradle eklentisi (mevcut Hilt/KSP/Compose eklentilerine ek olarak)

**Storage**: N/A — Crashlytics SDK kendi yerel kuyruklamasını kendi içinde yönetir; Room şeması
değişmez

**Testing**: JUnit birim testleri (mevcut `RecordingErrorReporter`/`NoOpErrorReporter`/
`SilentErrorReporter` fake'leriyle kurulu desen korunur); `FirebaseCrashlyticsErrorReporter` için
ayrı birim testi yok — bkz. research.md kararı

**Target Platform**: Android, minSdk 24 / compileSdk 36-37 / targetSdk 36 — mevcut proje hedefleriyle
aynı

**Project Type**: mobile-app (çok modüllü Gradle projesi)

**Performance Goals**: Hata/çökme raporlama kullanıcı akışını bloklamamalı; SDK'nın kendi arka plan
işleyişine güvenilir, UI thread'de senkron ağır iş yapılmaz

**Constraints**: Offline-capable (raporlar SDK tarafından kuyruklanıp bağlantı gelince gönderilir),
raporlar PII içermez, mevcut modül bağımlılık yönü (`:feature:*` → `:core:domain`, asla
`:core:data`/yeni raporlama modülüne değil) korunur

**Scale/Scope**: Proje genelindeki tüm yakalanmış hata noktaları (bugün 3 çağrı noktası:
`RefreshLessonContentUseCase`, `SyncLessonsUseCase`, `TextToSpeechNarrator`) + tüm süreç çökmeleri;
yeni bir modül (`:core:crashreporting`), mevcut `ErrorReporter` binding'inin değişimi, mevcut
retry/hata UI desenlerinin (Home pull-to-refresh + `ShowSyncError`, Reader `RetryRequested`)
gözden geçirilmesi

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Prensip | Değerlendirme | Sonuç |
|---|---|---|
| I. Defensible Decisions | Yeni bağımlılıklar (Firebase BoM, Crashlytics, Google Services) `gradle/libs.versions.toml`'a eklenip README "Key Decisions"ta gerekçelendirilecek; yeni modül kararı aynı bölümde belgelenecek | PASS |
| II. Clean Architecture & Model Separation | Yeni `:core:crashreporting` modülü yalnızca `:core:domain`'deki `ErrorReporter` arayüzünü implement eder ve `@Binds` ile bağlanır; hiçbir `:feature:*` modülü Firebase'i doğrudan görmez; `:app` bağımlılığı ekler (mevcut `:core:audio` deseniyle birebir) | PASS |
| III. MVI (Single Mutation Path) | Bu özellik yeni ekran/intent eklemiyor; mevcut reducer'lara dokunmuyor (retry akışları zaten var) | N/A |
| IV. Offline-First & Visible Failures | Bu prensibin doğrudan tamamlanmasıdır: her hata raporlanır, çevrimdışıyken kuyruklanır, önbellek erişilebilir kalır | PASS |
| V. Design System | Bu özellik yeni UI tokenı/ekranı eklemiyor; mevcut hata/retry bileşenleri zaten token kullanıyor | N/A |

Gate sonucu: **PASS** — Complexity Tracking tablosuna gerek yok.

## Project Structure

### Documentation (this feature)

```text
specs/005-error-handling-crashlytics/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── error-reporter-contract.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
core/crashreporting/                          # YENİ modül — :core:audio ile aynı desen
├── build.gradle.kts                          # Firebase BoM + Crashlytics bağımlılığı burada izole
└── src/main/java/com/example/nativeminds/crashreporting/
    ├── FirebaseCrashlyticsErrorReporter.kt   # ErrorReporter implementasyonu (non-fatal + context)
    └── di/
        └── CrashReportingModule.kt           # FirebaseCrashlytics @Provides + ErrorReporter @Binds

core/data/src/main/java/com/example/nativeminds/data/
├── observability/LogcatErrorReporter.kt      # KALDIRILACAK (yerini crashreporting modülü alır)
└── di/DataModule.kt                          # errorReporter @Binds satırı kaldırılır

core/domain/src/main/kotlin/com/example/nativeminds/domain/observability/
└── ErrorReporter.kt                          # DEĞİŞMEZ — mevcut tek soyutlama noktası

app/
├── build.gradle.kts                          # google-services + crashlytics Gradle eklentileri,
│                                              #   :core:crashreporting bağımlılığı eklenir
└── google-services.json                      # YER TUTUCU — gerçek Firebase projesinden gelecek
                                                #   (bkz. research.md ve README Cut Corners)

gradle/libs.versions.toml                     # firebase-bom, firebase-crashlytics, google-services
                                                #   ve firebase-crashlytics Gradle eklentisi eklenir

feature/home/src/main/java/.../ui/            # DOKUNULMAYACAK — mevcut ShowSyncError/retry deseni
feature/reader/src/main/java/.../ui/          # DOKUNULMAYACAK — mevcut RetryRequested deseni
                                                #   yalnızca mesaj metni/string tutarlılığı gözden
                                                #   geçirilir (FR-001, FR-009)
```

**Structure Decision**: Firebase Crashlytics bağımlılığı, kullanıcının istediği modülerlik gereği
`:core:data`'ya değil, `:core:audio` ile aynı şekle sahip yeni ve odaklı bir `:core:crashreporting`
modülüne konur. Bu modül yalnızca `:core:domain`'e bağımlıdır ve tek görevi `ErrorReporter`
arayüzünü Firebase'e bağlamaktır. `:app`, `DataModule`'daki mevcut `LogcatErrorReporter` binding'ini
kaldırır ve yeni modülün binding'ini kullanır — böylece "nerede olursa olsun tek bir yerden
toplama" isteği, zaten var olan tek soyutlama noktası (`ErrorReporter`) üzerinden, yeni bir
soyutlama katmanı eklenmeden karşılanır.

## Complexity Tracking

*Gerek yok — Constitution Check'te ihlal bulunmadı.*
