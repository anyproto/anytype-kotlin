package com.agileburo.anytype.core_ui.features.editor.holders.other

import android.view.View
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_block_page.view.*

class Page(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder, SupportNesting {

    private val untitled = itemView.resources.getString(R.string.untitled)
    private val icon = itemView.pageIcon
    private val emoji = itemView.linkEmoji
    private val image = itemView.linkImage
    private val title = itemView.pageTitle
    private val guideline = itemView.pageGuideline

    fun bind(
        item: BlockView.Page,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        itemView.isSelected = item.isSelected

        title.text = if (item.text.isNullOrEmpty()) untitled else item.text

        when {
            item.emoji != null -> {
                image.setImageDrawable(null)
                Glide
                    .with(emoji)
                    .load(Emojifier.uri(item.emoji))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(emoji)
            }
            item.image != null -> {
                image.visible()
                Glide
                    .with(image)
                    .load(item.image)
                    .centerInside()
                    .circleCrop()
                    .into(image)
            }
            item.isEmpty -> {
                icon.setImageResource(R.drawable.ic_block_empty_page)
                image.setImageDrawable(null)
            }
            else -> {
                icon.setImageResource(R.drawable.ic_block_page_without_emoji)
                image.setImageDrawable(null)
            }
        }

        title.setOnClickListener { clicked(ListenerType.Page(item.id)) }
        title.setOnLongClickListener(
            EditorLongClickListener(
                t = item.id,
                click = { onBlockLongClick(itemView, it, clicked) }
            )
        )
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(
            item.indent * dimen(R.dimen.indent)
        )
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Page) { "Expected a page block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
        }
    }
}