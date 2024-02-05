package com.anytypeio.anytype.core_ui.features.editor.holders.upload

import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaPlaceholderBinding
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class BookmarkUpload(
    binding: ItemBlockMediaPlaceholderBinding
) : MediaUpload(binding), DecoratableCardViewHolder {

    override fun uploadClick(target: String, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Bookmark.Upload(target))
    }

    fun setUrl(url: Url?) {
        binding.fileName.text = url
    }

    override fun bind(item: BlockView.Upload, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        binding.fileIcon.setImageDrawable(itemView.context.drawable(R.drawable.ic_bookmark_inactive))
    }
}