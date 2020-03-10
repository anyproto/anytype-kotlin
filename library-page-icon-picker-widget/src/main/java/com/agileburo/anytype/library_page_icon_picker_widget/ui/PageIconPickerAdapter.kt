package com.agileburo.anytype.library_page_icon_picker_widget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerView
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerViewDiffUtil
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_CHOOSE_EMOJI
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_EMOJI_FILTER
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_EMOJI_ITEM
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_PICK_RANDOM_EMOJI
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder.Companion.HOLDER_UPLOAD_PHOTO
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_filter.view.*

class PageIconPickerAdapter(
    private var views: List<PageIconPickerView>,
    private val onUploadPhotoClicked: () -> Unit,
    private val onSetRandomEmojiClicked: () -> Unit,
    private val onFilterQueryChanged: (String) -> Unit,
    private val onEmojiClicked: (String, String) -> Unit
) : RecyclerView.Adapter<PageIconPickerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageIconPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HOLDER_UPLOAD_PHOTO -> PageIconPickerViewHolder.UploadPhoto(
                view = inflater.inflate(
                    R.layout.item_page_icon_picker_upload_photo,
                    parent,
                    false
                )
            ).apply {
                itemView.setOnClickListener { onUploadPhotoClicked() }
            }
            HOLDER_PICK_RANDOM_EMOJI -> PageIconPickerViewHolder.PickRandom(
                view = inflater.inflate(
                    R.layout.item_page_icon_picker_pick_emoji_randomly,
                    parent,
                    false
                )
            ).apply {
                itemView.setOnClickListener { onSetRandomEmojiClicked() }
            }
            HOLDER_CHOOSE_EMOJI -> PageIconPickerViewHolder.ChooseEmoji(
                view = inflater.inflate(
                    R.layout.item_page_icon_picker_choose_emoji,
                    parent,
                    false
                )
            )
            HOLDER_EMOJI_CATEGORY_HEADER -> PageIconPickerViewHolder.CategoryHeader(
                view = inflater.inflate(
                    R.layout.item_page_icon_picker_emoji_category_header,
                    parent,
                    false
                )
            )
            HOLDER_EMOJI_ITEM -> PageIconPickerViewHolder.EmojiItem(
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_page_icon_picker_emoji_item,
                    parent,
                    false
                )
            )
            HOLDER_EMOJI_FILTER -> PageIconPickerViewHolder.EmojiFilter(
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

    override fun onBindViewHolder(holder: PageIconPickerViewHolder, position: Int) {
        when (holder) {
            is PageIconPickerViewHolder.CategoryHeader -> {
                holder.bind(views[position] as PageIconPickerView.GroupHeader)
            }
            is PageIconPickerViewHolder.EmojiItem -> {
                holder.bind(
                    item = views[position] as PageIconPickerView.Emoji,
                    onEmojiClicked = onEmojiClicked
                )
            }
        }
    }

    fun update(update: List<PageIconPickerView>) {
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