package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockFeaturedRelationsBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class FeaturedRelationListViewHolder(
    val binding: ItemBlockFeaturedRelationsBinding
) : BlockViewHolder(binding.root) {

    private val root = binding.featuredRelationRoot

    fun bind(item: BlockView.FeaturedRelation, click: (ListenerType) -> Unit) {
        if (item.isTodoLayout) {
            val lr = itemView.context.dimen(R.dimen.dp_60).toInt()
            binding.featuredRelationRoot.setPadding(lr, 0, 0, 0)
        } else {
            val lr = itemView.context.dimen(R.dimen.dp_20).toInt()
            binding.featuredRelationRoot.setPadding(lr, 0, 0, 0)
        }
        root.set(item, click)
    }
}