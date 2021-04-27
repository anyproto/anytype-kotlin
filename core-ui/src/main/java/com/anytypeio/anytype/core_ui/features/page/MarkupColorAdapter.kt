package com.anytypeio.anytype.core_ui.features.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.ColorCircleWidget
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.markup.MarkupColorView

class MarkupColorAdapter(
    private var items: List<MarkupColorView>,
    private val onColorClicked: (MarkupColorView) -> Unit
) : RecyclerView.Adapter<MarkupColorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_markup_color_text -> {
                ViewHolder(
                    view = inflater.inflate(
                        viewType,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        onColorClicked(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_markup_color_background -> {
                ViewHolder(
                    view = inflater.inflate(
                        viewType,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        onColorClicked(items[bindingAdapterPosition])
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    fun update(update: List<MarkupColorView>) {
        items = update
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) = when (val item = items[position]) {
        is MarkupColorView.Background -> holder.bind(item)
        is MarkupColorView.Text -> holder.bind(item)
    }

    override fun getItemViewType(
        position: Int
    ): Int = when (items[position]) {
        is MarkupColorView.Background -> R.layout.item_markup_color_background
        is MarkupColorView.Text -> R.layout.item_markup_color_text
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val circle = itemView.findViewById<ColorCircleWidget>(R.id.circle)
        fun bind(view: MarkupColorView.Text) {
            val color = ThemeColor.values().first { it.title == view.code }
            circle.isSelected = view.isSelected
            circle.innerColor = color.text
        }

        fun bind(view: MarkupColorView.Background) {
            val color = ThemeColor.values().first { it.title == view.code }
            circle.isSelected = view.isSelected
            circle.innerColor = color.background
        }
    }
}