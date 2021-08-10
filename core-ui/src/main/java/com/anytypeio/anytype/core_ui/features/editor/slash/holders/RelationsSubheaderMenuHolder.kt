package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import kotlinx.android.synthetic.main.item_slash_widget_subheader.view.*

class RelationsSubheaderMenuHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: RelationListViewModel.Model.Section.SlashWidget) = with(itemView) {
        val text = when (item) {
            RelationListViewModel.Model.Section.SlashWidget.Subheader -> {
                flBack.invisible()
                R.string.slash_widget_main_relations
            }
            RelationListViewModel.Model.Section.SlashWidget.SubheaderWithBack -> {
                flBack.visible()
                R.string.slash_widget_main_relations
            }
        }
        subheader.setText(text)
    }
}