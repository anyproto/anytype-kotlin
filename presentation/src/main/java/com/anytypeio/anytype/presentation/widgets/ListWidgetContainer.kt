package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.Subscriptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ListWidgetContainer(
    private val workspace: Id,
    private val subscription: Id,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    isWidgetCollapsed: Flow<Boolean>
) : WidgetContainer {

    override val view: Flow<WidgetView> = isWidgetCollapsed.flatMapLatest { isCollapsed ->
        if (isCollapsed) {
            flowOf(
                WidgetView.ListOfObjects(
                    id = subscription,
                    type = resolveType(),
                    elements = emptyList(),
                    isExpanded = false
                )
            )
        } else {
            storage.subscribe(buildParams()).map { objects ->
                WidgetView.ListOfObjects(
                    id = subscription,
                    type = resolveType(),
                    elements = objects.map { obj ->
                        WidgetView.ListOfObjects.Element(
                            obj = obj,
                            icon = ObjectIcon.from(
                                obj = obj,
                                layout = obj.layout,
                                builder = urlBuilder
                            )
                        )
                    },
                    isExpanded = true,
                )
            }
        }
    }

    private fun buildParams() = params(subscription = subscription, workspace = workspace)

    private fun resolveType() = when (subscription) {
        Subscriptions.SUBSCRIPTION_RECENT -> WidgetView.ListOfObjects.Type.Recent
        Subscriptions.SUBSCRIPTION_SETS -> WidgetView.ListOfObjects.Type.Sets
        Subscriptions.SUBSCRIPTION_FAVORITES -> WidgetView.ListOfObjects.Type.Favorites
        else -> throw IllegalStateException("Unexpected subscription: $subscription")
    }

    companion object {
        private const val MAX_COUNT = 3
        fun params(subscription: Id, workspace: Id) = when (subscription) {
            Subscriptions.SUBSCRIPTION_RECENT -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = ObjectSearchConstants.sortTabRecent,
                    filters = ObjectSearchConstants.filterTabRecent(workspace),
                    keys = ObjectSearchConstants.defaultKeys,
                    limit = MAX_COUNT
                )
            }
            Subscriptions.SUBSCRIPTION_SETS -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = ObjectSearchConstants.sortTabSets,
                    filters = ObjectSearchConstants.filterTabSets(workspace),
                    keys = ObjectSearchConstants.defaultKeys,
                    limit = MAX_COUNT
                )
            }
            Subscriptions.SUBSCRIPTION_ARCHIVED -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = ObjectSearchConstants.sortTabArchive,
                    filters = ObjectSearchConstants.filterTabArchive(workspace),
                    keys = ObjectSearchConstants.defaultKeys,
                    limit = MAX_COUNT
                )
            }
            Subscriptions.SUBSCRIPTION_FAVORITES -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = emptyList(),
                    filters = ObjectSearchConstants.filterTabFavorites(workspace),
                    keys = ObjectSearchConstants.defaultKeys,
                    limit = MAX_COUNT
                )
            }
            else -> throw IllegalStateException("Unexpected subscription: $subscription")
        }
    }
}