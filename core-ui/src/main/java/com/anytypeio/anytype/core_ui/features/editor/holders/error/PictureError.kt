package com.anytypeio.anytype.core_ui.features.editor.holders.error

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaErrorBinding
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class PictureError(
    binding: ItemBlockMediaErrorBinding
) : MediaError(binding) {

    override fun errorClick(item: BlockView.Error, clicked: (ListenerType) -> Unit) {
        if (item is BlockView.Error.Picture) {
            clicked(ListenerType.Picture.Error(item.id))
        }
    }

    override fun bind(item: BlockView.Error, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        if (item is BlockView.Error.Picture) {
            binding.fileName.text = if (item.name.isNullOrBlank()) {
                itemView.resources.getString(R.string.hint_upload_image)
            } else
                item.name
        }
        binding.fileIcon.setImageDrawable(itemView.context.drawable(R.drawable.ic_picture_inactive))
    }
}