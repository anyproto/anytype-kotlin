package com.anytypeio.anytype.feature_os_widgets.receiver

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.anytypeio.anytype.feature_os_widgets.ui.OsCreateObjectWidget
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for the Create Object widget.
 * Registered in AndroidManifest as the entry point for widget lifecycle events.
 */
class OsCreateObjectWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = OsCreateObjectWidget()

    private val coroutineScope = MainScope()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Clean up widget configurations when widgets are removed
        coroutineScope.launch {
            val dataStore = OsWidgetsDataStore(context)
            appWidgetIds.forEach { appWidgetId ->
                dataStore.deleteCreateObjectConfig(appWidgetId)
            }
        }
    }
}
