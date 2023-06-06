package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaPlaceholderBinding
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.google.android.material.card.MaterialCardView

abstract class MediaPlaceholder(
    val binding: ItemBlockMediaPlaceholderBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.DragAndDropHolder,
    BlockViewHolder.IndentableHolder,
    SupportCustomTouchProcessor,
    DecoratableCardViewHolder {

    protected val root: View = binding.root
    protected val card: MaterialCardView = binding.card
    protected val title: TextView = binding.title

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    override val decoratableCard: View
        get() = binding.card

    abstract fun placeholderClick(target: String, clicked: (ListenerType) -> Unit)
    abstract fun setup()

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
        setup()
        select(item.isSelected)
        with(itemView) {
            setOnClickListener { placeholderClick(item.id, clicked) }
        }
    }

    open fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.MediaPlaceholder) { "Expected a media placeholder, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    private fun select(isSelected: Boolean) {
        binding.selected.isSelected = isSelected
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        binding.selected.applySelectorOffset<FrameLayout.LayoutParams>(
            content = binding.card,
            res = itemView.resources
        )
        card.setCardBackgroundColor(
            card.resources.veryLight(decorations.last().background, 0)
        )
    }
}