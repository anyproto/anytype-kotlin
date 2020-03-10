package com.agileburo.anytype.library_page_icon_picker_widget.model

import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_CHOOSE_EMOJI
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_EMOJI_FILTER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_EMOJI_ITEM
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_PICK_RANDOM_EMOJI
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_UPLOAD_PHOTO

sealed class PageIconPickerView : ViewType {

    /**
     * @property alias short name or convenient name for an emoji.
     */
    data class Emoji(
        val alias: String,
        val unicode: String
    ) : PageIconPickerView() {
        override fun getViewType() = HOLDER_EMOJI_ITEM
    }

    /**
     * @property category emoji category
     */
    data class GroupHeader(
        val category: String
    ) : PageIconPickerView() {
        override fun getViewType() = HOLDER_EMOJI_CATEGORY_HEADER
    }

    /**
     * Emoji filter.
     */
    object EmojiFilter : PageIconPickerView() {
        override fun getViewType() = HOLDER_EMOJI_FILTER
    }

    /**
     * User actions related to emoji picker feature.
     */
    sealed class Action : PageIconPickerView() {
        object UploadPhoto : Action() {
            override fun getViewType() = HOLDER_UPLOAD_PHOTO
        }
        object PickRandomly : Action() {
            override fun getViewType() = HOLDER_PICK_RANDOM_EMOJI
        }
        object ChooseEmoji : Action() {
            override fun getViewType() = HOLDER_CHOOSE_EMOJI
        }
    }
}