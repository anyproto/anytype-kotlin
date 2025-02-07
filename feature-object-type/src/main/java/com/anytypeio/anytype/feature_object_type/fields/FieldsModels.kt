package com.anytypeio.anytype.feature_object_type.fields

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon

data class TypeFieldsVmParams(
    val objectId: Id,
    val spaceId: SpaceId
)

//region Top bar
sealed class UiFieldsTitleState {
    data object Hidden : UiFieldsTitleState()
    data class Visible(val title: String) : UiFieldsTitleState()
}

sealed class UiFieldsCancelButtonState {
    data object Hidden : UiFieldsCancelButtonState()
    data object Visible : UiFieldsCancelButtonState()
}

sealed class UiFieldsSaveButtonState {
    data object Hidden : UiFieldsSaveButtonState()
    data object Visible : UiFieldsSaveButtonState()
}

sealed class UiFieldsEditingPanelState {
    data object Hidden : UiFieldsEditingPanelState()
    data object Visible : UiFieldsEditingPanelState()
}
//endregion

//region Fields List
data class UiFieldsListState(val items: List<UiFieldsListItem>) {
    companion object {
        val EMPTY = UiFieldsListState(emptyList())
    }
}

sealed class UiFieldsListItem {
    abstract val id: Id

    data class FieldItem(
        override val id: Id,
        val fieldKey: Key,
        val fieldTitle: String,
        val format: RelationFormat,
        val limitObjectTypes: List<UiFieldObjectItem> = emptyList(),
        val canDrag: Boolean = false,
        val canDelete: Boolean = false
    ) : UiFieldsListItem()

    sealed class Section : UiFieldsListItem() {
        data class Header(
            override val id: Id = "section_header",
        ) : Section()
        data class FieldsMenu(
            override val id: Id = "section_fields_menu",
        ) : Section()
        data class Hidden(
            override val id: Id = "section_hidden",
        ): Section()
    }
}
//endregion

//region Edit or New Field

data class UiFieldObjectItem(
    val id: Id, val key: Key, val title: String, val icon: ObjectIcon
)

sealed class UiFieldEditOrNewState {
    data object Hidden : UiFieldEditOrNewState()
    sealed class Visible : UiFieldEditOrNewState() {
        abstract val item: UiFieldsListItem.FieldItem

        data class Edit(
            override val item: UiFieldsListItem.FieldItem
        ) : Visible()

        data class New(
            override val item: UiFieldsListItem.FieldItem
        ) : Visible()
    }
}
//endregion

//region ERRORS
sealed class UiFieldsErrorState {
    data object Hidden : UiFieldsErrorState()
    data class Show(val reason: Reason) : UiFieldsErrorState()

    sealed class Reason {
        data class ErrorGettingObjects(val msg: String) : Reason()
        data class Other(val msg: String) : Reason()
    }
}
//endregion

//region COMMANDS
sealed class TypeFieldsCommand {
    data class OpenEmojiPicker(val emoji: String) : TypeFieldsCommand()
}
//endregion


