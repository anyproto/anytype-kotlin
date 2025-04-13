package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderLeftBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashPropertyView

class RelationsSubheaderMenuHolder(
    val binding: ItemSlashWidgetSubheaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashPropertyView.Section) = with(binding) {
        val text = when (item) {
            SlashPropertyView.Section.Subheader -> {
                flBack.invisible()
                R.string.slash_widget_main_relations
            }
            SlashPropertyView.Section.SubheaderWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_relations
            }
        }
        subheader.setText(text)
    }
}

class RelationsSubheaderOnlyMenuHolder(
    val binding: ItemSlashWidgetSubheaderLeftBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind() = with(binding) {
        subheader.setText(R.string.slash_widget_main_relations)
    }
}