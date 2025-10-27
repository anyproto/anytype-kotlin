package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockFileBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.readableFileSize
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import timber.log.Timber

class File(val binding: ItemBlockFileBinding) : Media(binding.root), DecoratableViewHolder {

    override val container = binding.root
    override val root: View = itemView
    override val clickContainer: View = binding.text
    private val icon = binding.graphic
    private val name = binding.text

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    init {
        clickContainer.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(item: BlockView.Media.File, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)

        if (item.size != null) {
            val size = item.size?.readableFileSize() ?: ""
            val spannable = SpannableString("${item.name}  $size")
            val start = item.name.length + 2
            val end = item.name.length + 2 + size.length
            spannable.setSpan(
                RelativeSizeSpan(0.87f),
                start,
                end,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            spannable.setSpan(
                ForegroundColorSpan(itemView.context.color(R.color.text_secondary)),
                start,
                end,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            name.post {
                name.text = spannable
            }
        } else {
            name.text = item.name
        }

        applySearchHighlight(item)

        val mimeIcon = item.mime.getMimeIcon(item.fileExt)
        icon.setImageResource(mimeIcon)

        applyBackground(item.background)
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY }
            ?.let { field ->
                applySearchHighlight(field)
            } ?: clearSearchHighlights()
    }

    private fun applySearchHighlight(field: BlockView.Searchable.Field) {
        val spannableText = SpannableStringBuilder("${name.text}")
        spannableText.removeSpans<SearchHighlightSpan>()
        spannableText.removeSpans<SearchTargetHighlightSpan>()

        val textLength = spannableText.length

        field.highlights.forEach { highlight ->
            // Validate span bounds before applying
            if (highlight.first >= 0 &&
                highlight.last <= textLength &&
                highlight.first <= highlight.last) {
                spannableText.setSpan(
                    SearchHighlightSpan(),
                    highlight.first,
                    highlight.last,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                Timber.w("Invalid highlight range [${highlight.first}, ${highlight.last}) for text length $textLength")
            }
        }

        if (field.isTargeted) {
            // Validate target span bounds before applying
            if (field.target.first >= 0 &&
                field.target.last <= textLength &&
                field.target.first <= field.target.last) {
                spannableText.setSpan(
                    SearchTargetHighlightSpan(),
                    field.target.first,
                    field.target.last,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                Timber.w("Invalid target range [${field.target.first}, ${field.target.last}) for text length $textLength")
            }
        }

        name.text = spannableText
    }

    private fun clearSearchHighlights() {
        val spannableText = SpannableStringBuilder("${name.text}")
        spannableText.removeSpans<SearchHighlightSpan>()
        spannableText.removeSpans<SearchTargetHighlightSpan>()
        name.text = spannableText
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.File.View(item.id))
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    override fun select(isSelected: Boolean) {
        itemView.isSelected = isSelected
    }

    override fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        super.processChangePayload(payloads, item)
        check(item is BlockView.Media.File)
        payloads.forEach { payload ->
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        Timber.w("Trying to apply decorations $decorations")
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