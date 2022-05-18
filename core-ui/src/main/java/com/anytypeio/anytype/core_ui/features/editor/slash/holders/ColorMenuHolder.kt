package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetColorBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class ColorMenuHolder(
    val binding: ItemSlashWidgetColorBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashItem.Color) = with(binding) {
        circle.isSelected = item.isSelected
        circle.innerColor = view.context.resources.getColor(item)
        val capitalizedTitle = view.context.getTitle(item.themeColor)
        title.text = when (item) {
            is SlashItem.Color.Text -> capitalizedTitle
            is SlashItem.Color.Background -> itemView.resources.getString(
                R.string.slash_widget_background_item,
                capitalizedTitle
            )
        }
    }

    @ColorInt
    private fun Resources.getColor(item: SlashItem.Color): Int {
        return when (item) {
            is SlashItem.Color.Text -> dark(item.themeColor)
            is SlashItem.Color.Background -> veryLight(item.themeColor)
        }
    }

    private fun Context.getTitle(color: ThemeColor): String {
        return when (color) {
            ThemeColor.DEFAULT -> getString(R.string.slash_widget_default_color_title)
            ThemeColor.GREY -> getString(R.string.slash_widget_gray_color_title)
            ThemeColor.YELLOW -> getString(R.string.slash_widget_yellow_color_title)
            ThemeColor.ORANGE -> getString(R.string.slash_widget_orange_color_title)
            ThemeColor.RED -> getString(R.string.slash_widget_red_color_title)
            ThemeColor.PINK -> getString(R.string.slash_widget_pink_color_title)
            ThemeColor.PURPLE -> getString(R.string.slash_widget_purple_color_title)
            ThemeColor.BLUE -> getString(R.string.slash_widget_blue_color_title)
            ThemeColor.ICE -> getString(R.string.slash_widget_ice_color_title)
            ThemeColor.TEAL -> getString(R.string.slash_widget_teal_color_title)
            ThemeColor.LIME -> getString(R.string.slash_widget_lime_color_title)
        }
    }
}