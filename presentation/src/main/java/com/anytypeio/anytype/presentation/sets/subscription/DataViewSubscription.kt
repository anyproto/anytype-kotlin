package com.anytypeio.anytype.presentation.sets.subscription

import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultDataViewFilters
import com.anytypeio.anytype.presentation.sets.filterOutDeletedAndMissingObjects
import com.anytypeio.anytype.presentation.sets.getSetOfValue
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.updateFormatForSubscription
import com.anytypeio.anytype.presentation.sets.viewerByIdOrFirst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

interface DataViewSubscription {

    suspend fun startObjectSetSubscription(
        context: Id,
        space: Id,
        state: ObjectState.DataView.Set,
        currentViewerId: Id?,
        offset: Long,
        dataViewRelationLinks: List<RelationLink>
    ): Flow<DataViewState>

    suspend fun startObjectCollectionSubscription(
        context: Id,
        collection: Id,
        space: Id,
        state: ObjectState.DataView.Collection,
        currentViewerId: Id?,
        offset: Long,
        dataViewRelationLinks: List<RelationLink>
    ): Flow<DataViewState>

    suspend fun startObjectTypeSetSubscription(
        context: Id,
        space: Id,
        state: ObjectState.DataView.TypeSet,
        currentViewerId: Id?,
        offset: Long,
        dataViewRelationLinks: List<RelationLink>
    ): Flow<DataViewState>

    suspend fun unsubscribe(ids: List<Id>)
}

class DefaultDataViewSubscription(
    private val dataViewSubscriptionContainer: DataViewSubscriptionContainer
) : DataViewSubscription {

    override suspend fun startObjectCollectionSubscription(
        context: Id,
        collection: Id,
        space: Id,
        state: ObjectState.DataView.Collection,
        currentViewerId: Id?,
        offset: Long,
        dataViewRelationLinks: List<RelationLink>
    ): Flow<DataViewState> {
        if (context.isEmpty() || collection.isEmpty()) {
            Timber.w("Data view collection subscription: context or collection is empty")
            return emptyFlow()
        }
        val activeViewer = state.viewerByIdOrFirst(currentViewerId)
        if (activeViewer == null) {
            Timber.w("Data view collection subscription: active viewer is null")
            return emptyFlow()
        }
        val filters = buildList {
            addAll(activeViewer.filters.updateFormatForSubscription(relationLinks = dataViewRelationLinks))
            addAll(defaultDataViewFilters())
        }
        val dataViewLinksKeys = state.dataViewContent.relationLinks.map { it.key }
        val keys = ObjectSearchConstants.defaultDataViewKeys + dataViewLinksKeys

        val sorts = getSortsWithDefaultCreatedDate(
            viewerSorts = activeViewer.sorts,
            relationLinks = dataViewRelationLinks
        )

        val params = DataViewSubscriptionContainer.Params(
            space = SpaceId(space),
            collection = collection,
            subscription = getDataViewSubscriptionId(context),
            sorts = sorts,
            filters = filters,
            sources = listOf(),
            keys = keys,
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = offset
        )
        return dataViewSubscriptionContainer.observe(params)
    }

    override suspend fun startObjectSetSubscription(
        context: Id,
        space: Id,
        state: ObjectState.DataView.Set,
        currentViewerId: Id?,
        offset: Long,
        dataViewRelationLinks: List<RelationLink>
    ): Flow<DataViewState> {
        if (context.isEmpty()) {
            Timber.w("Data view set subscription: context is empty")
            return emptyFlow()
        }
        val activeViewer = state.viewerByIdOrFirst(currentViewerId)
        if (activeViewer == null) {
            Timber.w("Data view set subscription: active viewer is null")
            return emptyFlow()
        }

        val setOfValue = state.getSetOfValue(ctx = context)
        if (setOfValue.isEmpty()) {
            Timber.w("Data view set subscription: setOf value is empty, proceed without subscription")
            return emptyFlow()
        }

        val query = state.filterOutDeletedAndMissingObjects(setOfValue)
        if (query.isEmpty()) {
            Timber.w(
                "Data view set subscription: query has no valid types or relations, " +
                        "proceed without subscription"
            )
            return emptyFlow()
        }

        val filters = buildList {
            addAll(activeViewer.filters.updateFormatForSubscription(relationLinks = dataViewRelationLinks))
            addAll(defaultDataViewFilters())
        }
        val dataViewLinksKeys = state.dataViewContent.relationLinks.map { it.key }
        val keys = ObjectSearchConstants.defaultDataViewKeys + dataViewLinksKeys
        val sorts = getSortsWithDefaultCreatedDate(
            viewerSorts = activeViewer.sorts,
            relationLinks = dataViewRelationLinks
        )

        val params = DataViewSubscriptionContainer.Params(
            space = SpaceId(space),
            subscription = getDataViewSubscriptionId(context),
            sorts = sorts,
            filters = filters,
            sources = query,
            keys = keys,
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = offset
        )
        return dataViewSubscriptionContainer.observe(params)
    }

    override suspend fun startObjectTypeSetSubscription(
        context: Id,
        space: Id,
        state: ObjectState.DataView.TypeSet,
        currentViewerId: Id?,
        offset: Long,
        dataViewRelationLinks: List<RelationLink>
    ): Flow<DataViewState> {
        if (context.isEmpty()) {
            Timber.w("Data view TypeSet subscription: context is empty")
            return emptyFlow()
        }
        val activeViewer = state.viewerByIdOrFirst(currentViewerId)
        if (activeViewer == null) {
            Timber.w("Data view TypeSet subscription: active viewer is null")
            return emptyFlow()
        }

        val setOfValue = state.getSetOfValue(ctx = context)
        if (setOfValue.isEmpty()) {
            Timber.w("Data view TypeSet subscription: setOf value is empty, proceed without subscription")
            return emptyFlow()
        }

        val query = state.filterOutDeletedAndMissingObjects(setOfValue)
        if (query.isEmpty()) {
            Timber.w(
                "Data view TypeSet subscription: query has no valid types or relations, " +
                        "proceed without subscription"
            )
            return emptyFlow()
        }

        val filters = buildList {
            addAll(activeViewer.filters.updateFormatForSubscription(relationLinks = dataViewRelationLinks))
            addAll(defaultDataViewFilters())
        }
        val dataViewLinksKeys = state.dataViewContent.relationLinks.map { it.key }
        val keys = ObjectSearchConstants.defaultDataViewKeys + dataViewLinksKeys
        val sorts = getSortsWithDefaultCreatedDate(
            viewerSorts = activeViewer.sorts,
            relationLinks = dataViewRelationLinks
        )

        val params = DataViewSubscriptionContainer.Params(
            space = SpaceId(space),
            subscription = getDataViewSubscriptionId(context),
            sorts = sorts,
            filters = filters,
            sources = query,
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

private fun List<DVSort>.updateWithRelationFormat(relationLinks: List<RelationLink>): List<DVSort> {
    return map { sort ->
        val relationLink = relationLinks.find { it.key == sort.relationKey }
        sort.copy(relationFormat = relationLink?.format ?: RelationFormat.LONG_TEXT)
    }
}

private fun getSortsWithDefaultCreatedDate(
    viewerSorts: List<DVSort>,
    relationLinks: List<RelationLink>
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
    }.updateWithRelationFormat(relationLinks)
}