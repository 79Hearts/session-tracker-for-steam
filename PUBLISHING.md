# Publishing to the Google Play Store — Step by Step

A baby-step walkthrough. Plan on this taking 2–3 hours your first time, plus 1–7 days of waiting for Google to review.

> Cost: **$25 USD one-time fee** to register as a Play Console developer. Everything else is free.

---

## Prerequisites checklist

Before you start, make sure you have:

- [ ] A Google account you'll use as the developer.
- [ ] $25 USD (credit/debit card) for the one-time Play Console fee.
- [ ] A signed release build of the app (we'll cover this in step 4).
- [ ] Your privacy policy hosted at a public URL (we'll cover this in step 3).
- [ ] At least 2 phone screenshots and 1 feature graphic (see [STORE_LISTING.md](STORE_LISTING.md)).

---

## Step 1 — Register a Play Console developer account

1. Go to https://play.google.com/console/signup
2. Sign in with the Google account you want to use.
3. Choose **Personal** account (vs Organization).
4. Verify identity — Google will ask for a government ID. This is now a hard requirement; you cannot publish without it.
5. Pay the $25 registration fee.
6. Wait for verification — usually a few hours, sometimes up to 48.

You only do this once. After that, every app you publish goes through the same Console.

---

## Step 2 — Create the app in Play Console

1. Open https://play.google.com/console
2. Click **Create app** (top right).
3. Fill in:
   - **App name**: `Session Tracker for Steam` (or your chosen name)
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
   - Check both declarations (Developer Program Policies, US export laws).
4. Click **Create app**.

You're now on the app dashboard. The left sidebar lists every step Google requires before the app can be published. Tackle them in roughly this order.

---

## Step 3 — Host the privacy policy

The Play Console verifies the privacy policy URL responds with HTTP 200. Easiest free path: GitHub Pages.

1. Create a free GitHub account if you don't have one: https://github.com/signup
2. Create a new repository — call it whatever, e.g. `steamtimeline`.
3. Upload your project files OR just upload `PRIVACY.md` if you want to keep code private.
4. In repo settings → **Pages**:
   - Source: **Deploy from a branch**
   - Branch: `main`, folder: `/ (root)`
   - Save.
5. Wait ~2 minutes. Your URL will be `https://<username>.github.io/<repo>/PRIVACY` (Markdown gets rendered automatically).
6. **Test the URL in a browser** — it must load.
7. Save that URL — you'll paste it in step 5.

> Alternative: paste the contents of `PRIVACY.md` into a free service like https://app.termly.io/, which generates a hosted URL.

---

## Step 4 — Build a signed release App Bundle (.aab)

The Play Store accepts only Android App Bundles (`.aab`), not raw APKs.

### 4a. Generate a signing key

1. In Android Studio, click ☰ → **Build → Generate Signed App Bundle / APK**.
2. Choose **Android App Bundle** → Next.
3. **Key store path**: click **Create new...**
4. In the dialog:
   - Key store path: pick a folder you'll back up forever (e.g. `Documents/keys/session-tracker.jks`)
   - Password: pick a strong one and **save it somewhere you will not lose**.
   - Key alias: `release`
   - Key password: same or different — also save it.
   - Validity (years): 25 (the minimum Google accepts is 25)
   - Certificate fields: First/last name, organizational unit, organization, city, state, country code (e.g., US). Doesn't have to be accurate, but must be filled in.
5. Click **OK**, then **Next**.

> **WARNING:** Lose this keystore file or its password and you can never publish updates to this app again. Google will let you opt into Play App Signing as a backup — accept it when offered (in Step 5 of the Play Console flow).

### 4b. Build the bundle

1. Build variant: **release**.
2. Signature versions: check both **V1** and **V2** (or V3/V4 if shown).
3. Click **Create**.

The build takes 1–3 minutes. When done, Android Studio shows a notification at the bottom-right with a "locate" link. The file is at:

```
app/release/app-release.aab
```

Save that path — you'll upload it next step.

---

## Step 5 — Fill in the Play Console pages

In the Play Console sidebar, work through every section. The sidebar shows ✅ when each is complete.

### 5a. Main store listing

Sidebar → **Grow → Store presence → Main store listing**.

Paste in everything from [STORE_LISTING.md](STORE_LISTING.md):
- App name
- Short description
- Full description
- App icon (512 × 512 PNG)
- Feature graphic (1024 × 500)
- Phone screenshots (at least 2)

### 5b. Privacy policy

Sidebar → **Policy → App content → Privacy policy**.

Paste your hosted URL from Step 3. Save.

### 5c. App access

Sidebar → **Policy → App content → App access**.

Select **All functionality is available without special access**. Save.

### 5d. Ads

Sidebar → **Policy → App content → Ads**.

Select **No, my app does not contain ads**. Save.

### 5e. Content rating

Sidebar → **Policy → App content → Content rating**.

Run through the questionnaire — answers are in [STORE_LISTING.md](STORE_LISTING.md) → "Content rating questionnaire" section. Submit.

### 5f. Target audience

Sidebar → **Policy → App content → Target audience and content**.

- Age groups: **Ages 13–15** and above (uncheck under-13 boxes).
- Appeal to children: **No**.

### 5g. Data safety

Sidebar → **Policy → App content → Data safety**.

Use the answers from [STORE_LISTING.md](STORE_LISTING.md) → "Data safety form".

### 5h. News app

Pick **No, my app is not a news app**.

### 5i. Government app

Pick **No**.

### 5j. Financial features

Pick **My app doesn't have financial features**.

### 5k. Health

Pick **My app doesn't access health data**.

### 5l. Categorization

- App category: **Tools**
- Tags: pick up to 5 — recommended: **Productivity**, **Utilities**.

### 5m. Country / region

Sidebar → **Production → Countries / regions**. Pick where you want it available — the safe answer is **all countries**.

---

## Step 6 — Upload the bundle and create a release

1. Sidebar → **Production → Create new release**.
2. **Play App Signing**: when prompted, click **Use Play App Signing**. (Google holds a backup of your signing key — protects you if you lose your keystore.)
3. **Upload App Bundle**: drag in `app/release/app-release.aab`.
4. **Release name**: leave the default (it's your version name).
5. **Release notes**: paste the "What's new" text from [STORE_LISTING.md](STORE_LISTING.md).
6. Click **Next → Save → Review release**.
7. Fix any **errors** the Console flags (warnings can be ignored, but errors block submission).
8. Click **Start rollout to Production → Confirm**.

---

## Step 7 — Wait for review

Google reviews every new app. Timing:

| Stage | Typical time |
|---|---|
| **Initial review** (first publish) | 1–7 days, occasionally longer |
| **Updates** to an already-published app | A few hours to 2 days |

You'll get an email when it's approved or rejected. If rejected, the email explains what to fix — usually it's something simple like a missing privacy clause or a screenshot that doesn't reflect the actual UI.

After approval, the listing typically goes live within an hour. Search indexing on the Play Store takes another few hours.

---

## Step 8 — Test the live install

Once it's live:

1. Open the Play Store on your phone.
2. Search for "Session Tracker for Steam" or visit `https://play.google.com/store/apps/details?id=com.sessiontracker.steam`.
3. Tap **Install**.
4. Confirm everything works on a fresh install (no leftover state from your dev build).

---

## Updating the app later

Every time you ship a new version:

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`. (versionCode must be a higher integer than the last release; versionName is human-readable like `1.0.1`.)
2. Build a new signed App Bundle (same keystore, same alias, same passwords).
3. Play Console → **Production → Create new release** → upload, write release notes, roll out.

---

## Troubleshooting

**"App Bundle not signed with Play App Signing key"** — happens if you opt out of Play App Signing or upload an unsigned bundle. Re-build with the right key.

**"Privacy policy URL must be reachable"** — your hosted URL returns 404 or requires a login. Test it in an incognito browser tab.

**"Target API level must be 34 or higher"** (or whatever the current floor is) — bump `targetSdk` in `app/build.gradle.kts`. The current value is 35, which is fine.

**"Foreground service permission requirements not met"** — Google now requires extra justification for foreground services. Our `dataSync` type is allowed for "ongoing user-initiated tasks" — you may need to declare this when prompted. The Console's text walks you through it.

**Rejection because of trademark concerns ("Steam")** — the current name "Session Tracker for Steam" follows the safer "...for Steam" pattern that Valve generally tolerates, and the listing includes a "not affiliated with Valve" disclaimer. If a complaint still arrives, you can rename to a Steam-free name (e.g. "Session Tracker") and resubmit; the applicationId and existing user data are unaffected.
