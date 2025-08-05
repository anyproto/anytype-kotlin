package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.content.Context
import android.text.Spannable
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.imageLoader
import coil3.load
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.target.Target
import coil3.video.VideoFrameDecoder
import coil3.video.videoFrameMillis
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleImageBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleProfileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleTodoBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleVideoBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.editor.KeyPressedEvent
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import timber.log.Timber

sealed class Title(view: View) : BlockViewHolder(view), TextHolder {

    private val cover: ImageView? = itemView.findViewById(R.id.cover)

    abstract val icon: View
    abstract val image: ImageView
    override val root: View = itemView

    fun bind(
        item: BlockView.Title,
        onCoverClicked: () -> Unit,
        click: (ListenerType) -> Unit
    ) {
        setImage(item)
        applyTextColor(item)
        applyBackground(item)
        setCover(
            coverColor = item.coverColor,
            coverImage = item.coverImage,
            coverGradient = item.coverGradient
        )
        if (item.mode == BlockView.Mode.READ) {
            enableReadMode()
        } else {
            enableEditMode()
            if (item.isFocused) setCursor(item)
            focus(item.isFocused)
        }
        content.pauseTextWatchers {
            if (!item.hint.isNullOrBlank()) {
                content.hint = item.hint
            }
            content.setText(item.text, TextView.BufferType.EDITABLE)
        }
        cover?.setOnClickListener { onCoverClicked() }
    }

    private fun setCover(
        coverColor: CoverColor?,
        coverImage: String?,
        coverGradient: String?
    ) {
        when {
            coverColor != null -> setColorCover(coverColor)
            coverImage != null -> setCoverImage(coverImage)
            coverGradient != null -> setCoverGradient(coverGradient)
            else -> setCoverless()
        }
    }

    private fun setCoverless() {
        cover?.apply {
            setImageDrawable(null)
            setBackgroundColor(0)
            gone()
        }
    }

    private fun setCoverGradient(coverGradient: String) {
        cover?.apply {
            setImageDrawable(null)
            setBackgroundColor(0)
            val resourceId = when (coverGradient) {
                CoverGradient.YELLOW -> R.drawable.cover_gradient_yellow
                CoverGradient.RED -> R.drawable.cover_gradient_red
                CoverGradient.BLUE -> R.drawable.cover_gradient_blue
                CoverGradient.TEAL -> R.drawable.cover_gradient_teal
                CoverGradient.PINK_ORANGE -> R.drawable.wallpaper_gradient_1
                CoverGradient.BLUE_PINK -> R.drawable.wallpaper_gradient_2
                CoverGradient.GREEN_ORANGE -> R.drawable.wallpaper_gradient_3
                CoverGradient.SKY -> R.drawable.wallpaper_gradient_4
                else -> {
                    Timber.w("Unknown cover gradient: $coverGradient")
                    0
                }
            }
            setBackgroundResource(resourceId)
            visible()
        }
    }

    private fun setCoverImage(coverImage: String) {
        cover?.apply {
            visible()
            setBackgroundColor(0)
            load(coverImage)
        }
    }

    private fun setColorCover(coverColor: CoverColor) {
        cover?.apply {
            visible()
            setImageDrawable(null)
            setBackgroundColor(coverColor.color)
        }
    }

    fun applySearchHighlights(item: BlockView.Searchable) {
        content.editableText.removeSpans<SearchHighlightSpan>()
        content.editableText.removeSpans<SearchTargetHighlightSpan>()
        item.searchFields.forEach { field ->
            field.highlights.forEach { highlight ->
                content.editableText.setSpan(
                    SearchHighlightSpan(),
                    highlight.first,
                    highlight.last,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (field.isTargeted) {
                content.editableText.setSpan(
                    SearchTargetHighlightSpan(),
                    field.target.first,
                    field.target.last,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    open fun setImage(item: BlockView.Title) {
        // Default no-op implementation
        // Each subclass handles its own icon logic using ObjectIconWidget
    }

    open fun processPayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Title
    ) {
        Timber.d("Processing change payload $payloads for $item")

        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.TEXT_CHANGED)) {
                content.pauseTextWatchers {
                    if (content.text.toString() != item.text) {
                        content.setText(item.text, TextView.BufferType.EDITABLE)
                    }
                }
            }
            if (payload.isCursorChanged) {
                if (item.isFocused) setCursor(item)
            }
            if (payload.focusChanged()) {
                focus(item.isFocused)
            }
            if (payload.readWriteModeChanged()) {
                content.pauseTextWatchers {
                    if (item.mode == BlockView.Mode.EDIT)
                        enableEditMode()
                    else
                        enableReadMode()
                }
            }

            if (payload.isCoverChanged) {
                setCover(
                    coverColor = item.coverColor,
                    coverImage = item.coverImage,
                    coverGradient = item.coverGradient
                )
            }
            if (payload.isBackgroundColorChanged) {
                applyBackground(item)
            }
            if (payload.isTextColorChanged) {
                applyTextColor(item)
            }
        }
    }

    fun focus(focused: Boolean) {
        if (focused) {
            focus()
        } else
            content.clearFocus()
    }

    fun onTitleEnterKeyListener(
        views: List<BlockView>,
        textView: TextView,
        range: IntRange,
        onKeyPressedEvent: (KeyPressedEvent) -> Unit
    ) {
        val pos = bindingAdapterPosition
        val text = textView.text.toString()
        if (pos != RecyclerView.NO_POSITION) {
            val view = views[pos]
            check(view is BlockView.Title)
            onKeyPressedEvent.invoke(
                KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                    target = view.id,
                    text = text,
                    range = range
                )
            )
        }
    }

    override fun select(item: BlockView.Selectable) = Unit
    abstract fun applyTextColor(item: BlockView.Title)
    abstract fun applyBackground(item: BlockView.Title)

    class Document(val binding: ItemBlockTitleBinding) : Title(binding.root) {

        override val icon: ObjectIconWidget = binding.objectIconWidget
        override val image: ImageView = binding.objectIconWidget.binding.ivImage
        override val selectionView: View = itemView

        override val root: View = itemView
        override val content: TextInputWidget = binding.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Basic,
            onPageIconClicked: () -> Unit,
            onCoverClicked: () -> Unit,
            click: (ListenerType) -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = onCoverClicked,
                click = click
            )
            setIcon(item)
            applySearchHighlights(item)

            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onPageIconClicked() }
            }
        }

        private fun setIcon(item: BlockView.Title.Basic) {
            icon.setIcon(item.icon)
            
            // Adjust ObjectIconWidget size based on icon type
            when (item.icon) {
                is ObjectIcon.Basic.Emoji -> {
                    icon.updateLayoutParams<FrameLayout.LayoutParams> {
                        width = dimen(R.dimen.dp_88)
                        height = dimen(R.dimen.dp_88)
                        topMargin = if (item.hasCover) dimen(R.dimen.dp_164) else dimen(R.dimen.dp_120)
                    }
                }
                is ObjectIcon.Basic.Image -> {
                    icon.updateLayoutParams<FrameLayout.LayoutParams> {
                        width = dimen(R.dimen.dp_104)
                        height = dimen(R.dimen.dp_104)
                        topMargin = if (item.hasCover) dimen(R.dimen.dp_148) else dimen(R.dimen.dp_120)
                    }
                }
                else -> {
                    //do nothing for other icon types
                }
            }
        }

        override fun processPayloads(
            payloads: List<BlockViewDiffUtil.Payload>,
            item: BlockView.Title
        ) {
            super.processPayloads(payloads, item)
            if (item is BlockView.Title.Basic) {
                payloads.forEach { payload ->
                    if (payload.isTitleIconChanged || payload.isCoverChanged) {
                        setIcon(item)
                    }
                    if (payload.isSearchHighlightChanged) {
                        applySearchHighlights(item)
                    }
                }
            }
        }

        override fun applyTextColor(item: BlockView.Title) {
            setTextColor(item.color)
        }

        override fun applyBackground(item: BlockView.Title) {
            content.setBlockBackgroundColor(item.background)
        }
    }

    class Profile(val binding: ItemBlockTitleProfileBinding) : Title(binding.root) {

        override val icon: ObjectIconWidget = binding.objectIconWidget
        override val image: ImageView = binding.objectIconWidget.binding.ivImage
        override val content: TextInputWidget = binding.title
        override val selectionView: View = itemView

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Profile,
            onProfileIconClicked: (ListenerType) -> Unit,
            onCoverClicked: () -> Unit,
            click: (ListenerType) -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = onCoverClicked,
                click = click
            )
            setIcon(item)
            applySearchHighlights(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onProfileIconClicked(ListenerType.ProfileImageIcon) }
            }
        }

        private fun setIcon(item: BlockView.Title.Profile) {
            icon.setIcon(item.icon)
        }

        fun onTitleTextChanged(item: BlockView, text: String) {
            if (item is BlockView.Title.Profile && item.icon is ObjectIcon.Profile.Avatar) {
                icon.setIcon(ObjectIcon.Profile.Avatar(text))
            }
        }

        override fun processPayloads(
            payloads: List<BlockViewDiffUtil.Payload>,
            item: BlockView.Title
        ) {
            super.processPayloads(payloads, item)
            if (item is BlockView.Title.Profile) {
                payloads.forEach { payload ->
                    if (payload.isTitleIconChanged) {
                        setIcon(item)
                    }
                    if (payload.isSearchHighlightChanged) {
                        applySearchHighlights(item)
                    }
                }
            }
        }

        override fun applyTextColor(item: BlockView.Title) {
            setTextColor(item.color)
        }

        override fun applyBackground(item: BlockView.Title) {
            binding.title.setBlockBackgroundColor(item.background)
        }
    }

    class Todo(val binding: ItemBlockTitleTodoBinding) : Title(binding.root) {

        override val icon: ObjectIconWidget = binding.objectIconWidget
        override val image: ImageView = binding.objectIconWidget.binding.ivImage
        override val selectionView: View = itemView

        val checkbox = binding.objectIconWidget.checkbox
        var isLocked: Boolean = false

        override val root: View = itemView
        override val content: TextInputWidget = binding.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Todo,
            onCoverClicked: () -> Unit,
            click: (ListenerType) -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = onCoverClicked,
                click = click
            )
            setLocked(item.mode)
            setIcon(item)
            applySearchHighlights(item)
        }

        private fun setLocked(mode: BlockView.Mode) {
            isLocked = mode == BlockView.Mode.READ
        }

        override fun setImage(item: BlockView.Title) {}

        override fun processPayloads(
            payloads: List<BlockViewDiffUtil.Payload>,
            item: BlockView.Title
        ) {
            super.processPayloads(payloads, item)
            if (item is BlockView.Title.Todo) {
                setLocked(item.mode)
                payloads.forEach { payload ->
                    if (payload.isSearchHighlightChanged) {
                        applySearchHighlights(item)
                    }
                    if (payload.isTitleCheckboxChanged || payload.isCoverChanged) {
                        setIcon(item)
                    }
                }
            }
        }

        private fun setIcon(item: BlockView.Title.Todo) {
            icon.setIcon(item.icon)
        }

        override fun applyTextColor(item: BlockView.Title) {
            setTextColor(item.color)
        }

        override fun applyBackground(item: BlockView.Title) {
            content.setBlockBackgroundColor(item.background)
        }
    }

    class File(val binding: ItemBlockTitleFileBinding) : Title(binding.root) {

        override val icon: ObjectIconWidget = binding.objectIconWidget
        override val image: ImageView = binding.cover
        override val selectionView: View = itemView
        override val root: View = itemView
        override val content: TextInputWidget = binding.title

        fun bind(
            item: BlockView.Title.File,
        ) {
            super.bind(
                item = item,
                onCoverClicked = {},
                click = {}
            )
            icon.setIcon(item.icon)
        }

        override fun applyTextColor(item: BlockView.Title) {
            //do nothing
        }
        override fun applyBackground(item: BlockView.Title) {
            //do nothing
        }
    }

    class Image(private val imageBinding: ItemBlockTitleImageBinding) : Title(imageBinding.root) {

        private val context: Context = imageBinding.root.context

        override val icon: ObjectIconWidget = imageBinding.objectIconWidget
        override val image: ImageView = imageBinding.imageIcon
        override val content: TextInputWidget = imageBinding.title
        override val selectionView: View = itemView

        private val progress = imageBinding.progress

        override fun applyTextColor(item: BlockView.Title) {
            // Do nothing
        }

        override fun applyBackground(item: BlockView.Title) {
            // Do nothing
        }

        fun bind(
            item: BlockView.Title.Image,
            clicked: (ListenerType) -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = {
                    // Click event intentionally ignored
                },
                click = {
                    // Click event intentionally ignored
                }
            )
            content.setText(item.text)

            image.setOnClickListener {
                clicked(ListenerType.Header.Image)
            }
        }

        override fun setImage(item: BlockView.Title) {
            item.image?.let { url ->
                image.visible()
                loadImageWithCustomResize(image, url)
            } ?: run { image.setImageDrawable(null) }
        }

        private fun loadImageWithCustomResize(imageView: ImageView, url: String) {
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            val request = ImageRequest.Builder(context)
                .data(url)
                .listener(
                    onStart = {
                        progress.visible()
                    },
                    onSuccess = { _, _ ->
                        progress.gone()
                    },
                    onError = { _, _ ->
                        progress.gone()
                    }
                )
                .target(object : Target {
                    override fun onSuccess(result: coil3.Image) {
                        if (result is android.graphics.drawable.BitmapDrawable) {
                            val bitmap = result.bitmap
                            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val calculatedHeight = (screenWidth / aspectRatio).toInt()

                            val imageHeight = when {
                                calculatedHeight < dpToPx(context, 188) -> dpToPx(context, 188)
                                calculatedHeight > dpToPx(context, 443) -> dpToPx(context, 443)
                                else -> calculatedHeight
                            }

                            imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                width = screenWidth
                                height = imageHeight
                            }
                            imageView.setImageDrawable(result)
                        } else {
                            imageView.load(url)
                        }
                    }

                    override fun onError(result: coil3.Image?) {
                        imageView.setImageDrawable(null)
                    }

                    override fun onStart(placeholder: coil3.Image?) {
                        // Do nothing
                    }
                })
                .build()

            context.imageLoader.enqueue(request)
        }

        private fun dpToPx(context: Context, dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }
    }

    class Video(
        private val videoBinding: ItemBlockTitleVideoBinding
    ) : Title(videoBinding.root) {

        override val icon: ObjectIconWidget = videoBinding.objectIconWidget
        override val image: ImageView = videoBinding.cover
        override val content: TextInputWidget = videoBinding.title
        override val selectionView: View = itemView

        fun bind(
            item: BlockView.Title.Video,
            onPlayClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = {},
                click = {}
            )
            content.setText(item.text)
            setupPreview(onPlayClicked, item.videoUrl)
        }

        private fun setupPreview(
            onPlayClicked: () -> Unit,
            url: String?
        ) {
            with(videoBinding) {

                progress.visible()
                objectIconWidget.gone()
                playButton.setOnClickListener {
                    onPlayClicked()
                }

                if (!url.isNullOrEmpty()) {
                    val imageLoader = ImageLoader.Builder(itemView.context)
                        .components {
                            add(VideoFrameDecoder.Factory())
                        }
                        .build()
                    videoThumbnail.load(url, imageLoader) {
                        crossfade(true)
                        videoFrameMillis(1000L)
                        listener(
                            onStart = {
                                progress.visible()
                            },
                            onSuccess = { _, _ ->
                                progress.gone()
                                playButton.visible()
                            },
                            onError = { _, _ ->
                                progress.gone()
                                playButton.visible()
                            }
                        )
                    }
                }
            }
        }

        override fun applyTextColor(item: BlockView.Title) {
            // Do nothing
        }

        override fun applyBackground(item: BlockView.Title) {
            // Do nothing
        }
    }
}