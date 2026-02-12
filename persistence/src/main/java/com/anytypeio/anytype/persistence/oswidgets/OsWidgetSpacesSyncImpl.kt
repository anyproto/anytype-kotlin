package com.anytypeio.anytype.persistence.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.widgets.OsWidgetSpacesSync

/**
 * Implementation of [OsWidgetSpacesSync] that persists spaces data
 * to DataStore for OS home screen widget consumption.
 */
class OsWidgetSpacesSyncImpl(
    private val context: Context,
    private val urlBuilder: UrlBuilder
) : OsWidgetSpacesSync {

    private val dataStore by lazy { OsWidgetsDataStore(context) }

    override suspend fun sync(spaces: List<ObjectWrapper.SpaceView>) {
        val entities = spaces
            // Only include active, pinned spaces (spaces with non-null/non-empty spaceOrder)
            .filter { it.isActive && !it.spaceOrder.isNullOrEmpty() }
            // Sort pinned spaces by spaceOrder (ascending)
            .sortedWith(compareBy(nullsLast()) { it.spaceOrder })
            .map { space -> space.toWidgetEntity() }
        dataStore.saveSpaces(entities)
    }

    private fun ObjectWrapper.SpaceView.toWidgetEntity(): OsWidgetSpaceEntity {
        val iconHash = iconImage?.takeIf { it.isNotEmpty() }
        return OsWidgetSpaceEntity(
            spaceId = targetSpaceId.orEmpty(),
            name = name.orEmpty(),
            iconImageUrl = iconHash?.let { urlBuilder.thumbnail(it) },
            iconColorIndex = iconOption?.toInt() ?: 0,
            spaceUxType = spaceUxType?.ordinal ?: 0
        )
    }
}
