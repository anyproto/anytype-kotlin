package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleProfileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleTodoBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.editor.KeyPressedEvent
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber

sealed class Title(view: View) : BlockViewHolder(view), TextHolder {

    val ivCover: ImageView? = itemView.findViewById(R.id.cover)
    private val title: View = itemView.findViewById(R.id.title)
    private val coverAndIconContainer: ViewGroup? =
        itemView.findViewById(R.id.coverAndIconContainer)
    private val cover: ImageView? = itemView.findViewById(R.id.cover)

    abstract val icon: FrameLayout
    abstract val image: ImageView
    override val root: View = itemView

    fun bind(
        item: BlockView.Title,
        onCoverClicked: () -> Unit
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
            content.pauseTextWatchers {
                content.setText(item.text, TextView.BufferType.EDITABLE)
            }
        } else {
            enableEditMode()
            content.pauseTextWatchers {
                content.setText(item.text, TextView.BufferType.EDITABLE)
            }
            if (item.isFocused) setCursor(item)
            focus(item.isFocused)
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
        ivCover?.apply {
            setImageDrawable(null)
            setBackgroundColor(0)
            gone()
        }
        coverAndIconContainer?.updatePadding(top = dimen(R.dimen.dp_48))
    }

    private fun setCoverGradient(coverGradient: String) {
        ivCover?.apply {
            setImageDrawable(null)
            setBackgroundColor(0)
            when (coverGradient) {
                CoverGradient.YELLOW -> setBackgroundResource(R.drawable.cover_gradient_yellow)
                CoverGradient.RED -> setBackgroundResource(R.drawable.cover_gradient_red)
                CoverGradient.BLUE -> setBackgroundResource(R.drawable.cover_gradient_blue)
                CoverGradient.TEAL -> setBackgroundResource(R.drawable.cover_gradient_teal)
                CoverGradient.PINK_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_1)
                CoverGradient.BLUE_PINK -> setBackgroundResource(R.drawable.wallpaper_gradient_2)
                CoverGradient.GREEN_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_3)
                CoverGradient.SKY -> setBackgroundResource(R.drawable.wallpaper_gradient_4)
            }
            visible()
        }
        addPaddingForCover()
    }

    private fun setCoverImage(coverImage: String) {
        ivCover?.apply {
            visible()
            setBackgroundColor(0)
            Glide
                .with(itemView)
                .load(coverImage)
                .centerCrop()
                .into(this)
        }
        addPaddingForCover()
    }

    private fun setColorCover(coverColor: CoverColor) {
        ivCover?.apply {
            visible()
            setImageDrawable(null)
            setBackgroundColor(coverColor.color)
        }
        addPaddingForCover()
    }

    protected open fun addPaddingForCover() {
        title.updateLayoutParams<LinearLayout.LayoutParams> {
            topMargin = dimen(R.dimen.dp_8)
        }
        coverAndIconContainer?.updatePadding(top = 0)
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
        item.image?.let { url ->
            image.visible()
            Glide
                .with(image)
                .load(url)
                .centerCrop()
                .into(image)
        } ?: apply { image.setImageDrawable(null) }
    }

    private fun showKeyboard() {
        content.postDelayed(16L) {
            imm().showSoftInput(content, InputMethodManager.SHOW_IMPLICIT)
        }
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

        override val icon: FrameLayout = binding.docEmojiIconContainer
        override val image: ImageView = binding.imageIcon
        private val emoji: ImageView = binding.emojiIcon

        override val root: View = itemView
        override val content: TextInputWidget = binding.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Basic,
            onPageIconClicked: () -> Unit,
            onCoverClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = onCoverClicked
            )
            setEmoji(item)
            applySearchHighlights(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onPageIconClicked() }
                image.setOnClickListener { onPageIconClicked() }
            }
            setupIconVisibility(item)
        }

        private fun setupIconVisibility(item: BlockView.Title.Basic) {
            when {
                item.image != null -> {
                    binding.imageIcon.visible()
                    binding.docEmojiIconContainer.gone()
                    binding.title.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_10)
                    }
                }
                item.emoji != null -> {
                    binding.imageIcon.gone()
                    binding.docEmojiIconContainer.visible()
                    binding.title.updateLayoutParams<LinearLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_8)
                    }
                }
                else -> {
                    binding.imageIcon.gone()
                    binding.docEmojiIconContainer.gone()
                    if (!item.hasCover) {
                        binding.title.updateLayoutParams<LinearLayout.LayoutParams> {
                            topMargin = dimen(R.dimen.dp_48)
                        }
                    } else {
                        binding.title.updateLayoutParams<LinearLayout.LayoutParams> {
                            topMargin = dimen(R.dimen.dp_8)
                        }
                    }
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
                    if (payload.isTitleIconChanged) {
                        setEmoji(item)
                        setImage(item)
                        setupIconVisibility(item)
                    }
                    if (payload.isSearchHighlightChanged) {
                        applySearchHighlights(item)
                    }
                }
            }
        }

        private fun setEmoji(item: BlockView.Title.Basic) {
            try {
                if (item.emoji != null) {
                    try {
                        Glide
                            .with(emoji)
                            .load(Emojifier.uri(item.emoji!!))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(emoji)
                    } catch (e: Throwable) {
                        Timber.e(e, "Error while setting emoji icon for: ${item.emoji}")
                    }
                } else {
                    emoji.setImageDrawable(null)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Could not set emoji icon")
            }
        }

        override fun applyTextColor(item: BlockView.Title) {
            setTextColor(item.color)
        }

        override fun applyBackground(item: BlockView.Title) {
            content.setBlockBackgroundColor(item.background)
        }
    }

    class Archive(val binding: ItemBlockTitleBinding) : Title(binding.root) {

        override val icon: FrameLayout = binding.docEmojiIconContainer
        override val image: ImageView = binding.imageIcon

        override val root: View = itemView
        override val content: TextInputWidget = binding.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Archive
        ) {
            super.bind(
                item = item,
                onCoverClicked = {}
            )
            setImage(item)
        }

        override fun setImage(item: BlockView.Title) {
            image.scaleType = ImageView.ScaleType.CENTER
            Glide.with(itemView.context)
                .load(R.drawable.ic_bin_big)
                .into(image)
        }

        override fun applyTextColor(item: BlockView.Title) {}
        override fun applyBackground(item: BlockView.Title) {}
    }

    class Profile(val binding: ItemBlockTitleProfileBinding) : Title(binding.root) {

        override val icon: FrameLayout = binding.docProfileIconContainer
        override val image: ImageView = binding.imageIcon
        override val content: TextInputWidget = binding.title

        private val iconText = binding.imageText
        private var hasImage = false

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Profile,
            onProfileIconClicked: (ListenerType) -> Unit,
            onCoverClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = onCoverClicked
            )
            applySearchHighlights(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onProfileIconClicked(ListenerType.ProfileImageIcon) }
            }
        }

        override fun setImage(item: BlockView.Title) {
            item.image?.let { url ->
                iconText.text = ""
                hasImage = true
                image.visible()
                Glide
                    .with(image)
                    .load(url)
                    .centerInside()
                    .circleCrop()
                    .into(image)
            } ?: apply {
                hasImage = false
                setIconText(item.text)
                image.setImageDrawable(null)
            }
        }

        private fun setIconText(name: String?) {
            if (name.isNullOrEmpty()) {
                iconText.text = "U"
            } else {
                iconText.text = name.first().uppercaseChar().toString()
            }
        }

        fun onTitleTextChanged(text: String) {
            if (!hasImage) {
                setIconText(text)
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
                        setImage(item)
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

        override val icon: FrameLayout = binding.documentIconContainer
        override val image: ImageView = binding.imageIcon

        val checkbox = binding.todoTitleCheckbox
        var isLocked: Boolean = false

        override val root: View = itemView
        override val content: TextInputWidget = binding.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Todo,
            onPageIconClicked: () -> Unit,
            onCoverClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onCoverClicked = onCoverClicked
            )
            setLocked(item.mode)
            checkbox.isSelected = item.isChecked
            applySearchHighlights(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onPageIconClicked() }
            }
        }

        override fun addPaddingForCover() {}

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
                    if (payload.isTitleCheckboxChanged) {
                        checkbox.isSelected = item.isChecked
                    }
                }
            }
        }

        override fun applyTextColor(item: BlockView.Title) {
            setTextColor(item.color)
        }

        override fun applyBackground(item: BlockView.Title) {
            binding.titleContainer.setBlockBackgroundColor(item.background)
        }
    }
}