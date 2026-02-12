package com.anytypeio.anytype.persistence.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.widgets.OsWidgetSpacesSync

/**
 * Implementation of [OsWidgetSpacesSync] that persists spaces data
 * to DataStore for OS home screen widget consumption.
 */
class OsWidgetSpacesSyncImpl(
    private val context: Context
) : OsWidgetSpacesSync {

    private val dataStore by lazy { OsWidgetsDataStore(context) }

    override suspend fun sync(spaces: List<ObjectWrapper.SpaceView>) {
        val entities = spaces
            .filter { it.isActive }
            .map { space -> space.toWidgetEntity() }
        dataStore.saveSpaces(entities)
    }

    private fun ObjectWrapper.SpaceView.toWidgetEntity(): OsWidgetSpaceEntity {
        return OsWidgetSpaceEntity(
            spaceId = targetSpaceId.orEmpty(),
            name = name.orEmpty(),
            iconImageHash = iconImage?.takeIf { it.isNotEmpty() },
            iconColorIndex = iconOption?.toInt() ?: 0,
            spaceUxType = spaceUxType?.ordinal ?: 0
        )
    }
}
