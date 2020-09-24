package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_block_page.view.*
import timber.log.Timber

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

        title.enableReadMode()
        val text = if (item.text.isNullOrEmpty()) untitled else item.text
        title.setText(text)

        when {
            item.emoji != null -> {
                image.setImageDrawable(null)
                try {
                    Glide
                        .with(emoji)
                        .load(Emojifier.uri(item.emoji))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(emoji)
                } catch (e: Throwable) {
                    Timber.e(e, "Error while setting emoji icon for: ${item.emoji}")
                }
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