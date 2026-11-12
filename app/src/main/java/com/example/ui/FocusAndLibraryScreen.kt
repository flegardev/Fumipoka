package com.example.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AmbientSoundType
import com.example.data.FocusModeType
import com.example.data.FocusSessionEntity
import com.example.data.MissionEntity
import com.example.data.ProjectEntity
import com.example.data.ReferenceItemEntity
import com.example.data.ReferenceType
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusStudioScreen(
    projects: List<ProjectEntity>,
    missions: List<MissionEntity>,
    selectedProjectId: Long,
    focusRuntime: ActiveFocusRuntimeState,
    focusHistory: List<FocusSessionEntity>,
    onConfigureFocus: (FocusModeType, Int, Long?, Long?) -> Unit,
    onAdjustPlannedMinutes: (Int) -> Unit = {},
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    onToggleZenMode: (Boolean) -> Unit,
    onSetAmbientSound: (AmbientSoundType) -> Unit,
    onDismissSavePointPrompt: () -> Unit,
    onOpenSavePointAfterFocus: () -> Unit
) {
    val customMins by remember { mutableIntStateOf(35) }
    val activeProject = projects.find { it.id == (focusRuntime.projectId ?: selectedProjectId) } ?: projects.firstOrNull()
    val projectMissions = missions.filter { it.projectId == activeProject?.id }
    val linkedMission = missions.find { it.id == focusRuntime.missionId } ?: projectMissions.firstOrNull()

    val displaySeconds = if (focusRuntime.mode == FocusModeType.STOPWATCH) {
        focusRuntime.elapsedSeconds
    } else {
        (focusRuntime.plannedSeconds - focusRuntime.elapsedSeconds).coerceAtLeast(0L)
    }
    val mm = displaySeconds / 60
    val ss = displaySeconds % 60
    val progress = if (focusRuntime.plannedSeconds > 0 && focusRuntime.mode != FocusModeType.STOPWATCH) {
        (focusRuntime.elapsedSeconds.toFloat() / focusRuntime.plannedSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        ((focusRuntime.elapsedSeconds % 60L).toFloat() / 60f)
    }
    val animatedRingProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "focus_ring_progress"
    )

    if (focusRuntime.isZenFullScreen) {
        // Full-Screen Zen Mode
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(28.dp)
                .testTag("focus_zen_fullscreen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "ZEN FOCUS • ${focusRuntime.mode.label.uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = activeProject?.name ?: "Deep Work",
                    style = MaterialTheme.typography.headlineMedium
                )
                if (linkedMission != null) {
                    Text(
                        text = "Mission: ${linkedMission.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = String.format(Locale.US, "%02d:%02d", mm, ss),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (focusRuntime.isRunning && !focusRuntime.isPaused) {
                        Button(onClick = onPause) { Text("Pause") }
                    } else {
                        Button(onClick = onStartOrResume) { Text("Resume") }
                    }
                    OutlinedButton(onClick = onFinish) { Text("Finish & Save") }
                    OutlinedButton(onClick = { onToggleZenMode(false) }) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exit Zen")
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("focus_studio_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Focus Studio", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Pomodoro, Deep Work, Stopwatch, Zen Mode & 100% offline synthesized ambient audio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = { onToggleZenMode(true) }) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Zen Mode")
                }
            }
        }

        // Mode Selector Chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FocusModeType.entries.forEach { mode ->
                    FilterChip(
                        selected = focusRuntime.mode == mode,
                        onClick = {
                            onConfigureFocus(
                                mode,
                                if (mode == FocusModeType.CUSTOM) customMins else mode.defaultMinutes,
                                activeProject?.id,
                                linkedMission?.id
                            )
                        },
                        label = {
                            Text(
                                if (mode == FocusModeType.STOPWATCH) "Stopwatch"
                                else "${mode.label} (${if (mode == FocusModeType.CUSTOM) customMins else mode.defaultMinutes}m)"
                            )
                        }
                    )
                }
            }
        }

        // Main Monotonic Circular Timer Display Card
        item {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant

            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "${activeProject?.name ?: "Workspace"} • ${linkedMission?.title ?: "General Focus"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Custom Canvas Circular Progress Ring around Timer Readout
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(196.dp)
                            .testTag("focus_circular_ring")
                    ) {
                        Canvas(modifier = Modifier.size(196.dp)) {
                            val strokeW = 14.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(primaryColor, secondaryColor, primaryColor)
                                ),
                                startAngle = -90f,
                                sweepAngle = (360f * animatedRingProgress).coerceAtLeast(8f),
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", mm, ss),
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier.testTag("focus_timer_readout")
                            )
                            Text(
                                text = if (focusRuntime.isRunning && !focusRuntime.isPaused) "IN FLOW"
                                else if (focusRuntime.isPaused) "PAUSED"
                                else focusRuntime.mode.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (focusRuntime.mode != FocusModeType.STOPWATCH) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onAdjustPlannedMinutes(-5) },
                                modifier = Modifier.testTag("focus_minus_5_button")
                            ) {
                                Text("-5m")
                            }
                            Text(
                                text = "Planned: ${focusRuntime.plannedSeconds / 60}m",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { onAdjustPlannedMinutes(5) },
                                modifier = Modifier.testTag("focus_plus_5_button")
                            ) {
                                Text("+5m")
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!focusRuntime.isRunning || focusRuntime.isPaused) {
                            Button(
                                onClick = onStartOrResume,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("focus_start_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (focusRuntime.isPaused) "Resume Focus" else "Start Focus")
                            }
                        } else {
                            Button(
                                onClick = onPause,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("focus_pause_button")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause")
                            }
                        }

                        OutlinedButton(
                            onClick = onFinish,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("focus_finish_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complete Session")
                        }
                    }

                    OutlinedTextField(
                        value = focusRuntime.sessionNotes,
                        onValueChange = onUpdateNotes,
                        label = { Text("Live Session Notes (preserved in Focus History)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Offline Procedural Ambient Sound Selector
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Offline Ambient Soundscape (Zero Network)", style = MaterialTheme.typography.titleMedium)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AmbientSoundType.entries.forEach { sound ->
                            FilterChip(
                                selected = focusRuntime.ambientSound == sound,
                                onClick = { onSetAmbientSound(sound) },
                                label = { Text(sound.label) }
                            )
                        }
                    }
                }
            }
        }

        // Focus History & Stats
        item {
            val totalMins = focusHistory.sumOf { it.actualElapsedSeconds } / 60
            Text(
                text = "Focus History (${focusHistory.size} sessions • $totalMins total mins)",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(focusHistory, key = { it.id }) { session ->
            val pName = projects.find { it.id == session.projectId }?.name ?: "Project"
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "$pName • ${session.mode} • ${session.actualElapsedSeconds / 60}m ${session.actualElapsedSeconds % 60}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (session.notes.isNotBlank()) {
                        Text(session.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    // Automatic Save Point Prompt at end of focus session
    if (focusRuntime.showCompletionSavePointPrompt) {
        AlertDialog(
            onDismissRequest = onDismissSavePointPrompt,
            title = { Text("Want to save where you stopped?") },
            text = {
                Text("Your focus session is complete. Capture a Save Point right now while your exact working context and next step are fresh.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissSavePointPrompt()
                        onOpenSavePointAfterFocus()
                    },
                    modifier = Modifier.testTag("prompt_save_where_stopped_confirm")
                ) {
                    Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save My Progress")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissSavePointPrompt) {
                    Text("Not Now")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    projects: List<ProjectEntity>,
    selectedProjectId: Long,
    references: List<ReferenceItemEntity>,
    incomingSharedText: String?,
    onConsumeSharedText: () -> Unit,
    onAddReference: (
        projectId: Long?,
        title: String,
        type: ReferenceType,
        contentOrUri: String,
        description: String,
        folder: String,
        category: String,
        tags: String,
        hasPersistableUri: Boolean
    ) -> Unit,
    onToggleFavorite: (ReferenceItemEntity) -> Unit,
    onDeleteReference: (Long) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    var draftTitle by remember { mutableStateOf("") }
    var draftContent by remember { mutableStateOf("") }
    var draftType by remember { mutableStateOf(ReferenceType.LINK) }
    var draftHasPersistableUri by remember { mutableStateOf(false) }

    // Automatically open Add Reference dialog if user shared text/link into Returnmark via Android Share Sheet
    LaunchedEffect(incomingSharedText) {
        if (!incomingSharedText.isNullOrBlank()) {
            draftContent = incomingSharedText
            draftTitle = incomingSharedText.lineSequence().firstOrNull()?.take(48) ?: "Shared Reference"
            draftType = if (incomingSharedText.startsWith("http")) ReferenceType.LINK else ReferenceType.SNIPPET
            showAddDialog = true
            onConsumeSharedText()
        }
    }

    // Android System Document Picker (Zero broad storage permissions)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var persisted = false
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                persisted = true
            } catch (_: Throwable) {
            }
            draftContent = uri.toString()
            if (draftTitle.isBlank()) {
                draftTitle = uri.lastPathSegment ?: "Attached Document"
            }
            draftType = ReferenceType.DOCUMENT
            draftHasPersistableUri = persisted
            showAddDialog = true
        }
    }

    val filtered = references.filter { ref ->
        (!favoritesOnly || ref.isFavorite) &&
            (selectedTypeFilter == "ALL" || ref.type == selectedTypeFilter) &&
            (searchQuery.isBlank() ||
                ref.title.contains(searchQuery, ignoreCase = true) ||
                ref.contentOrUri.contains(searchQuery, ignoreCase = true) ||
                ref.description.contains(searchQuery, ignoreCase = true) ||
                ref.folder.contains(searchQuery, ignoreCase = true) ||
                ref.tags.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("References & Knowledge Library", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Links, snippets, notes, checklists, and system document references organized by project.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { documentPickerLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick File")
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_reference_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search references, folders, categories, tags...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = favoritesOnly,
                    onClick = { favoritesOnly = !favoritesOnly },
                    label = { Text("★ Favorites") }
                )
                FilterChip(
                    selected = selectedTypeFilter == "ALL",
                    onClick = { selectedTypeFilter = "ALL" },
                    label = { Text("All Types (${references.size})") }
                )
                ReferenceType.entries.forEach { t ->
                    FilterChip(
                        selected = selectedTypeFilter == t.name,
                        onClick = { selectedTypeFilter = t.name },
                        label = { Text(t.label) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            val pName = projects.find { it.id == item.projectId }?.name ?: "Global Library"
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reference_card_${item.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.type} • $pName • Folder: ${item.folder}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            IconButton(onClick = { onToggleFavorite(item) }, modifier = Modifier.size(34.dp)) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite reference",
                                    tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteReference(item.id) }, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete reference")
                            }
                        }
                    }

                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (item.contentOrUri.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.contentOrUri,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    if (item.description.isNotBlank()) {
                        Text(item.description, style = MaterialTheme.typography.bodySmall)
                    }
                    if (item.type == ReferenceType.DOCUMENT.name || item.type == ReferenceType.FILE_REF.name) {
                        Text(
                            text = if (item.hasPersistableUriAccess) "URI Access: Persistable read grant retained"
                            else "URI Reference: External file pointer (re-pick via SAF if moved)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var desc by remember { mutableStateOf("") }
        var folder by remember { mutableStateOf("Architecture") }
        var category by remember { mutableStateOf("Core Docs") }
        var tags by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Save Reference to Knowledge Library") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { draftTitle = it },
                        label = { Text("Reference Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_reference_title_input")
                    )
                    OutlinedTextField(
                        value = draftContent,
                        onValueChange = { draftContent = it },
                        label = { Text("URL, Text Snippet, Checklist, or Document URI") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_reference_content_input")
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Why this matters / Context description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = folder,
                            onValueChange = { folder = it },
                            label = { Text("Folder") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Reference Type", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReferenceType.entries.forEach { t ->
                            FilterChip(
                                selected = draftType == t,
                                onClick = { draftType = t },
                                label = { Text(t.label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (draftTitle.isNotBlank()) {
                            onAddReference(
                                selectedProjectId,
                                draftTitle,
                                draftType,
                                draftContent,
                                desc,
                                folder,
                                category,
                                tags,
                                draftHasPersistableUri
                            )
                            draftTitle = ""
                            draftContent = ""
                            draftHasPersistableUri = false
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_reference_button")
                ) {
                    Text("Save Reference")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
