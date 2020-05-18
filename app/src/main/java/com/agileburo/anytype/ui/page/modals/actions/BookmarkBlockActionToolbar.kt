package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_PLACEHOLDER
import com.bumptech.glide.Glide

class BookmarkBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Bookmark

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout(): Int = when (block.getViewType()) {
        HOLDER_BOOKMARK -> R.layout.item_block_bookmark
        HOLDER_BOOKMARK_ERROR -> R.layout.item_block_bookmark_error
        HOLDER_BOOKMARK_PLACEHOLDER -> R.layout.item_block_bookmark_placeholder
        else -> throw RuntimeException("No layout for bookmark block with type ${block.getViewType()}")
    }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) =
        when (block.getViewType()) {
            HOLDER_BOOKMARK -> initBookmark(view)
            HOLDER_BOOKMARK_ERROR -> initError(view)
            HOLDER_BOOKMARK_PLACEHOLDER -> initPlaceholder(view)
            else -> throw RuntimeException("No layout for bookmark block with type ${block.getViewType()}")
        }

    private fun initPlaceholder(view: View) {
        view.findViewById<ConstraintLayout>(R.id.bookmarkPlaceholderRoot)
            .updateLayoutParams<FrameLayout.LayoutParams> {
                this.apply {
                    rightMargin = 0
                }
            }
    }

    private fun initBookmark(view: View) {
        val item = block as BlockView.Bookmark.View
        view.findViewById<TextView>(R.id.bookmarkTitle).apply {
            text = item.title
        }
        view.findViewById<TextView>(R.id.bookmarkDescription).apply {
            text = item.description
        }
        view.findViewById<TextView>(R.id.bookmarkUrl).apply {
            text = item.url
        }
        item.imageUrl?.let { url ->
            view.findViewById<ImageView>(R.id.bookmarkImage).apply {
                Glide.with(this)
                    .load(url)
                    .into(this)
            }
        }
        item.faviconUrl?.let { url ->
            view.findViewById<ImageView>(R.id.bookmarkLogo).apply {
                Glide.with(this)
                    .load(url)
                    .into(this)
            }
        }

        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                this.apply {
                    rightMargin = 0
                    topMargin = 0
                    bottomMargin = 0
                    topMargin = 0
                }
            }
            setPadding(0, 0, 0, 0)
        }

        view.findViewById<FrameLayout>(R.id.block_container)
            .setPadding(0, 0, 0, 0)
    }

    private fun initError(view: View) {
        val item = block as BlockView.Bookmark.Error
        view.findViewById<TextView>(R.id.errorBookmarkUrl).apply {
            text = item.url
        }
        view.findViewById<FrameLayout>(R.id.bookmarkErrorRoot)
            .updateLayoutParams<FrameLayout.LayoutParams> {
                this.apply {
                    rightMargin = 0
                }
            }
    }
}