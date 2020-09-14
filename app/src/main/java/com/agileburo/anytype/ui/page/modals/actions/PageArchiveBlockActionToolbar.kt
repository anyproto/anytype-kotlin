package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.Span
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.VALUE_ROUNDED
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.lang.Exception

class PageArchiveBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.PageArchive
    lateinit var icon: ImageView
    lateinit var emoji: ImageView
    lateinit var image: ImageView
    lateinit var title: TextInputWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_page_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {

        image = view.findViewById(R.id.linkImage)
        emoji = view.findViewById(R.id.linkEmoji)
        title = view.findViewById(R.id.pageTitle)

        title.enableReadMode()

        val untitled = getString(R.string.untitled)
        val archived = getString(R.string.archived)

        val text = if (block.text.isNullOrEmpty()) {
            SpannableStringBuilder("$untitled $archived").apply {
                setSpan(
                    Span.Keyboard(VALUE_ROUNDED),
                    untitled.length + 1,
                    untitled.length + 1 + archived.length,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        } else {
            SpannableStringBuilder("${block.text} $archived").apply {
                setSpan(
                    Span.Keyboard(VALUE_ROUNDED),
                    block.text!!.length + 1,
                    block.text!!.length + 1 + archived.length,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        }

        title.setTextColor(requireContext().color(R.color.page_archive_text_color))
        title.setText(text, TextView.BufferType.SPANNABLE)

        when {
            block.emoji != null -> {
                image.setImageDrawable(null)
                try {
                    Glide
                        .with(emoji)
                        .load(Emojifier.uri(block.emoji!!))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(emoji)
                } catch (e: Exception) {
                    icon.setImageResource(com.agileburo.anytype.core_ui.R.drawable.ic_block_page_without_emoji)
                }
            }
            block.image != null -> {
                image.visible()
                Glide
                    .with(image)
                    .load(block.image)
                    .centerInside()
                    .circleCrop()
                    .into(image)
            }
            block.isEmpty -> {
                icon.setImageResource(com.agileburo.anytype.core_ui.R.drawable.ic_block_empty_page)
                image.setImageDrawable(null)
            }
            else -> {
                icon.setImageResource(com.agileburo.anytype.core_ui.R.drawable.ic_block_page_without_emoji)
                image.setImageDrawable(null)
            }
        }

        setConstraints()
    }
}