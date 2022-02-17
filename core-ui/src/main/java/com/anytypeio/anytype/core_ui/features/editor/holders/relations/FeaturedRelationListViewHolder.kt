package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import com.anytypeio.anytype.core_ui.databinding.ItemBlockFeaturedRelationsBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class FeaturedRelationListViewHolder(
    val binding: ItemBlockFeaturedRelationsBinding
) : BlockViewHolder(binding.root) {

    private val root = binding.featuredRelationRoot

    fun bind(item: BlockView.FeaturedRelation, click: (ListenerType) -> Unit) {
        root.set(item, click)
    }
}