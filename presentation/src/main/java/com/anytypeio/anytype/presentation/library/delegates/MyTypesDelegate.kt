package com.anytypeio.anytype.presentation.library.delegates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.presentation.library.QueryListenerMyTypes
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

class MyTypesDelegate @Inject constructor(
    private val container: StorelessSubscriptionContainer,
    private val spaceManager: SpaceManager,
    private val urlBuilder: UrlBuilder,
    private val dispatchers: AppCoroutineDispatchers
) : LibraryListDelegate, QueryListenerMyTypes {

    override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

    override fun onQueryMyTypes(string: String) {
        queryFlow.value = string
    }

    @FlowPreview
    override val itemsFlow: Flow<LibraryScreenState.Tabs.TabData> = combine(
        itemsFlow(), queryFlow()
    ) { items, query ->
        LibraryScreenState.Tabs.TabData(
            items
                .toLibraryViews(urlBuilder)
                .filterByQuery(query)
                .optAddCreateTypeView(query)
        )
    }

    @FlowPreview
    override fun itemsFlow() = flow {
        emit(spaceManager.get())
    }.flatMapMerge { space: Id ->
        val searchParams = buildSearchParams(space = space)
        container.subscribe(searchParams)
    }

    private fun buildSearchParams(space: Id): StoreSearchParams {
        return StoreSearchParams(
            subscription = SUB_LIBRARY_MY_TYPES,
            keys = ObjectSearchConstants.defaultKeys + listOf(
                Relations.SOURCE_OBJECT,
                Relations.RESTRICTIONS
            ),
            filters = buildList {
                addAll(ObjectSearchConstants.filterTypes())
                add(
                    DVFilter(
                        relation = Relations.SPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = space
                    )
                )
            }
        )
    }

    override suspend fun unsubscribe() = with(dispatchers.io) {
        container.unsubscribe(listOf(SUB_LIBRARY_MY_TYPES))
    }

}

private fun List<LibraryView>.optAddCreateTypeView(query: String): List<LibraryView> {
    val q = query.trim()
    val result = this
    return if (q.isNotEmpty() && result.none { it.name.lowercase() == q.lowercase() }) {
        buildList {
            add(LibraryView.CreateNewTypeView(name = q))
            addAll(result)
        }
    } else {
        result
    }
}

private const val SUB_LIBRARY_MY_TYPES = "subscription.library_my_types"