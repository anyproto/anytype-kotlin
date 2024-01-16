package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaPlaceholderBinding
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType

class PicturePlaceholder(binding: ItemBlockMediaPlaceholderBinding) : MediaPlaceholder(binding) {

    override fun placeholderClick(target: String, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Picture.Placeholder(target))
    }

    override fun setup() {
        title.text = itemView.resources.getString(R.string.hint_upload_image)
        binding.fileIcon.setImageDrawable(itemView.context.drawable(R.drawable.ic_picture))
    }
}