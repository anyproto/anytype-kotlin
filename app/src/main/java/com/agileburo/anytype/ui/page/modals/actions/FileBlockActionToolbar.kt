package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_utils.const.MimeTypes

class FileBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = when (block.getViewType()) {
        BlockViewHolder.HOLDER_FILE -> R.layout.item_block_file_preview
        BlockViewHolder.HOLDER_FILE_PLACEHOLDER -> R.layout.item_block_file_placeholder_preview
        BlockViewHolder.HOLDER_FILE_ERROR -> R.layout.item_block_file_error_preview
        else -> R.layout.item_block_file_uploading_preview
    }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) =
        when (block.getViewType()) {
            BlockViewHolder.HOLDER_FILE -> initFile(view)
            else -> Unit
        }

    private fun initFile(view: View) {
        val item = block as BlockView.File.View

        view.findViewById<TextView>(R.id.filename).apply {
            text = item.name
        }
        view.findViewById<TextView>(R.id.fileSize).apply {
            item.size?.let {
                text = Formatter.formatFileSize(context, it)
            }
        }
        view.findViewById<ImageView>(R.id.fileIcon).apply {
            when (item.mime?.let { MimeTypes.category(it) }) {
                MimeTypes.Category.PDF -> setImageResource(R.drawable.ic_mime_pdf)
                MimeTypes.Category.IMAGE -> setImageResource(R.drawable.ic_mime_image)
                MimeTypes.Category.AUDIO -> setImageResource(R.drawable.ic_mime_music)
                MimeTypes.Category.TEXT -> setImageResource(R.drawable.ic_mime_text)
                MimeTypes.Category.VIDEO -> setImageResource(R.drawable.ic_mime_video)
                MimeTypes.Category.ARCHIVE -> setImageResource(R.drawable.ic_mime_archive)
                MimeTypes.Category.TABLE -> setImageResource(R.drawable.ic_mime_table)
                MimeTypes.Category.PRESENTATION -> setImageResource(R.drawable.ic_mime_presentation)
                MimeTypes.Category.OTHER -> setImageResource(R.drawable.ic_mime_other)
            }
        }
    }
}