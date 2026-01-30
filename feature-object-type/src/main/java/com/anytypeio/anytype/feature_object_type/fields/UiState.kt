package com.anytypeio.anytype.feature_object_type.fields

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.feature_properties.edit.UiPropertyLimitTypeItem
import com.anytypeio.anytype.core_models.ui.ObjectIcon

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

//todo rename to UiPropertiesListItem
sealed class UiFieldsListItem {
    abstract val id: Id

    sealed class Item : UiFieldsListItem() {
        abstract val fieldKey: Key
        abstract val fieldTitle: String
        abstract val format: RelationFormat
        abstract val limitObjectTypes: List<Id>
        abstract val isPossibleToUnlinkFromType: Boolean
        abstract val isPossibleToMoveToBin: Boolean
        abstract val isEditableField: Boolean

        data class Draggable(
            override val id: Id,
            override val fieldKey: Key,
            override val fieldTitle: String,
            override val format: RelationFormat,
            override val limitObjectTypes: List<Id>,
            override val isPossibleToUnlinkFromType: Boolean,
            override val isPossibleToMoveToBin: Boolean,
            override val isEditableField: Boolean,
            val isPossibleToDrag: Boolean
        ) : Item()

        data class Local(
            override val id: Id,
            override val fieldKey: Key,
            override val fieldTitle: String,
            override val format: RelationFormat,
            override val limitObjectTypes: List<Id>,
            override val isPossibleToUnlinkFromType: Boolean = false,
            override val isPossibleToMoveToBin: Boolean = false,
            override val isEditableField: Boolean
        ) : Item()
    }

    sealed class Section : UiFieldsListItem() {
        abstract val canAdd: Boolean
        abstract val isEmptyState: Boolean

        data class Header(
            override val id: Id = ID,
            override val canAdd: Boolean = false,
            override val isEmptyState: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_header"
            }
        }

        data class SideBar(
            override val id: Id = ID,
            override val canAdd: Boolean = false,
            override val isEmptyState: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_sidebar"
            }
        }

        data class Hidden(
            override val id: Id = ID,
            override val canAdd: Boolean = false,
            override val isEmptyState: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_hidden"
            }
        }

        data class Local(
            override val id: Id = ID,
            override val canAdd: Boolean = false,
            override val isEmptyState: Boolean= false
        ) : Section() {
            companion object {
                const val ID = "section_local"
            }
        }

        data class File(
            override val id: Id = ID,
            override val canAdd: Boolean = false,
            override val isEmptyState: Boolean = false
        ) : Section() {
            companion object {
                const val ID = "section_file_recommended"
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

        data class ViewOnly(
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

//region Section Local Fields Info
sealed class UiLocalsFieldsInfoState {
    data object Hidden : UiLocalsFieldsInfoState()
    data object Visible : UiLocalsFieldsInfoState()
}
//endregion


