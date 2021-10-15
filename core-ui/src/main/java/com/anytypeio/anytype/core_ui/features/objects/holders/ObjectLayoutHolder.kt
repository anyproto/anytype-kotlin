package com.anytypeio.anytype.core_ui.features.objects.holders

import android.view.View
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.extensions.getIconSize24
import com.anytypeio.anytype.core_ui.extensions.getName
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView
import kotlinx.android.synthetic.main.item_layout.view.*

class ObjectLayoutHolder(view: View) : AbstractViewHolder<ObjectLayoutView>(view) {

    override fun bind(item: ObjectLayoutView) {
        val icon = item.getIconSize24()
        val name = item.getName()
        if (icon != null) {
            itemView.ivIcon.setImageResource(icon)
        } else {
            itemView.ivIcon.setImageDrawable(null)
        }
        if (name != null) {
            itemView.tvTitle.setText(name)
        } else {
            itemView.tvTitle.text = null
        }
        if (item.isSelected) {
            itemView.ivChecked.visible()
        } else {
            itemView.ivChecked.invisible()
        }
    }
}