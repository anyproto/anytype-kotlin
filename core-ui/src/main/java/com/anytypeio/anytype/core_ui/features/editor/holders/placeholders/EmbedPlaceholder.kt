package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockEmbedBinding
import com.anytypeio.anytype.core_ui.extensions.canOpenEmbedExternally
import com.anytypeio.anytype.core_ui.extensions.toEmbedIconResource
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
        timber.log.Timber.d("EmbedPlaceholder: bind called with isSelected=${item.isSelected} for id=${item.id}")
        select(item.isSelected)

        val trimmedText = item.text.trim()
        val canOpen = item.processor.canOpenEmbedExternally() && trimmedText.isNotEmpty()

        // Determine the appropriate message based on state
        binding.embedTitle.text = when {
            trimmedText.isEmpty() -> {
                // No URL provided - embed is empty
                binding.root.context.getString(R.string.embed_is_empty, item.processor)
            }
            item.processor.canOpenEmbedExternally() -> {
                // Can be opened in external app or browser
                binding.root.context.getString(R.string.embed_opens_externally, item.processor)
            }
            else -> {
                // Not available on mobile (e.g., Mermaid, Chart, etc.)
                binding.root.context.getString(R.string.embed_content_not_available, item.processor)
            }
        }

        // Set the embed icon based on processor type
        binding.embedIcon.setImageResource(item.processor.toEmbedIconResource())

        // Show/hide Open button based on whether embed can be opened
        if (canOpen) {
            binding.openButton.visible()
            binding.openButton.setOnClickListener {
                clicked(ListenerType.Embed.Click(item))
            }
        } else {
            binding.openButton.gone()
        }

        // Keep root clickable for selection mode
        binding.root.setOnClickListener {
            clicked(ListenerType.Embed.Click(item))
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Embed,
        clicked: (ListenerType) -> Unit
    ) {
        timber.log.Timber.d("EmbedPlaceholder: processChangePayload called for id=${item.id}, payloads=${payloads.map { it.javaClass.simpleName }}")
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                timber.log.Timber.d("EmbedPlaceholder: Selection changed detected, setting isSelected=${item.isSelected}")
                select(item.isSelected)
            }
        }
    }

    private fun select(isSelected: Boolean) {
        timber.log.Timber.d("EmbedPlaceholder: select called with isSelected=$isSelected")
        card.isSelected = isSelected
        // Force refresh the selectors
        card.refreshDrawableState()
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        binding.containerWithBackground.setBackgroundColor(
            binding.containerWithBackground.resources.veryLight(decorations.last().background, 0)
        )
    }
}
