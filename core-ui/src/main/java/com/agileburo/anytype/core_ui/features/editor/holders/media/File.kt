package com.agileburo.anytype.core_ui.features.editor.holders.media

import android.text.SpannableString
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.TextView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_utils.const.MimeTypes
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.indentize
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
            name.setText(item.name)
        }

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
}