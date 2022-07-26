package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetStyleBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class OtherMenuHolder(
    val binding: ItemSlashWidgetStyleBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashItem.Other) = with(binding) {
        when (item) {
            SlashItem.Other.Line -> {
                tvTitle.setText(R.string.slash_widget_other_line)
                ivIcon.setImageResource(R.drawable.ic_slash_other_line)
                tvSubtitle.gone()
            }
            SlashItem.Other.Dots -> {
                tvTitle.setText(R.string.slash_widget_other_dots)
                ivIcon.setImageResource(R.drawable.ic_slash_other_dots)
                tvSubtitle.gone()
            }
            SlashItem.Other.TOC -> {
                tvTitle.setText(R.string.slash_widget_other_toc)
                ivIcon.setImageResource(R.drawable.ic_slash_toc)
                tvSubtitle.gone()
            }
            is SlashItem.Other.Table -> {
                val rowCount = item.rowCount
                val columnCount = item.columnCount
                if (rowCount != null && columnCount != null) {
                    tvTitle.text = binding.root.resources.getString(
                        R.string.slash_widgth_other_simple_table_rows_columns_count,
                        rowCount,
                        columnCount
                    )
                    tvSubtitle.visible()
                    tvSubtitle.setText(R.string.slash_widget_other_simple_table_subtitle)
                } else {
                    tvTitle.setText(R.string.slash_widget_other_simple_table)
                    tvSubtitle.gone()
                }
                ivIcon.setImageResource(R.drawable.ic_slash_simple_tables)
            }
        }
    }
}