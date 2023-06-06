package com.anytypeio.anytype.core_ui.features.editor.holders.upload

import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

abstract class MediaUpload(
    view: View
) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder,
    SupportCustomTouchProcessor {

    abstract val root: View
    abstract fun uploadClick(target: String, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.Upload,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(itemView) {
            setOnClickListener { uploadClick(item.id, clicked) }
        }
    }

    open fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Upload) { "Expected upload block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }
}