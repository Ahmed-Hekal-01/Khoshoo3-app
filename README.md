# 🕌 Khoshoo3 — Auto-Silent for Prayer

> **A Proof of Concept Android app that automatically toggles Do Not Disturb mode during Islamic prayer times.**

This project was inspired by the [Fajr app by Blink22](https://play.google.com/store/apps/details?id=com.blink22.fajr). I originally searched for the repository to contribute an "Auto-Silent" feature. When I found the app wasn't open source, I built the logic myself in this PoC.

> [!NOTE]
> This app was generated with the assistance of **Antigravity** (AI coding agent) as a rapid PoC to validate the idea.

---

## 📱 Screenshots

| Main Screen | DND Active |
|:-----------:|:----------:|
| ![Main Screen](screenshots/main_screen.png) | ![DND Active](screenshots/dnd_active.png) |

> *Screenshots will be added after device testing.*

---

## ✨ Features

- **Local Prayer Time Calculation** — Uses the [Adhan](https://github.com/batoulapps/adhan-java) library with the **Egyptian General Authority of Survey** method. No external API calls, no API keys needed.
- **Auto Do Not Disturb** — Automatically enables DND mode within a 15-minute window of each prayer time, and restores the previous state afterward.
- **Smart DND Restore** — Tracks whether *the app* enabled DND, so it never overrides a user's manual DND.
- **Background Worker** — Uses **WorkManager** with a 15-minute periodic check to toggle DND even when the app is closed.
- **Test Mode** — A "Test DND Now" button that enables DND for 30 seconds, so you can verify the feature without waiting for a prayer.
- **Permission Handling** — Gracefully handles `ACCESS_FINE_LOCATION` and `ACCESS_NOTIFICATION_POLICY` with in-app prompts and settings deep-links.
- **Next Prayer Countdown** — Live countdown timer to the next prayer.

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM |
| Prayer Calculation | `com.batoulapps.adhan:adhan:1.2.1` |
| Background Tasks | WorkManager |
| Location | Google Play Services (FusedLocationProvider) |
| Permissions | Accompanist Permissions |

---

## 🗂️ Project Structure

```
app/src/main/java/com/khoshoo3/app/
├── MainActivity.kt                 # Entry point
├── data/
│   ├── PrayerRepository.kt         # Adhan library wrapper
│   └── SilenceManager.kt           # DND enable/disable (crash-safe)
├── worker/
│   └── PrayerCheckWorker.kt        # 15-min periodic WorkManager task
└── ui/
    ├── MainViewModel.kt            # UI state + countdown ticker
    ├── MainScreen.kt               # Single-screen Compose UI
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 🔑 Key Design Decisions

1. **Crash-safe DND control** — `SilenceManager` always checks `isNotificationPolicyAccessGranted()` before calling `setInterruptionFilter()`, preventing crashes on devices where the permission hasn't been granted.

2. **No external API** — All prayer times are calculated locally using the Adhan library, making the app work offline and avoiding API key management.

3. **Smart restore** — The worker persists a `we_enabled_dnd` flag in SharedPreferences so it only disables DND if it was the one that turned it on.

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/Ahmed-Hekal-01/Khoshoo3-app.git
   ```

2. Open in Android Studio: `File → Open → Khoshoo3-app/`

3. Build & run:
   ```bash
   ./gradlew assembleDebug
   ```

4. On the device:
   - Grant **Location** permission when prompted
   - Grant **DND access** by tapping the Auto-Silent toggle (opens Settings)
   - Use **"Test DND Now"** to verify the feature immediately

---

## 📄 License

This project is a Proof of Concept and is provided as-is for demonstration purposes.

---

*Built with ❤️ by Ahmed Hekal — powered by Antigravity*
