package com.anytypeio.anytype.presentation.library.delegates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.LibrarySearchParams
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.dashboard.DEFAULT_KEYS
import com.anytypeio.anytype.presentation.library.LibraryInteractor
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.QueryListenerMyRelations
import com.anytypeio.anytype.presentation.library.filterByQuery
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

class MyRelationsDelegate @Inject constructor(
    private val interactor: LibraryInteractor,
    private val workspaceManager: WorkspaceManager
) : LibraryListDelegate, QueryListenerMyRelations {

    override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

    override fun onQueryMyRelations(string: String) {
        queryFlow.value = string
    }

    @FlowPreview
    override val itemsFlow: Flow<LibraryScreenState.Tabs.TabData> = combine(
        itemsFlow(),
        queryFlow()
    ) { items, query ->
        LibraryScreenState.Tabs.TabData(items.filterByQuery(query))
    }

    @FlowPreview
    override fun itemsFlow() = flow {
        emit(workspaceManager.getCurrentWorkspace())
    }.flatMapMerge {
        val searchParams = buildSearchParams(it)
        interactor.subscribe(searchParams)
    }

    private fun buildSearchParams(workspaceId: Id): LibrarySearchParams {
        return LibrarySearchParams(
            subscription = SUB_LIBRARY_MY_RELATIONS,
            keys = DEFAULT_KEYS,
            filters = buildList {
                addAll(ObjectSearchConstants.filterMyRelations())
                add(
                    DVFilter(
                        relationKey = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = workspaceId
                    )
                )
                add(
                    DVFilter(
                        relationKey = Relations.RELATION_KEY,
                        condition = DVFilterCondition.NOT_IN,
                        value = Relations.systemRelationKeys
                    )
                )
            }
        )
    }

}

private const val SUB_LIBRARY_MY_RELATIONS = "subscription.library_my_relations"