package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPreview(
    val schemaVersion: Int,
    val exportedAt: Long,
    val projectsCount: Int,
    val savePointsCount: Int,
    val missionsCount: Int,
    val referencesCount: Int,
    val focusSessionsCount: Int,
    val rescueSessionsCount: Int,
    val includesPreferences: Boolean,
    val sampleProjectNames: List<String>
)

sealed class BackupValidationResult {
    data class Valid(
        val preview: BackupPreview,
        val payload: ParsedBackupPayload
    ) : BackupValidationResult()

    data class Invalid(val errorMessage: String) : BackupValidationResult()
}

data class ParsedBackupPayload(
    val schemaVersion: Int,
    val exportedAt: Long,
    val projects: List<ProjectEntity>,
    val savePoints: List<SavePointEntity>,
    val missions: List<MissionEntity>,
    val references: List<ReferenceItemEntity>,
    val focusSessions: List<FocusSessionEntity>,
    val rescueSessions: List<RescueSessionEntity>,
    val preferences: UserPreferencesState?
)

object BackupVaultManager {
    const val CURRENT_SCHEMA_VERSION = 3

    fun exportFullBackupJson(
        projects: List<ProjectEntity>,
        savePoints: List<SavePointEntity>,
        missions: List<MissionEntity>,
        references: List<ReferenceItemEntity>,
        focusSessions: List<FocusSessionEntity>,
        rescueSessions: List<RescueSessionEntity>,
        preferences: UserPreferencesState
    ): String {
        val root = JSONObject()
        root.put("app", "Returnmark")
        root.put("schemaVersion", CURRENT_SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("encryption", "NONE_PLAINTEXT_JSON")

        val projectsArray = JSONArray()
        projects.forEach { p ->
            projectsArray.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("tagline", p.tagline)
                    put("description", p.description)
                    put("colorHex", p.colorHex)
                    put("iconName", p.iconName)
                    put("status", p.status)
                    put("isPinned", p.isPinned)
                    put("currentContextSummary", p.currentContextSummary)
                    put("lastCompletedAction", p.lastCompletedAction)
                    put("nextManualStep", p.nextManualStep)
                    put("openBlockers", p.openBlockers)
                    put("milestonesJson", p.milestonesJson)
                    put("templateSource", p.templateSource)
                    put("createdAt", p.createdAt)
                    put("updatedAt", p.updatedAt)
                    put("lastOpenedAt", p.lastOpenedAt)
                    put("totalFocusSeconds", p.totalFocusSeconds)
                    put("isSampleData", p.isSampleData)
                }
            )
        }
        root.put("projects", projectsArray)

        val spArray = JSONArray()
        savePoints.forEach { sp ->
            spArray.put(
                JSONObject().apply {
                    put("id", sp.id)
                    put("projectId", sp.projectId)
                    if (sp.missionId != null) put("missionId", sp.missionId)
                    put("title", sp.title)
                    put("completedSummary", sp.completedSummary)
                    put("happeningNow", sp.happeningNow)
                    put("nextAction", sp.nextAction)
                    put("notes", sp.notes)
                    put("blockers", sp.blockers)
                    put("referencesSummary", sp.referencesSummary)
                    put("importantLinks", sp.importantLinks)
                    put("attachmentsUriList", sp.attachmentsUriList)
                    put("tags", sp.tags)
                    put("isPinned", sp.isPinned)
                    put("isArchived", sp.isArchived)
                    put("isQuickCheckpoint", sp.isQuickCheckpoint)
                    put("sessionDurationSeconds", sp.sessionDurationSeconds)
                    put("timestamp", sp.timestamp)
                    put("attachedReferenceIdsCsv", sp.attachedReferenceIdsCsv)
                }
            )
        }
        root.put("savePoints", spArray)

        val missionsArray = JSONArray()
        missions.forEach { m ->
            missionsArray.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("projectId", m.projectId)
                    put("title", m.title)
                    put("description", m.description)
                    put("status", m.status)
                    put("priority", m.priority)
                    put("customStatusLabel", m.customStatusLabel)
                    if (m.dueDateMillis != null) put("dueDateMillis", m.dueDateMillis)
                    put("tags", m.tags)
                    put("subtasksRaw", m.subtasksRaw)
                    put("checklistRaw", m.checklistRaw)
                    put("dependsOnIdsCsv", m.dependsOnIdsCsv)
                    put("manualOrder", m.manualOrder)
                    put("recurrenceRule", m.recurrenceRule)
                    if (m.linkedSavePointId != null) put("linkedSavePointId", m.linkedSavePointId)
                    put("isArchived", m.isArchived)
                    put("isTemplate", m.isTemplate)
                    put("historyLog", m.historyLog)
                    put("createdAt", m.createdAt)
                    if (m.completedAt != null) put("completedAt", m.completedAt)
                }
            )
        }
        root.put("missions", missionsArray)

        val refsArray = JSONArray()
        references.forEach { r ->
            refsArray.put(
                JSONObject().apply {
                    put("id", r.id)
                    if (r.projectId != null) put("projectId", r.projectId)
                    if (r.savePointId != null) put("savePointId", r.savePointId)
                    put("title", r.title)
                    put("type", r.type)
                    put("contentOrUri", r.contentOrUri)
                    put("description", r.description)
                    put("folder", r.folder)
                    put("category", r.category)
                    put("tags", r.tags)
                    put("isFavorite", r.isFavorite)
                    put("hasPersistableUriAccess", r.hasPersistableUriAccess)
                    put("createdAt", r.createdAt)
                    put("lastOpenedAt", r.lastOpenedAt)
                }
            )
        }
        root.put("references", refsArray)

        val focusArray = JSONArray()
        focusSessions.forEach { f ->
            focusArray.put(
                JSONObject().apply {
                    put("id", f.id)
                    if (f.projectId != null) put("projectId", f.projectId)
                    if (f.missionId != null) put("missionId", f.missionId)
                    put("mode", f.mode)
                    put("plannedDurationSeconds", f.plannedDurationSeconds)
                    put("actualElapsedSeconds", f.actualElapsedSeconds)
                    put("notes", f.notes)
                    put("ambientSound", f.ambientSound)
                    if (f.createdSavePointId != null) put("createdSavePointId", f.createdSavePointId)
                    put("startedAt", f.startedAt)
                    if (f.endedAt != null) put("endedAt", f.endedAt)
                    put("isCompleted", f.isCompleted)
                }
            )
        }
        root.put("focusSessions", focusArray)

        val rescueArray = JSONArray()
        rescueSessions.forEach { rs ->
            rescueArray.put(
                JSONObject().apply {
                    put("id", rs.id)
                    put("projectId", rs.projectId)
                    put("whereStopped", rs.whereStopped)
                    put("whatIsBlocking", rs.whatIsBlocking)
                    put("smallestNextAction", rs.smallestNextAction)
                    put("whatToOpen", rs.whatToOpen)
                    put("plannedWorkMinutes", rs.plannedWorkMinutes)
                    if (rs.createdSavePointId != null) put("createdSavePointId", rs.createdSavePointId)
                    put("timestamp", rs.timestamp)
                }
            )
        }
        root.put("rescueSessions", rescueArray)

        val prefsObj = JSONObject().apply {
            put("themeMode", preferences.themeMode.name)
            put("appearanceMode", preferences.appearanceMode.name)
            put("customAccentHex", preferences.customAccentHex)
            put("layoutDensity", preferences.layoutDensity.name)
            put("fontScale", preferences.fontScale.toDouble())
            put("reducedMotion", preferences.reducedMotion)
            put("showStatistics", preferences.showStatistics)
            put("autoPromptSavePointAfterFocus", preferences.autoPromptSavePointAfterFocus)
            put("focusNotificationsEnabled", preferences.focusNotificationsEnabled)
            put("persistentContextNotification", preferences.persistentContextNotification)
            put("startDestination", preferences.startDestination)
            put("selectedProjectId", preferences.selectedProjectId)
            put("activeLayoutPresetName", preferences.activeLayoutPresetName)
            put("widgetConfigsRaw", preferences.widgetConfigsRaw)
            put("savedLayoutPresetsRaw", preferences.savedLayoutPresetsRaw)
            put("savedSearchesCsv", preferences.savedSearchesCsv)
            put("customTemplatesRaw", preferences.customTemplatesRaw)
        }
        root.put("preferences", prefsObj)

        return root.toString(2)
    }

    fun exportSingleCheckpointJson(savePoint: SavePointEntity, projectName: String): String {
        return JSONObject().apply {
            put("app", "Returnmark")
            put("type", "SavePointExport")
            put("projectName", projectName)
            put("title", savePoint.title)
            put("completedSummary", savePoint.completedSummary)
            put("happeningNow", savePoint.happeningNow)
            put("nextAction", savePoint.nextAction)
            put("notes", savePoint.notes)
            put("blockers", savePoint.blockers)
            put("referencesSummary", savePoint.referencesSummary)
            put("importantLinks", savePoint.importantLinks)
            put("attachedReferenceIdsCsv", savePoint.attachedReferenceIdsCsv)
            put("tags", savePoint.tags)
            put("timestamp", savePoint.timestamp)
        }.toString(2)
    }

    /**
     * Generates a human-readable Markdown Status & Resume Brief for a single project or the entire workspace.
     */
    fun exportProjectMarkdownBrief(
        project: ProjectEntity,
        savePoints: List<SavePointEntity>,
        missions: List<MissionEntity>,
        references: List<ReferenceItemEntity>
    ): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val projSp = savePoints.filter { it.projectId == project.id && !it.isArchived }.sortedByDescending { it.timestamp }
        val projMissions = missions.filter { it.projectId == project.id && !it.isArchived }
        val projRefs = references.filter { it.projectId == project.id }
        val latest = projSp.firstOrNull()

        return buildString {
            appendLine("# 🔖 Returnmark Brief: ${project.name}")
            if (project.tagline.isNotBlank()) appendLine("> ${project.tagline}")
            appendLine()
            appendLine("## 📍 Continue Where I Left Off")
            appendLine("- **Status:** ${project.status}")
            appendLine("- **Where I Stopped:** ${project.currentContextSummary.ifBlank { latest?.happeningNow ?: "N/A" }}")
            appendLine("- **Last Completed Action:** ${project.lastCompletedAction.ifBlank { latest?.completedSummary ?: "N/A" }}")
            appendLine("- **Next Manual Step:** ${project.nextManualStep.ifBlank { latest?.nextAction ?: "N/A" }}")
            if (project.openBlockers.isNotBlank() || !latest?.blockers.isNullOrBlank()) {
                appendLine("- **Open Blockers:** ${project.openBlockers.ifBlank { latest?.blockers ?: "" }}")
            }
            appendLine("- **Total Focus Time:** ${project.totalFocusSeconds / 60} minutes")
            appendLine()
            appendLine("## 🎯 Active & Completed Missions (${projMissions.count { it.status == MissionStatus.DONE.name }}/${projMissions.size})")
            projMissions.forEach { m ->
                val box = if (m.status == MissionStatus.DONE.name) "[x]" else "[ ]"
                appendLine("- $box **${m.title}** (${m.priority} • ${m.status})")
            }
            appendLine()
            appendLine("## 🕒 Recent Save Points (${projSp.size})")
            projSp.take(5).forEach { sp ->
                appendLine("### ${sp.title} (${fmt.format(Date(sp.timestamp))})")
                if (sp.completedSummary.isNotBlank()) appendLine("- **Done:** ${sp.completedSummary}")
                if (sp.happeningNow.isNotBlank()) appendLine("- **Context:** ${sp.happeningNow}")
                if (sp.nextAction.isNotBlank()) appendLine("- **Next:** ${sp.nextAction}")
                if (sp.blockers.isNotBlank()) appendLine("- **Blockers:** ${sp.blockers}")
            }
            if (projRefs.isNotEmpty()) {
                appendLine()
                appendLine("## 📎 Attached References (${projRefs.size})")
                projRefs.forEach { r ->
                    appendLine("- **[${r.type}] ${r.title}:** ${r.contentOrUri}")
                }
            }
        }
    }

    fun validateBackupJson(rawJson: String): BackupValidationResult {
        if (rawJson.isBlank()) {
            return BackupValidationResult.Invalid("Backup JSON is empty. Select or paste a valid Returnmark export file.")
        }
        return try {
            validateDocumentStructure(rawJson)
            val root = JSONObject(rawJson)
            val appName = root.optString("app", "")
            if (appName != "Returnmark") {
                return BackupValidationResult.Invalid("Unrecognized backup header: expected \"app\": \"Returnmark\".")
            }
            val version = root.optInt("schemaVersion", 1)
            if (version < 1 || version > CURRENT_SCHEMA_VERSION) {
                return BackupValidationResult.Invalid("Unsupported backup schemaVersion ($version). Supported versions: 1..$CURRENT_SCHEMA_VERSION.")
            }
            if (!root.has("projects") || !root.has("savePoints")) {
                return BackupValidationResult.Invalid("Backup JSON is missing required 'projects' or 'savePoints' arrays.")
            }

            val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())
            val projectsArr = root.getJSONArray("projects")
            val projects = mutableListOf<ProjectEntity>()
            val projectIds = mutableSetOf<Long>()
            for (i in 0 until projectsArr.length()) {
                val o = projectsArr.getJSONObject(i)
                val id = o.getLong("id")
                val name = o.optString("name", "").trim()
                if (name.isEmpty()) {
                    return BackupValidationResult.Invalid("Project at index $i has an empty name.")
                }
                if (!projectIds.add(id)) {
                    return BackupValidationResult.Invalid("Duplicate project id=$id detected in backup.")
                }
                projects.add(
                    ProjectEntity(
                        id = id,
                        name = name,
                        tagline = o.optString("tagline", ""),
                        description = o.optString("description", ""),
                        colorHex = o.optString("colorHex", "#8B6FE8"),
                        iconName = o.optString("iconName", "bookmark"),
                        status = o.optString("status", ProjectStatus.ACTIVE.name),
                        isPinned = o.optBoolean("isPinned", false),
                        currentContextSummary = o.optString("currentContextSummary", ""),
                        lastCompletedAction = o.optString("lastCompletedAction", ""),
                        nextManualStep = o.optString("nextManualStep", ""),
                        openBlockers = o.optString("openBlockers", ""),
                        milestonesJson = o.optString("milestonesJson", ""),
                        templateSource = o.optString("templateSource", "Blank"),
                        createdAt = o.optLong("createdAt", exportedAt),
                        updatedAt = o.optLong("updatedAt", exportedAt),
                        lastOpenedAt = o.optLong("lastOpenedAt", exportedAt),
                        totalFocusSeconds = o.optLong("totalFocusSeconds", 0L),
                        isSampleData = o.optBoolean("isSampleData", false)
                    )
                )
            }

            val spArr = root.getJSONArray("savePoints")
            val savePoints = mutableListOf<SavePointEntity>()
            for (i in 0 until spArr.length()) {
                val o = spArr.getJSONObject(i)
                val pid = o.getLong("projectId")
                if (!projectIds.contains(pid)) {
                    return BackupValidationResult.Invalid("Save Point '${o.optString("title")}' references missing projectId=$pid.")
                }
                savePoints.add(
                    SavePointEntity(
                        id = o.getLong("id"),
                        projectId = pid,
                        missionId = if (o.has("missionId") && !o.isNull("missionId")) o.getLong("missionId") else null,
                        title = o.optString("title", "Checkpoint"),
                        completedSummary = o.optString("completedSummary", ""),
                        happeningNow = o.optString("happeningNow", ""),
                        nextAction = o.optString("nextAction", ""),
                        notes = o.optString("notes", ""),
                        blockers = o.optString("blockers", ""),
                        referencesSummary = o.optString("referencesSummary", ""),
                        importantLinks = o.optString("importantLinks", ""),
                        attachmentsUriList = o.optString("attachmentsUriList", ""),
                        tags = o.optString("tags", ""),
                        isPinned = o.optBoolean("isPinned", false),
                        isArchived = o.optBoolean("isArchived", false),
                        isQuickCheckpoint = o.optBoolean("isQuickCheckpoint", false),
                        sessionDurationSeconds = o.optLong("sessionDurationSeconds", 0L),
                        timestamp = o.optLong("timestamp", exportedAt),
                        attachedReferenceIdsCsv = o.optString("attachedReferenceIdsCsv", "")
                    )
                )
            }

            val missions = mutableListOf<MissionEntity>()
            val missionsArr = root.optJSONArray("missions") ?: JSONArray()
            for (i in 0 until missionsArr.length()) {
                val o = missionsArr.getJSONObject(i)
                val pid = o.getLong("projectId")
                if (!projectIds.contains(pid)) {
                    return BackupValidationResult.Invalid("Mission '${o.optString("title")}' references missing projectId=$pid.")
                }
                missions.add(
                    MissionEntity(
                        id = o.getLong("id"),
                        projectId = pid,
                        title = o.optString("title", "Mission"),
                        description = o.optString("description", ""),
                        status = o.optString("status", MissionStatus.NEXT_UP.name),
                        priority = o.optString("priority", MissionPriority.MEDIUM.name),
                        customStatusLabel = o.optString("customStatusLabel", ""),
                        dueDateMillis = if (o.has("dueDateMillis") && !o.isNull("dueDateMillis")) o.getLong("dueDateMillis") else null,
                        tags = o.optString("tags", ""),
                        subtasksRaw = o.optString("subtasksRaw", ""),
                        checklistRaw = o.optString("checklistRaw", ""),
                        dependsOnIdsCsv = o.optString("dependsOnIdsCsv", ""),
                        manualOrder = o.optInt("manualOrder", 0),
                        recurrenceRule = o.optString("recurrenceRule", RecurrenceRule.NONE.name),
                        linkedSavePointId = if (o.has("linkedSavePointId") && !o.isNull("linkedSavePointId")) o.getLong("linkedSavePointId") else null,
                        isArchived = o.optBoolean("isArchived", false),
                        isTemplate = o.optBoolean("isTemplate", false),
                        historyLog = o.optString("historyLog", ""),
                        createdAt = o.optLong("createdAt", exportedAt),
                        completedAt = if (o.has("completedAt") && !o.isNull("completedAt")) o.getLong("completedAt") else null
                    )
                )
            }

            val references = mutableListOf<ReferenceItemEntity>()
            val refsArr = root.optJSONArray("references") ?: JSONArray()
            for (i in 0 until refsArr.length()) {
                val o = refsArr.getJSONObject(i)
                references.add(
                    ReferenceItemEntity(
                        id = o.getLong("id"),
                        projectId = if (o.has("projectId") && !o.isNull("projectId")) o.getLong("projectId") else null,
                        savePointId = if (o.has("savePointId") && !o.isNull("savePointId")) o.getLong("savePointId") else null,
                        title = o.optString("title", "Reference"),
                        type = o.optString("type", ReferenceType.LINK.name),
                        contentOrUri = o.optString("contentOrUri", ""),
                        description = o.optString("description", ""),
                        folder = o.optString("folder", "General"),
                        category = o.optString("category", "Core Docs"),
                        tags = o.optString("tags", ""),
                        isFavorite = o.optBoolean("isFavorite", false),
                        hasPersistableUriAccess = o.optBoolean("hasPersistableUriAccess", false),
                        createdAt = o.optLong("createdAt", exportedAt),
                        lastOpenedAt = o.optLong("lastOpenedAt", exportedAt)
                    )
                )
            }

            val focusSessions = mutableListOf<FocusSessionEntity>()
            val focusArr = root.optJSONArray("focusSessions") ?: JSONArray()
            for (i in 0 until focusArr.length()) {
                val o = focusArr.getJSONObject(i)
                focusSessions.add(
                    FocusSessionEntity(
                        id = o.getLong("id"),
                        projectId = if (o.has("projectId") && !o.isNull("projectId")) o.getLong("projectId") else null,
                        missionId = if (o.has("missionId") && !o.isNull("missionId")) o.getLong("missionId") else null,
                        mode = o.optString("mode", FocusModeType.POMODORO.name),
                        plannedDurationSeconds = o.optLong("plannedDurationSeconds", 1500L),
                        actualElapsedSeconds = o.optLong("actualElapsedSeconds", 0L),
                        notes = o.optString("notes", ""),
                        ambientSound = o.optString("ambientSound", AmbientSoundType.NONE.name),
                        createdSavePointId = if (o.has("createdSavePointId") && !o.isNull("createdSavePointId")) o.getLong("createdSavePointId") else null,
                        startedAt = o.optLong("startedAt", exportedAt),
                        endedAt = if (o.has("endedAt") && !o.isNull("endedAt")) o.getLong("endedAt") else null,
                        isCompleted = o.optBoolean("isCompleted", false)
                    )
                )
            }

            val rescueSessions = mutableListOf<RescueSessionEntity>()
            val rescueArr = root.optJSONArray("rescueSessions") ?: JSONArray()
            for (i in 0 until rescueArr.length()) {
                val o = rescueArr.getJSONObject(i)
                val pid = o.getLong("projectId")
                if (!projectIds.contains(pid)) {
                    return BackupValidationResult.Invalid("Rescue Session at index $i references missing projectId=$pid.")
                }
                rescueSessions.add(
                    RescueSessionEntity(
                        id = o.getLong("id"),
                        projectId = pid,
                        whereStopped = o.optString("whereStopped", ""),
                        whatIsBlocking = o.optString("whatIsBlocking", ""),
                        smallestNextAction = o.optString("smallestNextAction", ""),
                        whatToOpen = o.optString("whatToOpen", ""),
                        plannedWorkMinutes = o.optInt("plannedWorkMinutes", 15),
                        createdSavePointId = if (o.has("createdSavePointId") && !o.isNull("createdSavePointId")) o.getLong("createdSavePointId") else null,
                        timestamp = o.optLong("timestamp", exportedAt)
                    )
                )
            }

            val prefsObj = root.optJSONObject("preferences")
            val parsedPrefs = prefsObj?.let { p ->
                val resolvedTheme = ReturnmarkThemeMode.entries.find { it.name == p.optString("themeMode") }
                    ?: ReturnmarkThemeMode.LAVENDER
                val resolvedAppearance = ThemeAppearanceMode.entries.find { it.name == p.optString("appearanceMode") }
                    ?: when {
                        resolvedTheme == ReturnmarkThemeMode.DARK_OLED -> ThemeAppearanceMode.OLED
                        resolvedTheme.isDark -> ThemeAppearanceMode.DARK
                        else -> ThemeAppearanceMode.LIGHT
                    }
                UserPreferencesState(
                    themeMode = resolvedTheme,
                    appearanceMode = resolvedAppearance,
                    customAccentHex = p.optString("customAccentHex", resolvedTheme.previewAccentHex),
                    layoutDensity = LayoutDensityMode.entries.find { it.name == p.optString("layoutDensity") }
                        ?: LayoutDensityMode.SPACIOUS,
                    fontScale = p.optDouble("fontScale", 1.0).toFloat(),
                    reducedMotion = p.optBoolean("reducedMotion", false),
                    showStatistics = p.optBoolean("showStatistics", true),
                    autoPromptSavePointAfterFocus = p.optBoolean("autoPromptSavePointAfterFocus", true),
                    focusNotificationsEnabled = p.optBoolean("focusNotificationsEnabled", true),
                    persistentContextNotification = p.optBoolean("persistentContextNotification", false),
                    startDestination = p.optString("startDestination", "HOME"),
                    selectedProjectId = p.optLong("selectedProjectId", projects.firstOrNull()?.id ?: 1L),
                    activeLayoutPresetName = p.optString("activeLayoutPresetName", "Command Center (All 20)"),
                    widgetConfigsRaw = p.optString("widgetConfigsRaw", ""),
                    savedLayoutPresetsRaw = p.optString("savedLayoutPresetsRaw", ""),
                    savedSearchesCsv = p.optString("savedSearchesCsv", ""),
                    customTemplatesRaw = p.optString("customTemplatesRaw", "")
                )
            }

            val preview = BackupPreview(
                schemaVersion = version,
                exportedAt = exportedAt,
                projectsCount = projects.size,
                savePointsCount = savePoints.size,
                missionsCount = missions.size,
                referencesCount = references.size,
                focusSessionsCount = focusSessions.size,
                rescueSessionsCount = rescueSessions.size,
                includesPreferences = parsedPrefs != null,
                sampleProjectNames = projects.take(5).map { it.name }
            )

            BackupValidationResult.Valid(
                preview = preview,
                payload = ParsedBackupPayload(
                    schemaVersion = version,
                    exportedAt = exportedAt,
                    projects = projects,
                    savePoints = savePoints,
                    missions = missions,
                    references = references,
                    focusSessions = focusSessions,
                    rescueSessions = rescueSessions,
                    preferences = parsedPrefs
                )
            )
        } catch (e: Exception) {
            BackupValidationResult.Invalid("Malformed JSON structure: ${e.localizedMessage ?: "unable to parse backup"}")
        }
    }

    /**
     * Executes an atomic, rollback-protected recovery across BOTH Room SQLite entities
     * and DataStore preferences so partial restores cannot corrupt user state.
     */
    suspend fun executeAtomicFullRestore(
        dao: ReturnmarkDao,
        prefsRepo: PreferencesRepository,
        payload: ParsedBackupPayload,
        afterRoomBeforePrefsHook: (suspend () -> Unit)? = null
    ): Result<Unit> {
        val snapshotProjects = dao.getAllProjectsSnapshot()
        val snapshotSavePoints = dao.getAllSavePointsSnapshot()
        val snapshotMissions = dao.getAllMissionsSnapshot()
        val snapshotRefs = dao.getAllReferencesSnapshot()
        val snapshotFocus = dao.getAllFocusSessionsSnapshot()
        val snapshotRescue = dao.getAllRescueSessionsSnapshot()
        val snapshotPrefs = prefsRepo.getSnapshot()

        return try {
            dao.replaceAllDataTransactionally(
                projects = payload.projects,
                savePoints = payload.savePoints,
                missions = payload.missions,
                references = payload.references,
                focusSessions = payload.focusSessions,
                rescueSessions = payload.rescueSessions
            )
            afterRoomBeforePrefsHook?.invoke()
            if (payload.preferences != null) {
                prefsRepo.restorePreferencesState(payload.preferences)
            }
            Result.success(Unit)
        } catch (restoreError: Throwable) {
            // Roll back both Room and DataStore to pre-restore snapshot
            try {
                dao.replaceAllDataTransactionally(
                    projects = snapshotProjects,
                    savePoints = snapshotSavePoints,
                    missions = snapshotMissions,
                    references = snapshotRefs,
                    focusSessions = snapshotFocus,
                    rescueSessions = snapshotRescue
                )
                prefsRepo.restorePreferencesState(snapshotPrefs)
            } catch (_: Throwable) {
            }
            Result.failure(restoreError)
        }
    }
}
