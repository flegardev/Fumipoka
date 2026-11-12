package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProjectEntity::class,
        SavePointEntity::class,
        MissionEntity::class,
        ReferenceItemEntity::class,
        FocusSessionEntity::class,
        RescueSessionEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class ReturnmarkDatabase : RoomDatabase() {
    abstract fun dao(): ReturnmarkDao

    companion object {
        @Volatile
        private var INSTANCE: ReturnmarkDatabase? = null

        /**
         * Non-destructive migration from version 1 to 2: ensures all v2 tables and indices exist
         * without dropping any pre-existing user records.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `projects` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `tagline` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `isPinned` INTEGER NOT NULL,
                        `currentContextSummary` TEXT NOT NULL,
                        `lastCompletedAction` TEXT NOT NULL,
                        `nextManualStep` TEXT NOT NULL,
                        `openBlockers` TEXT NOT NULL,
                        `milestonesJson` TEXT NOT NULL,
                        `templateSource` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `lastOpenedAt` INTEGER NOT NULL,
                        `totalFocusSeconds` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_projects_status` ON `projects` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_projects_isPinned` ON `projects` (`isPinned`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_projects_lastOpenedAt` ON `projects` (`lastOpenedAt`)")
            }
        }

        /**
         * Non-destructive migration from version 2 to 3:
         * 1. Adds `isSampleData` column to `projects` (default 0) so opt-in demo records are
         *    cleanly separated from user-authored projects.
         * 2. Adds `attachedReferenceIdsCsv` column to `save_points` (default '') so checkpoints
         *    can directly link Knowledge Library references.
         * 3. Upgrades `rescue_sessions` to enforce `FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE CASCADE`
         *    while preserving all valid existing rescue history rows.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `projects` ADD COLUMN `isSampleData` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `save_points` ADD COLUMN `attachedReferenceIdsCsv` TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rescue_sessions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `projectId` INTEGER NOT NULL,
                        `whereStopped` TEXT NOT NULL,
                        `whatIsBlocking` TEXT NOT NULL,
                        `smallestNextAction` TEXT NOT NULL,
                        `whatToOpen` TEXT NOT NULL,
                        `plannedWorkMinutes` INTEGER NOT NULL,
                        `createdSavePointId` INTEGER,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `rescue_sessions_new` (
                        `id`, `projectId`, `whereStopped`, `whatIsBlocking`,
                        `smallestNextAction`, `whatToOpen`, `plannedWorkMinutes`,
                        `createdSavePointId`, `timestamp`
                    )
                    SELECT
                        `id`, `projectId`, `whereStopped`, `whatIsBlocking`,
                        `smallestNextAction`, `whatToOpen`, `plannedWorkMinutes`,
                        `createdSavePointId`, `timestamp`
                    FROM `rescue_sessions`
                    WHERE `projectId` IN (SELECT `id` FROM `projects`)
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `rescue_sessions`")
                db.execSQL("ALTER TABLE `rescue_sessions_new` RENAME TO `rescue_sessions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_rescue_sessions_projectId` ON `rescue_sessions` (`projectId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_rescue_sessions_timestamp` ON `rescue_sessions` (`timestamp`)")
            }
        }

        fun getInstance(context: Context): ReturnmarkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReturnmarkDatabase::class.java,
                    "returnmark_v2.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Explicitly loads opt-in demonstration projects marked with `isSampleData = true`.
         * Never called automatically on app restart when a user has deleted their projects.
         */
        suspend fun loadOptInSampleWorkspace(dao: ReturnmarkDao) {
            // Avoid duplicating sample projects if they are already loaded
            if (dao.getAllProjectsSnapshot().any { it.isSampleData }) return

            val now = System.currentTimeMillis()
            val hour = 3_600_000L
            val day = 24 * hour

            val p1Id = dao.insertProject(
                ProjectEntity(
                    name = "Solaris Offline Mesh Engine",
                    tagline = "Zero-config local peer-to-peer sync protocol in Kotlin",
                    description = "Building an offline-first encrypted mesh frame router with deterministic state reconciliation and zero external servers.",
                    colorHex = "#CCFF00",
                    iconName = "terminal",
                    status = ProjectStatus.ACTIVE.name,
                    isPinned = true,
                    currentContextSummary = "Validating CRC32 frame chunking in PacketAssembler.kt at line 142.",
                    lastCompletedAction = "Finished sliding-window ACK bitmask unit tests.",
                    nextManualStep = "Wire boundary timeout retry into MeshChannelLoop and run 64KB payload test.",
                    openBlockers = "MTU fragmentation on legacy BLE 4.2 controllers needs 180-byte cap.",
                    milestonesJson = ProjectEntity.encodeMilestones(
                        listOf(
                            MilestoneItem("m1", "Frame Wire Format v1", true, "Completed"),
                            MilestoneItem("m2", "Sliding Window ACK Engine", true, "Completed"),
                            MilestoneItem("m3", "BLE + Wi-Fi Aware Transport Bridge", false, "This Week"),
                            MilestoneItem("m4", "Field Benchmark 1,000 Nodes", false, "Next Month")
                        )
                    ),
                    templateSource = "Software Development",
                    createdAt = now - 14 * day,
                    updatedAt = now - 2 * hour,
                    lastOpenedAt = now - 2 * hour,
                    totalFocusSeconds = 11_400L,
                    isSampleData = true
                )
            )

            val p2Id = dao.insertProject(
                ProjectEntity(
                    name = "Atlas of Forgotten Instruments",
                    tagline = "Illustrated monograph on 19th-century acoustic synthesizers",
                    description = "Archival research, patent schematics, and chapter drafts covering mechanical resonators and Helmholtz tuning forks.",
                    colorHex = "#34D399",
                    iconName = "menu_book",
                    status = ProjectStatus.ACTIVE.name,
                    isPinned = true,
                    currentContextSummary = "Drafting Chapter 4: The Telharmonium Dynamo Alternators (section 4.3).",
                    lastCompletedAction = "Transcribed 1897 Cahill patent plates and footnote citations.",
                    nextManualStep = "Cross-check tone-wheel gear ratios against Smithsonian archive Box 14 notes.",
                    openBlockers = "Waiting on high-res scan of Figure 7b harmonic rheostat.",
                    milestonesJson = ProjectEntity.encodeMilestones(
                        listOf(
                            MilestoneItem("w1", "Archive Catalog & Outline", true, "Completed"),
                            MilestoneItem("w2", "Chapters 1–3 Manuscript", true, "Completed"),
                            MilestoneItem("w3", "Chapter 4: Telharmonium", false, "In Progress"),
                            MilestoneItem("w4", "Vector Acoustic Diagrams", false, "Upcoming")
                        )
                    ),
                    templateSource = "Writing Project",
                    createdAt = now - 21 * day,
                    updatedAt = now - 18 * hour,
                    lastOpenedAt = now - 18 * hour,
                    totalFocusSeconds = 8_700L,
                    isSampleData = true
                )
            )

            val p3Id = dao.insertProject(
                ProjectEntity(
                    name = "Nordic Studio Acoustic Treatment",
                    tagline = "Custom birch slat broadband absorbers & bass traps",
                    description = "Designing and building modular rockwool acoustic panels with Helmholtz slot resonators for the main workspace.",
                    colorHex = "#60A5FA",
                    iconName = "architecture",
                    status = ProjectStatus.PAUSED.name,
                    isPinned = false,
                    currentContextSummary = "Cut list finalized for 6 corner bass traps; stopped before milling French cleats.",
                    lastCompletedAction = "Measured room modes at 63Hz and 125Hz with sweep mic.",
                    nextManualStep = "Rip 18mm Baltic birch plywood strips for the 4 wall cleat hangers.",
                    openBlockers = "Need breathable charcoal linen fabric roll from local textile mill.",
                    milestonesJson = ProjectEntity.encodeMilestones(
                        listOf(
                            MilestoneItem("h1", "Room Acoustic Sweep & CAD", true, "Completed"),
                            MilestoneItem("h2", "Lumber & Rockwool Prep", false, "In Progress"),
                            MilestoneItem("h3", "Wall Cleat Installation", false, "Queued")
                        )
                    ),
                    templateSource = "Home Improvement",
                    createdAt = now - 30 * day,
                    updatedAt = now - 5 * day,
                    lastOpenedAt = now - 5 * day,
                    totalFocusSeconds = 5_400L,
                    isSampleData = true
                )
            )

            val m1Id = dao.insertMission(
                MissionEntity(
                    projectId = p1Id,
                    title = "Implement sliding-window ACK bitmask",
                    description = "Bitwise 64-packet window tracker with zero heap allocations inside hot loop.",
                    status = MissionStatus.DONE.name,
                    priority = MissionPriority.HIGH.name,
                    tags = "protocol,core",
                    subtasksRaw = MissionEntity.encodeItems(
                        listOf(
                            SubtaskItem("s1", "Write bitshift ring buffer", true),
                            SubtaskItem("s2", "Verify wrap-around sequence numbers", true)
                        )
                    ),
                    manualOrder = 1,
                    createdAt = now - 4 * day,
                    completedAt = now - 1 * day
                )
            )

            val m2Id = dao.insertMission(
                MissionEntity(
                    projectId = p1Id,
                    title = "Wire boundary timeout retry into MeshChannelLoop",
                    description = "Trigger selective retransmission when ACK bitmask has gaps older than 120ms.",
                    status = MissionStatus.IN_PROGRESS.name,
                    priority = MissionPriority.CRITICAL.name,
                    dueDateMillis = now + 2 * day,
                    tags = "protocol,retry,urgent",
                    subtasksRaw = MissionEntity.encodeItems(
                        listOf(
                            SubtaskItem("s3", "Add monotonic retry deadline array", true),
                            SubtaskItem("s4", "Cap MTU fragmentation to 180 bytes for BLE 4.2", false),
                            SubtaskItem("s5", "Run 64KB payload stress test", false)
                        )
                    ),
                    checklistRaw = MissionEntity.encodeItems(
                        listOf(
                            SubtaskItem("c1", "Zero thread blocking on UI dispatcher", true),
                            SubtaskItem("c2", "Pass Robolectric packet drop test", false)
                        )
                    ),
                    dependsOnIdsCsv = m1Id.toString(),
                    manualOrder = 2,
                    createdAt = now - 2 * day
                )
            )

            dao.insertMission(
                MissionEntity(
                    projectId = p1Id,
                    title = "Benchmark 1,000-node topology simulation",
                    description = "Simulate multi-hop packet relay with 8% synthetic packet loss.",
                    status = MissionStatus.NEXT_UP.name,
                    priority = MissionPriority.MEDIUM.name,
                    dueDateMillis = now + 5 * day,
                    tags = "benchmark,simulation",
                    dependsOnIdsCsv = m2Id.toString(),
                    manualOrder = 3,
                    createdAt = now - 1 * day
                )
            )

            val m4Id = dao.insertMission(
                MissionEntity(
                    projectId = p2Id,
                    title = "Verify tone-wheel gear ratios in Chapter 4.3",
                    description = "Calculate tempered pitch frequencies from the 12-shaft alternator drawings.",
                    status = MissionStatus.NEXT_UP.name,
                    priority = MissionPriority.HIGH.name,
                    dueDateMillis = now + 1 * day,
                    tags = "manuscript,acoustics",
                    subtasksRaw = MissionEntity.encodeItems(
                        listOf(
                            SubtaskItem("s6", "Check Smithsonian Box 14 table", true),
                            SubtaskItem("s7", "Annotate marginalia on page 88", false)
                        )
                    ),
                    manualOrder = 1,
                    createdAt = now - 3 * day
                )
            )

            val ref1Id = dao.insertReference(
                ReferenceItemEntity(
                    projectId = p1Id,
                    title = "Mesh Frame Header 12-Byte Layout",
                    type = ReferenceType.SNIPPET.name,
                    contentOrUri = "[0..1] Magic 0x524D | [2..3] SeqId | [4..5] ChunkIdx/Total | [6..7] Flags | [8..11] CRC32",
                    description = "Exact byte offsets for PacketAssembler.kt zero-copy parser.",
                    folder = "Architecture",
                    category = "Specifications",
                    tags = "binary,protocol,crc32",
                    isFavorite = true,
                    createdAt = now - 5 * day,
                    lastOpenedAt = now - 2 * hour
                )
            )

            val ref2Id = dao.insertReference(
                ReferenceItemEntity(
                    projectId = p1Id,
                    title = "Android BLE GATT MTU Negotiation Guide",
                    type = ReferenceType.LINK.name,
                    contentOrUri = "https://developer.android.com/develop/connectivity/bluetooth/ble/transfer-ble-data",
                    description = "Reference for requestMtu(517) fallback behavior when remote peripheral caps at 185 bytes.",
                    folder = "Connectivity",
                    category = "Documentation",
                    tags = "ble,android,mtu",
                    isFavorite = true,
                    createdAt = now - 4 * day,
                    lastOpenedAt = now - 6 * hour
                )
            )

            val sp1Id = dao.insertSavePoint(
                SavePointEntity(
                    projectId = p1Id,
                    missionId = m1Id,
                    title = "ACK Bitmask Verified & Green",
                    completedSummary = "Implemented 64-slot bitmask ring buffer and passed wrap-around tests.",
                    happeningNow = "Transitioning from unit tests to real MeshChannelLoop integration.",
                    nextAction = "Open PacketAssembler.kt line 142 and wire boundary timeout retry.",
                    notes = "Keep payload header strictly 12 bytes so 180-byte BLE packets have 168 bytes of body.",
                    blockers = "None on core logic; watch BLE 4.2 MTU ceiling.",
                    referencesSummary = "RFC-style Wire Spec Note, PacketAssembler benchmark log",
                    importantLinks = "local://specs/mesh-wire-v1.md",
                    tags = "milestone,protocol",
                    isPinned = true,
                    sessionDurationSeconds = 3_600L,
                    timestamp = now - 26 * hour,
                    attachedReferenceIdsCsv = ref1Id.toString()
                )
            )

            val sp2Id = dao.insertSavePoint(
                SavePointEntity(
                    projectId = p1Id,
                    missionId = m2Id,
                    title = "Mid-debugging BLE 180B Fragmentation",
                    completedSummary = "Added monotonic retry deadline array to MeshChannelLoop.",
                    happeningNow = "Validating CRC32 frame chunking in PacketAssembler.kt at line 142.",
                    nextAction = "Wire boundary timeout retry into MeshChannelLoop and run 64KB payload test.",
                    notes = "Chunk index 0 carries total frame count in upper 16 bits.",
                    blockers = "MTU fragmentation on legacy BLE 4.2 controllers needs 180-byte cap.",
                    referencesSummary = "BLE 4.2 L2CAP MTU Notes",
                    importantLinks = "https://developer.android.com/develop/connectivity/bluetooth/ble/transfer-ble-data",
                    tags = "checkpoint,ble,debug",
                    isPinned = true,
                    sessionDurationSeconds = 2_700L,
                    timestamp = now - 2 * hour,
                    attachedReferenceIdsCsv = "$ref1Id,$ref2Id"
                )
            )

            dao.insertSavePoint(
                SavePointEntity(
                    projectId = p2Id,
                    missionId = m4Id,
                    title = "Paused at Chapter 4.3 Dynamo Alternators",
                    completedSummary = "Transcribed 1897 Cahill patent plates and footnote citations.",
                    happeningNow = "Drafting Chapter 4: The Telharmonium Dynamo Alternators (section 4.3).",
                    nextAction = "Cross-check tone-wheel gear ratios against Smithsonian archive Box 14 notes.",
                    notes = "Paragraph 4 needs a clearer explanation of why inductors were wound on iron teeth.",
                    blockers = "Waiting on high-res scan of Figure 7b harmonic rheostat.",
                    referencesSummary = "US Patent 580,035 Transcript",
                    importantLinks = "local://archive/cahill-1897-notes.txt",
                    tags = "writing,chapter-4",
                    isPinned = false,
                    sessionDurationSeconds = 3_000L,
                    timestamp = now - 18 * hour
                )
            )

            dao.insertFocusSession(
                FocusSessionEntity(
                    projectId = p1Id,
                    missionId = m2Id,
                    mode = FocusModeType.DEEP_WORK.name,
                    plannedDurationSeconds = 3_000L,
                    actualElapsedSeconds = 2_700L,
                    notes = "Traced BLE fragmentation edge case to 180B threshold; saved checkpoint at line 142.",
                    ambientSound = AmbientSoundType.RAIN_CANOPY.name,
                    createdSavePointId = sp2Id,
                    startedAt = now - 3 * hour,
                    endedAt = now - 2 * hour,
                    isCompleted = true
                )
            )

            dao.insertRescueSession(
                RescueSessionEntity(
                    projectId = p3Id,
                    whereStopped = "Stopped after measuring room modes and finalizing the 6-trap cut list.",
                    whatIsBlocking = "Waiting on charcoal linen upholstery roll, which made me stall on the woodwork too.",
                    smallestNextAction = "Rip the 4 French cleat strips from scrap 18mm birch—doesn't require fabric at all.",
                    whatToOpen = "Workshop Cut List Note & Pre-Flight Table Saw Checklist",
                    plannedWorkMinutes = 15,
                    createdSavePointId = sp1Id,
                    timestamp = now - 3 * day
                )
            )
        }
    }
}
