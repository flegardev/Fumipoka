package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FocusModeType
import com.example.data.ThemeAppearanceMode
import com.example.ui.CommandPaletteDialog
import com.example.ui.CustomizationAndVaultScreen
import com.example.ui.DashboardScreen
import com.example.ui.FocusStudioScreen
import com.example.ui.LibraryScreen
import com.example.ui.MainViewModel
import com.example.ui.MissionsScreen
import com.example.ui.NativeWidgetsGalleryScreen
import com.example.ui.PrimaryDestination
import com.example.ui.ProjectsScreen
import com.example.ui.QuickCaptureDialog
import com.example.ui.RescueModeScreen
import com.example.ui.SavePointBottomSheet
import com.example.ui.SavePointsScreen
import com.example.ui.SecondaryDestination
import com.example.ui.StatisticsAndActivityScreen
import com.example.ui.TemplatesStudioScreen
import com.example.ui.theme.ReturnmarkTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        inspectIncomingIntent(intent)

        setContent {
            val prefs by viewModel.preferences.collectAsStateWithLifecycle()
            ReturnmarkTheme(
                themeMode = prefs.themeMode,
                appearanceMode = prefs.appearanceMode,
                customAccentHex = prefs.customAccentHex,
                fontScale = prefs.fontScale
            ) {
                ReturnmarkAppRoot(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        inspectIncomingIntent(intent)
    }

    private fun inspectIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val sharedText = if (intent.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)?.toString()
        } else null
        val widgetAction = intent.getStringExtra("widget_shortcut_action")
        if (!sharedText.isNullOrBlank() || !widgetAction.isNullOrBlank()) {
            viewModel.handleIncomingShareOrWidgetShortcut(sharedText, widgetAction)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnmarkAppRoot(viewModel: MainViewModel) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val savePoints by viewModel.savePoints.collectAsStateWithLifecycle()
    val missions by viewModel.missions.collectAsStateWithLifecycle()
    val references by viewModel.references.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val rescueSessions by viewModel.rescueSessions.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val primaryDest by viewModel.primaryDest.collectAsStateWithLifecycle()
    val secondaryDest by viewModel.secondaryDest.collectAsStateWithLifecycle()
    val missionViewMode by viewModel.missionViewMode.collectAsStateWithLifecycle()
    val focusRuntime by viewModel.focusState.collectAsStateWithLifecycle()

    val showSavePointSheet by viewModel.showSavePointSheet.collectAsStateWithLifecycle()
    val savePointQuickMode by viewModel.savePointSheetQuickMode.collectAsStateWithLifecycle()
    val showCommandPalette by viewModel.showCommandPalette.collectAsStateWithLifecycle()
    val showQuickCapture by viewModel.showQuickCaptureDialog.collectAsStateWithLifecycle()
    val sharedIncomingText by viewModel.sharedIncomingText.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val exportedBackupJson by viewModel.exportedBackupJson.collectAsStateWithLifecycle()
    val backupValidation by viewModel.backupValidationState.collectAsStateWithLifecycle()

    if (secondaryDest != SecondaryDestination.NONE) {
        BackHandler { viewModel.closeSecondaryDestination() }
    } else if (primaryDest != PrimaryDestination.HOME) {
        BackHandler { viewModel.navigatePrimary(PrimaryDestination.HOME) }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideExpanded = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    navigationIcon = {
                        if (secondaryDest != SecondaryDestination.NONE) {
                            IconButton(
                                onClick = { viewModel.closeSecondaryDestination() },
                                modifier = Modifier.testTag("back_from_secondary_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = if (secondaryDest != SecondaryDestination.NONE) secondaryDest.label else "Fumipoka",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Your project notebook.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        // Instant 1-Tap Manual Light / Dark / OLED / System Appearance Switch
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.cycleAppearanceMode() }
                                .testTag("top_bar_appearance_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (preferences.appearanceMode) {
                                        ThemeAppearanceMode.LIGHT -> Icons.Default.LightMode
                                        ThemeAppearanceMode.DARK -> Icons.Default.DarkMode
                                        ThemeAppearanceMode.OLED -> Icons.Default.Contrast
                                        ThemeAppearanceMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    },
                                    contentDescription = "Switch Light/Dark/OLED/System Mode",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (androidx.compose.ui.platform.LocalDensity.current.fontScale < 1.3f) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = preferences.appearanceMode.shortBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setShowCommandPalette(true) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("open_command_palette_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search & Command Palette")
                        }
                        IconButton(
                            onClick = { viewModel.navigateSecondary(SecondaryDestination.SETTINGS) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("open_settings_vault_button")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Customization & Backup Vault")
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openSavePointSheet(quickMode = false) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    expanded = androidx.compose.ui.platform.LocalDensity.current.fontScale < 1.3f && maxWidth >= 400.dp,
                    icon = { Icon(Icons.Default.BookmarkAdded, contentDescription = "Save Point") },
                    text = { Text("Save Point", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_save_point")
                )
            },
            bottomBar = {
                if (!isWideExpanded) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        PrimaryDestination.entries.forEach { dest ->
                            val selected = secondaryDest == SecondaryDestination.NONE && primaryDest == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigatePrimary(dest) },
                                icon = { Icon(dest.icon(), contentDescription = dest.label) },
                                label = { Text(dest.label, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                modifier = Modifier.testTag("nav_tab_${dest.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideExpanded) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        PrimaryDestination.entries.forEach { dest ->
                            val selected = secondaryDest == SecondaryDestination.NONE && primaryDest == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigatePrimary(dest) },
                                icon = { Icon(dest.icon(), contentDescription = dest.label) },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("nav_rail_${dest.name.lowercase()}")
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    bannerMessage?.let { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("status_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearBanner() }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }

                    val screenRouteKey = if (secondaryDest != SecondaryDestination.NONE) {
                        "SEC_${secondaryDest.name}"
                    } else {
                        "PRI_${primaryDest.name}"
                    }
                    val animDuration = if (preferences.reducedMotion) 0 else 200

                    Crossfade(
                        targetState = screenRouteKey,
                        animationSpec = tween(durationMillis = animDuration),
                        label = "returnmark_screen_transition"
                    ) { routeKey ->
                        when (routeKey) {
                            "SEC_MISSIONS" -> {
                                MissionsScreen(
                                    projects = projects,
                                    selectedProjectId = preferences.selectedProjectId,
                                    missions = missions,
                                    viewMode = missionViewMode,
                                    onChangeViewMode = viewModel::setMissionViewMode,
                                    onCreateMission = viewModel::createMission,
                                    onToggleComplete = viewModel::toggleMissionComplete,
                                    onUpdateStatus = viewModel::updateMissionStatus,
                                    onToggleSubtask = viewModel::toggleMissionSubtask,
                                    onAddDependencySafely = viewModel::addMissionDependencySafely,
                                    onDuplicateMission = viewModel::duplicateMission,
                                    onBulkUpdateStatus = viewModel::bulkUpdateMissionStatus,
                                    onReorderMission = viewModel::reorderMission,
                                    onStartFocusForMission = { mission ->
                                        viewModel.configureFocusSession(
                                            mode = FocusModeType.POMODORO,
                                            projectId = mission.projectId,
                                            missionId = mission.id
                                        )
                                        viewModel.navigatePrimary(PrimaryDestination.FOCUS)
                                    }
                                )
                            }

                            "SEC_RESCUE" -> {
                                RescueModeScreen(
                                    projects = projects,
                                    selectedProjectId = preferences.selectedProjectId,
                                    savePoints = savePoints,
                                    missions = missions,
                                    references = references,
                                    rescueHistory = rescueSessions,
                                    onSelectProject = viewModel::selectActiveProject,
                                    onLaunchRescueFocusSession = viewModel::completeRescueSessionAndStartFocus
                                )
                            }

                            "SEC_TEMPLATES" -> {
                                TemplatesStudioScreen(
                                    onApplyTemplateToNewProject = { tpl, name ->
                                        viewModel.createProject(
                                            name = name,
                                            tagline = tpl.tagline,
                                            description = tpl.description,
                                            colorHex = tpl.colorHex,
                                            iconName = tpl.iconName,
                                            template = tpl
                                        )
                                        viewModel.navigatePrimary(PrimaryDestination.PROJECTS)
                                    },
                                    onShowBanner = viewModel::showBanner
                                )
                            }

                            "SEC_STATISTICS", "SEC_ACTIVITY" -> {
                                StatisticsAndActivityScreen(
                                    projects = projects,
                                    savePoints = savePoints,
                                    missions = missions,
                                    focusSessions = focusSessions,
                                    showStatsEnabled = preferences.showStatistics,
                                    onToggleShowStats = viewModel::setShowStatistics
                                )
                            }

                            "SEC_WIDGETS" -> {
                                NativeWidgetsGalleryScreen(
                                    projects = projects,
                                    savePoints = savePoints,
                                    missions = missions,
                                    onTriggerQuickAction = { viewModel.openSavePointSheet(true) }
                                )
                            }

                            "SEC_SETTINGS" -> {
                                CustomizationAndVaultScreen(
                                    preferences = preferences,
                                    exportedJson = exportedBackupJson,
                                    backupValidationResult = backupValidation,
                                    hasSampleProjects = projects.any { it.isSampleData },
                                    onSetThemeMode = viewModel::setThemeMode,
                                    onSetAppearanceMode = viewModel::setAppearanceMode,
                                    onSetCustomAccent = viewModel::setCustomAccent,
                                    onSetDensity = viewModel::setLayoutDensity,
                                    onSetFontScale = viewModel::setFontScale,
                                    onSetReducedMotion = viewModel::setReducedMotion,
                                    onSetAutoPromptSavePoint = viewModel::setAutoPromptSavePoint,
                                    onSetFocusNotifications = viewModel::setFocusNotificationsEnabled,
                                    onSetPersistentContextNotification = viewModel::setPersistentContextNotification,
                                    onGenerateBackupJson = viewModel::generateFullBackupJson,
                                    onExportBackupToUri = viewModel::exportFullBackupToDocumentUri,
                                    onImportBackupFromUri = viewModel::importAndValidateBackupFromDocumentUri,
                                    onValidateBackupJson = viewModel::validateBackupCandidate,
                                    onConfirmRestore = viewModel::confirmAndExecuteRestore,
                                    onClearBackupValidation = viewModel::clearBackupValidation,
                                    onLoadSampleWorkspace = viewModel::loadOptInSampleProjects,
                                    onRemoveSampleWorkspace = viewModel::removeSampleProjects,
                                    onShowBanner = viewModel::showBanner
                                )
                            }

                            "PRI_PROJECTS" -> {
                                ProjectsScreen(
                                    projects = projects,
                                    selectedProjectId = preferences.selectedProjectId,
                                    savePoints = savePoints,
                                    missions = missions,
                                    references = references,
                                    focusSessions = focusSessions,
                                    onSelectProject = viewModel::selectActiveProject,
                                    onCreateProject = viewModel::createProject,
                                    onTogglePin = viewModel::toggleProjectPin,
                                    onToggleMilestone = viewModel::toggleMilestone,
                                    onAddMilestone = viewModel::addMilestoneToProject,
                                    onDeleteProjectConfirmed = viewModel::deleteProjectConfirmed,
                                    onOpenSavePointSheet = viewModel::openSavePointSheet,
                                    onStartFocusForProject = { pid ->
                                        viewModel.configureFocusSession(
                                            mode = FocusModeType.DEEP_WORK,
                                            projectId = pid
                                        )
                                        viewModel.navigatePrimary(PrimaryDestination.FOCUS)
                                    },
                                    onOpenRescueMode = { viewModel.navigateSecondary(SecondaryDestination.RESCUE) },
                                    onLoadSampleWorkspace = viewModel::loadOptInSampleProjects,
                                    onRemoveSampleWorkspace = viewModel::removeSampleProjects,
                                    onShowBanner = viewModel::showBanner
                                )
                            }

                            "PRI_SAVE_POINTS" -> {
                                SavePointsScreen(
                                    projects = projects,
                                    selectedProjectId = preferences.selectedProjectId,
                                    savePoints = savePoints,
                                    references = references,
                                    onOpenSavePointSheet = viewModel::openSavePointSheet,
                                    onTogglePin = viewModel::toggleSavePointPin,
                                    onToggleArchive = viewModel::toggleSavePointArchive,
                                    onDuplicate = viewModel::duplicateSavePoint,
                                    onRestoreContext = viewModel::restoreWorkingContextFromSavePoint,
                                    onUpdateSavePoint = viewModel::updateSavePointNotesAndMetadata,
                                    onShowBanner = viewModel::showBanner
                                )
                            }

                            "PRI_FOCUS" -> {
                                FocusStudioScreen(
                                    projects = projects,
                                    missions = missions,
                                    selectedProjectId = preferences.selectedProjectId,
                                    focusRuntime = focusRuntime,
                                    focusHistory = focusSessions,
                                    onConfigureFocus = viewModel::configureFocusSession,
                                    onAdjustPlannedMinutes = viewModel::adjustFocusPlannedMinutes,
                                    onStartOrResume = viewModel::startOrResumeFocusTimer,
                                    onPause = viewModel::pauseFocusTimer,
                                    onFinish = { viewModel.finishFocusSession(completedNaturally = false) },
                                    onUpdateNotes = viewModel::updateFocusNotes,
                                    onToggleZenMode = viewModel::setFocusZenMode,
                                    onSetAmbientSound = viewModel::setFocusAmbientSound,
                                    onDismissSavePointPrompt = viewModel::dismissFocusSavePointPrompt,
                                    onOpenSavePointAfterFocus = { viewModel.openSavePointSheet(quickMode = false) }
                                )
                            }

                            "PRI_LIBRARY" -> {
                                LibraryScreen(
                                    projects = projects,
                                    selectedProjectId = preferences.selectedProjectId,
                                    references = references,
                                    incomingSharedText = sharedIncomingText,
                                    onConsumeSharedText = viewModel::consumeSharedIncomingText,
                                    onAddReference = { pid, title, type, content, desc, folder, category, tags, persistable ->
                                        viewModel.addReferenceItem(
                                            projectId = pid,
                                            title = title,
                                            type = type,
                                            contentOrUri = content,
                                            description = desc,
                                            folder = folder,
                                            category = category,
                                            tags = tags,
                                            hasPersistableUriAccess = persistable
                                        )
                                    },
                                    onToggleFavorite = viewModel::toggleReferenceFavorite,
                                    onDeleteReference = viewModel::deleteReference
                                )
                            }

                            else -> {
                                DashboardScreen(
                                    projects = projects,
                                    savePoints = savePoints,
                                    missions = missions,
                                    references = references,
                                    focusSessions = focusSessions,
                                    focusRuntime = focusRuntime,
                                    preferences = preferences,
                                    onSelectProject = viewModel::selectActiveProject,
                                    onSetThemeMode = viewModel::setThemeMode,
                                    onSetAppearanceMode = viewModel::setAppearanceMode,
                                    onOpenSavePointSheet = viewModel::openSavePointSheet,
                                    onOpenQuickCapture = { viewModel.setShowQuickCaptureDialog(true) },
                                    onNavigatePrimary = viewModel::navigatePrimary,
                                    onNavigateSecondary = viewModel::navigateSecondary,
                                    onToggleMissionDone = viewModel::toggleMissionComplete,
                                    onUpdateWidgets = viewModel::updateDashboardWidgets,
                                    onSaveLayoutPreset = viewModel::saveDashboardLayoutPreset,
                                    onLoadSampleWorkspace = viewModel::loadOptInSampleProjects,
                                    onRemoveSampleWorkspace = viewModel::removeSampleProjects
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSavePointSheet) {
        SavePointBottomSheet(
            projects = projects,
            missions = missions,
            references = references,
            selectedProjectId = preferences.selectedProjectId,
            initialQuickMode = savePointQuickMode,
            onDismiss = viewModel::closeSavePointSheet,
            onSaveCheckpoint = { pid, mid, title, completed, happening, next, notes, blockers, refs, links, tags, pinned, quick, attachedIdsCsv ->
                viewModel.createSavePoint(
                    projectId = pid,
                    missionId = mid,
                    title = title,
                    completedSummary = completed,
                    happeningNow = happening,
                    nextAction = next,
                    notes = notes,
                    blockers = blockers,
                    referencesSummary = refs,
                    importantLinks = links,
                    tags = tags,
                    isPinned = pinned,
                    isQuickCheckpoint = quick,
                    attachedReferenceIdsCsv = attachedIdsCsv
                )
            }
        )
    }

    if (showCommandPalette) {
        CommandPaletteDialog(
            projects = projects,
            missions = missions,
            savePoints = savePoints,
            references = references,
            focusSessions = focusSessions,
            savedSearchesCsv = preferences.savedSearchesCsv,
            onSaveSearch = viewModel::saveSearchQuery,
            onDismiss = { viewModel.setShowCommandPalette(false) },
            onOpenSavePointSheet = { viewModel.openSavePointSheet(false) },
            onSelectProjectAndOpen = { pid ->
                viewModel.selectActiveProject(pid)
                viewModel.navigatePrimary(PrimaryDestination.PROJECTS)
            },
            onStartFocusSession = { viewModel.navigatePrimary(PrimaryDestination.FOCUS) },
            onOpenMissions = { viewModel.navigateSecondary(SecondaryDestination.MISSIONS) },
            onOpenRescueMode = { viewModel.navigateSecondary(SecondaryDestination.RESCUE) }
        )
    }

    if (showQuickCapture) {
        QuickCaptureDialog(
            selectedProjectId = preferences.selectedProjectId,
            onDismiss = { viewModel.setShowQuickCaptureDialog(false) },
            onSubmitCapture = viewModel::quickCaptureEntry
        )
    }
}

private fun PrimaryDestination.icon(): ImageVector = when (this) {
    PrimaryDestination.HOME -> Icons.Default.Home
    PrimaryDestination.PROJECTS -> Icons.Default.Folder
    PrimaryDestination.SAVE_POINTS -> Icons.Default.Bookmark
    PrimaryDestination.FOCUS -> Icons.Default.Timer
    PrimaryDestination.LIBRARY -> Icons.Default.LocalLibrary
}
