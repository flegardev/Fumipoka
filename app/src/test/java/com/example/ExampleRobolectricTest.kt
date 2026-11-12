package com.example

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.BackupValidationResult
import com.example.data.BackupVaultManager
import com.example.data.FocusModeType
import com.example.data.FocusSessionEntity
import com.example.data.MissionEntity
import com.example.data.MissionPriority
import com.example.data.MissionStatus
import com.example.data.PreferencesRepository
import com.example.data.ProjectEntity
import com.example.data.ReferenceItemEntity
import com.example.data.ReferenceType
import com.example.data.RescueSessionEntity
import com.example.data.ReturnmarkDatabase
import com.example.data.ReturnmarkThemeMode
import com.example.data.SavePointEntity
import com.example.data.UserPreferencesState
import com.example.domain.DependencyValidator
import com.example.widget.ReturnmarkWidgetUpdater
import com.example.widget.WidgetSnapshotData
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: ReturnmarkDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ReturnmarkDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `manifest disables automatic backup and requests zero internet permission`() {
        assertEquals("Fumipoka", context.getString(R.string.app_name))
        val appInfo = context.applicationInfo
        val allowBackupEnabled = (appInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0
        assertFalse("Android automatic cloud backup must be disabled in manifest", allowBackupEnabled)

        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val requestedPermissions = pkgInfo.requestedPermissions?.toList().orEmpty()
        assertFalse(
            "Returnmark must not request INTERNET permission",
            requestedPermissions.contains(android.Manifest.permission.INTERNET)
        )
    }

    @Test
    fun `empty database stays empty until user creates project or opts into sample workspace`() = runBlocking {
        val dao = db.dao()
        // Fresh database has 0 projects by default (no unwanted auto-seeded demo projects)
        assertEquals(0, dao.getAllProjectsSnapshot().size)

        // User creates a real personal project
        val realProjectId = dao.insertProject(
            ProjectEntity(
                name = "Personal Hardware Synth",
                tagline = "STM32 wavetable oscillator",
                isSampleData = false
            )
        )
        assertEquals(1, dao.getAllProjectsSnapshot().size)

        // User opts into loading example workspace
        ReturnmarkDatabase.loadOptInSampleWorkspace(dao)
        val allProjects = dao.getAllProjectsSnapshot()
        assertEquals(4, allProjects.size)
        assertEquals(3, allProjects.count { it.isSampleData })
        assertEquals(1, allProjects.count { !it.isSampleData })

        // User removes sample projects -> only sample projects are deleted, real project survives!
        val removedCount = dao.deleteSampleProjects()
        assertEquals(3, removedCount)
        val remaining = dao.getAllProjectsSnapshot()
        assertEquals(1, remaining.size)
        assertEquals(realProjectId, remaining.first().id)
        assertEquals("Personal Hardware Synth", remaining.first().name)
    }

    @Test
    fun `non-destructive MIGRATION_2_3 preserves user projects save points and rescue history`() {
        val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // in-memory SQLite DB for migration test
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
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
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `save_points` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `projectId` INTEGER NOT NULL,
                            `missionId` INTEGER,
                            `title` TEXT NOT NULL,
                            `completedSummary` TEXT NOT NULL,
                            `happeningNow` TEXT NOT NULL,
                            `nextAction` TEXT NOT NULL,
                            `notes` TEXT NOT NULL,
                            `blockers` TEXT NOT NULL,
                            `referencesSummary` TEXT NOT NULL,
                            `importantLinks` TEXT NOT NULL,
                            `attachmentsUriList` TEXT NOT NULL,
                            `tags` TEXT NOT NULL,
                            `isPinned` INTEGER NOT NULL,
                            `isArchived` INTEGER NOT NULL,
                            `isQuickCheckpoint` INTEGER NOT NULL,
                            `sessionDurationSeconds` INTEGER NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `rescue_sessions` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `projectId` INTEGER NOT NULL,
                            `whereStopped` TEXT NOT NULL,
                            `whatIsBlocking` TEXT NOT NULL,
                            `smallestNextAction` TEXT NOT NULL,
                            `whatToOpen` TEXT NOT NULL,
                            `plannedWorkMinutes` INTEGER NOT NULL,
                            `createdSavePointId` INTEGER,
                            `timestamp` INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) = Unit
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
        val sqliteDb = helper.writableDatabase

        // Insert v2 user data
        sqliteDb.execSQL(
            """
            INSERT INTO `projects` VALUES (
                7, 'Legacy v2 User Project', 'Tagline', 'Desc', '#CCFF00', 'bookmark',
                'ACTIVE', 1, 'Stopped at v2', 'Done v2', 'Next v2', '', '', 'Blank', 1000, 1000, 1000, 600
            )
            """.trimIndent()
        )
        sqliteDb.execSQL(
            """
            INSERT INTO `save_points` VALUES (
                11, 7, NULL, 'v2 Checkpoint', 'Completed v2', 'Happening v2', 'Next v2',
                'Notes', '', '', '', '', 'v2', 1, 0, 0, 300, 1000
            )
            """.trimIndent()
        )
        sqliteDb.execSQL(
            """
            INSERT INTO `rescue_sessions` VALUES (
                19, 7, 'Where stopped v2', 'Blocker v2', 'Smallest step v2', 'File v2', 15, 11, 1000
            )
            """.trimIndent()
        )

        // Execute non-destructive MIGRATION_2_3
        ReturnmarkDatabase.MIGRATION_2_3.migrate(sqliteDb)

        // Verify v2 project survived and has isSampleData = 0
        sqliteDb.query("SELECT name, isSampleData FROM projects WHERE id = 7").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Legacy v2 User Project", c.getString(0))
            assertEquals(0, c.getInt(1))
        }

        // Verify v2 save_point survived and has attachedReferenceIdsCsv = ''
        sqliteDb.query("SELECT title, attachedReferenceIdsCsv FROM save_points WHERE id = 11").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("v2 Checkpoint", c.getString(0))
            assertEquals("", c.getString(1))
        }

        // Verify v2 rescue_session survived migration
        sqliteDb.query("SELECT smallestNextAction FROM rescue_sessions WHERE id = 19").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Smallest step v2", c.getString(0))
        }

        helper.close()
    }

    @Test
    fun `save points append without overwriting earlier progress and preserve attached reference IDs`() = runBlocking {
        val dao = db.dao()
        val projectId = dao.insertProject(
            ProjectEntity(
                name = "Kernel Compiler",
                tagline = "Register allocator pass",
                currentContextSummary = "Initial state"
            )
        )
        val refId = dao.insertReference(
            ReferenceItemEntity(
                projectId = projectId,
                title = "SSA Dominance Frontier Paper",
                type = ReferenceType.DOCUMENT.name,
                contentOrUri = "content://docs/ssa.pdf"
            )
        )

        val sp1Id = dao.createCheckpointAndSyncProject(
            SavePointEntity(
                projectId = projectId,
                title = "Checkpoint 1: Lexer Done",
                completedSummary = "Token stream validated",
                happeningNow = "Starting AST parser",
                nextAction = "Implement Pratt precedence table",
                attachedReferenceIdsCsv = refId.toString()
            )
        )

        val sp2Id = dao.createCheckpointAndSyncProject(
            SavePointEntity(
                projectId = projectId,
                title = "Checkpoint 2: Pratt Parser Midpoint",
                completedSummary = "Binary operators parsed",
                happeningNow = "Debugging prefix unary minus",
                nextAction = "Add test for nested unary expressions",
                blockers = "Token lookahead off-by-one"
            )
        )

        val allCheckpoints = dao.getAllSavePointsSnapshot()
        assertEquals(2, allCheckpoints.size)
        val sp1Loaded = dao.getSavePointById(sp1Id)
        assertNotNull(sp1Loaded)
        assertEquals(listOf(refId), sp1Loaded?.parsedAttachedReferenceIds())

        val updatedProject = dao.getProjectById(projectId)
        assertEquals("Debugging prefix unary minus", updatedProject?.currentContextSummary)
        assertEquals("Add test for nested unary expressions", updatedProject?.nextManualStep)
        assertEquals("Token lookahead off-by-one", updatedProject?.openBlockers)

        // Restore earlier Checkpoint 1 context and verify Checkpoint 2 is still preserved
        val restored = dao.restoreCheckpointContext(sp1Id)
        assertTrue(restored)
        val afterRestoreProject = dao.getProjectById(projectId)
        assertEquals("Starting AST parser", afterRestoreProject?.currentContextSummary)
        assertEquals("Implement Pratt precedence table", afterRestoreProject?.nextManualStep)
        assertEquals(2, dao.getAllSavePointsSnapshot().size)
    }

    @Test
    fun `circular dependency validator blocks direct and transitive cycles`() {
        val m1 = MissionEntity(id = 1L, projectId = 10L, title = "M1", dependsOnIdsCsv = "2")
        val m2 = MissionEntity(id = 2L, projectId = 10L, title = "M2", dependsOnIdsCsv = "3")
        val m3 = MissionEntity(id = 3L, projectId = 10L, title = "M3", dependsOnIdsCsv = "")
        val m4 = MissionEntity(id = 4L, projectId = 10L, title = "M4", dependsOnIdsCsv = "")
        val list = listOf(m1, m2, m3, m4)

        assertTrue(DependencyValidator.wouldCreateCircularDependency(1L, 1L, list))
        assertTrue(DependencyValidator.wouldCreateCircularDependency(3L, 1L, list))
        assertFalse(DependencyValidator.wouldCreateCircularDependency(3L, 4L, list))
    }

    @Test
    fun `atomic full restore rolls back Room and DataStore if an error occurs mid-restore`() = runBlocking {
        val dao = db.dao()
        val prefsRepo = PreferencesRepository(context)
        prefsRepo.setThemeMode(ReturnmarkThemeMode.FOREST_GREEN)

        val originalProjectId = dao.insertProject(
            ProjectEntity(id = 50L, name = "Original Protected Project", tagline = "Must survive failed restore")
        )
        dao.insertSavePoint(
            SavePointEntity(id = 60L, projectId = originalProjectId, title = "Original Save Point")
        )

        // Build a valid backup payload containing different data
        val backupJson = BackupVaultManager.exportFullBackupJson(
            projects = listOf(ProjectEntity(id = 999L, name = "Replacement Project")),
            savePoints = listOf(SavePointEntity(id = 888L, projectId = 999L, title = "Replacement Save Point")),
            missions = emptyList(),
            references = emptyList(),
            focusSessions = emptyList(),
            rescueSessions = emptyList(),
            preferences = UserPreferencesState(themeMode = ReturnmarkThemeMode.SOFT_IVORY)
        )
        val validation = BackupVaultManager.validateBackupJson(backupJson) as BackupValidationResult.Valid

        // Simulate a failure right after Room replacement before DataStore completes
        val failedResult = BackupVaultManager.executeAtomicFullRestore(
            dao = dao,
            prefsRepo = prefsRepo,
            payload = validation.payload,
            afterRoomBeforePrefsHook = {
                throw IllegalStateException("Simulated mid-restore failure")
            }
        )
        assertTrue(failedResult.isFailure)

        // Verify original Room project and Save Point were rolled back intact!
        val projectsAfterRollback = dao.getAllProjectsSnapshot()
        assertEquals(1, projectsAfterRollback.size)
        assertEquals("Original Protected Project", projectsAfterRollback.first().name)
        assertEquals(1, dao.getAllSavePointsSnapshot().size)
        assertEquals("Original Save Point", dao.getAllSavePointsSnapshot().first().title)
        assertEquals(ReturnmarkThemeMode.FOREST_GREEN, prefsRepo.getSnapshot().themeMode)

        // Now run a clean atomic restore without error and verify both Room and DataStore update
        val successResult = BackupVaultManager.executeAtomicFullRestore(
            dao = dao,
            prefsRepo = prefsRepo,
            payload = validation.payload
        )
        assertTrue(successResult.isSuccess)
        assertEquals("Replacement Project", dao.getAllProjectsSnapshot().first().name)
        assertEquals(ReturnmarkThemeMode.SOFT_IVORY, prefsRepo.getSnapshot().themeMode)
    }

    @Test
    fun `native widget snapshot updater stores and retrieves next manual step and project context`() {
        val snapshot = WidgetSnapshotData(
            activeProjectName = "Solaris Mesh",
            activeContext = "Line 142 PacketAssembler.kt",
            lastSavePointTitle = "BLE 180B Cap",
            nextManualStep = "Run 64KB payload test",
            currentMissionTitle = "Wire retry loop",
            nextMissionTitle = "Benchmark 1000 nodes",
            activeProjectsCount = 3,
            completedMissionsCount = 4,
            totalMissionsCount = 6,
            totalFocusMinutes = 190
        )
        ReturnmarkWidgetUpdater.pushSnapshotAndRefreshAll(context, snapshot)
        val readBack = ReturnmarkWidgetUpdater.readSnapshot(context)
        assertEquals("Solaris Mesh", readBack.activeProjectName)
        assertEquals("Line 142 PacketAssembler.kt", readBack.activeContext)
        assertEquals("Run 64KB payload test", readBack.nextManualStep)
        assertEquals(3, readBack.activeProjectsCount)
    }

    @Test
    fun `returnmark 3_0 pastel and lofi japanese themes appearance cycling and markdown brief export work deterministically`() = runBlocking {
        val prefsRepo = PreferencesRepository(context)
        val lofiJapaneseThemes = listOf(
            ReturnmarkThemeMode.SAKURA_BLUSH,
            ReturnmarkThemeMode.MATCHA_MIST,
            ReturnmarkThemeMode.KYOTO_SKY,
            ReturnmarkThemeMode.LANTERN_AMBER,
            ReturnmarkThemeMode.INDIGO_NIGHT,
            ReturnmarkThemeMode.RAINY_TOKYO,
            ReturnmarkThemeMode.PLUM_INK,
            ReturnmarkThemeMode.BAMBOO_GREEN,
            ReturnmarkThemeMode.SOFT_CREAM,
            ReturnmarkThemeMode.MOONLIGHT_BLUE
        )
        assertEquals(10, ReturnmarkThemeMode.entries.count { it.isLofiJapanese })
        lofiJapaneseThemes.forEach { mode ->
            assertTrue(mode.isLofiJapanese)
            prefsRepo.setThemeMode(mode)
            val snap = prefsRepo.getSnapshot()
            assertEquals(mode, snap.themeMode)
            val tokens = com.example.ui.theme.getPastelPaletteTokens(mode)
            assertNotNull(tokens.lightPrimary)
            assertNotNull(tokens.darkPrimary)
        }

        val signatureThemes = listOf(
            ReturnmarkThemeMode.LAVENDER,
            ReturnmarkThemeMode.SKY_BLUE,
            ReturnmarkThemeMode.MINT,
            ReturnmarkThemeMode.PEACH,
            ReturnmarkThemeMode.ROSE,
            ReturnmarkThemeMode.OCEAN,
            ReturnmarkThemeMode.MIDNIGHT
        )
        signatureThemes.forEach { mode ->
            prefsRepo.setThemeMode(mode)
            val snap = prefsRepo.getSnapshot()
            assertEquals(mode, snap.themeMode)
            val tokens = com.example.ui.theme.getPastelPaletteTokens(mode)
            assertNotNull(tokens.lightPrimary)
            assertNotNull(tokens.darkPrimary)
        }

        // Verify instant appearance mode cycling: LIGHT -> DARK -> OLED -> SYSTEM -> LIGHT
        prefsRepo.setAppearanceMode(com.example.data.ThemeAppearanceMode.LIGHT)
        assertEquals(com.example.data.ThemeAppearanceMode.LIGHT, prefsRepo.getSnapshot().appearanceMode)
        prefsRepo.cycleAppearanceMode()
        assertEquals(com.example.data.ThemeAppearanceMode.DARK, prefsRepo.getSnapshot().appearanceMode)
        prefsRepo.cycleAppearanceMode()
        assertEquals(com.example.data.ThemeAppearanceMode.OLED, prefsRepo.getSnapshot().appearanceMode)
        prefsRepo.cycleAppearanceMode()
        assertEquals(com.example.data.ThemeAppearanceMode.SYSTEM, prefsRepo.getSnapshot().appearanceMode)
        prefsRepo.cycleAppearanceMode()
        assertEquals(com.example.data.ThemeAppearanceMode.LIGHT, prefsRepo.getSnapshot().appearanceMode)

        // Verify Markdown brief export
        val proj = ProjectEntity(
            id = 1L,
            name = "Orbit Synthesis",
            tagline = "Pastel UI & Save Point Engine",
            currentContextSummary = "Completed 10 Lofi Japanese & 7 pastel themes",
            nextManualStep = "Verify Robolectric suite"
        )
        val sp = SavePointEntity(
            id = 10L,
            projectId = 1L,
            title = "Pastel Theme Engine Ready",
            completedSummary = "Light, Dark, and True OLED tokens",
            happeningNow = "Testing Markdown export",
            nextAction = "Run unit tests"
        )
        val mission = MissionEntity(
            id = 20L,
            projectId = 1L,
            title = "Ship Returnmark 3.0",
            status = MissionStatus.IN_PROGRESS.name,
            priority = MissionPriority.HIGH.name
        )
        val markdown = BackupVaultManager.exportProjectMarkdownBrief(
            project = proj,
            savePoints = listOf(sp),
            missions = listOf(mission),
            references = emptyList()
        )
        assertTrue(markdown.contains("# 🔖 Returnmark Brief: Orbit Synthesis"))
        assertTrue(markdown.contains("Verify Robolectric suite"))
        assertTrue(markdown.contains("Pastel Theme Engine Ready"))

        // Verify local notification channel creation succeeds offline
        com.example.domain.ReturnmarkNotificationHelper.ensureChannelsCreated(context)
    }
}

