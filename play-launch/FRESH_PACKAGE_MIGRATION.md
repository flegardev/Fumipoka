# Fumipoka fresh package migration

The proposed Play package is `com.tiniflegar.fumipoka`. The source previously built as `com.aistudio.returnmark.rmk20`; its `com.example` Kotlin/resource namespace remains internal to the source and does not define the Play application ID. The deleted Console package `com.tiniflegar.returnmark` is a separate identity and is not restored or reused.

## Moving personal records

Android treats the two application IDs as separate apps. Fumipoka cannot read the old installation's private database or preferences directly. Before removing or replacing the old installation, open its **Backup Vault** and export a full JSON backup to a location you control. Install the new Fumipoka package, open **Backup Vault**, choose the saved JSON through the document picker, inspect the import preview, then confirm the restore. Keep the export until the new app's projects and preferences look correct. Imported references to external files still depend on those original files being available. CSV is an export format, not a full restore format.

Room v3, DataStore preference keys, backup schema and import validation remain unchanged. The JSON importer is independent of the Android package ID, so existing backup app/schema identifiers are intentionally preserved for compatibility. Manual export is the user-controlled bridge; launcher widgets and pending reminder/notification state are installation-scoped and should be reviewed/reconfigured after installing the new package.

## Release gate

Play Console reported this package name available on 13 November 2026. Console app creation remains pending the owner review of the required policy and US export attestations. The live privacy URL needs owner review and publication of the matching package statement before a release. No claim is made that package creation, signing, closed testing or publication is complete.
