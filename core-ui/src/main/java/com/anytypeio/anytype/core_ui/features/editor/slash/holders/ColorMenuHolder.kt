package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetColorBinding
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import java.util.*

class ColorMenuHolder(
    val binding: ItemSlashWidgetColorBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val locale: Locale = Locale.getDefault()

    fun bind(item: SlashItem.Color) = with(binding) {
        when (item) {
            is SlashItem.Color.Text -> {
                circle.isSelected = item.isSelected
                val color = ThemeColor.values().first { it.title == item.code }
                circle.innerColor = color.text
                title.text = item.code.capitalize(locale)
            }
            is SlashItem.Color.Background -> {
                circle.isSelected = item.isSelected
                val color = ThemeColor.values().first { it.title == item.code }
                circle.innerColor = color.background
                val background = item.code.capitalize(Locale.getDefault())
                title.text =
                    itemView.resources.getString(R.string.slash_widget_background_item, background)
            }
        }
    }
}