package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class DividerDots(view: View) : Divider(view) {

    fun bind(item: BlockView.DividerDots, clicked: (ListenerType) -> Unit) {
        super.bind(
            id = item.id,
            item = item,
            isItemSelected = item.isSelected,
            clicked = clicked
        )
    }
}