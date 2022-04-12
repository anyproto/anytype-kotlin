package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkBinding
import com.anytypeio.anytype.core_ui.extensions.lighter
import com.anytypeio.anytype.core_ui.features.editor.*
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Searchable.Field.Companion.DEFAULT_SEARCH_FIELD_KEY
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import timber.log.Timber

class LinkToObject(
    val binding: ItemBlockObjectLinkBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    private val root = binding.root
    private val container = binding.container
    private val untitled = itemView.resources.getString(R.string.untitled)
    private val objectIcon = binding.objectIconWidget
    private val objectIconContainer = binding.iconObjectContainer
    private val title = binding.text
    private val guideline = binding.guideline

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

        container.isSelected = item.isSelected

        applyText(item)

        applySearchHighlight(item)

        applyImageOrEmoji(item)

        applyBackground(item.backgroundColor)

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
                container.isSelected = item.isSelected
            }
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
            if (payload.isObjectTitleChanged)
                applyText(item)
            if (payload.isObjectIconChanged)
                applyImageOrEmoji(item)
            if (payload.isBackgroundColorChanged)
                applyBackground(item.backgroundColor)
        }
    }

    private fun applyBackground(
        background: String?
    ) {
        Timber.d("Setting background color: $background")
        if (!background.isNullOrEmpty()) {
            val value = ThemeColor.values().find { value -> value.title == background }
            if (value != null && value != ThemeColor.DEFAULT) {
                root.setBackgroundColor(itemView.resources.lighter(value, 0))
            } else {
                Timber.e("Could not find value for background color: $background, setting background to null")
                root.setBackgroundColor(0)
            }
        } else {
            Timber.d("Background color is null, setting background to null")
            root.setBackgroundColor(0)
        }
    }
}