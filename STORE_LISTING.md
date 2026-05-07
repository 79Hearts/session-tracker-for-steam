# Play Store Listing — Session Tracker for Steam

Copy/paste these values into the Google Play Console when you create the listing. Each section corresponds to a field on the "Main store listing" page (Play Console → your app → Grow → Store presence → Main store listing).

---

## App name (max 30 characters)

```
Session Tracker for Steam
```

(Length: 25 chars)

## Short description (max 80 characters)

```
Track your Steam play sessions on a live timeline. 100% local, no ads.
```

(Length: 70 chars)

## Full description (max 4000 characters)

```
Session Tracker for Steam turns your Steam activity into a precise, beautiful timeline.

While the official Steam app shows you total hours played, Session Tracker for Steam records each session — when you started, when you stopped, and exactly how long you played — and visualizes it on a horizontal timeline so you can see your day at a glance.

KEY FEATURES

• Live "Now Playing" card that updates within seconds of launching a game
• Visual timeline of every session, color-coded per game
• Swipe through the last 30 days of history
• Full-screen calendar picker with today highlighted
• Summary view with 3-day, 7-day, 14-day, and 30-day breakdowns
• Per-game total time, session count, and time-share bar chart
• Dark Material 3 theme inspired by Steam's color palette

PRECISE & LOCAL

• Polls every 60 seconds using a battery-friendly foreground service
• All data is stored locally on your device — never uploaded
• 30-day retention with automatic cleanup
• No analytics, no ads, no third-party trackers
• Open Steam Web API only — your data goes to Valve and to nowhere else

HOW IT WORKS

1. Get a free Steam Web API key from steamcommunity.com/dev/apikey
2. Open Session Tracker for Steam → Settings → paste your key and Steam64 ID
3. Set your Steam profile to Public → Game details: Public
4. Start playing — sessions appear automatically

REQUIREMENTS

• Android 8.0 (Oreo) or newer
• Steam profile with public Game Details
• A Steam Web API key (free, takes 2 minutes to register)

NOT AFFILIATED WITH VALVE

This app is an independent project. It is not affiliated with, endorsed by, or sponsored by Valve Corporation. "Steam" and the Steam logo are trademarks of Valve Corporation. Session Tracker for Steam only reads activity that you have chosen to make public via Steam's privacy settings.

PRIVACY

Session Tracker for Steam does not collect personal data. Your Steam credentials and session history live only on your device. See the Privacy Policy linked from this listing for details.
```

(Length: ~1,800 characters — well under the 4,000 limit.)

---

## Application ID (permanent — read carefully)

```
com.sessiontracker.steam
```

This is the unique identifier on the Play Store and **cannot be changed once published**. Already set in `app/build.gradle.kts`. If you decide to change the app's name later, the Play Store name can be edited freely — the ID stays the same.

---

## Category

- **Category**: Tools
- **Tags**: Productivity, Utilities

(Don't pick "Games" — the Play Store reserves that for actual games and you'll get reclassified anyway.)

---

## Contact details

- **Email**: _your email — required and shown publicly_
- **Phone**: optional, leave blank
- **Website**: optional, leave blank or paste a GitHub repo URL

---

## Privacy policy URL

You **must** host the [PRIVACY.md](PRIVACY.md) file at a publicly reachable URL and paste that URL here. Cheapest option:

1. Push this repo to GitHub (free).
2. Either:
   - Use GitHub's raw rendering: `https://github.com/<user>/<repo>/blob/main/PRIVACY.md` (works but not pretty)
   - Or enable **GitHub Pages** (Settings → Pages → Source: main / root): then your URL is `https://<user>.github.io/<repo>/PRIVACY.html` (rename `PRIVACY.md` to `index.md` or use a Jekyll redirect).

The Play Console verifies the URL responds with HTTP 200, so make sure it's reachable.

---

## Graphics

You'll need to create these and upload them — I can't generate images. Required dimensions:

| Asset | Size | Format | Required? |
|---|---|---|---|
| App icon | 512 × 512 px | 32-bit PNG | Yes |
| Feature graphic | 1024 × 500 px | JPG or PNG (no alpha) | Yes |
| Phone screenshots | min 1080 × 1920 px (9:16) | JPG/PNG | Yes — at least 2, max 8 |
| 7" tablet screenshots | optional | JPG/PNG | If you support tablets |
| 10" tablet screenshots | optional | JPG/PNG | If you support tablets |

### Recommended phone screenshots (in order)

1. **Home screen with an active session** — Now Playing card pulsing, timeline bar populated, sessions list below.
2. **Calendar dialog open**, with a few days highlighted and today emphasized.
3. **Summary screen on 7D range**, showing the stacked time-share bar and a couple of games.
4. **A historical day** (e.g., yesterday) with a busy timeline.
5. **Settings screen** showing the test-connection success state.
6. _(optional)_ Welcome / onboarding card the user sees before entering credentials.

### How to capture screenshots

- Plug your phone in.
- Run the app (`▶ Run` in Android Studio).
- Press **Power + Volume Down** simultaneously to take a screenshot. Photos app saves it under "Screenshots".
- Pull screenshots to your PC by dragging them out of File Explorer's MTP view, or use `adb pull /sdcard/Pictures/Screenshots/ ./screenshots`.

### Feature graphic (1024 × 500)

Suggested layout: dark navy background, three colored timeline bars (matching the app's accent palette), and the text "**Session Tracker** — Your Steam sessions, visualized." Easiest tool: [Canva](https://www.canva.com/) (free) — it has a "Custom size" option and Play Store templates. Or use [GIMP](https://www.gimp.org/) / [Figma](https://www.figma.com/) if you prefer.

### App icon

Re-use the in-app launcher icon. Easiest extraction in Android Studio:

1. Right-click `app/src/main/res` → **New → Image Asset**.
2. Icon Type: **Launcher Icons (Adaptive and Legacy)**.
3. Foreground: pick `ic_launcher_foreground.xml`.
4. Background: solid color `#1B2838`.
5. Click **Next → Finish**.
6. The generated PNGs (`mipmap-xxxhdpi/ic_launcher.png` is 192×192) — to get 512×512 for the store listing, run Image Asset Studio a second time using the **Web Icon** option, or scale up `ic_launcher.png` in any image editor.

---

## What's new (release notes)

```
First release of Session Tracker for Steam.

• Live monitoring of your active Steam game with a persistent notification
• Per-day timeline with 30-day swipeable history
• Full-screen calendar picker
• Summary view with 3/7/14/30-day breakdowns and per-game totals
• Material 3 dark theme

Local-only data. No accounts, no ads, no trackers.
```

---

## Content rating questionnaire

When prompted by the Play Console, answer these (the questionnaire takes ~5 minutes):

| Question | Answer |
|---|---|
| Does your app contain violence? | No |
| Sexual content? | No |
| Profanity? | No |
| Drugs / alcohol / tobacco? | No |
| Gambling? | No |
| User-generated content? | No |
| Social features (chat, etc.)? | No |
| Shares user location? | No |
| Allows digital purchases? | No |

Expected rating: **Everyone / 3+**.

---

## Data safety form

The Play Console "Data safety" section is mandatory and asks what data you collect and share. Use these answers:

- **Does your app collect or share any of the required user data types?** → **Yes** (because we collect game activity).
- **Is all of the user data collected by your app encrypted in transit?** → **Yes** (HTTPS to Valve only).
- **Do you provide a way for users to request that their data be deleted?** → **Yes** (uninstalling deletes all on-device data; user can also wipe via Android app info).

For each data type:

| Data type | Collected? | Shared? | Purpose | Required/Optional |
|---|---|---|---|---|
| Personal: name, email, etc. | No | No | — | — |
| Financial info | No | No | — | — |
| Health & fitness | No | No | — | — |
| Messages | No | No | — | — |
| Photos / video / audio | No | No | — | — |
| Files & docs | No | No | — | — |
| Calendar / contacts | No | No | — | — |
| App activity (in-app activity, search history) | **Yes — in-app activity** | No | App functionality | Required |
| Device or other IDs | No | No | — | — |

For "in-app activity": purpose is "App functionality"; data is **processed ephemerally** (sent to Valve, returned, stored only on device, never to our servers).

---

## Target audience

- **Age range**: 13+ (Steam's own minimum age).
- **Designed primarily for children?** No.
- **Appeals to children?** No.

---

## App access

The Play Console asks "Is any part of your app restricted in any way?" If you mark **All functionality is available without restrictions**, Google's reviewers don't need test credentials. That's correct here — without API credentials the app shows the onboarding card; with them, it works.
