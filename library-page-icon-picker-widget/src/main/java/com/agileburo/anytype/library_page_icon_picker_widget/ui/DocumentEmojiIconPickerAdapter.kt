package com.agileburo.anytype.library_page_icon_picker_widget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.library_page_icon_picker_widget.R
import com.agileburo.anytype.library_page_icon_picker_widget.model.DocumentEmojiIconPickerView
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerViewDiffUtil
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_FILTER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerViewHolder.Companion.HOLDER_EMOJI_ITEM
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_filter.view.*

class DocumentEmojiIconPickerAdapter(
    private var views: List<DocumentEmojiIconPickerView>,
    private val onFilterQueryChanged: (String) -> Unit,
    private val onEmojiClicked: (String, String) -> Unit
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
            HOLDER_EMOJI_FILTER -> DocumentEmojiIconPickerViewHolder.EmojiFilter(
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_page_icon_picker_emoji_filter,
                    parent,
                    false
                )
            ).apply {
                itemView.filterInputField.doOnTextChanged { text, _, _, _ ->
                    onFilterQueryChanged(text.toString())
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemCount(): Int = views.size
    override fun getItemViewType(position: Int) = views[position].getViewType()

    override fun onBindViewHolder(holder: DocumentEmojiIconPickerViewHolder, position: Int) {
        when (holder) {
            is DocumentEmojiIconPickerViewHolder.CategoryHeader -> {
                holder.bind(views[position] as DocumentEmojiIconPickerView.GroupHeader)
            }
            is DocumentEmojiIconPickerViewHolder.EmojiItem -> {
                holder.bind(
                    item = views[position] as DocumentEmojiIconPickerView.Emoji,
                    onEmojiClicked = onEmojiClicked
                )
            }
        }
    }

    fun update(update: List<DocumentEmojiIconPickerView>) {
        val result = DiffUtil.calculateDiff(
            PageIconPickerViewDiffUtil(
                old = views,
                new = update
            )
        )
        views = update
        result.dispatchUpdatesTo(this)
    }
}