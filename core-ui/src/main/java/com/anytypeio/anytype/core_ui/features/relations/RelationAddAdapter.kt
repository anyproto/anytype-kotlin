package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.relations.holders.DefaultRelationViewHolder
import com.anytypeio.anytype.presentation.relations.model.RelationView

class RelationAddAdapter(
    val onItemClick: (RelationView.Existing) -> Unit
) : ListAdapter<RelationView.Existing, DefaultRelationViewHolder>(Differ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = DefaultRelationViewHolder(parent).apply {
        itemView.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                onItemClick(getItem(bindingAdapterPosition))
        }
    }

    override fun onBindViewHolder(holder: DefaultRelationViewHolder, position: Int) {
       getItem(position).apply { holder.bind(name = name, format = format) }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_relation_format

    object Differ : DiffUtil.ItemCallback<RelationView.Existing>() {
        override fun areItemsTheSame(
            oldItem: RelationView.Existing,
            newItem: RelationView.Existing
        ): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(
            oldItem: RelationView.Existing,
            newItem: RelationView.Existing
        ): Boolean = oldItem == newItem
    }
}

class RelationAddHeaderAdapter : RecyclerView.Adapter<RelationAddHeaderAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(parent)
    override fun onBindViewHolder(holder: VH, position: Int) {}
    override fun getItemCount(): Int = 1

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_relation_create_from_scratch,
            parent,
            false
        )
    )
}

