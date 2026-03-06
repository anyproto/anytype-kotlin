package com.anytypeio.anytype.feature_os_widgets.persistence

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
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
        viewerId: String
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

        val typesMap = fetchObjectTypesForSpace(SpaceId(spaceId))
        return fetchDataViewItems(objectView, viewerId, spaceId, typesMap)
    }

    /**
     * Fetches items using an already-loaded [ObjectView] and pre-fetched [typesMap].
     */
    suspend fun fetchItems(
        objectView: ObjectView,
        viewerId: String,
        spaceId: String,
        typesMap: Map<Id, ObjectWrapper.Type>
    ): List<OsWidgetDataViewItemEntity> {
        return fetchDataViewItems(objectView, viewerId, spaceId, typesMap)
    }

    private suspend fun fetchDataViewItems(
        objectView: ObjectView,
        viewerId: String,
        spaceId: String,
        typesMap: Map<Id, ObjectWrapper.Type>
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
            fetchCollectionItems(objectView, spaceId, filters, sorts, keys)
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
        keys: List<String>
    ): List<ObjectWrapper.Basic> {
        val subscriptionId = "os-widget-dv-${objectView.root}"
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

    private suspend fun fetchObjectTypesForSpace(spaceId: SpaceId): Map<Id, ObjectWrapper.Type> {
        val filters = buildList {
            add(DVFilter(
                relation = Relations.IS_DELETED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            ))
            add(DVFilter(
                relation = Relations.IS_ARCHIVED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            ))
            add(DVFilter(
                relation = Relations.TYPE_UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = ObjectTypeUniqueKeys.TEMPLATE
            ))
            add(DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.EQUAL,
                value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
            ))
            add(DVFilter(
                relation = Relations.UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EMPTY
            ))
        }
        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = emptyList(),
            keys = ObjectSearchConstants.defaultKeysObjectType,
            limit = 0
        )
        return try {
            val results = searchObjects(params).getOrNull() ?: emptyList()
            results.mapNotNull { obj ->
                obj.map.mapToObjectWrapperType()?.let { type ->
                    type.id to type
                }
            }.toMap()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching object types for space")
            emptyMap()
        }
    }
}
