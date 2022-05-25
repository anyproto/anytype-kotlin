package com.anytypeio.anytype.core_ui.features.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.databinding.ItemLayoutBinding
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectLayoutHolder
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView

class ObjectLayoutAdapter(
    private val onItemClick: (ObjectLayoutView) -> Unit
) : AbstractAdapter<ObjectLayoutView>(emptyList()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ObjectLayoutHolder = ObjectLayoutHolder(
        binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
        itemView.setOnClickListener {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(items[bindingAdapterPosition])
            }
        }
    }
}