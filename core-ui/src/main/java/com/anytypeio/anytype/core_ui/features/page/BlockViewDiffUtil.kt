package com.anytypeio.anytype.core_ui.features.page

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.editor.model.BlockView.Indentable
import com.anytypeio.anytype.presentation.page.editor.model.Focusable
import timber.log.Timber

class BlockViewDiffUtil(
    private val old: List<BlockView>,
    private val new: List<BlockView>
) : DiffUtil.Callback() {

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition].id == old[oldItemPosition].id

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition] == old[oldItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

        val oldBlock = old[oldItemPosition]
        val newBlock = new[newItemPosition]

        if (newBlock::class != oldBlock::class)
            return super.getChangePayload(oldItemPosition, newItemPosition)

        val changes = mutableListOf<Int>()

        if (newBlock is BlockView.Title.Document && oldBlock is BlockView.Title.Document) {
            if (newBlock.text != oldBlock.text)
                changes.add(TEXT_CHANGED)
            if (newBlock.emoji != oldBlock.emoji || newBlock.image != oldBlock.image)
                changes.add(TITLE_ICON_CHANGED)
            if (newBlock.coverColor != oldBlock.coverColor
                || newBlock.coverGradient != oldBlock.coverGradient
                || newBlock.coverImage != oldBlock.coverImage
            ) {
                changes.add(COVER_CHANGED)
            }
        }

        if (newBlock is BlockView.Title.Profile && oldBlock is BlockView.Title.Profile) {
            if (newBlock.text != oldBlock.text)
                changes.add(TEXT_CHANGED)
            if (newBlock.image != oldBlock.image)
                changes.add(TITLE_ICON_CHANGED)
            if (newBlock.coverColor != oldBlock.coverColor
                || newBlock.coverGradient != oldBlock.coverGradient
                || newBlock.coverImage != oldBlock.coverImage
            ) {
                changes.add(COVER_CHANGED)
            }
        }

        if (newBlock is BlockView.TextSupport && oldBlock is BlockView.TextSupport) {
            if (newBlock.text != oldBlock.text)
                changes.add(TEXT_CHANGED)
            if (newBlock.color != oldBlock.color)
                changes.add(TEXT_COLOR_CHANGED)
            if (newBlock.backgroundColor != oldBlock.backgroundColor)
                changes.add(BACKGROUND_COLOR_CHANGED)
        }

        if (newBlock is Markup && oldBlock is Markup) {
            if (newBlock.marks != oldBlock.marks)
                changes.add(MARKUP_CHANGED)
        }

        if (newBlock is Focusable && oldBlock is Focusable) {
            if (newBlock.isFocused != oldBlock.isFocused) {
                changes.add(FOCUS_CHANGED)
                Timber.d("Focus changed!")
            } else
                Timber.d("Focus hasn't changed")
        }

        if (newBlock is BlockView.Cursor && oldBlock is BlockView.Cursor) {
            if (newBlock.cursor != oldBlock.cursor)
                changes.add(CURSOR_CHANGED)
        }

        if (newBlock is Indentable && oldBlock is Indentable) {
            if (newBlock.indent != oldBlock.indent)
                changes.add(INDENT_CHANGED)
        }

        if (newBlock is BlockView.Text.Numbered && oldBlock is BlockView.Text.Numbered) {
            if (newBlock.number != oldBlock.number)
                changes.add(NUMBER_CHANGED)
        }

        if (newBlock is BlockView.Text.Toggle && oldBlock is BlockView.Text.Toggle) {
            if (newBlock.isEmpty != oldBlock.isEmpty)
                changes.add(TOGGLE_EMPTY_STATE_CHANGED)
        }

        if (newBlock is BlockView.Selectable && oldBlock is BlockView.Selectable) {
            if (newBlock.isSelected != oldBlock.isSelected)
                changes.add(SELECTION_CHANGED)
        }

        if (newBlock is BlockView.Permission && oldBlock is BlockView.Permission) {
            if (newBlock.mode != oldBlock.mode)
                changes.add(READ_WRITE_MODE_CHANGED)
        }

        if (newBlock is BlockView.Alignable && oldBlock is BlockView.Alignable) {
            if (newBlock.alignment != oldBlock.alignment)
                changes.add(ALIGNMENT_CHANGED)
        }

        if (newBlock is BlockView.Searchable && oldBlock is BlockView.Searchable) {
            if (newBlock.searchFields != oldBlock.searchFields)
                changes.add(SEARCH_HIGHLIGHT_CHANGED)
        }

        if (newBlock is BlockView.Loadable && oldBlock is BlockView.Loadable) {
            if (newBlock.isLoading != oldBlock.isLoading)
                changes.add(LOADING_STATE_CHANGED)
        }

        return if (changes.isNotEmpty())
            Payload(changes).also { Timber.d("Returning payload: $it") }
        else
            super.getChangePayload(oldItemPosition, newItemPosition)
    }

    /**
     * Payload of changes to apply to a block view.
     */
    data class Payload(
        val changes: List<Int>
    ) {

        val isLoadingChanged: Boolean get() = changes.contains(LOADING_STATE_CHANGED)
        val isIndentChanged: Boolean get() = changes.contains(INDENT_CHANGED)
        val isCursorChanged: Boolean get() = changes.contains(CURSOR_CHANGED)
        val isCoverChanged: Boolean get() = changes.contains(COVER_CHANGED)
        val isMarkupChanged: Boolean get() = changes.contains(MARKUP_CHANGED)
        val isTextChanged: Boolean get() = changes.contains(TEXT_CHANGED)
        val isTextColorChanged: Boolean get() = changes.contains(TEXT_COLOR_CHANGED)
        val isFocusChanged: Boolean get() = changes.contains(FOCUS_CHANGED)
        val isBackgroundColorChanged: Boolean get() = changes.contains(BACKGROUND_COLOR_CHANGED)
        val isModeChanged: Boolean get() = changes.contains(READ_WRITE_MODE_CHANGED)
        val isSelectionChanged: Boolean get() = changes.contains(SELECTION_CHANGED)
        val isTitleIconChanged: Boolean get() = changes.contains(TITLE_ICON_CHANGED)
        val isSearchHighlightChanged: Boolean get() = changes.contains(SEARCH_HIGHLIGHT_CHANGED)
        val isAlignmentChanged: Boolean get() = changes.contains(ALIGNMENT_CHANGED)

        fun markupChanged() = changes.contains(MARKUP_CHANGED)
        fun textChanged() = changes.contains(TEXT_CHANGED)
        fun textColorChanged() = changes.contains(TEXT_COLOR_CHANGED)
        fun focusChanged() = changes.contains(FOCUS_CHANGED)
        fun backgroundColorChanged() = changes.contains(BACKGROUND_COLOR_CHANGED)
        fun readWriteModeChanged() = changes.contains(READ_WRITE_MODE_CHANGED)
        fun selectionChanged() = changes.contains(SELECTION_CHANGED)
        fun alignmentChanged() = changes.contains(ALIGNMENT_CHANGED)
    }

    companion object {
        const val TEXT_CHANGED = 0
        const val MARKUP_CHANGED = 1
        const val FOCUS_CHANGED = 3
        const val TEXT_COLOR_CHANGED = 4
        const val NUMBER_CHANGED = 5
        const val BACKGROUND_COLOR_CHANGED = 6
        const val INDENT_CHANGED = 7
        const val TOGGLE_EMPTY_STATE_CHANGED = 8
        const val READ_WRITE_MODE_CHANGED = 9
        const val SELECTION_CHANGED = 10
        const val ALIGNMENT_CHANGED = 11
        const val CURSOR_CHANGED = 12
        const val TITLE_ICON_CHANGED = 13
        const val SEARCH_HIGHLIGHT_CHANGED = 14
        const val LOADING_STATE_CHANGED = 15
        const val COVER_CHANGED = 16
    }
}