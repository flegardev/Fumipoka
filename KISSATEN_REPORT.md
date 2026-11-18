> Current launch follow-up (13 November 2026): [release preparation](play-launch/RELEASE_READINESS.md). This report retains the earlier transformation evidence; use play-launch for current artifacts, tests and blockers.

> Repository handoff: source, assets and reviewed QA summaries are prepared for GitHub synchronization on 12 November 2026. The audit below records the preceding local verification run. Its statements about no remote push describe that earlier run. Source publication does not constitute an Android or Play release. Original recovery archives and raw runtime records remain private on the workstation.

# Fumipoka — implementation and verification

Status: **BLOCKED for production**. Source implementation and local QA are reviewable. Owner upload credentials and the published signing identity were not available in these supplied projects; this is not a signed Play release. Branding remains provisional. No publication, repository rename, remote push, key creation/replacement, app uninstall or personal-phone test was performed.

## Real changes

- Removed unused Google-services/secrets build plugins and Firebase/AI/authentication catalog scaffolding. These declarations were not runtime features; no functioning account or cloud service was removed.
- Added a private monotonic timer-recovery snapshot. An interrupted session preserves project, mission, notes, mode and elapsed time and reopens paused for explicit resume. Clock epochs are not compared across reboots, and storage-write failure is surfaced rather than crashing the timer.
- Rebranded visible app, backup filenames, Android widgets and launcher to Fumipoka while preserving com.aistudio.returnmark.rmk20, Room v3, original backup app/schema identifiers, preference keys and production signing configuration.
- Replaced the old stock-photo dashboard with original quiet notebook/café artwork and local licensed Japanese-designed fonts. Added the original adaptive/monochrome/density/store icon family and feature graphic.
- Unified the full palette resolver between Compose and Android widget snapshots, including persisted custom accents and forced black OLED surfaces. Added a regression across every built-in palette, appearance mode and both system-dark states.
- Fixed large-font action layout: home actions wrap as whole buttons, navigation labels stay one line, the title ellipsizes, and the floating Save Point action becomes an icon at large font scales. Theme choices wrap within the available width.
- Rebuilt the native widget hierarchy after real 180x110dp captures revealed clipped headings. Short widgets keep the project title and two separate 48dp actions; larger widgets show context and progress. Resize updates rebind the layout.
- Hardened imports with a 16 MiB UTF-8 byte limit, malformed-UTF-8 rejection and a JSON nesting limit of 64. Document I/O and parsing run off the UI thread. Preserved transactional Room/DataStore rollback. Tightened Android cloud/device-transfer exclusion rules.
- Retained opt-in examples and empty-workspace behavior. Restored missing Gradle launcher/wrapper files from the sibling project with the same wrapper version; no project recreation or database reset.

The family design system and scope are documented in [BRAND_GUIDE](verification/BRAND_GUIDE.md). Original source recovery snapshots and SHA-256 manifests are in `../kissaten/baseline`. Retired unused assets remain recoverable there. These directories arrived without Git metadata; no history was initialized or replaced.

## Verification results

| Check | Result | Evidence |
|---|---|---|
| Latest host build task batch | Exit 0 | [exact command/log metadata](verification/evidence/cloud-cleanup-final.json) |
| Host unit/Robolectric tests | 18 tests, 0 failures/errors | [suite totals](verification/evidence/host-tests.json) |
| Debug Android lint | 0 errors; 81 warnings retained | [individual findings](verification/evidence/lint-summary.json) |
| Device instrumentation | 1 tests; passed=True | [runner result](verification/evidence/instrumentation/KissatenDeviceTest.json) |
| Package, namespace, preference/action identifiers | Preserved | [compatibility audit](verification/evidence/compatibility.json) |
| Release bundle | Built; unsigned=True | [hash, native ELF and signature audit](verification/evidence/artifact-audit.json) |
| Runtime dependency inventory | 84 resolved Maven artifacts | [coordinates, purposes and licenses](verification/evidence/dependency-inventory.json) |
| OSV Maven lookup | 0 returned matches; database coverage is limited | [response summary](verification/evidence/osv-audit.json) |
| Store phone screenshots | 7 genuine selected captures | [provenance](play-store/SCREENSHOT_PROVENANCE.json) |
| process-death | PASS | [JSON](verification/evidence/process-death.json) |
| upgrade-data | PASS | [JSON](verification/evidence/upgrade-data.json) |
| document-flow | PASS | [JSON](verification/evidence/document-flow.json) |
| timer-process-death | PASS | [JSON](verification/evidence/timer-process-death.json) |

Host suite details:

| Suite | Tests | Failures/errors |
|---|---|---|
| com.example.DocumentSafetyTest | 3 | 0 |
| com.example.ExampleRobolectricTest | 8 | 0 |
| com.example.ExampleUnitTest | 1 | 0 |
| com.example.FocusRecoveryTest | 4 | 0 |
| com.example.KissatenContrastTest | 1 | 0 |
| com.example.KissatenPaletteTest | 1 | 0 |

Exact latest full host-check wrapper command (Windows):

```powershell
Set-Location -LiteralPath '<workspace>\returnmark (1)'
& "<workspace>\returnmark (1)\gradlew.bat" --no-daemon --no-configuration-cache --console plain --max-workers=1 -I <workspace>\kissaten\tools\qa.init.gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:bundleRelease -PkissatenUnsignedRelease=true
```

The QA init script reuses this workstation's existing debug certificate and optionally suppresses unavailable release signing for validation only. It never generates or changes a key. Source signing blocks are byte-for-byte unchanged. The Java test runtime is complete Android Studio JBR 25, Robolectric 4.17; compileSdk 37, targetSdk 36 and original minSdk/versionCode are preserved. `--no-configuration-cache`, one worker and nonpersistent daemons avoid the observed build-tool/cache/file-lock failures. Earlier failed build/test logs remain in evidence; only the successful latest batch supports the final build claim.

Device commands from the family workspace:

```powershell
Set-Location -LiteralPath '<workspace>'
python kissaten/tools/device.py install returnmark
python kissaten/tools/instrument.py returnmark com.example.KissatenDeviceTest
python kissaten/tools/matrix.py returnmark
python kissaten/tools/runtime_data.py returnmark
python kissaten/tools/upgrade.py returnmark
```

Device evidence is bounded to `emulator-5554`, boot-completed `HydroNudge_Pixel10a_API37`, Android API 37. Tests use direct install-r/instrument, not a connected Gradle task that might uninstall the app. Resolution/density/font changes simulate several layouts on this one emulator. They do not establish minimum-API, OEM, physical-tablet or production-certificate behavior. See [device matrix](verification/evidence/device-matrix.json), per-run logs and [gallery](verification/GALLERY.html).



Runtime license/notice copies and the inventory are packaged in the final APK/AAB assets as well as the source. The [license audit](verification/research/DEPENDENCY_LICENSES.md) distinguishes Yorulune's GPL-with-Classpath-Exception backport library. The [runtime limits](verification/research/RUNTIME_LIMITS.md) retain the system/display failures and investigation. A successful later test is bounded evidence, not a declaration that those causes are solved.

## Data compatibility

- Version 3, identity `80a459078fad2e2a38feece699d23c58`: JSON equality.

The original compiled APK and transformed debug APK are round-tripped with install-r, with fixture Room row counts/digests and SQLite integrity checks recorded. This verifies source-version fixture compatibility under the workstation QA certificate. It does not prove an update from every historical store version or the owner's production signing key. Supplied older schema exports are incomplete; available migration fixtures are tested without inventing missing historical artifacts. Raw fixture databases/JSON are in private-runtime evidence and excluded from store assets. The existing Hydro UI fixture temporarily replaces emulator Room rows under snapshot/finally-restore; fixture-only document restores also replace or merge emulator records. No adb clear-data action or production/personal database reset occurred.

## Screenshot review

Actual widget rendering exposed a narrow badge column and clipped project title; the revised compact widget shows a one-line title and two separate actions. The first 150% phone capture exposed vertical letter wrapping and an oversized floating action; whole-button wrapping, compact toolbar/FAB and one-line navigation address it. Long project names deliberately ellipsize. A system activity-service hang occurred during display/font configuration changes; original diagnostics and the repeat test are retained.

Use the [gallery](verification/GALLERY.html) for standard/small/large/tablet, 150% font, theme, offline restart and secondary-screen captures actually present. Historical prototypes and old pulled screenshots are excluded. Store captures are 1080x1920 RGB PNGs with source hashes and pixel equality after RGB encoding; no UI compositing or feature simulation. Icon: 512x512 RGB; feature graphic: 1024x500 RGB. See [play-store](play-store) and [asset provenance](play-store/ASSET_PROVENANCE.md).

## Privacy, dependencies and security

Merged debug/release manifests were inspected. Release permissions: `android.permission.POST_NOTIFICATIONS`, `com.aistudio.returnmark.rmk20.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Internet permission is absent. Automatic backup is disabled and cloud/device-transfer rules exclude local app data. Core persistence remains private Room/DataStore. User-created exports are unencrypted; a document provider selected by the user may store them remotely, as disclosed in the app and privacy draft.

No advertising, billing, login, remote AI, cloud database, external analytics or network-client SDK surfaced in the runtime inventory/source inspection. A redacted source-pattern scan found no private-key/token signatures, destructive Room fallback, mutable pending intents or the checked unbounded document-read pattern. This is a bounded audit, not a security certification. [Manifest components](verification/evidence/manifest-audit.json), [source scan](verification/evidence/source-security-scan.json), [licenses](THIRD_PARTY_NOTICES.md) and [original artifact notices](licenses/notice-provenance.json) remain reviewable. Font SIL OFL notices are bundled; source font files are unmodified.

All included 64-bit native ELF LOAD segments meet the measured 16 KiB alignment check. Complete installed-APK/Play validation is a separate release gate. Zero OSV matches means none returned for the queried Maven coordinates, not that vulnerabilities cannot exist. Lint warnings remain visible, including inherited hardcoded-string/deprecation/update recommendations; no lint baseline was added to hide them.

## Remaining release blockers

- Verify owner upload/app-signing certificates, obtain the existing signing credentials securely, choose an eligible increasing versionCode and build/verify the production-signed artifact. Current AAB is unsigned and must not be uploaded as release-ready.
- Complete Google Play/name/logo similarity and jurisdiction-specific trademark clearance. Exact searches and zero returned Apple Slovenia results are limited evidence. See [brand investigation](verification/research/BRAND_CLEARANCE.md).
- Supply legal owner/support details and a controlled public privacy-policy URL; review real Data safety, audience, content rating and all declarations. Draft placeholders are not submission-ready.
- Verify the owner's actual Play Console state, account eligibility and required testing. No testers, opt-ins, feedback, review approval or production access are claimed. Mizupuku additionally needs the health/wellness declaration and the existing account-eligibility question resolved.
- Repeat display/font stress on a stable platform and physical devices. The Hydro input ANR and later Android system-server/display transition failure remain in the evidence; their root cause is not established by this run. Cold-profile successes do not erase them.
- Expand minimum-API/OEM, TalkBack/switch-access, landscape/foldable, extreme text/locale, long-running timer/process death, periodic reminder/Doze/reboot/exact-alarm permission and full historical production-upgrade coverage. Check every relevant secondary screen/palette/widget action before making an exhaustive claim.
- Complete final legal notice/redistribution review and installed APK/Play native-page-size validation. Runtime artifact metadata and source license inventory are prepared, not a legal certification.

No owner-only approval was requested because no publication, signing-identity change or account mutation was attempted. Technical and owner gates keep the final status **BLOCKED**.

## Changed files

Machine-readable [changed-files.json](verification/evidence/changed-files.json) compares each retained source file against the pre-edit SHA-256 manifest, excluding build caches. Retired files are recoverable from the original snapshot.

### changed

- `.env.example`
- `.gitignore`
- `README.md`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/java/com/example/data/BackupVaultManager.kt`
- `app/src/main/java/com/example/data/Entities.kt`
- `app/src/main/java/com/example/data/PreferencesRepository.kt`
- `app/src/main/java/com/example/data/ProjectTemplatesCatalog.kt`
- `app/src/main/java/com/example/data/ReturnmarkDao.kt`
- `app/src/main/java/com/example/data/ReturnmarkDatabase.kt`
- `app/src/main/java/com/example/domain/AmbientSoundSynthesizer.kt`
- `app/src/main/java/com/example/domain/DependencyValidator.kt`
- `app/src/main/java/com/example/domain/ReturnmarkNotificationHelper.kt`
- `app/src/main/java/com/example/ui/DashboardScreen.kt`
- `app/src/main/java/com/example/ui/FocusAndLibraryScreen.kt`
- `app/src/main/java/com/example/ui/MainViewModel.kt`
- `app/src/main/java/com/example/ui/MissionsAndRescueScreen.kt`
- `app/src/main/java/com/example/ui/ProjectsAndSavePointsScreen.kt`
- `app/src/main/java/com/example/ui/SecondaryStudioScreens.kt`
- `app/src/main/java/com/example/ui/theme/Color.kt`
- `app/src/main/java/com/example/ui/theme/Theme.kt`
- `app/src/main/java/com/example/ui/theme/Type.kt`
- `app/src/main/java/com/example/widget/ReturnmarkAppWidgetProviders.kt`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/layout/returnmark_app_widget.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/mipmap-hdpi/ic_launcher.png`
- `app/src/main/res/mipmap-hdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-mdpi/ic_launcher.png`
- `app/src/main/res/mipmap-mdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xhdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `app/src/test/java/com/example/ExampleRobolectricTest.kt`
- `build.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`
### added

- `KISSATEN_REPORT.md`
- `THIRD_PARTY_NOTICES.md`
- `app/src/androidTest/java/com/example/KissatenDeviceTest.kt`
- `app/src/debug/AndroidManifest.xml`
- `app/src/debug/java/com/example/qa/WidgetPreviewActivity.kt`
- `app/src/main/assets/licenses/ShipporiMincho-OFL.txt`
- `app/src/main/assets/licenses/ZenMaruGothic-OFL.txt`
- `app/src/main/assets/third_party/THIRD_PARTY_NOTICES.md`
- `app/src/main/assets/third_party/dependency-inventory.json`
- `app/src/main/assets/third_party/licenses/Apache-2.0.txt`
- `app/src/main/assets/third_party/licenses/artifact-notices/4b9abebc4338048a7c2dc184e9f800deb349366bdf28eb23c2677a77b4c87726.txt`
- `app/src/main/assets/third_party/licenses/artifact-notices/809fa1ed21450f59827d1e9aec720bbc4b687434fa22283c6cb5dd82a47ab9c0.txt`
- `app/src/main/assets/third_party/licenses/notice-provenance.json`
- `app/src/main/java/com/example/data/DocumentSafety.kt`
- `app/src/main/java/com/example/data/FocusRecoveryStore.kt`
- `app/src/main/java/com/example/ui/theme/Kissaten.kt`
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`
- `app/src/main/res/font/shippori_regular.ttf`
- `app/src/main/res/font/zen_maru_bold.ttf`
- `app/src/main/res/font/zen_maru_regular.ttf`
- `app/src/test/java/com/example/DocumentSafetyTest.kt`
- `app/src/test/java/com/example/FocusRecoveryTest.kt`
- `app/src/test/java/com/example/KissatenContrastTest.kt`
- `app/src/test/java/com/example/KissatenPaletteTest.kt`
- `gradle/gradle-daemon-jvm.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew`
- `gradlew.bat`
- `licenses/Apache-2.0.txt`
- `licenses/artifact-notices/4b9abebc4338048a7c2dc184e9f800deb349366bdf28eb23c2677a77b4c87726.txt`
- `licenses/artifact-notices/809fa1ed21450f59827d1e9aec720bbc4b687434fa22283c6cb5dd82a47ab9c0.txt`
- `licenses/notice-provenance.json`
- `play-store/ASSET_PROVENANCE.md`
- `play-store/SCREENSHOT_PROVENANCE.json`
- `play-store/feature-1024x500.png`
- `play-store/icon-512.png`
- `play-store/icon-source.svg`
- `play-store/listing-en.json`
- `play-store/privacy-policy-draft.md`
- `play-store/release-notes-en.txt`
- `play-store/screenshots/phone/01-light-home.png`
- `play-store/screenshots/phone/02-projects.png`
- `play-store/screenshots/phone/03-save-points.png`
- `play-store/screenshots/phone/04-focus.png`
- `play-store/screenshots/phone/05-library.png`
- `play-store/screenshots/phone/06-dark-home.png`
- `play-store/screenshots/phone/07-oled-home.png`
- `play-store/screenshots/phone/README.md`
### retired

- `app/src/main/res/drawable/img_app_icon_fg_1794335149279.jpg`
- `app/src/main/res/drawable/img_hero_checkpoint_1794335149905.jpg`
- `app/src/main/res/drawable/img_kyoto_cafe_hero_1794407456981.jpg`
- `app/src/main/res/font/jetbrains_mono.ttf`
- `app/src/main/res/font/lora.ttf`
- `app/src/main/res/font/plus_jakarta_sans.ttf`
- `app/src/main/res/font/space_grotesk.ttf`
