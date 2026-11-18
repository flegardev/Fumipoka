package com.example.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BackupValidationResult
import com.example.data.FocusSessionEntity
import com.example.data.LayoutDensityMode
import com.example.data.MissionEntity
import com.example.data.MissionStatus
import com.example.data.ProjectEntity
import com.example.data.ProjectStatus
import com.example.data.ProjectTemplateModel
import com.example.data.ProjectTemplatesCatalog
import com.example.data.ReferenceItemEntity
import com.example.data.ReturnmarkThemeMode
import com.example.data.SavePointEntity
import com.example.data.ThemeAppearanceMode
import com.example.data.UserPreferencesState
import com.example.domain.ReturnmarkNotificationHelper
import com.example.ui.theme.parseHexColorSafely
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommandPaletteDialog(
    projects: List<ProjectEntity>,
    missions: List<MissionEntity>,
    savePoints: List<SavePointEntity>,
    references: List<ReferenceItemEntity>,
    focusSessions: List<FocusSessionEntity>,
    savedSearchesCsv: String,
    onSaveSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenSavePointSheet: () -> Unit,
    onSelectProjectAndOpen: (Long) -> Unit,
    onStartFocusSession: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenRescueMode: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val savedQueries = remember(savedSearchesCsv) {
        savedSearchesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    val q = query.trim()
    val matchingProjects = if (q.isBlank()) emptyList() else projects.filter {
        it.name.contains(q, true) || it.currentContextSummary.contains(q, true) || it.openBlockers.contains(q, true)
    }
    val matchingSavePoints = if (q.isBlank()) emptyList() else savePoints.filter {
        it.title.contains(q, true) || it.happeningNow.contains(q, true) || it.nextAction.contains(q, true) || it.blockers.contains(q, true) || it.tags.contains(q, true)
    }
    val matchingMissions = if (q.isBlank()) emptyList() else missions.filter {
        it.title.contains(q, true) || it.description.contains(q, true) || it.tags.contains(q, true)
    }
    val matchingRefs = if (q.isBlank()) emptyList() else references.filter {
        it.title.contains(q, true) || it.contentOrUri.contains(q, true) || it.tags.contains(q, true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search & Command Center") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("command_palette_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search projects, save points, blockers, missions, tags...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("command_palette_search_input")
                )

                if (savedQueries.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        savedQueries.forEach { sq ->
                            FilterChip(
                                selected = query == sq,
                                onClick = { query = sq },
                                label = { Text("#$sq") }
                            )
                        }
                        if (q.isNotBlank() && !savedQueries.contains(q)) {
                            TextButton(onClick = { onSaveSearch(q) }) {
                                Text("+ Save Search")
                            }
                        }
                    }
                }

                Text("INSTANT COMMANDS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                CommandRowButton("🔖 Save Checkpoint Now") {
                    onDismiss()
                    onOpenSavePointSheet()
                }
                CommandRowButton("▶ Resume Most Recent Project") {
                    projects.firstOrNull()?.let { onSelectProjectAndOpen(it.id) }
                    onDismiss()
                }
                CommandRowButton("⏱ Begin Focus Session") {
                    onDismiss()
                    onStartFocusSession()
                }
                CommandRowButton("🛟 Launch Rescue Mode (Find Blockers)") {
                    onDismiss()
                    onOpenRescueMode()
                }
                CommandRowButton("✓ View Completed Missions") {
                    onDismiss()
                    onOpenMissions()
                }

                if (q.isNotBlank()) {
                    HorizontalDivider()
                    Text(
                        text = "RESULTS (${matchingProjects.size + matchingSavePoints.size + matchingMissions.size + matchingRefs.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    matchingProjects.forEach { p ->
                        Text(
                            text = "📁 Project: ${p.name} — ${p.currentContextSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectProjectAndOpen(p.id)
                                    onDismiss()
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                    matchingSavePoints.forEach { sp ->
                        Text(
                            text = "🔖 Save Point: ${sp.title} → ${sp.nextAction}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                    matchingMissions.forEach { m ->
                        Text(
                            text = "🎯 Mission [${m.status}]: ${m.title}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                    matchingRefs.forEach { r ->
                        Text(
                            text = "📎 Reference [${r.type}]: ${r.title}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun CommandRowButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
        )
    }
}

@Composable
fun QuickCaptureDialog(
    selectedProjectId: Long,
    onDismiss: () -> Unit,
    onSubmitCapture: (String, String, String, Long) -> Unit
) {
    var kind by remember { mutableStateOf("IDEA") }
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Capture") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("IDEA" to "Saved Idea", "MISSION" to "Mission", "SAVE_POINT" to "Quick Save").forEach { (k, label) ->
                        FilterChip(
                            selected = kind == k,
                            onClick = { kind = k },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Headline") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_capture_title_input")
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Context, Link, or Next Step") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSubmitCapture(kind, title, details, selectedProjectId)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_quick_capture_button")
            ) {
                Text("Capture")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TemplatesStudioScreen(
    onApplyTemplateToNewProject: (ProjectTemplateModel, String) -> Unit,
    onShowBanner: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var customImportedTemplates by remember { mutableStateOf(listOf<ProjectTemplateModel>()) }
    var selectedForProject by remember { mutableStateOf<ProjectTemplateModel?>(null) }
    var projectNameInput by remember { mutableStateOf("") }
    var importJsonInput by remember { mutableStateOf("") }

    val allTemplates = ProjectTemplatesCatalog.builtInTemplates + customImportedTemplates

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("templates_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Project Templates Studio", style = MaterialTheme.typography.headlineMedium)
            Text(
                "11 ready-to-launch project blueprints (including Blank) with milestones, missions, and JSON import/export.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Import Template JSON Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Import Template from JSON", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        label = { Text("Paste ReturnmarkTemplate JSON") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val parsed = ProjectTemplatesCatalog.parseImportedTemplateJson(importJsonInput)
                            if (parsed != null) {
                                customImportedTemplates = customImportedTemplates + parsed
                                importJsonInput = ""
                                onShowBanner("Imported template \"${parsed.name}\"")
                            } else {
                                onShowBanner("Invalid template JSON format")
                            }
                        }
                    ) {
                        Text("Import Template")
                    }
                }
            }
        }

        items(allTemplates, key = { it.id }) { tpl ->
            val accent = parseHexColorSafely(tpl.colorHex)
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tpl.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = accent)
                        Text("${tpl.defaultFocusMode.label} (${tpl.defaultFocusMinutes}m)", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(tpl.name, style = MaterialTheme.typography.titleLarge)
                    Text(tpl.tagline, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(tpl.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Milestones: ${tpl.defaultMilestones.joinToString(" → ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            selectedForProject = tpl
                            projectNameInput = "My ${tpl.name}"
                        }) {
                            Text("Use Template")
                        }
                        OutlinedButton(onClick = {
                            val json = ProjectTemplatesCatalog.exportTemplateToJson(tpl)
                            clipboard.setText(AnnotatedString(json))
                            onShowBanner("Copied \"${tpl.name}\" template JSON to clipboard")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON")
                        }
                    }
                }
            }
        }
    }

    selectedForProject?.let { tpl ->
        AlertDialog(
            onDismissRequest = { selectedForProject = null },
            title = { Text("Create Project from ${tpl.name}") },
            text = {
                OutlinedTextField(
                    value = projectNameInput,
                    onValueChange = { projectNameInput = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (projectNameInput.isNotBlank()) {
                        onApplyTemplateToNewProject(tpl, projectNameInput)
                        selectedForProject = null
                    }
                }) {
                    Text("Create Workspace")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForProject = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StatisticsAndActivityScreen(
    projects: List<ProjectEntity>,
    savePoints: List<SavePointEntity>,
    missions: List<MissionEntity>,
    focusSessions: List<FocusSessionEntity>,
    showStatsEnabled: Boolean,
    onToggleShowStats: (Boolean) -> Unit
) {
    val completedProjects = projects.count { it.status == ProjectStatus.COMPLETED.name }
    val completedMissions = missions.count { it.status == MissionStatus.DONE.name }
    val totalFocusMinutes = focusSessions.sumOf { it.actualElapsedSeconds } / 60
    val completedMilestones = projects.sumOf { p -> p.parsedMilestones().count { it.isCompleted } }
    var selectedDayIndex by remember { mutableIntStateOf(6) }

    val dayStats = remember(savePoints, focusSessions) {
        val cal = Calendar.getInstance()
        val dayFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val shortFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val nowMillis = System.currentTimeMillis()
        (6 downTo 0).map { daysAgo ->
            val startOfDay = nowMillis - (daysAgo + 1) * 86_400_000L
            val endOfDay = nowMillis - daysAgo * 86_400_000L
            cal.timeInMillis = endOfDay
            val spInDay = savePoints.count { it.timestamp in startOfDay..endOfDay }
            val focusMinsInDay = (focusSessions.filter { it.startedAt in startOfDay..endOfDay }.sumOf { it.actualElapsedSeconds } / 60L).toInt()
            DayStatBucket(
                shortLabel = shortFmt.format(cal.time).take(3),
                fullLabel = dayFmt.format(cal.time),
                savePointsCount = spInDay,
                focusMinutes = focusMinsInDay,
                combinedScore = (spInDay * 15 + focusMinsInDay).coerceAtLeast(if (daysAgo == 0 && savePoints.isNotEmpty()) 18 else 4)
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("statistics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Statistics, Charts & Timeline", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "100% local visual analytics with interactive charts & zero pressure scoring.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = showStatsEnabled, onCheckedChange = onToggleShowStats)
            }
        }

        if (!showStatsEnabled) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Calm Mode Active: Statistics and numerical counters are hidden so you can focus purely on your current checkpoint.",
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryTile("Projects", "${projects.size} ($completedProjects done)", Modifier.weight(1f))
                    StatSummaryTile("Save Points", "${savePoints.size} captured", Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryTile("Missions Done", "$completedMissions / ${missions.size}", Modifier.weight(1f))
                    StatSummaryTile("Focus Time", "${totalFocusMinutes}m ($completedMilestones milestones)", Modifier.weight(1f))
                }
            }

            // Chart 1: Interactive 7-Day Momentum Bar Chart
            item {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val maxScore = (dayStats.maxOfOrNull { it.combinedScore } ?: 30).coerceAtLeast(20)
                val selectedDay = dayStats.getOrNull(selectedDayIndex) ?: dayStats.last()

                ElevatedCard(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_momentum_bar_chart")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("7-Day Save Point & Focus Momentum", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${selectedDay.fullLabel}: ${selectedDay.savePointsCount} Save Points • ${selectedDay.focusMinutes}m Focus",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            val count = dayStats.size
                            val gap = 12.dp.toPx()
                            val barW = ((size.width - gap * (count - 1)) / count).coerceAtLeast(10f)
                            dayStats.forEachIndexed { idx, d ->
                                val ratio = (d.combinedScore.toFloat() / maxScore.toFloat()).coerceIn(0.12f, 1f)
                                val barH = size.height * ratio
                                val x = idx * (barW + gap)
                                val y = size.height - barH

                                drawRoundRect(
                                    color = trackColor,
                                    topLeft = Offset(x, 0f),
                                    size = Size(barW, size.height),
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = if (idx == selectedDayIndex) {
                                            listOf(primaryColor, secondaryColor)
                                        } else {
                                            listOf(primaryColor.copy(alpha = 0.65f), secondaryColor.copy(alpha = 0.45f))
                                        },
                                        startY = y,
                                        endY = size.height
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barW, barH),
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayStats.forEachIndexed { idx, d ->
                                FilterChip(
                                    selected = idx == selectedDayIndex,
                                    onClick = { selectedDayIndex = idx },
                                    label = { Text(d.shortLabel, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            // Chart 2: Project Workload & Save Point Donut Ring Chart + Distribution
            item {
                val fallbackColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                ElevatedCard(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_project_donut_chart")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Workload & Save Point Distribution", style = MaterialTheme.typography.titleMedium)

                        if (projects.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(104.dp)
                                ) {
                                    val projectSlices = projects.map { proj ->
                                        val weight = (savePoints.count { it.projectId == proj.id } * 2 +
                                            missions.count { it.projectId == proj.id } + 1).toFloat()
                                        parseHexColorSafely(proj.colorHex, fallbackColor) to weight
                                    }
                                    val totalWeight = projectSlices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)

                                    Canvas(modifier = Modifier.size(104.dp)) {
                                        val stroke = 14.dp.toPx()
                                        drawArc(
                                            color = trackColor,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = stroke)
                                        )
                                        var startAngle = -90f
                                        projectSlices.forEach { (sliceColor, w) ->
                                            val sweep = (w / totalWeight) * 360f
                                            drawArc(
                                                color = sliceColor,
                                                startAngle = startAngle,
                                                sweepAngle = (sweep - 4f).coerceAtLeast(8f),
                                                useCenter = false,
                                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                                            )
                                            startAngle += sweep
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${savePoints.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("Saves", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    projects.forEach { proj ->
                                        val spCount = savePoints.count { it.projectId == proj.id }
                                        val mCount = missions.count { it.projectId == proj.id }
                                        val ratio = if (savePoints.isNotEmpty()) spCount.toFloat() / savePoints.size.toFloat() else 0.35f
                                        val pColor = parseHexColorSafely(proj.colorHex, fallbackColor)
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(pColor)
                                                )
                                                Text(
                                                    text = "${proj.name}: $spCount Saves • $mCount Missions",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            LinearProgressIndicator(
                                                progress = { ratio.coerceIn(0.10f, 1f) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = pColor
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Create a project or load example projects to inspect workload distribution.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Chronological Project Activity Timeline
        item {
            Text("Project Activity Timeline", style = MaterialTheme.typography.titleMedium)
        }
        items(savePoints.take(10), key = { it.id }) { sp ->
            val pName = projects.find { it.id == sp.projectId }?.name ?: "Project"
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("$pName • Save Point", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(sp.title, fontWeight = FontWeight.Bold)
                    if (sp.nextAction.isNotBlank()) {
                        Text("Next: ${sp.nextAction}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private data class DayStatBucket(
    val shortLabel: String,
    val fullLabel: String,
    val savePointsCount: Int,
    val focusMinutes: Int,
    val combinedScore: Int
)

@Composable
private fun StatSummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun NativeWidgetsGalleryScreen(
    projects: List<ProjectEntity>,
    savePoints: List<SavePointEntity>,
    missions: List<MissionEntity>,
    onTriggerQuickAction: (String) -> Unit
) {
    val activeProj = projects.firstOrNull()
    val latestSp = savePoints.firstOrNull()
    val nextMission = missions.firstOrNull { it.status != MissionStatus.DONE.name }

    val nativeWidgets = listOf(
        Triple("1. Continue Project Widget", activeProj?.name ?: "Workspace", "Resume + Quick Save dual action"),
        Triple("2. Current Mission Widget", nextMission?.title ?: "Mission", "Status: ${nextMission?.status ?: "ACTIVE"} + Launch Focus"),
        Triple("3. Quick Save Point Widget", "1-Tap Save Progress", "Latest: ${latestSp?.title ?: "Ready"}"),
        Triple("4. Active Projects Widget", "${projects.count { it.status == ProjectStatus.ACTIVE.name }} Active Workspaces", "Switch project or launch Rescue Mode"),
        Triple("5. Focus Timer Widget", "Pomodoro & Deep Work", "Launch offline Focus Studio + Save Point"),
        Triple("6. Project Progress Widget", "Mission & Milestone Bar", "${missions.count { it.status == MissionStatus.DONE.name }}/${missions.size} completed"),
        Triple("7. Next Mission Widget", nextMission?.title ?: "Queue Clear", "Priority: ${nextMission?.priority ?: "MEDIUM"}"),
        Triple("8. Pinned Project Widget", projects.firstOrNull { it.isPinned }?.name ?: "Pinned Workspace", "Instant launcher shortcut"),
        Triple("9. Recent Checkpoints Widget", latestSp?.title ?: "Timeline", "Next: ${latestSp?.nextAction ?: "Capture checkpoint"}"),
        Triple("10. Quick Capture Widget", "+ Idea / Mission / Link", "Zero-friction home screen intake")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("native_widgets_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("10 Native Android Home-Screen Widgets", style = MaterialTheme.typography.headlineMedium)
            Text(
                "All 10 AppWidgetProviders are registered in AndroidManifest.xml with dual PendingIntent actions and automatically refresh when local Room data changes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(nativeWidgets, key = { it.first }) { (title, headline, sub) ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Text(headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizationAndVaultScreen(
    preferences: UserPreferencesState,
    exportedJson: String,
    backupValidationResult: BackupValidationResult?,
    hasSampleProjects: Boolean = false,
    onSetThemeMode: (ReturnmarkThemeMode) -> Unit,
    onSetAppearanceMode: (ThemeAppearanceMode) -> Unit = {},
    onSetCustomAccent: (String) -> Unit,
    onSetDensity: (LayoutDensityMode) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onSetAutoPromptSavePoint: (Boolean) -> Unit,
    onSetFocusNotifications: (Boolean) -> Unit = {},
    onSetPersistentContextNotification: (Boolean) -> Unit = {},
    onGenerateBackupJson: () -> Unit,
    onExportBackupToUri: (Uri) -> Unit = {},
    onImportBackupFromUri: (Uri, (String) -> Unit) -> Unit = { _, _ -> },
    onValidateBackupJson: (String) -> Unit,
    onConfirmRestore: () -> Unit,
    onClearBackupValidation: () -> Unit,
    onLoadSampleWorkspace: () -> Unit = {},
    onRemoveSampleWorkspace: () -> Unit = {},
    onShowBanner: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var restoreInputJson by remember { mutableStateOf("") }
    val accentSwatches = listOf(
        "#C86D85", "#598B6E", "#5386B6", "#C97A3E", "#9E8CE8", "#5CC8C2",
        "#7E5285", "#497D5E", "#8A6547", "#7AA6E8", "#8B6FE8", "#3B9AE1",
        "#2BB67D", "#F07B52", "#E25B88", "#1CA3BA", "#CCFF00", "#34D399"
    )

    // Runtime permission launcher for Android 13+ local notifications
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onShowBanner("Local offline notifications enabled")
        } else {
            onShowBanner("Notification permission denied; alerts will remain in-app only")
        }
    }

    // Native Android Document Picker for Exporting .JSON Backup File
    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportBackupToUri(uri)
        }
    }

    // Native Android Document Picker for Selecting & Validating .JSON Backup File
    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportBackupFromUri(uri) { loadedText ->
                restoreInputJson = loadedText
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("customization_and_vault_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Customization Studio & Backup Vault", style = MaterialTheme.typography.headlineMedium)
            PrivacyPolicyPanel()
            Text(
                "100% free Lofi Japanese stationery & Kyoto café themes, instant Washi Light / Rainy Dark / True OLED / System mode, local reminders, and atomic JSON backup vault.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Backup & Restore Vault Card (Native SAF File Export/Import + Atomic Rollback Protection)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BACKUP & RESTORE VAULT (JSON v3)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                    // Explicit Unencrypted Plaintext Export Warning
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unencrypted_backup_warning")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Privacy Notice: Fumipoka JSON exports are unencrypted, human-readable plaintext files. Android automatic cloud backup is disabled (allowBackup=false). The system document picker may offer a cloud provider chosen and managed by you. Store exports in a secure location. Import limit: 16 MiB.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                exportDocumentLauncher.launch("fumipoka-backup-${System.currentTimeMillis()}.json")
                            },
                            modifier = Modifier.testTag("export_backup_file_button")
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Backup .JSON File")
                        }

                        OutlinedButton(
                            onClick = onGenerateBackupJson,
                            modifier = Modifier.testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate in App")
                        }

                        if (exportedJson.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(exportedJson))
                                    restoreInputJson = exportedJson
                                    onShowBanner("Backup JSON copied to clipboard & loaded into preview")
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy & Load")
                            }
                        }
                    }

                    if (exportedJson.isNotBlank()) {
                        OutlinedTextField(
                            value = exportedJson,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Exported Fumipoka v3 JSON (Unencrypted)") },
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exported_json_output")
                        )
                    }

                    HorizontalDivider()
                    Text("Validate & Restore Backup", style = MaterialTheme.typography.titleMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                importDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            modifier = Modifier.testTag("select_backup_file_button")
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select .JSON Backup File")
                        }

                        Button(
                            onClick = { onValidateBackupJson(restoreInputJson) },
                            modifier = Modifier.testTag("validate_backup_button")
                        ) {
                            Text("Validate & Preview")
                        }
                    }

                    OutlinedTextField(
                        value = restoreInputJson,
                        onValueChange = { restoreInputJson = it },
                        label = { Text("Or Paste Backup JSON to Validate") },
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restore_json_input")
                    )

                    when (backupValidationResult) {
                        is BackupValidationResult.Invalid -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("backup_validation_error")
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(backupValidationResult.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }

                        is BackupValidationResult.Valid -> {
                            val p = backupValidationResult.preview
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("backup_validation_preview")
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Backup Verified (Schema v${p.schemaVersion})", fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        "Contains: ${p.projectsCount} Projects • ${p.savePointsCount} Save Points • ${p.missionsCount} Missions • ${p.referencesCount} References • ${p.focusSessionsCount} Focus Sessions • ${p.rescueSessionsCount} Rescue Logs"
                                    )
                                    Text(
                                        "Confirming restore will atomically replace current Room records and DataStore preferences (with automatic rollback if any failure occurs).",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = onConfirmRestore,
                                            modifier = Modifier.testTag("confirm_restore_button")
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Confirm & Replace Data")
                                        }
                                        TextButton(onClick = onClearBackupValidation) {
                                            Text("Cancel")
                                        }
                                    }
                                }
                            }
                        }

                        null -> {}
                    }
                }
            }
        }

        // Theme Studio Card (Appearance Switcher + 10 Lofi Japanese Themes + 7 Pastel Themes + 7 Classic Themes)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "APPEARANCE MODE (WASHI LIGHT / RAINY DARK / TRUE OLED / SYSTEM)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeAppearanceMode.entries.forEach { appMode ->
                            val selected = preferences.appearanceMode == appMode
                            FilterChip(
                                selected = selected,
                                onClick = { onSetAppearanceMode(appMode) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (appMode) {
                                            ThemeAppearanceMode.LIGHT -> Icons.Default.LightMode
                                            ThemeAppearanceMode.DARK -> Icons.Default.DarkMode
                                            ThemeAppearanceMode.OLED -> Icons.Default.Contrast
                                            ThemeAppearanceMode.SYSTEM -> Icons.Default.BrightnessAuto
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(appMode.label) },
                                modifier = Modifier.testTag("appearance_mode_${appMode.name.lowercase()}")
                            )
                        }
                    }

                    HorizontalDivider()
                    Text(
                        text = "🍵 LOFI JAPANESE & KYOTO CAFÉ PALETTES (100% FREE)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ReturnmarkThemeMode.entries.filter { it.isLofiJapanese }.forEach { mode ->
                        ThemeOptionRow(
                            mode = mode,
                            selected = preferences.themeMode == mode,
                            onSelect = { onSetThemeMode(mode) }
                        )
                    }

                    HorizontalDivider()
                    Text(
                        text = "SIGNATURE PASTEL COLOR THEMES (100% FREE)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ReturnmarkThemeMode.entries.filter { it.isPastelSignature && !it.isLofiJapanese }.forEach { mode ->
                        ThemeOptionRow(
                            mode = mode,
                            selected = preferences.themeMode == mode,
                            onSelect = { onSetThemeMode(mode) }
                        )
                    }

                    HorizontalDivider()
                    Text(
                        text = "CLASSIC & ACCESSIBILITY STUDIO THEMES (100% FREE)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ReturnmarkThemeMode.entries.filter { !it.isPastelSignature && !it.isLofiJapanese }.forEach { mode ->
                        ThemeOptionRow(
                            mode = mode,
                            selected = preferences.themeMode == mode,
                            onSelect = { onSetThemeMode(mode) }
                        )
                    }

                    HorizontalDivider()
                    Text("Custom Stationery & Lantern Accent Ink", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        accentSwatches.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafely(hex))
                                    .border(
                                        width = if (preferences.customAccentHex.equals(hex, true)) 3.dp else 1.dp,
                                        color = if (preferences.customAccentHex.equals(hex, true)) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { onSetCustomAccent(hex) }
                                    .testTag("accent_swatch_${hex.removePrefix("#").lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // Layout Density, Typography, Notifications & Accessibility Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "LAYOUT, TYPOGRAPHY, REMINDERS & ACCESSIBILITY",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LayoutDensityMode.entries.forEach { d ->
                            FilterChip(
                                selected = preferences.layoutDensity == d,
                                onClick = { onSetDensity(d) },
                                label = { Text("${d.label} Density") }
                            )
                        }
                    }

                    Text("Font Scale (${String.format("%.2f", preferences.fontScale)}x)", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = preferences.fontScale,
                        onValueChange = onSetFontScale,
                        valueRange = 0.85f..1.30f
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reduced Motion (Accessibility)")
                        Switch(checked = preferences.reducedMotion, onCheckedChange = onSetReducedMotion)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prompt Save Point after Focus Session")
                        Switch(checked = preferences.autoPromptSavePointAfterFocus, onCheckedChange = onSetAutoPromptSavePoint)
                    }

                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Local Offline Notifications & Context Shade", style = MaterialTheme.typography.titleSmall)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Focus Session Completion Alert")
                            Text(
                                "Notify when a Pomodoro or Deep Work timer finishes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.focusNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !ReturnmarkNotificationHelper.hasNotificationPermission(context)
                                ) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                onSetFocusNotifications(enabled)
                            },
                            modifier = Modifier.testTag("toggle_focus_notifications")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pin Active Save Point in Notification Shade")
                            Text(
                                "Keep your current project's Next Step visible for instant 1-tap resume",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.persistentContextNotification,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !ReturnmarkNotificationHelper.hasNotificationPermission(context)
                                ) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                onSetPersistentContextNotification(enabled)
                            },
                            modifier = Modifier.testTag("toggle_pinned_context_notification")
                        )
                    }

                    HorizontalDivider()
                    Text("Opt-In Example Projects", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Sample projects are never automatically re-created when you delete them. Load or remove them anytime:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onLoadSampleWorkspace,
                            modifier = Modifier.testTag("vault_load_sample_button")
                        ) {
                            Text("Load Example Workspace")
                        }
                        if (hasSampleProjects) {
                            OutlinedButton(
                                onClick = onRemoveSampleWorkspace,
                                modifier = Modifier.testTag("vault_remove_sample_button")
                            ) {
                                Text("Remove Examples")
                            }
                        }
                    }
                }
            }
        }


    }
}

@Composable
private fun ThemeOptionRow(
    mode: ReturnmarkThemeMode,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onSelect)
            .padding(12.dp)
            .testTag("theme_option_${mode.name.lowercase()}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            )
            Text(
                text = mode.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(parseHexColorSafely(mode.previewBgHex))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(parseHexColorSafely(mode.previewAccentHex))
            )
        }
    }
}
