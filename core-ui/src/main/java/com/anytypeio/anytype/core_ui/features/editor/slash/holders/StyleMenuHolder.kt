package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetStyleBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class StyleMenuHolder(
    val binding: ItemSlashWidgetStyleBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashItem.Style) = with(binding) {
        when (item) {
            is SlashItem.Style.Markup.Bold -> {
                tvTitle.setText(R.string.slash_widget_style_bold)
                ivIcon.setImageResource(R.drawable.ic_slash_style_bold)
                tvSubtitle.gone()
            }
            is SlashItem.Style.Markup.Breakthrough -> {
                tvTitle.setText(R.string.slash_widget_style_breakthrough)
                ivIcon.setImageResource(R.drawable.ic_slash_style_breakthrough)
                tvSubtitle.gone()
            }
            is SlashItem.Style.Markup.Code -> {
                tvTitle.setText(R.string.slash_widget_style_code)
                ivIcon.setImageResource(R.drawable.ic_slash_style_code)
                tvSubtitle.gone()
            }
            is SlashItem.Style.Markup.Italic -> {
                tvTitle.setText(R.string.slash_widget_style_italic)
                ivIcon.setImageResource(R.drawable.ic_slash_style_italic)
                tvSubtitle.gone()
            }
            is SlashItem.Style.Type.Bulleted -> {
                tvTitle.setText(R.string.slash_widget_style_bulleted)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_bulleted_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_bulleted)
            }
            is SlashItem.Style.Type.Callout -> {
                tvTitle.setText(R.string.slash_widget_style_callout)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_callout_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_callout)
            }
            is SlashItem.Style.Type.Checkbox -> {
                tvTitle.setText(R.string.slash_widget_style_checkbox)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_checkbox_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_checkbox)
            }
            is SlashItem.Style.Type.Heading -> {
                tvTitle.setText(R.string.slash_widget_style_heading)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_heading_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_heading)
            }
            is SlashItem.Style.Type.Highlighted -> {
                tvTitle.setText(R.string.slash_widget_style_highlighted)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_highlighted_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_highlighted)
            }
            is SlashItem.Style.Type.Numbered -> {
                tvTitle.setText(R.string.slash_widget_style_numbered)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_numbered_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_numbered)
            }
            is SlashItem.Style.Type.Subheading -> {
                tvTitle.setText(R.string.slash_widget_style_subheading)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_subheading_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_subheading)
            }
            is SlashItem.Style.Type.Text -> {
                tvTitle.setText(R.string.slash_widget_style_text)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_text_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_text)
            }
            is SlashItem.Style.Type.Title -> {
                tvTitle.setText(R.string.slash_widget_style_title)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_title_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_title)
            }
            is SlashItem.Style.Type.Toggle -> {
                tvTitle.setText(R.string.slash_widget_style_toggle)
                tvSubtitle.visible()
                tvSubtitle.setText(R.string.slash_widget_style_toggle_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_style_toggle)
            }
        }
    }
}