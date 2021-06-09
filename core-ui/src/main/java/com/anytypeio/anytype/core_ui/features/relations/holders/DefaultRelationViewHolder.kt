package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import kotlinx.android.synthetic.main.item_relation_format_create_from_scratch.view.*

class DefaultRelationViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_relation_format,
        parent,
        false
    )
) {

    fun bind(
        name: String,
        format: Relation.Format
    ) = with(itemView) {
        ivRelationFormat.bind(format)
        tvRelationName.text = name
    }
}

class DefaultRelationFormatViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_relation_format_create_from_scratch,
        parent,
        false
    )
) {

    fun bind(
        name: String,
        format: Relation.Format,
        isSelected: Boolean
    ) {
        itemView.ivRelationFormat.bind(format)
        itemView.tvRelationName.text = name
        itemView.selectionIcon.isVisible = isSelected
    }

    fun setIsSelected(isSelected: Boolean) {
        itemView.selectionIcon.isVisible = isSelected
    }
}