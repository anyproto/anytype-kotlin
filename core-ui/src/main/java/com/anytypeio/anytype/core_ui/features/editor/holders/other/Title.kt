package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.content.res.ColorStateList
import android.text.Editable
import android.text.Spannable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.postDelayed
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.extensions.avatarColor
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.firstDigitByHash
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_block_title.view.*
import kotlinx.android.synthetic.main.item_block_title.view.documentIconContainer
import kotlinx.android.synthetic.main.item_block_title.view.imageIcon
import kotlinx.android.synthetic.main.item_block_title.view.title
import kotlinx.android.synthetic.main.item_block_title_profile.view.*
import timber.log.Timber

sealed class Title(view: View) : BlockViewHolder(view), TextHolder {

    abstract val icon: FrameLayout
    abstract val image: ImageView
    override val root: View = itemView

    fun bind(
        item: BlockView.Title,
        onTitleTextChanged: (Editable) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit
    ) {
        setImage(item)
        if (item.mode == BlockView.Mode.READ) {
            enableReadMode()
            content.setText(item.text, TextView.BufferType.EDITABLE)
        } else {
            enableEditMode()
            content.setText(item.text, TextView.BufferType.EDITABLE)
            if (item.isFocused) setCursor(item)
            focus(item.isFocused)
            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTitleTextChanged(text)
                }
            )
            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
                if (hasFocus) showKeyboard()
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
                .centerInside()
                .circleCrop()
                .into(image)
        } ?: apply { image.setImageDrawable(null) }
    }

    private fun showKeyboard() {
        content.postDelayed(KEYBOARD_SHOW_DELAY) {
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
        }
    }

    fun focus(focused: Boolean) {
        if (focused) {
            focus()
        } else
            content.clearFocus()
    }

    override fun select(item: BlockView.Selectable) = Unit

    class Document(view: View) : Title(view) {

        override val icon: FrameLayout = itemView.documentIconContainer
        override val image: ImageView = itemView.imageIcon
        private val emoji: ImageView = itemView.emojiIcon

        override val root: View = itemView
        override val content: TextInputWidget = itemView.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Document,
            onTitleTextChanged: (BlockView.Title) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onPageIconClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onTitleTextChanged = {
                    item.text = it.toString()
                    onTitleTextChanged(item)
                },
                onFocusChanged = onFocusChanged
            )
            setEmoji(item)
            applySearchHighlights(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onPageIconClicked() }
            }
        }

        override fun processPayloads(
            payloads: List<BlockViewDiffUtil.Payload>,
            item: BlockView.Title
        ) {
            super.processPayloads(payloads, item)
            if (item is BlockView.Title.Document) {
                payloads.forEach { payload ->
                    if (payload.isTitleIconChanged) {
                        setEmoji(item)
                        setImage(item)
                    }
                    if (payload.isSearchHighlightChanged) {
                        applySearchHighlights(item)
                    }
                }
            }
        }

        private fun setEmoji(item: BlockView.Title.Document) {
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

        override val icon: FrameLayout = itemView.documentIconContainer
        override val image: ImageView = itemView.imageIcon

        override val root: View = itemView
        override val content: TextInputWidget = itemView.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Document
        ) {
            super.bind(
                item = item,
                onTitleTextChanged = {},
                onFocusChanged = { _, _ -> }
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

        override val icon: FrameLayout = itemView.documentIconContainer
        override val image: ImageView = itemView.imageIcon
        override val content: TextInputWidget = itemView.title

        private val iconText = itemView.imageText

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title.Profile,
            onTitleTextChanged: (BlockView.Title) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onProfileIconClicked: () -> Unit
        ) {
            Timber.d("Binding profile title view: $item")
            super.bind(
                item = item,
                onFocusChanged = onFocusChanged,
                onTitleTextChanged = {
                    item.text = it.toString()
                    onTitleTextChanged(item)
                },
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
                icon.backgroundTintList = ColorStateList.valueOf(itemView.context.avatarColor(pos))
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
}