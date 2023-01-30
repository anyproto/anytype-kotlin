package com.anytypeio.anytype.presentation.library.delegates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_ID
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.LibrarySearchParams
import com.anytypeio.anytype.presentation.dashboard.DEFAULT_KEYS
import com.anytypeio.anytype.presentation.library.LibraryInteractor
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.QueryListenerLibTypes
import com.anytypeio.anytype.presentation.library.filterByQuery
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class LibraryTypesDelegate @Inject constructor(
    private val interactor: LibraryInteractor
) : LibraryListDelegate, QueryListenerLibTypes {

    override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

    override fun onQueryLibTypes(string: String) {
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
            subscription = SUB_LIBRARY_TYPES,
            keys = DEFAULT_KEYS,
            filters = buildList {
                ObjectSearchConstants.filterTypes()
                add(
                    DVFilter(
                        relation = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = MARKETPLACE_ID
                    )
                )
            }
        )
    }

}

private const val SUB_LIBRARY_TYPES = "subscription.library_types"