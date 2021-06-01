package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemRelationFormatBinding

class DefaultRelationViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_relation_format,
        parent,
        false
    )
) {

    val binding = ItemRelationFormatBinding.bind(itemView)

    fun bind(
        name: String,
        format: Relation.Format
    ) = with(itemView) {
        binding.ivRelationFormat.bind(format)
        binding.tvRelationName.text = name
    }
}