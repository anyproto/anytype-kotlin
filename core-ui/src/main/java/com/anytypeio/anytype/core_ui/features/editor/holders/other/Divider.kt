package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_divider_line.view.*

open class Divider(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    val divider: View get() = itemView.divider

    fun bind(
        id: String,
        item: BlockView.Indentable,
        isItemSelected: Boolean,
        clicked: (ListenerType) -> Unit
    ) = with(itemView) {
        indentize(item)
        isSelected = isItemSelected
        setOnClickListener { clicked(ListenerType.DividerClick(id)) }
        setOnLongClickListener(
            EditorLongClickListener(
                t = id,
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