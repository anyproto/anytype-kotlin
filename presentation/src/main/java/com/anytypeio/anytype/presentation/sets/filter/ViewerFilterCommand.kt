package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.core_models.Id

sealed class ViewerFilterCommand {

    sealed class Modal : ViewerFilterCommand() {

        object ShowRelationList : Modal()

        data class UpdateInputValueFilter(
            val relation: Id,
            val filterIndex: Int
        ) : Modal()

        data class UpdateSelectValueFilter(
            val relation: Id,
            val filterIndex: Int
        ) : Modal()
    }
}