package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.features.editor.*
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Searchable.Field.Companion.DEFAULT_SEARCH_FIELD_KEY
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.android.synthetic.main.item_block_object_link.view.*

class LinkToObject(view: View) :
    BlockViewHolder(view),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    private val untitled = itemView.resources.getString(R.string.untitled)
    private val objectIcon = itemView.objectIconWidget
    private val objectIconContainer = itemView.iconObjectContainer
    private val title = itemView.pageTitle
    private val guideline = itemView.pageGuideline

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Text,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        itemView.isSelected = item.isSelected

        applyText(item)

        applySearchHighlight(item)

        applyImageOrEmoji(item)

        itemView.setOnClickListener { clicked(ListenerType.LinkToObject(item.id)) }
    }

    private fun applyText(item: BlockView.LinkToObject.Default.Text) {
        val name = item.text
        when {
            name == null -> {
                title.text = null
                title.gone()
            }
            name.isEmpty() -> {
                val sb = SpannableString(untitled)
                sb.setSpan(UnderlineSpan(), 0, sb.length, 0)
                title.visible()
                title.setText(sb, TextView.BufferType.EDITABLE)
            }
            else -> {
                val sb = SpannableString(name)
                sb.setSpan(UnderlineSpan(), 0, sb.length, 0)
                title.visible()
                title.setText(sb, TextView.BufferType.EDITABLE)
            }
        }
    }

    private fun applyImageOrEmoji(item: BlockView.LinkToObject.Default.Text) {
        when (item.icon) {
            ObjectIcon.None -> {
                objectIconContainer.gone()
            }
            else -> {
                objectIconContainer.visible()
                objectIcon.setIcon(item.icon)
            }
        }
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == DEFAULT_SEARCH_FIELD_KEY }?.let { field ->
            applySearchHighlight(field, title)
        } ?: clearSearchHighlights()
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

    private fun clearSearchHighlights() {
        title.editableText?.removeSpans<SearchHighlightSpan>()
        title.editableText?.removeSpans<SearchTargetHighlightSpan>()
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Default.Text) { "Expected a link to object block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
                applyImageOrEmoji(item)
            }
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
            if (payload.isObjectTitleChanged)
                applyText(item)
            if (payload.isObjectIconChanged)
                applyImageOrEmoji(item)
        }
    }
}