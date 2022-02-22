package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationTagBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderBinding
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.RelationViewHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.RelationNewHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.RelationsSubheaderMenuHolder
import com.anytypeio.anytype.core_utils.diff.DefaultDiffUtil
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import timber.log.Timber

class SlashRelationsAdapter(
    private var items: List<SlashRelationView>,
    private val clicks: (SlashItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_relation_list_relation_default -> {
                RelationViewHolder.Default(view = inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val view = items[bindingAdapterPosition]
                        check(view is SlashRelationView.Item)
                        clicks(SlashItem.Relation(view))
                    }
                    itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
                        gone()
                    }
                    itemView.findViewById<View>(R.id.divider).apply {
                        visible()
                    }
                }
            }
            R.layout.item_relation_list_relation_checkbox -> {
                RelationViewHolder.Checkbox(view = inflater.inflate(viewType, parent, false))
                    .apply {
                        itemView.setOnClickListener {
                            val view = items[bindingAdapterPosition]
                            check(view is SlashRelationView.Item)
                            clicks(SlashItem.Relation(view))
                        }
                        itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
                            gone()
                        }
                        itemView.findViewById<View>(R.id.divider).apply {
                            visible()
                        }
                    }
            }
            R.layout.item_relation_list_relation_object -> {
                RelationViewHolder.Object(
                    binding = ItemRelationListRelationObjectBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val view = items[bindingAdapterPosition]
                        check(view is SlashRelationView.Item)
                        clicks(SlashItem.Relation(view))
                    }
                    itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
                        gone()
                    }
                    itemView.findViewById<View>(R.id.divider).apply {
                        visible()
                    }
                }
            }
            R.layout.item_relation_list_relation_status -> {
                RelationViewHolder.Status(view = inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val view = items[bindingAdapterPosition]
                        check(view is SlashRelationView.Item)
                        clicks(SlashItem.Relation(view))
                    }
                    itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
                        gone()
                    }
                    itemView.findViewById<View>(R.id.divider).apply {
                        visible()
                    }
                }
            }
            R.layout.item_relation_list_relation_tag -> {
                RelationViewHolder.Tags(
                    binding = ItemRelationListRelationTagBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val view = items[bindingAdapterPosition]
                        check(view is SlashRelationView.Item)
                        clicks(SlashItem.Relation(view))
                    }
                    itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
                        gone()
                    }
                    itemView.findViewById<View>(R.id.divider).apply {
                        visible()
                    }
                }
            }
            R.layout.item_relation_list_relation_file -> {
                RelationViewHolder.File(
                    ItemRelationListRelationFileBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val view = items[bindingAdapterPosition]
                        check(view is SlashRelationView.Item)
                        clicks(SlashItem.Relation(view))
                    }
                    itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
                        gone()
                    }
                    itemView.findViewById<View>(R.id.divider).apply {
                        visible()
                    }
                }
            }
            R.layout.item_slash_widget_subheader -> {
                RelationsSubheaderMenuHolder(
                    binding = ItemSlashWidgetSubheaderBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.findViewById<FrameLayout>(R.id.flBack).setOnClickListener {
                        clicks(SlashItem.Back)
                    }
                }
            }
            R.layout.item_relation_add_new -> {
                RelationNewHolder(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        clicks(SlashItem.RelationNew)
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is RelationViewHolder.Status -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is DocumentRelationView.Status)
                holder.bind(view)
            }
            is RelationViewHolder.Checkbox -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is DocumentRelationView.Checkbox)
                holder.bind(view)
            }
            is RelationViewHolder.Tags -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is DocumentRelationView.Tags)
                holder.bind(view)
            }
            is RelationViewHolder.Object -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is DocumentRelationView.Object)
                holder.bind(view)
            }
            is RelationViewHolder.File -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is DocumentRelationView.File)
                holder.bind(view)
            }
            is RelationViewHolder.Default -> {
                check(item is SlashRelationView.Item)
                val view = item.view
                check(view is DocumentRelationView.Default)
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
                is DocumentRelationView.Checkbox -> R.layout.item_relation_list_relation_checkbox
                is DocumentRelationView.Object -> R.layout.item_relation_list_relation_object
                is DocumentRelationView.Status -> R.layout.item_relation_list_relation_status
                is DocumentRelationView.Tags -> R.layout.item_relation_list_relation_tag
                is DocumentRelationView.File -> R.layout.item_relation_list_relation_file
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