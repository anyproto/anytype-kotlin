package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Searchable.Field.Companion.DEFAULT_SEARCH_FIELD_KEY
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class LinkToObject(
    val binding: ItemBlockObjectLinkBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    DecoratableViewHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    private val root = binding.root
    private val container = binding.container
    private val untitled = itemView.resources.getString(R.string.untitled)
    private val objectIcon = binding.objectIconWidget
    private val objectIconContainer = binding.iconObjectContainer
    private val title = binding.text

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    override val decoratableContainer = binding.decorationContainer

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updatePadding(
                left = dimen(R.dimen.default_document_item_padding_start),
                right = dimen(R.dimen.default_document_item_padding_end)
            )
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                bottomMargin = dimen(R.dimen.default_graphic_text_root_margin_bottom)
                topMargin = dimen(R.dimen.default_graphic_text_root_margin_top)
            }
            container.updatePadding(
                left = dimen(R.dimen.default_graphic_text_container_padding_start),
                right = dimen(R.dimen.default_graphic_text_container_padding_end)
            )
        }
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
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                marginStart = item.indent * dimen(R.dimen.indent)
            }
        }
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

    private fun applyBackground(background: String?) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.setBlockBackgroundColor(background)
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations) { rect ->
                binding.container.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = dimen(R.dimen.default_indent) + rect.left
                    marginEnd = dimen(R.dimen.dp_8) + rect.right
                    bottomMargin = rect.bottom
                    // TODO handle top and bottom offsets
                }
            }
        }
    }
}