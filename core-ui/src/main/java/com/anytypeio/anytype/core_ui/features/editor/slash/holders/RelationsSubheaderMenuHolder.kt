package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView

class RelationsSubheaderMenuHolder(
    val binding: ItemSlashWidgetSubheaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashRelationView.Section) = with(binding) {
        val text = when (item) {
            SlashRelationView.Section.Subheader -> {
                flBack.invisible()
                R.string.slash_widget_main_relations
            }
            SlashRelationView.Section.SubheaderWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_relations
            }
        }
        subheader.setText(text)
    }
}