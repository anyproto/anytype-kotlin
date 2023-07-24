package com.anytypeio.anytype.core_ui.features.editor

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Indentable
import com.anytypeio.anytype.presentation.editor.editor.model.Focusable
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
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

        if (newBlock is BlockView.Title.Basic && oldBlock is BlockView.Title.Basic) {
            if (newBlock.emoji != oldBlock.emoji || newBlock.image != oldBlock.image)
                changes.add(TITLE_ICON_CHANGED)
            if (newBlock.coverColor != oldBlock.coverColor
                || newBlock.coverGradient != oldBlock.coverGradient
                || newBlock.coverImage != oldBlock.coverImage
            ) {
                changes.add(COVER_CHANGED)
            }
        }

        if (newBlock is BlockView.Title.Todo && oldBlock is BlockView.Title.Todo) {
            if (newBlock.coverColor != oldBlock.coverColor
                || newBlock.coverGradient != oldBlock.coverGradient
                || newBlock.coverImage != oldBlock.coverImage
            ) {
                changes.add(COVER_CHANGED)
            }
            if (newBlock.isChecked != oldBlock.isChecked) {
                changes.add(TITLE_CHECKBOX_CHANGED)
            }
        }

        if (newBlock is BlockView.Title.Profile && oldBlock is BlockView.Title.Profile) {
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
            if (newBlock.background != oldBlock.background)
                changes.add(BACKGROUND_COLOR_CHANGED)
        }

        if (newBlock is Markup && oldBlock is Markup) {
            if (newBlock.marks != oldBlock.marks)
                changes.add(MARKUP_CHANGED)
        }

        if (newBlock is Focusable && oldBlock is Focusable) {
            if (newBlock.isFocused != oldBlock.isFocused) {
                changes.add(FOCUS_CHANGED)
                Timber.d("Focus changed to [${newBlock.isFocused}] for block ${newBlock.id}")
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
            if (newBlock.toggled != oldBlock.toggled)
                changes.add(TOGGLE_STATE_CHANGED)
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

        if (newBlock is BlockView.SupportGhostEditorSelection && oldBlock is BlockView.SupportGhostEditorSelection) {
            if (newBlock.ghostEditorSelection != oldBlock.ghostEditorSelection)
                changes.add(GHOST_EDITOR_SELECTION_CHANGED)
        }

        if (newBlock is BlockView.Loadable && oldBlock is BlockView.Loadable) {
            if (newBlock.isLoading != oldBlock.isLoading)
                changes.add(LOADING_STATE_CHANGED)
        }

        if (newBlock is BlockView.LinkToObject.Default && oldBlock is BlockView.LinkToObject.Default) {
            if (newBlock.text != oldBlock.text)
                changes.add(OBJECT_TITLE_CHANGED)
            if (newBlock.icon != oldBlock.icon)
                changes.add(OBJECT_ICON_CHANGED)
            if (newBlock.description != oldBlock.description)
                changes.add(OBJECT_DESCRIPTION_CHANGED)
            if (newBlock.background != oldBlock.background)
                changes.add(BACKGROUND_COLOR_CHANGED)
            if (newBlock.objectTypeName != oldBlock.objectTypeName)
                changes.add(OBJECT_TYPE_CHANGED)
        }

        if (newBlock is BlockView.LinkToObject.Default.Card.SmallIconCover
            && oldBlock is BlockView.LinkToObject.Default.Card.SmallIconCover) {
            if (newBlock.cover != oldBlock.cover)
                changes.add(OBJECT_COVER_CHANGED)
            if (newBlock.background != oldBlock.background)
                changes.add(BACKGROUND_COLOR_CHANGED)
        }

        if (newBlock is BlockView.LinkToObject.Default.Card.MediumIconCover
            && oldBlock is BlockView.LinkToObject.Default.Card.MediumIconCover) {
            if (newBlock.cover != oldBlock.cover)
                changes.add(OBJECT_COVER_CHANGED)
            if (newBlock.background != oldBlock.background)
                changes.add(BACKGROUND_COLOR_CHANGED)
        }

        if (newBlock is BlockView.Latex && oldBlock is BlockView.Latex) {
            if (newBlock.latex != oldBlock.latex)
                changes.add(LATEX_CHANGED)
            if (newBlock.background != oldBlock.background)
                changes.add(BACKGROUND_COLOR_CHANGED)
        }

        if (newBlock is BlockView.TableOfContents && oldBlock is BlockView.TableOfContents) {
            if (newBlock.background != oldBlock.background)
                changes.add(BACKGROUND_COLOR_CHANGED)
        }

        if (newBlock is BlockView.Relation.Related && oldBlock is BlockView.Relation.Related) {
            if (newBlock.background != oldBlock.background) {
                changes.add(BACKGROUND_COLOR_CHANGED)
            }

            val newRelationView = newBlock.view
            val oldRelationView = oldBlock.view

            if (newBlock.view.name != oldBlock.view.name) {
                changes.add(RELATION_NAME_CHANGED)
            }

            when {
                newRelationView is ObjectRelationView.Default && oldRelationView is ObjectRelationView.Default -> {
                    if (newBlock.view.value != oldBlock.view.value) {
                        changes.add(RELATION_VALUE_CHANGED)
                    }
                }
                newRelationView is ObjectRelationView.Checkbox && oldRelationView is ObjectRelationView.Checkbox -> {
                    if (newRelationView.isChecked != oldRelationView.isChecked) {
                        changes.add(RELATION_VALUE_CHANGED)
                    }
                }
                newRelationView is ObjectRelationView.Status && oldRelationView is ObjectRelationView.Status -> {
                    if (newRelationView.status != oldRelationView.status) {
                        changes.add(RELATION_VALUE_CHANGED)
                    }
                }
                newRelationView is ObjectRelationView.Tags && oldRelationView is ObjectRelationView.Tags -> {
                    if (newRelationView.tags != oldRelationView.tags) {
                        changes.add(RELATION_VALUE_CHANGED)
                    }
                }
                newRelationView is ObjectRelationView.Object && oldRelationView is ObjectRelationView.Object -> {
                    if (newRelationView.objects != oldRelationView.objects) {
                        changes.add(RELATION_VALUE_CHANGED)
                    }
                }

                newRelationView is ObjectRelationView.File && oldRelationView is ObjectRelationView.File -> {
                    if (newRelationView.files != oldRelationView.files) {
                        changes.add(RELATION_VALUE_CHANGED)
                    }
                }
            }
        }

        if (newBlock is BlockView.Decoratable && oldBlock is BlockView.Decoratable) {
            if (newBlock.decorations != oldBlock.decorations) {
                changes.add(DECORATION_CHANGED)
            }
        }

        if(newBlock is BlockView.Text.Callout && oldBlock is BlockView.Text.Callout) {
            if(newBlock.icon != oldBlock.icon) {
                changes.add(CALLOUT_ICON_CHANGED)
            }
        }

        if (newBlock is BlockView.Table && oldBlock is BlockView.Table) {
            if (newBlock.columns != oldBlock.columns || newBlock.rows != oldBlock.rows) {
                return super.getChangePayload(oldItemPosition, newItemPosition)
            }
            if (newBlock.cells != oldBlock.cells) {
                changes.add(TABLE_CELLS_CHANGED)
            }
            if (newBlock.selectedCellsIds != oldBlock.selectedCellsIds) {
                changes.add(TABLE_CELLS_SELECTION_CHANGED)
            }
            if (newBlock.tab != oldBlock.tab) {
                changes.add(TABLE_CELLS_SELECTION_CHANGED)
            }
        }

        if (newBlock is BlockView.DataView && oldBlock is BlockView.DataView) {
            if (newBlock.title != oldBlock.title) {
                changes.add(DATA_VIEW_TITLE_CHANGED)
            }
            if (newBlock.icon != oldBlock.icon) {
                changes.add(DATA_VIEW_ICON_CHANGED)
            }
            if (newBlock.background != oldBlock.background) {
                changes.add(BACKGROUND_COLOR_CHANGED)
            }
            if (newBlock.isCollection != oldBlock.isCollection) {
                changes.add(DATA_VIEW_TYPE_CHANGED)
            }
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

        val isIndentChanged: Boolean get() = changes.contains(INDENT_CHANGED)
        val isCursorChanged: Boolean get() = changes.contains(CURSOR_CHANGED)
        val isCoverChanged: Boolean get() = changes.contains(COVER_CHANGED)
        val isTextChanged: Boolean get() = changes.contains(TEXT_CHANGED)
        val isTextColorChanged: Boolean get() = changes.contains(TEXT_COLOR_CHANGED)
        val isBackgroundColorChanged: Boolean get() = changes.contains(BACKGROUND_COLOR_CHANGED)
        val isSelectionChanged: Boolean get() = changes.contains(SELECTION_CHANGED)
        val isTitleIconChanged: Boolean get() = changes.contains(TITLE_ICON_CHANGED)
        val isSearchHighlightChanged: Boolean get() = changes.contains(SEARCH_HIGHLIGHT_CHANGED)
        val isGhostEditorSelectionChanged: Boolean
            get() = changes.contains(
                GHOST_EDITOR_SELECTION_CHANGED
            )
        val isTitleCheckboxChanged: Boolean get() = changes.contains(TITLE_CHECKBOX_CHANGED)
        val isObjectTitleChanged: Boolean get() = changes.contains(OBJECT_TITLE_CHANGED)
        val isObjectIconChanged: Boolean get() = changes.contains(OBJECT_ICON_CHANGED)
        val isObjectDescriptionChanged: Boolean get() = changes.contains(OBJECT_DESCRIPTION_CHANGED)
        val isObjectCoverChanged: Boolean get() = changes.contains(OBJECT_COVER_CHANGED)
        val isObjectTypeChanged: Boolean get() = changes.contains(OBJECT_TYPE_CHANGED)

        val isLatexChanged: Boolean get() = changes.contains(LATEX_CHANGED)

        val isToggleStateChanged: Boolean get() = changes.contains(TOGGLE_STATE_CHANGED)
        val isToggleEmptyStateChanged: Boolean get() = changes.contains(TOGGLE_EMPTY_STATE_CHANGED)

        val isDecorationChanged: Boolean get() = changes.contains(DECORATION_CHANGED)

        val isCalloutIconChanged: Boolean get() = changes.contains(CALLOUT_ICON_CHANGED)

        val isDataViewTitleChanged : Boolean get() = changes.contains(DATA_VIEW_TITLE_CHANGED)
        val isDataViewIconChanged : Boolean get() = changes.contains(DATA_VIEW_ICON_CHANGED)
        val isDataViewBackgroundChanged : Boolean get() = changes.contains(DATA_VIEW_BACKGROUND_CHANGED)
        val isDataViewTypeChanged : Boolean get() = changes.contains(DATA_VIEW_TYPE_CHANGED)

        fun markupChanged() = changes.contains(MARKUP_CHANGED)
        fun textChanged() = changes.contains(TEXT_CHANGED)
        fun textColorChanged() = changes.contains(TEXT_COLOR_CHANGED)
        fun focusChanged() = changes.contains(FOCUS_CHANGED)
        fun backgroundColorChanged() = changes.contains(BACKGROUND_COLOR_CHANGED)
        fun readWriteModeChanged() = changes.contains(READ_WRITE_MODE_CHANGED)
        fun selectionChanged() = changes.contains(SELECTION_CHANGED)
        fun alignmentChanged() = changes.contains(ALIGNMENT_CHANGED)
        fun relationValueChanged() = changes.contains(RELATION_VALUE_CHANGED)
        fun relationNameChanged() = changes.contains(RELATION_NAME_CHANGED)

        fun tableCellsSelectionChanged() = changes.contains(TABLE_CELLS_SELECTION_CHANGED)
        fun tableCellsChanged() = changes.contains(TABLE_CELLS_CHANGED)
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
        const val TOGGLE_STATE_CHANGED = 26
        const val READ_WRITE_MODE_CHANGED = 9
        const val SELECTION_CHANGED = 10
        const val ALIGNMENT_CHANGED = 11
        const val CURSOR_CHANGED = 12
        const val TITLE_ICON_CHANGED = 13
        const val SEARCH_HIGHLIGHT_CHANGED = 14
        const val LOADING_STATE_CHANGED = 15
        const val COVER_CHANGED = 16
        const val TITLE_CHECKBOX_CHANGED = 17
        const val GHOST_EDITOR_SELECTION_CHANGED = 18
        const val LATEX_CHANGED = 21
        const val RELATION_NAME_CHANGED = 22
        const val RELATION_VALUE_CHANGED = 23

        const val OBJECT_TITLE_CHANGED = 319
        const val OBJECT_ICON_CHANGED = 320
        const val OBJECT_COVER_CHANGED = 321
        const val OBJECT_DESCRIPTION_CHANGED = 322
        const val OBJECT_TYPE_CHANGED = 323

        const val DECORATION_CHANGED = 27
        const val CALLOUT_ICON_CHANGED = 28

        const val TABLE_CELLS_SELECTION_CHANGED = 340
        const val TABLE_CELLS_CHANGED = 341

        const val DATA_VIEW_TITLE_CHANGED = 350
        const val DATA_VIEW_ICON_CHANGED = 351
        const val DATA_VIEW_BACKGROUND_CHANGED = 352
        const val DATA_VIEW_TYPE_CHANGED = 353
    }
}