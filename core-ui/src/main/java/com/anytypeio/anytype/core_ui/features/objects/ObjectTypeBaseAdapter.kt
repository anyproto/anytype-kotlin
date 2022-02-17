package com.anytypeio.anytype.core_ui.features.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeHorizontalItemBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeItemBinding
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeHolder
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeHorizontalHolder
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeSearchHolder
import com.anytypeio.anytype.presentation.objects.ObjectTypeView

abstract class ObjectTypeBaseAdapter(
    private var data: ArrayList<ObjectTypeView>,
    private val onItemClick: (Id, String) -> Unit,
    private val onSearchClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(data: List<ObjectTypeView>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            R.layout.item_object_type_item -> {
                ObjectTypeHolder(
                    binding = ItemObjectTypeItemBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            val item = data[bindingAdapterPosition] as ObjectTypeView.Item
                            onItemClick(item.id, item.name)
                        }
                    }
                }
            }
            R.layout.item_object_type_horizontal_item -> {
                ObjectTypeHorizontalHolder(
                    binding = ItemObjectTypeHorizontalItemBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            val item = data[bindingAdapterPosition] as ObjectTypeView.Item
                            onItemClick(item.id, item.name)
                        }
                    }
                }
            }
            R.layout.item_object_type_search -> {
                ObjectTypeSearchHolder(parent).apply {
                    itemView.setOnClickListener {
                        onSearchClick?.invoke()
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
        return viewHolder
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ObjectTypeHolder -> {
                holder.bind(data[position] as ObjectTypeView.Item)
            }
            is ObjectTypeHorizontalHolder -> {
                holder.bind(data[position] as ObjectTypeView.Item)
            }
        }
    }

    override fun getItemCount(): Int = data.size
}

class ObjectTypeVerticalAdapter(
    data: ArrayList<ObjectTypeView>,
    onItemClick: (Id, String) -> Unit
) : ObjectTypeBaseAdapter(data, onItemClick) {

    override fun getItemViewType(position: Int): Int = R.layout.item_object_type_item
}


class ObjectTypeHorizontalListAdapter(
    private var data: ArrayList<ObjectTypeView>,
    onItemClick: (Id, String) -> Unit,
    onSearchClick: () -> Unit
) : ObjectTypeBaseAdapter(data, onItemClick, onSearchClick) {

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is ObjectTypeView.Item -> R.layout.item_object_type_horizontal_item
            ObjectTypeView.Search -> R.layout.item_object_type_search
            else -> throw IllegalStateException("Unexpected ObjectTypeView!")
        }
    }
}