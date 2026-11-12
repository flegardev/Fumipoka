package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DashboardWidgetConfig
import com.example.data.DashboardWidgetType
import com.example.data.FocusSessionEntity
import com.example.data.LayoutDensityMode
import com.example.data.MissionEntity
import com.example.data.MissionStatus
import com.example.data.ProjectEntity
import com.example.data.ProjectStatus
import com.example.data.ReferenceItemEntity
import com.example.data.ReturnmarkThemeMode
import com.example.data.SavePointEntity
import com.example.data.ThemeAppearanceMode
import com.example.data.UserPreferencesState
import com.example.data.WidgetSize
import com.example.ui.theme.parseHexColorSafely
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    projects: List<ProjectEntity>,
    savePoints: List<SavePointEntity>,
    missions: List<MissionEntity>,
    references: List<ReferenceItemEntity>,
    focusSessions: List<FocusSessionEntity>,
    focusRuntime: ActiveFocusRuntimeState,
    preferences: UserPreferencesState,
    onSelectProject: (Long) -> Unit,
    onSetThemeMode: (ReturnmarkThemeMode) -> Unit = {},
    onSetAppearanceMode: (ThemeAppearanceMode) -> Unit = {},
    onOpenSavePointSheet: (Boolean) -> Unit,
    onOpenQuickCapture: () -> Unit,
    onNavigatePrimary: (PrimaryDestination) -> Unit,
    onNavigateSecondary: (SecondaryDestination) -> Unit,
    onToggleMissionDone: (MissionEntity) -> Unit,
    onUpdateWidgets: (List<DashboardWidgetConfig>, String?) -> Unit,
    onSaveLayoutPreset: (String, List<DashboardWidgetConfig>) -> Unit,
    onLoadSampleWorkspace: () -> Unit = {},
    onRemoveSampleWorkspace: () -> Unit = {}
) {
    var showWidgetCustomizer by remember { mutableStateOf(false) }
    val activeProject = projects.find { it.id == preferences.selectedProjectId } ?: projects.firstOrNull()
    val latestSavePoint = savePoints.firstOrNull { it.projectId == activeProject?.id && !it.isArchived }
        ?: savePoints.firstOrNull { !it.isArchived }
    val nextMission = missions.firstOrNull {
        it.projectId == activeProject?.id && it.status != MissionStatus.DONE.name && !it.isArchived
    } ?: missions.firstOrNull { it.status != MissionStatus.DONE.name && !it.isArchived }

    val widgetConfigs = remember(preferences.widgetConfigsRaw, preferences.customAccentHex) {
        preferences.parsedWidgetConfigs()
    }
    val visibleWidgets = widgetConfigs.filter { it.isVisible }.sortedBy { it.orderIndex }
    val cardSpacing = if (preferences.layoutDensity == LayoutDensityMode.COMPACT) 10.dp else 16.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(cardSpacing)
    ) {
        // 1. Hero Command Header Card with Kyoto Café Notebook Art & Warm Lantern Glow
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    com.example.ui.theme.CafeIllustration(
                        modifier = Modifier.fillMaxWidth().height(220.dp), product = 1, quiet = true
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "YOUR LITTLE SAVE POINT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { showWidgetCustomizer = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("customize_dashboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DashboardCustomize,
                                    contentDescription = "Customize Dashboard Widgets",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activeProject?.name ?: "Open a Fresh Notebook Page",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeProject?.currentContextSummary?.ifBlank {
                                "Never lose your place. Never lose your progress."
                            } ?: "Quiet café desk • Save your place and return effortlessly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Prominent Primary Action Bar: SAVE MY PROGRESS + Continue Working
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onOpenSavePointSheet(false) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("save_my_progress_button")
                            ) {
                                Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save my place", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onNavigatePrimary(PrimaryDestination.PROJECTS) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("continue_working_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // 1B. Instant Lofi Japanese & Pastel Theme Quick Bar (100% Free)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .testTag("quick_pastel_theme_bar")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Make yourself at home",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ThemeAppearanceMode.entries.forEach { appMode ->
                                val isCurrent = preferences.appearanceMode == appMode
                                Surface(
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSetAppearanceMode(appMode) }
                                        .testTag("quick_appearance_${appMode.name.lowercase()}")
                                ) {
                                    Text(
                                        text = appMode.shortBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val signatureThemes = ReturnmarkThemeMode.entries.filter { it.isLofiJapanese || it.isPastelSignature }
                        items(signatureThemes, key = { it.name }) { mode ->
                            val isSelected = preferences.themeMode == mode
                            val swatchColor = parseHexColorSafely(mode.previewAccentHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(mode.label) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                    )
                                },
                                modifier = Modifier.testTag("dashboard_theme_chip_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // Atmospheric Kyoto Stationery Empty State / Opt-In Demo Workspace Control
        if (projects.isEmpty() || projects.any { it.isSampleData }) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                        .testTag("sample_workspace_control_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (projects.isEmpty()) "📓 A Quiet Desk & Fresh Washi Page"
                                else "🍵 Example Notebooks Active (Marked as Sample)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (projects.isEmpty()) {
                                    "Begin your own project notebook or optionally load example workspaces to explore Fumipoka's save-points."
                                } else {
                                    "Sample notebooks are kept separate from your personal work and can be cleared in one tap."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        if (projects.isEmpty()) {
                            OutlinedButton(
                                onClick = onLoadSampleWorkspace,
                                modifier = Modifier.testTag("load_sample_workspace_button")
                            ) {
                                Text("Load Examples")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onRemoveSampleWorkspace,
                                modifier = Modifier.testTag("remove_sample_workspace_button")
                            ) {
                                Text("Clear Examples")
                            }
                        }
                    }
                }
            }
        }

        // 2. Project Switcher & Quick Secondary Destinations Strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(projects, key = { it.id }) { project ->
                        val isSelected = project.id == activeProject?.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectProject(project.id) },
                            label = {
                                Text(
                                    text = project.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColorSafely(project.colorHex))
                                )
                            }
                        )
                    }
                }

                // Secondary Destinations Quick Access Row
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryActionChip("Missions (${missions.count { it.status != MissionStatus.DONE.name }})", "nav_missions_chip") {
                        onNavigateSecondary(SecondaryDestination.MISSIONS)
                    }
                    SecondaryActionChip("Rescue Mode", "nav_rescue_chip") {
                        onNavigateSecondary(SecondaryDestination.RESCUE)
                    }
                    SecondaryActionChip("Templates", "nav_templates_chip") {
                        onNavigateSecondary(SecondaryDestination.TEMPLATES)
                    }
                    SecondaryActionChip("Statistics & Charts", "nav_statistics_chip") {
                        onNavigateSecondary(SecondaryDestination.STATISTICS)
                    }
                    SecondaryActionChip("Widgets", "nav_widgets_chip") {
                        onNavigateSecondary(SecondaryDestination.WIDGETS)
                    }
                    SecondaryActionChip("Vault & Themes", "nav_settings_chip") {
                        onNavigateSecondary(SecondaryDestination.SETTINGS)
                    }
                }
            }
        }

        // 2B. Interactive Momentum & Progress Visual Overview Card (Custom Canvas Ring + 7-Day Bar Chart)
        if (preferences.showStatistics) {
            item {
                DashboardMomentumChartCard(
                    projects = projects,
                    savePoints = savePoints,
                    missions = missions,
                    focusSessions = focusSessions,
                    onOpenFullStats = { onNavigateSecondary(SecondaryDestination.STATISTICS) }
                )
            }
        }

        // 3. Layout Preset Banner + Customize Trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMMAND CENTER WIDGETS (${visibleWidgets.size}/20)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Layout: ${preferences.activeLayoutPresetName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { showWidgetCustomizer = true }) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reorder & Presets")
                }
            }
        }

        // 4. Render all visible customizable Dashboard Widgets (up to 20)
        items(visibleWidgets, key = { it.type.name }) { config ->
            DashboardWidgetCard(
                config = config,
                activeProject = activeProject,
                projects = projects,
                savePoints = savePoints,
                latestSavePoint = latestSavePoint,
                missions = missions,
                nextMission = nextMission,
                references = references,
                focusSessions = focusSessions,
                focusRuntime = focusRuntime,
                showStats = preferences.showStatistics,
                onSelectProject = onSelectProject,
                onOpenSavePointSheet = onOpenSavePointSheet,
                onOpenQuickCapture = onOpenQuickCapture,
                onNavigatePrimary = onNavigatePrimary,
                onNavigateSecondary = onNavigateSecondary,
                onToggleMissionDone = onToggleMissionDone
            )
        }
    }

    if (showWidgetCustomizer) {
        DashboardWidgetCustomizerSheet(
            currentConfigs = widgetConfigs,
            activePresetName = preferences.activeLayoutPresetName,
            onDismiss = { showWidgetCustomizer = false },
            onUpdateConfigs = { updated, preset ->
                onUpdateWidgets(updated, preset)
            },
            onSavePreset = { name, configs ->
                onSaveLayoutPreset(name, configs)
            }
        )
    }
}

@Composable
private fun DashboardMomentumChartCard(
    projects: List<ProjectEntity>,
    savePoints: List<SavePointEntity>,
    missions: List<MissionEntity>,
    focusSessions: List<FocusSessionEntity>,
    onOpenFullStats: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val doneMissions = missions.count { it.status == MissionStatus.DONE.name }
    val totalMissions = missions.size.coerceAtLeast(1)
    val completionRatio = if (missions.isEmpty()) 0f else doneMissions.toFloat() / totalMissions.toFloat()
    val animatedCompletion by animateFloatAsState(
        targetValue = completionRatio,
        animationSpec = tween(durationMillis = 500),
        label = "dashboard_ring_anim"
    )

    // Compute 7-day Save Point + Focus activity buckets
    val dayBuckets = remember(savePoints, focusSessions) {
        val cal = Calendar.getInstance()
        val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val nowMillis = System.currentTimeMillis()
        (6 downTo 0).map { daysAgo ->
            val startOfDay = nowMillis - (daysAgo + 1) * 86_400_000L
            val endOfDay = nowMillis - daysAgo * 86_400_000L
            cal.timeInMillis = endOfDay
            val label = dayFmt.format(cal.time).take(3)
            val spCount = savePoints.count { it.timestamp in startOfDay..endOfDay }
            val focusMins = (focusSessions.filter { it.startedAt in startOfDay..endOfDay }.sumOf { it.actualElapsedSeconds } / 60L).toInt()
            val score = (spCount * 15 + focusMins).coerceAtLeast(if (daysAgo == 0 && savePoints.isNotEmpty()) 20 else 4)
            Triple(label, spCount, score)
        }
    }
    val maxScore = (dayBuckets.maxOfOrNull { it.third } ?: 30).coerceAtLeast(20)

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .clickable { onOpenFullStats() }
            .testTag("dashboard_momentum_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROGRESS & CHECKPOINT PULSE",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${savePoints.size} Checkpoints • $doneMissions/${missions.size} Missions • ${projects.size} Workspaces",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Charts →",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Circular Completion Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(84.dp)
                ) {
                    Canvas(modifier = Modifier.size(84.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        drawArc(
                            color = surfaceVariantColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor, primaryColor)),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedCompletion.coerceIn(0.05f, 1f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(completionRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 7-Day Activity Bar Chart
                Column(modifier = Modifier.weight(1f)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        val barCount = dayBuckets.size
                        val spacing = 8.dp.toPx()
                        val totalSpacing = spacing * (barCount - 1)
                        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(8f)

                        dayBuckets.forEachIndexed { idx, (_, _, score) ->
                            val ratio = (score.toFloat() / maxScore.toFloat()).coerceIn(0.14f, 1f)
                            val barHeight = size.height * ratio
                            val left = idx * (barWidth + spacing)
                            val top = size.height - barHeight

                            drawRoundRect(
                                color = surfaceVariantColor,
                                topLeft = Offset(left, 0f),
                                size = Size(barWidth, size.height),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor, secondaryColor),
                                    startY = top,
                                    endY = size.height
                                ),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayBuckets.forEach { (dayLabel, _, _) ->
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryActionChip(
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DashboardWidgetCard(
    config: DashboardWidgetConfig,
    activeProject: ProjectEntity?,
    projects: List<ProjectEntity>,
    savePoints: List<SavePointEntity>,
    latestSavePoint: SavePointEntity?,
    missions: List<MissionEntity>,
    nextMission: MissionEntity?,
    references: List<ReferenceItemEntity>,
    focusSessions: List<FocusSessionEntity>,
    focusRuntime: ActiveFocusRuntimeState,
    showStats: Boolean,
    onSelectProject: (Long) -> Unit,
    onOpenSavePointSheet: (Boolean) -> Unit,
    onOpenQuickCapture: () -> Unit,
    onNavigatePrimary: (PrimaryDestination) -> Unit,
    onNavigateSecondary: (SecondaryDestination) -> Unit,
    onToggleMissionDone: (MissionEntity) -> Unit
) {
    val accent = parseHexColorSafely(config.accentHex, MaterialTheme.colorScheme.primary)
    val paddingVal = when (config.size) {
        WidgetSize.COMPACT -> 12.dp
        WidgetSize.MEDIUM -> 16.dp
        WidgetSize.EXPANDED -> 20.dp
    }

    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .testTag("widget_${config.type.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(paddingVal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${config.type.index} • ${config.type.defaultTitle.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = config.size.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (config.type) {
                DashboardWidgetType.ACTIVE_PROJECTS -> {
                    val activeList = projects.filter { it.status == ProjectStatus.ACTIVE.name }
                    Text(
                        text = "${activeList.size} Active Projects (${projects.size} total)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    activeList.take(if (config.size == WidgetSize.COMPACT) 2 else 4).forEach { proj ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectProject(proj.id) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(proj.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(proj.templateSource, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                DashboardWidgetType.CONTINUE_WHERE_LEFT_OFF -> {
                    Text(
                        text = activeProject?.name ?: "No active project",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Stopped at: ${activeProject?.currentContextSummary?.ifBlank { "No context recorded yet" } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!activeProject?.nextManualStep.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next Step → ${activeProject?.nextManualStep}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onNavigatePrimary(PrimaryDestination.PROJECTS) }) {
                            Text("Open Workspace")
                        }
                        OutlinedButton(onClick = { onNavigateSecondary(SecondaryDestination.RESCUE) }) {
                            Text("Rescue Mode")
                        }
                    }
                }

                DashboardWidgetType.LAST_SAVE_POINT -> {
                    if (latestSavePoint != null) {
                        Text(latestSavePoint.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Completed: ${latestSavePoint.completedSummary.ifBlank { "Recorded checkpoint" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (latestSavePoint.nextAction.isNotBlank()) {
                            Text(
                                text = "Next: ${latestSavePoint.nextAction}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = accent
                            )
                        }
                    } else {
                        Text("No Save Points yet. Tap SAVE MY PROGRESS to capture your first checkpoint.")
                    }
                }

                DashboardWidgetType.CURRENT_MISSION -> {
                    if (nextMission != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(nextMission.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${nextMission.priority} Priority • ${nextMission.status}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onToggleMissionDone(nextMission) }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Complete mission", tint = accent)
                            }
                        }
                    } else {
                        Text("All missions completed for this project!")
                    }
                }

                DashboardWidgetType.TODAYS_MISSIONS -> {
                    val openMissions = missions.filter { it.status != MissionStatus.DONE.name && !it.isArchived }.take(3)
                    if (openMissions.isEmpty()) {
                        Text("Queue is clear for today.")
                    } else {
                        openMissions.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• ${m.title}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(m.priority, style = MaterialTheme.typography.labelSmall, color = accent)
                            }
                        }
                    }
                }

                DashboardWidgetType.FOCUS_TIMER -> {
                    val mins = focusRuntime.elapsedSeconds / 60
                    val secs = focusRuntime.elapsedSeconds % 60
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (focusRuntime.isRunning) {
                                    String.format(Locale.US, "Active %s • %02d:%02d", focusRuntime.mode.label, mins, secs)
                                } else {
                                    "Ready for ${focusRuntime.mode.label} (${focusRuntime.mode.defaultMinutes}m)"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Offline Ambient Sound: ${focusRuntime.ambientSound.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = { onNavigatePrimary(PrimaryDestination.FOCUS) }) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Focus Studio")
                        }
                    }
                }

                DashboardWidgetType.PROJECT_PROGRESS -> {
                    val projMissions = missions.filter { it.projectId == activeProject?.id }
                    val doneCount = projMissions.count { it.status == MissionStatus.DONE.name }
                    val ratio = if (projMissions.isNotEmpty()) doneCount.toFloat() / projMissions.size else 0.5f
                    Text(
                        text = "${activeProject?.name ?: "Workspace"}: ${(ratio * 100).toInt()}% Missions Done",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accent
                    )
                }

                DashboardWidgetType.WEEKLY_OVERVIEW -> {
                    val totalFocusMins = focusSessions.sumOf { it.actualElapsedSeconds } / 60
                    Text(
                        text = if (showStats) {
                            "${savePoints.size} Checkpoints • ${missions.count { it.status == MissionStatus.DONE.name }} Missions Done • ${totalFocusMins}m Focus"
                        } else {
                            "Statistics hidden in zero-pressure mode."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                DashboardWidgetType.RECENT_ACTIVITY -> {
                    val recentSp = savePoints.take(2)
                    recentSp.forEach { sp ->
                        Text(
                            text = "🔖 Checkpoint: ${sp.title}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                DashboardWidgetType.RECENT_REFERENCES -> {
                    references.take(2).forEach { ref ->
                        Text(
                            text = "📎 [${ref.type}] ${ref.title}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                DashboardWidgetType.QUICK_SAVE_POINT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("One-tap context snapshot", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { onOpenSavePointSheet(true) }) {
                            Text("Quick Save")
                        }
                    }
                }

                DashboardWidgetType.QUICK_CAPTURE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Capture idea, link, or mission", style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = onOpenQuickCapture) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Quick Capture")
                        }
                    }
                }

                DashboardWidgetType.UPCOMING_MILESTONES -> {
                    val milestones = activeProject?.parsedMilestones().orEmpty()
                    if (milestones.isEmpty()) {
                        Text("No milestones defined for current project.")
                    } else {
                        milestones.forEach { ms ->
                            Text(
                                text = "${if (ms.isCompleted) "✓" else "○"} ${ms.title} (${ms.targetDateLabel})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                DashboardWidgetType.PROJECT_TIMELINE -> {
                    val fmt = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
                    savePoints.filter { it.projectId == activeProject?.id }.take(3).forEach { sp ->
                        Text(
                            text = "${fmt.format(Date(sp.timestamp))} — ${sp.title}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                DashboardWidgetType.FOCUS_STATISTICS -> {
                    val sessionsCount = focusSessions.size
                    val totalMins = focusSessions.sumOf { it.actualElapsedSeconds } / 60
                    Text("$sessionsCount Focus Sessions completed ($totalMins minutes total)", style = MaterialTheme.typography.bodyMedium)
                }

                DashboardWidgetType.SAVED_IDEAS -> {
                    val ideas = references.filter { it.folder.equals("Saved Ideas", ignoreCase = true) || it.tags.contains("idea") }
                    Text(
                        text = if (ideas.isNotEmpty()) "${ideas.size} Saved Ideas: ${ideas.first().title}" else "Use Quick Capture to store raw project ideas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                DashboardWidgetType.COMPLETED_MISSIONS -> {
                    val completed = missions.filter { it.status == MissionStatus.DONE.name }
                    Text("${completed.size} Completed Missions preserved in project history.", style = MaterialTheme.typography.bodySmall)
                }

                DashboardWidgetType.RECENTLY_OPENED_PROJECTS -> {
                    projects.sortedByDescending { it.lastOpenedAt }.take(2).forEach { p ->
                        Text("• ${p.name}: ${p.tagline}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }

                DashboardWidgetType.PROJECT_SHORTCUTS -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onNavigatePrimary(PrimaryDestination.SAVE_POINTS) }) { Text("Timeline") }
                        TextButton(onClick = { onNavigatePrimary(PrimaryDestination.LIBRARY) }) { Text("Library") }
                        TextButton(onClick = { onNavigateSecondary(SecondaryDestination.RESCUE) }) { Text("Rescue") }
                    }
                }

                DashboardWidgetType.PERSONAL_PROGRESS_SUMMARY -> {
                    val stalled = projects.filter { it.status == ProjectStatus.PAUSED.name || it.openBlockers.isNotBlank() }
                    Text(
                        text = "${projects.size} Projects • ${savePoints.size} Checkpoints • ${stalled.size} needing attention",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardWidgetCustomizerSheet(
    currentConfigs: List<DashboardWidgetConfig>,
    activePresetName: String,
    onDismiss: () -> Unit,
    onUpdateConfigs: (List<DashboardWidgetConfig>, String?) -> Unit,
    onSavePreset: (String, List<DashboardWidgetConfig>) -> Unit
) {
    var workingList by remember(currentConfigs) { mutableStateOf(currentConfigs.sortedBy { it.orderIndex }) }
    var newPresetName by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("Customize Command Center (20 Widgets)", style = MaterialTheme.typography.titleLarge)
            Text(
                "Reorder, resize (Compact/Medium/Expanded), toggle visibility, or save a named layout preset.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Quick Layout Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val allVisible = workingList.mapIndexed { idx, c -> c.copy(isVisible = true, orderIndex = idx + 1) }
                        workingList = allVisible
                        onUpdateConfigs(allVisible, "Command Center (All 20)")
                    }
                ) {
                    Text("Show All 20")
                }
                OutlinedButton(
                    onClick = {
                        val minimal = workingList.mapIndexed { idx, c ->
                            c.copy(isVisible = c.type.index in listOf(1, 2, 3, 4, 6, 11), orderIndex = idx + 1)
                        }
                        workingList = minimal
                        onUpdateConfigs(minimal, "Minimal Resume (Top 6)")
                    }
                ) {
                    Text("Minimal (6)")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Save Current Layout Preset") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSavePreset(newPresetName, workingList)
                            newPresetName = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(workingList.size) { index ->
                    val item = workingList[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${index + 1}. ${item.type.defaultTitle}", style = MaterialTheme.typography.titleSmall)
                            Text("Size: ${item.size.label}", style = MaterialTheme.typography.labelSmall)
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    val nextSize = when (item.size) {
                                        WidgetSize.COMPACT -> WidgetSize.MEDIUM
                                        WidgetSize.MEDIUM -> WidgetSize.EXPANDED
                                        WidgetSize.EXPANDED -> WidgetSize.COMPACT
                                    }
                                    workingList = workingList.toMutableList().also {
                                        it[index] = item.copy(size = nextSize)
                                    }
                                    onUpdateConfigs(workingList, null)
                                }
                            ) {
                                Text(item.size.name.first().toString(), fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val mutable = workingList.toMutableList()
                                        val prev = mutable[index - 1]
                                        mutable[index - 1] = item.copy(orderIndex = index)
                                        mutable[index] = prev.copy(orderIndex = index + 1)
                                        workingList = mutable
                                        onUpdateConfigs(mutable, null)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up")
                            }
                            IconButton(
                                onClick = {
                                    if (index < workingList.lastIndex) {
                                        val mutable = workingList.toMutableList()
                                        val next = mutable[index + 1]
                                        mutable[index + 1] = item.copy(orderIndex = index + 1)
                                        mutable[index] = next.copy(orderIndex = index + 2)
                                        workingList = mutable
                                        onUpdateConfigs(mutable, null)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down")
                            }
                            IconButton(
                                onClick = {
                                    workingList = workingList.toMutableList().also {
                                        it[index] = item.copy(isVisible = !item.isVisible)
                                    }
                                    onUpdateConfigs(workingList, null)
                                }
                            ) {
                                Icon(
                                    imageVector = if (item.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
