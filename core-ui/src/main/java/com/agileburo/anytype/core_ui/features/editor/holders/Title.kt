package com.agileburo.anytype.core_ui.features.editor.holders

import android.content.res.ColorStateList
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.postDelayed
import com.agileburo.anytype.core_ui.extensions.avatarColor
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.TextHolder
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.firstDigitByHash
import com.agileburo.anytype.core_utils.ext.imm
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.emojifier.Emojifier
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
            enableReadOnlyMode()
            content.setText(item.text, TextView.BufferType.EDITABLE)
        } else {
            enableEditMode()
            if (item.isFocused) setCursor(item)
            focus(item.isFocused)
            content.setText(item.text, TextView.BufferType.EDITABLE)
            setCursor(item)
            setupTextWatcher({ _, editable -> onTitleTextChanged(editable) }, item)
            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
                if (hasFocus) showKeyboard()
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

    fun processPayloads(
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
                if (item.mode == BlockView.Mode.EDIT)
                    enableEditMode()
                else
                    enableTitleReadOnlyMode()
            }
        }
    }

    fun focus(focused: Boolean) {
        if (focused) {
            content.requestFocus()
            showKeyboard()
        } else
            content.clearFocus()
    }

    override fun enableBackspaceDetector(
        onEmptyBlockBackspaceClicked: () -> Unit,
        onNonEmptyBlockBackspaceClicked: () -> Unit
    ) = Unit

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = Pair(0, 0)

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
            onTitleTextChanged: (Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onPageIconClicked: () -> Unit
        ) {
            super.bind(
                item = item,
                onTitleTextChanged = onTitleTextChanged,
                onFocusChanged = onFocusChanged
            )
            setEmoji(item)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onPageIconClicked() }
            }
        }

        private fun setEmoji(item: BlockView.Title.Document) {
            try {
                if (item.emoji != null) {
                    Glide
                        .with(emoji)
                        .load(Emojifier.uri(item.emoji))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(emoji)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Could not set emoji icon")
            }
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
            onTitleTextChanged: (Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onProfileIconClicked: () -> Unit
        ) {
            Timber.d("Binding profile title view: $item")
            super.bind(item, onTitleTextChanged, onFocusChanged)
            if (item.mode == BlockView.Mode.EDIT) {
                icon.setOnClickListener { onProfileIconClicked() }
            }
        }

        override fun setImage(item: BlockView.Title) {
            item.image?.let { url ->
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
    }
}