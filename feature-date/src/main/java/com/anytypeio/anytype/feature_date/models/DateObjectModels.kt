package com.anytypeio.anytype.feature_date.models

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class DateObjectTopToolbarState {

    data object Hidden : DateObjectTopToolbarState()
    data class Content(
        val syncStatus: SpaceSyncStatus
    ) : DateObjectTopToolbarState()

    sealed class Action {
        data object SyncStatus : Action()
        data object Calendar : Action()
    }
}

sealed class DateObjectHeaderState {

    data object Hidden : DateObjectHeaderState()
    data class Content(
        val title: String
    ) : DateObjectHeaderState()

    sealed class Action {
        data object Next : Action()
        data object Previous : Action()
    }
}


sealed class DateObjectHorizontalListState {

    data object Empty : DateObjectHorizontalListState()

    data class Content(
        val items: List<UiHorizontalListItem>,
        val selectedItem: Id?
    ) : DateObjectHorizontalListState()
}

sealed class UiHorizontalListItem {
    abstract val id: String

    data class MentionedIn(
        override val id: String = "UiHorizontalListItem-MentionedIn-Id"
    ) : UiHorizontalListItem()

    data class Settings(
        override val id: String = "UiHorizontalListItem-Settings-Id"
    ) : UiHorizontalListItem()

    data class Item(
        override val id: String, val key: RelationKey, val title: String
    ) : UiHorizontalListItem()
}

sealed class DateObjectVerticalListState {

    data object Empty : DateObjectVerticalListState()

    data class Content(
        val items: List<UiVerticalListItem>
    ) : DateObjectVerticalListState()
}

data class UiVerticalListItem(
    val id: String,
    val name: String,
    val space: SpaceId,
    val type: String? = null,
    val typeName: String? = null,
    val createdBy: String? = null,
    val layout: ObjectType.Layout? = null,
    val icon: ObjectIcon = ObjectIcon.None
)

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

