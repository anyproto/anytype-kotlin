package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaUploadingBinding
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType

class BookmarkPlaceholder(binding: ItemBlockMediaUploadingBinding) : MediaPlaceholder(binding) {

    override fun placeholderClick(target: String, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Bookmark.Placeholder(target))
    }

    override fun setup() {
        title.text = itemView.resources.getString(R.string.hint_add_a_web_bookmark)
        binding.fileIcon.setImageDrawable(itemView.context.drawable(R.drawable.ic_bookmark_placeholder))
    }
}