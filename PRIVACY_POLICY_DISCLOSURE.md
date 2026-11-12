# Returnmark 2.0 — Shipped Privacy Policy & Technical Disclosure

**Application ID**: `com.aistudio.returnmark.rmk20`  
**Version**: `2.0.0` (Room Database Schema v3 / Backup Vault Schema v3)

## 1. 100% Local-First & Offline Architecture
- **Zero Network Access**: Returnmark does not request `android.permission.INTERNET` or `android.permission.ACCESS_NETWORK_STATE`. At the Android OS level, the application is incapable of transmitting personal project data, Save Points, missions, references, focus logs, or preferences to any remote server.
- **Zero AI, Zero Telemetry, Zero Ads**: Returnmark contains no generative AI SDKs, no analytics trackers, no crash-reporting telemetry, no advertising SDKs, and no account or subscription gates.

## 2. Android Automatic Backup Discrepancy Resolution
- **Android Auto Backup Disabled**: `AndroidManifest.xml` explicitly sets `android:allowBackup="false"`.
- **Backup & Data Extraction Rules**: Both `res/xml/backup_rules.xml` (`<full-backup-content>`) and `res/xml/data_extraction_rules.xml` (`<cloud-backup>` and `<device-transfer>`) explicitly `<exclude>` the `database`, `sharedpref`, `file`, and `root` domains. Project data is never silently uploaded to cloud backup providers.

## 3. User-Controlled Backup & Restore Vault (Plaintext JSON Warning)
- **Native Document Picker Export & Import**: Users can export and import full backups as `.json` files using the Android Storage Access Framework (`ActivityResultContracts.CreateDocument("application/json")` and `ActivityResultContracts.OpenDocument()`), without granting broad storage permissions.
- **Unencrypted Plaintext Format**: Exported `.json` files (`schemaVersion = 3`) are **unencrypted, human-readable JSON documents** so users retain permanent, tool-independent ownership of their data. Users are warned in the Backup Vault UI to store exported `.json` files in a secure location.
- **Validated & Rollback-Safe Restoration**: Importing a backup file parses and validates schema version, required collections, and foreign-key integrity into a preview summary before modifying any data. Upon explicit user confirmation, Returnmark executes a coordinated restore across Room SQLite and DataStore preferences with automatic rollback to the pre-restore snapshot if any step fails.

## 4. Document & Share-Sheet Permissions
- **System Document Picker**: Attaching external documents in the Knowledge Library uses Android's zero-permission `OpenDocument` contract and requests persistable read URI grants (`FLAG_GRANT_READ_URI_PERMISSION`) only for the specific file selected by the user.
- **System Share Sheet**: Sharing text, links, or media into Returnmark via `Intent.ACTION_SEND` pre-fills the Knowledge Library capture dialog for explicit user review before saving.
