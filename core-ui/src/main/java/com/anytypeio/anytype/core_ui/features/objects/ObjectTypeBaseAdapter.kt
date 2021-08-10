package com.anytypeio.anytype.core_ui.features.objects

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeHolder
import com.anytypeio.anytype.presentation.objects.ObjectTypeView

class ObjectTypeBaseAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<ObjectTypeView.Item, ObjectTypeHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectTypeHolder =
        ObjectTypeHolder(parent).apply {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    onItemClick(getItem(bindingAdapterPosition).id)
            }
        }

    override fun onBindViewHolder(holder: ObjectTypeHolder, position: Int) {
        getItem(position).apply { holder.bind(item = this) }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_object_type_item

    object Differ : DiffUtil.ItemCallback<ObjectTypeView.Item>() {
        override fun areItemsTheSame(
            oldItem: ObjectTypeView.Item,
            newItem: ObjectTypeView.Item
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ObjectTypeView.Item,
            newItem: ObjectTypeView.Item
        ): Boolean = oldItem == newItem
    }
}