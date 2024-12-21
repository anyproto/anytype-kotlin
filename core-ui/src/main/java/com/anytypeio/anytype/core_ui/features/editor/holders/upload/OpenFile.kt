package com.anytypeio.anytype.core_ui.features.editor.holders.upload

import android.view.View
import com.anytypeio.anytype.core_ui.databinding.ItemBlockOpenFileBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class OpenFile(
    binding: ItemBlockOpenFileBinding
) : BlockViewHolder(binding.root) {

    private val root: View = itemView

    fun bind(item: BlockView.OpenFile.File, click: (ListenerType) -> Unit) {
        root.setOnClickListener {
            click(
                ListenerType.File.View(
                    target = item.targetId,
                )
            )
        }
    }
}

class OpenImage(
    binding: ItemBlockOpenFileBinding
) : BlockViewHolder(binding.root) {

    private val root: View = itemView

    fun bind(item: BlockView.OpenFile.Image, click: (ListenerType) -> Unit) {
        root.setOnClickListener {
            click(
                ListenerType.Picture.View(
                    target = item.id
                )
            )
        }
    }
}