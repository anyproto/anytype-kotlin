package com.anytypeio.anytype.feature_os_widgets.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.anytypeio.anytype.feature_os_widgets.receiver.OsObjectShortcutWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Utility object to update the Object Shortcut widget.
 * Used by the configuration activity to refresh the widget after configuration is saved.
 */
object OsObjectShortcutWidgetUpdater {

    private const val TAG = "OsObjectShortcutWidget"
    private const val UPDATE_DELAY_MS = 500L

    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Updates a specific widget by its appWidgetId.
     */
    fun update(context: Context, appWidgetId: Int) {
        Timber.tag(TAG).d("Updater: triggering update for widget $appWidgetId")

        // Send broadcast to trigger receiver's onUpdate -> provideGlance
        sendUpdateBroadcast(context, appWidgetId)

        // Schedule delayed Glance update as fallback
        scheduleDelayedUpdate(context, appWidgetId)
    }

    private fun sendUpdateBroadcast(context: Context, appWidgetId: Int) {
        try {
            Timber.tag(TAG).d("Updater: sending broadcast for widget $appWidgetId")
            val intent = Intent(context, OsObjectShortcutWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            context.sendBroadcast(intent)
            Timber.tag(TAG).d("Updater: broadcast sent for widget $appWidgetId")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Updater: broadcast failed for widget $appWidgetId")
        }
    }

    private fun scheduleDelayedUpdate(context: Context, appWidgetId: Int) {
        Timber.tag(TAG).d("Updater: scheduling delayed update for widget $appWidgetId in ${UPDATE_DELAY_MS}ms")
        handler.postDelayed({
            scope.launch {
                try {
                    Timber.tag(TAG).d("Updater: executing delayed update for widget $appWidgetId")
                    val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                    OsObjectShortcutWidget().update(context, glanceId)
                    Timber.tag(TAG).d("Updater: delayed update completed for widget $appWidgetId")
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Updater: delayed update failed for widget $appWidgetId")
                }
            }
        }, UPDATE_DELAY_MS)
    }
}
