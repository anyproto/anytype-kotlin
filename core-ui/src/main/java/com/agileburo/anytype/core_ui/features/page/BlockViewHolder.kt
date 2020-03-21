package com.agileburo.anytype.core_ui.features.page

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.view.View
import android.widget.TextView.BufferType
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.BuildConfig
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.*
import com.agileburo.anytype.core_ui.extensions.*
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.NUMBER_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Payload
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.const.MimeTypes
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
import kotlinx.android.synthetic.main.item_block_bulleted.view.*
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*
import kotlinx.android.synthetic.main.item_block_contact.view.*
import kotlinx.android.synthetic.main.item_block_file.view.*
import kotlinx.android.synthetic.main.item_block_header_one.view.*
import kotlinx.android.synthetic.main.item_block_header_three.view.*
import kotlinx.android.synthetic.main.item_block_header_two.view.*
import kotlinx.android.synthetic.main.item_block_highlight.view.*
import kotlinx.android.synthetic.main.item_block_numbered.view.*
import kotlinx.android.synthetic.main.item_block_page.view.*
import kotlinx.android.synthetic.main.item_block_picture.view.*
import kotlinx.android.synthetic.main.item_block_task.view.*
import kotlinx.android.synthetic.main.item_block_text.view.*
import kotlinx.android.synthetic.main.item_block_title.view.*
import kotlinx.android.synthetic.main.item_block_toggle.view.*
import kotlinx.android.synthetic.main.item_block_video.view.*
import kotlinx.android.synthetic.main.item_block_video_error.view.*
import kotlinx.android.synthetic.main.item_block_video_error.view.icMore
import android.text.format.Formatter as FileSizeFormatter

/**
 * Viewholder for rendering different type of blocks (i.e its UI-models).
 * @see BlockView
 * @see BlockAdapter
 */
sealed class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class Paragraph(view: View) : BlockViewHolder(view), TextHolder {

        override val root: View = itemView
        override val content: TextInputWidget = itemView.textContent

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Paragraph,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            content.clearTextWatchers()

            if (item.marks.isLinksPresent()) {
                content.setLinksClickable()
            }

            content.setText(item.toSpannable(), BufferType.SPANNABLE)

            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }

            setFocus(item)

            setupTextWatcher(onTextChanged, item)

            content.setOnFocusChangeListener { _, focused ->
                item.focused = focused
                onFocusChanged(item.id, focused)
            }
            content.selectionDetector = { onSelectionChanged(item.id, it) }
        }
    }

    class Title(view: View) : BlockViewHolder(view), TextHolder {

        private val icon = itemView.logo

        override val root: View = itemView
        override val content: TextInputWidget = itemView.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onPageIconClicked: () -> Unit
        ) {
            content.clearTextWatchers()
            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }
            content.setText(item.text, BufferType.EDITABLE)
            setupTextWatcher(onTextChanged, item)
            icon.text = item.emoji ?: EMPTY_EMOJI
            icon.setOnClickListener { onPageIconClicked() }
        }

        fun processPayloads(
            payloads: List<Payload>,
            item: BlockView.Title
        ) {
            payloads.forEach { payload ->
                if (payload.changes.contains(TEXT_CHANGED)) {
                    content.pauseTextWatchers {
                        if (content.text.toString() != item.text) {
                            content.setText(item.text, BufferType.EDITABLE)
                        }
                    }
                }
            }
        }

        override fun processChangePayload(payloads: List<Payload>, item: BlockView) {}
        override fun enableBackspaceDetector(
            onEmptyBlockBackspaceClicked: () -> Unit,
            onNonEmptyBlockBackspaceClicked: () -> Unit
        ) = Unit

        companion object {
            private const val EMPTY_EMOJI = ""
        }
    }

    class HeaderOne(view: View) : BlockViewHolder(view), TextHolder {

        private val header = itemView.headerOne
        override val root: View = itemView
        override val content: TextInputWidget
            get() = header

        fun bind(
            item: BlockView.HeaderOne,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            header.clearTextWatchers()

            header.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }
            header.setText(item.text)

            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }

            header.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )
        }
    }

    class HeaderTwo(view: View) : BlockViewHolder(view), TextHolder {

        private val header = itemView.headerTwo
        override val content: TextInputWidget
            get() = header
        override val root: View = itemView

        fun bind(
            item: BlockView.HeaderTwo,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            header.clearTextWatchers()

            header.setText(item.text)

            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }

            header.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            header.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }
        }
    }

    class HeaderThree(view: View) : BlockViewHolder(view), TextHolder {

        private val header = itemView.headerThree
        override val content: TextInputWidget
            get() = header
        override val root: View = itemView

        fun bind(
            item: BlockView.HeaderThree,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            header.clearTextWatchers()

            header.setText(item.text)

            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }

            header.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }

            header.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )
        }
    }

    class Code(view: View) : BlockViewHolder(view) {

        private val code = itemView.snippet

        fun bind(item: BlockView.Code) {
            code.text = item.snippet
        }
    }

    class Checkbox(view: View) : BlockViewHolder(view), TextHolder {

        private val checkbox = itemView.checkboxIcon
        override val content: TextInputWidget = itemView.checkboxContent
        override val root: View = itemView

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Checkbox,
            onTextChanged: (String, Editable) -> Unit,
            onCheckboxClicked: (String) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            content.clearTextWatchers()

            checkbox.isSelected = item.isChecked

            if (item.marks.isLinksPresent()) {
                content.setLinksClickable()
            }

            if (item.marks.isNotEmpty())
                content.setText(item.toSpannable(), BufferType.SPANNABLE)
            else
                content.setText(item.text)

            setFocus(item)

            checkbox.setOnClickListener {
                checkbox.isSelected = !checkbox.isSelected
                onCheckboxClicked(item.id)
            }

            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }

            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            content.selectionDetector = { onSelectionChanged(item.id, it) }
        }
    }

    class Task(view: View) : BlockViewHolder(view) {

        private val checkbox = itemView.taskIcon
        private val content = itemView.taskContent

        fun bind(item: BlockView.Task) {
            checkbox.isSelected = item.checked
            content.text = item.text
        }
    }

    class Bulleted(view: View) : BlockViewHolder(view), TextHolder {

        private val bullet = itemView.bullet
        override val content: TextInputWidget = itemView.bulletedListContent
        override val root: View = itemView

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Bulleted,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            content.clearTextWatchers()

            if (item.marks.isLinksPresent()) {
                content.setLinksClickable()
            }

            if (item.marks.isNotEmpty())
                content.setText(item.toSpannable(), BufferType.SPANNABLE)
            else
                content.setText(item.text)

            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }

            setFocus(item)

            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }

            content.selectionDetector = { onSelectionChanged(item.id, it) }
        }

        override fun setTextColor(color: String) {
            super.setTextColor(color)
            bullet.tint(Color.parseColor(color))
        }

        override fun setTextColor(color: Int) {
            super.setTextColor(color)
            bullet.tint(content.context.color(R.color.black))
        }
    }

    class Numbered(view: View) : BlockViewHolder(view), TextHolder {

        private val number = itemView.number
        override val content: TextInputWidget = itemView.numberedListContent
        override val root: View = itemView

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Numbered,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            content.clearTextWatchers()

            number.text = item.number

            content.setText(item.toSpannable(), BufferType.SPANNABLE)

            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }

            setFocus(item)

            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }

            content.selectionDetector = { onSelectionChanged(item.id, it) }
        }

        override fun processChangePayload(payloads: List<Payload>, item: BlockView) {
            super.processChangePayload(payloads, item)
            payloads.forEach { payload ->
                if (payload.changes.contains(NUMBER_CHANGED))
                    number.text = (item as BlockView.Numbered).number
            }
        }
    }

    class Toggle(view: View) : BlockViewHolder(view) {

        private val toggle = itemView.toggle
        private val content = itemView.toggleContent

        fun bind(item: BlockView.Toggle) {
            content.text = item.text
            toggle.rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
        }

        companion object {
            /**
             * Rotation value for a toggle icon for expanded state.
             */
            const val EXPANDED_ROTATION = 90f
            /**
             * Rotation value for a toggle icon for collapsed state.
             */
            const val COLLAPSED_ROTATION = 0f
        }
    }

    class Contact(view: View) : BlockViewHolder(view) {

        private val name = itemView.name
        private val avatar = itemView.avatar

        fun bind(item: BlockView.Contact) {
            name.text = item.name
            avatar.bind(item.name)
        }
    }

    class File(view: View) : BlockViewHolder(view) {

        private val icon = itemView.fileIcon
        private val size = itemView.fileSize
        private val name = itemView.filename

        fun bind(
            item: BlockView.File.View,
            onDownloadFileClicked: (String) -> Unit
        ) {
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
            itemView.setOnClickListener { onDownloadFileClicked(item.id) }
        }

        class Placeholder(view: View) : BlockViewHolder(view) {

            fun bind(item: BlockView.File.Placeholder, onAddLocalFileClick: (String) -> Unit) {
                itemView.setOnClickListener {
                    onAddLocalFileClick(item.id)
                }
                itemView.icMore.setOnClickListener {
                    it.context.toast("Not implemented yet!")
                }
            }
        }

        class Error(view: View) : BlockViewHolder(view) {

            fun bind(msg: String) {
                itemView.tvError.text = msg
            }
        }

        class Upload(view: View) : BlockViewHolder(view)
    }

    class Video(view: View) : BlockViewHolder(view) {

        fun bind(item: BlockView.Video.View) {
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

        class Placeholder(view: View) : BlockViewHolder(view) {

            fun bind(item: BlockView.Video.Placeholder, onAddLocalVideoClick: (String) -> Unit) {
                itemView.setOnClickListener {
                    onAddLocalVideoClick(item.id)
                }
            }
        }

        class Error(view: View) : BlockViewHolder(view) {

            fun bind(msg: String) {
                itemView.tvError.text = msg
            }
        }

        class Upload(view: View) : BlockViewHolder(view)
    }

    class Page(view: View) : BlockViewHolder(view) {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val icon = itemView.pageIcon
        private val title = itemView.pageTitle

        fun bind(
            item: BlockView.Page,
            onPageClicked: (String) -> Unit
        ) {
            title.text = item.text ?: untitled
            if (item.isEmpty)
                icon.setImageResource(R.drawable.ic_block_empty_page)
            else if (item.emoji == null)
                icon.setBackgroundResource(R.drawable.ic_block_page_without_emoji)
            title.setOnClickListener { onPageClicked(item.id) }
        }
    }

    class Bookmark(view: View) : BlockViewHolder(view) {

        private val title = itemView.bookmarkTitle
        private val description = itemView.bookmarkDescription
        private val url = itemView.bookmarkUrl
        private val image = itemView.bookmarkImage
        private val logo = itemView.bookmarkLogo
        private val error = itemView.loadBookmarkPictureError

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

        fun bind(item: BlockView.Bookmark.View) {
            title.text = item.title
            description.text = item.description
            url.text = item.url

            item.imageUrl?.let { url ->
                Glide.with(image)
                    .load(url)
                    .listener(listener)
                    .into(image)
            }
            item.faviconUrl?.let { url ->
                Glide.with(logo)
                    .load(url)
                    .listener(listener)
                    .into(logo)
            }
        }

        class Placeholder(view: View) : BlockViewHolder(view) {

            fun bind(
                item: BlockView.Bookmark.Placeholder,
                onBookmarkPlaceholderClicked: (String) -> Unit
            ) {
                itemView.setOnClickListener { onBookmarkPlaceholderClicked(item.id) }
            }
        }
    }

    class Picture(view: View) : BlockViewHolder(view) {

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

        fun bind(item: BlockView.Picture.View) {
            Glide.with(image).load(item.url).listener(listener).into(image)
        }

        class Placeholder(view: View): BlockViewHolder(view) {

            fun bind(item: BlockView.Picture.Placeholder, onAddLocalPictureClick: (String) -> Unit) {
                itemView.setOnClickListener {
                    onAddLocalPictureClick(item.id)
                }
            }
        }

        class Error(view: View): BlockViewHolder(view)
        class Upload(view: View): BlockViewHolder(view)
    }

    class Divider(view: View) : BlockViewHolder(view)

    class Highlight(view: View) : BlockViewHolder(view), TextHolder {

        override val content: TextInputWidget = itemView.highlightContent
        override val root: View = itemView

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Highlight,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            content.clearTextWatchers()
            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }
            content.setText(item.text)
            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )
        }
    }

    class Footer(view: View) : BlockViewHolder(view) {

        private val footer = itemView

        fun bind(
            onFooterClicked: () -> Unit
        ) {
            footer.setOnClickListener { onFooterClicked() }
        }
    }

    companion object {
        const val HOLDER_PARAGRAPH = 0
        const val HOLDER_TITLE = 1
        const val HOLDER_HEADER_ONE = 2
        const val HOLDER_HEADER_TWO = 3
        const val HOLDER_HEADER_THREE = 4
        const val HOLDER_CODE_SNIPPET = 5
        const val HOLDER_CHECKBOX = 6
        const val HOLDER_TASK = 7
        const val HOLDER_BULLET = 8
        const val HOLDER_NUMBERED = 9
        const val HOLDER_TOGGLE = 10
        const val HOLDER_CONTACT = 11
        const val HOLDER_PAGE = 13
        const val HOLDER_DIVIDER = 16
        const val HOLDER_HIGHLIGHT = 17
        const val HOLDER_FOOTER = 18

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

        const val HOLDER_FILE = 30
        const val HOLDER_FILE_PLACEHOLDER = 31
        const val HOLDER_FILE_UPLOAD = 32
        const val HOLDER_FILE_ERROR = 33

        const val FOCUS_TIMEOUT_MILLIS = 16L
    }
}
