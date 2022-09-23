package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetStyleBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class ActionMenuHolder(val binding: ItemSlashWidgetStyleBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SlashItem.Actions) = with(binding) {
        when (item) {
            SlashItem.Actions.CleanStyle -> {
                tvTitle.setText(R.string.slash_widget_actions_clean_style)
                ivIcon.setImageResource(R.drawable.ic_slash_actions_clean_style)
                tvSubtitle.gone()
            }
            SlashItem.Actions.Copy -> {
                tvTitle.setText(R.string.slash_widget_actions_copy)
                ivIcon.setImageResource(R.drawable.ic_copy_24)
                tvSubtitle.gone()
            }
            SlashItem.Actions.Delete -> {
                tvTitle.setText(R.string.slash_widget_actions_delete)
                ivIcon.setImageResource(R.drawable.ic_delete_24)
                tvSubtitle.gone()
            }
            SlashItem.Actions.Duplicate -> {
                tvTitle.setText(R.string.slash_widget_actions_duplicate)
                ivIcon.setImageResource(R.drawable.ic_slash_actions_duplicate)
                tvSubtitle.gone()
            }
            SlashItem.Actions.Move -> {
                tvTitle.setText(R.string.slash_widget_actions_move)
                ivIcon.setImageResource(R.drawable.ic_slash_actions_move)
                tvSubtitle.gone()
            }
            SlashItem.Actions.MoveTo -> {
                tvTitle.setText(R.string.slash_widget_actions_moveto)
                ivIcon.setImageResource(R.drawable.ic_slash_actions_move_to)
                tvSubtitle.gone()
            }
            SlashItem.Actions.Paste -> {
                tvTitle.setText(R.string.slash_widget_actions_paste)
                ivIcon.setImageResource(R.drawable.ic_slash_actions_clean_style)
                tvSubtitle.gone()
            }
            SlashItem.Actions.LinkTo -> {
                tvTitle.setText(R.string.slash_widget_actions_link_to)
                ivIcon.setImageResource(R.drawable.ic_slash_actions_link_to)
                tvSubtitle.setText(R.string.slash_widget_actions_link_to_subtitle)
            }
        }
    }
}