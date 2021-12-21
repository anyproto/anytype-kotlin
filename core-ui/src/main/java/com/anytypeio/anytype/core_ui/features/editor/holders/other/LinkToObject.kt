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
import kotlinx.android.synthetic.main.item_block_object_link.view.pageGuideline
import kotlinx.android.synthetic.main.item_block_object_link.view.pageTitle

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
    private val progress = itemView.progress
    private val syncing = itemView.syncing

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        itemView.isSelected = item.isSelected

        applyText(item)

        applySearchHighlight(item)

        applyImageOrEmoji(item)

        itemView.setOnClickListener { clicked(ListenerType.LinkToObject(item.id)) }

        bindLoading(item.isLoading)
    }

    private fun applyText(item: BlockView.LinkToObject.Default) {
        //title.enableReadMode()
        val sb = SpannableString(if (item.text.isNullOrEmpty()) untitled else item.text)
        sb.setSpan(UnderlineSpan(), 0, sb.length, 0)
        title.setText(sb, TextView.BufferType.EDITABLE)
    }

    private fun applyImageOrEmoji(item: BlockView.LinkToObject.Default) {
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

    private fun bindLoading(isLoading: Boolean) {
        if (isLoading) {
            objectIcon.invisible()
            title.invisible()
            progress.visible()
            syncing.visible()
        } else {
            progress.invisible()
            syncing.invisible()
            objectIcon.visible()
            title.visible()
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
        title.editableText.removeSpans<SearchHighlightSpan>()
        title.editableText.removeSpans<SearchTargetHighlightSpan>()
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Default) { "Expected a link to object block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
            if (payload.isLoadingChanged)
                bindLoading(item.isLoading)
            if (payload.isObjectTitleChanged)
                applyText(item)
            if (payload.isObjectIconChanged)
                applyImageOrEmoji(item)
        }
    }
}