package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.formatIcon
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import kotlinx.android.synthetic.main.item_list_base.view.*

abstract class PickColumnAdapter(
    private val columns: List<ColumnView>,
    private val columnSelectedKey: String?,
    private val click: (String?, String) -> Unit
) : RecyclerView.Adapter<PickColumnAdapter.ColumnHolder>() {

    abstract fun isColumnAvailable(key: String): Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColumnHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ColumnHolder(
            inflater.inflate(R.layout.item_list_base, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ColumnHolder, position: Int) {
        holder.bind(
            columnSelectedKey = columnSelectedKey,
            column = columns[position],
            click = click,
            isColumnAvailable = isColumnAvailable(columns[position].key)
        )
    }

    override fun getItemCount(): Int = columns.size

    inner class ColumnHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val root = itemView
        private val title = itemView.text
        private val icon = itemView.icon

        fun bind(
            columnSelectedKey: String?,
            isColumnAvailable: Boolean,
            column: ColumnView,
            click: (String?, String) -> Unit
        ) {
            setIcon(icon, column.format)
            title.text = column.text
            root.isSelected = column.key == columnSelectedKey
            root.alpha = if (root.isSelected || !isColumnAvailable) {
                root.setOnClickListener { click(columnSelectedKey, column.key) }
                ALPHA_ENABLE
            } else {
                ALPHA_DISABLE
            }
        }

        private fun setIcon(icon: ImageView, format: ColumnView.Format) {
            icon.setImageDrawable(itemView.context.formatIcon(format))
        }
    }

    companion object {
        private const val ALPHA_ENABLE = 1.0f
        private const val ALPHA_DISABLE = 0.2f
    }
}