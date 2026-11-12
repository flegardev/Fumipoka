# Returnmark Changelog

## v3.1.0 — Lofi Whimsical Japanese & Kyoto Café Stationery Aesthetic

### 1. 10 Core Lofi Japanese & Kyoto Café Palettes (100% Free)
- Added 10 atmospheric Japanese stationery & rainy-evening lofi themes alongside all existing pastel and studio themes:
  - **Sakura Blush** (`SAKURA_BLUSH` — muted sakura petals, warm washi cream & gentle plum ink)
  - **Matcha Mist** (`MATCHA_MIST` — whisked uji matcha, tatami sage & morning mist paper)
  - **Kyoto Sky** (`KYOTO_SKY` — faded Kamo river sky blue, paper clouds & twilight slate)
  - **Lantern Amber** (`LANTERN_AMBER` — warm paper lantern glow, roasted cedar wood & café cream)
  - **Indigo Night** (`INDIGO_NIGHT` — rainy-night Kyoto indigo, dusty lavender & warm window light)
  - **Rainy Tokyo** (`RAINY_TOKYO` — charcoal blue-gray alleyways, muted teal & soft neon reflection)
  - **Plum Ink** (`PLUM_INK` — fountain-pen plum ink, dusty wisteria lavender & notebook cream)
  - **Bamboo Green** (`BAMBOO_GREEN` — Arashiyama bamboo grove, mossy stone & warm hinoki wood)
  - **Soft Cream** (`SOFT_CREAM` — vintage Japanese stationery, warm beige washi & sumi brush ink)
  - **Moonlight Blue** (`MOONLIGHT_BLUE` — midnight train ride, moonlight blue & quiet silver mist)
- **Washi Light, Rainy Lofi Dark, True OLED & System Modes**: Every Lofi Japanese palette includes warm paper-textured daylight tokens, deep rainy-night lofi dark tokens, and pure `#000000` True OLED support.
- **Japanese Stationery Editorial Typography & Hero Artwork**:
  - Bundled `Lora` (`res/font/lora.ttf`) for literary notebook display and headline typography paired with `Plus Jakarta Sans` and `JetBrains Mono`.
  - Added custom Kyoto evening café desk & fountain-pen notebook hero artwork (`img_kyoto_cafe_hero_1794407456981.jpg`) and atmospheric empty-state cards.

---

## v3.0.0 — Pastel Redesign, Instant Light/Dark/OLED Switch, Interactive Charts & Local Reminders

### 1. Complete Soft Pastel & Multi-Theme Visual Redesign (100% Free)
- **7 New Signature Pastel Themes**: Added **Lavender**, **Sky Blue**, **Mint**, **Peach**, **Rose**, **Ocean**, and **Midnight** alongside all 7 classic studio & accessibility themes (**Midnight Graphite**, **Soft Ivory**, **Forest Green**, **Electric Lime**, **True Dark OLED**, **Material You**, **High Contrast**).
- **Instant Light / Dark / True OLED / System Switcher**: Added `ThemeAppearanceMode` (`LIGHT`, `DARK`, `OLED`, `SYSTEM`) with a 1-tap switcher directly in the top app bar (`top_bar_appearance_toggle`), a quick selector bar on the Dashboard, and full controls in the Customization Studio. Every color theme adapts seamlessly across bright pastel daylight, low-glare dark slate, and pure `#000000` AMOLED black.
- **Custom Accent Swatches & Smooth Transitions**: Added 12 pastel & vivid accent swatches, `Crossfade` screen transitions (respecting `reducedMotion`), and `animateContentSize()` widget cards.

### 2. Interactive Custom Canvas Charts & Focus Ring
- **Dashboard Momentum & Checkpoint Pulse Card**: Added a custom Jetpack Compose `Canvas` circular completion ring and 7-day Save Point + Focus momentum bar chart directly on the Command Center dashboard.
- **Statistics & Activity Interactive Charts**:
  - **7-Day Focus & Save Point Momentum Bar Chart**: Tap any day to inspect exact Save Points captured and Focus minutes logged.
  - **Project Workload & Save Point Donut Ring Chart**: Multi-segment pastel ring chart visualizing checkpoint and mission distribution across projects.
- **Focus Studio Circular Timer Ring**: Added a glowing pastel sweep-gradient circular progress ring around the monotonic timer readout, plus `-5m` / `+5m` duration adjusters.

### 3. Local Offline Notifications & Markdown Project Brief Export
- **100% Local Notification Helper (`ReturnmarkNotificationHelper`)**:
  - Optional **Focus Session Completion Alerts** with a direct action to capture a Save Point when a timer finishes.
  - Optional **Pinned Active Save-Point Context Notification** in the Android notification shade showing the active project's where-stopped summary and next manual step.
- **Markdown Project Brief Export**: Added a 1-tap **MD Brief** button on the **Continue Where I Left Off** card to export a formatted Markdown summary of any project, its missions, Save Points, and attached references.

---

## v2.0.0 — Reliability, Privacy & Save-Point Recovery Upgrade

### 1. Privacy & Manifest Alignment
- Set `android:allowBackup="false"` in `AndroidManifest.xml` to match Returnmark's published privacy policy.
- Configured explicit `<exclude>` rules in `res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml`.
- Maintained zero `INTERNET`, `ACCESS_NETWORK_STATE`, or broad storage permissions.

### 2. Database Safety, Non-Destructive Migrations & Opt-In Sample Separation
- Enabled Room schema export (`exportSchema = true`) at `app/schemas/com.example.data.ReturnmarkDatabase/3.json`.
- Implemented non-destructive `MIGRATION_1_2` and `MIGRATION_2_3`.
- Separated opt-in example projects (`isSampleData = true`) from genuine user projects so an empty database stays empty on restart.

### 3. Native Backup Vault & Atomic Rollback Recovery
- Added native Android Storage Access Framework (`CreateDocument` / `OpenDocument`) `.json` file export and import.
- Implemented `BackupVaultManager.executeAtomicFullRestore` with pre-restore snapshots and automatic rollback across both Room and DataStore.
