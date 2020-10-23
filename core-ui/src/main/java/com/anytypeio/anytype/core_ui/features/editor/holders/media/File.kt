package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.text.Spannable
import android.text.SpannableString
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.ListenerType
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.core_utils.ext.removeSpans
import kotlinx.android.synthetic.main.item_block_file.view.*

class File(view: View) : Media(view) {

    override val root: View = itemView
    override val clickContainer: View = itemView.filename
    private val icon = itemView.fileIcon
    private val name = itemView.filename

    fun bind(item: BlockView.Media.File, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        name.enableReadMode()
        if (item.size != null && item.name != null) {
            val size = Formatter.formatFileSize(itemView.context, item.size)
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
                ForegroundColorSpan(itemView.context.color(R.color.block_file_size_text_color)),
                start,
                end,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            name.setText(spannable, TextView.BufferType.SPANNABLE)
        } else {
            name.setText(item.name, TextView.BufferType.SPANNABLE)
        }

        applySearchHighlight(item)

        when (item.mime?.let { MimeTypes.category(it) }) {
            MimeTypes.Category.PDF -> icon.setImageResource(R.drawable.ic_mime_pdf)
            MimeTypes.Category.IMAGE -> icon.setImageResource(R.drawable.ic_mime_image)
            MimeTypes.Category.AUDIO -> icon.setImageResource(R.drawable.ic_mime_music)
            MimeTypes.Category.TEXT -> icon.setImageResource(R.drawable.ic_mime_text)
            MimeTypes.Category.VIDEO -> icon.setImageResource(R.drawable.ic_mime_video)
            MimeTypes.Category.ARCHIVE -> icon.setImageResource(R.drawable.ic_mime_archive)
            MimeTypes.Category.TABLE -> icon.setImageResource(R.drawable.ic_mime_table)
            MimeTypes.Category.PRESENTATION -> icon.setImageResource(R.drawable.ic_mime_presentation)
            MimeTypes.Category.OTHER -> icon.setImageResource(R.drawable.ic_mime_other)
        }
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY }
            ?.let { field ->
                applySearchHighlight(field, name)
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
        name.editableText.removeSpans<SearchHighlightSpan>()
        name.editableText.removeSpans<SearchTargetHighlightSpan>()
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.File.View(item.id))
    }

    override fun indentize(item: BlockView.Indentable) {
        itemView.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.file_default_margin_start)
        )
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
}