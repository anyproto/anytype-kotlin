package com.anytypeio.anytype.core_ui.features.sets

import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import kotlinx.android.synthetic.main.item_search_relation.view.*

class SearchRelationAdapter(
    private val onRelationClicked: (SimpleRelationView) -> Unit
) : AbstractAdapter<SimpleRelationView>(emptyList()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        view = inflate(parent, R.layout.item_search_relation)
    ).apply {
        itemView.setOnClickListener { onRelationClicked(items[bindingAdapterPosition]) }
    }

    class ViewHolder(view: View) : AbstractViewHolder<SimpleRelationView>(view) {
        override fun bind(item: SimpleRelationView) {
            itemView.ivRelation.setImageResource(item.format.relationIcon())
            itemView.tvRelationName.text = item.title
        }
    }
}