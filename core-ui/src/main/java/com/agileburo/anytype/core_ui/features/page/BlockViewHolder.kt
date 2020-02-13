package com.agileburo.anytype.core_ui.features.page

import android.graphics.Color
import android.text.Editable
import android.view.View
import android.widget.TextView.BufferType
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.*
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.tint
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.FOCUS_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.MARKUP_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.NUMBER_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_COLOR_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Payload
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.text.BackspaceKeyDetector
import com.agileburo.anytype.core_utils.text.DefaultEnterKeyDetector
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
import kotlinx.android.synthetic.main.item_block_task.view.*
import kotlinx.android.synthetic.main.item_block_text.view.*
import kotlinx.android.synthetic.main.item_block_title.view.*
import kotlinx.android.synthetic.main.item_block_toggle.view.*
import timber.log.Timber

/**
 * Viewholder for rendering different type of blocks (i.e its UI-models).
 * @see BlockView
 * @see BlockAdapter
 */
sealed class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    /**
     * Provides default implementation for common behavior for text blocks.
     */
    interface TextHolder {

        /**
         * Block's content widget.
         * Common behavior is applied to this widget.
         */
        val content: TextInputWidget

        fun enableEnterKeyDetector(
            onEndLineEnterClicked: (Editable) -> Unit,
            onSplitLineEnterClicked: () -> Unit
        ) {
            content.filters = arrayOf(
                DefaultEnterKeyDetector(
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onEndLineEnterClicked = { onEndLineEnterClicked(content.editableText) }
                )
            )
        }

        fun enableBackspaceDetector(
            onEmptyBlockBackspaceClicked: () -> Unit,
            onNonEmptyBlockBackspaceClicked: () -> Unit
        ) {
            content.setOnKeyListener(
                BackspaceKeyDetector {
                    if (content.text.toString().isEmpty()) {
                        // Refactoring needed, there are cases when we shouldn't clear text watchers
                        //content.clearTextWatchers()
                        //content.setOnKeyListener(null)
                        onEmptyBlockBackspaceClicked()
                    } else {
                        // Refactoring needed, there are cases when we shouldn't clear text watchers
                        //content.clearTextWatchers()
                        //content.setOnKeyListener(null)
                        onNonEmptyBlockBackspaceClicked()
                    }
                }
            )
        }

        fun setTextColor(color: String) {
            content.setTextColor(Color.parseColor(color))
        }

        fun setTextColor(color: Int) {
            content.setTextColor(color)
        }

        fun setFocus(item: Focusable) {
            if (item.focused && !content.hasFocus())
                focus()
            else
                content.clearFocus()
        }

        fun setMarkup(markup: Markup) {
            content.text?.setMarkup(markup)
        }

        fun setupTextWatcher(
            onTextChanged: (String, Editable) -> Unit,
            item: BlockView
        ) {
            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )
        }

        private fun focus() {
            content.apply {
                postDelayed(
                    { requestFocus() }
                    , FOCUS_TIMEOUT_MILLIS
                )
            }
        }

        fun processChangePayload(
            payloads: List<Payload>,
            item: BlockView
        ) = payloads.forEach { payload ->

            Timber.d("Processing $payload for new view:\n$item")

            if (item is BlockView.Text) {
                if (payload.changes.contains(TEXT_CHANGED))
                    if (content.text.toString() != item.text) {
                        Timber.d("Text changed.\nBefore:${content.text.toString()}\nAfter:${item.text}")
                        content.pauseTextWatchers {
                            if (item is Markup)
                                content.setText(item.toSpannable(), BufferType.SPANNABLE)
                        }
                    }

                if (payload.changes.contains(TEXT_COLOR_CHANGED))
                    item.color?.let { setTextColor(it) }
            }

            if (item is Markup) {
                if (payload.changes.contains(MARKUP_CHANGED) && !payload.changes.contains(
                        TEXT_CHANGED
                    )
                )
                    setMarkup(item)
            }

            if (item is Focusable) {
                if (payload.changes.contains(FOCUS_CHANGED))
                    setFocus(item)
            }
        }
    }

    class Paragraph(view: View) : BlockViewHolder(view), TextHolder {

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

        override val content: TextInputWidget = itemView.title

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Title,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }
            content.setText(item.text, BufferType.EDITABLE)
            setupTextWatcher(onTextChanged, item)
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
    }

    class HeaderOne(view: View) : BlockViewHolder(view), TextHolder {

        private val header = itemView.headerOne
        override val content: TextInputWidget
            get() = header

        fun bind(
            item: BlockView.HeaderOne,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
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

        fun bind(
            item: BlockView.HeaderTwo,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
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

    class HeaderThree(view: View) : BlockViewHolder(view), TextHolder {

        private val header = itemView.headerThree
        override val content: TextInputWidget
            get() = header

        fun bind(
            item: BlockView.HeaderThree,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
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

        fun bind(
            item: BlockView.Checkbox,
            onTextChanged: (String, Editable) -> Unit,
            onCheckboxClicked: (String) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
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

        init {
            content.setSpannableFactory(DefaultSpannableFactory())
        }

        fun bind(
            item: BlockView.Bulleted,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
            Timber.d("Binding bullet")

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

        fun bind(
            item: BlockView.Numbered,
            onTextChanged: (String, Editable) -> Unit,
            onSelectionChanged: (String, IntRange) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
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

        fun bind(item: BlockView.File) {
            name.text = item.filename
            size.text = item.size
            // TODO set file icon.
        }
    }

    class Page(view: View) : BlockViewHolder(view) {

        private val icon = itemView.pageIcon
        private val title = itemView.pageTitle

        fun bind(item: BlockView.Page) {
            title.text = item.text
            if (item.isEmpty)
                icon.setImageResource(R.drawable.ic_block_empty_page)
            else if (item.emoji == null)
                icon.setBackgroundResource(R.drawable.ic_block_page_without_emoji)
        }
    }

    class Bookmark(view: View) : BlockViewHolder(view) {

        private val title = itemView.bookmarkTitle
        private val description = itemView.bookmarkDescription
        private val url = itemView.bookmarkUrl
        private val image = itemView.bookmarkImage
        private val logo = itemView.bookmarkLogo

        fun bind(item: BlockView.Bookmark) {
            title.text = item.title
            description.text = item.description
            url.text = item.url
            // TODO set logo icon and website's image
        }
    }

    class Picture(view: View) : BlockViewHolder(view) {

        fun bind(item: BlockView.Picture) {
            // TODO
        }
    }

    class Divider(view: View) : BlockViewHolder(view)

    class Highlight(view: View) : BlockViewHolder(view), TextHolder {

        override val content: TextInputWidget = itemView.highlightContent

        fun bind(
            item: BlockView.Highlight,
            onTextChanged: (String, Editable) -> Unit,
            onFocusChanged: (String, Boolean) -> Unit
        ) {
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
        const val HOLDER_FILE = 12
        const val HOLDER_PAGE = 13
        const val HOLDER_BOOKMARK = 14
        const val HOLDER_PICTURE = 15
        const val HOLDER_DIVIDER = 16
        const val HOLDER_HIGHLIGHT = 17
        const val HOLDER_FOOTER = 18

        const val FOCUS_TIMEOUT_MILLIS = 60L
    }
}
