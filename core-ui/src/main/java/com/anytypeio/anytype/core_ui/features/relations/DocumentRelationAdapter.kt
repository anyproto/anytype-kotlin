package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationCheckboxBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationTagBinding
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.ListRelationViewHolder
import com.anytypeio.anytype.core_utils.diff.DefaultDiffUtil
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import timber.log.Timber

class DocumentRelationAdapter(
    private var items: List<RelationListViewModel.Model>,
    private val onRelationClicked: (RelationListViewModel.Model.Item) -> Unit,
    private val onCheckboxClicked: (RelationListViewModel.Model.Item) -> Unit,
    private val onDeleteClicked: (RelationListViewModel.Model.Item) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_relation_list_relation_default -> {
                val binding =
                    ItemRelationListRelationDefaultBinding.inflate(inflater, parent, false)
                ListRelationViewHolder.Default(binding = binding).apply {
                    binding.featuredRelationCheckbox.visible()
                    itemView.setOnClickListener {
                        relationClicked(bindingAdapterPosition)
                    }
                    binding.featuredRelationCheckbox.setOnClickListener {
                        checkboxClicked(bindingAdapterPosition)
                    }
                    binding.ivActionDelete.setOnClickListener {
                        deleteClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_checkbox -> {
                val binding =
                    ItemRelationListRelationCheckboxBinding.inflate(inflater, parent, false)
                ListRelationViewHolder.Checkbox(binding = binding).apply {
                    binding.featuredRelationCheckbox.visible()
                    itemView.setOnClickListener {
                        relationClicked(bindingAdapterPosition)
                    }
                    binding.featuredRelationCheckbox.setOnClickListener {
                        checkboxClicked(bindingAdapterPosition)
                    }
                    binding.ivActionDelete.setOnClickListener {
                        deleteClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_object -> {
                val binding = ItemRelationListRelationObjectBinding.inflate(
                    inflater, parent, false
                )
                ListRelationViewHolder.Object(binding = binding).apply {
                    binding.featuredRelationCheckbox.visible()
                    itemView.setOnClickListener {
                        relationClicked(bindingAdapterPosition)
                    }
                    binding.featuredRelationCheckbox.setOnClickListener {
                        checkboxClicked(bindingAdapterPosition)
                    }
                    binding.ivActionDelete.setOnClickListener {
                        deleteClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_status -> {
                val binding = ItemRelationListRelationStatusBinding.inflate(inflater, parent, false)
                ListRelationViewHolder.Status(binding = binding).apply {
                    binding.featuredRelationCheckbox.visible()
                    itemView.setOnClickListener {
                        relationClicked(bindingAdapterPosition)
                    }
                    binding.featuredRelationCheckbox.setOnClickListener {
                        checkboxClicked(bindingAdapterPosition)
                    }
                    binding.ivActionDelete.setOnClickListener {
                        deleteClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_tag -> {
                val binding = ItemRelationListRelationTagBinding.inflate(
                    inflater, parent, false
                )
                ListRelationViewHolder.Tags(binding = binding).apply {
                    binding.featuredRelationCheckbox.visible()
                    itemView.setOnClickListener {
                        relationClicked(bindingAdapterPosition)
                    }
                    binding.featuredRelationCheckbox.setOnClickListener {
                        checkboxClicked(bindingAdapterPosition)
                    }
                    binding.ivActionDelete.setOnClickListener {
                        deleteClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_file -> {
                val binding = ItemRelationListRelationFileBinding.inflate(
                    inflater, parent, false
                )
                ListRelationViewHolder.File(binding = binding).apply {
                    binding.featuredRelationCheckbox.visible()
                    itemView.setOnClickListener {
                        relationClicked(bindingAdapterPosition)
                    }
                    binding.featuredRelationCheckbox.setOnClickListener {
                        checkboxClicked(bindingAdapterPosition)
                    }
                    binding.ivActionDelete.setOnClickListener {
                        deleteClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_section -> {
                SectionViewHolder(view = inflater.inflate(viewType, parent, false))
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    private fun relationClicked(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val view = items[position]
            check(view is RelationListViewModel.Model.Item)
            onRelationClicked(view)
        }
    }

    private fun checkboxClicked(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val view = items[position]
            check(view is RelationListViewModel.Model.Item)
            onCheckboxClicked(view)
        }
    }

    private fun deleteClicked(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val view = items[position]
            check(view is RelationListViewModel.Model.Item)
            onDeleteClicked(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (holder is ListRelationViewHolder) {
                payloads.forEach { payload ->
                    if (payload is GranularChange) {
                        if (payload.isModeChanged) {
                            val item = items[position]
                            check(item is RelationListViewModel.Model.Item)
                            holder.setIsRemovable(item.isRemovable)
                        } else {
                            super.onBindViewHolder(holder, position, payloads)
                        }
                    }
                }
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ListRelationViewHolder.Status -> {
                check(item is RelationListViewModel.Model.Item)
                val view = item.view
                check(view is ObjectRelationView.Status)
                holder.bind(view)
            }
            is ListRelationViewHolder.Checkbox -> {
                check(item is RelationListViewModel.Model.Item)
                val view = item.view
                check(view is ObjectRelationView.Checkbox)
                holder.bind(view)
            }
            is ListRelationViewHolder.Tags -> {
                check(item is RelationListViewModel.Model.Item)
                val view = item.view
                check(view is ObjectRelationView.Tags)
                holder.bind(view)
            }
            is ListRelationViewHolder.Object -> {
                check(item is RelationListViewModel.Model.Item)
                val view = item.view
                check(view is ObjectRelationView.Object)
                holder.bind(view)
            }
            is ListRelationViewHolder.File -> {
                check(item is RelationListViewModel.Model.Item)
                val view = item.view
                check(view is ObjectRelationView.File)
                holder.bind(view)
            }
            is ListRelationViewHolder.Default -> {
                check(item is RelationListViewModel.Model.Item)
                val view = item.view
                check(view is ObjectRelationView.Default)
                holder.bind(view)
            }
            is SectionViewHolder -> {
                check(item is RelationListViewModel.Model.Section)
                holder.bind(item)
            }
            else -> {
                Timber.d("Skipping binding for: $holder")
            }
        }
        if (holder is ListRelationViewHolder) {
            check(item is RelationListViewModel.Model.Item)
            holder.setIsFeatured(item.view.featured)
            holder.setIsRemovable(item.isRemovable)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is RelationListViewModel.Model.Item -> {
            when (item.view) {
                is ObjectRelationView.Checkbox -> R.layout.item_relation_list_relation_checkbox
                is ObjectRelationView.Object -> R.layout.item_relation_list_relation_object
                is ObjectRelationView.Status -> R.layout.item_relation_list_relation_status
                is ObjectRelationView.Tags -> R.layout.item_relation_list_relation_tag
                is ObjectRelationView.File -> R.layout.item_relation_list_relation_file
                else -> R.layout.item_relation_list_relation_default
            }
        }
        RelationListViewModel.Model.Section.Featured -> R.layout.item_relation_list_section
        RelationListViewModel.Model.Section.Other -> R.layout.item_relation_list_section
        is RelationListViewModel.Model.Section.TypeFrom -> R.layout.item_relation_list_section
        else -> throw IllegalStateException("Unexpected item type: $item")
    }

    fun update(update: List<RelationListViewModel.Model>) {
        val differ = Differ(old = items, new = update)
        val result = DiffUtil.calculateDiff(differ, false)
        items = update
        result.dispatchUpdatesTo(this)
    }

    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(section: RelationListViewModel.Model.Section) {
            when (section) {
                RelationListViewModel.Model.Section.Featured -> {
                    itemView.findViewById<TextView>(R.id.tvSectionName)
                        .setText(R.string.featured_relations)
                }
                RelationListViewModel.Model.Section.Other -> {
                    itemView.findViewById<TextView>(R.id.tvSectionName)
                        .setText(R.string.other_relations)
                }
                is RelationListViewModel.Model.Section.TypeFrom -> {
                    val text = itemView.resources.getString(R.string.from_type, section.typeName)
                    itemView.findViewById<TextView>(R.id.tvSectionName).text = text
                }
                else -> throw IllegalStateException("Unexpected item type: $section")
            }
        }
    }

    class Differ(
        private val old: List<RelationListViewModel.Model>,
        private val new: List<RelationListViewModel.Model>
    ) : DefaultDiffUtil<RelationListViewModel.Model>(old = old, new = new) {
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]
            return if (oldItem is RelationListViewModel.Model.Item && newItem is RelationListViewModel.Model.Item) {
                if (newItem.isRemovable != oldItem.isRemovable)
                    GranularChange(isModeChanged = true)
                else
                    null
            } else
                null
        }
    }

    data class GranularChange(val isModeChanged: Boolean = false)
}