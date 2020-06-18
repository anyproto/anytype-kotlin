package com.agileburo.anytype.library_page_icon_picker_widget.model

import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_FILTER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_ITEM

sealed class DocumentEmojiIconPickerView : ViewType {
    /**
     * @property alias short name or convenient name for an emoji.
     */
    data class Emoji(
        val alias: String,
        val unicode: String
    ) : DocumentEmojiIconPickerView() {
        override fun getViewType() = HOLDER_EMOJI_ITEM
    }

    /**
     * @property category emoji category
     */
    data class GroupHeader(
        val category: String
    ) : DocumentEmojiIconPickerView() {
        override fun getViewType() = HOLDER_EMOJI_CATEGORY_HEADER
    }

    /**
     * Emoji filter.
     */
    object EmojiFilter : DocumentEmojiIconPickerView() {
        override fun getViewType() = HOLDER_EMOJI_FILTER
    }
}