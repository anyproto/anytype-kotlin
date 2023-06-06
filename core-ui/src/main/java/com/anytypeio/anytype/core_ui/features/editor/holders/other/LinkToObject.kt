package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
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
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_models.ThemeColor
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
    SupportCustomTouchProcessor {

    private val root = binding.root
    private val container = binding.container
    private val untitled = itemView.resources.getString(R.string.untitled)
    val objectIcon = binding.objectIconWidget
    private val objectIconContainer = binding.iconObjectContainer
    private val title = binding.text
    private val description = binding.tvDescription
    private val objectType = binding.tvObjectType

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    override val decoratableContainer = binding.decorationContainer

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Text,
        clicked: (ListenerType) -> Unit
    ) {
        applySelectedState(item)
        applyText(item)
        applyDescription(item)
        applyObjectType(item)
        applySearchHighlight(item)
        applyImageOrEmoji(item)
        itemView.setOnClickListener { clicked(ListenerType.LinkToObject(item.id)) }
    }

    private fun applySelectedState(item: BlockView.LinkToObject.Default.Text) {
        container.isSelected = item.isSelected
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
                if (item.icon !is ObjectIcon.None) {
                    val firstLineMargin =
                        itemView.resources.getDimensionPixelOffset(R.dimen.default_graphic_text_text_first_line_margin_start)
                    sb.setSpan(
                        LeadingMarginSpan.Standard(firstLineMargin, 0), 0, sb.length, 0
                    )
                }
                title.visible()
                title.text = sb
            }
            else -> {
                val sb = SpannableString(name)
                if (item.icon !is ObjectIcon.None) {
                    val firstLineMargin =
                        itemView.resources.getDimensionPixelOffset(R.dimen.default_graphic_text_text_first_line_margin_start)
                    sb.setSpan(
                        LeadingMarginSpan.Standard(firstLineMargin, 0), 0, sb.length, 0
                    )
                }
                title.visible()
                title.text = sb
            }
        }
    }

    private fun applyDescription(item: BlockView.LinkToObject.Default.Text) {
        if (item.description.isNullOrBlank()) {
            description.gone()
        } else {
            description.visible()
            description.text = item.description
        }
    }

    private fun applyObjectType(item: BlockView.LinkToObject.Default.Text) {
        if (item.objectTypeName.isNullOrBlank()) {
            objectType.gone()
        } else {
            objectType.visible()
            objectType.text = item.objectTypeName
        }
    }

    private fun applyImageOrEmoji(item: BlockView.LinkToObject.Default.Text) {
        when (item.icon) {
            ObjectIcon.None -> {
                objectIconContainer.gone()
                applyText(item)
            }
            else -> {
                objectIconContainer.visible()
                objectIcon.setIcon(item.icon)
                applyText(item)
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

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Default.Text) { "Expected a link to object block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                applySelectedState(item)
            }
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
            if (payload.isObjectTitleChanged) {
                applyText(item)
            }
            if (payload.isObjectIconChanged) {
                applyImageOrEmoji(item)
            }
            if (payload.isObjectDescriptionChanged) {
                applyDescription(item)
            }
            if (payload.isObjectTypeChanged) {
                applyObjectType(item)
            }
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        decoratableContainer.decorate(decorations) { rect ->
            binding.container.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.dp_8) + rect.left
                marginEnd = dimen(R.dimen.dp_8) + rect.right
                bottomMargin = if (rect.bottom > 0) {
                    rect.bottom
                } else {
                    dimen(R.dimen.dp_2)
                }
            }
        }
    }
}