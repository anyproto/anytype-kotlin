package com.agileburo.anytype.core_ui.features.editor.holders.media

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.gone
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.item_block_bookmark.view.*

class Bookmark(view: View) : Media(view) {

    override val root: View = itemView
    private val title = itemView.bookmarkTitle
    private val description = itemView.bookmarkDescription
    private val url = itemView.bookmarkUrl
    private val image = itemView.bookmarkImage
    private val logo = itemView.bookmarkLogo
    private val error = itemView.loadBookmarkPictureError
    private val card = itemView.bookmarkRoot
    override val clickContainer: View = card

    private val listener: RequestListener<Drawable> = object : RequestListener<Drawable> {

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            error.visible()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            error.invisible()
            return false
        }
    }

    fun bind(item: BlockView.Media.Bookmark, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        title.text = item.title
        description.text = item.description
        url.text = item.url
        if (item.imageUrl != null) {
            image.visible()
            Glide.with(image)
                .load(item.imageUrl)
                .centerCrop()
                .listener(listener)
                .into(image)
        } else {
            image.gone()
        }
        if (item.faviconUrl != null) {
            logo.visible()
            Glide.with(logo)
                .load(item.faviconUrl)
                .listener(listener)
                .into(logo)
        } else {
            logo.gone()
        }
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Bookmark.View(item = item as BlockView.Media.Bookmark))
    }

    override fun indentize(item: BlockView.Indentable) {
        (root.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val default = dimen(R.dimen.bookmark_default_margin_start)
            val extra = item.indent * dimen(R.dimen.indent)
            leftMargin = default + extra
        }
    }

    override fun select(isSelected: Boolean) {
        itemView.isSelected = isSelected
    }
}