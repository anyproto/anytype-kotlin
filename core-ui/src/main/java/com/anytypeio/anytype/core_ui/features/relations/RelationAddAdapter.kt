package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.relations.holders.DefaultRelationFormatViewHolder
import com.anytypeio.anytype.core_ui.features.relations.holders.DefaultRelationViewHolder
import com.anytypeio.anytype.presentation.relations.model.RelationView
import kotlinx.android.synthetic.main.item_relation_create_from_scratch_name_input.view.*

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

class RelationFormatAdapter(
    val onItemClick: (RelationView.CreateFromScratch) -> Unit
) : ListAdapter<RelationView.CreateFromScratch, DefaultRelationFormatViewHolder>(Differ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = DefaultRelationFormatViewHolder(parent).apply {
        itemView.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                onItemClick(getItem(bindingAdapterPosition))
        }
    }

    override fun onBindViewHolder(holder: DefaultRelationFormatViewHolder, position: Int) {
        getItem(position).apply {
            holder.bind(
                name = format.prettyName,
                format = format,
                isSelected = isSelected
            )
        }
    }

    override fun onBindViewHolder(
        holder: DefaultRelationFormatViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            payloads.forEach { payload ->
                if (payload == Differ.SELECTION_CHANGED) {
                    holder.setIsSelected(getItem(position).isSelected)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_relation_format_create_from_scratch

    object Differ : DiffUtil.ItemCallback<RelationView.CreateFromScratch>() {
        override fun areItemsTheSame(
            oldItem: RelationView.CreateFromScratch,
            newItem: RelationView.CreateFromScratch
        ): Boolean = oldItem.format == newItem.format

        override fun areContentsTheSame(
            oldItem: RelationView.CreateFromScratch,
            newItem: RelationView.CreateFromScratch
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: RelationView.CreateFromScratch,
            newItem: RelationView.CreateFromScratch
        ): Any? {
            return if (newItem.isSelected != oldItem.isSelected)
                SELECTION_CHANGED
            else
                super.getChangePayload(oldItem, newItem)
        }


        const val SELECTION_CHANGED = 1
    }
}

class RelationNameInputAdapter(
    val onTextInputChanged: (String) -> Unit
) : RecyclerView.Adapter<RelationNameInputAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(parent).apply {
            itemView.textInputField.doAfterTextChanged { onTextInputChanged(it.toString()) }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}

    override fun getItemCount(): Int = 1
    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_relation_create_from_scratch_name_input,
            parent,
            false
        )
    ) {
    }
}

class RelationAddHeaderAdapter(
    val onItemClick: () -> Unit
) : RecyclerView.Adapter<RelationAddHeaderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent).apply {
        itemView.setOnClickListener { onItemClick() }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    override fun getItemCount(): Int = 1

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_relation_create_from_scratch,
            parent,
            false
        )
    )
}

