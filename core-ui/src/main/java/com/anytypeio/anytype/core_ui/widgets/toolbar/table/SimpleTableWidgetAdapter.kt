package com.anytypeio.anytype.core_ui.widgets.toolbar.table

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSimpleTableActionBinding
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem

class SimpleTableWidgetAdapter(
    private var items: List<SimpleTableWidgetItem>,
    private val onClick: (SimpleTableWidgetItem) -> Unit
) : RecyclerView.Adapter<SimpleTableWidgetAdapter.VH>() {

    fun update(items: List<SimpleTableWidgetItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val holder = VH(
            binding = ItemSimpleTableActionBinding.inflate(inflater, parent, false)
        ).apply {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onClick(items[pos])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(binding: ItemSimpleTableActionBinding) : RecyclerView.ViewHolder(binding.root) {

        val icon = binding.icon
        val title = binding.title

        fun bind(item: SimpleTableWidgetItem) {
            when (item) {
                SimpleTableWidgetItem.Cell.ClearContents,
                is SimpleTableWidgetItem.Row.ClearContents,
                is SimpleTableWidgetItem.Column.ClearContents -> {
                    title.setText(R.string.simple_tables_widget_item_clear_contents)
                    icon.setImageResource(R.drawable.ic_clear_32)
                }
                SimpleTableWidgetItem.Cell.ResetStyle,
                is SimpleTableWidgetItem.Column.ResetStyle,
                is SimpleTableWidgetItem.Row.ResetStyle -> {
                    title.setText(R.string.simple_tables_widget_item_clear_style)
                    icon.setImageResource(R.drawable.ic_reset_32)
                }
                SimpleTableWidgetItem.Cell.Color,
                is SimpleTableWidgetItem.Column.Color,
                is SimpleTableWidgetItem.Row.Color -> {
                    title.setText(R.string.simple_tables_widget_item_color)
                    icon.setImageResource(R.drawable.ic_color_32)
                }
                SimpleTableWidgetItem.Cell.Style,
                is SimpleTableWidgetItem.Row.Style,
                is SimpleTableWidgetItem.Column.Style -> {
                    title.setText(R.string.simple_tables_widget_item_style)
                    icon.setImageResource(R.drawable.ic_style_32)
                }
                is SimpleTableWidgetItem.Column.Delete,
                is SimpleTableWidgetItem.Row.Delete -> {
                    title.setText(R.string.toolbar_action_delete)
                    icon.setImageResource(R.drawable.ic_block_action_delete)
                }
                is SimpleTableWidgetItem.Column.Duplicate,
                is SimpleTableWidgetItem.Row.Duplicate -> {
                    title.setText(R.string.toolbar_action_duplicate)
                    icon.setImageResource(R.drawable.ic_block_action_duplicate)
                }
                is SimpleTableWidgetItem.Column.InsertLeft -> {
                    title.setText(R.string.simple_tables_widget_item_insert_left)
                    icon.setImageResource(R.drawable.ic_column_insert_left)
                }
                is SimpleTableWidgetItem.Column.InsertRight -> {
                    title.setText(R.string.simple_tables_widget_item_insert_right)
                    icon.setImageResource(R.drawable.ic_column_insert_right)
                }
                is SimpleTableWidgetItem.Column.MoveLeft -> {
                    title.setText(R.string.simple_tables_widget_item_move_left)
                    icon.setImageResource(R.drawable.ic_move_column_left)
                }
                is SimpleTableWidgetItem.Column.MoveRight -> {
                    title.setText(R.string.simple_tables_widget_item_move_right)
                    icon.setImageResource(R.drawable.ic_move_column_right)
                }
                is SimpleTableWidgetItem.Row.InsertAbove -> {
                    title.setText(R.string.simple_tables_widget_item_insert_above)
                    icon.setImageResource(R.drawable.ic_add_row_above)
                }
                is SimpleTableWidgetItem.Row.InsertBelow -> {
                    title.setText(R.string.simple_tables_widget_item_insert_below)
                    icon.setImageResource(R.drawable.ic_add_row_below)
                }
                is SimpleTableWidgetItem.Row.MoveDown -> {
                    title.setText(R.string.simple_tables_widget_item_move_down)
                    icon.setImageResource(R.drawable.ic_move_row_down)
                }
                is SimpleTableWidgetItem.Row.MoveUp -> {
                    title.setText(R.string.simple_tables_widget_item_move_up)
                    icon.setImageResource(R.drawable.ic_move_row_up)
                }
                is SimpleTableWidgetItem.Column.Copy,
                is SimpleTableWidgetItem.Row.Copy -> {
                    title.setText(R.string.simple_tables_widget_item_copy)
                    icon.setImageResource(R.drawable.ic_copy_32)
                }
                is SimpleTableWidgetItem.Column.Sort -> {
                    title.setText(R.string.simple_tables_widget_item_sort)
                    icon.setImageResource(R.drawable.ic_action_32)
                }
                SimpleTableWidgetItem.Tab.Cell -> Unit
                SimpleTableWidgetItem.Tab.Column -> Unit
                SimpleTableWidgetItem.Tab.Row -> Unit
            }
        }
    }
}