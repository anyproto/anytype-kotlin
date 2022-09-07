package com.anytypeio.anytype.core_ui.features.objects.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeHorizontalItemBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeItemBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectTypeView

class ObjectTypeHolder(
    val binding: ItemObjectTypeItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ObjectTypeView.Item) = with(binding) {
        if (item.isSelected) {
            icSelected.visible()
        } else {
            icSelected.gone()
        }
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

class ObjectTypeHorizontalHolder(
    val binding: ItemObjectTypeHorizontalItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ObjectTypeView.Item) = with(binding) {
        icon.setIcon(
            emoji = item.emoji,
            image = null,
            name = item.name
        )
        name.text = item.name
    }
}

class ObjectTypeSearchHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_object_type_search,
        parent,
        false
    )
)