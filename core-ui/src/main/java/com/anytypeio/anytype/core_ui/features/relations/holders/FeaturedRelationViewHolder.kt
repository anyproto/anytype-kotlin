package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import kotlinx.android.synthetic.main.item_featured_relation_default.view.*
import kotlinx.android.synthetic.main.item_featured_relation_status.view.*
import kotlinx.android.synthetic.main.item_featured_relation_tags.view.*

sealed class FeaturedRelationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class Default(view: View) : FeaturedRelationViewHolder(view) {
        fun bind(item: DocumentRelationView) {
            itemView.tvFeaturedRelationValue.text = item.value
        }
    }

    class Tags(view: View) : FeaturedRelationViewHolder(view) {

        private val container = itemView.featuredRelationTagContainer

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

    class Status(view: View) : FeaturedRelationViewHolder(view) {

        private val container = itemView.featuredRelationStatusContainer

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