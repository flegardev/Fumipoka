package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.os.BundleCompat
import com.example.MainActivity
import com.example.R

data class WidgetSnapshotData(
    val activeProjectName: String = "",
    val activeContext: String = "",
    val lastSavePointTitle: String = "",
    val nextManualStep: String = "",
    val appearance: String = "SYSTEM",
    val activeProjectsCount: Int = 0
)

object ReturnmarkWidgetUpdater {
    private const val PREFS_NAME = "returnmark_widget_snapshot"

    fun pushSnapshotAndRefreshAll(context: Context, snapshot: WidgetSnapshotData) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString("activeProjectName", snapshot.activeProjectName)
            putString("activeContext", snapshot.activeContext)
            putString("lastSavePointTitle", snapshot.lastSavePointTitle)
            putString("nextManualStep", snapshot.nextManualStep)
            putString("appearance", snapshot.appearance)
            putInt("activeProjectsCount", snapshot.activeProjectsCount)
        }.apply()
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
    }

    fun readSnapshot(context: Context): WidgetSnapshotData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WidgetSnapshotData(
            activeProjectName = prefs.getString("activeProjectName", "").orEmpty(),
            activeContext = prefs.getString("activeContext", "").orEmpty(),
            lastSavePointTitle = prefs.getString("lastSavePointTitle", "").orEmpty(),
            nextManualStep = prefs.getString("nextManualStep", "").orEmpty(),
            appearance = prefs.getString("appearance", "SYSTEM").orEmpty(),
            activeProjectsCount = prefs.getInt("activeProjectsCount", 0)
        )
    }
}

abstract class BaseReturnmarkWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val snapshot = ReturnmarkWidgetUpdater.readSnapshot(context)
        for (id in ids) {
            val options = manager.getAppWidgetOptions(id)
            fun render(size: SizeF) = content(context, size, snapshot)
            val sizes = if (Build.VERSION.SDK_INT >= 31) {
                BundleCompat.getParcelableArrayList(options, AppWidgetManager.OPTION_APPWIDGET_SIZES, SizeF::class.java)
            } else null
            val views = if (Build.VERSION.SDK_INT >= 31 && !sizes.isNullOrEmpty()) {
                RemoteViews(sizes.distinct().take(16).associateWith(::render))
            } else render(SizeF(
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 122).toFloat(),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 58).toFloat()
            ))
            manager.updateAppWidget(id, views)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        onUpdate(context, manager, intArrayOf(id))
    }

    private fun content(context: Context, size: SizeF, snapshot: WidgetSnapshotData): RemoteViews {
        val config = context.resources.configuration
        val isOled = snapshot.appearance == "OLED"
        val isDark = isOled || snapshot.appearance == "DARK" ||
            (snapshot.appearance == "SYSTEM" && config.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
        val ink = if (isDark) 0xFFF9EEE3.toInt() else 0xFF302721.toInt()
        val actionInk = if (isDark) 0xFFFAB99D.toInt() else 0xFF923F2B.toInt()
        val compact = size.width < 110f
        val hasProject = snapshot.activeProjectsCount > 0
        return RemoteViews(context.packageName, R.layout.returnmark_app_widget).apply {
            setInt(android.R.id.background, "setBackgroundResource", when {
                isOled -> R.drawable.adaptive_widget_background_oled
                isDark -> R.drawable.adaptive_widget_background_dark
                else -> R.drawable.adaptive_widget_background
            })
            val padding = ((if (compact || size.height < 96f) 4 else 10) * context.resources.displayMetrics.density).toInt()
            setViewPadding(android.R.id.background, padding, padding, padding, padding)
            for (id in listOf(R.id.widget_headline, R.id.widget_detail, R.id.widget_more)) setTextColor(id, ink)
            setTextColor(R.id.widget_add, actionInk)
            setTextViewText(R.id.widget_headline, if (hasProject) snapshot.activeProjectName.ifBlank { "Projects" } else "Fumipoka")
            setTextViewTextSize(R.id.widget_headline, TypedValue.COMPLEX_UNIT_SP, if (compact) 13f else 16f)
            setTextViewText(R.id.widget_add, if (size.width >= 172f) "+ Save" else "+")
            setTextViewText(R.id.widget_detail, if (hasProject) snapshot.lastSavePointTitle.ifBlank { "No save points yet" } else "Create a project")
            setTextViewText(R.id.widget_more, snapshot.activeContext)
            setViewVisibility(R.id.widget_add, if (compact || !hasProject) View.GONE else View.VISIBLE)
            setViewVisibility(R.id.widget_detail_row, if (!compact && size.height >= 95f) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.widget_more, if (!compact && size.height >= 145f && snapshot.activeContext.isNotBlank()) View.VISIBLE else View.GONE)
            setContentDescription(R.id.widget_add, "Save progress")
            setOnClickPendingIntent(android.R.id.background, launch(context, if (hasProject) "OPEN_CONTINUE" else "OPEN_HOME"))
            setOnClickPendingIntent(R.id.widget_add, launch(context, "OPEN_QUICK_SAVE"))
        }
    }

    private fun launch(context: Context, shortcut: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = Uri.Builder().scheme("fumipoka").authority("widget").appendPath(shortcut).build()
            putExtra("widget_shortcut_action", shortcut)
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

class ContinueProjectWidgetReceiver : BaseReturnmarkWidgetProvider()
class CurrentMissionWidgetReceiver : BaseReturnmarkWidgetProvider()
class QuickSavePointWidgetReceiver : BaseReturnmarkWidgetProvider()
class ActiveProjectsWidgetReceiver : BaseReturnmarkWidgetProvider()
class FocusTimerWidgetReceiver : BaseReturnmarkWidgetProvider()
class ProjectProgressWidgetReceiver : BaseReturnmarkWidgetProvider()
class NextMissionWidgetReceiver : BaseReturnmarkWidgetProvider()
class PinnedProjectWidgetReceiver : BaseReturnmarkWidgetProvider()
class RecentCheckpointsWidgetReceiver : BaseReturnmarkWidgetProvider()
class QuickCaptureWidgetReceiver : BaseReturnmarkWidgetProvider()
