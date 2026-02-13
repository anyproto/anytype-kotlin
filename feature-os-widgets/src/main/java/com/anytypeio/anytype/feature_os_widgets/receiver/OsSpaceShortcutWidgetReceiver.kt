package com.anytypeio.anytype.feature_os_widgets.receiver

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.anytypeio.anytype.feature_os_widgets.ui.OsSpaceShortcutWidget
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for the Space Shortcut widget.
 * Registered in AndroidManifest as the entry point for widget lifecycle events.
 */
class OsSpaceShortcutWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = OsSpaceShortcutWidget()

    private val coroutineScope = MainScope()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Clean up widget configurations when widgets are removed
        coroutineScope.launch {
            val dataStore = OsWidgetsDataStore(context)
            appWidgetIds.forEach { appWidgetId ->
                dataStore.deleteSpaceShortcutConfig(appWidgetId)
            }
        }
    }
}
