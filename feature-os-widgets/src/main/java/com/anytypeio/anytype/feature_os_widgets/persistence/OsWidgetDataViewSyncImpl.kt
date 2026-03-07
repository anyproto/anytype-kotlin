package com.anytypeio.anytype.feature_os_widgets.persistence

import android.content.Context
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.widgets.OsWidgetDataViewSync
import timber.log.Timber

/**
 * Implementation of [OsWidgetDataViewSync] that reads all configured data view widgets,
 * re-fetches items for each, and persists the updated data to DataStore.
 */
class OsWidgetDataViewSyncImpl(
    private val context: Context,
    private val getObject: GetObject,
    private val searchObjects: SearchObjects,
    private val blockRepository: BlockRepository
) : OsWidgetDataViewSync {

    private val dataStore by lazy { OsWidgetsDataStore(context) }
    private val fetcher by lazy {
        DataViewItemsFetcher(
            getObject = getObject,
            searchObjects = searchObjects,
            blockRepository = blockRepository
        )
    }

    companion object {
        private const val TAG = "OsWidget"
    }

    override suspend fun sync() {
        val configs = dataStore.getAllDataViewConfigs()
        if (configs.isEmpty()) return

        Timber.tag(TAG).d("Data view widget sync: refreshing ${configs.size} widget(s)")

        for (config in configs) {
            try {
                val items = fetcher.fetchItems(
                    spaceId = config.spaceId,
                    objectId = config.objectId,
                    viewerId = config.viewerId,
                    subscriptionKey = config.appWidgetId.toString()
                )
                val updated = config.copy(items = items)
                dataStore.saveDataViewConfig(updated)
                Timber.tag(TAG).d(
                    "Synced widget ${config.appWidgetId}: ${items.size} items"
                )
            } catch (e: Exception) {
                Timber.tag(TAG).w(
                    e, "Failed to sync widget ${config.appWidgetId}"
                )
            }
        }
    }
}
