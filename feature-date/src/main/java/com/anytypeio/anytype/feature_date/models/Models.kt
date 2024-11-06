package com.anytypeio.anytype.feature_date.models

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class DateLayoutTopToolbarState {

    data object Hidden : DateLayoutTopToolbarState()
    data class Content(
        val syncStatus: SpaceSyncStatus
    ) : DateLayoutTopToolbarState()

    sealed class Action {
        data object SyncStatus : Action()
        data object Calendar : Action()
    }
}

sealed class DateLayoutHeaderState {

    data object Hidden : DateLayoutHeaderState()
    data class Content(
        val title: String
    ) : DateLayoutHeaderState()

    sealed class Action {
        data object Next : Action()
        data object Previous : Action()
    }
}


sealed class DateLayoutHorizontalListState {

    data object Empty : DateLayoutHorizontalListState()

    data class Content(
        val items: List<UiHorizontalListItem>,
        val selectedItem: Id?
    ) : DateLayoutHorizontalListState()
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

sealed class DateLayoutVerticalListState {

    data object Empty : DateLayoutVerticalListState()

    data class Content(
        val items: List<UiVerticalListItem>
    ) : DateLayoutVerticalListState()
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

data class DateLayoutBottomMenu(val isOwnerOrEditor: Boolean = true) {
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

