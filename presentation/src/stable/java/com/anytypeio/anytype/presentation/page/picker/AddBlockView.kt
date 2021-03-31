package com.anytypeio.anytype.presentation.page.picker

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.ui.ViewType
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock

sealed class AddBlockView : ViewType {

    object AddBlockHeader : AddBlockView() {
        override fun getViewType(): Int = VIEW_HOLDER_ADD_BLOCK_HEADER
    }

    object TurnIntoHeader : AddBlockView() {
        override fun getViewType(): Int = VIEW_HOLDER_TURN_INTO_HEADER
    }

    data class Section(val category: UiBlock.Category) : AddBlockView() {
        override fun getViewType(): Int = VIEW_HOLDER_SECTION
    }

    data class Item(val type: UiBlock) : AddBlockView() {
        override fun getViewType(): Int = VIEW_HOLDER_ITEM
    }

    data class ObjectView(
        val url: String,
        val name: String,
        val emoji: String?,
        val description: String?,
        val layout: ObjectType.Layout
    ) : AddBlockView() {
        override fun getViewType(): Int = VIEW_HOLDER_OBJECT_TYPES
    }

    companion object {
        const val VIEW_HOLDER_SECTION = 0
        const val VIEW_HOLDER_ITEM = 1
        const val VIEW_HOLDER_ADD_BLOCK_HEADER = 2
        const val VIEW_HOLDER_TURN_INTO_HEADER = 3
        const val VIEW_HOLDER_OBJECT_TYPES = 4

        fun items(): List<AddBlockView> = listOf(
            Section(category = UiBlock.Category.TEXT),
            Item(type = UiBlock.TEXT),
            Item(type = UiBlock.HEADER_ONE),
            Item(type = UiBlock.HEADER_TWO),
            Item(type = UiBlock.HEADER_THREE),
            Item(type = UiBlock.HIGHLIGHTED),
            Section(category = UiBlock.Category.LIST),
            Item(type = UiBlock.CHECKBOX),
            Item(type = UiBlock.BULLETED),
            Item(type = UiBlock.NUMBERED),
            Item(type = UiBlock.TOGGLE),
            Section(category = UiBlock.Category.OBJECT),
            Item(type = UiBlock.PAGE),
            Item(type = UiBlock.FILE),
            Item(type = UiBlock.IMAGE),
            Item(type = UiBlock.VIDEO),
            Item(type = UiBlock.BOOKMARK),
            Item(type = UiBlock.LINK_TO_OBJECT),
            Section(category = UiBlock.Category.OTHER),
            Item(type = UiBlock.LINE_DIVIDER),
            Item(type = UiBlock.THREE_DOTS),
            Item(type = UiBlock.CODE)
        )
    }
}
