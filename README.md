# Session Tracker for Steam

An Android app that monitors your Steam gaming sessions and visualizes them on a precise, swipeable timeline.

> **Application ID**: `com.sessiontracker.steam` · **Min SDK**: 26 (Android 8.0)

> **Not affiliated with Valve Corporation.** "Steam" and the Steam logo are trademarks of Valve. This is an independent app that uses Steam's public Web API to read activity that you have made public.

## Features

- **Live monitoring** — a foreground service polls Steam's Web API every 60 seconds to detect what you're playing, with second-level accuracy on session start/end.
- **Now Playing card** — pulsing live indicator that updates within seconds of launching a game.
- **Daily timeline** — horizontal bar showing every session as a colored segment, proportional to play time. Time axis from midnight to "now" for today, full 24 hours for past days.
- **Swipeable history** — swipe right through the last 30 days, swipe left to come back.
- **Calendar picker** — full-screen calendar with today highlighted; tap any day in the last 30 to jump there.
- **Summary view** — totals across 3-day, 7-day, 14-day, and 30-day ranges, with a stacked time-share bar and a sorted leaderboard of games.
- **Persistent control** — start/stop monitoring from Settings or directly from the notification.
- **30-day retention** — old sessions are auto-pruned to keep storage minimal.
- **Local-only data** — no analytics, no ads, no third-party trackers. Your data never leaves your device except for the calls to Valve's API.

## Setup

### 1. Get a free Steam Web API Key

1. Go to https://steamcommunity.com/dev/apikey
2. Log in with your Steam account.
3. Enter any domain (e.g. `localhost`) and click **Register**.
4. Copy the 32-character key.

### 2. Find your Steam64 ID

Open your Steam profile in a browser. Session Tracker for Steam accepts:
- The 17-digit numeric ID (e.g. `76561198XXXXXXXXX`)
- The full profile URL (`https://steamcommunity.com/profiles/76561198XXXXXXXXX`)
- A custom URL (`https://steamcommunity.com/id/yourname`) — auto-resolved by the app

### 3. Set your Steam profile to Public

In Steam → **Edit Profile → Privacy Settings**: set both **My profile** and **Game details** to **Public**.

### 4. Configure Session Tracker for Steam

Open the app → tap **Settings** (⚙) → tap **Get API Key** to open Steam in your browser → copy → paste with the 📋 button → repeat for the Steam ID field with **Open Profile** → tap **Test connection** to verify → tap **Save**.

The app starts polling within 60 seconds and shows a persistent notification while monitoring is on.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Database | Room |
| Background | Foreground service + WorkManager fallback |
| HTTP | Retrofit + OkHttp |
| Images | Coil |
| DI | Hilt |
| Preferences | DataStore |
| State | ViewModel + StateFlow |

## Project layout

```
app/src/main/
├── java/com/steamtimeline/app/
│   ├── data/
│   │   ├── local/          Room entities, DAOs, database
│   │   ├── remote/         Retrofit service + response models
│   │   └── repository/     SteamRepository, PreferencesRepository
│   ├── domain/             SessionTracker (state machine + cleanup)
│   ├── service/            Foreground service + boot receiver
│   ├── worker/             15-min WorkManager fallback poll
│   ├── ui/
│   │   ├── home/           Home screen, calendar dialog, day pager
│   │   ├── summary/        3/7/14/30-day analytics
│   │   ├── settings/       Credentials, monitoring toggle, about
│   │   └── theme/          Dark Steam-inspired Material 3 palette
│   ├── di/                 Hilt module
│   ├── MainActivity.kt
│   └── Session Tracker for SteamApp.kt
└── res/
    ├── drawable/           Launcher icon foreground (vector)
    ├── mipmap-anydpi-v26/  Adaptive icon definitions
    └── values/             colors.xml, strings.xml, themes.xml
```

## Build

Requirements: Android Studio Hedgehog or newer, JDK 17+, Android SDK 26+.

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

For release builds and Play Store publishing, see [PUBLISHING.md](PUBLISHING.md).

## Documentation

| File | Purpose |
|---|---|
| [PRIVACY.md](PRIVACY.md) | Privacy policy (must be hosted at a public URL for the Play Store) |
| [STORE_LISTING.md](STORE_LISTING.md) | All copy, screenshot guidance, content-rating answers, data-safety form |
| [PUBLISHING.md](PUBLISHING.md) | Step-by-step Play Store publishing walkthrough |
| [TODO.md](TODO.md) | Tracked feature ideas |

## Notes & caveats

- The Steam API only reports games that are *currently running on Steam*. Non-Steam games launched through Steam (added as shortcuts) usually don't appear, and games run outside of Steam never do.
- "Steam Cloud Sync" timing isn't visible to the API — sessions reflect actual gameplay, not just launches.
- If you stop the persistent notification, monitoring stops until you reopen the app or toggle it back on in Settings.
- After 30 days, ended sessions are deleted automatically. Active sessions (one ever exists at a time) are never deleted.

## License

Personal project. No license declared yet — add one before publishing source publicly.
