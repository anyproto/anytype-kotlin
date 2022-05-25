package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.SortingExpression

sealed class ViewerSortByCommand {

    sealed class Modal : ViewerSortByCommand() {
        data class ShowSortingKeyList(
            val old: String?,
            val relations: ArrayList<SimpleRelationView>,
            val sortingExpression: ArrayList<SortingExpression>
        ) : Modal()

        data class ShowSortingTypeList(val key: String, val selected: Int) : Modal()
    }

    data class Apply(val sorts: List<SortingExpression>) : ViewerSortByCommand()

    object BackToCustomize: ViewerSortByCommand()
}