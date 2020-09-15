package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.text.SpannableString
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.const.MimeTypes

class FileBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Media.File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_file
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<ConstraintLayout>(R.id.root)
            .updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
                leftMargin = 0
                rightMargin = 0
            }
        initFile(view)
    }

    private fun initFile(view: View) {
        val item = block

        view.findViewById<TextInputWidget>(R.id.filename).apply {
            enableReadMode()
            val name = block.name
            val blockSize = block.size
            if (blockSize != null && name != null) {
                val size = Formatter.formatFileSize(context, blockSize)
                val spannable = SpannableString("${item.name}  $size")
                val start = name.length + 2
                val end = name.length + 2 + size.length
                spannable.setSpan(
                    RelativeSizeSpan(0.87f),
                    start,
                    end,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
                spannable.setSpan(
                    ForegroundColorSpan(context.color(com.agileburo.anytype.core_ui.R.color.block_file_size_text_color)),
                    start,
                    end,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
                setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                setText(item.name)
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
        setConstraints()
    }
}