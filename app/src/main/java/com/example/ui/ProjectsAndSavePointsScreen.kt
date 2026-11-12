package com.example.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BackupVaultManager
import com.example.data.FocusSessionEntity
import com.example.data.MissionEntity
import com.example.data.ProjectEntity
import com.example.data.ProjectStatus
import com.example.data.ProjectTemplateModel
import com.example.data.ProjectTemplatesCatalog
import com.example.data.ReferenceItemEntity
import com.example.data.SavePointEntity
import com.example.ui.theme.parseHexColorSafely
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    selectedProjectId: Long,
    savePoints: List<SavePointEntity>,
    missions: List<MissionEntity>,
    references: List<ReferenceItemEntity>,
    focusSessions: List<FocusSessionEntity>,
    onSelectProject: (Long) -> Unit,
    onCreateProject: (String, String, String, String, String, ProjectTemplateModel) -> Unit,
    onTogglePin: (ProjectEntity) -> Unit,
    onToggleMilestone: (ProjectEntity, String) -> Unit,
    onAddMilestone: (ProjectEntity, String, String) -> Unit,
    onDeleteProjectConfirmed: (Long) -> Unit,
    onOpenSavePointSheet: (Boolean) -> Unit,
    onStartFocusForProject: (Long) -> Unit,
    onOpenRescueMode: () -> Unit,
    onLoadSampleWorkspace: () -> Unit = {},
    onRemoveSampleWorkspace: () -> Unit = {},
    onShowBanner: (String) -> Unit = {}
) {
    val clipboard = LocalClipboardManager.current
    var statusFilter by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectPendingDelete by remember { mutableStateOf<ProjectEntity?>(null) }
    var newMilestoneTitle by remember { mutableStateOf("") }

    val activeProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()
    val projectSavePoints = savePoints.filter { it.projectId == activeProject?.id && !it.isArchived }
    val latestCheckpoint = projectSavePoints.firstOrNull()
    val checkpointRefIds = latestCheckpoint?.parsedAttachedReferenceIds().orEmpty().toSet()
    val projectRefs = references.filter {
        it.projectId == activeProject?.id || checkpointRefIds.contains(it.id)
    }.distinctBy { it.id }.take(5)
    val lastSessionSecs = focusSessions.firstOrNull { it.projectId == activeProject?.id }?.actualElapsedSeconds
        ?: latestCheckpoint?.sessionDurationSeconds ?: 0L

    val filteredProjects = projects.filter {
        statusFilter == "ALL" || it.status == statusFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("projects_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Create Project button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Projects & Workspaces", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Select a project to inspect its exact working context and resume instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project")
                }
            }
        }

        if (projects.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("No Active Projects Yet", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Create your first real project workspace above, or load the opt-in example workspace to explore Save Points, Missions, and Rescue Mode.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { showCreateDialog = true }) {
                                Text("Create Real Project")
                            }
                            OutlinedButton(onClick = onLoadSampleWorkspace) {
                                Text("Load Opt-In Examples")
                            }
                        }
                    }
                }
            }
        }

        // Continue Where I Left Off — Full Resume Experience for Selected Project
        if (activeProject != null) {
            item {
                val accent = parseHexColorSafely(activeProject.colorHex, MaterialTheme.colorScheme.primary)
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .testTag("continue_where_left_off_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = accent.copy(alpha = 0.18f), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "CONTINUE WHERE I LEFT OFF",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                if (activeProject.isSampleData) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "SAMPLE",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Row {
                                IconButton(onClick = { onTogglePin(activeProject) }) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Pin project",
                                        tint = if (activeProject.isPinned) accent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { projectPendingDelete = activeProject },
                                    modifier = Modifier.testTag("delete_project_button")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete project",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Text(activeProject.name, style = MaterialTheme.typography.headlineLarge)
                        if (activeProject.tagline.isNotBlank()) {
                            Text(
                                activeProject.tagline,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider()

                        ResumeDetailRow("Last Working Context", activeProject.currentContextSummary.ifBlank { latestCheckpoint?.happeningNow ?: "Ready to start" })
                        ResumeDetailRow("Last Completed Action", activeProject.lastCompletedAction.ifBlank { latestCheckpoint?.completedSummary ?: "Project initialized" })
                        ResumeDetailRow(
                            label = "Next Recommended Manual Step",
                            value = activeProject.nextManualStep.ifBlank { latestCheckpoint?.nextAction ?: "Capture a Save Point" },
                            highlightColor = accent
                        )
                        if (activeProject.openBlockers.isNotBlank() || !latestCheckpoint?.blockers.isNullOrBlank()) {
                            ResumeDetailRow(
                                label = "Open Blockers",
                                value = activeProject.openBlockers.ifBlank { latestCheckpoint?.blockers ?: "" },
                                highlightColor = MaterialTheme.colorScheme.error
                            )
                        }
                        if (!latestCheckpoint?.notes.isNullOrBlank()) {
                            ResumeDetailRow("Relevant Checkpoint Notes", latestCheckpoint?.notes ?: "")
                        }
                        ResumeDetailRow(
                            "Previous Session Duration",
                            "${lastSessionSecs / 60}m (${activeProject.totalFocusSeconds / 60}m total project focus)"
                        )

                        if (projectRefs.isNotEmpty()) {
                            Text("Attached & Recent References:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            projectRefs.forEach { ref ->
                                val isFromCheckpoint = checkpointRefIds.contains(ref.id)
                                Text(
                                    text = "${if (isFromCheckpoint) "🔖 " else "• "}[${ref.type}] ${ref.title} — ${ref.contentOrUri}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Milestones Section
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Project Milestones", style = MaterialTheme.typography.titleSmall)
                        activeProject.parsedMilestones().forEach { ms ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleMilestone(activeProject, ms.id) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = ms.isCompleted,
                                    onCheckedChange = { onToggleMilestone(activeProject, ms.id) }
                                )
                                Text(
                                    text = "${ms.title} ${if (ms.targetDateLabel.isNotBlank()) "(${ms.targetDateLabel})" else ""}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newMilestoneTitle,
                                onValueChange = { newMilestoneTitle = it },
                                label = { Text("Add Milestone") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = {
                                    if (newMilestoneTitle.isNotBlank()) {
                                        onAddMilestone(activeProject, newMilestoneTitle, "Upcoming")
                                        newMilestoneTitle = ""
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStartFocusForProject(activeProject.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume Project")
                            }
                            OutlinedButton(onClick = { onOpenSavePointSheet(false) }) {
                                Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Point")
                            }
                            OutlinedButton(onClick = onOpenRescueMode) {
                                Text("Rescue")
                            }
                            OutlinedButton(
                                onClick = {
                                    val md = BackupVaultManager.exportProjectMarkdownBrief(
                                        project = activeProject,
                                        savePoints = savePoints,
                                        missions = missions,
                                        references = references
                                    )
                                    clipboard.setText(AnnotatedString(md))
                                    onShowBanner("Copied Markdown Project Brief to clipboard")
                                },
                                modifier = Modifier.testTag("export_project_markdown_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MD Brief")
                            }
                        }
                    }
                }
            }
        }

        // Status Filter Row
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf("ALL") + ProjectStatus.entries.map { it.name }).forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = status },
                        label = { Text(status) }
                    )
                }
                if (projects.any { it.isSampleData }) {
                    TextButton(onClick = onRemoveSampleWorkspace) {
                        Text("Remove Sample Projects")
                    }
                }
            }
        }

        // All Projects List
        items(filteredProjects, key = { it.id }) { project ->
            val isSelected = project.id == activeProject?.id
            val accent = parseHexColorSafely(project.colorHex)
            val spCount = savePoints.count { it.projectId == project.id }
            val mCount = missions.count { it.projectId == project.id }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectProject(project.id) }
                    .testTag("project_card_${project.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(project.name, style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            text = if (project.isSampleData) "${project.status} • SAMPLE" else project.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.currentContextSummary.ifBlank { project.tagline },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$spCount Save Points • $mCount Missions • Template: ${project.templateSource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, tagline, desc, color, icon, tpl ->
                onCreateProject(name, tagline, desc, color, icon, tpl)
                showCreateDialog = false
            }
        )
    }

    projectPendingDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text("Delete \"${proj.name}\"?") },
            text = {
                Text("This permanently deletes the project and its associated Save Points and Missions. Export a backup first if you may need to recover it.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProjectConfirmed(proj.id)
                        projectPendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_project_button")
                ) {
                    Text("Delete Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ResumeDetailRow(
    label: String,
    value: String,
    highlightColor: androidx.compose.ui.graphics.Color? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface,
            fontWeight = if (highlightColor != null) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, ProjectTemplateModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#8B6FE8") }
    var selectedTemplate by remember { mutableStateOf(ProjectTemplatesCatalog.builtInTemplates.first()) }
    val colors = listOf("#8B6FE8", "#3B9AE1", "#2BB67D", "#F07B52", "#E25B88", "#1CA3BA", "#CCFF00")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Project Workspace") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_name_input")
                )
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Short Goal / Tagline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Scope") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Project Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parseHexColorSafely(hex))
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                Text("Template (${selectedTemplate.name})", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProjectTemplatesCatalog.builtInTemplates.take(6).forEach { tpl ->
                        FilterChip(
                            selected = selectedTemplate.id == tpl.id,
                            onClick = { selectedTemplate = tpl },
                            label = { Text(tpl.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, tagline, description, selectedColor, selectedTemplate.iconName, selectedTemplate)
                    }
                },
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Text("Create Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavePointsScreen(
    projects: List<ProjectEntity>,
    selectedProjectId: Long,
    savePoints: List<SavePointEntity>,
    references: List<ReferenceItemEntity> = emptyList(),
    onOpenSavePointSheet: (Boolean) -> Unit,
    onTogglePin: (SavePointEntity) -> Unit,
    onToggleArchive: (SavePointEntity) -> Unit,
    onDuplicate: (SavePointEntity) -> Unit,
    onRestoreContext: (SavePointEntity) -> Unit,
    onUpdateSavePoint: (SavePointEntity, String, String, String, String, String, String) -> Unit,
    onShowBanner: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var pinnedOnly by remember { mutableStateOf(false) }
    var compareFirst by remember { mutableStateOf<SavePointEntity?>(null) }
    var compareSecond by remember { mutableStateOf<SavePointEntity?>(null) }
    var editingSavePoint by remember { mutableStateOf<SavePointEntity?>(null) }
    val clipboard = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()) }

    val filtered = savePoints.filter { sp ->
        (showArchived || !sp.isArchived) &&
            (!pinnedOnly || sp.isPinned) &&
            (searchQuery.isBlank() ||
                sp.title.contains(searchQuery, ignoreCase = true) ||
                sp.happeningNow.contains(searchQuery, ignoreCase = true) ||
                sp.nextAction.contains(searchQuery, ignoreCase = true) ||
                sp.tags.contains(searchQuery, ignoreCase = true) ||
                sp.notes.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("save_points_screen"),
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
                    Text("Save Point Timeline", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Chronological working context checkpoints. Compare diffs, pin, attach references, or restore earlier context.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onOpenSavePointSheet(false) },
                    modifier = Modifier.testTag("timeline_new_save_point_button")
                ) {
                    Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Progress")
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search checkpoints, actions, blockers, tags...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = pinnedOnly,
                        onClick = { pinnedOnly = !pinnedOnly },
                        label = { Text("Pinned Only") }
                    )
                    FilterChip(
                        selected = showArchived,
                        onClick = { showArchived = !showArchived },
                        label = { Text("Include Archived") }
                    )
                }
            }
        }

        // Side-by-side Checkpoint Comparison Card with Diff Highlighting
        if (compareFirst != null || compareSecond != null) {
            item {
                val a = compareFirst
                val b = compareSecond
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkpoint_comparison_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Checkpoint Comparison & Diff", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = {
                                compareFirst = null
                                compareSecond = null
                            }) {
                                Text("Clear Comparison")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Checkpoint A", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(a?.title ?: "Select 1st checkpoint", fontWeight = FontWeight.Bold)
                                Text("Done: ${a?.completedSummary ?: "-"}")
                                Text("Context: ${a?.happeningNow ?: "-"}")
                                Text("Next: ${a?.nextAction ?: "-"}")
                                Text("Blockers: ${a?.blockers?.ifBlank { "None" } ?: "-"}")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Checkpoint B", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(b?.title ?: "Tap Compare on 2nd checkpoint", fontWeight = FontWeight.Bold)
                                Text("Done: ${b?.completedSummary ?: "-"}")
                                Text("Context: ${b?.happeningNow ?: "-"}")
                                Text("Next: ${b?.nextAction ?: "-"}")
                                Text("Blockers: ${b?.blockers?.ifBlank { "None" } ?: "-"}")
                            }
                        }
                        if (a != null && b != null) {
                            HorizontalDivider()
                            val diffs = buildList {
                                if (a.happeningNow != b.happeningNow) add("Working Context changed")
                                if (a.nextAction != b.nextAction) add("Next Manual Action changed")
                                if (a.completedSummary != b.completedSummary) add("Completed Summary changed")
                                if (a.blockers != b.blockers) add("Blockers changed")
                                if (a.attachedReferenceIdsCsv != b.attachedReferenceIdsCsv) add("Attached References changed")
                            }
                            Text(
                                text = if (diffs.isEmpty()) "✓ Checkpoints have identical context fields."
                                else "Δ Differences: ${diffs.joinToString(" • ")}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { sp ->
            val projName = projects.find { it.id == sp.projectId }?.name ?: "Project #${sp.projectId}"
            val attachedRefs = sp.parsedAttachedReferenceIds().mapNotNull { rid ->
                references.find { it.id == rid }
            }

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_point_card_${sp.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$projName • ${dateFormat.format(Date(sp.timestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            IconButton(onClick = { onTogglePin(sp) }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.PushPin,
                                    contentDescription = "Pin checkpoint",
                                    tint = if (sp.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { editingSavePoint = sp }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit checkpoint")
                            }
                            IconButton(onClick = { onDuplicate(sp) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate checkpoint")
                            }
                            IconButton(onClick = { onToggleArchive(sp) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Archive, contentDescription = "Archive checkpoint")
                            }
                        }
                    }

                    Text(sp.title, style = MaterialTheme.typography.titleLarge)

                    if (sp.completedSummary.isNotBlank()) {
                        Text("✓ Completed: ${sp.completedSummary}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (sp.happeningNow.isNotBlank()) {
                        Text("📍 Context: ${sp.happeningNow}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (sp.nextAction.isNotBlank()) {
                        Text(
                            "→ Next Action: ${sp.nextAction}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (sp.blockers.isNotBlank()) {
                        Text(
                            "⚠ Blockers: ${sp.blockers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (sp.notes.isNotBlank()) {
                        Text("Notes: ${sp.notes}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (attachedRefs.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            attachedRefs.forEach { ref ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "📎 ${ref.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (sp.importantLinks.isNotBlank() || sp.referencesSummary.isNotBlank()) {
                        Text(
                            "Refs & Links: ${listOf(sp.referencesSummary, sp.importantLinks).filter { it.isNotBlank() }.joinToString(" • ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRestoreContext(sp) },
                            modifier = Modifier.testTag("restore_context_button_${sp.id}")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore Working Context")
                        }
                        TextButton(
                            onClick = {
                                if (compareFirst == null || compareFirst?.id == sp.id) {
                                    compareFirst = sp
                                } else {
                                    compareSecond = sp
                                }
                            }
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compare")
                        }
                        TextButton(
                            onClick = {
                                val exported = BackupVaultManager.exportSingleCheckpointJson(sp, projName)
                                clipboard.setText(AnnotatedString(exported))
                                onShowBanner("Exported checkpoint JSON to clipboard")
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON")
                        }
                    }
                }
            }
        }
    }

    editingSavePoint?.let { sp ->
        var editTitle by remember(sp) { mutableStateOf(sp.title) }
        var editNext by remember(sp) { mutableStateOf(sp.nextAction) }
        var editNotes by remember(sp) { mutableStateOf(sp.notes) }
        var editBlockers by remember(sp) { mutableStateOf(sp.blockers) }
        var editTags by remember(sp) { mutableStateOf(sp.tags) }
        var editAttachedIds by remember(sp) { mutableStateOf(sp.parsedAttachedReferenceIds().toSet()) }

        AlertDialog(
            onDismissRequest = { editingSavePoint = null },
            title = { Text("Edit Save Point & Attached References") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title") })
                    OutlinedTextField(value = editNext, onValueChange = { editNext = it }, label = { Text("Next Action") })
                    OutlinedTextField(value = editNotes, onValueChange = { editNotes = it }, label = { Text("Notes") })
                    OutlinedTextField(value = editBlockers, onValueChange = { editBlockers = it }, label = { Text("Blockers") })
                    OutlinedTextField(value = editTags, onValueChange = { editTags = it }, label = { Text("Tags") })
                    if (references.isNotEmpty()) {
                        Text("Attach Knowledge Library References:", style = MaterialTheme.typography.labelMedium)
                        references.take(6).forEach { ref ->
                            val attached = editAttachedIds.contains(ref.id)
                            FilterChip(
                                selected = attached,
                                onClick = {
                                    editAttachedIds = if (attached) editAttachedIds - ref.id else editAttachedIds + ref.id
                                },
                                label = { Text(ref.title, maxLines = 1) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateSavePoint(
                        sp,
                        editTitle,
                        editNext,
                        editNotes,
                        editBlockers,
                        editTags,
                        editAttachedIds.joinToString(",")
                    )
                    editingSavePoint = null
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSavePoint = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SavePointBottomSheet(
    projects: List<ProjectEntity>,
    missions: List<MissionEntity>,
    references: List<ReferenceItemEntity> = emptyList(),
    selectedProjectId: Long,
    initialQuickMode: Boolean,
    onDismiss: () -> Unit,
    onSaveCheckpoint: (
        projectId: Long,
        missionId: Long?,
        title: String,
        completedSummary: String,
        happeningNow: String,
        nextAction: String,
        notes: String,
        blockers: String,
        referencesSummary: String,
        importantLinks: String,
        tags: String,
        isPinned: Boolean,
        isQuick: Boolean,
        attachedRefIdsCsv: String
    ) -> Unit
) {
    var isQuickMode by remember { mutableStateOf(initialQuickMode) }
    var targetProjectId by remember { mutableStateOf(selectedProjectId) }
    val projectMissions = missions.filter { it.projectId == targetProjectId }
    var selectedMissionId by remember(targetProjectId) { mutableStateOf(projectMissions.firstOrNull()?.id) }
    var attachedReferenceIds by remember { mutableStateOf(setOf<Long>()) }

    var title by remember { mutableStateOf("") }
    var completedSummary by remember { mutableStateOf("") }
    var happeningNow by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var blockers by remember { mutableStateOf("") }
    var referencesSummary by remember { mutableStateOf("") }
    var importantLinks by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("save_point_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Capture Save Point", style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quick Mode", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(checked = isQuickMode, onCheckedChange = { isQuickMode = it })
                }
            }

            // Project Selector Chips
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.take(5).forEach { p ->
                    FilterChip(
                        selected = p.id == targetProjectId,
                        onClick = { targetProjectId = p.id },
                        label = { Text(p.name, maxLines = 1) }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Checkpoint Title (e.g. Paused at line 142 / Section 4.3)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_point_title_input")
            )

            OutlinedTextField(
                value = happeningNow,
                onValueChange = { happeningNow = it },
                label = { Text("What is currently happening? (Exact working context)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_point_context_input")
            )

            OutlinedTextField(
                value = nextAction,
                onValueChange = { nextAction = it },
                label = { Text("Next manual action when you return") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_point_next_action_input")
            )

            // Attach Knowledge Library References directly to this Save Point
            val candidateRefs = references.filter { it.projectId == null || it.projectId == targetProjectId }.take(6)
            if (candidateRefs.isNotEmpty()) {
                Text("Attach Library References:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    candidateRefs.forEach { ref ->
                        val isAttached = attachedReferenceIds.contains(ref.id)
                        FilterChip(
                            selected = isAttached,
                            onClick = {
                                attachedReferenceIds = if (isAttached) attachedReferenceIds - ref.id else attachedReferenceIds + ref.id
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = { Text(ref.title, maxLines = 1) }
                        )
                    }
                }
            }

            if (!isQuickMode) {
                OutlinedTextField(
                    value = completedSummary,
                    onValueChange = { completedSummary = it },
                    label = { Text("What was just completed?") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = blockers,
                    onValueChange = { blockers = it },
                    label = { Text("Open blockers (if any)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & reminders for future self") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = referencesSummary,
                    onValueChange = { referencesSummary = it },
                    label = { Text("Additional open files / references summary") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = importantLinks,
                    onValueChange = { importantLinks = it },
                    label = { Text("Important links / URIs") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                    Text("Pin this Save Point")
                }
                Button(
                    onClick = {
                        onSaveCheckpoint(
                            targetProjectId,
                            selectedMissionId,
                            title,
                            completedSummary,
                            happeningNow,
                            nextAction,
                            notes,
                            blockers,
                            referencesSummary,
                            importantLinks,
                            tags,
                            isPinned,
                            isQuickMode,
                            attachedReferenceIds.joinToString(",")
                        )
                        onDismiss()
                    },
                    modifier = Modifier.testTag("submit_save_point_button")
                ) {
                    Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Checkpoint")
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
