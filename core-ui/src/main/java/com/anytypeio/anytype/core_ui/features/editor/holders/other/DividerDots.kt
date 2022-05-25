package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDividerDotsBinding
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class DividerDots(binding: ItemBlockDividerDotsBinding) : Divider(binding.root) {

    override val container: View = binding.container

    fun bind(item: BlockView.DividerDots, clicked: (ListenerType) -> Unit) {
        super.bind(
            id = item.id,
            item = item,
            isItemSelected = item.isSelected,
            clicked = clicked,
            background = item.backgroundColor
        )
    }
}