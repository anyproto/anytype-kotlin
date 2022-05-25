package com.anytypeio.anytype.presentation.editor.picker

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.ui.ViewType
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock

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

        fun itemsExperimental(): List<AddBlockView> = listOf(
            AddBlockView.Section(category = UiBlock.Category.TEXT),
            AddBlockView.Item(type = UiBlock.TEXT),
            AddBlockView.Item(type = UiBlock.HEADER_ONE),
            AddBlockView.Item(type = UiBlock.HEADER_TWO),
            AddBlockView.Item(type = UiBlock.HEADER_THREE),
            AddBlockView.Item(type = UiBlock.HIGHLIGHTED),
            AddBlockView.Section(category = UiBlock.Category.LIST),
            AddBlockView.Item(type = UiBlock.CHECKBOX),
            AddBlockView.Item(type = UiBlock.BULLETED),
            AddBlockView.Item(type = UiBlock.NUMBERED),
            AddBlockView.Item(type = UiBlock.TOGGLE),
            AddBlockView.Section(category = UiBlock.Category.OBJECT),
            AddBlockView.Item(type = UiBlock.PAGE),
            AddBlockView.Item(type = UiBlock.FILE),
            AddBlockView.Item(type = UiBlock.IMAGE),
            AddBlockView.Item(type = UiBlock.VIDEO),
            AddBlockView.Item(type = UiBlock.BOOKMARK),
            AddBlockView.Item(type = UiBlock.LINK_TO_OBJECT),
            AddBlockView.Section(category = UiBlock.Category.RELATION),
            AddBlockView.Item(type = UiBlock.RELATION),
            AddBlockView.Section(category = UiBlock.Category.OTHER),
            AddBlockView.Item(type = UiBlock.LINE_DIVIDER),
            AddBlockView.Item(type = UiBlock.THREE_DOTS),
            AddBlockView.Item(type = UiBlock.CODE)
        )

        fun itemsStable(): List<AddBlockView> = listOf(
            AddBlockView.Section(category = UiBlock.Category.TEXT),
            AddBlockView.Item(type = UiBlock.TEXT),
            AddBlockView.Item(type = UiBlock.HEADER_ONE),
            AddBlockView.Item(type = UiBlock.HEADER_TWO),
            AddBlockView.Item(type = UiBlock.HEADER_THREE),
            AddBlockView.Item(type = UiBlock.HIGHLIGHTED),
            AddBlockView.Section(category = UiBlock.Category.LIST),
            AddBlockView.Item(type = UiBlock.CHECKBOX),
            AddBlockView.Item(type = UiBlock.BULLETED),
            AddBlockView.Item(type = UiBlock.NUMBERED),
            AddBlockView.Item(type = UiBlock.TOGGLE),
            AddBlockView.Section(category = UiBlock.Category.OBJECT),
            AddBlockView.Item(type = UiBlock.PAGE),
            AddBlockView.Item(type = UiBlock.FILE),
            AddBlockView.Item(type = UiBlock.IMAGE),
            AddBlockView.Item(type = UiBlock.VIDEO),
            AddBlockView.Item(type = UiBlock.BOOKMARK),
            AddBlockView.Item(type = UiBlock.LINK_TO_OBJECT),
            AddBlockView.Section(category = UiBlock.Category.OTHER),
            AddBlockView.Item(type = UiBlock.LINE_DIVIDER),
            AddBlockView.Item(type = UiBlock.THREE_DOTS),
            AddBlockView.Item(type = UiBlock.CODE)
        )
    }
}