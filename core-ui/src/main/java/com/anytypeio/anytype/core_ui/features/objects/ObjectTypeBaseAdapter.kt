package com.anytypeio.anytype.core_ui.features.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeItemBinding
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeHolder
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeHorizontalHolder
import com.anytypeio.anytype.presentation.objects.ObjectTypeView

class ObjectTypeVerticalAdapter(
    private var data: ArrayList<ObjectTypeView>,
    private val onItemClick: (Id, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(data: List<ObjectTypeView>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder = ObjectTypeHolder(
            binding = ItemObjectTypeItemBinding.inflate(
                inflater, parent, false
            )
        ).apply {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val item = data[bindingAdapterPosition] as ObjectTypeView
                    onItemClick(item.id, item.name)
                }
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ObjectTypeHolder -> {
                holder.bind(data[position] as ObjectTypeView)
            }
            is ObjectTypeHorizontalHolder -> {
                holder.bind(data[position] as ObjectTypeView)
            }
        }
    }

    override fun getItemCount(): Int = data.size
}