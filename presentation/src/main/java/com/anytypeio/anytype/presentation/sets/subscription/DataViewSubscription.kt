package com.anytypeio.anytype.presentation.sets.subscription

import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultDataViewFilters
import com.anytypeio.anytype.presentation.sets.filterOutDeletedAndMissingObjects
import com.anytypeio.anytype.presentation.sets.setOfValue
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.extension.removeUnsupportedFilters
import com.anytypeio.anytype.presentation.sets.updateFormatForSubscription
import com.anytypeio.anytype.presentation.sets.viewerByIdOrFirst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

interface DataViewSubscription {

    /**
     * Observes the objects shown by the data view block [blockId] of [context].
     */
    suspend fun startDataViewSubscription(
        context: Id,
        space: Id,
        state: ObjectState.DataView,
        blockId: Id,
        currentViewerId: Id?,
        offset: Long,
        storeOfRelations: StoreOfRelations
    ): Flow<DataViewState>

    suspend fun unsubscribe(ids: List<Id>)
}

class DefaultDataViewSubscription(
    private val dataViewSubscriptionContainer: DataViewSubscriptionContainer
) : DataViewSubscription {

    override suspend fun startDataViewSubscription(
        context: Id,
        space: Id,
        state: ObjectState.DataView,
        blockId: Id,
        currentViewerId: Id?,
        offset: Long,
        storeOfRelations: StoreOfRelations
    ): Flow<DataViewState> {
        if (context.isEmpty()) {
            Timber.w("Data view subscription: context is empty")
            return emptyFlow()
        }
        val activeViewer = state.viewerByIdOrFirst(currentViewerId)
        if (activeViewer == null) {
            Timber.w("Data view subscription: active viewer is null")
            return emptyFlow()
        }

        val isCollection = state.isCollection(blockId)
        val sources = if (isCollection) {
            emptyList()
        } else {
            val setOfValue = state.setOfValue(blockId = blockId)
            if (setOfValue.isEmpty()) {
                Timber.w("Data view subscription: setOf value is empty, proceed without subscription")
                return emptyFlow()
            }
            val query = state.filterOutDeletedAndMissingObjects(setOfValue)
            if (query.isEmpty()) {
                Timber.w(
                    "Data view subscription: query has no valid types or relations, " +
                            "proceed without subscription"
                )
                return emptyFlow()
            }
            query
        }

        val filters = buildList {
            addAll(activeViewer.filters.updateFormatForSubscription(storeOfRelations).removeUnsupportedFilters())
            addAll(defaultDataViewFilters())
        }
        val dataViewLinksKeys = state.dataViewContent.relationLinks.map { it.key }
        val keys = ObjectSearchConstants.defaultDataViewKeys + dataViewLinksKeys
        val sorts = getSortsWithDefaultCreatedDate(
            viewerSorts = activeViewer.sorts,
            storeOfRelations = storeOfRelations
        )

        val params = DataViewSubscriptionContainer.Params(
            space = SpaceId(space),
            collection = if (isCollection) state.source(blockId) else null,
            subscription = getDataViewSubscriptionId(context),
            sorts = sorts,
            filters = filters,
            sources = sources,
            keys = keys,
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = offset
        )
        return dataViewSubscriptionContainer.observe(params)
    }

    override suspend fun unsubscribe(ids: List<Id>) {
        dataViewSubscriptionContainer.unsubscribe(ids)
    }

    companion object {
        private const val DATA_VIEW_SUBSCRIPTION_POSTFIX = "-dataview"
        fun getDataViewSubscriptionId(context: Id) = "$context$DATA_VIEW_SUBSCRIPTION_POSTFIX"
    }
}

suspend fun List<DVSort>.updateWithRelationFormat(storeOfRelations: StoreOfRelations): List<DVSort> {
    return map { sort ->
        val relation = storeOfRelations.getByKey(sort.relationKey)
        sort.copy(relationFormat = relation?.format ?: RelationFormat.LONG_TEXT)
    }
}


private suspend fun getSortsWithDefaultCreatedDate(
    viewerSorts: List<DVSort>,
    storeOfRelations: StoreOfRelations
): List<DVSort> {
    return viewerSorts.ifEmpty {
        listOf(
            DVSort(
                relationKey = Relations.CREATED_DATE,
                type = DVSortType.DESC,
                includeTime = true,
                relationFormat = RelationFormat.DATE
            )
        )
    }.updateWithRelationFormat(storeOfRelations)
}