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
| Design tokens | Ported the design system into a typed Compose token layer (`ui/theme/`) rather than styling per screen | One place to change a color or a text style; a palette regression is caught in `ThemePreview.kt` instead of on a device | More indirection for a small app. At 10× (multiple squads, more surfaces) this becomes a shared `:design-system` module with screenshot tests per component |
| Material 3 vs. custom | Material 3 scheme for standard roles + a small `NativeMindsColors` for what M3 has no slot for | Keeps M3 components (ripples, text fields, sheets) correct for free while still allowing brand roles the spec doesn't model | Two places to look up a color. The rule "standard → MaterialTheme, brand → NativeMindsTheme" is documented in CLAUDE.md to keep it unambiguous |
| Dynamic color | Disabled | The paper-and-terracotta ground and serif reading voice *are* the product's atmosphere; repainting them from wallpaper trades identity for a personalization win a reading app doesn't benefit from | Users who expect Material You theming don't get it |
| Error color | Mapped M3 `error` onto the deep terracotta ramp instead of adding a red | The design system has no red — warnings use terracotta, success uses sage. A stock red would read as a system dialog dropped into the app | Less "alarming" than a red; acceptable because the app has no destructive actions so far |
| Fonts | Bundled static instances of Caprasimo / Newsreader / Figtree (OFL) instead of downloadable fonts | The type is part of the brand — it has to be right on first launch and offline, which a provider round-trip can't guarantee. Static instances (not variable) because variable axes need API 26 and `minSdk` is 24 | ~400 KB of APK. At scale: ship only the weights in use (already done), and revisit variable fonts once `minSdk` ≥ 26 |
| Accent split | Two accents: `primary` (#C67139) for fills, `accentText` (#B2622D) for glyphs | The fill accent only reaches ~2.7:1 on the paper ground — it fails WCAG AA as text | One more token to reason about, in exchange for accessible accent text everywhere |
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
