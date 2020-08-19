package com.agileburo.anytype.core_ui.features.editor.holders

import android.content.res.ColorStateList
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
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

class Title(view: View) : BlockViewHolder(view), TextHolder {

    private val icon = itemView.documentIconContainer
    private val image = itemView.imageIcon
    private val emoji = itemView.emojiIcon

    override val root: View = itemView
    override val content: TextInputWidget = itemView.title

    init {
        content.setSpannableFactory(DefaultSpannableFactory())
    }

    fun bind(
        item: BlockView.Title,
        onTitleTextChanged: (Editable) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onPageIconClicked: () -> Unit
    ) {

        Timber.d("Binding title view: $item")

        item.image?.let { url ->
            image.visible()
            Glide
                .with(image)
                .load(url)
                .centerInside()
                .circleCrop()
                .into(image)
        } ?: apply { image.setImageDrawable(null) }

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
            icon.setOnClickListener { onPageIconClicked() }
        }
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

    /**
     * Mention is not used in Title
     */
    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = Pair(0, 0)

    companion object {
        private const val EMPTY_EMOJI = ""
    }
}

class ProfileTitle(view: View) : BlockViewHolder(view), TextHolder {

    private val icon = itemView.documentIconContainer
    private val iconText = itemView.imageText
    private val image = itemView.imageIcon

    override val root: View = itemView
    override val content: TextInputWidget = itemView.title

    init {
        content.setSpannableFactory(DefaultSpannableFactory())
    }

    fun bind(
        item: BlockView.ProfileTitle,
        onTitleTextChanged: (Editable) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onProfileIconClicked: () -> Unit
    ) {

        Timber.d("Binding profile title view: $item")

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
            icon.setOnClickListener { onProfileIconClicked() }
        }
    }

    private fun showKeyboard() {
        content.postDelayed(KEYBOARD_SHOW_DELAY) {
            imm().showSoftInput(content, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /**
     * Mention is not used in ProfileTitle
     */
    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = Pair(0, 0)

    fun processPayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.ProfileTitle
    ) {

        Timber.d("Processing change payload $payloads for $item")

        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.TEXT_CHANGED)) {
                setIconText(item.text)
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

    private fun setIconText(name: String?) {
        if (name.isNullOrEmpty()) {
            iconText.text = ""
        } else {
            iconText.text = name.first().toUpperCase().toString()
        }
    }
}