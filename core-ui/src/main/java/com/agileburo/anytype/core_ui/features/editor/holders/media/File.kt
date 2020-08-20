package com.agileburo.anytype.core_ui.features.editor.holders.media

import android.text.format.Formatter
import android.view.View
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_utils.const.MimeTypes
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.indentize
import kotlinx.android.synthetic.main.item_block_file.view.*

class File(view: View) : Media(view) {

    override val root: View = itemView
    override val clickContainer: View = root
    private val icon = itemView.fileIcon
    private val size = itemView.fileSize
    private val name = itemView.filename

    fun bind(item: BlockView.Media.File, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        name.text = item.name
        item.size?.let {
            size.text = Formatter.formatFileSize(itemView.context, it)
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