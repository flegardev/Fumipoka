package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.ProjectEntity
import com.example.data.ProjectStatus
import com.example.data.SavePointEntity

@Composable
fun NotebookHome(
    projects: List<ProjectEntity>,
    savePoints: List<SavePointEntity>,
    onNewProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onSaveProgress: () -> Unit
) {
    val active = projects.filter { it.status == ProjectStatus.ACTIVE.name }
        .sortedByDescending { it.lastOpenedAt }
    val activeIds = active.mapTo(mutableSetOf()) { it.id }
    val latest = savePoints.filter { !it.isArchived && it.projectId in activeIds }.maxByOrNull { it.timestamp }
    val latestProject = latest?.let { point -> active.firstOrNull { it.id == point.projectId } }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Pick up where you left off", style = MaterialTheme.typography.displaySmall)
            }
            if (active.isEmpty()) {
                item {
                    Text(
                        if (projects.isEmpty()) "Create a project to save your first note."
                        else "No active projects.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onNewProject, modifier = Modifier.heightIn(min = 48.dp)) { Text("New project") }
                }
            } else {
                item {
                    if (latestProject != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium,
                            onClick = { onOpenProject(latestProject.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(latestProject.name, style = MaterialTheme.typography.labelLarge)
                                Text(latest.happeningNow.ifBlank { latest.notes.ifBlank { latest.title } },
                                    style = MaterialTheme.typography.headlineSmall, maxLines = 4,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text("Continue project", style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        Text("No save points yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onSaveProgress, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text("Save progress")
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Active projects", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = onNewProject) { Text("New project") }
                    }
                }
                items(active, key = { it.id }) { project ->
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .clickable { onOpenProject(project.id) },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(project.name, style = MaterialTheme.typography.titleMedium)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
