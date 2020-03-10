package com.agileburo.anytype.library_page_icon_picker_widget.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerView
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_category_header.view.*
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_item.view.*

sealed class PageIconPickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class UploadPhoto(view: View) : PageIconPickerViewHolder(view)

    class PickRandom(view: View) : PageIconPickerViewHolder(view)

    class ChooseEmoji(view: View) : PageIconPickerViewHolder(view)

    class CategoryHeader(view: View) : PageIconPickerViewHolder(view) {

        private val category = itemView.category

        fun bind(item: PageIconPickerView.GroupHeader) {
            category.text = item.category
        }

    }

    class EmojiItem(view: View) : PageIconPickerViewHolder(view) {

        private val emoji = itemView.emoji

        fun bind(
            item: PageIconPickerView.Emoji,
            onEmojiClicked: (String, String) -> Unit
        ) {
            emoji.text = item.unicode
            itemView.setOnClickListener { onEmojiClicked(item.unicode, item.alias) }
        }
    }

    class EmojiFilter(view: View) : PageIconPickerViewHolder(view)

    companion object {
        const val HOLDER_UPLOAD_PHOTO = 0
        const val HOLDER_PICK_RANDOM_EMOJI = 1
        const val HOLDER_CHOOSE_EMOJI = 2
        const val HOLDER_EMOJI_CATEGORY_HEADER = 3
        const val HOLDER_EMOJI_ITEM = 4
        const val HOLDER_EMOJI_FILTER = 5
    }
}