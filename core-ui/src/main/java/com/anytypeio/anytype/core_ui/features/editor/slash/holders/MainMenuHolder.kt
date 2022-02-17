package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetMainBinding
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class MainMenuHolder(val binding: ItemSlashWidgetMainBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashItem.Main) = with(binding) {
        when (item) {
            SlashItem.Main.Actions -> {
                textMain.setText(R.string.slash_widget_main_actions)
                iconMain.setImageResource(R.drawable.ic_slash_main_actions)
            }
            SlashItem.Main.Alignment -> {
                textMain.setText(R.string.slash_widget_main_alignment)
                iconMain.setImageResource(R.drawable.ic_slash_main_alignment)
            }
            SlashItem.Main.Background -> {
                textMain.setText(R.string.slash_widget_main_background)
                iconMain.setImageResource(R.drawable.ic_slash_main_rectangle)
            }
            SlashItem.Main.Color -> {
                textMain.setText(R.string.slash_widget_main_color)
                iconMain.setImageResource(R.drawable.ic_slash_main_color)
            }
            SlashItem.Main.Media -> {
                textMain.setText(R.string.slash_widget_main_media)
                iconMain.setImageResource(R.drawable.ic_slash_main_media)
            }
            SlashItem.Main.Objects -> {
                textMain.setText(R.string.slash_widget_main_objects)
                iconMain.setImageResource(R.drawable.ic_slash_main_objects)
            }
            SlashItem.Main.Other -> {
                textMain.setText(R.string.slash_widget_main_other)
                iconMain.setImageResource(R.drawable.ic_slash_main_other)
            }
            SlashItem.Main.Relations -> {
                textMain.setText(R.string.slash_widget_main_relations)
                iconMain.setImageResource(R.drawable.ic_slash_main_relations)
            }
            SlashItem.Main.Style -> {
                textMain.setText(R.string.slash_widget_main_style)
                iconMain.setImageResource(R.drawable.ic_slash_main_style)
            }
        }
    }
}