package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ProjectStatus(val label: String) {
    ACTIVE("Active"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    ARCHIVED("Archived")
}

enum class MissionStatus(val label: String) {
    BACKLOG("Backlog"),
    NEXT_UP("Next Up"),
    IN_PROGRESS("In Progress"),
    BLOCKED("Blocked"),
    DONE("Done"),
    ARCHIVED("Archived")
}

enum class MissionPriority(val label: String, val weight: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

enum class RecurrenceRule(val label: String) {
    NONE("One-Time"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

enum class ReferenceType(val label: String) {
    LINK("Link"),
    SNIPPET("Text Snippet"),
    NOTE("Note"),
    IMAGE("Image"),
    DOCUMENT("Document"),
    FILE_REF("File Reference"),
    CHECKLIST("Checklist"),
    RESOURCE("Project Resource"),
    INSTRUCTION("Useful Instruction")
}

enum class FocusModeType(val label: String, val defaultMinutes: Int) {
    POMODORO("Pomodoro", 25),
    DEEP_WORK("Deep Work", 50),
    STOPWATCH("Stopwatch", 0),
    CUSTOM("Custom Session", 35),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

enum class AmbientSoundType(val label: String, val description: String) {
    NONE("Silent", "Pure silence"),
    RAIN_CANOPY("Rain Canopy", "Soft procedural rainfall"),
    WARM_TAPE("Warm Tape", "Filtered warm brown noise"),
    BINAURAL_DRONE("Deep Chord", "Calm harmonic focus drone"),
    FOREST_WIND("Forest Wind", "Gentle modulated breeze")
}

enum class DashboardWidgetType(val index: Int, val defaultTitle: String) {
    ACTIVE_PROJECTS(1, "Active Projects"),
    CONTINUE_WHERE_LEFT_OFF(2, "Continue Where You Left Off"),
    LAST_SAVE_POINT(3, "Last Save Point"),
    CURRENT_MISSION(4, "Current Mission"),
    TODAYS_MISSIONS(5, "Today's Missions"),
    FOCUS_TIMER(6, "Focus Timer"),
    PROJECT_PROGRESS(7, "Project Progress"),
    WEEKLY_OVERVIEW(8, "Weekly Overview"),
    RECENT_ACTIVITY(9, "Recent Activity"),
    RECENT_REFERENCES(10, "Recent References"),
    QUICK_SAVE_POINT(11, "Quick Save Point"),
    QUICK_CAPTURE(12, "Quick Capture"),
    UPCOMING_MILESTONES(13, "Upcoming Milestones"),
    PROJECT_TIMELINE(14, "Project Timeline"),
    FOCUS_STATISTICS(15, "Focus Statistics"),
    SAVED_IDEAS(16, "Saved Ideas"),
    COMPLETED_MISSIONS(17, "Completed Missions"),
    RECENTLY_OPENED_PROJECTS(18, "Recently Opened Projects"),
    PROJECT_SHORTCUTS(19, "Project Shortcuts"),
    PERSONAL_PROGRESS_SUMMARY(20, "Personal Progress Summary")
}

enum class WidgetSize(val label: String) {
    COMPACT("Compact"),
    MEDIUM("Medium"),
    EXPANDED("Expanded")
}

data class DashboardWidgetConfig(
    val type: DashboardWidgetType,
    val isVisible: Boolean = true,
    val size: WidgetSize = WidgetSize.MEDIUM,
    val accentHex: String = "#CCFF00",
    val orderIndex: Int = type.index
)

data class DashboardLayoutPreset(
    val id: String,
    val name: String,
    val widgets: List<DashboardWidgetConfig>
)

data class MilestoneItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val targetDateLabel: String = ""
)

data class SubtaskItem(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

@Entity(
    tableName = "projects",
    indices = [Index("status"), Index("isPinned"), Index("lastOpenedAt")]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val tagline: String = "",
    val description: String = "",
    val colorHex: String = "#CCFF00",
    val iconName: String = "bookmark",
    val status: String = ProjectStatus.ACTIVE.name,
    val isPinned: Boolean = false,
    val currentContextSummary: String = "",
    val lastCompletedAction: String = "",
    val nextManualStep: String = "",
    val openBlockers: String = "",
    val milestonesJson: String = "", // Pipe-delimited id::title::done::date
    val templateSource: String = "Blank",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val totalFocusSeconds: Long = 0L,
    @ColumnInfo(defaultValue = "0") val isSampleData: Boolean = false
) {
    fun parsedMilestones(): List<MilestoneItem> {
        if (milestonesJson.isBlank()) return emptyList()
        return milestonesJson.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 2) {
                MilestoneItem(
                    id = parts[0],
                    title = parts[1],
                    isCompleted = parts.getOrNull(2)?.toBoolean() ?: false,
                    targetDateLabel = parts.getOrNull(3) ?: ""
                )
            } else null
        }
    }

    companion object {
        fun encodeMilestones(items: List<MilestoneItem>): String {
            return items.joinToString("||") { "${it.id}::${it.title.replace("::", " ")}::${it.isCompleted}::${it.targetDateLabel.replace("::", " ")}" }
        }
    }
}

@Entity(
    tableName = "save_points",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("timestamp"), Index("isPinned"), Index("isArchived")]
)
data class SavePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val missionId: Long? = null,
    val title: String,
    val completedSummary: String = "",
    val happeningNow: String = "",
    val nextAction: String = "",
    val notes: String = "",
    val blockers: String = "",
    val referencesSummary: String = "",
    val importantLinks: String = "",
    val attachmentsUriList: String = "",
    val tags: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isQuickCheckpoint: Boolean = false,
    val sessionDurationSeconds: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "") val attachedReferenceIdsCsv: String = ""
) {
    fun parsedAttachedReferenceIds(): List<Long> =
        attachedReferenceIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
}

@Entity(
    tableName = "missions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("status"), Index("priority"), Index("manualOrder")]
)
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val description: String = "",
    val status: String = MissionStatus.NEXT_UP.name,
    val priority: String = MissionPriority.MEDIUM.name,
    val customStatusLabel: String = "",
    val dueDateMillis: Long? = null,
    val tags: String = "",
    val subtasksRaw: String = "", // Pipe-delimited id::title::isDone
    val checklistRaw: String = "", // Pipe-delimited id::title::isDone
    val dependsOnIdsCsv: String = "", // Comma-separated mission IDs
    val manualOrder: Int = 0,
    val recurrenceRule: String = RecurrenceRule.NONE.name,
    val linkedSavePointId: Long? = null,
    val isArchived: Boolean = false,
    val isTemplate: Boolean = false,
    val historyLog: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    fun parsedSubtasks(): List<SubtaskItem> = parseItems(subtasksRaw)
    fun parsedChecklist(): List<SubtaskItem> = parseItems(checklistRaw)
    fun parsedDependencies(): List<Long> =
        dependsOnIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }

    companion object {
        fun parseItems(raw: String): List<SubtaskItem> {
            if (raw.isBlank()) return emptyList()
            return raw.split("||").mapNotNull { token ->
                val parts = token.split("::")
                if (parts.size >= 2) {
                    SubtaskItem(
                        id = parts[0],
                        title = parts[1],
                        isDone = parts.getOrNull(2)?.toBoolean() ?: false
                    )
                } else null
            }
        }

        fun encodeItems(items: List<SubtaskItem>): String =
            items.joinToString("||") { "${it.id}::${it.title.replace("::", " ")}::${it.isDone}" }
    }
}

@Entity(
    tableName = "reference_items",
    indices = [Index("projectId"), Index("savePointId"), Index("isFavorite"), Index("type")]
)
data class ReferenceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val savePointId: Long? = null,
    val title: String,
    val type: String = ReferenceType.LINK.name,
    val contentOrUri: String = "",
    val description: String = "",
    val folder: String = "General",
    val category: String = "Core Docs",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val hasPersistableUriAccess: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "focus_sessions",
    indices = [Index("projectId"), Index("missionId"), Index("startedAt")]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val missionId: Long? = null,
    val mode: String = FocusModeType.POMODORO.name,
    val plannedDurationSeconds: Long = 1500L,
    val actualElapsedSeconds: Long = 0L,
    val notes: String = "",
    val ambientSound: String = AmbientSoundType.NONE.name,
    val createdSavePointId: Long? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "rescue_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("timestamp")]
)
data class RescueSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val whereStopped: String = "",
    val whatIsBlocking: String = "",
    val smallestNextAction: String = "",
    val whatToOpen: String = "",
    val plannedWorkMinutes: Int = 15,
    val createdSavePointId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectTemplateModel(
    val id: String,
    val name: String,
    val category: String,
    val tagline: String,
    val description: String,
    val colorHex: String,
    val iconName: String,
    val defaultMilestones: List<String>,
    val defaultMissions: List<String>,
    val suggestedManualSections: List<String>,
    val savePointPrompts: List<String>,
    val referenceCategories: List<String>,
    val defaultFocusMode: FocusModeType,
    val defaultFocusMinutes: Int,
    val isBuiltIn: Boolean = true
)
