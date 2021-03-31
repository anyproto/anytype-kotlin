package com.anytypeio.anytype.core_ui.features.relations.create

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_models.Relation.Format.*
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_utils.tools.randomColor
import com.anytypeio.anytype.presentation.relations.RelationFormatView
import kotlinx.android.synthetic.main.item_create_data_view_relation_relation_format.view.*

class RelationFormatAdapter(
    private val onClick: (RelationFormatView) -> Unit
) : AbstractAdapter<RelationFormatView>(emptyList()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AbstractViewHolder<RelationFormatView> = ViewHolder(
        view = inflate(parent, R.layout.item_create_data_view_relation_relation_format)
    )

    override fun onBindViewHolder(holder: AbstractViewHolder<RelationFormatView>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            onClick(items[position])
        }
    }

    class ViewHolder(view: View) : AbstractViewHolder<RelationFormatView>(view) {

        private val name = itemView.formatName
        private val icon = itemView.formatIcon

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