package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.ObjectWrapper

/**
 * Interface for syncing spaces data to OS home screen widget.
 * The widget displays a list of spaces for quick access.
 */
interface OsWidgetSpacesSync {

    /**
     * Updates the widget's cached spaces list.
     * Should be called whenever the spaces list changes in the app.
     *
     * @param spaces List of active SpaceView objects to display in widget
     */
    suspend fun sync(spaces: List<ObjectWrapper.SpaceView>)
}
