package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import androidx.compose.ui.graphics.toArgb
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

data class WidgetSnapshotData(
    val activeProjectName: String = "Fumipoka Workspace",
    val activeContext: String = "Create a project or load example workspace",
    val lastSavePointTitle: String = "No Save Point yet",
    val nextManualStep: String = "Capture your first Save Point",
    val currentMissionTitle: String = "All missions clear",
    val nextMissionTitle: String = "Create your next mission",
    val activeProjectsCount: Int = 0,
    val completedMissionsCount: Int = 0,
    val totalMissionsCount: Int = 0,
    val backgroundArgb: Int = 0xFFFFF9F4.toInt(),
    val inkArgb: Int = 0xFF302737.toInt(),
    val accentArgb: Int = 0xFF72577E.toInt(),
    val totalFocusMinutes: Int = 0
)

object ReturnmarkWidgetUpdater {
    private const val PREFS_NAME = "returnmark_widget_snapshot"

    fun pushSnapshotAndRefreshAll(context: Context, snapshot: WidgetSnapshotData) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putInt("kissaten_background", snapshot.backgroundArgb)
                putInt("kissaten_ink", snapshot.inkArgb)
                putInt("kissaten_accent", snapshot.accentArgb)
                putString("activeProjectName", snapshot.activeProjectName)
                putString("activeContext", snapshot.activeContext)
                putString("lastSavePointTitle", snapshot.lastSavePointTitle)
                putString("nextManualStep", snapshot.nextManualStep)
                putString("currentMissionTitle", snapshot.currentMissionTitle)
                putString("nextMissionTitle", snapshot.nextMissionTitle)
                putInt("activeProjectsCount", snapshot.activeProjectsCount)
                putInt("completedMissionsCount", snapshot.completedMissionsCount)
                putInt("totalMissionsCount", snapshot.totalMissionsCount)
                putInt("totalFocusMinutes", snapshot.totalFocusMinutes)
                apply()
            }
            val manager = AppWidgetManager.getInstance(context)
            val providers = listOf(
                ContinueProjectWidgetReceiver::class.java,
                CurrentMissionWidgetReceiver::class.java,
                QuickSavePointWidgetReceiver::class.java,
                ActiveProjectsWidgetReceiver::class.java,
                FocusTimerWidgetReceiver::class.java,
                ProjectProgressWidgetReceiver::class.java,
                NextMissionWidgetReceiver::class.java,
                PinnedProjectWidgetReceiver::class.java,
                RecentCheckpointsWidgetReceiver::class.java,
                QuickCaptureWidgetReceiver::class.java
            )
            providers.forEach { cls ->
                val ids = manager.getAppWidgetIds(ComponentName(context, cls))
                if (ids.isNotEmpty()) {
                    val intent = Intent(context, cls).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            }
        } catch (_: Throwable) {
            // Safe in unit-test environments
        }
    }

    fun readSnapshot(context: Context): WidgetSnapshotData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WidgetSnapshotData(
            backgroundArgb = prefs.getInt("kissaten_background", 0xFFFFF9F4.toInt()),
            inkArgb = prefs.getInt("kissaten_ink", 0xFF302737.toInt()),
            accentArgb = prefs.getInt("kissaten_accent", 0xFF72577E.toInt()),
            activeProjectName = prefs.getString("activeProjectName", "Fumipoka Workspace") ?: "Fumipoka Workspace",
            activeContext = prefs.getString("activeContext", "Never lose your place.") ?: "",
            lastSavePointTitle = prefs.getString("lastSavePointTitle", "No Save Point yet") ?: "",
            nextManualStep = prefs.getString("nextManualStep", "Capture your first Save Point") ?: "",
            currentMissionTitle = prefs.getString("currentMissionTitle", "All missions clear") ?: "",
            nextMissionTitle = prefs.getString("nextMissionTitle", "Create your next mission") ?: "",
            activeProjectsCount = prefs.getInt("activeProjectsCount", 0),
            completedMissionsCount = prefs.getInt("completedMissionsCount", 0),
            totalMissionsCount = prefs.getInt("totalMissionsCount", 0),
            totalFocusMinutes = prefs.getInt("totalFocusMinutes", 0)
        )
    }
}

abstract class BaseReturnmarkWidgetProvider(
    private val badgeText: String,
    private val pillText: String,
    private val shortcutAction: String,
    private val secondaryPillText: String = "+ SAVE",
    private val secondaryShortcutAction: String = "OPEN_QUICK_SAVE"
) : AppWidgetProvider() {

    abstract fun bindContent(snapshot: WidgetSnapshotData): Triple<String, String, String>

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val snapshot = ReturnmarkWidgetUpdater.readSnapshot(context)
        val (title, subtitle, footer) = bindContent(snapshot)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.returnmark_app_widget)
            val compact = appWidgetManager.getAppWidgetOptions(appWidgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 240) < 160
            for (id in listOf(R.id.widget_badge, R.id.widget_subtitle, R.id.widget_footer)) {
                views.setViewVisibility(id, if (compact) android.view.View.GONE else android.view.View.VISIBLE)
            }
            val padding = ((if (compact) 8 else 14) * context.resources.displayMetrics.density).toInt()
            views.setViewPadding(R.id.widget_root, padding, padding, padding, padding)
            views.setInt(R.id.widget_root, "setBackgroundColor", snapshot.backgroundArgb)
            for (id in listOf(R.id.widget_title, R.id.widget_subtitle, R.id.widget_footer)) views.setTextColor(id, snapshot.inkArgb)
            for (id in listOf(R.id.widget_badge, R.id.widget_secondary_action)) views.setTextColor(id, snapshot.accentArgb)
            views.setInt(R.id.widget_secondary_action, "setBackgroundColor", snapshot.backgroundArgb)
            views.setInt(R.id.widget_action_pill, "setBackgroundColor", snapshot.accentArgb)
            views.setTextColor(R.id.widget_action_pill, com.example.ui.theme.kissatenInk(androidx.compose.ui.graphics.Color(snapshot.accentArgb)).toArgb())

            views.setTextViewText(R.id.widget_badge, badgeText)
            views.setTextViewText(R.id.widget_action_pill, pillText)
            views.setTextViewText(R.id.widget_secondary_action, secondaryPillText)
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_subtitle, subtitle)
            views.setTextViewText(R.id.widget_footer, footer)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("widget_shortcut_action", shortcutAction)
            }
            val primaryPendingIntent = PendingIntent.getActivity(
                context,
                shortcutAction.hashCode() + appWidgetId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, primaryPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_action_pill, primaryPendingIntent)

            val secondaryIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("widget_shortcut_action", secondaryShortcutAction)
            }
            val secondaryPendingIntent = PendingIntent.getActivity(
                context,
                (secondaryShortcutAction.hashCode() * 31) + appWidgetId,
                secondaryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_secondary_action, secondaryPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: android.os.Bundle) {
        onUpdate(context, manager, intArrayOf(id))
    }
}

// 1. Continue Project Widget
class ContinueProjectWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "CONTINUE PROJECT",
    pillText = "RESUME",
    shortcutAction = "OPEN_CONTINUE",
    secondaryPillText = "+ SAVE",
    secondaryShortcutAction = "OPEN_QUICK_SAVE"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        snapshot.activeProjectName,
        "Stopped: ${snapshot.activeContext}",
        "Next → ${snapshot.nextManualStep}"
    )
}

// 2. Current Mission Widget
class CurrentMissionWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "CURRENT MISSION",
    pillText = "MISSIONS",
    shortcutAction = "OPEN_MISSIONS",
    secondaryPillText = "FOCUS",
    secondaryShortcutAction = "OPEN_FOCUS"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        snapshot.currentMissionTitle,
        "Project: ${snapshot.activeProjectName}",
        "${snapshot.completedMissionsCount}/${snapshot.totalMissionsCount} missions completed"
    )
}

// 3. Quick Save Point Widget
class QuickSavePointWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "QUICK SAVE POINT",
    pillText = "+ SAVE",
    shortcutAction = "OPEN_QUICK_SAVE",
    secondaryPillText = "TIMELINE",
    secondaryShortcutAction = "OPEN_SAVE_POINTS"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        "Save My Progress",
        "Capture exact working context for ${snapshot.activeProjectName}",
        "Latest: ${snapshot.lastSavePointTitle}"
    )
}

// 4. Active Projects Widget
class ActiveProjectsWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "ACTIVE PROJECTS",
    pillText = "VIEW",
    shortcutAction = "OPEN_PROJECTS",
    secondaryPillText = "RESCUE",
    secondaryShortcutAction = "OPEN_RESCUE"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        "${snapshot.activeProjectsCount} Active Workspaces",
        "Primary: ${snapshot.activeProjectName}",
        "Next: ${snapshot.nextManualStep}"
    )
}

// 5. Focus Timer Widget
class FocusTimerWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "FOCUS STUDIO",
    pillText = "START",
    shortcutAction = "OPEN_FOCUS",
    secondaryPillText = "+ SAVE",
    secondaryShortcutAction = "OPEN_QUICK_SAVE"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        "Pomodoro & Deep Work",
        "Linked to: ${snapshot.currentMissionTitle}",
        "${snapshot.totalFocusMinutes} total focused minutes logged"
    )
}

// 6. Project Progress Widget
class ProjectProgressWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "PROJECT PROGRESS",
    pillText = "STATS",
    shortcutAction = "OPEN_STATS",
    secondaryPillText = "RESUME",
    secondaryShortcutAction = "OPEN_CONTINUE"
) {
    override fun bindContent(snapshot: WidgetSnapshotData): Triple<String, String, String> {
        val pct = if (snapshot.totalMissionsCount > 0) {
            (snapshot.completedMissionsCount * 100) / snapshot.totalMissionsCount
        } else 0
        return Triple(
            "$pct% Mission Completion",
            snapshot.activeProjectName,
            "${snapshot.completedMissionsCount} of ${snapshot.totalMissionsCount} missions done"
        )
    }
}

// 7. Next Mission Widget
class NextMissionWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "NEXT ACTIONABLE",
    pillText = "START",
    shortcutAction = "OPEN_MISSIONS",
    secondaryPillText = "FOCUS",
    secondaryShortcutAction = "OPEN_FOCUS"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        snapshot.nextMissionTitle,
        "Up next in ${snapshot.activeProjectName}",
        "Ready to launch with zero blockers"
    )
}

// 8. Pinned Project Widget
class PinnedProjectWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "PINNED WORKSPACE",
    pillText = "OPEN",
    shortcutAction = "OPEN_PROJECTS",
    secondaryPillText = "+ SAVE",
    secondaryShortcutAction = "OPEN_QUICK_SAVE"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        snapshot.activeProjectName,
        snapshot.activeContext,
        "Next → ${snapshot.nextManualStep}"
    )
}

// 9. Recent Checkpoints Widget
class RecentCheckpointsWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "RECENT CHECKPOINT",
    pillText = "TIMELINE",
    shortcutAction = "OPEN_SAVE_POINTS",
    secondaryPillText = "+ SAVE",
    secondaryShortcutAction = "OPEN_QUICK_SAVE"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        snapshot.lastSavePointTitle,
        snapshot.activeContext,
        "Next → ${snapshot.nextManualStep}"
    )
}

// 10. Quick Capture Widget
class QuickCaptureWidgetReceiver : BaseReturnmarkWidgetProvider(
    badgeText = "QUICK CAPTURE",
    pillText = "+ CAPTURE",
    shortcutAction = "OPEN_QUICK_CAPTURE",
    secondaryPillText = "+ SAVE",
    secondaryShortcutAction = "OPEN_QUICK_SAVE"
) {
    override fun bindContent(snapshot: WidgetSnapshotData) = Triple(
        "Capture Idea, Mission or Link",
        "Instantly save to ${snapshot.activeProjectName} or Knowledge Library",
        "100% Offline • Zero Friction"
    )
}
