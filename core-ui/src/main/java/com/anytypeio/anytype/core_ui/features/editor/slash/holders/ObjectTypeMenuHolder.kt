package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_list_object_small.view.*

class ObjectTypeMenuHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: SlashItem.ObjectType) = with(itemView) {
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