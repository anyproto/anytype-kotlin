package com.anytypeio.anytype.library_page_icon_picker_widget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.library_page_icon_picker_widget.R
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_ITEM

class DocumentEmojiIconPickerAdapter(
    private var views: List<EmojiPickerView>,
    private val onEmojiClicked: (String) -> Unit
) : RecyclerView.Adapter<DocumentEmojiIconPickerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentEmojiIconPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HOLDER_EMOJI_CATEGORY_HEADER -> DocumentEmojiIconPickerViewHolder.CategoryHeader(
                view = inflater.inflate(
                    R.layout.item_page_icon_picker_emoji_category_header,
                    parent,
                    false
                )
            )
            HOLDER_EMOJI_ITEM -> DocumentEmojiIconPickerViewHolder.EmojiItem(
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_page_icon_picker_emoji_item,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemCount(): Int = views.size
    override fun getItemViewType(position: Int) = views[position].getViewType()

    override fun onBindViewHolder(holder: DocumentEmojiIconPickerViewHolder, position: Int) {
        when (holder) {
            is DocumentEmojiIconPickerViewHolder.CategoryHeader -> {
                holder.bind(views[position] as EmojiPickerView.GroupHeader)
            }
            is DocumentEmojiIconPickerViewHolder.EmojiItem -> {
                holder.bind(
                    item = views[position] as EmojiPickerView.Emoji,
                    onEmojiClicked = onEmojiClicked
                )
            }
        }
    }

    fun update(update: List<EmojiPickerView>) {
        views = update
        notifyDataSetChanged()
    }
}