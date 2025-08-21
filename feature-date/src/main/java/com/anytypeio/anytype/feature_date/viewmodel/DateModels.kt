package com.anytypeio.anytype.feature_date.viewmodel

import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds

data class DateObjectVmParams(
    val objectId: Id,
    val spaceId: SpaceId
)

data class ActiveField(
    val key: RelationKey,
    val format: RelationFormat,
    val sort: DVSortType = DVSortType.DESC
)

sealed class UiHeaderState {

    data object Empty : UiHeaderState()
    data object Loading : UiHeaderState()
    data class Content(
        val title: String,
        val relativeDate: RelativeDate
    ) : UiHeaderState()
}

sealed class UiCalendarIconState {
    data object Hidden : UiCalendarIconState()
    data class Visible(val timestampInSeconds: TimestampInSeconds) : UiCalendarIconState()
}

sealed class UiSyncStatusBadgeState {
    data object Hidden : UiSyncStatusBadgeState()
    data class Visible(val status: SpaceSyncAndP2PStatusState) : UiSyncStatusBadgeState()
}

data class UiFieldsState(
    val items: List<UiFieldsItem>,
    val selectedRelationKey: RelationKey? = null,
    val needToScrollTo: Boolean = false
) {
    companion object {

        val Empty = UiFieldsState(items = emptyList())

        val LoadingState =
            UiFieldsState(
                items = listOf(
                    UiFieldsItem.Loading.Settings("Loading-Settings"),
                    UiFieldsItem.Loading.Item("Loading-Item-1"),
                    UiFieldsItem.Loading.Item("Loading-Item-2"),
                    UiFieldsItem.Loading.Item("Loading-Item-3"),
                    UiFieldsItem.Loading.Item("Loading-Item-4")
                ),
            )
    }
}

sealed class UiFieldsItem {

    abstract val id: String

    sealed class Loading(override val id: String) : UiFieldsItem() {
        data class Item(override val id: String) : Loading(id)
        data class Settings(override val id: String) : Loading(id)
    }

    data class Settings(
        override val id: String = "UiHorizontalListItem-Settings-Id"
    ) : UiFieldsItem()

    sealed class Item : UiFieldsItem() {
        abstract val key: RelationKey
        abstract val relationFormat: RelationFormat
        abstract val title: String

        data class Default(
            override val id: String,
            override val key: RelationKey,
            override val relationFormat: RelationFormat,
            override val title: String
        ) : Item()

        data class Mention(
            override val id: String,
            override val key: RelationKey,
            override val relationFormat: RelationFormat,
            override val title: String
        ) : Item()
    }
}

sealed class UiFieldsSheetState {
    data object Hidden : UiFieldsSheetState()
    data class Visible(
        val items: List<UiFieldsItem>
    ) : UiFieldsSheetState()
}

sealed class UiCalendarState {
    data object Hidden : UiCalendarState()
    data class Calendar(
        val timeInMillis: TimeInMillis?
    ) : UiCalendarState()
}

sealed class UiErrorState {
    data object Hidden : UiErrorState()
    data class Show(val reason: Reason) : UiErrorState()

    sealed class Reason {
        data class YearOutOfRange(val min: Int, val max: Int) : Reason()
        data class ErrorGettingFields(val msg: String) : Reason()
        data class ErrorGettingObjects(val msg: String) : Reason()
        data class Other(val msg: String) : Reason()
    }
}

sealed class UiSnackbarState {
    data object Hidden : UiSnackbarState()
    data class Visible(val message: String, val objId: Id) : UiSnackbarState()
}
