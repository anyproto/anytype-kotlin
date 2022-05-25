package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.*
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_utils.ext.formatTimestamp
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.DateDescription
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView

class CreateFilterAdapter(
    private val onItemClicked: (CreateFilterView) -> Unit
) : RecyclerView.Adapter<CreateFilterAdapter.ViewHolder>() {

    private var views: List<CreateFilterView> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_create_filter_tag -> {
                ViewHolder.Tag(
                    binding = ItemCreateFilterTagBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onItemClicked(views[pos])
                        }
                    }
                }
            }
            R.layout.item_create_filter_date -> {
                ViewHolder.Date(
                    binding = ItemCreateFilterDateBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onItemClicked(views[pos])
                        }
                    }
                }
            }
            R.layout.item_create_filter_status -> {
                ViewHolder.Status(
                    binding = ItemCreateFilterStatusBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onItemClicked(views[pos])
                        }
                    }
                }
            }
            R.layout.item_create_filter_object -> {
                ViewHolder.Object(
                    binding = ItemCreateFilterObjectBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onItemClicked(views[pos])
                        }
                    }
                }
            }
            R.layout.item_create_filter_checkbox -> {
                ViewHolder.Checkbox(
                    binding = ItemCreateFilterCheckboxBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onItemClicked(views[pos])
                        }
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) = when (holder) {
        is ViewHolder.Tag -> holder.bind(views[position] as CreateFilterView.Tag)
        is ViewHolder.Status -> holder.bind(views[position] as CreateFilterView.Status)
        is ViewHolder.Date -> holder.bind(views[position] as CreateFilterView.Date)
        is ViewHolder.Object -> holder.bind(views[position] as CreateFilterView.Object)
        is ViewHolder.Checkbox -> holder.bind(views[position] as CreateFilterView.Checkbox)
    }

    override fun getItemCount(): Int = views.size

    override fun getItemViewType(position: Int): Int = when (views[position]) {
        is CreateFilterView.Tag -> R.layout.item_create_filter_tag
        is CreateFilterView.Status -> R.layout.item_create_filter_status
        is CreateFilterView.Date -> R.layout.item_create_filter_date
        is CreateFilterView.Object -> R.layout.item_create_filter_object
        is CreateFilterView.Checkbox -> R.layout.item_create_filter_checkbox
    }

    fun update(update: List<CreateFilterView>) {
        views = update
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Tag(val binding: ItemCreateFilterTagBinding) : ViewHolder(binding.root) {
            fun bind(item: CreateFilterView.Tag) = with(binding) {
                ivSelectTagIcon.isSelected = item.isSelected
                tvTagName.text = item.name
                val color = ThemeColor.values().find { v -> v.title == item.color }
                val defaultTextColor = itemView.resources.getColor(R.color.text_primary, null)
                val defaultBackground = itemView.resources.getColor(R.color.shape_primary, null)
                if (color != null && color != ThemeColor.DEFAULT) {
                    tvTagName.background.setDrawableColor(
                        itemView.resources.light(
                            color,
                            defaultTextColor
                        )
                    )
                    tvTagName.setTextColor(itemView.resources.dark(color, defaultBackground))
                } else {
                    tvTagName.background.setDrawableColor(defaultBackground)
                    tvTagName.setTextColor(defaultTextColor)
                }
            }
        }

        class Status(val binding: ItemCreateFilterStatusBinding) : ViewHolder(binding.root) {
            fun bind(item: CreateFilterView.Status) = with(binding) {
                ivSelectStatusIcon.isSelected = item.isSelected
                tvStatusName.text = item.name
                val color = ThemeColor.values().find { v -> v.title == item.color }
                val defaultTextColor = itemView.resources.getColor(R.color.text_primary, null)
                if (color != null && color != ThemeColor.DEFAULT) {
                    tvStatusName.setTextColor(itemView.resources.dark(color, defaultTextColor))
                } else {
                    tvStatusName.setTextColor(defaultTextColor)
                }
            }
        }

        class Date(val binding: ItemCreateFilterDateBinding) : ViewHolder(binding.root) {
            fun bind(item: CreateFilterView.Date) = with(binding) {
                tvDateTitle.text = item.description
                if (item.type == DateDescription.EXACT_DAY) {
                    tvDate.visible()
                    tvDate.text = item.timeInMillis.formatTimestamp(isMillis = true)
                } else {
                    tvDate.invisible()
                }
                if (item.isSelected) {
                    iconChecked.visible()
                } else {
                    tvDate.invisible()
                    iconChecked.invisible()
                }
            }
        }

        class Object(val binding: ItemCreateFilterObjectBinding) : ViewHolder(binding.root) {
            fun bind(item: CreateFilterView.Object) = with(binding) {
                tvObjectName.text = item.name
                tvObjectType.text = item.typeName
                ivSelectObjectIcon.isSelected = item.isSelected
                objectIconWidget.setIcon(item.icon)
            }
        }

        class Checkbox(val binding: ItemCreateFilterCheckboxBinding) : ViewHolder(binding.root) {
            fun bind(item: CreateFilterView.Checkbox) = with(binding) {
                tvCheckbox.text = if (item.isChecked) {
                    itemView.resources.getString(R.string.dv_filter_checkbox_checked)
                } else {
                    itemView.resources.getString(R.string.dv_filter_checkbox_not_checked)
                }
                if (item.isSelected) {
                    iconChecked.visible()
                } else {
                    iconChecked.invisible()
                }
            }
        }
    }
}