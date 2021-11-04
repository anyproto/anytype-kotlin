package com.anytypeio.anytype.core_ui.features.objects.holders

import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.extensions.getIconSize24
import com.anytypeio.anytype.core_ui.extensions.getName
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView
import kotlinx.android.synthetic.main.item_layout.view.*

class ObjectLayoutHolder(view: View) : AbstractViewHolder<ObjectLayoutView>(view) {

    override fun bind(item: ObjectLayoutView) {
        when(item) {
            is ObjectLayoutView.Basic -> {
                itemView.ivIcon.setImageResource(R.drawable.ic_layout_basic)
                itemView.tvTitle.setText(R.string.layout_basic_name)
                itemView.tvSubtitle.setText(R.string.layout_basic_description)
            }
            is ObjectLayoutView.Note -> {
                itemView.ivIcon.setImageResource(R.drawable.ic_layout_note)
                itemView.tvTitle.setText(R.string.layout_note_name)
                itemView.tvSubtitle.setText(R.string.layout_note_description)
            }
            is ObjectLayoutView.Profile -> {
                itemView.ivIcon.setImageResource(R.drawable.ic_layout_profile)
                itemView.tvTitle.setText(R.string.layout_profile_name)
                itemView.tvSubtitle.setText(R.string.layout_profile_description)
            }
            is ObjectLayoutView.Todo -> {
                itemView.ivIcon.setImageResource(R.drawable.ic_layout_task)
                itemView.tvTitle.setText(R.string.layout_todo_name)
                itemView.tvSubtitle.setText(R.string.layout_todo_description)
            }
            else -> {
                // TODO
            }
        }

        if (item.isSelected) {
            itemView.ivChecked.visible()
        } else {
            itemView.ivChecked.invisible()
        }
    }
}