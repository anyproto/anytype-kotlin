package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_object_link_delete.view.*

class LinkToObjectDelete(view: View) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor {

    private val guideline = itemView.pageGuideline
    private val progress = itemView.progress
    private val syncing = itemView.syncing
    private val iconContainer = itemView.pageIconContainer
    private val title = itemView.linkDeleteTitle

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
        bindLoading(isLoading = item.isLoading)
        itemView.setOnClickListener { clicked(ListenerType.LinkToObjectDeleted(item.id)) }
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
    }

    private fun bindLoading(isLoading: Boolean) {
        if (isLoading) {
            iconContainer.invisible()
            title.invisible()
            progress.visible()
            syncing.visible()
        } else {
            progress.invisible()
            syncing.invisible()
            iconContainer.visible()
            title.visible()
        }
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