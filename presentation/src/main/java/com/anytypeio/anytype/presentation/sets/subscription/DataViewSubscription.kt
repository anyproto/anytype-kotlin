package com.anytypeio.anytype.presentation.sets.subscription

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.getSetOf
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.updateFormatForSubscription
import com.anytypeio.anytype.presentation.sets.viewerById
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

interface DataViewSubscription {

    suspend fun startObjectSetSubscription(
        context: Id,
        workspaceId: Id,
        state: ObjectState.DataView.Set,
        session: ObjectSetSession,
        offset: Long,
        storeOfRelations: StoreOfRelations
    ): Flow<DataViewSubscriptionContainer.Index>

    suspend fun startObjectCollectionSubscription(
        context: Id,
        collection: Id,
        workspaceId: Id,
        state: ObjectState.DataView.Collection,
        session: ObjectSetSession,
        offset: Long,
        storeOfRelations: StoreOfRelations
    ): Flow<DataViewSubscriptionContainer.Index>
}

class DefaultDataViewSubscription(private val dataViewSubscriptionContainer: DataViewSubscriptionContainer) :
    DataViewSubscription {

    override suspend fun startObjectCollectionSubscription(
        context: Id,
        collection: Id,
        workspaceId: Id,
        state: ObjectState.DataView.Collection,
        session: ObjectSetSession,
        offset: Long,
        storeOfRelations: StoreOfRelations
    ): Flow<DataViewSubscriptionContainer.Index> {
        if (context.isEmpty() || collection.isEmpty()) {
            Timber.w("Data view collection subscription: context or collection is empty")
            return emptyFlow()
        }
        val currentViewerId = session.currentViewerId.value
        val activeViewer = state.viewerById(currentViewerId)
        if (activeViewer == null) {
            Timber.w("Data view collection subscription: active viewer is null")
            return emptyFlow()
        }
        val filters =
            activeViewer.filters.updateFormatForSubscription(storeOfRelations) + ObjectSearchConstants.defaultDataViewFilters(
                workspaceId = workspaceId
            )
        val dataViewLinksKeys = state.dataViewContent.relationLinks.map { it.key }
        val keys = ObjectSearchConstants.defaultDataViewKeys + dataViewLinksKeys

        val params = DataViewSubscriptionContainer.Params(
            collection = collection,
            subscription = getSubscriptionId(context),
            sorts = activeViewer.sorts,
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
        workspaceId: Id,
        state: ObjectState.DataView.Set,
        session: ObjectSetSession,
        offset: Long,
        storeOfRelations: StoreOfRelations
    ): Flow<DataViewSubscriptionContainer.Index> {
        if (context.isEmpty()) {
            Timber.w("Data view set subscription: context is empty")
            return emptyFlow()
        }
        val currentViewerId = session.currentViewerId.value
        val activeViewer = state.viewerById(currentViewerId)
        if (activeViewer == null) {
            Timber.w("Data view set subscription: active viewer is null")
            return emptyFlow()
        }
        val source = state.getSetOf(ctx = context)
        if (source.isEmpty()) {
            Timber.w("Data view set subscription: source is empty")
            return emptyFlow()
        }
        val filters =
            activeViewer.filters.updateFormatForSubscription(storeOfRelations) + ObjectSearchConstants.defaultDataViewFilters(
                workspaceId = workspaceId
            )
        val dataViewLinksKeys = state.dataViewContent.relationLinks.map { it.key }
        val keys = ObjectSearchConstants.defaultDataViewKeys + dataViewLinksKeys

        val params = DataViewSubscriptionContainer.Params(
            subscription = getSubscriptionId(context),
            sorts = activeViewer.sorts,
            filters = filters,
            sources = source,
            keys = keys,
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = offset
        )
        return dataViewSubscriptionContainer.observe(params)
    }

    companion object {
        const val DATA_VIEW_SUBSCRIPTION_POSTFIX = "-dataview"

        fun getSubscriptionId(context: Id) = "$context$DATA_VIEW_SUBSCRIPTION_POSTFIX"
    }
}