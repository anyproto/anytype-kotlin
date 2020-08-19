package com.agileburo.anytype.core_ui.features.editor.holders

import android.view.View
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener

class Divider(view: View) : BlockViewHolder(view) {

    fun bind(
        item: BlockView.Divider,
        clicked: (ListenerType) -> Unit
    ) {
        itemView.setOnLongClickListener(
            EditorLongClickListener(
                t = item.id,
                click = { onBlockLongClick(itemView, it, clicked) }
            )
        )
    }
}