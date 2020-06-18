package com.agileburo.anytype.library_page_icon_picker_widget.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.library_page_icon_picker_widget.model.DocumentEmojiIconPickerView
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_category_header.view.*
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_item.view.*

sealed class DocumentEmojiIconPickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class CategoryHeader(view: View) : DocumentEmojiIconPickerViewHolder(view) {

        private val category = itemView.category

        fun bind(item: DocumentEmojiIconPickerView.GroupHeader) {
            category.text = item.category
        }

    }

    class EmojiItem(view: View) : DocumentEmojiIconPickerViewHolder(view) {

        private val emoji = itemView.emoji

        fun bind(
            item: DocumentEmojiIconPickerView.Emoji,
            onEmojiClicked: (String, String) -> Unit
        ) {
            emoji.text = item.unicode
            itemView.setOnClickListener { onEmojiClicked(item.unicode, item.alias) }
        }
    }

    class EmojiFilter(view: View) : DocumentEmojiIconPickerViewHolder(view)

    companion object {
        const val HOLDER_EMOJI_CATEGORY_HEADER = 1
        const val HOLDER_EMOJI_ITEM = 2
        const val HOLDER_EMOJI_FILTER = 3
    }
}