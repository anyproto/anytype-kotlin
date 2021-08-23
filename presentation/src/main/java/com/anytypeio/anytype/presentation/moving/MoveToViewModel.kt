package com.anytypeio.anytype.presentation.moving

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MoveToViewModel(
    urlBuilder: UrlBuilder,
    searchObjects: SearchObjects,
    getObjectTypes: GetObjectTypes,
    analytics: Analytics,
    private val getFlavourConfig: GetFlavourConfig
) : ObjectSearchViewModel(
    urlBuilder = urlBuilder,
    getObjectTypes = getObjectTypes,
    searchObjects = searchObjects,
    analytics = analytics,
    getFlavourConfig = getFlavourConfig,
) {

    val commands = MutableSharedFlow<Command>(replay = 0)

    override fun getSearchObjectsParams(): SearchObjects.Params {

        val filteredTypes = if (getFlavourConfig.isDataViewEnabled()) {
            types.value
                .filter { objectType -> objectType.smartBlockTypes.contains(SmartBlockType.PAGE) }
                .map { objectType -> objectType.url }
        } else {
            listOf(ObjectTypeConst.PAGE)
        }

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
            ),
            DVFilter(
                relationKey = Relations.IS_READ_ONLY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )

        val sorts = listOf(
            DVSort(
                relationKey = Relations.NAME,
                type = DVSortType.ASC
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
            commands.emit(Command.Move(target = target))
        }
    }

    override fun onBottomSheetHidden() {
        viewModelScope.launch {
            commands.emit(Command.Exit)
        }
    }

    sealed class Command {
        object Exit : Command()
        data class Move(val target: Id) : Command()
    }
}