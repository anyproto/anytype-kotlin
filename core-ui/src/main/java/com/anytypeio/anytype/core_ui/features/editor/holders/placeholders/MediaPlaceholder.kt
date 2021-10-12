package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import android.view.View
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_utils.ext.PopupExtensions
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

abstract class MediaPlaceholder(view: View) :
    BlockViewHolder(view),
    BlockViewHolder.DragAndDropHolder,
    BlockViewHolder.IndentableHolder,
    SupportCustomTouchProcessor {

    abstract val root: View
    abstract fun placeholderClick(target: String, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.MediaPlaceholder,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(itemView) {
            setOnClickListener { placeholderClick(item.id, clicked) }
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.MediaPlaceholder) { "Expected a media placeholder, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }
}

