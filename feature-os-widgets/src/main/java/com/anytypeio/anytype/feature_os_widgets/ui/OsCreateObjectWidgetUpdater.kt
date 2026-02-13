package com.anytypeio.anytype.feature_os_widgets.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.anytypeio.anytype.feature_os_widgets.receiver.OsCreateObjectWidgetReceiver
import timber.log.Timber

/**
 * Utility object to update the Create Object widget from outside the feature module.
 * Used by the configuration activity to refresh the widget after configuration is saved.
 */
object OsCreateObjectWidgetUpdater {
    
    /**
     * Updates a specific Create Object widget by its appWidgetId.
     */
    suspend fun updateWidget(context: Context, appWidgetId: Int) {
        Timber.d("OsCreateObjectWidgetUpdater: updateWidget called for appWidgetId=$appWidgetId")
        try {
            // Small delay to ensure DataStore has fully persisted the data
            kotlinx.coroutines.delay(100)
            
            // Use updateAll - simpler and more reliable
            val widget = OsCreateObjectWidget()
            Timber.d("OsCreateObjectWidgetUpdater: calling updateAll")
            widget.updateAll(context)
            Timber.d("OsCreateObjectWidgetUpdater: updateAll completed")
        } catch (e: Exception) {
            Timber.e(e, "OsCreateObjectWidgetUpdater: update failed for appWidgetId=$appWidgetId")
            // Don't re-throw - let the config activity complete
        }
    }
    
    /**
     * Updates all Create Object widgets.
     */
    suspend fun updateAllWidgets(context: Context) {
        Timber.d("OsCreateObjectWidgetUpdater: updateAllWidgets called")
        OsCreateObjectWidget().updateAll(context)
    }
}
