package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemListBaseBinding
import com.anytypeio.anytype.core_ui.extensions.formatIcon
import com.anytypeio.anytype.presentation.sets.model.ColumnView

abstract class PickColumnAdapter(
    private val columns: List<ColumnView>,
    private val columnSelectedKey: String?,
    private val click: (String?, String) -> Unit
) : RecyclerView.Adapter<PickColumnAdapter.ColumnHolder>() {

    abstract fun isColumnAvailable(key: String): Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColumnHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ColumnHolder(
            binding = ItemListBaseBinding.inflate(
                inflater, parent, false
            )
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

    inner class ColumnHolder(val binding: ItemListBaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val root = itemView
        private val title = binding.text
        private val icon = binding.icon

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