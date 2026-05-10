# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project identity

- **App name**: Session Tracker for Steam
- **Application ID** (Play Store permanent): `com.sessiontracker.steam`
- **Internal Kotlin package**: `com.steamtimeline.app` — intentionally not refactored, no user-visible impact
- **Repo**: `D:\ClaudePrograms\SteamTracker\`
- **Current version**: versionCode 1, versionName "1.0" (bump to 2 / "1.1" when starting v1.1 build)
- **Min SDK**: 26, **Target/Compile SDK**: 35

## Build commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (signed AAB for Play Store)
./gradlew bundleRelease

# Lint
./gradlew lint

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.steamtimeline.app.ExampleUnitTest"
```

Build output goes to `app/build/outputs/`. Signed AAB lives at `app/release/app-release.aab` (gitignored).

## Architecture

Standard MVVM + clean architecture with Hilt DI throughout.

```
data/
  local/        — Room: SteamDatabase, GameSessionDao, GameSession entity
  remote/       — Retrofit: SteamApiService (response models co-located in same file)
  repository/   — SteamRepository (wraps DAO + API), PreferencesRepository (DataStore)
domain/         — SessionTracker: poll logic (start/stop/switch session state machine)
service/        — SteamMonitorService (foreground service), BootReceiver
di/             — AppModule: provides OkHttpClient, Retrofit, Room, DAO
ui/
  NavHost.kt    — sealed Screen class + NavHost composable
  home/         — HomeScreen, HomeViewModel, CalendarDialog
  settings/     — SettingsScreen, SettingsViewModel
  summary/      — SummaryScreen, SummaryViewModel
  theme/        — Theme.kt
worker/         — SteamPollingWorker (WorkManager, currently unused in favour of foreground service)
```

## Key data flows

**Polling loop**: `SteamMonitorService.onStartCommand` launches a coroutine that calls `SessionTracker.poll()` every 60 s. `SessionTracker` reads credentials from `PreferencesRepository`, calls `SteamRepository.getCurrentlyPlayingGame()` (→ `GetPlayerSummaries` API), then starts/closes/switches the active `GameSession` in Room. The service is `START_STICKY` and auto-restarts via `BootReceiver`.

**Active session detection**: `gameid` and `gameextrainfo` fields on `PlayerSummary` are non-null only when the player is in-game. No `gameid` = not playing.

**Day-scoped queries**: `SteamRepository.dayBounds(date)` converts a `LocalDate` to epoch-ms start/end using the device timezone. All `GameSessionDao` day queries use `startTime >= startMs AND startTime < endMs` (known issue — v1.1 will switch to overlap logic for cross-midnight sessions).

**Credentials flow**: API key + Steam ID stored in DataStore via `PreferencesRepository`. `SteamRepository.resolveSteamId()` accepts a raw 64-bit ID, profile URL, or vanity URL and normalises it to a 17-digit Steam ID.

**Game artwork**: Session cards load banner images via Coil using the Steam CDN URL pattern `https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/{appId}/header.jpg`.

## Room schema

Single table `game_sessions`:
- `id` (Long, PK autoGenerate)
- `appId` (Long) — Steam app ID
- `gameName` (String)
- `startTime` (Long) — epoch ms
- `endTime` (Long?) — null = active session
- `durationSeconds` (Long)

**When adding DAO queries**: Room's `@Database` version must be incremented in `SteamDatabase.kt` and a migration added, or `fallbackToDestructiveMigration()` used. Current schema version is 1.

## Navigation

`NavHost.kt` defines a `sealed class Screen` and a single `SteamTimelineNavHost()` composable. To add a new screen: add a `Screen` subclass, add a `composable()` entry, and pass a navigation lambda down from `NavHost` into the parent screen.

## Foreground service notes

The service type is declared as `dataSync` in `AndroidManifest.xml`. **Known issue for v1.1**: Android 15 blocks `dataSync` from `BOOT_COMPLETED` receivers — fix is to change to `foregroundServiceType="specialUse"`, swap the permission, and add the `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property. Do not start a new foreground service type without also updating the Play Console App Content declaration.

## Store assets

```
store-assets/
  ic_launcher-playstore.png   — 512×512 Play Store icon
  feature-graphic.png         — Play Store feature graphic
  screenshots/                — 4 phone screenshots (01–04)
```

Privacy policy: https://79hearts.github.io/session-tracker-for-steam/PRIVACY.html
