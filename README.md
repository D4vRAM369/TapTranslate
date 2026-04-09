# TapTranslate

> **Instant, private, offline translation — directly from any app's text selection menu.**

[![Android](https://img.shields.io/badge/Platform-Android%206.0%2B-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![ML Kit](https://img.shields.io/badge/AI-Google%20ML%20Kit-4285F4?logo=google)](https://developers.google.com/ml-kit/language/translation)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-03DAC5?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## What is TapTranslate?

TapTranslate is a minimalist Android utility that integrates directly into your OS text-selection menu. Select any text in any app, tap the three-dot overflow menu, and choose **TapTranslate** — your text is translated instantly, offline, with no data ever leaving your device.

No ads. No cloud. No account. Just translation.

---

## Key Features

- **⚡ Instant** — Translates in-place via `ACTION_PROCESS_TEXT`. The text you selected is replaced by its translation automatically.
- **🔒 100% Private** — Powered by Google ML Kit On-Device Translation. All processing happens locally. Zero network requests during translation.
- **🌐 Bidirectional** — Toggle between **Spanish → English** (Write Mode) and **English → Spanish** (Read Mode) from the settings panel.
- **🤖 Auto-Paste** — For apps that restrict the context menu (e.g. other users' Reddit comments), share text to TapTranslate and it auto-pastes the translation using Android's Accessibility API.
- **🎨 Minimal UI** — The app lives in the background. Its only visible screen is a clean settings panel and a first-run onboarding carousel.
- **🌍 Localized** — UI adapts to your system language (English / Spanish supported).

---

## How It Works

```
[User selects text in any app]
         ↓
[OS text menu shows TapTranslate]
         ↓
[ProcessTextActivity receives the text (invisible Activity)]
         ↓
[PreferencesManager reads the selected translation direction]
         ↓
[TranslationEngine calls ML Kit On-Device model]
         ↓
[Translated text is returned to the originating app]
```

For read-only text in closed apps (Reddit posts, etc.):
```
[User shares text  →  TapTranslate]
         ↓
[Translation is copied to clipboard]
         ↓
[AutoPasteService (Accessibility) injects ACTION_PASTE into focused field]
```

---

## Requirements

| Requirement | Version |
|---|---|
| Android OS | 6.0+ (API 24) |
| Target SDK | Android 15 (API 35) |
| Build Tools | Android Gradle Plugin 8.2.0 |
| Gradle | 8.4 |
| Kotlin | 1.9.24 |
| Java | 17 |

---

## Installation

### Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/d4vram/TapTranslate.git
   cd TapTranslate
   ```

2. Open in Android Studio (Hedgehog or newer).

3. Sync Gradle and run on your device or emulator (API 24+).

4. First launch: go through the onboarding carousel and optionally enable the Accessibility Service for Auto-Paste.

---

## First-Time Setup

### Step 1 — Basic Translation (works everywhere)
No setup needed. After installing, select any text in any app → tap ⋯ → choose **TapTranslate**.

### Step 2 — Auto-Paste Mode (optional, for restricted apps)
For full automation in apps like Reddit:

1. Open TapTranslate.
2. Tap the **⚠️ Super-Autopaste Disabled** banner.
3. In the Accessibility settings, find **TapTranslate** and enable it.
4. Return to TapTranslate — the banner disappears confirming it's active.

---

## Project Structure

```
app/
├── src/main/
│   ├── java/com/d4vram/taptranslate/
│   │   ├── AutoPasteService.kt       # Accessibility Service — auto-paste engine
│   │   ├── MainActivity.kt           # Settings UI (Jetpack Compose)
│   │   ├── OnboardingScreen.kt       # First-run carousel (HorizontalPager)
│   │   ├── PreferencesManager.kt     # SharedPreferences wrapper
│   │   ├── ProcessTextActivity.kt    # Invisible interceptor — translation trigger
│   │   └── TranslationEngine.kt      # ML Kit wrapper — bidirectional translation
│   ├── res/
│   │   ├── drawable/
│   │   │   └── ic_taptranslate.xml   # Vector icon
│   │   ├── values/
│   │   │   └── strings.xml           # ES strings (default)
│   │   ├── values-en/
│   │   │   └── strings.xml           # EN strings
│   │   └── xml/
│   │       └── accessibility_service_config.xml
│   └── AndroidManifest.xml
├── build.gradle.kts
└── gradle/wrapper/
    └── gradle-wrapper.properties     # Gradle 8.4 pinned
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Translation AI | Google ML Kit On-Device Translation |
| Async | Kotlin Coroutines + `kotlinx-coroutines-play-services` |
| Persistence | Android SharedPreferences |
| OS Integration | `ACTION_PROCESS_TEXT`, `ACTION_SEND`, `AccessibilityService` |

---

## Privacy

TapTranslate does **not**:
- Connect to any external server
- Collect any user data
- Require an internet connection to translate

The Accessibility Service permission is used exclusively to paste translated text into the focused input field after using the **Share** route. It does not read, log, or transmit screen content.

---

## Known Limitations

- Only supports **Spanish ↔ English** translation in v1.0. Additional language pairs planned.
- First use requires a one-time ~30MB model download per language pair.
- Auto-Paste via Accessibility requires manual permission activation by the user (Android security policy).
- Some apps may suppress the `ACTION_PROCESS_TEXT` menu entirely (use Share fallback instead).

---

## Roadmap

- [ ] Support for additional language pairs (FR, DE, PT…)
- [ ] Language auto-detection (remove manual mode toggle)
- [ ] ViewModel + MVVM architecture refactor
- [ ] Unit test coverage for `TranslationEngine` and `PreferencesManager`
- [ ] Play Store listing

---

## License

MIT License © 2026 d4vram
