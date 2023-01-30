package com.anytypeio.anytype.presentation.library.delegates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.LibrarySearchParams
import com.anytypeio.anytype.presentation.dashboard.DEFAULT_KEYS
import com.anytypeio.anytype.presentation.library.LibraryInteractor
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.QueryListenerLibRelations
import com.anytypeio.anytype.presentation.library.filterByQuery
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class LibraryRelationsDelegate @Inject constructor(
    private val interactor: LibraryInteractor
) : LibraryListDelegate, QueryListenerLibRelations {

    override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

    override fun onQueryLibRelations(string: String) {
        queryFlow.value = string
    }

    override val itemsFlow: Flow<LibraryScreenState.Tabs.TabData> = combine(
        itemsFlow(),
        queryFlow()
    ) { items, query ->
        LibraryScreenState.Tabs.TabData(items.filterByQuery(query))
    }

    override fun itemsFlow() = interactor.subscribe(buildSearchParams())

    private fun buildSearchParams(): LibrarySearchParams {
        return LibrarySearchParams(
            subscription = SUB_LIBRARY_RELATIONS,
            keys = DEFAULT_KEYS,
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

private const val SUB_LIBRARY_RELATIONS = "subscription.library_relations"