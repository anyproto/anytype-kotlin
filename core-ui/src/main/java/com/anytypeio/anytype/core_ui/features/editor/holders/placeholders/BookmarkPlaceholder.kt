package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaPlaceholderBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class BookmarkPlaceholder(binding: ItemBlockMediaPlaceholderBinding) : MediaPlaceholder(binding) {

    override fun placeholderClick(target: String, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Bookmark.Placeholder(target))
    }

    override fun setup() {
        title.text = itemView.resources.getString(R.string.hint_add_a_web_bookmark)
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bookmark_placeholder, 0, 0, 0)
    }

    override fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        super.processChangePayload(payloads, item)
        // TODO process loading state changes
    }

    fun isLoading(boolean: Boolean) {
        // TODO process loading state changes
    }
}