package com.example.domain

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

/**
 * 100% Local, Offline Notification & Context Reminder Manager for Returnmark 3.0.
 * Zero network calls, zero external push services.
 */
object ReturnmarkNotificationHelper {

    private const val CHANNEL_FOCUS_ALERTS = "returnmark_focus_alerts_v3"
    private const val CHANNEL_PINNED_CONTEXT = "returnmark_pinned_context_v3"

    const val NOTIF_ID_FOCUS_COMPLETE = 3001
    const val NOTIF_ID_PINNED_CONTEXT = 3002

    fun ensureChannelsCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS_ALERTS,
                "Focus Session & Save Point Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Local reminders to capture a Save Point when a Focus Session finishes."
            }

            val pinnedChannel = NotificationChannel(
                CHANNEL_PINNED_CONTEXT,
                "Pinned Active Project Save Point",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays your current project's working context and next manual step in the notification shade."
            }

            manager.createNotificationChannel(focusChannel)
            manager.createNotificationChannel(pinnedChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun notifyFocusSessionCompleted(
        context: Context,
        projectName: String,
        elapsedMinutes: Long,
        nextManualStep: String
    ) {
        if (!hasNotificationPermission(context)) return
        ensureChannelsCreated(context)

        val openSaveIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("widget_shortcut_action", "OPEN_QUICK_SAVE")
        }
        val pendingSave = PendingIntent.getActivity(
            context,
            301,
            openSaveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FOCUS_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Focus Complete (${elapsedMinutes}m) • $projectName")
            .setContentText("Want to save where you stopped? Next: $nextManualStep")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Great focus sprint on $projectName! Tap to capture a Save Point while your working context is fresh.\nNext step: $nextManualStep"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingSave)
            .addAction(0, "Capture Save Point", pendingSave)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_FOCUS_COMPLETE, notification)
        } catch (_: SecurityException) {
        }
    }

    fun updatePinnedContextNotification(
        context: Context,
        enabled: Boolean,
        projectName: String,
        whereStopped: String,
        nextManualStep: String
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!enabled) {
            manager.cancel(NOTIF_ID_PINNED_CONTEXT)
            return
        }
        if (!hasNotificationPermission(context)) return
        ensureChannelsCreated(context)

        val resumeIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("widget_shortcut_action", "OPEN_CONTINUE")
        }
        val pendingResume = PendingIntent.getActivity(
            context,
            302,
            resumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickSaveIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("widget_shortcut_action", "OPEN_QUICK_SAVE")
        }
        val pendingQuickSave = PendingIntent.getActivity(
            context,
            303,
            quickSaveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PINNED_CONTEXT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔖 $projectName")
            .setContentText("Next → $nextManualStep")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Stopped at: $whereStopped\nNext Manual Action → $nextManualStep"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingResume)
            .addAction(0, "Resume", pendingResume)
            .addAction(0, "Quick Save", pendingQuickSave)
            .build()

        try {
            manager.notify(NOTIF_ID_PINNED_CONTEXT, notification)
        } catch (_: SecurityException) {
        }
    }
}
