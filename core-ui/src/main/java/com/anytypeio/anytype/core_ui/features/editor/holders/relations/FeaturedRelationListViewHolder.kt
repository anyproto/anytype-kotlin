package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import android.view.View
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_featured_relations.view.*

class FeaturedRelationListViewHolder(view: View) : BlockViewHolder(view) {

    private val root = itemView.featuredRelationRoot

    fun bind(item: BlockView.FeaturedRelation, click: (ListenerType) -> Unit) {
        root.set(item, click)
    }
}