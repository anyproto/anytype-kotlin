package com.anytypeio.anytype.core_ui.features.objects

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectLayoutHolder
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView

class ObjectLayoutAdapter(
    private val onItemClick: (Int) -> Unit
) : AbstractAdapter<ObjectLayoutView>(emptyList()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ObjectLayoutHolder = ObjectLayoutHolder(
        view = inflate(parent, R.layout.item_layout)
    ).apply {
        itemView.setOnClickListener {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(items[bindingAdapterPosition].id)
            }
        }
    }
}