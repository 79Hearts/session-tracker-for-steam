# Privacy Policy — Session Tracker for Steam

> Internal package: `com.sessiontracker.steam`

_Last updated: 2026-05-06_

Session Tracker for Steam ("the app") is provided as-is by an independent developer. This document describes what information the app collects, how it is used, and where it is stored.

## Summary

- **No data leaves your device except direct calls to Steam's public Web API.**
- **No analytics, no ads, no third-party trackers.**
- **All session history is stored locally** in an on-device database.

## Information You Provide

When you set up Session Tracker for Steam, you enter:

| Item | Why | Where it's stored |
|---|---|---|
| Steam Web API Key | To authenticate calls to Steam's public Web API | Locally, in Android's encrypted DataStore |
| Steam64 ID | To identify which account's public activity to read | Locally, in Android's encrypted DataStore |

These values are **only** sent to `api.steampowered.com` (Valve's official API). They are never transmitted to the developer or any third party.

## Information the App Collects Automatically

While monitoring is enabled, the app polls Steam's API roughly every 60 seconds and records, **on your device only**:

- Game name
- Steam App ID
- Session start time
- Session end time
- Session duration

This information is stored in a local Room database on your device. It is automatically deleted after 30 days. You can disable monitoring at any time via the Settings screen or the persistent notification's "Stop" action.

## Information We Share With Third Parties

**None.** The app makes outbound HTTPS requests only to Valve's `api.steampowered.com` servers. The information transmitted in those requests is your API key and Steam64 ID, which are required by Valve's API. The app receives game-activity information from Valve in response.

The app does **not** include analytics SDKs, advertising SDKs, crash reporters, or any other third-party data collection.

## Permissions

| Permission | Why |
|---|---|
| `INTERNET` | To call Steam's public Web API |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` | To run the background poller while showing a persistent notification |
| `POST_NOTIFICATIONS` | To display the monitoring notification on Android 13+ |
| `RECEIVE_BOOT_COMPLETED` | To resume monitoring after device reboot if you previously enabled it |

## Your Choices

- **Stop monitoring**: tap "Stop" on the persistent notification, or toggle off "Background monitoring" in Settings.
- **Delete all data**: uninstall the app. Android removes all local data with the app.
- **Change credentials**: re-enter values in the Settings screen.

## Children

The app does not knowingly collect information from children. Steam's terms of service set their own minimum age for accounts; Session Tracker for Steam simply mirrors data those accounts have already chosen to make public.

## Changes

If this policy changes, the updated version will replace this file in the app's repository, and a notice will be added to the app's release notes.

## Contact

For questions, open an issue on the project's GitHub repository.

---

_This app is not affiliated with, endorsed by, or sponsored by Valve Corporation. "Steam" and the Steam logo are trademarks of Valve Corporation._
