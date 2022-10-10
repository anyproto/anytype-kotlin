package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkDeleteBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class LinkToObjectDelete(
    val binding: ItemBlockObjectLinkDeleteBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    DecoratableViewHolder,
    SupportCustomTouchProcessor {

    private val root = binding.root
    private val guideline = binding.pageGuideline

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
        applyDefaultOffsets()
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
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                marginStart = item.indent * dimen(R.dimen.indent)
            }
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

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations) { rect ->
                binding.content.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = dimen(R.dimen.dp_8) + rect.left
                    marginEnd = dimen(R.dimen.dp_8) + rect.right
                    bottomMargin = if (rect.bottom > 0) {
                        rect.bottom
                    } else {
                        dimen(R.dimen.dp_2)
                    }
                }
                binding.content.updatePadding(
                    left = dimen(R.dimen.default_document_content_padding_start),
                    right = dimen(R.dimen.default_document_item_padding_end)
                )
            }
        }
    }

    private fun applyDefaultOffsets() {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            binding.content.updatePadding(
                left = dimen(R.dimen.default_document_content_padding_start),
                right = dimen(R.dimen.default_document_item_padding_end)
            )
            binding.content.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.default_document_item_padding_start)
                marginEnd = dimen(R.dimen.default_document_item_padding_end)
                topMargin = dimen(R.dimen.dp_1)
                bottomMargin = dimen(R.dimen.dp_1)
            }
        }
    }
}