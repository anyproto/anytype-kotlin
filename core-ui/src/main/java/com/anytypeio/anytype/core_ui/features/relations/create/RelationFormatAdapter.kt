package com.anytypeio.anytype.core_ui.features.relations.create

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anytypeio.anytype.core_models.Relation.Format.*
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.databinding.ItemCreateDataViewRelationRelationFormatBinding
import com.anytypeio.anytype.core_utils.tools.randomColor
import com.anytypeio.anytype.presentation.relations.RelationFormatView

class RelationFormatAdapter(
    private val onClick: (RelationFormatView) -> Unit
) : AbstractAdapter<RelationFormatView>(emptyList()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AbstractViewHolder<RelationFormatView> = ViewHolder(
        ItemCreateDataViewRelationRelationFormatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: AbstractViewHolder<RelationFormatView>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            onClick(items[position])
        }
    }

    class ViewHolder(
        val binding: ItemCreateDataViewRelationRelationFormatBinding
    ) : AbstractViewHolder<RelationFormatView>(binding.root) {

        private val name = binding.formatName
        private val icon = binding.formatIcon

        override fun bind(item: RelationFormatView) {
            when (item.format) {
                SHORT_TEXT -> {
                    name.setText(R.string.relation_format_short_text)
                }
                LONG_TEXT -> {
                    name.setText(R.string.relation_format_long_text)
                }
                NUMBER -> {
                    name.setText(R.string.relation_format_number)
                }
                STATUS -> {
                    name.setText(R.string.relation_format_status)
                }
                TAG -> {
                    name.setText(R.string.relation_format_tag)
                }
                DATE -> {
                    name.setText(R.string.relation_format_date)
                }
                FILE -> {
                    name.setText(R.string.relation_format_file)
                }
                CHECKBOX -> {
                    name.setText(R.string.relation_format_checkbox)
                }
                URL -> {
                    name.setText(R.string.relation_format_url)
                }
                EMAIL -> {
                    name.setText(R.string.relation_format_email)
                }
                PHONE -> {
                    name.setText(R.string.relation_format_phone)
                }
                EMOJI -> {
                    name.setText(R.string.relation_format_emoji)
                }
                OBJECT -> {
                    name.setText(R.string.relation_format_object)
                }
            }

            // Setting temporarily random color
            icon.backgroundTintList = ColorStateList.valueOf(name.text.toString().randomColor())
            itemView.isSelected = item.isSelected
        }
    }
}