# NativeMinds — Bite-Sized Story App (Mobile Case Study)

> A short-story reading & listening app: browse and search stories, read or listen, free users get a taste, subscribers unlock everything — including an AI-powered feature.

<!-- TODO: 1-2 cümlelik final ürün tanımı + ekran görüntüleri / GIF -->

## 📱 Live Demo

- **APK / Store link:** _TODO (deploy edilince eklenecek)_
- **Demo video (2–5 min):** _TODO_
- **Architecture diagram (Miro/Excalidraw):** _TODO link_

## ✨ Features

<!-- Tamamlandıkça işaretle -->
- [ ] Story list — browse & search
- [ ] Story reader (read experience)
- [ ] Listen mode (audio / TTS)
- [ ] Free vs Premium gating (paywall)
- [ ] AI feature: _TODO — hangi AI özelliği, neden_
- [ ] Offline support (cached stories readable without network)
- [ ] Analytics (key funnel events)
- [ ] Crash / error reporting

## 🏗 Architecture

<!-- Diagram buraya embed edilecek + 1 paragraf özet -->

**Stack:** Kotlin, Jetpack Compose (Material 3), _TODO: diğerleri (Room, Hilt, Media3, Firebase...)_

**Shape:** MVVM + unidirectional data flow, repository layer, offline-first (local DB as source of truth).

<!-- TODO: katman diyagramı, veri akışı, modül yapısı -->

## 🧠 Key Decisions & Reasoning

<!-- Her önemli karar için: ne seçtim, neden, trade-off ne, 10× ölçekte ne değişirdi -->

| Decision | What I chose | Why | Trade-off / at 10× scale |
|---|---|---|---|
| Platform & UI | Native Android, Compose | _TODO_ | _TODO_ |
| Architecture pattern | _TODO_ | _TODO_ | _TODO_ |
| Data / offline strategy | _TODO_ | _TODO_ | _TODO_ |
| Audio approach (TTS vs pre-generated) | _TODO_ | _TODO_ | _TODO_ |
| Subscription / gating model | _TODO_ | _TODO_ | _TODO_ |
| AI feature design | _TODO_ | _TODO_ | _TODO_ |
| Analytics & crash reporting | _TODO_ | _TODO_ | _TODO_ |

## 🤖 How I Worked With AI

<!-- Case Part 2: AI'ı nasıl yönlendirdim -->

### Setup
<!-- TODO: Claude Code, CLAUDE.md kuralları, custom command/agent/MCP vs. kısaca -->

### How I framed & steered
<!-- TODO: problemi nasıl çerçeveledim, ne istedim, ilk cevap yanlışken nasıl iterate ettim — 2-3 somut örnek -->

### Where I overrode the AI
<!-- TODO: AI çıktısını reddedip kendi yolumla yaptığım yerler — somut örnekler -->

## ✂️ Cut Corners & Assumptions

<!-- Bilerek kısılan köşeler — anında buraya ekle, sona bırakma -->
- _TODO: örn. mock billing (gerçek Play Billing yerine), seed content, ..._

## 🔭 What I'd Do Next / At 10× Scale

- _TODO_

## 🚀 Getting Started

```bash
./gradlew installDebug   # build & install on a connected device/emulator
./gradlew test           # unit tests
```

<!-- TODO: gerekli API key / local.properties ayarları varsa buraya -->
