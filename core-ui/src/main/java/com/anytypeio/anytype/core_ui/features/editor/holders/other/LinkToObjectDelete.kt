package com.anytypeio.anytype.core_ui.features.editor.holders.other

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkDeleteBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class LinkToObjectDelete(
    val binding: ItemBlockObjectLinkDeleteBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor {

    private val guideline = binding.pageGuideline

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Deleted,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        itemView.isSelected = item.isSelected
        itemView.setOnClickListener { clicked(ListenerType.LinkToObjectDeleted(item.id)) }
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Deleted) { "Expected a object link deleted block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
        }
    }
}