package com.anytypeio.anytype.feature_object_type.fields

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.presentation.objects.ObjectIcon

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

    sealed class Item : UiFieldsListItem() {
        abstract val fieldKey: Key
        abstract val fieldTitle: String
        abstract val format: RelationFormat
        abstract val limitObjectTypes: List<UiFieldObjectItem>
        abstract val canDelete: Boolean

        data class Default(
            override val id: Id,
            override val fieldKey: Key,
            override val fieldTitle: String,
            override val format: RelationFormat,
            override val limitObjectTypes: List<UiFieldObjectItem> = emptyList(),
            override val canDelete: Boolean = false
        ) : Item()

        data class Draggable(
            override val id: Id,
            override val fieldKey: Key,
            override val fieldTitle: String,
            override val format: RelationFormat,
            override val limitObjectTypes: List<UiFieldObjectItem> = emptyList(),
            override val canDelete: Boolean = false
        ) : Item()
    }

    sealed class Section : UiFieldsListItem() {
        abstract val canAdd: Boolean

        data class Header(
            override val id: Id = ID,
            override val canAdd: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_header"
            }
        }

        data class SideBar(
            override val id: Id = ID,
            override val canAdd: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_sidebar"
            }
        }

        data class Hidden(
            override val id: Id = ID,
            override val canAdd: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_hidden"
            }
        }

        data class Local(
            override val id: Id = ID,
            override val canAdd: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_local"
            }
        }
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
        abstract val item: UiFieldsListItem.Item

        data class Edit(
            override val item: UiFieldsListItem.Item
        ) : Visible()

        data class New(
            override val item: UiFieldsListItem.Item
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


