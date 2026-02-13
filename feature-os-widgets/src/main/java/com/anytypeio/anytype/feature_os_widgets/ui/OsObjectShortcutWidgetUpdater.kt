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

    private const val TAG = "OsWidget"
    private const val UPDATE_DELAY_MS = 500L

    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Updates a specific widget by its appWidgetId.
     */
    fun update(context: Context, appWidgetId: Int) {
        // Send broadcast to trigger receiver's onUpdate -> provideGlance
        sendUpdateBroadcast(context, appWidgetId)

        // Schedule delayed Glance update as fallback
        scheduleDelayedUpdate(context, appWidgetId)
    }

    private fun sendUpdateBroadcast(context: Context, appWidgetId: Int) {
        try {
            val intent = Intent(context, OsObjectShortcutWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to send update broadcast for widget $appWidgetId")
        }
    }

    private fun scheduleDelayedUpdate(context: Context, appWidgetId: Int) {
        handler.postDelayed({
            scope.launch {
                try {
                    val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                    OsObjectShortcutWidget().update(context, glanceId)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed delayed update for widget $appWidgetId")
                }
            }
        }, UPDATE_DELAY_MS)
    }
}
