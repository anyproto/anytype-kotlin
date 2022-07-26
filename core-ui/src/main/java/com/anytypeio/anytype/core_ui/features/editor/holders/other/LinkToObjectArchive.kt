package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkArchiveBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_utils.ext.VALUE_ROUNDED
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Searchable.Field.Companion.DEFAULT_SEARCH_FIELD_KEY
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class LinkToObjectArchive(
    val binding: ItemBlockObjectLinkArchiveBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    DecoratableViewHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    private val untitled = itemView.resources.getString(R.string.untitled)
    private val archived = itemView.resources.getString(R.string.archived)
    private val icon = binding.pageIcon
    private val emoji = binding.linkEmoji
    private val image = binding.linkImage
    private val title = binding.pageTitle
    private val guideline = binding.pageGuideline

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
        applyDefaultOffsets()
    }

    fun bind(
        item: BlockView.LinkToObject.Archived,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        itemView.isSelected = item.isSelected

        val text = if (item.text.isNullOrEmpty()) {
            SpannableStringBuilder("$untitled $archived").apply {
                setSpan(
                    Span.Keyboard(VALUE_ROUNDED),
                    untitled.length + 1,
                    untitled.length + 1 + archived.length,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        } else {
            SpannableStringBuilder("${item.text} $archived").apply {
                setSpan(
                    Span.Keyboard(VALUE_ROUNDED),
                    item.text!!.length + 1,
                    item.text!!.length + 1 + archived.length,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        }

        title.setText(text, TextView.BufferType.EDITABLE)

        applySearchHighlight(item)

        when {
            item.emoji != null -> {
                image.setImageDrawable(null)
                try {
                    Glide
                        .with(emoji)
                        .load(Emojifier.uri(item.emoji!!))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(emoji)
                } catch (e: Exception) {
                    icon.setImageResource(R.drawable.ic_block_page_without_emoji)
                }
            }
            item.image != null -> {
                image.visible()
                Glide
                    .with(image)
                    .load(item.image)
                    .centerInside()
                    .circleCrop()
                    .into(image)
            }
            item.isEmpty -> {
                icon.setImageResource(R.drawable.ic_block_empty_page)
                image.setImageDrawable(null)
            }
            else -> {
                icon.setImageResource(R.drawable.ic_block_page_without_emoji)
                image.setImageDrawable(null)
            }
        }

        itemView.setOnClickListener { clicked(ListenerType.LinkToObjectArchived(item.id)) }
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            guideline.setGuidelineBegin(
                item.indent * dimen(R.dimen.indent)
            )
        }
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == DEFAULT_SEARCH_FIELD_KEY }?.let { field ->
            applySearchHighlight(field, title)
        } ?: clearSearchHighlights()
    }

    private fun clearSearchHighlights() {
        title.editableText.removeSpans<SearchHighlightSpan>()
        title.editableText.removeSpans<SearchTargetHighlightSpan>()
    }

    private fun applySearchHighlight(field: BlockView.Searchable.Field, input: TextView) {
        input.editableText.removeSpans<SearchHighlightSpan>()
        input.editableText.removeSpans<SearchTargetHighlightSpan>()
        field.highlights.forEach { highlight ->
            input.editableText.setSpan(
                SearchHighlightSpan(),
                highlight.first,
                highlight.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (field.isTargeted) {
            input.editableText.setSpan(
                SearchTargetHighlightSpan(),
                field.target.first,
                field.target.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Archived) { "Expected a object link archive block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations) { rect ->
                binding.content.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = dimen(R.dimen.default_indent) + rect.left
                    marginEnd = dimen(R.dimen.dp_8) + rect.right
                    bottomMargin = rect.bottom
                    // TODO handle top and bottom offsets
                }
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