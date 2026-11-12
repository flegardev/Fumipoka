package com.example.ui

import androidx.compose.ui.graphics.toArgb
import android.app.Application
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AmbientSoundType
import com.example.data.BackupValidationResult
import com.example.data.BackupVaultManager
import com.example.data.DashboardWidgetConfig
import com.example.data.FocusModeType
import com.example.data.FocusSessionEntity
import com.example.data.LayoutDensityMode
import com.example.data.MilestoneItem
import com.example.data.MissionEntity
import com.example.data.MissionPriority
import com.example.data.MissionStatus
import com.example.data.PreferencesRepository
import com.example.data.ProjectEntity
import com.example.data.ProjectStatus
import com.example.data.ProjectTemplateModel
import com.example.data.ProjectTemplatesCatalog
import com.example.data.RecurrenceRule
import com.example.data.ReferenceItemEntity
import com.example.data.ReferenceType
import com.example.data.RescueSessionEntity
import com.example.data.ReturnmarkDatabase
import com.example.data.ReturnmarkThemeMode
import com.example.data.SavePointEntity
import com.example.data.SubtaskItem
import com.example.data.ThemeAppearanceMode
import com.example.data.UserPreferencesState
import com.example.domain.AmbientSoundSynthesizer
import com.example.domain.DependencyValidator
import com.example.domain.ReturnmarkNotificationHelper
import com.example.widget.ReturnmarkWidgetUpdater
import com.example.widget.WidgetSnapshotData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PrimaryDestination(val label: String) {
    HOME("Home"),
    PROJECTS("Projects"),
    SAVE_POINTS("Save Points"),
    FOCUS("Focus"),
    LIBRARY("Library")
}

enum class SecondaryDestination(val label: String) {
    NONE("None"),
    MISSIONS("Missions"),
    RESCUE("Rescue Mode"),
    ACTIVITY("Activity"),
    STATISTICS("Statistics"),
    TEMPLATES("Templates"),
    WIDGETS("Widgets"),
    SETTINGS("Customization & Vault")
}

enum class MissionViewMode(val label: String) {
    LIST("List"),
    KANBAN("Kanban"),
    TIMELINE("Timeline"),
    CALENDAR("Calendar"),
    COMPACT("Compact"),
    COMPLETED("Completed")
}

data class ActiveFocusRuntimeState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isZenFullScreen: Boolean = false,
    val mode: FocusModeType = FocusModeType.POMODORO,
    val projectId: Long? = null,
    val missionId: Long? = null,
    val plannedSeconds: Long = 25 * 60L,
    val elapsedSeconds: Long = 0L,
    val sessionNotes: String = "",
    val ambientSound: AmbientSoundType = AmbientSoundType.NONE,
    val showCompletionSavePointPrompt: Boolean = false,
    val startedTimestampMillis: Long = 0L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ReturnmarkDatabase.getInstance(application)
    private val dao = db.dao()
    private val prefsRepo = PreferencesRepository(application)
    private val ambientSynth = AmbientSoundSynthesizer()

    val projects: StateFlow<List<ProjectEntity>> = dao.observeAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savePoints: StateFlow<List<SavePointEntity>> = dao.observeAllSavePoints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missions: StateFlow<List<MissionEntity>> = dao.observeAllMissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val references: StateFlow<List<ReferenceItemEntity>> = dao.observeAllReferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = dao.observeAllFocusSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rescueSessions: StateFlow<List<RescueSessionEntity>> = dao.observeAllRescueSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<UserPreferencesState> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesState())

    // Navigation & UI Dialog State
    private val _primaryDest = MutableStateFlow(PrimaryDestination.HOME)
    val primaryDest: StateFlow<PrimaryDestination> = _primaryDest.asStateFlow()

    private val _secondaryDest = MutableStateFlow(SecondaryDestination.NONE)
    val secondaryDest: StateFlow<SecondaryDestination> = _secondaryDest.asStateFlow()

    private val _missionViewMode = MutableStateFlow(MissionViewMode.LIST)
    val missionViewMode: StateFlow<MissionViewMode> = _missionViewMode.asStateFlow()

    private val _showSavePointSheet = MutableStateFlow(false)
    val showSavePointSheet: StateFlow<Boolean> = _showSavePointSheet.asStateFlow()

    private val _savePointSheetQuickMode = MutableStateFlow(false)
    val savePointSheetQuickMode: StateFlow<Boolean> = _savePointSheetQuickMode.asStateFlow()

    private val _showCommandPalette = MutableStateFlow(false)
    val showCommandPalette: StateFlow<Boolean> = _showCommandPalette.asStateFlow()

    private val _showQuickCaptureDialog = MutableStateFlow(false)
    val showQuickCaptureDialog: StateFlow<Boolean> = _showQuickCaptureDialog.asStateFlow()

    private val _sharedIncomingText = MutableStateFlow<String?>(null)
    val sharedIncomingText: StateFlow<String?> = _sharedIncomingText.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Monotonic Focus Timer State
    private val focusRecovery = com.example.data.FocusRecoveryStore(application)
    private val recoveredFocus = focusRecovery.restore()
    private val _focusState = MutableStateFlow(recoveredFocus?.let { recovered ->
        runCatching {
            val p = recovered.payload
            val mode = FocusModeType.valueOf(p.getString("mode"))
            val planned = p.getLong("planned").coerceIn(0, 180 * 60L)
            val elapsed = (p.getLong("elapsed") + recovered.secondsWhileAway).coerceAtLeast(0)
            ActiveFocusRuntimeState(
                isRunning = p.getBoolean("running"), isPaused = p.getBoolean("running"), mode = mode,
                plannedSeconds = planned,
                elapsedSeconds = if (mode == FocusModeType.STOPWATCH) elapsed else elapsed.coerceAtMost(planned),
                projectId = p.optLong("project").takeIf { it > 0 }, missionId = p.optLong("mission").takeIf { it > 0 },
                sessionNotes = p.optString("notes"), ambientSound = AmbientSoundType.valueOf(p.getString("sound")),
                startedTimestampMillis = p.getLong("started")
            )
        }.getOrDefault(ActiveFocusRuntimeState())
    } ?: ActiveFocusRuntimeState())
    val focusState: StateFlow<ActiveFocusRuntimeState> = _focusState.asStateFlow()
    private var timerJob: Job? = null
    private var lastTickElapsedRealtime: Long = 0L
    private var accumulatedMillis: Long = if (_focusState.value.isRunning) _focusState.value.elapsedSeconds * 1000L else 0L

    // Backup & Restore Vault State
    private val _exportedBackupJson = MutableStateFlow("")
    val exportedBackupJson: StateFlow<String> = _exportedBackupJson.asStateFlow()

    private val _backupValidationState = MutableStateFlow<BackupValidationResult?>(null)
    val backupValidationState: StateFlow<BackupValidationResult?> = _backupValidationState.asStateFlow()

    init {
        ReturnmarkNotificationHelper.ensureChannelsCreated(application)
        viewModelScope.launch {
            // Persist transitions and notes, not four timer ticks every second.
            _focusState.map { it.copy(elapsedSeconds = 0) }.distinctUntilChanged().collect {
                val state = _focusState.value
                val payload = org.json.JSONObject().put("mode", state.mode.name).put("planned", state.plannedSeconds)
                    .put("elapsed", state.elapsedSeconds).put("running", state.isRunning)
                    .put("project", state.projectId ?: 0).put("mission", state.missionId ?: 0)
                    .put("notes", state.sessionNotes).put("sound", state.ambientSound.name)
                    .put("started", state.startedTimestampMillis)
                if (!withContext(Dispatchers.IO) { focusRecovery.save(payload, state.isRunning && !state.isPaused) }) _statusBannerMessage.value = "Could not save timer recovery. Keep the app open until storage is available."
            }
        }
        if (_focusState.value.isPaused) _statusBannerMessage.value = "Recovered your focus session, paused. Resume when you are ready."

        // Keep Native Android Home-Screen Widgets & optional Pinned Context Notification synchronized
        viewModelScope.launch {
            combine(projects, savePoints, missions, focusSessions, preferences) { pList, spList, mList, fList, prefs ->
                val activeProj = pList.find { it.id == prefs.selectedProjectId } ?: pList.firstOrNull()
                val latestSp = spList.firstOrNull { it.projectId == activeProj?.id && !it.isArchived }
                    ?: spList.firstOrNull { !it.isArchived }
                val currentMission = mList.firstOrNull {
                    it.projectId == activeProj?.id && it.status == MissionStatus.IN_PROGRESS.name
                } ?: mList.firstOrNull { it.status != MissionStatus.DONE.name }
                val nextMission = mList.firstOrNull {
                    it.projectId == activeProj?.id && it.status == MissionStatus.NEXT_UP.name
                } ?: currentMission
                val doneCount = mList.count { it.status == MissionStatus.DONE.name }
                val totalFocusMins = (fList.sumOf { it.actualElapsedSeconds } / 60L).toInt()

                val widgetColors = com.example.ui.theme.resolveReturnmarkColors(
                    getApplication<Application>(), prefs.themeMode, prefs.appearanceMode, prefs.customAccentHex,
                    (getApplication<Application>().resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                )
                val snapshot = WidgetSnapshotData(
                    backgroundArgb = widgetColors.surface.toArgb(),
                    inkArgb = widgetColors.onSurface.toArgb(),
                    accentArgb = widgetColors.primary.toArgb(),
                    activeProjectName = activeProj?.name ?: "Fumipoka Workspace",
                    activeContext = activeProj?.currentContextSummary?.ifBlank { activeProj.tagline } ?: "Tap to create or resume a project",
                    lastSavePointTitle = latestSp?.title ?: "No Save Point yet",
                    nextManualStep = activeProj?.nextManualStep?.ifBlank { latestSp?.nextAction ?: "Capture your first Save Point" }
                        ?: "Capture your first Save Point",
                    currentMissionTitle = currentMission?.title ?: "All missions clear",
                    nextMissionTitle = nextMission?.title ?: "Create your next mission",
                    activeProjectsCount = pList.count { it.status == ProjectStatus.ACTIVE.name },
                    completedMissionsCount = doneCount,
                    totalMissionsCount = mList.size,
                    totalFocusMinutes = totalFocusMins
                )

                ReturnmarkNotificationHelper.updatePinnedContextNotification(
                    context = getApplication(),
                    enabled = prefs.persistentContextNotification,
                    projectName = snapshot.activeProjectName,
                    whereStopped = snapshot.activeContext,
                    nextManualStep = snapshot.nextManualStep
                )

                snapshot
            }.collect { snapshot ->
                ReturnmarkWidgetUpdater.pushSnapshotAndRefreshAll(getApplication(), snapshot)
            }
        }
    }

    fun showBanner(message: String) {
        _statusBannerMessage.value = message
    }

    fun clearBanner() {
        _statusBannerMessage.value = null
    }

    // --- Opt-In Sample Data Management ---
    fun loadOptInSampleProjects() {
        viewModelScope.launch {
            ReturnmarkDatabase.loadOptInSampleWorkspace(dao)
            val all = dao.getAllProjectsSnapshot()
            all.firstOrNull()?.let { prefsRepo.setSelectedProjectId(it.id) }
            showBanner("Loaded opt-in demonstration workspace (marked as Sample Data)")
        }
    }

    fun removeSampleProjects() {
        viewModelScope.launch {
            val removed = dao.deleteSampleProjects()
            val remaining = dao.getAllProjectsSnapshot()
            remaining.firstOrNull()?.let { prefsRepo.setSelectedProjectId(it.id) }
            showBanner("Removed $removed sample demonstration projects; user projects preserved")
        }
    }

    // --- Navigation Actions ---
    fun navigatePrimary(dest: PrimaryDestination) {
        _secondaryDest.value = SecondaryDestination.NONE
        _primaryDest.value = dest
    }

    fun navigateSecondary(dest: SecondaryDestination) {
        _secondaryDest.value = dest
    }

    fun closeSecondaryDestination() {
        _secondaryDest.value = SecondaryDestination.NONE
    }

    fun setMissionViewMode(mode: MissionViewMode) {
        _missionViewMode.value = mode
    }

    fun openSavePointSheet(quickMode: Boolean = false) {
        _savePointSheetQuickMode.value = quickMode
        _showSavePointSheet.value = true
    }

    fun closeSavePointSheet() {
        _showSavePointSheet.value = false
    }

    fun setShowCommandPalette(visible: Boolean) {
        _showCommandPalette.value = visible
    }

    fun setShowQuickCaptureDialog(visible: Boolean) {
        _showQuickCaptureDialog.value = visible
    }

    fun handleIncomingShareOrWidgetShortcut(
        sharedText: String?,
        widgetShortcut: String?
    ) {
        if (!sharedText.isNullOrBlank()) {
            _sharedIncomingText.value = sharedText
            _primaryDest.value = PrimaryDestination.LIBRARY
            showBanner("Shared content ready to save in Knowledge Library")
        }
        when (widgetShortcut) {
            "OPEN_CONTINUE" -> navigatePrimary(PrimaryDestination.PROJECTS)
            "OPEN_MISSIONS" -> navigateSecondary(SecondaryDestination.MISSIONS)
            "OPEN_QUICK_SAVE" -> openSavePointSheet(quickMode = true)
            "OPEN_PROJECTS" -> navigatePrimary(PrimaryDestination.PROJECTS)
            "OPEN_FOCUS" -> navigatePrimary(PrimaryDestination.FOCUS)
            "OPEN_STATS" -> navigateSecondary(SecondaryDestination.STATISTICS)
            "OPEN_SAVE_POINTS" -> navigatePrimary(PrimaryDestination.SAVE_POINTS)
            "OPEN_QUICK_CAPTURE" -> _showQuickCaptureDialog.value = true
            "OPEN_RESCUE" -> navigateSecondary(SecondaryDestination.RESCUE)
        }
    }

    fun consumeSharedIncomingText() {
        _sharedIncomingText.value = null
    }

    // --- Project Operations ---
    fun selectActiveProject(projectId: Long) {
        viewModelScope.launch {
            prefsRepo.setSelectedProjectId(projectId)
            val project = dao.getProjectById(projectId)
            if (project != null) {
                dao.updateProject(project.copy(lastOpenedAt = System.currentTimeMillis()))
            }
        }
    }

    fun createProject(
        name: String,
        tagline: String,
        description: String,
        colorHex: String,
        iconName: String,
        template: ProjectTemplateModel = ProjectTemplatesCatalog.builtInTemplates.first()
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val milestones = template.defaultMilestones.mapIndexed { idx, title ->
                MilestoneItem("ms_${now}_$idx", title, isCompleted = false, targetDateLabel = "Phase ${idx + 1}")
            }
            val newProjectId = dao.insertProject(
                ProjectEntity(
                    name = cleanName,
                    tagline = tagline.trim().ifBlank { template.tagline },
                    description = description.trim().ifBlank { template.description },
                    colorHex = colorHex,
                    iconName = iconName,
                    status = ProjectStatus.ACTIVE.name,
                    isPinned = false,
                    currentContextSummary = "Initialized from ${template.name} template.",
                    lastCompletedAction = "Project workspace created.",
                    nextManualStep = template.defaultMissions.firstOrNull() ?: "Capture first Save Point.",
                    milestonesJson = ProjectEntity.encodeMilestones(milestones),
                    templateSource = template.name,
                    createdAt = now,
                    updatedAt = now,
                    lastOpenedAt = now,
                    isSampleData = false
                )
            )
            template.defaultMissions.forEachIndexed { idx, missionTitle ->
                dao.insertMission(
                    MissionEntity(
                        projectId = newProjectId,
                        title = missionTitle,
                        description = "Template mission (${template.name})",
                        status = if (idx == 0) MissionStatus.IN_PROGRESS.name else MissionStatus.NEXT_UP.name,
                        priority = if (idx == 0) MissionPriority.HIGH.name else MissionPriority.MEDIUM.name,
                        manualOrder = idx + 1,
                        createdAt = now
                    )
                )
            }
            dao.insertSavePoint(
                SavePointEntity(
                    projectId = newProjectId,
                    title = "Workspace Initialized: $cleanName",
                    completedSummary = "Created project structure from ${template.name}.",
                    happeningNow = "Ready to begin first mission.",
                    nextAction = template.defaultMissions.firstOrNull() ?: "Define first step.",
                    tags = "kickoff,${template.category.lowercase()}",
                    timestamp = now
                )
            )
            prefsRepo.setSelectedProjectId(newProjectId)
            showBanner("Created project \"$cleanName\"")
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleProjectPin(project: ProjectEntity) {
        viewModelScope.launch {
            dao.updateProject(project.copy(isPinned = !project.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleMilestone(project: ProjectEntity, milestoneId: String) {
        viewModelScope.launch {
            val updatedMilestones = project.parsedMilestones().map {
                if (it.id == milestoneId) it.copy(isCompleted = !it.isCompleted) else it
            }
            dao.updateProject(
                project.copy(
                    milestonesJson = ProjectEntity.encodeMilestones(updatedMilestones),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun addMilestoneToProject(project: ProjectEntity, title: String, targetDateLabel: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val current = project.parsedMilestones().toMutableList()
            current.add(
                MilestoneItem(
                    id = "ms_${System.currentTimeMillis()}",
                    title = title.trim(),
                    isCompleted = false,
                    targetDateLabel = targetDateLabel.trim()
                )
            )
            dao.updateProject(
                project.copy(
                    milestonesJson = ProjectEntity.encodeMilestones(current),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteProjectConfirmed(projectId: Long) {
        viewModelScope.launch {
            dao.deleteProjectById(projectId)
            val remaining = dao.getAllProjectsSnapshot()
            remaining.firstOrNull()?.let { prefsRepo.setSelectedProjectId(it.id) }
            showBanner("Project deleted")
        }
    }

    // --- Save Point Operations ---
    fun createSavePoint(
        projectId: Long,
        missionId: Long? = null,
        title: String,
        completedSummary: String,
        happeningNow: String,
        nextAction: String,
        notes: String = "",
        blockers: String = "",
        referencesSummary: String = "",
        importantLinks: String = "",
        attachmentsUriList: String = "",
        tags: String = "",
        isPinned: Boolean = false,
        isQuickCheckpoint: Boolean = false,
        sessionDurationSeconds: Long = 0L,
        attachedReferenceIdsCsv: String = ""
    ) {
        val cleanTitle = title.trim().ifBlank {
            "Checkpoint @ ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
        }
        viewModelScope.launch {
            dao.createCheckpointAndSyncProject(
                SavePointEntity(
                    projectId = projectId,
                    missionId = missionId,
                    title = cleanTitle,
                    completedSummary = completedSummary.trim(),
                    happeningNow = happeningNow.trim(),
                    nextAction = nextAction.trim(),
                    notes = notes.trim(),
                    blockers = blockers.trim(),
                    referencesSummary = referencesSummary.trim(),
                    importantLinks = importantLinks.trim(),
                    attachmentsUriList = attachmentsUriList.trim(),
                    tags = tags.trim(),
                    isPinned = isPinned,
                    isQuickCheckpoint = isQuickCheckpoint,
                    sessionDurationSeconds = sessionDurationSeconds,
                    timestamp = System.currentTimeMillis(),
                    attachedReferenceIdsCsv = attachedReferenceIdsCsv.trim()
                )
            )
            showBanner("Save Point captured: \"$cleanTitle\"")
        }
    }

    fun toggleSavePointPin(savePoint: SavePointEntity) {
        viewModelScope.launch {
            dao.updateSavePoint(savePoint.copy(isPinned = !savePoint.isPinned))
        }
    }

    fun toggleSavePointArchive(savePoint: SavePointEntity) {
        viewModelScope.launch {
            dao.updateSavePoint(savePoint.copy(isArchived = !savePoint.isArchived))
            showBanner(if (!savePoint.isArchived) "Save Point archived" else "Save Point unarchived")
        }
    }

    fun duplicateSavePoint(savePoint: SavePointEntity) {
        viewModelScope.launch {
            dao.insertSavePoint(
                savePoint.copy(
                    id = 0,
                    title = "${savePoint.title} (Copy)",
                    timestamp = System.currentTimeMillis()
                )
            )
            showBanner("Checkpoint duplicated")
        }
    }

    fun updateSavePointNotesAndMetadata(
        savePoint: SavePointEntity,
        newTitle: String,
        newNextAction: String,
        newNotes: String,
        newBlockers: String,
        newTags: String,
        newAttachedRefIdsCsv: String = savePoint.attachedReferenceIdsCsv
    ) {
        viewModelScope.launch {
            dao.updateSavePoint(
                savePoint.copy(
                    title = newTitle.trim().ifBlank { savePoint.title },
                    nextAction = newNextAction.trim(),
                    notes = newNotes.trim(),
                    blockers = newBlockers.trim(),
                    tags = newTags.trim(),
                    attachedReferenceIdsCsv = newAttachedRefIdsCsv.trim()
                )
            )
            showBanner("Checkpoint updated")
        }
    }

    fun restoreWorkingContextFromSavePoint(savePoint: SavePointEntity) {
        viewModelScope.launch {
            val ok = dao.restoreCheckpointContext(savePoint.id)
            if (ok) {
                prefsRepo.setSelectedProjectId(savePoint.projectId)
                showBanner("Restored working context from \"${savePoint.title}\" (all history preserved)")
            }
        }
    }

    // --- Mission Operations ---
    fun createMission(
        projectId: Long,
        title: String,
        description: String,
        status: MissionStatus = MissionStatus.NEXT_UP,
        priority: MissionPriority = MissionPriority.MEDIUM,
        dueDateMillis: Long? = null,
        tags: String = "",
        subtaskTitles: List<String> = emptyList(),
        checklistTitles: List<String> = emptyList(),
        dependsOnMissionId: Long? = null,
        recurrenceRule: RecurrenceRule = RecurrenceRule.NONE
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val allMissions = dao.getAllMissionsSnapshot()
            val subtasks = subtaskTitles.filter { it.isNotBlank() }.mapIndexed { i, t ->
                SubtaskItem("sub_${now}_$i", t.trim(), false)
            }
            val checklist = checklistTitles.filter { it.isNotBlank() }.mapIndexed { i, t ->
                SubtaskItem("chk_${now}_$i", t.trim(), false)
            }
            val nextOrder = (allMissions.filter { it.projectId == projectId }.maxOfOrNull { it.manualOrder } ?: 0) + 1
            dao.insertMission(
                MissionEntity(
                    projectId = projectId,
                    title = cleanTitle,
                    description = description.trim(),
                    status = status.name,
                    priority = priority.name,
                    dueDateMillis = dueDateMillis,
                    tags = tags.trim(),
                    subtasksRaw = MissionEntity.encodeItems(subtasks),
                    checklistRaw = MissionEntity.encodeItems(checklist),
                    dependsOnIdsCsv = dependsOnMissionId?.toString() ?: "",
                    manualOrder = nextOrder,
                    recurrenceRule = recurrenceRule.name,
                    historyLog = "Created mission",
                    createdAt = now
                )
            )
            showBanner("Mission added: \"$cleanTitle\"")
        }
    }

    fun toggleMissionComplete(mission: MissionEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val markingDone = mission.status != MissionStatus.DONE.name
            val newStatus = if (markingDone) MissionStatus.DONE.name else MissionStatus.IN_PROGRESS.name
            dao.updateMission(
                mission.copy(
                    status = newStatus,
                    completedAt = if (markingDone) now else null,
                    historyLog = "${mission.historyLog}\n• Status -> $newStatus"
                )
            )
            if (markingDone && mission.recurrenceRule != RecurrenceRule.NONE.name) {
                val intervalMillis = when (mission.recurrenceRule) {
                    RecurrenceRule.DAILY.name -> 24 * 3_600_000L
                    RecurrenceRule.WEEKLY.name -> 7 * 24 * 3_600_000L
                    RecurrenceRule.MONTHLY.name -> 30L * 24 * 3_600_000L
                    else -> 0L
                }
                val nextDue = (mission.dueDateMillis ?: now) + intervalMillis
                val resetSubtasks = mission.parsedSubtasks().map { it.copy(isDone = false) }
                val resetChecklist = mission.parsedChecklist().map { it.copy(isDone = false) }
                dao.insertMission(
                    mission.copy(
                        id = 0,
                        status = MissionStatus.NEXT_UP.name,
                        dueDateMillis = nextDue,
                        subtasksRaw = MissionEntity.encodeItems(resetSubtasks),
                        checklistRaw = MissionEntity.encodeItems(resetChecklist),
                        createdAt = now,
                        completedAt = null
                    )
                )
                showBanner("Completed & scheduled next ${mission.recurrenceRule.lowercase()} mission")
            }
        }
    }

    fun updateMissionStatus(mission: MissionEntity, newStatus: MissionStatus) {
        viewModelScope.launch {
            dao.updateMission(
                mission.copy(
                    status = newStatus.name,
                    completedAt = if (newStatus == MissionStatus.DONE) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun toggleMissionSubtask(mission: MissionEntity, subtaskId: String, isChecklist: Boolean = false) {
        viewModelScope.launch {
            if (isChecklist) {
                val updated = mission.parsedChecklist().map {
                    if (it.id == subtaskId) it.copy(isDone = !it.isDone) else it
                }
                dao.updateMission(mission.copy(checklistRaw = MissionEntity.encodeItems(updated)))
            } else {
                val updated = mission.parsedSubtasks().map {
                    if (it.id == subtaskId) it.copy(isDone = !it.isDone) else it
                }
                dao.updateMission(mission.copy(subtasksRaw = MissionEntity.encodeItems(updated)))
            }
        }
    }

    fun addMissionDependencySafely(mission: MissionEntity, candidateDependencyId: Long) {
        viewModelScope.launch {
            val all = dao.getAllMissionsSnapshot()
            if (DependencyValidator.wouldCreateCircularDependency(mission.id, candidateDependencyId, all)) {
                showBanner("Blocked circular dependency: Mission #${candidateDependencyId} already depends on #${mission.id}")
                return@launch
            }
            val updatedDeps = (mission.parsedDependencies() + candidateDependencyId).distinct()
            dao.updateMission(mission.copy(dependsOnIdsCsv = updatedDeps.joinToString(",")))
            showBanner("Dependency linked safely")
        }
    }

    fun duplicateMission(mission: MissionEntity) {
        viewModelScope.launch {
            dao.insertMission(
                mission.copy(
                    id = 0,
                    title = "${mission.title} (Copy)",
                    createdAt = System.currentTimeMillis(),
                    completedAt = null
                )
            )
            showBanner("Mission duplicated")
        }
    }

    fun bulkUpdateMissionStatus(missionIds: Set<Long>, status: MissionStatus) {
        if (missionIds.isEmpty()) return
        viewModelScope.launch {
            val all = dao.getAllMissionsSnapshot()
            all.filter { missionIds.contains(it.id) }.forEach { m ->
                dao.updateMission(
                    m.copy(
                        status = status.name,
                        completedAt = if (status == MissionStatus.DONE) System.currentTimeMillis() else m.completedAt
                    )
                )
            }
            showBanner("Updated ${missionIds.size} missions to ${status.label}")
        }
    }

    fun reorderMission(mission: MissionEntity, delta: Int) {
        viewModelScope.launch {
            dao.updateMission(mission.copy(manualOrder = (mission.manualOrder + delta).coerceAtLeast(1)))
        }
    }

    // --- Rescue Mode 2.0 Operations ---
    fun completeRescueSessionAndStartFocus(
        projectId: Long,
        whereStopped: String,
        whatIsBlocking: String,
        smallestNextAction: String,
        whatToOpen: String,
        plannedWorkMinutes: Int
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val checkpointId = dao.createCheckpointAndSyncProject(
                SavePointEntity(
                    projectId = projectId,
                    title = "Rescue Checkpoint: Restarting Momentum",
                    completedSummary = whereStopped.trim(),
                    happeningNow = "Rescued stalled project via Rescue Mode ($plannedWorkMinutes min sprint).",
                    nextAction = smallestNextAction.trim(),
                    notes = "Open first: ${whatToOpen.trim()}",
                    blockers = whatIsBlocking.trim(),
                    tags = "rescue,recovery",
                    timestamp = now
                )
            )
            dao.insertRescueSession(
                RescueSessionEntity(
                    projectId = projectId,
                    whereStopped = whereStopped.trim(),
                    whatIsBlocking = whatIsBlocking.trim(),
                    smallestNextAction = smallestNextAction.trim(),
                    whatToOpen = whatToOpen.trim(),
                    plannedWorkMinutes = plannedWorkMinutes,
                    createdSavePointId = checkpointId,
                    timestamp = now
                )
            )
            prefsRepo.setSelectedProjectId(projectId)
            configureFocusSession(
                mode = FocusModeType.CUSTOM,
                customMinutes = plannedWorkMinutes.coerceIn(5, 120),
                projectId = projectId,
                missionId = null
            )
            startOrResumeFocusTimer()
            navigatePrimary(PrimaryDestination.FOCUS)
            showBanner("Rescue Session started ($plannedWorkMinutes min sprint)")
        }
    }

    // --- Focus Studio Operations (Monotonic Elapsed Time) ---
    fun configureFocusSession(
        mode: FocusModeType,
        customMinutes: Int = mode.defaultMinutes,
        projectId: Long? = _focusState.value.projectId,
        missionId: Long? = _focusState.value.missionId
    ) {
        pauseFocusTimer()
        accumulatedMillis = 0L
        val plannedSecs = if (mode == FocusModeType.STOPWATCH) 0L else (customMinutes.coerceAtLeast(1) * 60L)
        _focusState.value = _focusState.value.copy(
            isRunning = false,
            isPaused = false,
            mode = mode,
            projectId = projectId,
            missionId = missionId,
            plannedSeconds = plannedSecs,
            elapsedSeconds = 0L,
            showCompletionSavePointPrompt = false
        )
    }

    fun adjustFocusPlannedMinutes(deltaMinutes: Int) {
        val current = _focusState.value
        if (current.mode == FocusModeType.STOPWATCH) return
        val newPlanned = (current.plannedSeconds + deltaMinutes * 60L).coerceIn(5 * 60L, 180 * 60L)
        _focusState.value = current.copy(plannedSeconds = newPlanned)
    }

    fun startOrResumeFocusTimer() {
        val current = _focusState.value
        if (current.isRunning && !current.isPaused) return
        val startWallMillis = if (current.startedTimestampMillis == 0L) System.currentTimeMillis() else current.startedTimestampMillis
        lastTickElapsedRealtime = SystemClock.elapsedRealtime()

        _focusState.value = current.copy(
            isRunning = true,
            elapsedSeconds = if (current.isRunning) current.elapsedSeconds else 0L,
            isPaused = false,
            startedTimestampMillis = startWallMillis,
            showCompletionSavePointPrompt = false
        )
        ambientSynth.setSound(_focusState.value.ambientSound, active = true)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_focusState.value.isRunning && !_focusState.value.isPaused) {
                delay(250L)
                val nowRealtime = SystemClock.elapsedRealtime()
                val delta = (nowRealtime - lastTickElapsedRealtime).coerceAtLeast(0L)
                lastTickElapsedRealtime = nowRealtime
                accumulatedMillis += delta
                val elapsedSecs = accumulatedMillis / 1000L

                val state = _focusState.value
                if (state.mode != FocusModeType.STOPWATCH && state.plannedSeconds > 0 && elapsedSecs >= state.plannedSeconds) {
                    _focusState.value = state.copy(elapsedSeconds = state.plannedSeconds)
                    finishFocusSession(completedNaturally = true)
                    break
                } else {
                    _focusState.value = state.copy(elapsedSeconds = elapsedSecs)
                }
            }
        }
    }

    fun pauseFocusTimer() {
        val current = _focusState.value
        if (!current.isRunning || current.isPaused) return
        val nowRealtime = SystemClock.elapsedRealtime()
        accumulatedMillis += (nowRealtime - lastTickElapsedRealtime).coerceAtLeast(0L)
        timerJob?.cancel()
        ambientSynth.stop()
        _focusState.value = current.copy(
            isPaused = true,
            elapsedSeconds = accumulatedMillis / 1000L
        )
    }

    fun updateFocusNotes(notes: String) {
        _focusState.value = _focusState.value.copy(sessionNotes = notes)
    }

    fun setFocusZenMode(fullScreen: Boolean) {
        _focusState.value = _focusState.value.copy(isZenFullScreen = fullScreen)
    }

    fun setFocusAmbientSound(sound: AmbientSoundType) {
        val running = _focusState.value.isRunning && !_focusState.value.isPaused
        _focusState.value = _focusState.value.copy(ambientSound = sound)
        ambientSynth.setSound(sound, active = running)
    }

    fun finishFocusSession(completedNaturally: Boolean = false) {
        timerJob?.cancel()
        ambientSynth.stop()
        val state = _focusState.value
        val finalElapsed = state.elapsedSeconds.coerceAtLeast(1L)
        val activeProjectId = state.projectId ?: preferences.value.selectedProjectId

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dao.insertFocusSession(
                FocusSessionEntity(
                    projectId = activeProjectId,
                    missionId = state.missionId,
                    mode = state.mode.name,
                    plannedDurationSeconds = state.plannedSeconds,
                    actualElapsedSeconds = finalElapsed,
                    notes = state.sessionNotes.trim(),
                    ambientSound = state.ambientSound.name,
                    startedAt = if (state.startedTimestampMillis > 0) state.startedTimestampMillis else (now - finalElapsed * 1000L),
                    endedAt = now,
                    isCompleted = completedNaturally || finalElapsed >= 60L
                )
            )
            val proj = dao.getProjectById(activeProjectId)
            if (proj != null) {
                dao.updateProject(
                    proj.copy(
                        totalFocusSeconds = proj.totalFocusSeconds + finalElapsed,
                        updatedAt = now
                    )
                )
                if (preferences.value.focusNotificationsEnabled) {
                    ReturnmarkNotificationHelper.notifyFocusSessionCompleted(
                        context = getApplication(),
                        projectName = proj.name,
                        elapsedMinutes = (finalElapsed / 60L).coerceAtLeast(1L),
                        nextManualStep = proj.nextManualStep.ifBlank { "Capture a Save Point" }
                    )
                }
            }
            val shouldPrompt = preferences.value.autoPromptSavePointAfterFocus
            accumulatedMillis = 0L
            _focusState.value = state.copy(
                isRunning = false,
                isPaused = false,
                isZenFullScreen = false,
                showCompletionSavePointPrompt = shouldPrompt,
                startedTimestampMillis = 0L
            )
        }
    }

    fun dismissFocusSavePointPrompt() {
        _focusState.value = _focusState.value.copy(showCompletionSavePointPrompt = false)
    }

    // --- Knowledge Library & References Operations ---
    fun addReferenceItem(
        projectId: Long?,
        savePointId: Long? = null,
        title: String,
        type: ReferenceType,
        contentOrUri: String,
        description: String,
        folder: String,
        category: String,
        tags: String,
        hasPersistableUriAccess: Boolean = false
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dao.insertReference(
                ReferenceItemEntity(
                    projectId = projectId,
                    savePointId = savePointId,
                    title = cleanTitle,
                    type = type.name,
                    contentOrUri = contentOrUri.trim(),
                    description = description.trim(),
                    folder = folder.trim().ifBlank { "General" },
                    category = category.trim().ifBlank { "Core Docs" },
                    tags = tags.trim(),
                    isFavorite = false,
                    hasPersistableUriAccess = hasPersistableUriAccess,
                    createdAt = now,
                    lastOpenedAt = now
                )
            )
            showBanner("Saved to Knowledge Library: \"$cleanTitle\"")
        }
    }

    fun toggleReferenceFavorite(item: ReferenceItemEntity) {
        viewModelScope.launch {
            dao.updateReference(item.copy(isFavorite = !item.isFavorite, lastOpenedAt = System.currentTimeMillis()))
        }
    }

    fun deleteReference(id: Long) {
        viewModelScope.launch {
            dao.deleteReferenceById(id)
            showBanner("Reference removed")
        }
    }

    // --- Quick Capture (Idea, Mission, Reference, or Save Point) ---
    fun quickCaptureEntry(
        captureKind: String,
        title: String,
        details: String,
        projectId: Long
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        when (captureKind) {
            "MISSION" -> createMission(
                projectId = projectId,
                title = cleanTitle,
                description = details,
                status = MissionStatus.NEXT_UP,
                priority = MissionPriority.MEDIUM
            )
            "SAVE_POINT" -> createSavePoint(
                projectId = projectId,
                title = cleanTitle,
                completedSummary = "",
                happeningNow = details,
                nextAction = cleanTitle,
                isQuickCheckpoint = true
            )
            else -> addReferenceItem(
                projectId = projectId,
                title = cleanTitle,
                type = ReferenceType.NOTE,
                contentOrUri = details,
                description = "Captured via Quick Capture",
                folder = "Saved Ideas",
                category = "Quick Capture",
                tags = "idea,quick-capture"
            )
        }
    }

    // --- Customization & Dashboard Widgets ---
    fun setThemeMode(mode: ReturnmarkThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setAppearanceMode(mode: ThemeAppearanceMode) {
        viewModelScope.launch { prefsRepo.setAppearanceMode(mode) }
    }

    fun cycleAppearanceMode() {
        viewModelScope.launch { prefsRepo.cycleAppearanceMode() }
    }

    fun setCustomAccent(hex: String) {
        viewModelScope.launch { prefsRepo.setCustomAccent(hex) }
    }

    fun setLayoutDensity(density: LayoutDensityMode) {
        viewModelScope.launch { prefsRepo.setLayoutDensity(density) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { prefsRepo.setFontScale(scale) }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setReducedMotion(enabled) }
    }

    fun setShowStatistics(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setShowStatistics(enabled) }
    }

    fun setAutoPromptSavePoint(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setAutoPromptSavePoint(enabled) }
    }

    fun setFocusNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setFocusNotificationsEnabled(enabled) }
    }

    fun setPersistentContextNotification(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setPersistentContextNotification(enabled) }
    }

    fun updateDashboardWidgets(configs: List<DashboardWidgetConfig>, presetName: String? = null) {
        viewModelScope.launch {
            prefsRepo.updateWidgetConfigs(configs, presetName)
        }
    }

    fun saveDashboardLayoutPreset(name: String, configs: List<DashboardWidgetConfig>) {
        if (name.isBlank()) return
        viewModelScope.launch {
            prefsRepo.saveNamedLayoutPreset(name.trim(), configs)
            showBanner("Saved dashboard layout preset \"${name.trim()}\"")
        }
    }

    fun saveSearchQuery(query: String) {
        viewModelScope.launch { prefsRepo.addSavedSearch(query) }
    }

    // --- Backup & Restore Vault (with Native SAF Document Picker Export & Import) ---
    private suspend fun buildCurrentBackupJsonString(): String {
        return BackupVaultManager.exportFullBackupJson(
            projects = dao.getAllProjectsSnapshot(),
            savePoints = dao.getAllSavePointsSnapshot(),
            missions = dao.getAllMissionsSnapshot(),
            references = dao.getAllReferencesSnapshot(),
            focusSessions = dao.getAllFocusSessionsSnapshot(),
            rescueSessions = dao.getAllRescueSessionsSnapshot(),
            preferences = prefsRepo.getSnapshot()
        )
    }

    fun generateFullBackupJson() {
        viewModelScope.launch {
            val json = buildCurrentBackupJsonString()
            _exportedBackupJson.value = json
            showBanner("Generated full Fumipoka v3 JSON backup (unencrypted plaintext)")
        }
    }

    fun exportFullBackupToDocumentUri(uri: Uri) {
        viewModelScope.launch {
            val json = buildCurrentBackupJsonString()
            _exportedBackupJson.value = json
            val result = withContext(Dispatchers.IO) {
                try {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                        out.flush()
                    } ?: throw IllegalStateException("Unable to open output stream for selected file")
                    Result.success(Unit)
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
            if (result.isSuccess) {
                showBanner("Saved unencrypted JSON to your chosen document provider")
            } else {
                showBanner("Backup file export failed: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun importAndValidateBackupFromDocumentUri(uri: Uri, onLoadedText: (String) -> Unit = {}) {
        viewModelScope.launch {
            val readResult = withContext(Dispatchers.IO) {
                try {
                    val text = getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        com.example.data.readDocumentText(input)
                    } ?: throw IllegalStateException("Unable to read selected backup file")
                    Result.success(text)
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
            readResult.onSuccess { rawJson ->
                onLoadedText(rawJson)
                _backupValidationState.value = withContext(Dispatchers.Default) { BackupVaultManager.validateBackupJson(rawJson) }
                showBanner("Loaded backup file for validation preview")
            }.onFailure { err ->
                _backupValidationState.value = BackupValidationResult.Invalid(
                    "Failed to read selected file: ${err.localizedMessage ?: "unknown I/O error"}"
                )
            }
        }
    }

    fun validateBackupCandidate(rawJson: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _backupValidationState.value = withContext(Dispatchers.Default) { BackupVaultManager.validateBackupJson(rawJson) }
        }
    }

    fun clearBackupValidation() {
        _backupValidationState.value = null
    }

    fun confirmAndExecuteRestore() {
        val currentValidation = _backupValidationState.value
        if (currentValidation !is BackupValidationResult.Valid) return
        val payload = currentValidation.payload
        viewModelScope.launch {
            val outcome = BackupVaultManager.executeAtomicFullRestore(
                dao = dao,
                prefsRepo = prefsRepo,
                payload = payload
            )
            if (outcome.isSuccess) {
                _backupValidationState.value = null
                showBanner("Restore complete: ${payload.projects.size} projects & ${payload.savePoints.size} Save Points restored")
            } else {
                showBanner("Restore aborted & rolled back safely: ${outcome.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        ambientSynth.stop()
    }
}
