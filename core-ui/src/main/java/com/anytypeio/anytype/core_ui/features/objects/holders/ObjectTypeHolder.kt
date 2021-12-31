package com.anytypeio.anytype.core_ui.features.objects.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import kotlinx.android.synthetic.main.item_object_type_horizontal_item.view.*
import kotlinx.android.synthetic.main.item_object_type_horizontal_item.view.name
import kotlinx.android.synthetic.main.item_object_type_item.view.*


class ObjectTypeHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_object_type_item,
        parent,
        false
    )
) {

    fun bind(item: ObjectTypeView.Item) = with(itemView) {
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

class ObjectTypeHorizontalHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_object_type_horizontal_item,
        parent,
        false
    )
) {

    fun bind(item: ObjectTypeView.Item) = with(itemView) {
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