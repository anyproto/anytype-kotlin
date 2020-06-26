package com.agileburo.anytype.library_page_icon_picker_widget.model

import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_ITEM

sealed class EmojiPickerView : ViewType {

    /**
     * @property page emoji's page (emoji category)
     * @property index emoji's index on the [page]
     * @property unicode emoji's char
     */
    data class Emoji(
        val unicode: String,
        val page: Int,
        val index: Int
    ) : EmojiPickerView() {
        override fun getViewType() = HOLDER_EMOJI_ITEM
    }

    /**
     * @property category emoji category
     */
    data class GroupHeader(
        val category: Int
    ) : EmojiPickerView() {
        override fun getViewType() = HOLDER_EMOJI_CATEGORY_HEADER
    }
}