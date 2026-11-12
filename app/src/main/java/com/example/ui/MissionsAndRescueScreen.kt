package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.MissionEntity
import com.example.data.MissionPriority
import com.example.data.MissionStatus
import com.example.data.ProjectEntity
import com.example.data.RecurrenceRule
import com.example.data.ReferenceItemEntity
import com.example.data.RescueSessionEntity
import com.example.data.SavePointEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissionsScreen(
    projects: List<ProjectEntity>,
    selectedProjectId: Long,
    missions: List<MissionEntity>,
    viewMode: MissionViewMode,
    onChangeViewMode: (MissionViewMode) -> Unit,
    onCreateMission: (
        projectId: Long,
        title: String,
        description: String,
        status: MissionStatus,
        priority: MissionPriority,
        dueDateMillis: Long?,
        tags: String,
        subtasks: List<String>,
        checklists: List<String>,
        dependsOnId: Long?,
        recurrence: RecurrenceRule
    ) -> Unit,
    onToggleComplete: (MissionEntity) -> Unit,
    onUpdateStatus: (MissionEntity, MissionStatus) -> Unit,
    onToggleSubtask: (MissionEntity, String, Boolean) -> Unit,
    onAddDependencySafely: (MissionEntity, Long) -> Unit,
    onDuplicateMission: (MissionEntity) -> Unit,
    onBulkUpdateStatus: (Set<Long>, MissionStatus) -> Unit,
    onReorderMission: (MissionEntity, Int) -> Unit,
    onStartFocusForMission: (MissionEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterProjectOnly by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedIdsForBulk by remember { mutableStateOf(setOf<Long>()) }
    var dependencyTargetMission by remember { mutableStateOf<MissionEntity?>(null) }

    val filteredMissions = missions.filter { m ->
        (!filterProjectOnly || m.projectId == selectedProjectId) &&
            (viewMode != MissionViewMode.COMPLETED || m.status == MissionStatus.DONE.name) &&
            (searchQuery.isBlank() ||
                m.title.contains(searchQuery, ignoreCase = true) ||
                m.description.contains(searchQuery, ignoreCase = true) ||
                m.tags.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("missions_screen"),
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
                    Text("Mission Management 2.0", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Subtasks, checklists, dependencies (cycle-guarded), recurrence, and 6 views.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_mission_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Mission")
                }
            }
        }

        // 6 Mission Views Switcher
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MissionViewMode.entries.forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { onChangeViewMode(mode) },
                        label = { Text(mode.label) }
                    )
                }
                FilterChip(
                    selected = filterProjectOnly,
                    onClick = { filterProjectOnly = !filterProjectOnly },
                    label = { Text("Active Project Only") }
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search missions, tags, or descriptions...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bulk Editing Toolbar when missions are checked for bulk action
        if (selectedIdsForBulk.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${selectedIdsForBulk.size} selected", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(onClick = {
                                onBulkUpdateStatus(selectedIdsForBulk, MissionStatus.DONE)
                                selectedIdsForBulk = emptySet()
                            }) { Text("Mark Done") }
                            TextButton(onClick = {
                                onBulkUpdateStatus(selectedIdsForBulk, MissionStatus.IN_PROGRESS)
                                selectedIdsForBulk = emptySet()
                            }) { Text("In Progress") }
                            TextButton(onClick = { selectedIdsForBulk = emptySet() }) { Text("Clear") }
                        }
                    }
                }
            }
        }

        // Render according to selected MissionViewMode
        when (viewMode) {
            MissionViewMode.KANBAN -> {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            MissionStatus.NEXT_UP,
                            MissionStatus.IN_PROGRESS,
                            MissionStatus.BLOCKED,
                            MissionStatus.DONE
                        ).forEach { colStatus ->
                            val colItems = filteredMissions.filter { it.status == colStatus.name }
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .width(270.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${colStatus.label.uppercase()} (${colItems.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    colItems.forEach { m ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(m.title, style = MaterialTheme.typography.titleSmall)
                                                Text("Priority: ${m.priority}", style = MaterialTheme.typography.labelSmall)
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    TextButton(onClick = { onToggleComplete(m) }) {
                                                        Text(if (m.status == MissionStatus.DONE.name) "Reopen" else "Complete")
                                                    }
                                                    TextButton(onClick = { onStartFocusForMission(m) }) {
                                                        Text("Focus")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            MissionViewMode.CALENDAR, MissionViewMode.TIMELINE -> {
                val dateFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                val sortedByDue = filteredMissions.sortedBy { it.dueDateMillis ?: Long.MAX_VALUE }
                items(sortedByDue, key = { it.id }) { m ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = m.dueDateMillis?.let { "Due: ${dateFmt.format(Date(it))}" } ?: "Unscheduled / Ongoing",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(m.title, style = MaterialTheme.typography.titleMedium)
                                Text("Status: ${m.status} • Order #${m.manualOrder}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onToggleComplete(m) }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Toggle complete")
                            }
                        }
                    }
                }
            }

            MissionViewMode.COMPACT -> {
                items(filteredMissions, key = { it.id }) { m ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = m.status == MissionStatus.DONE.name,
                                    onCheckedChange = { onToggleComplete(m) }
                                )
                                Text(
                                    text = m.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (m.status == MissionStatus.DONE.name) TextDecoration.LineThrough else null
                                )
                            }
                            Text(m.priority, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            else -> {
                // Detailed LIST and COMPLETED views
                items(filteredMissions, key = { it.id }) { m ->
                    val projName = projects.find { it.id == m.projectId }?.name ?: "Workspace"
                    val isBulkSelected = selectedIdsForBulk.contains(m.id)
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mission_card_${m.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isBulkSelected,
                                        onCheckedChange = { checked ->
                                            selectedIdsForBulk = if (checked) selectedIdsForBulk + m.id else selectedIdsForBulk - m.id
                                        }
                                    )
                                    Text(
                                        text = "$projName • ${m.priority} • ${m.status}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(onClick = { onReorderMission(m, -1) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                                    }
                                    IconButton(onClick = { onReorderMission(m, 1) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                                    }
                                    IconButton(onClick = { dependencyTargetMission = m }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Link, contentDescription = "Link dependency")
                                    }
                                    IconButton(onClick = { onDuplicateMission(m) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate mission")
                                    }
                                }
                            }

                            Text(
                                text = m.title,
                                style = MaterialTheme.typography.titleLarge,
                                textDecoration = if (m.status == MissionStatus.DONE.name) TextDecoration.LineThrough else null
                            )
                            if (m.description.isNotBlank()) {
                                Text(m.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (m.recurrenceRule != RecurrenceRule.NONE.name) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Recurring: ${m.recurrenceRule}", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            val deps = m.parsedDependencies()
                            if (deps.isNotEmpty()) {
                                Text(
                                    text = "Depends on Mission IDs: ${deps.joinToString(", ") { "#$it" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Subtasks
                            val subtasks = m.parsedSubtasks()
                            if (subtasks.isNotEmpty()) {
                                Text("Subtasks:", style = MaterialTheme.typography.labelSmall)
                                subtasks.forEach { st ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onToggleSubtask(m, st.id, false) }
                                    ) {
                                        Checkbox(checked = st.isDone, onCheckedChange = { onToggleSubtask(m, st.id, false) })
                                        Text(st.title, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Checklist
                            val checklist = m.parsedChecklist()
                            if (checklist.isNotEmpty()) {
                                Text("Verification Checklist:", style = MaterialTheme.typography.labelSmall)
                                checklist.forEach { ck ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onToggleSubtask(m, ck.id, true) }
                                    ) {
                                        Checkbox(checked = ck.isDone, onCheckedChange = { onToggleSubtask(m, ck.id, true) })
                                        Text(ck.title, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { onToggleComplete(m) }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (m.status == MissionStatus.DONE.name) "Reopen" else "Complete")
                                }
                                OutlinedButton(onClick = { onStartFocusForMission(m) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Focus Session")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(MissionPriority.HIGH) }
        var recurrence by remember { mutableStateOf(RecurrenceRule.NONE) }
        var subtasksCsv by remember { mutableStateOf("") }
        var checklistCsv by remember { mutableStateOf("") }
        var tags by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Actionable Mission") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Mission Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_mission_title_input")
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description & Context") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subtasksCsv,
                        onValueChange = { subtasksCsv = it },
                        label = { Text("Subtasks (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = checklistCsv,
                        onValueChange = { checklistCsv = it },
                        label = { Text("Checklist items (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Priority", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MissionPriority.entries.forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p.label) }
                            )
                        }
                    }
                    Text("Recurrence", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RecurrenceRule.entries.forEach { r ->
                            FilterChip(
                                selected = recurrence == r,
                                onClick = { recurrence = r },
                                label = { Text(r.label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateMission(
                                selectedProjectId,
                                title,
                                desc,
                                MissionStatus.NEXT_UP,
                                priority,
                                System.currentTimeMillis() + 86_400_000L * 2,
                                tags,
                                subtasksCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                checklistCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                null,
                                recurrence
                            )
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_mission_button")
                ) {
                    Text("Add Mission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    dependencyTargetMission?.let { sourceMission ->
        val candidates = missions.filter { it.id != sourceMission.id }
        AlertDialog(
            onDismissRequest = { dependencyTargetMission = null },
            title = { Text("Add Prerequisite Dependency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Select a mission that \"${sourceMission.title}\" depends on. Circular dependencies are automatically blocked.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    candidates.forEach { candidate ->
                        OutlinedButton(
                            onClick = {
                                onAddDependencySafely(sourceMission, candidate.id)
                                dependencyTargetMission = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("#${candidate.id}: ${candidate.title}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dependencyTargetMission = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun RescueModeScreen(
    projects: List<ProjectEntity>,
    selectedProjectId: Long,
    savePoints: List<SavePointEntity>,
    missions: List<MissionEntity>,
    references: List<ReferenceItemEntity>,
    rescueHistory: List<RescueSessionEntity>,
    onSelectProject: (Long) -> Unit,
    onLaunchRescueFocusSession: (
        projectId: Long,
        whereStopped: String,
        whatIsBlocking: String,
        smallestNextAction: String,
        whatToOpen: String,
        plannedWorkMinutes: Int
    ) -> Unit
) {
    val activeProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()
    val lastSavePoint = savePoints.firstOrNull { it.projectId == activeProject?.id && !it.isArchived }
    val mostRecentCompletedMission = missions.firstOrNull {
        it.projectId == activeProject?.id && it.status == MissionStatus.DONE.name
    }
    val lastUnfinishedMission = missions.firstOrNull {
        it.projectId == activeProject?.id && it.status != MissionStatus.DONE.name
    }
    val relevantRefs = references.filter { it.projectId == activeProject?.id }.take(3)

    var whereStopped by remember(activeProject, lastSavePoint) {
        mutableStateOf(lastSavePoint?.happeningNow ?: activeProject?.currentContextSummary ?: "")
    }
    var whatIsBlocking by remember(activeProject, lastSavePoint) {
        mutableStateOf(activeProject?.openBlockers?.ifBlank { lastSavePoint?.blockers ?: "" } ?: "")
    }
    var smallestNextAction by remember(activeProject, lastSavePoint) {
        mutableStateOf(activeProject?.nextManualStep?.ifBlank { lastSavePoint?.nextAction ?: "" } ?: "")
    }
    var whatToOpen by remember(relevantRefs) {
        mutableStateOf(relevantRefs.joinToString(", ") { it.title })
    }
    var workMinutes by remember { mutableIntStateOf(15) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rescue_mode_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Rescue Mode 2.0", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Distraction-free recovery for stalled projects. No AI—just your deterministic context and one small step.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Project Selector
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                projects.forEach { proj ->
                    FilterChip(
                        selected = proj.id == activeProject?.id,
                        onClick = { onSelectProject(proj.id) },
                        label = { Text(proj.name) }
                    )
                }
            }
        }

        // 7 Recovery Anchors Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("RECOVERY CONTEXT SNAPSHOT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("1. Last Save Point: ${lastSavePoint?.title ?: "None recorded"}", fontWeight = FontWeight.SemiBold)
                    Text("2. Most Recent Successful Mission: ${mostRecentCompletedMission?.title ?: "None completed yet"}")
                    Text("3. Last Unfinished Action: ${lastUnfinishedMission?.title ?: "No open mission"}")
                    Text(
                        text = "4. Current Recorded Blockers: ${whatIsBlocking.ifBlank { "No active blockers recorded" }}",
                        color = if (whatIsBlocking.isNotBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "5. Relevant References: ${if (relevantRefs.isNotEmpty()) relevantRefs.joinToString(" • ") { it.title } else "No linked references"}"
                    )
                    Text(
                        text = "6. User-Written Next Step: ${smallestNextAction.ifBlank { "Write a 5-minute action below" }}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 5 Structured Recovery Questions
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("STRUCTURED RECOVERY QUESTIONS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = whereStopped,
                        onValueChange = { whereStopped = it },
                        label = { Text("1. Where did I stop?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = whatIsBlocking,
                        onValueChange = { whatIsBlocking = it },
                        label = { Text("2. What is blocking me?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = smallestNextAction,
                        onValueChange = { smallestNextAction = it },
                        label = { Text("3. What's the smallest useful next action?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rescue_smallest_action_input")
                    )
                    OutlinedTextField(
                        value = whatToOpen,
                        onValueChange = { whatToOpen = it },
                        label = { Text("4. What do I need to open?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("5. How long do I want to work?", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 15, 25, 45).forEach { mins ->
                            FilterChip(
                                selected = workMinutes == mins,
                                onClick = { workMinutes = mins },
                                label = { Text("$mins min") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (activeProject != null) {
                                onLaunchRescueFocusSession(
                                    activeProject.id,
                                    whereStopped,
                                    whatIsBlocking,
                                    smallestNextAction.ifBlank { "Complete 1 small recovery step" },
                                    whatToOpen,
                                    workMinutes
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_rescue_focus_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("7. Save Recovery Checkpoint & Start $workMinutes-Min Focus")
                    }
                }
            }
        }

        // Local Rescue History
        item {
            Text("Local Rescue History (${rescueHistory.size})", style = MaterialTheme.typography.titleMedium)
        }
        items(rescueHistory, key = { it.id }) { session ->
            val pName = projects.find { it.id == session.projectId }?.name ?: "Project"
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("$pName • ${session.plannedWorkMinutes}m recovery sprint", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Smallest Action: ${session.smallestNextAction}", fontWeight = FontWeight.SemiBold)
                    if (session.whatIsBlocking.isNotBlank()) {
                        Text("Unblocked: ${session.whatIsBlocking}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
