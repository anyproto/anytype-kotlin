package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockEmbedBinding
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.google.android.material.card.MaterialCardView

class EmbedPlaceholder(
    val binding: ItemBlockEmbedBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.DragAndDropHolder,
    BlockViewHolder.IndentableHolder,
    SupportCustomTouchProcessor,
    DecoratableCardViewHolder {

    private val card: MaterialCardView = binding.card

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    override val decoratableCard: View
        get() = binding.card

    override val editorTouchProcessor = EditorTouchProcessor(
        touchSlop = ViewConfiguration.get(itemView.context).scaledTouchSlop,
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(item: BlockView.Embed, clicked: (ListenerType) -> Unit) {
        select(item.isSelected)
        binding.embedMessage.text = binding.root.context.getString(
            R.string.embed_content_not_available,
            item.processor
        )
        if (item.text.isNotEmpty()) {
            binding.embedUrl.visible()
            binding.embedUrl.text = item.text
        } else {
            binding.embedUrl.gone()
        }
        binding.root.setOnClickListener {
            clicked(ListenerType.Embed.Click(item))
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Embed,
        clicked: (ListenerType) -> Unit
    ) {
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    private fun select(isSelected: Boolean) {
        timber.log.Timber.d("EmbedPlaceholder: select called with isSelected=$isSelected")
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
