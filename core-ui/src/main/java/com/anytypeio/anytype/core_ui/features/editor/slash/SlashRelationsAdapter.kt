package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationCheckboxBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationTagBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderBinding
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.ListRelationViewHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.RelationNewHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.RelationsSubheaderMenuHolder
import com.anytypeio.anytype.core_utils.diff.DefaultDiffUtil
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import timber.log.Timber

class SlashRelationsAdapter(
    private var items: List<SlashRelationView>,
    private val clicks: (SlashItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val defaultPadding = parent.context.dimen(R.dimen.dp_20).toInt()
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_relation_list_relation_default -> {
                val binding =
                    ItemRelationListRelationDefaultBinding.inflate(inflater, parent, false)
                ListRelationViewHolder.Default(binding).apply {
                    updateHeight(binding.root)
                    updatePadding(binding.root, defaultPadding)
                    binding.divider.visible()
                    itemView.setOnClickListener {
                        onItemClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_checkbox -> {
                val binding =
                    ItemRelationListRelationCheckboxBinding.inflate(inflater, parent, false)
                ListRelationViewHolder.Checkbox(binding)
                    .apply {
                        updateHeight(binding.root)
                        updatePadding(binding.root, defaultPadding)
                        binding.divider.visible()
                        itemView.setOnClickListener {
                            onItemClicked(bindingAdapterPosition)
                        }
                    }
            }
            R.layout.item_relation_list_relation_object -> {
                val binding = ItemRelationListRelationObjectBinding.inflate(
                    inflater, parent, false
                )
                ListRelationViewHolder.Object(binding).apply {
                    updateHeight(binding.root)
                    updatePadding(binding.root, defaultPadding)
                    binding.divider.visible()
                    itemView.setOnClickListener {
                        onItemClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_status -> {
                val binding = ItemRelationListRelationStatusBinding.inflate(inflater, parent, false)
                ListRelationViewHolder.Status(binding).apply {
                    updateHeight(binding.root)
                    updatePadding(binding.root, defaultPadding)
                    binding.divider.visible()
                    itemView.setOnClickListener {
                        onItemClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_tag -> {
                val binding = ItemRelationListRelationTagBinding.inflate(
                    inflater, parent, false
                )
                ListRelationViewHolder.Tags(binding).apply {
                    updateHeight(binding.root)
                    updatePadding(binding.root, defaultPadding)
                    binding.divider.visible()
                    itemView.setOnClickListener {
                        onItemClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_relation_list_relation_file -> {
                val binding = ItemRelationListRelationFileBinding.inflate(
                    inflater, parent, false
                )
                ListRelationViewHolder.File(binding).apply {
                    updateHeight(binding.root)
                    updatePadding(binding.root, defaultPadding)
                    binding.divider.visible()
                    itemView.setOnClickListener {
                        onItemClicked(bindingAdapterPosition)
                    }
                }
            }
            R.layout.item_slash_widget_subheader -> {
                RelationsSubheaderMenuHolder(
                    binding = ItemSlashWidgetSubheaderBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.findViewById<View>(R.id.flBack).setOnClickListener {
                        clicks(SlashItem.Back)
                    }
                }
            }
            R.layout.item_relation_add_new -> {
                RelationNewHolder(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    updatePadding(this.itemView, defaultPadding)
                    itemView.setOnClickListener {
                        clicks(SlashItem.RelationNew)
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    private fun onItemClicked(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val view = items[position]
            check(view is SlashRelationView.Item)
            clicks(SlashItem.Relation(view))
        }
    }

    private fun updateHeight(root: View) {
        root.updateLayoutParams {
            height =
                root.resources.getDimensionPixelSize(R.dimen.object_slash_menu_relations_item_height)
        }
    }

    private fun updatePadding(root: View, padding: Int) {
        root.updatePadding(
            left = padding,
            right = padding
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ListRelationViewHolder.Status -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is ObjectRelationView.Status)
                holder.bind(view)
            }
            is ListRelationViewHolder.Checkbox -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is ObjectRelationView.Checkbox)
                holder.bind(view)
            }
            is ListRelationViewHolder.Tags -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is ObjectRelationView.Tags)
                holder.bind(view)
            }
            is ListRelationViewHolder.Object -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is ObjectRelationView.Object)
                holder.bind(view)
            }
            is ListRelationViewHolder.File -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is ObjectRelationView.File)
                holder.bind(view)
            }
            is ListRelationViewHolder.Default -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is ObjectRelationView.Default)
                holder.bind(view)
            }
            is RelationsSubheaderMenuHolder -> {
                check(item is SlashRelationView.Section)
                holder.bind(item)
            }
            else -> {
                Timber.d("Skipping binding for: $holder")
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is SlashRelationView.Item -> {
            when (item.view) {
                is ObjectRelationView.Checkbox -> R.layout.item_relation_list_relation_checkbox
                is ObjectRelationView.Object -> R.layout.item_relation_list_relation_object
                is ObjectRelationView.Status -> R.layout.item_relation_list_relation_status
                is ObjectRelationView.Tags -> R.layout.item_relation_list_relation_tag
                is ObjectRelationView.File -> R.layout.item_relation_list_relation_file
                else -> R.layout.item_relation_list_relation_default
            }
        }
        is SlashRelationView.Section.SubheaderWithBack -> R.layout.item_slash_widget_subheader
        SlashRelationView.Section.Subheader -> R.layout.item_slash_widget_subheader
        SlashRelationView.RelationNew -> R.layout.item_relation_add_new
    }

    fun update(update: List<SlashRelationView>) {
        Timber.d("Updating adapter: $update")
        val differ = DefaultDiffUtil(old = items, new = update)
        val result = DiffUtil.calculateDiff(differ, false)
        items = update
        result.dispatchUpdatesTo(this)
    }

    fun clear() {
        val size = items.size
        if (size > 0) {
            items = listOf()
            notifyItemRangeRemoved(0, size)
        }
    }
}