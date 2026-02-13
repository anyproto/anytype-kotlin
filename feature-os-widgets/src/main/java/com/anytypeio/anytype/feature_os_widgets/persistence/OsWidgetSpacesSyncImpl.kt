package com.anytypeio.anytype.feature_os_widgets.persistence

import android.content.Context
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.widgets.OsWidgetSpacesSync
import timber.log.Timber

/**
 * Implementation of [OsWidgetSpacesSync] that persists spaces data
 * to DataStore for OS home screen widget consumption.
 * 
 * Icons are downloaded and cached locally because the middleware HTTP server
 * is only available when the app is running, but widgets need to display
 * icons even when the app is closed.
 */
class OsWidgetSpacesSyncImpl(
    private val context: Context,
    private val urlBuilder: UrlBuilder
) : OsWidgetSpacesSync {

    private val dataStore by lazy { OsWidgetsDataStore(context) }
    private val iconCache by lazy { OsWidgetIconCache(context) }

    companion object {
        private const val TAG = "OsWidget"
    }

    override suspend fun sync(spaces: List<ObjectWrapper.SpaceView>) {
        val pinnedSpaces = spaces
            // Only include active, pinned spaces (spaces with non-null/non-empty spaceOrder)
            .filter { it.isActive && !it.spaceOrder.isNullOrEmpty() }
            // Sort pinned spaces by spaceOrder (ascending)
            .sortedWith(compareBy(nullsLast()) { it.spaceOrder })

        val entities = mutableListOf<OsWidgetSpaceEntity>()
        for (space in pinnedSpaces) {
            entities.add(space.toWidgetEntity())
        }
        dataStore.saveSpaces(entities)
        
        // Note: We don't cleanup stale icons here because sync() is called
        // multiple times during startup with partial data. Icons are cleaned
        // up on logout via clearAll() instead.
    }

    private suspend fun ObjectWrapper.SpaceView.toWidgetEntity(): OsWidgetSpaceEntity {
        val spaceId = targetSpaceId.orEmpty()
        val iconHash = iconImage?.takeIf { it.isNotEmpty() }
        
        Timber.tag(TAG).d("Processing space '$name' (id=$spaceId), iconHash=$iconHash")
        
        // Try to cache the icon locally (download from middleware server)
        val localIconPath = if (iconHash != null) {
            val url = urlBuilder.thumbnail(iconHash)
            Timber.tag(TAG).d("Built URL for space $spaceId: $url")
            iconCache.cacheIcon(url, spaceId) ?: iconCache.getCachedIconPath(spaceId)
        } else {
            Timber.tag(TAG).d("No icon hash for space $spaceId, using placeholder")
            null
        }
        
        Timber.tag(TAG).d("Final icon path for space $spaceId: $localIconPath")
        
        if (iconHash != null && localIconPath == null) {
            Timber.tag(TAG).w("Could not cache icon for space $spaceId, will use placeholder")
        }
        
        return OsWidgetSpaceEntity(
            spaceId = spaceId,
            name = name.orEmpty(),
            iconImageUrl = localIconPath,
            iconColorIndex = iconOption?.toInt() ?: 0,
            spaceUxType = spaceUxType?.ordinal ?: 0
        )
    }
}
