package com.agileburo.anytype.core_ui.features.editor.holders

import android.view.View
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.indentize
import kotlinx.android.synthetic.main.item_block_bookmark_placeholder.view.*

class FilePlaceholder(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    fun bind(
        item: BlockView.File.Placeholder,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        with(itemView) {
            isSelected = item.isSelected
            setOnClickListener { clicked(ListenerType.File.Placeholder(item.id)) }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(itemView, it, clicked) }
                )
            )
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        itemView.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.bookmark_default_margin_start)
        )
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.File.Placeholder) { "Expected a file placeholder block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
        }
    }
}

class VideoPlaceholder(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    fun bind(
        item: BlockView.Video.Placeholder,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        with(itemView) {
            isSelected = item.isSelected
            setOnClickListener { clicked(ListenerType.Video.Placeholder(item.id)) }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(itemView, it, clicked) }
                )
            )
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        itemView.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.bookmark_default_margin_start)
        )
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Video.Placeholder) { "Expected a video placeholder block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
        }
    }
}

class BookmarkPlaceholder(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    private val root = itemView.bookmarkPlaceholderRoot

    fun bind(
        item: BlockView.Bookmark.Placeholder,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(root) {
            setOnClickListener {
                clicked(ListenerType.Bookmark.Placeholder(item.id))
            }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(itemView, it, clicked) }
                )
            )
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        root.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.bookmark_default_margin_start)
        )
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView
    ) {
        check(item is BlockView.Bookmark.Placeholder) { "Expected a bookmark placeholder block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.selectionChanged()) {
                select(item.isSelected)
            }
        }
    }

    private fun select(isSelected: Boolean) {
        root.isSelected = isSelected
    }
}

class ImagePlaceholder(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    fun bind(
        item: BlockView.Picture.Placeholder,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        with(itemView) {
            isSelected = item.isSelected
            setOnClickListener { clicked(ListenerType.Picture.Placeholder(item.id)) }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(itemView, it, clicked) }
                )
            )
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        itemView.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.bookmark_default_margin_start)
        )
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Picture.Placeholder) { "Expected a picture placeholder block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
        }
    }
}