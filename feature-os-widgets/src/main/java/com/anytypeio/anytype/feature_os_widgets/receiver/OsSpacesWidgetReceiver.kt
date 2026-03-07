package com.anytypeio.anytype.feature_os_widgets.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.anytypeio.anytype.feature_os_widgets.ui.OsSpacesListWidget
import timber.log.Timber

/**
 * BroadcastReceiver for the Spaces List widget.
 * This is the entry point registered in the AndroidManifest.
 */
class OsSpacesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = OsSpacesListWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.tag(TAG).d("onUpdate called, widgetIds=${appWidgetIds.toList()}")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        private const val TAG = "OsSpacesListReceiver"
    }
}
