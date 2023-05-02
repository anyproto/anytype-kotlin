package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemListObjectSmallBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetObjectTypeBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class ObjectTypeMenuHolder(
    val binding: ItemSlashWidgetObjectTypeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashItem.ObjectType) = with(binding) {
        val objectType = item.objectTypeView
        ivIcon.setIcon(
            emoji = objectType.emoji,
            image = null,
            name = objectType.name
        )
        tvTitle.text = objectType.name
        if (objectType.description.isNullOrBlank()) {
            tvSubtitle.gone()
        } else {
            tvSubtitle.visible()
            tvSubtitle.text = objectType.description
        }
    }
}