package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.ListenerType

class DividerLine(view: View) : Divider(view) {

    fun bind(item: BlockView.DividerLine, clicked: (ListenerType) -> Unit) {
        super.bind(
            id = item.id,
            item = item,
            isItemSelected = item.isSelected,
            clicked = clicked
        )
    }
}