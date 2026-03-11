package com.anytypeio.anytype.feature_os_widgets.receiver

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.ui.OsDataViewWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for the Data View widget.
 * Registered in AndroidManifest as the entry point for widget lifecycle events.
 */
class OsDataViewWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = OsDataViewWidget()

    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        timber.log.Timber.tag("OsDataViewReceiver").d("onUpdate called, widgetIds=${appWidgetIds.toList()}")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        coroutineScope.launch {
            val dataStore = OsWidgetsDataStore(context)
            appWidgetIds.forEach { appWidgetId ->
                dataStore.deleteDataViewConfig(appWidgetId)
            }
        }
    }
}
