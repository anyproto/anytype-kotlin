package com.anytypeio.anytype.feature_date.models

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem.Loading
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class DateObjectTopToolbarState {

    data object Empty : DateObjectTopToolbarState()
    data class Content(val status: SpaceSyncAndP2PStatusState?) : DateObjectTopToolbarState()

    sealed class Action {
        data object SyncStatus : Action()
        data object Calendar : Action()
    }
}

sealed class DateObjectHeaderState {

    data object Empty : DateObjectHeaderState()
    data object Loading : DateObjectHeaderState()
    data class Content(
        val title: String
    ) : DateObjectHeaderState()

    sealed class Action {
        data object Next : Action()
        data object Previous : Action()
    }
}


data class DateObjectHorizontalListState(
    val items: List<UiHorizontalListItem>,
    val selectedRelationKey: RelationKey?
) {
    companion object {
        fun empty(): DateObjectHorizontalListState =
            DateObjectHorizontalListState(
                items = emptyList(),
                selectedRelationKey = null
            )

        fun loadingState(): DateObjectHorizontalListState =
            DateObjectHorizontalListState(
                items = listOf(
                    Loading.Settings("Loading-Settings"),
                    Loading.Item("Loading-Item-1"),
                    Loading.Item("Loading-Item-2"),
                    Loading.Item("Loading-Item-3"),
                    Loading.Item("Loading-Item-4")
                ),
                selectedRelationKey = null
            )
    }
}

sealed class UiHorizontalListItem {

    abstract val id: String

    sealed class Loading(override val id: String) : UiHorizontalListItem() {
        data class Item(override val id: String) : Loading(id)
        data class Settings(override val id: String) : Loading(id)
    }

    data class Settings(
        override val id: String = "UiHorizontalListItem-Settings-Id"
    ) : UiHorizontalListItem()

    data class Item(
        override val id: String,
        val key: RelationKey,
        val title: String,
        val relationFormat: RelationFormat
    ) : UiHorizontalListItem()
}

data class DateObjectVerticalListState(
    val items: List<UiVerticalListItem>
) {
    companion object {

        fun empty(): DateObjectVerticalListState =
            DateObjectVerticalListState(
                items = emptyList()
            )

        fun loadingState(): DateObjectVerticalListState = DateObjectVerticalListState(
            items = listOf(
                UiVerticalListItem.Loading("Loading-Item-1"),
                UiVerticalListItem.Loading("Loading-Item-2"),
                UiVerticalListItem.Loading("Loading-Item-3"),
                UiVerticalListItem.Loading("Loading-Item-4"),
            )
        )
    }
}

sealed class UiVerticalListItem {

    abstract val id: String

    data class Loading(override val id: String) : UiVerticalListItem()

    data class Item(
        override val id: String,
        val name: String,
        val space: SpaceId?,
        val type: String? = null,
        val typeName: String? = null,
        val createdBy: String? = null,
        val layout: ObjectType.Layout? = null,
        val icon: ObjectIcon = ObjectIcon.None
    ) : UiVerticalListItem()
}

data class DateObjectBottomMenu(val isOwnerOrEditor: Boolean = true) {
    sealed class Action {
        object GlobalSearch : Action()
        object AddDoc : Action()
        object CreateObjectLong : Action()
        object Back : Action()
        object BackLong : Action()
    }
}

sealed class UiContentState {
    data class Idle(val scrollToTop: Boolean = false) : UiContentState()
    data object InitLoading : UiContentState()
    data object Paging : UiContentState()
    data object Empty : UiContentState()
    data class Error(
        val message: String,
    ) : UiContentState()
}

sealed class DateObjectSheetState {
    data object Empty : DateObjectSheetState()
    data class Content(
        val items: List<UiHorizontalListItem>
    ) : DateObjectSheetState()
}

sealed class UiCalendarState {
    data object Empty : UiCalendarState()
    data class Calendar(
        val timeInMillis: TimeInMillis?
    ) : UiCalendarState()
}

sealed class UiErrorState {
    data object Hidden : UiErrorState()
    data class Show(val reason: Reason) : UiErrorState()

    sealed class Reason {
        data class YearOutOfRange(val min: Int, val max: Int) : Reason()
    }
}
