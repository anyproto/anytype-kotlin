package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_slash_widget_color.view.*
import java.util.*

class ColorMenuHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val locale: Locale = Locale.getDefault()

    fun bind(item: SlashItem.Color) = with(itemView) {
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
                title.text = resources.getString(R.string.slash_widget_background_item, background)
            }
        }
    }
}