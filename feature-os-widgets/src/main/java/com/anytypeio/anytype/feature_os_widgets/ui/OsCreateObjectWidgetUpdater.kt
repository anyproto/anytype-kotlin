package com.anytypeio.anytype.feature_os_widgets.ui

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll

/**
 * Utility object to update the Create Object widget from outside the feature module.
 * Used by the configuration activity to refresh the widget after configuration is saved.
 */
object OsCreateObjectWidgetUpdater {
    
    /**
     * Updates a specific Create Object widget by its appWidgetId.
     */
    suspend fun updateWidget(context: Context, appWidgetId: Int) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        OsCreateObjectWidget().update(context, glanceId)
    }
    
    /**
     * Updates all Create Object widgets.
     */
    suspend fun updateAllWidgets(context: Context) {
        OsCreateObjectWidget().updateAll(context)
    }
}
