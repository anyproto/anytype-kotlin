package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemRelationFormatBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationFormatCreateFromScratchBinding

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

class DefaultRelationFormatViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_relation_format_create_from_scratch,
        parent,
        false
    )
) {

    val binding = ItemRelationFormatCreateFromScratchBinding.bind(itemView)

    fun bind(
        name: String,
        format: Relation.Format,
        isSelected: Boolean
    ) = with(binding) {
        ivRelationFormat.bind(format)
        tvRelationName.text = name
        selectionIcon.isVisible = isSelected
    }

    fun setIsSelected(isSelected: Boolean) {
        binding.selectionIcon.isVisible = isSelected
    }
}