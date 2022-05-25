package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationTagsBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.DocumentRelationView


sealed class FeaturedRelationViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class Default(
        val binding: ItemFeaturedRelationDefaultBinding
    ) : FeaturedRelationViewHolder(binding.root) {
        fun bind(item: DocumentRelationView) {
            binding.tvFeaturedRelationValue.text = item.value
        }
    }

    class Tags(
        val binding: ItemFeaturedRelationTagsBinding
    ) : FeaturedRelationViewHolder(binding.root) {

        private val container = binding.featuredRelationTagContainer

        fun bind(item: DocumentRelationView.Tags) {
            container.removeAllViews()
            item.tags.forEach { tag ->
                container.addView(
                    TextView(itemView.context).apply {
                        text = tag.tag
                        setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
                        val themeColor = ThemeColor.values().find { it.title == tag.color }
                        if (themeColor != null) {
                            background.setDrawableColor(themeColor.background)
                            setTextColor(themeColor.text)
                        } else {
                            background.setDrawableColor(context.color(R.color.default_filter_tag_background_color))
                            setTextColor(context.color(R.color.default_filter_tag_text_color))
                        }
                    }
                )
            }
        }
    }

    class Status(
        val binding: ItemFeaturedRelationStatusBinding
    ) : FeaturedRelationViewHolder(binding.root) {

        private val container = binding.featuredRelationStatusContainer

        fun bind(item: DocumentRelationView.Status) {
            container.removeAllViews()
            item.status.forEach { status ->
                container.addView(
                    TextView(itemView.context).apply {
                        text = status.status
                        val themeColor = ThemeColor.values().find { it.title == status.color }
                        if (themeColor != null) {
                            setTextColor(themeColor.text)
                        } else {
                            setTextColor(context.color(R.color.default_filter_tag_text_color))
                        }
                    }
                )
            }
        }
    }
}