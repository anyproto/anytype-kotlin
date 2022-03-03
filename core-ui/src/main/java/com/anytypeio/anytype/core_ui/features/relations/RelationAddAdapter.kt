package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.*
import com.anytypeio.anytype.core_ui.features.relations.holders.DefaultRelationFormatViewHolder
import com.anytypeio.anytype.core_ui.features.relations.holders.DefaultRelationViewHolder
import com.anytypeio.anytype.presentation.relations.model.LimitObjectTypeValueView
import com.anytypeio.anytype.presentation.relations.model.RelationView

class RelationAddAdapter(
    val onItemClick: (RelationView.Existing) -> Unit
) : ListAdapter<RelationView.Existing, DefaultRelationViewHolder>(Differ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = DefaultRelationViewHolder(
        binding = ItemRelationFormatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
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
    ) = DefaultRelationFormatViewHolder(
        binding = ItemRelationFormatCreateFromScratchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
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

    override fun getItemViewType(position: Int): Int =
        R.layout.item_relation_format_create_from_scratch

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

    var query: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            binding = ItemRelationCreateFromScratchNameInputBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            binding.textInputField.doAfterTextChanged {
                query = it.toString()
                onTextInputChanged(it.toString())
            }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(query)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(query)
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(
        val binding: ItemRelationCreateFromScratchNameInputBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(query: String) {
            binding.textInputField.setText(query)
        }
    }
}

class RelationAddHeaderAdapter(
    val onItemClick: () -> Unit
) : RecyclerView.Adapter<RelationAddHeaderAdapter.ViewHolder>() {

    var query: String = EMPTY_QUERY
        set(value) {
            field = value
            notifyItemChanged(0, query)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            binding = ItemRelationCreateFromScratchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            itemView.setOnClickListener { onItemClick() }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(query)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(query)
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(
        val binding: ItemRelationCreateFromScratchBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(query: String) = with(binding) {
            if (query.isEmpty()) {
                tvCreateFromScratch.setText(R.string.create_from_scratch)
            } else {
                tvCreateFromScratch.text = itemView.resources.getString(
                    R.string.create_relation_with_name, query
                )
            }
        }
    }

    companion object {
        const val EMPTY_QUERY = ""
    }
}

class RelationConnectWithAdapter(
    private val onClick: () -> Unit
) : RecyclerView.Adapter<RelationConnectWithAdapter.ViewHolder>() {

    var format: RelationFormat = RelationFormat.LONG_TEXT
        set(value) {
            field = value
            notifyItemChanged(0, format)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemRelationCreateFromScratchConnectWithBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
        itemView.setOnClickListener { onClick() }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(format)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(format)
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(
        val binding: ItemRelationCreateFromScratchConnectWithBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(format: RelationFormat) {
            binding.ivRelationFormat.bind(format)
            binding.tvRelationName.text = format.prettyName
        }
    }
}

class LimitObjectTypeAdapter(
    private val onClick: () -> Unit
) : RecyclerView.Adapter<LimitObjectTypeAdapter.ViewHolder>() {

    var limitObjectTypeView : LimitObjectTypeValueView? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemRelationCreateFromScratchLimitObjectTypesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply { itemView.setOnClickListener { onClick() } }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = limitObjectTypeView
        if (item != null) {
            if (item.types.isNotEmpty()) {
                holder.binding.limitObjectTypes.text = item.types.joinToString { it.title }
            } else {
                holder.binding.limitObjectTypes.text = null
            }
        }
    }

    override fun getItemCount(): Int = if (limitObjectTypeView != null) 1 else 0

    class ViewHolder(
        val binding: ItemRelationCreateFromScratchLimitObjectTypesBinding
    ) : RecyclerView.ViewHolder(binding.root)
}

