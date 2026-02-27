package com.anytypeio.anytype.feature_os_widgets.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.anytypeio.anytype.feature_os_widgets.receiver.OsSpaceShortcutWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Utility object to update the Space Shortcut widget.
 * Used by the configuration activity to refresh the widget after configuration is saved.
 */
object OsSpaceShortcutWidgetUpdater {

    private const val TAG = "SpaceShortcutWidgetUpdater"
    private const val UPDATE_DELAY_MS = 300L

    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Updates a specific widget by its appWidgetId.
     */
    fun update(context: Context, appWidgetId: Int) {
        Timber.tag(TAG).d("Updating widget $appWidgetId")

        // Send broadcast to trigger receiver's onUpdate -> provideGlance
        sendUpdateBroadcast(context, appWidgetId)

        // Schedule delayed Glance update as fallback
        scheduleDelayedUpdate(context, appWidgetId)
    }

    private fun sendUpdateBroadcast(context: Context, appWidgetId: Int) {
        try {
            val intent = Intent(context, OsSpaceShortcutWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Broadcast failed for widget $appWidgetId")
        }
    }

    private fun scheduleDelayedUpdate(context: Context, appWidgetId: Int) {
        handler.postDelayed({
            scope.launch {
                try {
                    val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                    OsSpaceShortcutWidget().update(context, glanceId)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Delayed update failed for widget $appWidgetId")
                }
            }
        }, UPDATE_DELAY_MS)
    }
}
