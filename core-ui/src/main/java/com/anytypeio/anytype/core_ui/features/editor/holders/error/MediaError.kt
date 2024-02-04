package com.anytypeio.anytype.core_ui.features.editor.holders.error

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaErrorBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableMediaErrorViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

abstract class MediaError(
    val binding: ItemBlockMediaErrorBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    SupportCustomTouchProcessor, DecoratableMediaErrorViewHolder {

    override val decoratableContainer: EditorDecorationContainer get() = binding.decorationContainer
    override val decoratableCard: View get() = binding.card
    val errorIcon: View get() = binding.errorMessage
    abstract fun errorClick(item: BlockView.Error, clicked: (ListenerType) -> Unit)

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    open fun bind(
        item: BlockView.Error,
        clicked: (ListenerType) -> Unit
    ) {
        select(item.isSelected)
        with(itemView) {
            setOnClickListener { errorClick(item, clicked) }
        }
    }

    open fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Error) { "Expected error media block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    fun select(isSelected: Boolean) {
        binding.selected.isSelected = isSelected
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        binding.selected.updateLayoutParams<FrameLayout.LayoutParams> {
            val selectorLeftRightOffset = itemView.resources.getDimension(R.dimen.selection_left_right_offset).toInt()
            marginStart = binding.card.marginStart - selectorLeftRightOffset
            marginEnd = binding.card.marginEnd - selectorLeftRightOffset
            topMargin = itemView.resources.getDimension(R.dimen.card_block_extra_space_top).toInt()
            bottomMargin = 0
        }
        errorIcon.updateLayoutParams<FrameLayout.LayoutParams> {
            marginStart = binding.card.marginStart
        }
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }
}