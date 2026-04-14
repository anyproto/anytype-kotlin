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

    /**
     * In-memory record of the icon hash we last cached to disk for a given
     * spaceId. Because sync() runs on every debounced vault emission, this
     * lets us skip the network download + bitmap decode + file write when
     * the icon for a given space has not changed since the last sync.
     */
    private val lastCachedIconHashBySpace = mutableMapOf<String, String>()

    /**
     * In-memory snapshot of the entities we most recently wrote to DataStore.
     * Used to short-circuit the DataStore write when nothing changed across
     * successive debounced syncs.
     */
    private var lastWrittenEntities: List<OsWidgetSpaceEntity>? = null

    companion object {
        private const val TAG = "OsWidget"
    }

    override suspend fun sync(spaces: List<ObjectWrapper.SpaceView>) {
        Timber.tag(TAG).d("sync called with ${spaces.size} total spaces")
        spaces.forEachIndexed { i, s ->
            Timber.tag(TAG).d("  input[$i]: name=${s.name}, targetSpaceId=${s.targetSpaceId}, isActive=${s.isActive}, spaceOrder=${s.spaceOrder}, spaceUxType=${s.spaceUxType}")
        }
        val activeSpaces = spaces.filter { it.isActive }

        Timber.tag(TAG).d("Active spaces: ${activeSpaces.size} (from ${spaces.size} total)")

        val entities = mutableListOf<OsWidgetSpaceEntity>()
        for (space in activeSpaces) {
            entities.add(space.toWidgetEntity())
        }
        if (entities == lastWrittenEntities) {
            Timber.tag(TAG).d("Entities unchanged, skipping DataStore write")
            return
        }
        Timber.tag(TAG).d("Saving ${entities.size} entities to DataStore")
        dataStore.saveSpaces(entities)
        lastWrittenEntities = entities.toList()
        
        // Note: We don't cleanup stale icons here because sync() is called
        // multiple times during startup with partial data. Icons are cleaned
        // up on logout via clearAll() instead.
    }

    private suspend fun ObjectWrapper.SpaceView.toWidgetEntity(): OsWidgetSpaceEntity {
        val spaceId = targetSpaceId.orEmpty()
        val iconHash = iconImage?.takeIf { it.isNotEmpty() }
        
        Timber.tag(TAG).d("Processing space '$name' (id=$spaceId), iconHash=$iconHash")
        
        // Try to cache the icon locally (download from middleware server).
        // Skip the download if we already cached this exact hash for this
        // space — reuse the existing file on disk.
        val localIconPath = if (iconHash != null) {
            val alreadyCachedPath = iconCache.getCachedIconPath(spaceId)
                ?.takeIf { lastCachedIconHashBySpace[spaceId] == iconHash }
            if (alreadyCachedPath != null) {
                Timber.tag(TAG).d("Icon unchanged for space $spaceId, reusing $alreadyCachedPath")
                alreadyCachedPath
            } else {
                val url = urlBuilder.thumbnail(iconHash)
                Timber.tag(TAG).d("Built URL for space $spaceId: $url")
                val cached = iconCache.cacheIcon(url, spaceId)
                if (cached != null) {
                    lastCachedIconHashBySpace[spaceId] = iconHash
                    cached
                } else {
                    iconCache.getCachedIconPath(spaceId)
                }
            }
        } else {
            Timber.tag(TAG).d("No icon hash for space $spaceId, using placeholder")
            lastCachedIconHashBySpace.remove(spaceId)
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
            spaceUxType = spaceUxType?.ordinal ?: 0,
            spaceOrder = spaceOrder,
            isOneToOneSpace = isOneToOneSpace
        )
    }
}
