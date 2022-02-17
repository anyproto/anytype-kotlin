package com.anytypeio.anytype.core_ui.features.objects.holders

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.databinding.ItemLayoutBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView

class ObjectLayoutHolder(
    val binding: ItemLayoutBinding
) : AbstractViewHolder<ObjectLayoutView>(binding.root) {

    override fun bind(item: ObjectLayoutView) = with(binding) {
        when (item) {
            is ObjectLayoutView.Basic -> {
                ivIcon.setImageResource(R.drawable.ic_layout_basic)
                tvTitle.setText(R.string.layout_basic_name)
                tvSubtitle.setText(R.string.layout_basic_description)
            }
            is ObjectLayoutView.Note -> {
                ivIcon.setImageResource(R.drawable.ic_layout_note)
                tvTitle.setText(R.string.layout_note_name)
                tvSubtitle.setText(R.string.layout_note_description)
            }
            is ObjectLayoutView.Profile -> {
                ivIcon.setImageResource(R.drawable.ic_layout_profile)
                tvTitle.setText(R.string.layout_profile_name)
                tvSubtitle.setText(R.string.layout_profile_description)
            }
            is ObjectLayoutView.Todo -> {
                ivIcon.setImageResource(R.drawable.ic_layout_task)
                tvTitle.setText(R.string.layout_todo_name)
                tvSubtitle.setText(R.string.layout_todo_description)
            }
            else -> {
                // TODO
            }
        }

        if (item.isSelected) {
            ivChecked.visible()
        } else {
            ivChecked.invisible()
        }
    }
}