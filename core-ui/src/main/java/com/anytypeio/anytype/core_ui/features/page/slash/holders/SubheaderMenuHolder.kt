package com.anytypeio.anytype.core_ui.features.page.slash.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_slash_widget_subheader.view.*

class SubheaderMenuHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: SlashItem.Subheader) = with(itemView) {
        val text = when (item) {
            SlashItem.Subheader.Style -> {
                flBack.invisible()
                R.string.slash_widget_main_style
            }
            SlashItem.Subheader.StyleWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_style
            }
            SlashItem.Subheader.Media -> {
                flBack.invisible()
                R.string.slash_widget_main_media
            }
            SlashItem.Subheader.MediaWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_media
            }
            SlashItem.Subheader.ObjectType -> {
                flBack.invisible()
                R.string.slash_widget_main_objects_subheader
            }
            SlashItem.Subheader.ObjectTypeWithBlack -> {
                flBack.visible()
                R.string.slash_widget_main_objects_subheader
            }
            SlashItem.Subheader.Other -> {
                flBack.invisible()
                R.string.slash_widget_main_other
            }
            SlashItem.Subheader.OtherWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_other
            }
            SlashItem.Subheader.Actions -> {
                flBack.invisible()
                R.string.slash_widget_main_actions
            }
            SlashItem.Subheader.ActionsWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_actions
            }
            SlashItem.Subheader.Alignment -> {
                flBack.invisible()
                R.string.slash_widget_main_alignment
            }
            SlashItem.Subheader.AlignmentWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_alignment
            }
            SlashItem.Subheader.Color -> {
                flBack.invisible()
                R.string.slash_widget_main_color
            }
            SlashItem.Subheader.ColorWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_color
            }
            SlashItem.Subheader.Background -> {
                flBack.invisible()
                R.string.slash_widget_main_background
            }
            SlashItem.Subheader.BackgroundWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_background
            }
        }
        subheader.setText(text)
    }
}