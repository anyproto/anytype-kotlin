package com.anytypeio.anytype.library_page_icon_picker_widget.model

import androidx.recyclerview.widget.DiffUtil

class PageIconPickerViewDiffUtil(
    private val old: List<EmojiPickerView>,
    private val new: List<EmojiPickerView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}