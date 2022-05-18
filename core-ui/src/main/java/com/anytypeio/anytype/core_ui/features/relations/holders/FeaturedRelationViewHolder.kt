package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationTagsBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView


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
            // TODO optimize create and delete only diff between views
            container.removeAllViews()
            item.tags.forEach { tag ->
                container.addView(createTagView(tag))
            }
        }

        private fun createTagView(tag: TagView): View {
            val context = itemView.context
            val resources = context.resources
            val defaultTextColor = context.color(R.color.default_filter_tag_text_color)
            val defaultBackgroundColor = context.color(R.color.default_filter_tag_background_color)
            val themeColor = ThemeColor.values().find { it.code == tag.color } ?: ThemeColor.DEFAULT
            return TextView(context).apply {
                text = tag.tag
                setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
                background.setDrawableColor(resources.veryLight(themeColor, defaultBackgroundColor))
                setTextColor(resources.dark(themeColor, defaultTextColor))
            }
        }
    }

    class Status(
        val binding: ItemFeaturedRelationStatusBinding
    ) : FeaturedRelationViewHolder(binding.root) {

        private val container = binding.featuredRelationStatusContainer

        fun bind(item: DocumentRelationView.Status) {
            // TODO optimize create and delete only diff between views
            container.removeAllViews()
            item.status.forEach { status ->
                container.addView(createStatusView(status))
            }
        }

        private fun createStatusView(status: StatusView): View {
            val resources = itemView.context.resources
            val color = ThemeColor.values().find { it.code == status.color } ?: ThemeColor.DEFAULT
            val defaultTextColor = itemView.context.color(R.color.default_filter_tag_text_color)
            return TextView(itemView.context).apply {
                text = status.status
                setTextColor(resources.dark(color, defaultTextColor))
            }
        }
    }
}