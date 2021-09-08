package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class LinkToObjectViewModel(
    urlBuilder: UrlBuilder,
    searchObjects: SearchObjects,
    getObjectTypes: GetObjectTypes,
    analytics: Analytics
) : ObjectSearchViewModel(
    urlBuilder = urlBuilder,
    getObjectTypes = getObjectTypes,
    searchObjects = searchObjects,
    analytics = analytics
) {

    val commands = MutableSharedFlow<Command>(replay = 0)

    override fun getSearchObjectsParams(): SearchObjects.Params {

        val filteredTypes = types.value.map { objectType -> objectType.url }

        val filters = listOf(
            DVFilter(
                condition = DVFilterCondition.EQUAL,
                value = false,
                relationKey = Relations.IS_ARCHIVED,
                operator = DVFilterOperator.AND
            ),
            DVFilter(
                relationKey = Relations.IS_HIDDEN,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )

        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_OPENED_DATE,
                type = DVSortType.DESC
            )
        )

        return SearchObjects.Params(
            limit = SEARCH_LIMIT,
            objectTypeFilter = filteredTypes,
            filters = filters,
            sorts = sorts,
            fulltext = EMPTY_QUERY
        )
    }

    override fun onObjectClicked(target: Id, layout: ObjectType.Layout?) {
        viewModelScope.launch {
            commands.emit(Command.Link(link = target))
        }
    }

    override fun onDialogCancelled() {
        viewModelScope.launch {
            commands.emit(Command.Exit)
        }
    }

    override suspend fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.value = data.filter {
            SupportedLayouts.layouts.contains(it.layout)
        }
    }

    sealed class Command {
        object Exit : Command()
        data class Link(val link: Id) : Command()
    }
}