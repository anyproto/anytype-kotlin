package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkArchiveBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Searchable.Field.Companion.DEFAULT_SEARCH_FIELD_KEY

class LinkToObjectArchive(
    val binding: ItemBlockObjectLinkArchiveBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    DecoratableViewHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    private val root = binding.root
    private val untitled = itemView.resources.getString(R.string.untitled)
    private val title = binding.pageTitle

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Archived,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        itemView.isSelected = item.isSelected

        val text = if (item.text.isNullOrEmpty()) {
            untitled
        } else {
            item.text
        }

        title.setText(text, TextView.BufferType.EDITABLE)

        applySearchHighlight(item)

        itemView.setOnClickListener { clicked(ListenerType.LinkToObjectArchived(item.id)) }
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                marginStart = item.indent * dimen(R.dimen.indent)
            }
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