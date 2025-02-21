package com.anytypeio.anytype.feature_object_type.ui

import androidx.compose.runtime.Immutable
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.TemplateView

data class ObjectTypeVmParams(
    val objectId: Id,
    val spaceId: SpaceId,
    val withSubscriptions: Boolean,
    val showHiddenFields: Boolean
)

//region OBJECT TYPE HEADER (title + icon)
data class UiTitleState(val title: String, val isEditable: Boolean) {
    companion object {
        val EMPTY = UiTitleState(title = "", isEditable = false)
    }
}

data class UiIconState(val icon: ObjectIcon, val isEditable: Boolean) {
    companion object {
        val EMPTY = UiIconState(icon = ObjectIcon.None, isEditable = false)
    }
}
//endregion

//region LAYOUTS
sealed class UiLayoutButtonState{
    data object Hidden : UiLayoutButtonState()
    data class Visible(val layout: ObjectType.Layout) : UiLayoutButtonState()
}

sealed class UiLayoutTypeState{
    data object Hidden : UiLayoutTypeState()
    data class Visible(
        val layouts: List<ObjectType.Layout>,
        val selectedLayout: ObjectType.Layout? = null
    ) : UiLayoutTypeState()
}
//endregion

sealed class UiFieldsButtonState{
    data object Hidden : UiFieldsButtonState()
    data class Visible(val count: Int) : UiFieldsButtonState()

}

//region MENU
@Immutable
sealed class UiMenuSetItem{
    data object Hidden: UiMenuSetItem()
    data object CreateSet: UiMenuSetItem()
    @Immutable
    data class OpenSet(val setId: Id): UiMenuSetItem()
}

data class UiMenuState(
    val container: MenuSortsItem.Container,
    val sorts: List<MenuSortsItem.Sort>,
    val types: List<MenuSortsItem.SortType>,
    val objSetItem: UiMenuSetItem
) {
    companion object {
        val EMPTY = UiMenuState(
            container = MenuSortsItem.Container(sort = ObjectsListSort.ByName()),
            sorts = emptyList(),
            types = emptyList(),
            objSetItem = UiMenuSetItem.Hidden
        )
    }
}

@Immutable
sealed class UiSettingsMenuState {
    data object Hidden : UiSettingsMenuState()
    @Immutable
    data class Visible(
        val menuItems: List<UiSettingsMenuItem>
    ) : UiSettingsMenuState()
}

@Immutable
sealed class UiTemplatesMenuState {
    data object Hidden : UiTemplatesMenuState()
    @Immutable
    data class Visible(
        val menuItems: List<UiTemplatesMenuItem>
    ) : UiTemplatesMenuState()
}

@Immutable
enum class UiSettingsMenuItem {
    DELETE
}

@Immutable
enum class UiTemplatesMenuItem {
    DELETE, DUPLICATE
}

@Immutable
enum class UiObjectsMenuItem {
    OPEN_SET, SORT_BY,
}
//endregion

//region TEMPLATES HEADER
sealed class UiTemplatesHeaderState{
    data object Hidden : UiTemplatesHeaderState()
    data class Visible(val count: String): UiTemplatesHeaderState()
}

sealed class UiTemplatesAddIconState{
    data object Hidden : UiTemplatesAddIconState()
    data object Visible: UiTemplatesAddIconState()
}
//endregion

//region TEMPLATES LIST
data class UiTemplatesListState(
    val items: List<TemplateView>
) {
    companion object {
        val EMPTY = UiTemplatesListState(items = emptyList())
    }
}
//endregion

//region OBJECTS HEADER
data class UiObjectsHeaderState(val count: String) {
    companion object {
        val EMPTY = UiObjectsHeaderState(count = "")
    }
}

sealed class UiObjectsAddIconState{
    data object Hidden : UiObjectsAddIconState()
    data object Visible: UiObjectsAddIconState()
}

sealed class UiObjectsSettingsIconState{
    data object Hidden : UiObjectsSettingsIconState()
    data object Visible: UiObjectsSettingsIconState()
}
//endregion

sealed class UiEditButton {
    data object Hidden : UiEditButton()
    data object Visible : UiEditButton()
}

//region ERRORS
sealed class UiErrorState {
    data object Hidden : UiErrorState()
    data class Show(val reason: Reason) : UiErrorState()

    sealed class Reason {
        data class ErrorGettingObjects(val msg: String) : Reason()
        data class Other(val msg: String) : Reason()
    }
}
//endregion

//region ALERTS
sealed class UiDeleteAlertState {
    data object Hidden : UiDeleteAlertState()
    data object Show : UiDeleteAlertState()
}
//endregion

//region SYNC STATUS
sealed class UiSyncStatusWidgetState {
    data object Hidden : UiSyncStatusWidgetState()
    data class Visible(val status: SyncStatusWidgetState) : UiSyncStatusWidgetState()
}

sealed class UiSyncStatusBadgeState {
    data object Hidden : UiSyncStatusBadgeState()
    data class Visible(val status: SpaceSyncAndP2PStatusState) : UiSyncStatusBadgeState()
}
//endregion