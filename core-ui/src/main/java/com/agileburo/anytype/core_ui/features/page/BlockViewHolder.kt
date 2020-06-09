package com.agileburo.anytype.core_ui.features.page

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.BufferType
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.BuildConfig
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.common.toSpannable
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.tint
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.NUMBER_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.SELECTION_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TOGGLE_EMPTY_STATE_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Payload
import com.agileburo.anytype.core_ui.menu.AnytypeContextMenuEvent
import com.agileburo.anytype.core_ui.menu.AnytypeContextMenuType
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.actionmode.EmptyActionMode
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
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
import kotlinx.android.synthetic.main.item_block_bookmark_error.view.*
import kotlinx.android.synthetic.main.item_block_bookmark_placeholder.view.*
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
import android.text.format.Formatter as FileSizeFormatter

/**
 * Viewholder for rendering different type of blocks (i.e its UI-models).
 * @see BlockView
 * @see BlockAdapter
 */
sealed class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    interface IndentableHolder {
        fun indentize(item: BlockView.Indentable)
    }

    class Paragraph(
        view: View,
        onMarkupActionClicked: (Markup.Type) -> Unit
    ) : BlockViewHolder(view), TextHolder, IndentableHolder {

        override val root: View = itemView
        override val content: TextInputWidget = itemView.textContent

        init {
            setup(onMarkupActionClicked, ContextMenuType.TEXT)
        }

        fun bind(
            item: BlockView.Paragraph,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onLongClickListener: (String) -> Unit,
            anytypeContextMenuListener: ((AnytypeContextMenuEvent) -> Unit)? = null
        ) {

            indentize(item)
            anytypeContextMenuListener?.let { setAnytypeContextMenuListener(it) }

            if (item.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()
                setText(item)
                setTextColor(item)
                select(item)
            } else {
                enableEditMode()

                select(item)

                content.setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = onLongClickListener
                    )
                )

                content.clearTextWatchers()

                if (item.marks.isLinksPresent()) {
                    content.setLinksClickable()
                }

                setText(item)
                setTextColor(item)

                setFocus(item)
                if (item.isFocused) setCursor(item)

                setupTextWatcher(onTextChanged, item)

                content.setOnFocusChangeListener { _, focused ->
                    item.isFocused = focused
                    onFocusChanged(item.id, focused)
                }
                content.selectionDetector = {
                    onSelectionChanged(item.id, it)

                    /**
                     * [AnytypeContextMenu] logic
                     */
                    //Todo Remove before major release
                    if (it.first != it.last) {
                        anytypeContextMenuListener?.invoke(
                            AnytypeContextMenuEvent.Selected(
                                view = content,
                                type = AnytypeContextMenuType.DEFAULT
                            )
                        )
                    }
                }
            }
        }

        /**
         * [AnytypeContextMenu] logic
         */
        //Todo Remove before major release
        private fun setAnytypeContextMenuListener(listener: (AnytypeContextMenuEvent) -> Unit) {
            content.customSelectionActionModeCallback =
                EmptyActionMode { listener.invoke(AnytypeContextMenuEvent.Detached) }
        }

        private fun setText(item: BlockView.Paragraph) {
            content.setText(item.toSpannable(), BufferType.SPANNABLE)
        }

        private fun setTextColor(
            item: BlockView.Paragraph
        ) {
            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }
        }

        override fun indentize(item: BlockView.Indentable) {
            content.updatePadding(
                left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
            )
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
            onTitleTextChanged: (Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onPageIconClicked: () -> Unit
        ) {
            if (item.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()
                content.setText(item.text, BufferType.EDITABLE)
                icon.text = item.emoji ?: EMPTY_EMOJI
            } else {
                enableEditMode()
                if (item.isFocused) setCursor(item)
                focus(item.isFocused)
                content.setText(item.text, BufferType.EDITABLE)
                if (!item.text.isNullOrEmpty()) content.setSelection(item.text.length)
                setupTextWatcher({ _, editable -> onTitleTextChanged(editable) }, item)
                content.setOnFocusChangeListener { _, hasFocus ->
                    onFocusChanged(item.id, hasFocus)
                    if (hasFocus) showKeyboard()
                }
                with(icon) {
                    text = item.emoji ?: EMPTY_EMOJI
                    setOnClickListener { onPageIconClicked() }
                }
            }
        }

        private fun showKeyboard() {
            content.postDelayed(KEYBOARD_SHOW_DELAY) {
                imm().showSoftInput(content, SHOW_IMPLICIT)
            }
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

        companion object {
            private const val EMPTY_EMOJI = ""
        }
    }

    class HeaderOne(view: View, onMarkupActionClicked: (Markup.Type) -> Unit) :
        BlockViewHolder(view), TextHolder, IndentableHolder {

        private val header = itemView.headerOne
        override val root: View = itemView
        override val content: TextInputWidget
            get() = header

        init {
            setup(onMarkupActionClicked, ContextMenuType.HEADER)
        }

        fun bind(
            block: BlockView.HeaderOne,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            if (block.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()
                select(block)
                setBlockText(block)
                setBlockTextColor(block.color)
            } else {
                enableEditMode()
                select(block)
                setLinksClickable(block)
                setBlockText(block)
                setBlockTextColor(block.color)
                setFocus(block)
                if (block.isFocused) setCursor(block)
                with(header) {
                    clearTextWatchers()
                    setOnFocusChangeListener { _, hasFocus ->
                        onFocusChanged(block.id, hasFocus)
                    }
                    addTextChangedListener(
                        DefaultTextWatcher { text ->
                            onTextChanged(block.id, text)
                        }
                    )
                    selectionDetector = { onSelectionChanged(block.id, it) }
                }
            }
            header.setOnLongClickListener(
                EditorLongClickListener(
                    t = block.id,
                    click = onLongClickListener
                )
            )
            indentize(block)
        }

        override fun indentize(item: BlockView.Indentable) {
            header.updatePadding(
                left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
            )
        }

        private fun setBlockText(block: BlockView.HeaderOne) {
            if (block.marks.isNotEmpty())
                header.setText(block.toSpannable(), BufferType.SPANNABLE)
            else
                header.setText(block.text)
        }

        private fun setBlockTextColor(color: String?) {
            if (color != null)
                setTextColor(color)
            else
                setTextColor(content.context.color(R.color.black))
        }

        /**
         *  Should be set before @[setBlockText]!
         */
        private fun setLinksClickable(block: BlockView.HeaderOne) {
            if (block.marks.isLinksPresent()) {
                content.setLinksClickable()
            }
        }
    }

    class HeaderTwo(
        view: View,
        onMarkupActionClicked: (Markup.Type) -> Unit
    ) : BlockViewHolder(view), TextHolder, IndentableHolder {

        private val header = itemView.headerTwo
        override val content: TextInputWidget
            get() = header
        override val root: View = itemView

        init {
            setup(onMarkupActionClicked, ContextMenuType.HEADER)
        }

        fun bind(
            block: BlockView.HeaderTwo,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            if (block.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()
                select(block)
                setBlockText(block)
                setBlockTextColor(block.color)
            } else {
                enableEditMode()
                select(block)
                setLinksClickable(block)
                setBlockText(block)
                setBlockTextColor(block.color)
                setFocus(block)
                if (block.isFocused) setCursor(block)
                with(header) {
                    clearTextWatchers()
                    setOnFocusChangeListener { _, hasFocus ->
                        onFocusChanged(block.id, hasFocus)
                    }
                    addTextChangedListener(
                        DefaultTextWatcher { text ->
                            onTextChanged(block.id, text)
                        }
                    )
                    selectionDetector = { onSelectionChanged(block.id, it) }
                }
            }
            header.setOnLongClickListener(
                EditorLongClickListener(
                    t = block.id,
                    click = onLongClickListener
                )
            )
            indentize(block)
        }

        override fun indentize(item: BlockView.Indentable) {
            header.updatePadding(
                left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
            )
        }

        private fun setBlockText(block: BlockView.HeaderTwo) {
            if (block.marks.isNotEmpty())
                header.setText(block.toSpannable(), BufferType.SPANNABLE)
            else
                header.setText(block.text)
        }

        private fun setBlockTextColor(color: String?) {
            if (color != null)
                setTextColor(color)
            else
                setTextColor(content.context.color(R.color.black))
        }

        /**
         *  Should be set before @[setBlockText]!
         */
        private fun setLinksClickable(block: BlockView.HeaderTwo) {
            if (block.marks.isLinksPresent()) {
                content.setLinksClickable()
            }
        }
    }

    class HeaderThree(
        view: View,
        onMarkupActionClicked: (Markup.Type) -> Unit
    ) : BlockViewHolder(view), TextHolder, IndentableHolder {

        private val header = itemView.headerThree
        override val content: TextInputWidget
            get() = header
        override val root: View = itemView

        init {
            setup(onMarkupActionClicked, ContextMenuType.HEADER)
        }

        fun bind(
            block: BlockView.HeaderThree,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            if (block.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()
                select(block)
                setBlockText(block)
                setBlockTextColor(block.color)
            } else {
                enableEditMode()
                select(block)
                setLinksClickable(block)
                setBlockText(block)
                setBlockTextColor(block.color)
                setFocus(block)
                if (block.isFocused) setCursor(block)
                with(header) {
                    clearTextWatchers()
                    setOnFocusChangeListener { _, hasFocus ->
                        onFocusChanged(block.id, hasFocus)
                    }
                    addTextChangedListener(
                        DefaultTextWatcher { text ->
                            onTextChanged(block.id, text)
                        }
                    )
                    selectionDetector = { onSelectionChanged(block.id, it) }
                }
            }
            header.setOnLongClickListener(
                EditorLongClickListener(
                    t = block.id,
                    click = onLongClickListener
                )
            )
            indentize(block)
        }

        override fun indentize(item: BlockView.Indentable) {
            header.updatePadding(
                left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
            )
        }

        private fun setBlockText(block: BlockView.HeaderThree) {
            if (block.marks.isNotEmpty())
                header.setText(block.toSpannable(), BufferType.SPANNABLE)
            else
                header.setText(block.text)
        }

        private fun setBlockTextColor(color: String?) {
            if (color != null)
                setTextColor(color)
            else
                setTextColor(content.context.color(R.color.black))
        }

        /**
         *  Should be set before @[setBlockText]!
         */
        private fun setLinksClickable(block: BlockView.HeaderThree) {
            if (block.marks.isLinksPresent()) {
                content.setLinksClickable()
            }
        }
    }

    class Code(view: View) : BlockViewHolder(view), TextHolder {

        override val root: View
            get() = itemView
        override val content: TextInputWidget
            get() = itemView.snippet

        fun bind(
            item: BlockView.Code,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            if (item.mode == BlockView.Mode.READ) {
                content.setText(item.text)
                enableReadOnlyMode()
                select(item)
            } else {
                enableEditMode()

                select(item)

                content.setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = onLongClickListener
                    )
                )

                content.clearTextWatchers()

                content.setText(item.text)
                setFocus(item)

                setupTextWatcher(onTextChanged, item)

                content.setOnFocusChangeListener { _, focused ->
                    item.isFocused = focused
                    onFocusChanged(item.id, focused)
                }
                content.selectionDetector = { onSelectionChanged(item.id, it) }
            }
        }

        override fun select(item: BlockView.Selectable) {
            root.isSelected = item.isSelected
        }
    }

    class Checkbox(
        view: View,
        onMarkupActionClicked: (Markup.Type) -> Unit
    ) : BlockViewHolder(view), TextHolder, IndentableHolder {

        var mode = BlockView.Mode.EDIT

        private val checkbox: ImageView = itemView.checkboxIcon
        private val container = itemView.checkboxBlockContentContainer
        override val content: TextInputWidget = itemView.checkboxContent
        override val root: View = itemView

        init {
            setup(onMarkupActionClicked, ContextMenuType.TEXT)
        }

        fun bind(
            item: BlockView.Checkbox,
            onTextChanged: (String, Editable) -> Unit,
            onCheckboxClicked: (String) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            indentize(item)

            if (item.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()

                select(item)

                updateTextColor(
                    context = itemView.context,
                    view = content,
                    isSelected = checkbox.isActivated
                )
                checkbox.isActivated = item.isChecked
                if (item.marks.isNotEmpty())
                    content.setText(item.toSpannable(), BufferType.SPANNABLE)
                else
                    content.setText(item.text)
            } else {

                enableEditMode()

                select(item)

                content.setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = onLongClickListener
                    )
                )

                content.clearTextWatchers()
                checkbox.isActivated = item.isChecked

                updateTextColor(
                    context = itemView.context,
                    view = content,
                    isSelected = checkbox.isActivated
                )

                if (item.marks.isLinksPresent()) {
                    content.setLinksClickable()
                }

                if (item.marks.isNotEmpty())
                    content.setText(item.toSpannable(), BufferType.SPANNABLE)
                else
                    content.setText(item.text)

                if (item.isFocused) setCursor(item)

                setFocus(item)

                checkbox.setOnClickListener {
                    if (mode == BlockView.Mode.EDIT) {
                        checkbox.isActivated = !checkbox.isActivated
                        updateTextColor(
                            context = itemView.context,
                            view = content,
                            isSelected = checkbox.isActivated
                        )
                        onCheckboxClicked(item.id)
                    }
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

        override fun indentize(item: BlockView.Indentable) {
            checkbox.updatePadding(left = item.indent * dimen(R.dimen.indent))
        }

        override fun enableEditMode() {
            super.enableEditMode()
            mode = BlockView.Mode.EDIT
        }

        override fun enableReadOnlyMode() {
            super.enableReadOnlyMode()
            mode = BlockView.Mode.READ
        }

        override fun select(item: BlockView.Selectable) {
            container.isSelected = item.isSelected
        }

        private fun updateTextColor(context: Context, view: TextView, isSelected: Boolean) =
            view.setTextColor(
                context.color(
                    if (isSelected) R.color.checkbox_state_checked else R.color.black
                )
            )
    }

    class Task(view: View) : BlockViewHolder(view) {

        private val checkbox = itemView.taskIcon
        private val content = itemView.taskContent

        fun bind(item: BlockView.Task) {
            checkbox.isSelected = item.checked
            content.text = item.text
        }
    }

    class Bulleted(
        view: View,
        onMarkupActionClicked: (Markup.Type) -> Unit
    ) : BlockViewHolder(view), TextHolder, IndentableHolder {

        private val indent = itemView.bulletIndent
        private val bullet = itemView.bullet
        private val container = itemView.bulletBlockContainer
        override val content: TextInputWidget = itemView.bulletedListContent
        override val root: View = itemView

        init {
            setup(onMarkupActionClicked, ContextMenuType.TEXT)
        }

        fun bind(
            item: BlockView.Bulleted,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            indentize(item)

            if (item.mode == BlockView.Mode.READ) {

                enableReadOnlyMode()

                select(item)

                if (item.marks.isNotEmpty())
                    content.setText(item.toSpannable(), BufferType.SPANNABLE)
                else
                    content.setText(item.text)

                if (item.color != null)
                    setTextColor(item.color)
                else
                    setTextColor(content.context.color(R.color.black))

            } else {

                enableEditMode()

                select(item)

                content.setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = onLongClickListener
                    )
                )

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

                if (item.isFocused) setCursor(item)

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
        }

        override fun setTextColor(color: String) {
            super.setTextColor(color)
            bullet.setColorFilter(
                ThemeColor.values().first { value ->
                    value.title == color
                }.text
            )
        }

        override fun setTextColor(color: Int) {
            super.setTextColor(color)
            bullet.tint(content.context.color(R.color.black))
        }

        override fun indentize(item: BlockView.Indentable) {
            indent.updateLayoutParams { width = item.indent * dimen(R.dimen.indent) }
        }

        override fun select(item: BlockView.Selectable) {
            container.isSelected = item.isSelected
        }
    }

    class Numbered(
        view: View,
        onMarkupActionClicked: (Markup.Type) -> Unit
    ) : BlockViewHolder(view), TextHolder, IndentableHolder {

        private val container = itemView.numberedBlockContentContainer
        private val number = itemView.number
        override val content: TextInputWidget = itemView.numberedListContent
        override val root: View = itemView

        init {
            setup(onMarkupActionClicked, ContextMenuType.TEXT)
        }

        fun bind(
            item: BlockView.Numbered,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            indentize(item)

            if (item.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()

                select(item)

                number.gravity = when (item.number) {
                    in 1..19 -> Gravity.CENTER_HORIZONTAL
                    else -> Gravity.START
                }

                number.text = item.number.addDot()

                content.setText(item.toSpannable(), BufferType.SPANNABLE)

                if (item.color != null)
                    setTextColor(item.color)
                else
                    setTextColor(content.context.color(R.color.black))
            } else {

                enableEditMode()

                select(item)

                content.setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = onLongClickListener
                    )
                )

                content.clearTextWatchers()
                indentize(item)
                number.gravity = when (item.number) {
                    in 1..19 -> Gravity.CENTER_HORIZONTAL
                    else -> Gravity.START
                }
                number.text = item.number.addDot()

                if (item.marks.isLinksPresent()) {
                    content.setLinksClickable()
                }

                content.setText(item.toSpannable(), BufferType.SPANNABLE)

                if (item.color != null)
                    setTextColor(item.color)
                else
                    setTextColor(content.context.color(R.color.black))

                if (item.isFocused) setCursor(item)

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
        }

        override fun processChangePayload(
            payloads: List<Payload>,
            item: BlockView,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit
        ) {
            super.processChangePayload(payloads, item, onTextChanged, onSelectionChanged)
            payloads.forEach { payload ->
                if (payload.changes.contains(NUMBER_CHANGED))
                    number.text = "${(item as BlockView.Numbered).number}"
            }
        }

        override fun indentize(item: BlockView.Indentable) {
            number.updateLayoutParams<LinearLayout.LayoutParams> {
                setMargins(
                    item.indent * dimen(R.dimen.indent),
                    0,
                    0,
                    0
                )
            }
        }

        override fun select(item: BlockView.Selectable) {
            container.isSelected = item.isSelected
        }
    }

    class Toggle(
        view: View,
        onMarkupActionClicked: (Markup.Type) -> Unit
    ) : BlockViewHolder(view), TextHolder, IndentableHolder {

        private var mode = BlockView.Mode.EDIT

        private val toggle = itemView.toggle
        private val line = itemView.guideline
        private val placeholder = itemView.togglePlaceholder
        private val container = itemView.toolbarBlockContentContainer
        override val content: TextInputWidget = itemView.toggleContent
        override val root: View = itemView

        init {
            setup(onMarkupActionClicked, ContextMenuType.TEXT)
        }

        fun bind(
            item: BlockView.Toggle,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onToggleClicked: (String) -> Unit,
            onTogglePlaceholderClicked: (String) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {

            indentize(item)

            if (item.mode == BlockView.Mode.READ) {

                enableReadOnlyMode()

                select(item)

                content.setText(item.toSpannable(), BufferType.SPANNABLE)

                if (item.color != null)
                    setTextColor(item.color)
                else
                    setTextColor(content.context.color(R.color.black))

                placeholder.isVisible = false

                toggle.apply {
                    rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
                }
            } else {

                enableEditMode()

                select(item)

                content.setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = onLongClickListener
                    )
                )

                if (item.marks.isLinksPresent()) {
                    content.setLinksClickable()
                }

                content.clearTextWatchers()
                content.setText(item.toSpannable(), BufferType.SPANNABLE)

                if (item.color != null) {
                    setTextColor(item.color)
                } else {
                    setTextColor(content.context.color(R.color.black))
                }

                if (item.isFocused) setCursor(item)

                setFocus(item)

                setupTextWatcher(onTextChanged, item)

                content.setOnFocusChangeListener { _, focused ->
                    item.isFocused = focused
                    onFocusChanged(item.id, focused)
                }
                content.selectionDetector = { onSelectionChanged(item.id, it) }

                toggle.apply {
                    rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
                    setOnClickListener {
                        if (mode == BlockView.Mode.EDIT)
                            onToggleClicked(item.id)
                    }
                }

                placeholder.apply {
                    isVisible = item.isEmpty && item.toggled
                    setOnClickListener { onTogglePlaceholderClicked(item.id) }
                }
            }
        }

        override fun indentize(item: BlockView.Indentable) {
            line.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
        }

        override fun select(item: BlockView.Selectable) {
            container.isSelected = item.isSelected
        }

        override fun enableReadOnlyMode() {
            super.enableReadOnlyMode()
            mode = BlockView.Mode.READ
        }

        override fun enableEditMode() {
            super.enableEditMode()
            mode = BlockView.Mode.EDIT
        }

        override fun processChangePayload(
            payloads: List<Payload>,
            item: BlockView,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit
        ) {
            check(item is BlockView.Toggle) { "Expected a toggle block, but was: $item" }
            super.processChangePayload(payloads, item, onTextChanged, onSelectionChanged)
            payloads.forEach { payload ->
                if (payload.changes.contains(TOGGLE_EMPTY_STATE_CHANGED))
                    placeholder.isVisible = item.isEmpty
            }
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
                        click = { clicked(ListenerType.LongClick(it)) })
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

        class Placeholder(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.File.Placeholder,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.File.Placeholder(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) })
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
                check(item is BlockView.File.Placeholder) { "Expected a file placeholder block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }

        class Error(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.File.Error,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.File.Error(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) })
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
                check(item is BlockView.File.Error) { "Expected a file error block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }

        class Upload(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.File.Upload,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.File.Upload(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) })
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
                check(item is BlockView.File.Upload) { "Expected a file upload block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
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
                        click = { clicked(ListenerType.LongClick(it)) }
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

        class Placeholder(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.Video.Placeholder,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.Video.Placeholder(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
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
                check(item is BlockView.Video.Placeholder) { "Expected a video placeholder block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }

        class Error(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.Video.Error,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.Video.Error(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
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
                check(item is BlockView.Video.Error) { "Expected a video error block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }

        class Upload(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.Video.Upload,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.Video.Upload(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
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
                check(item is BlockView.Video.Upload) { "Expected a video upload block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }
    }

    class Page(view: View) : BlockViewHolder(view), IndentableHolder {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val icon = itemView.pageIcon
        private val emoji = itemView.emoji
        private val title = itemView.pageTitle
        private val guideline = itemView.pageGuideline

        fun bind(
            item: BlockView.Page,
            onPageClicked: (String) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            indentize(item)

            itemView.isSelected = item.isSelected

            title.text = if (item.text.isNullOrEmpty()) untitled else item.text

            when {
                item.emoji != null -> emoji.text = item.emoji
                item.isEmpty -> icon.setImageResource(R.drawable.ic_block_empty_page)
                else -> icon.setImageResource(R.drawable.ic_block_page_without_emoji)
            }

            title.setOnClickListener { onPageClicked(item.id) }
            title.setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = onLongClickListener
                )
            )
        }

        override fun indentize(item: BlockView.Indentable) {
            guideline.setGuidelineBegin(
                item.indent * dimen(R.dimen.indent)
            )
        }

        fun processChangePayload(payloads: List<Payload>, item: BlockView) {
            check(item is BlockView.Page) { "Expected a page block, but was: $item" }
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
                        click = { clicked(ListenerType.LongClick(it)) }
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

        class Placeholder(view: View) : BlockViewHolder(view), IndentableHolder {

            private val root = itemView.bookmarkPlaceholderRoot

            fun bind(
                item: BlockView.Bookmark.Placeholder,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                select(item.isSelected)
                with(root) {
                    setOnClickListener {
                        clicked(ListenerType.Bookmark.Placeholder(item.id))
                    }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
                        )
                    )
                }
            }

            override fun indentize(item: BlockView.Indentable) {
                root.indentize(
                    indent = item.indent,
                    defIndent = dimen(R.dimen.indent),
                    margin = dimen(R.dimen.bookmark_default_margin_start)
                )
            }

            fun processChangePayload(
                payloads: List<Payload>,
                item: BlockView
            ) {
                check(item is BlockView.Bookmark.Placeholder) { "Expected a bookmark placeholder block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.selectionChanged()) {
                        select(item.isSelected)
                    }
                }
            }

            private fun select(isSelected: Boolean) {
                root.isSelected = isSelected
            }
        }

        class Error(view: View) : BlockViewHolder(view), IndentableHolder {

            private val root = itemView.bookmarkErrorRoot
            private val url = itemView.errorBookmarkUrl

            fun bind(
                item: BlockView.Bookmark.Error,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                select(item.isSelected)
                url.text = item.url
                with(root) {
                    setOnClickListener {
                        clicked(ListenerType.Bookmark.Error(item))
                    }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
                        )
                    )
                }
            }

            override fun indentize(item: BlockView.Indentable) {
                root.indentize(
                    indent = item.indent,
                    defIndent = dimen(R.dimen.indent),
                    margin = dimen(R.dimen.bookmark_default_margin_start)
                )
            }

            fun processChangePayload(
                payloads: List<Payload>,
                item: BlockView
            ) {
                check(item is BlockView.Bookmark.Error) { "Expected a bookmark error block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.selectionChanged()) {
                        select(item.isSelected)
                    }
                }
            }

            private fun select(isSelected: Boolean) {
                root.isSelected = isSelected
            }
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
                        click = { clicked(ListenerType.LongClick(it)) }
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

        class Placeholder(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.Picture.Placeholder,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.Picture.Placeholder(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
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
                check(item is BlockView.Picture.Placeholder) { "Expected a picture placeholder block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }

        class Error(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.Picture.Error,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.Picture.Error(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
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
                check(item is BlockView.Picture.Error) { "Expected a picture error block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }

        class Upload(view: View) : BlockViewHolder(view), IndentableHolder {

            fun bind(
                item: BlockView.Picture.Upload,
                clicked: (ListenerType) -> Unit
            ) {
                indentize(item)
                with(itemView) {
                    isSelected = item.isSelected
                    setOnClickListener { clicked(ListenerType.Picture.Upload(item.id)) }
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = { clicked(ListenerType.LongClick(it)) }
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
                check(item is BlockView.Picture.Upload) { "Expected a picture upload block, but was: $item" }
                payloads.forEach { payload ->
                    if (payload.changes.contains(SELECTION_CHANGED)) {
                        itemView.isSelected = item.isSelected
                    }
                }
            }
        }
    }

    class Divider(view: View) : BlockViewHolder(view) {

        fun bind(
            item: BlockView.Divider,
            onLongClickListener: (String) -> Unit
        ) {
            itemView.setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = onLongClickListener
                )
            )
        }
    }

    class Highlight(view: View, onMarkupActionClicked: (Markup.Type) -> Unit) :
        BlockViewHolder(view), TextHolder, IndentableHolder {

        override val content: TextInputWidget = itemView.highlightContent
        override val root: View = itemView
        private val indent = itemView.highlightIndent
        private val container = itemView.highlightBlockContentContainer

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
            setup(onMarkupActionClicked, ContextMenuType.HIGHLIGHT)
        }

        fun bind(
            item: BlockView.Highlight,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onLongClickListener: (String) -> Unit
        ) {
            //indentize(item)

            if (item.mode == BlockView.Mode.READ) {
                enableReadOnlyMode()
                setText(item)
            } else {
                enableEditMode()
                setLinksClickable(item)
                setText(item)
                if (item.isFocused) setCursor(item)
                setFocus(item)
                with(content) {
                    clearTextWatchers()
                    setOnFocusChangeListener { _, hasFocus ->
                        onFocusChanged(item.id, hasFocus)
                    }
                    addTextChangedListener(
                        DefaultTextWatcher { text ->
                            onTextChanged(item.id, text)
                        }
                    )
                    setOnLongClickListener(
                        EditorLongClickListener(
                            t = item.id,
                            click = onLongClickListener
                        )
                    )
                    selectionDetector = { onSelectionChanged(item.id, it) }
                }
            }
        }

        override fun select(item: BlockView.Selectable) {
            container.isSelected = item.isSelected
        }

        override fun indentize(item: BlockView.Indentable) {
            indent.updateLayoutParams {
                width = item.indent * dimen(R.dimen.indent)
            }
        }

        private fun setText(item: BlockView.Highlight) {
            if (item.marks.isNotEmpty())
                content.setText(item.toSpannable(), BufferType.SPANNABLE)
            else
                content.setText(item.text)
        }

        /**
         *  Should be set before @[setText]!
         */
        private fun setLinksClickable(block: BlockView.Highlight) {
            if (block.marks.isLinksPresent()) {
                content.setLinksClickable()
            }
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
        const val HOLDER_BOOKMARK_ERROR = 30

        const val HOLDER_FILE = 31
        const val HOLDER_FILE_PLACEHOLDER = 32
        const val HOLDER_FILE_UPLOAD = 33
        const val HOLDER_FILE_ERROR = 34

        const val FOCUS_TIMEOUT_MILLIS = 16L
        const val KEYBOARD_SHOW_DELAY = 16L
    }
}
