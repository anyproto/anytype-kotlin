package com.anytypeio.anytype.core_ui.features.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultSectionViewHolder
import com.anytypeio.anytype.core_ui.databinding.ItemDefaultListSectionBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeEmptyStateBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeItemBinding
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeHolder
import com.anytypeio.anytype.core_ui.features.objects.holders.ObjectTypeHorizontalHolder
import com.anytypeio.anytype.presentation.objects.ObjectTypeItemView

class ObjectTypeVerticalAdapter(
    private var data: ArrayList<ObjectTypeItemView>,
    private val onItemClick: (Id, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(data: List<ObjectTypeItemView>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_OBJECT_TYPE -> {
                ObjectTypeHolder(
                    binding = ItemObjectTypeItemBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            val item = data[bindingAdapterPosition] as ObjectTypeItemView.Type
                            onItemClick(item.view.id, item.view.name)
                        }
                    }
                }
            }
            VIEW_TYPE_SECTION -> {
                DefaultSectionViewHolder(
                    binding = ItemDefaultListSectionBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            VIEW_TYPE_EMPTY_STATE -> {
                EmptyStateViewHolder(
                    binding = ItemObjectTypeEmptyStateBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val root = holder.itemView
        val item = data[position]
        when (holder) {
            is ObjectTypeHolder -> {
                check(item is ObjectTypeItemView.Type)
                holder.bind(item.view)
            }
            is ObjectTypeHorizontalHolder -> {
                check(item is ObjectTypeItemView.Type)
                holder.bind(item.view)
            }
            is DefaultSectionViewHolder -> {
                when(item) {
                    ObjectTypeItemView.Section.Marketplace -> {
                        holder.bind(
                            root.resources.getString(R.string.library)
                        )
                    }
                    ObjectTypeItemView.Section.Library -> {
                        holder.bind(
                            root.resources.getString(R.string.my_types)
                        )
                    }
                    else -> {}
                }
            }
            is EmptyStateViewHolder -> {
                check(item is ObjectTypeItemView.EmptyState)
                holder.bind(item.query)
            }
        }
    }

    class EmptyStateViewHolder(
        private val binding: ItemObjectTypeEmptyStateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(query: String) {
            binding.tvTitle.text = binding.root.resources.getString(R.string.no_type_named, query)
        }
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is ObjectTypeItemView.Type -> VIEW_TYPE_OBJECT_TYPE
        is ObjectTypeItemView.Section -> VIEW_TYPE_SECTION
        is ObjectTypeItemView.EmptyState -> VIEW_TYPE_EMPTY_STATE
    }

    override fun getItemCount(): Int = data.size

    companion object {
        const val VIEW_TYPE_OBJECT_TYPE = 0
        const val VIEW_TYPE_SECTION = 1
        const val VIEW_TYPE_EMPTY_STATE = 2
    }
}