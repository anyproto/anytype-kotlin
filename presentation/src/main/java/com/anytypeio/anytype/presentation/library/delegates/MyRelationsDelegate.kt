package com.anytypeio.anytype.presentation.library.delegates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.dashboard.DEFAULT_KEYS
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.presentation.library.QueryListenerMyRelations
import com.anytypeio.anytype.presentation.library.filterByQuery
import com.anytypeio.anytype.presentation.objects.toLibraryViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

class MyRelationsDelegate @Inject constructor(
    private val container: StorelessSubscriptionContainer,
    private val workspaceManager: WorkspaceManager,
    private val urlBuilder: UrlBuilder
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
        LibraryScreenState.Tabs.TabData(
            items
                .toLibraryViews(urlBuilder)
                .filterByQuery(query)
                .optAddCreateRelationView(query)
        )
    }

    @FlowPreview
    override fun itemsFlow() = flow {
        emit(workspaceManager.getCurrentWorkspace())
    }.flatMapMerge {
        val searchParams = buildSearchParams(it)
        container.subscribe(searchParams)
    }

    private fun buildSearchParams(workspaceId: Id): StoreSearchParams {
        return StoreSearchParams(
            subscription = SUB_LIBRARY_MY_RELATIONS,
            keys = DEFAULT_KEYS + listOf(
                Relations.SOURCE_OBJECT,
                Relations.RELATION_FORMAT,
                Relations.RELATION_READ_ONLY_VALUE
            ),
            filters = buildList {
                addAll(ObjectSearchConstants.filterMyRelations())
                add(
                    DVFilter(
                        relation = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = workspaceId
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.RELATION_KEY,
                        condition = DVFilterCondition.NOT_IN,
                        value = Relations.systemRelationKeys
                    )
                )
            }
        )
    }

}

private fun List<LibraryView>.optAddCreateRelationView(query: String): MutableList<LibraryView> {
    val q = query.trim()
    val result = this.toMutableList()
    return if (q.isNotEmpty() && result.none { it.name.lowercase() == q.lowercase() }) {
        result.apply {
            add(0, LibraryView.CreateNewRelationView(name = q))
        }
    } else {
        result
    }
}

private const val SUB_LIBRARY_MY_RELATIONS = "subscription.library_my_relations"