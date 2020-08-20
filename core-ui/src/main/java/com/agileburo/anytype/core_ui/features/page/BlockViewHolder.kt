package com.agileburo.anytype.core_ui.features.page

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.BuildConfig
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.SELECTION_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Payload
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_utils.const.MimeTypes
import com.agileburo.anytype.core_utils.ext.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.item_block_bookmark.view.*
import kotlinx.android.synthetic.main.item_block_file.view.*
import kotlinx.android.synthetic.main.item_block_picture.view.*
import kotlinx.android.synthetic.main.item_block_video.view.*
import timber.log.Timber
import android.text.format.Formatter as FileSizeFormatter

/**
 * Viewholder for rendering different type of blocks (i.e its UI-models).
 * @see BlockView
 * @see BlockAdapter
 */
open class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    interface IndentableHolder {
        fun indentize(item: BlockView.Indentable)
        fun processIndentChange(
            item: BlockView,
            payloads: List<Payload>
        ) {
            for (payload in payloads) {
                if (payload.isIndentChanged && item is BlockView.Indentable)
                    indentize(item)
            }
        }
    }

    class File(view: View) : BlockViewHolder(view), IndentableHolder {

        private val icon = itemView.fileIcon
        private val size = itemView.fileSize
        private val name = itemView.filename

        fun bind(
            item: BlockView.File.View,
            clicked: (ListenerType) -> Unit
        ) {
            indentize(item)
            name.text = item.name
            item.size?.let {
                size.text = FileSizeFormatter.formatFileSize(itemView.context, it)
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
            with(itemView) {
                isSelected = item.isSelected
                setOnClickListener { clicked(ListenerType.File.View(item.id)) }
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

        fun processChangePayload(payloads: List<Payload>, item: BlockView) {
            check(item is BlockView.File.View) { "Expected a file block, but was: $item" }
            payloads.forEach { payload ->
                if (payload.changes.contains(SELECTION_CHANGED)) {
                    itemView.isSelected = item.isSelected
                }
            }
        }
    }

    class Video(view: View) : BlockViewHolder(view), IndentableHolder {

        fun bind(
            item: BlockView.Video.View,
            clicked: (ListenerType) -> Unit
        ) {
            itemView.isSelected = item.isSelected
            itemView.playerView.findViewById<FrameLayout>(R.id.exo_controller).apply {
                setOnClickListener {
                    clicked(ListenerType.Video.View(item.id))
                }
                setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = { onBlockLongClick(itemView, it, clicked) }
                    )
                )
            }
            indentize(item)
            initPlayer(item.url)
        }

        private fun initPlayer(path: String) {
            itemView.playerView.visibility = View.VISIBLE
            val player = SimpleExoPlayer.Builder(itemView.context).build()
            val source = DefaultDataSourceFactory(
                itemView.context,
                Util.getUserAgent(itemView.context, BuildConfig.LIBRARY_PACKAGE_NAME)
            )
            val mediaSource =
                ProgressiveMediaSource.Factory(source).createMediaSource(Uri.parse(path))
            player.playWhenReady = false
            player.seekTo(0)
            player.prepare(mediaSource, false, false)
            itemView.playerView.player = player
        }

        override fun indentize(item: BlockView.Indentable) {
            itemView.indentize(
                indent = item.indent,
                defIndent = dimen(R.dimen.indent),
                margin = dimen(R.dimen.bookmark_default_margin_start)
            )
        }

        fun processChangePayload(payloads: List<Payload>, item: BlockView) {
            check(item is BlockView.Video.View) { "Expected a video block, but was: $item" }
            payloads.forEach { payload ->
                if (payload.changes.contains(SELECTION_CHANGED)) {
                    itemView.isSelected = item.isSelected
                }
            }
        }
    }

    class Bookmark(view: View) : BlockViewHolder(view), IndentableHolder {

        private val title = itemView.bookmarkTitle
        private val description = itemView.bookmarkDescription
        private val url = itemView.bookmarkUrl
        private val image = itemView.bookmarkImage
        private val logo = itemView.bookmarkLogo
        private val error = itemView.loadBookmarkPictureError
        private val card = itemView.bookmarkRoot
        private val root = itemView

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

        fun bind(
            item: BlockView.Bookmark.View,
            clicked: (ListenerType) -> Unit
        ) {
            indentize(item)
            select(item.isSelected)
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
            with(card) {
                setOnClickListener { clicked(ListenerType.Bookmark.View(item)) }
                setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = { onBlockLongClick(itemView, it, clicked) }
                    )
                )
            }
        }

        override fun indentize(item: BlockView.Indentable) {
            (root.layoutParams as ViewGroup.MarginLayoutParams).apply {
                val default = dimen(R.dimen.bookmark_default_margin_start)
                val extra = item.indent * dimen(R.dimen.indent)
                leftMargin = default + extra
            }
        }

        fun processChangePayload(
            payloads: List<Payload>,
            item: BlockView
        ) {
            check(item is BlockView.Bookmark.View) { "Expected a bookmark block, but was: $item" }
            payloads.forEach { payload ->
                if (payload.selectionChanged()) {
                    select(item.isSelected)
                }
            }
        }

        private fun select(isSelected: Boolean) {
            itemView.isSelected = isSelected
        }
    }

    class Picture(view: View) : BlockViewHolder(view), IndentableHolder {

        private val image = itemView.image
        private val error = itemView.error

        private val listener: RequestListener<Drawable> = object : RequestListener<Drawable> {

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                error.visible()
                Timber.e(e, "Error while loading picture")
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

        fun bind(
            item: BlockView.Picture.View,
            clicked: (ListenerType) -> Unit
        ) {
            indentize(item)
            with(itemView) {
                isSelected = item.isSelected
                setOnClickListener { clicked(ListenerType.Picture.View(item.id)) }
                setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = { onBlockLongClick(itemView, it, clicked) }
                    )
                )
            }
            Glide.with(image).load(item.url).listener(listener).into(image)
        }

        override fun indentize(item: BlockView.Indentable) {
            itemView.indentize(
                indent = item.indent,
                defIndent = dimen(R.dimen.indent),
                margin = dimen(R.dimen.bookmark_default_margin_start)
            )
        }

        fun processChangePayload(payloads: List<Payload>, item: BlockView) {
            check(item is BlockView.Picture.View) { "Expected a picture block, but was: $item" }
            payloads.forEach { payload ->
                if (payload.changes.contains(SELECTION_CHANGED)) {
                    itemView.isSelected = item.isSelected
                }
            }
        }
    }

    fun onBlockLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
        val rect = PopupExtensions.calculateRectInWindow(root)
        val dimensions = BlockDimensions(
            left = rect.left,
            top = rect.top,
            bottom = rect.bottom,
            right = rect.right,
            height = root.height,
            width = root.width
        )
        clicked(ListenerType.LongClick(target, dimensions))
    }

    companion object {
        const val HOLDER_PARAGRAPH = 0
        const val HOLDER_TITLE = 1
        const val HOLDER_PROFILE_TITLE = 35
        const val HOLDER_HEADER_ONE = 2
        const val HOLDER_HEADER_TWO = 3
        const val HOLDER_HEADER_THREE = 4
        const val HOLDER_CODE_SNIPPET = 5
        const val HOLDER_CHECKBOX = 6
        const val HOLDER_BULLET = 8
        const val HOLDER_NUMBERED = 9
        const val HOLDER_TOGGLE = 10
        const val HOLDER_PAGE = 13
        const val HOLDER_DIVIDER = 16
        const val HOLDER_HIGHLIGHT = 17

        const val HOLDER_VIDEO = 19
        const val HOLDER_VIDEO_PLACEHOLDER = 20
        const val HOLDER_VIDEO_UPLOAD = 21
        const val HOLDER_VIDEO_ERROR = 22

        const val HOLDER_PICTURE = 24
        const val HOLDER_PICTURE_PLACEHOLDER = 25
        const val HOLDER_PICTURE_UPLOAD = 26
        const val HOLDER_PICTURE_ERROR = 27

        const val HOLDER_BOOKMARK = 28
        const val HOLDER_BOOKMARK_PLACEHOLDER = 29
        const val HOLDER_BOOKMARK_ERROR = 30

        const val HOLDER_FILE = 31
        const val HOLDER_FILE_PLACEHOLDER = 32
        const val HOLDER_FILE_UPLOAD = 33
        const val HOLDER_FILE_ERROR = 34

        const val FOCUS_TIMEOUT_MILLIS = 16L
        const val KEYBOARD_SHOW_DELAY = 16L
    }
}
