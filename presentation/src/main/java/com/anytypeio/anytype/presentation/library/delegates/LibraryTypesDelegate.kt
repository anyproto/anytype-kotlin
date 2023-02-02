package com.anytypeio.anytype.presentation.library.delegates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_ID
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.dashboard.DEFAULT_KEYS
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.QueryListenerLibTypes
import com.anytypeio.anytype.presentation.library.filterByQuery
import com.anytypeio.anytype.presentation.objects.toLibraryViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class LibraryTypesDelegate @Inject constructor(
    private val container: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder
) : LibraryListDelegate, QueryListenerLibTypes {

    override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

    override fun onQueryLibTypes(string: String) {
        queryFlow.value = string
    }

    override val itemsFlow: Flow<LibraryScreenState.Tabs.TabData> = combine(
        itemsFlow(),
        queryFlow()
    ) { items, query ->
        LibraryScreenState.Tabs.TabData(
            items
                .toLibraryViews(urlBuilder)
                .filterByQuery(query)
        )
    }

    override fun itemsFlow() = container.subscribe(buildSearchParams())

    private fun buildSearchParams(): StoreSearchParams {
        return StoreSearchParams(
            subscription = SUB_LIBRARY_TYPES,
            keys = DEFAULT_KEYS,
            filters = buildList {
                addAll(ObjectSearchConstants.filterTypes())
                add(
                    DVFilter(
                        relation = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = MARKETPLACE_ID
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = MarketplaceObjectTypeIds.OBJECT_TYPE
                    )
                )
            }
        )
    }

}

private const val SUB_LIBRARY_TYPES = "subscription.library_types"