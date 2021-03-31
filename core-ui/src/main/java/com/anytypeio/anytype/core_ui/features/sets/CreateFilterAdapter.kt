package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.formatTimestamp
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.DateDescription
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import kotlinx.android.synthetic.main.item_create_filter_date.view.*
import kotlinx.android.synthetic.main.item_create_filter_object.view.*
import kotlinx.android.synthetic.main.item_create_filter_status.view.*
import kotlinx.android.synthetic.main.item_create_filter_tag.view.*
import kotlinx.android.synthetic.main.widget_object_icon_text.view.*
import java.util.*

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
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onItemClicked(views[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_create_filter_date -> {
                ViewHolder.Date(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onItemClicked(views[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_create_filter_status -> {
                ViewHolder.Status(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onItemClicked(views[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_create_filter_object -> {
                ViewHolder.Object(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onItemClicked(views[bindingAdapterPosition])
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
    }

    override fun getItemCount(): Int = views.size

    override fun getItemViewType(position: Int): Int = when (views[position]) {
        is CreateFilterView.Tag -> R.layout.item_create_filter_tag
        is CreateFilterView.Status -> R.layout.item_create_filter_status
        is CreateFilterView.Date -> R.layout.item_create_filter_date
        is CreateFilterView.Object -> R.layout.item_create_filter_object
    }

    fun update(update: List<CreateFilterView>) {
        views = update
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Tag(view: View) : ViewHolder(view) {
            fun bind(item: CreateFilterView.Tag) = with(itemView) {
                ivSelectTagIcon.isSelected = item.isSelected
                tvTagName.text = item.name
                val color = ThemeColor.values().find { v -> v.title == item.color }
                if (color != null) {
                    tvTagName.background.setDrawableColor(color.background)
                    tvTagName.setTextColor(color.text)
                } else {
                    tvTagName.background.setDrawableColor(context.color(R.color.default_filter_tag_background_color))
                    tvTagName.setTextColor(context.color(R.color.default_filter_tag_text_color))
                }
            }
        }

        class Status(view: View) : ViewHolder(view) {
            fun bind(item: CreateFilterView.Status) = with(itemView) {
                ivSelectStatusIcon.isSelected = item.isSelected
                tvStatusName.text = item.name
                val color = ThemeColor.values().find { v -> v.title == item.color }
                if (color != null) {
                    tvStatusName.setTextColor(color.text)
                } else {
                    tvStatusName.setTextColor(context.color(R.color.default_filter_status_text_color))
                }
            }
        }

        class Date(view: View) : ViewHolder(view) {
            fun bind(item: CreateFilterView.Date) = with(itemView) {
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

        class Object(view: View) : ViewHolder(view) {
            fun bind(item: CreateFilterView.Object) = with(itemView) {
                tvObjectName.text = item.name
                tvObjectType.text = item.type
                ivSelectObjectIcon.isSelected = item.isSelected
                objectIconWidget.setIcon(
                    emoji = item.emoji,
                    image = item.image,
                    name = item.name
                )
            }
        }
    }
}