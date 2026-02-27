package com.anytypeio.anytype.feature_os_widgets.ui

import android.content.Context
import androidx.glance.appwidget.updateAll
import timber.log.Timber

/**
 * Utility object to update the Spaces List widget.
 * Triggers a re-render of all instances of OsSpacesListWidget.
 */
object OsSpacesListWidgetUpdater {

    /**
     * Updates all Spaces List widgets.
     * Should be called after spaces data changes in DataStore.
     */
    suspend fun updateAllWidgets(context: Context) {
        try {
            OsSpacesListWidget().updateAll(context)
            Timber.d("OsSpacesListWidgetUpdater: updateAll completed")
        } catch (e: Exception) {
            Timber.e(e, "OsSpacesListWidgetUpdater: updateAll failed")
        }
    }
}
