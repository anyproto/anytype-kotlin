package com.anytypeio.anytype.feature_os_widgets.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import com.anytypeio.anytype.feature_os_widgets.ui.OsCreateObjectWidget
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * BroadcastReceiver for the Create Object widget.
 * This is the entry point registered in the AndroidManifest.
 */
class OsCreateObjectWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = OsCreateObjectWidget()

    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.d("OsCreateObjectWidgetReceiver: onUpdate called for widgets: ${appWidgetIds.toList()}")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("OsCreateObjectWidgetReceiver: onReceive action=${intent.action}")
        super.onReceive(context, intent)
    }

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
