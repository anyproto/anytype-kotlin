package com.anytypeio.anytype.feature_object_type.ui

import androidx.compose.runtime.Immutable
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.TemplateView

data class ObjectTypeVmParams(
    val objectId: Id,
    val spaceId: SpaceId,
    val showHiddenFields: Boolean,
    val initialViewId: Id? = null
)

sealed class ObjectTypeCommand {

    data object Back : ObjectTypeCommand()

    data class OpenTemplate(
        val templateId: Id,
        val typeId: Id,
        val typeKey: Key,
        val spaceId: Id
    ) : ObjectTypeCommand()

    data class OpenAddNewPropertyScreen(val typeId: Id, val space: Id) : ObjectTypeCommand()

    data class ShowToast(val msg: String) : ObjectTypeCommand()
}

//region OBJECT TYPE HEADER (title + icon)
data class UiTitleState(val title: String, val originalName: String, val isEditable: Boolean) {
    companion object {
        val EMPTY = UiTitleState(title = "", originalName = "", isEditable = false)
    }
}

data class UiIconState(val icon: ObjectIcon.TypeIcon, val isEditable: Boolean) {
    companion object {
        val EMPTY = UiIconState(icon = ObjectIcon.TypeIcon.Default.DEFAULT, isEditable = false)
    }
}

data class UiDescriptionState(
    val description: String,
    val isVisible: Boolean,
    val isEditable: Boolean
) {
    companion object {
        val EMPTY = UiDescriptionState(
            description = "",
            isVisible = false,
            isEditable = false
        )
    }
}
//endregion

//region LAYOUTS
sealed class UiLayoutButtonState {
    data object Hidden : UiLayoutButtonState()
    data class Visible(val layout: ObjectType.Layout) : UiLayoutButtonState()
}

sealed class UiLayoutTypeState {
    data object Hidden : UiLayoutTypeState()
    data class Visible(
        val layouts: List<ObjectType.Layout>,
        val selectedLayout: ObjectType.Layout? = null
    ) : UiLayoutTypeState()
}
//endregion

data class UiHorizontalButtonsState(
    val uiPropertiesButtonState: UiPropertiesButtonState,
    val uiLayoutButtonState: UiLayoutButtonState,
    val uiTemplatesButtonState: UiTemplatesButtonState,
    val isVisible: Boolean
)

sealed class UiPropertiesButtonState {
    data object Hidden : UiPropertiesButtonState()
    data class Visible(val count: Int) : UiPropertiesButtonState()

}

sealed class UiTemplatesButtonState {
    data object Hidden : UiTemplatesButtonState()
    data class Visible(val count: Int) : UiTemplatesButtonState()
}

//region MENU
@Immutable
sealed class UiMenuSetItem {
    data object Hidden : UiMenuSetItem()
    data object CreateSet : UiMenuSetItem()

    @Immutable
    data class OpenSet(val setId: Id) : UiMenuSetItem()
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
sealed class UiTemplatesHeaderState {
    data object Hidden : UiTemplatesHeaderState()
    data class Visible(val count: String) : UiTemplatesHeaderState()
}

sealed class UiTemplatesAddIconState {
    data object Hidden : UiTemplatesAddIconState()
    data object Visible : UiTemplatesAddIconState()
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

sealed class UiTemplatesModalListState {
    abstract val items: List<TemplateView>

    data class Hidden(
        override val items: List<TemplateView>,
    ) : UiTemplatesModalListState() {
        companion object {
            val EMPTY = Hidden(items = emptyList())
        }
    }

    data class Visible(
        override val items: List<TemplateView>,
        val showAddIcon: Boolean
    ) :
        UiTemplatesModalListState()
}
//endregion

//region OBJECTS HEADER
data class UiObjectsHeaderState(val count: String) {
    companion object {
        val EMPTY = UiObjectsHeaderState(count = "")
    }
}

sealed class UiObjectsAddIconState {
    data object Hidden : UiObjectsAddIconState()
    data object Visible : UiObjectsAddIconState()
}

sealed class UiObjectsSettingsIconState {
    data object Hidden : UiObjectsSettingsIconState()
    data object Visible : UiObjectsSettingsIconState()
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
        data object ErrorEditingTypeDetails : Reason()
        data class Other(val msg: String) : Reason()
    }
}
//endregion

//region ALERTS
sealed class UiDeleteAlertState {
    data object Hidden : UiDeleteAlertState()
    data object Show : UiDeleteAlertState()
}

/**
 * Represents an object item in the delete type confirmation dialog.
 * Shows objects that use the type being deleted.
 */
@Immutable
data class DeleteAlertObjectItem(
    val id: Id,
    val name: String,
    val icon: ObjectIcon
)

/**
 * State for the "Move Type to Bin" confirmation bottom sheet.
 * Shows a list of objects using this type, allowing users to select which ones
 * should also be moved to bin along with the type.
 */
@Immutable
sealed class UiDeleteTypeAlertState {
    data object Hidden : UiDeleteTypeAlertState()

    @Immutable
    data class Visible(
        val typeName: String,
        val objects: List<DeleteAlertObjectItem>,
        val selectedObjectIds: Set<Id>
    ) : UiDeleteTypeAlertState() {
        val isAllSelected: Boolean
            get() = objects.isNotEmpty() && selectedObjectIds.size == objects.size
    }
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

//region Type icon screen
sealed class UiIconsPickerState {
    data object Hidden : UiIconsPickerState()
    data object Visible : UiIconsPickerState()
}

//endregion
