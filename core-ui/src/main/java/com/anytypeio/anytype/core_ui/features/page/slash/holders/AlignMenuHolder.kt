package com.anytypeio.anytype.core_ui.features.page.slash.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_slash_widget_style.view.*

class AlignMenuHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: SlashItem.Alignment) = with(itemView) {
        when (item) {
            SlashItem.Alignment.Left -> {
                tvTitle.setText(R.string.slash_widget_align_left)
                ivIcon.setImageResource(R.drawable.ic_slash_align_left)
                tvSubtitle.gone()
            }
            SlashItem.Alignment.Center -> {
                tvTitle.setText(R.string.slash_widget_align_center)
                ivIcon.setImageResource(R.drawable.ic_slash_align_center)
                tvSubtitle.gone()
            }
            SlashItem.Alignment.Right -> {
                tvTitle.setText(R.string.slash_widget_align_right)
                ivIcon.setImageResource(R.drawable.ic_slash_align_right)
                tvSubtitle.gone()
            }
        }
    }
}