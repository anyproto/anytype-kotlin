package com.anytypeio.anytype.presentation.library.delegates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.dashboard.DEFAULT_KEYS
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.presentation.library.QueryListenerLibRelations
import com.anytypeio.anytype.presentation.library.filterByQuery
import com.anytypeio.anytype.presentation.objects.toLibraryViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class LibraryRelationsDelegate @Inject constructor(
    private val container: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder
) : LibraryListDelegate, QueryListenerLibRelations {

    override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

    override fun onQueryLibRelations(string: String) {
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
                .optAddEmptyPlaceholder(query)
        )
    }

    override fun itemsFlow() = container.subscribe(buildSearchParams())

    private fun buildSearchParams(): StoreSearchParams {
        return StoreSearchParams(
            subscription = SUB_LIBRARY_RELATIONS,
            keys = DEFAULT_KEYS + listOf(Relations.RELATION_FORMAT),
            filters = buildList {
                addAll(ObjectSearchConstants.filterMarketplaceRelations())
                add(
                    DVFilter(
                        relation = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = Marketplace.MARKETPLACE_ID
                    )
                )
            }
        )
    }

}

private fun List<LibraryView>.optAddEmptyPlaceholder(query: String): List<LibraryView> {
    val q = query.trim()
    val result = this
    return if (q.isNotEmpty() && result.isEmpty()) {
        listOf<LibraryView>(LibraryView.LibraryRelationsPlaceholderView(name = q))
    } else {
        result
    }
}

private const val SUB_LIBRARY_RELATIONS = "subscription.library_relations"