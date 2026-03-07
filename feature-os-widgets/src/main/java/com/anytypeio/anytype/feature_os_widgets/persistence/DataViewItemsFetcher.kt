package com.anytypeio.anytype.feature_os_widgets.persistence

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.extension.removeUnsupportedFilters
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.isCollection
import timber.log.Timber

/**
 * Shared helper for fetching data view items.
 * Used by both [DataViewWidgetConfigViewModel] and [OsWidgetDataViewSyncImpl].
 */
class DataViewItemsFetcher(
    private val getObject: GetObject,
    private val searchObjects: SearchObjects,
    private val blockRepository: BlockRepository
) {

    companion object {
        const val ITEMS_LIMIT = 10
    }

    /**
     * Fetches items for a data view widget config.
     * Resolves type names internally by fetching object types for the space.
     */
    suspend fun fetchItems(
        spaceId: String,
        objectId: String,
        viewerId: String,
        subscriptionKey: String? = null
    ): List<OsWidgetDataViewItemEntity> {
        val objectView = try {
            getObject.run(
                GetObject.Params(
                    target = objectId,
                    space = SpaceId(spaceId),
                    saveAsLastOpened = false
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error fetching object $objectId for widget sync")
            return emptyList()
        }

        val typesMap = fetchObjectTypesForSpace(searchObjects, SpaceId(spaceId))
        return fetchDataViewItems(
            objectView = objectView,
            viewerId = viewerId,
            spaceId = spaceId,
            typesMap = typesMap,
            subscriptionKey = subscriptionKey
        )
    }

    /**
     * Fetches items using an already-loaded [ObjectView] and pre-fetched [typesMap].
     */
    suspend fun fetchItems(
        objectView: ObjectView,
        viewerId: String,
        spaceId: String,
        typesMap: Map<Id, ObjectWrapper.Type>,
        subscriptionKey: String? = null
    ): List<OsWidgetDataViewItemEntity> {
        return fetchDataViewItems(
            objectView = objectView,
            viewerId = viewerId,
            spaceId = spaceId,
            typesMap = typesMap,
            subscriptionKey = subscriptionKey
        )
    }

    private suspend fun fetchDataViewItems(
        objectView: ObjectView,
        viewerId: String,
        spaceId: String,
        typesMap: Map<Id, ObjectWrapper.Type>,
        subscriptionKey: String?
    ): List<OsWidgetDataViewItemEntity> {
        val dv = objectView.blocks
            .find { it.content is Block.Content.DataView }
            ?.content as? Block.Content.DataView
            ?: return emptyList()

        val viewer = dv.viewers.find { it.id == viewerId }
            ?: dv.viewers.firstOrNull()
            ?: return emptyList()

        val viewerFilters = viewer.filters.removeUnsupportedFilters()
        val filters = buildList {
            addAll(viewerFilters)
            addAll(ObjectSearchConstants.defaultDataViewFilters())
        }
        val sorts = viewer.sorts.ifEmpty {
            listOf(
                DVSort(
                    relationKey = Relations.CREATED_DATE,
                    type = DVSortType.DESC,
                    includeTime = true,
                    relationFormat = RelationFormat.DATE
                )
            )
        }
        val dataViewKeys = dv.relationLinks.map { it.key }
        val keys = buildList {
            addAll(ObjectSearchConstants.defaultDataViewKeys)
            addAll(dataViewKeys)
        }.distinct()

        val isCollection = objectView.isCollection()

        val results: List<ObjectWrapper.Basic> = if (isCollection) {
            fetchCollectionItems(
                objectView = objectView,
                spaceId = spaceId,
                filters = filters,
                sorts = sorts,
                keys = keys,
                viewerId = viewerId,
                subscriptionKey = subscriptionKey
            )
        } else {
            fetchSetItems(objectView, spaceId, filters, sorts, keys)
        }

        return results.take(ITEMS_LIMIT).map { obj ->
            val typeId = obj.type.firstOrNull()
            val typeName = typeId?.let { typesMap[it] }?.name.orEmpty()
            OsWidgetDataViewItemEntity(
                id = obj.id,
                name = obj.name.orEmpty(),
                typeName = typeName
            )
        }
    }

    private suspend fun fetchSetItems(
        objectView: ObjectView,
        spaceId: String,
        filters: List<DVFilter>,
        sorts: List<DVSort>,
        keys: List<String>
    ): List<ObjectWrapper.Basic> {
        val struct = objectView.details[objectView.root] ?: emptyMap()
        val setOf = (struct[Relations.SET_OF] as? List<*>)?.firstOrNull() as? String
        if (setOf.isNullOrEmpty()) {
            Timber.w("setOf is empty for object ${objectView.root}")
            return emptyList()
        }
        val params = SearchObjects.Params(
            space = SpaceId(spaceId),
            filters = filters + DVFilter(
                relation = Relations.TYPE,
                condition = DVFilterCondition.IN,
                value = listOf(setOf)
            ),
            sorts = sorts,
            keys = keys,
            limit = ITEMS_LIMIT
        )
        return searchObjects(params).getOrNull().orEmpty()
    }

    private suspend fun fetchCollectionItems(
        objectView: ObjectView,
        spaceId: String,
        filters: List<DVFilter>,
        sorts: List<DVSort>,
        keys: List<String>,
        viewerId: String,
        subscriptionKey: String?
    ): List<ObjectWrapper.Basic> {
        val uniqueKey = subscriptionKey ?: viewerId
        val subscriptionId = "os-widget-dv-${objectView.root}-$uniqueKey"
        return try {
            val result = blockRepository.searchObjectsWithSubscription(
                space = SpaceId(spaceId),
                subscription = subscriptionId,
                sorts = sorts,
                filters = filters,
                keys = keys,
                source = emptyList(),
                offset = 0,
                limit = ITEMS_LIMIT,
                beforeId = null,
                afterId = null,
                ignoreWorkspace = null,
                noDepSubscription = true,
                collection = objectView.root
            )
            result.results
        } catch (e: Exception) {
            Timber.e(e, "Error fetching collection items")
            emptyList()
        } finally {
            try {
                blockRepository.cancelObjectSearchSubscription(listOf(subscriptionId))
            } catch (e: Exception) {
                Timber.w(e, "Error cancelling subscription")
            }
        }
    }

}
