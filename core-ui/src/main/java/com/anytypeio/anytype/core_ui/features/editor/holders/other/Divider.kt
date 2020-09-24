package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.page.ListenerType
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_divider.view.*

class Divider(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    val divider: View get() = itemView.divider

    fun bind(
        item: BlockView.Divider,
        clicked: (ListenerType) -> Unit
    ) = with(itemView) {
        indentize(item)
        isSelected = item.isSelected
        setOnClickListener { clicked(ListenerType.DividerClick(item.id)) }
        setOnLongClickListener(
            EditorLongClickListener(
                t = item.id,
                click = { onBlockLongClick(itemView, it, clicked) }
            )
        )
    }

    override fun indentize(item: BlockView.Indentable) {
        divider.updateLayoutParams<FrameLayout.LayoutParams> {
            marginStart = item.indent * dimen(R.dimen.indent)
        }
    }
}