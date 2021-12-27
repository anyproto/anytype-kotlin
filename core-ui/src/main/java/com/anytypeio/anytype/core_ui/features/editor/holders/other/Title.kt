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
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.editor.KeyPressedEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_block_title.view.*
import kotlinx.android.synthetic.main.item_block_title.view.emojiIcon
import kotlinx.android.synthetic.main.item_block_title.view.imageIcon
import kotlinx.android.synthetic.main.item_block_title.view.title
import kotlinx.android.synthetic.main.item_block_title_profile.view.*
import kotlinx.android.synthetic.main.item_block_title_todo.view.*
import timber.log.Timber

sealed class Title(view: View) : BlockViewHolder(view), TextHolder {

    val ivCover: ImageView? get() = itemView.findViewById(R.id.cover)

    abstract val icon: FrameLayout
    abstract val image: ImageView
    override val root: View = itemView

    fun bind(
        item: BlockView.Title,
        onFocusChanged: (String, Boolean) -> Unit,
        onCoverClicked: () -> Unit
    ) {
        setImage(item)
        setCover(
            coverColor = item.coverColor,
            coverImage = item.coverImage,
            coverGradient = item.coverGradient
        )
        if (item.mode == BlockView.Mode.READ) {
            enableReadMode()
            content.setText(item.text, TextView.BufferType.EDITABLE)
        } else {
            enableEditMode()
            content.setText(item.text, TextView.BufferType.EDITABLE)
            if (item.isFocused) setCursor(item)
            focus(item.isFocused)
            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
                if (hasFocus) showKeyboard()
            }
        }
        itemView.findViewById<ImageView?>(R.id.cover)?.apply {
            setOnClickListener { onCoverClicked() }
        }
    }

    private fun setCover(
        coverColor: CoverColor?,
        coverImage: String?,
        coverGradient: String?
    ) {
        when {
            coverColor != null -> {
                ivCover?.apply {
                    visible()
                    setImageDrawable(null)
                    setBackgroundColor(coverColor.color)
                }
                itemView.findViewById<ViewGroup?>(R.id.coverAndIconContainer)?.updatePadding(top = 0)
            }
            coverImage != null -> {
                ivCover?.apply {
                    visible()
                    setBackgroundColor(0)
                    Glide
                        .with(itemView)
                        .load(coverImage)
                        .centerCrop()
                        .into(this)
                }
                itemView.findViewById<ViewGroup?>(R.id.coverAndIconContainer)?.updatePadding(top = 0)
            }
            coverGradient != null -> {
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
                itemView.findViewById<ViewGroup?>(R.id.coverAndIconContainer)?.updatePadding(top = 0)
            }
            else -> {
                ivCover?.apply {
                    setImageDrawable(null)
                    setBackgroundColor(0)
                    gone()
                }
                itemView.findViewById<ViewGroup?>(R.id.coverAndIconContainer)?.updatePadding(top = dimen(R.dimen.dp_48))
            }
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

    class Document(view: View) : Title(view) {

        override val icon: FrameLayout = itemView.docEmojiIconContainer
        override val image: ImageView = itemView.imageIcon
        private val emoji: ImageView = itemView.emojiIcon

        override val root: View = itemView
        override val content: TextInputWidget = itemView.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Basic,
            onFocusChanged: (String, Boolean) -> Unit,
            onPageIconClicked: () -> Unit,
            onCoverClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onFocusChanged = onFocusChanged,
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
                    itemView.docImageIconContainer.visible()
                    itemView.docEmojiIconContainer.gone()
                    itemView.title.updateLayoutParams<LinearLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_12)
                    }
                }
                item.emoji != null -> {
                    itemView.docImageIconContainer.gone()
                    itemView.docEmojiIconContainer.visible()
                    itemView.title.updateLayoutParams<LinearLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_8)
                    }
                }
                else -> {
                    itemView.docImageIconContainer.gone()
                    itemView.docEmojiIconContainer.gone()
                    if (!item.hasCover) {
                        itemView.title.updateLayoutParams<LinearLayout.LayoutParams> {
                            topMargin = dimen(R.dimen.dp_48)
                        }
                    } else {
                        itemView.title.updateLayoutParams<LinearLayout.LayoutParams> {
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
    }

    class Archive(view: View) : Title(view) {

        override val icon: FrameLayout = itemView.docEmojiIconContainer
        override val image: ImageView = itemView.imageIcon

        override val root: View = itemView
        override val content: TextInputWidget = itemView.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Archive
        ) {
            super.bind(
                item = item,
                onFocusChanged = { _, _ -> },
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
    }

    class Profile(view: View) : Title(view) {

        override val icon: FrameLayout = itemView.docProfileIconContainer
        override val image: ImageView = itemView.imageIcon
        override val content: TextInputWidget = itemView.title

        private val iconText = itemView.imageText

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Profile,
            onFocusChanged: (String, Boolean) -> Unit,
            onProfileIconClicked: () -> Unit,
            onCoverClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onFocusChanged = onFocusChanged,
                onCoverClicked = onCoverClicked
            )
            applySearchHighlights(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onProfileIconClicked() }
            }
        }

        override fun setImage(item: BlockView.Title) {
            item.image?.let { url ->
                iconText.text = ""
                image.visible()
                Glide
                    .with(image)
                    .load(url)
                    .centerInside()
                    .circleCrop()
                    .into(image)
            } ?: apply {
                val pos = item.text?.firstDigitByHash() ?: 0
                setIconText(item.text)
                image.setImageDrawable(null)
            }
        }

        private fun setIconText(name: String?) {
            if (name.isNullOrEmpty()) {
                iconText.text = ""
            } else {
                iconText.text = name.first().toUpperCase().toString()
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
    }

    class Todo(view: View) : Title(view) {

        override val icon: FrameLayout = itemView.documentIconContainer
        override val image: ImageView = itemView.imageIcon

        val checkbox = itemView.todoTitleCheckbox

        override val root: View = itemView
        override val content: TextInputWidget = itemView.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Todo,
            onFocusChanged: (String, Boolean) -> Unit,
            onPageIconClicked: () -> Unit,
            onCoverClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onFocusChanged = onFocusChanged,
                onCoverClicked = onCoverClicked
            )
            checkbox.isSelected = item.isChecked
            applySearchHighlights(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onPageIconClicked() }
            }
        }

        override fun setImage(item: BlockView.Title) {}

        override fun processPayloads(
            payloads: List<BlockViewDiffUtil.Payload>,
            item: BlockView.Title
        ) {
            super.processPayloads(payloads, item)
            if (item is BlockView.Title.Todo) {
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
    }
}