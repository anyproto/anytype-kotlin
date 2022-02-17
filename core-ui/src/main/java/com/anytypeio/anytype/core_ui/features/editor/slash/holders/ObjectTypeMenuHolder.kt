package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemListObjectSmallBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class ObjectTypeMenuHolder(
    val binding: ItemListObjectSmallBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashItem.ObjectType) = with(binding) {
        ivIcon.setIcon(
            emoji = item.emoji,
            image = null,
            name = item.name
        )
        tvTitle.text = item.name
        if (item.description.isNullOrBlank()) {
            tvSubtitle.gone()
        } else {
            tvSubtitle.visible()
            tvSubtitle.text = item.description
        }
    }
}