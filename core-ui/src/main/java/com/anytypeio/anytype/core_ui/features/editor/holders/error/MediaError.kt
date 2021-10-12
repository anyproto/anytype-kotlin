package com.anytypeio.anytype.core_ui.features.editor.holders.error

import android.view.View
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

abstract class MediaError(
    view: View
) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor {

    abstract val root: View
    abstract fun errorClick(item: BlockView.Error, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.Error,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(itemView) {
            setOnClickListener { errorClick(item, clicked) }
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Error) { "Expected error block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                itemView.isSelected = item.isSelected
            }
        }
    }
}