package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDividerLineBinding
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class DividerLine(binding: ItemBlockDividerLineBinding) : Divider(binding.root) {

    override val container: View = binding.container

    fun bind(item: BlockView.DividerLine, clicked: (ListenerType) -> Unit) {
        super.bind(
            id = item.id,
            item = item,
            isItemSelected = item.isSelected,
            clicked = clicked,
            background = item.backgroundColor
        )
    }
}