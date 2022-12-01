package com.anytypeio.anytype.presentation.editor.editor.ext

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Document
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.editor.ControlPanelMachine
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Media.Bookmark.Companion.SEARCH_FIELD_DESCRIPTION_KEY
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Media.Bookmark.Companion.SEARCH_FIELD_TITLE_KEY
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Media.Bookmark.Companion.SEARCH_FIELD_URL_KEY
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Searchable.Field.Companion.DEFAULT_SEARCH_FIELD_KEY
import com.anytypeio.anytype.presentation.editor.selection.updateCellsMode
import com.anytypeio.anytype.presentation.extension.shift
import com.anytypeio.anytype.presentation.objects.appearance.LinkAppearanceFactory
import timber.log.Timber

fun List<BlockView>.singleStylingMode(
    target: Id
): List<BlockView> = map { view ->
    val isSelected = view.id == target
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Checkbox -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Bulleted -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Numbered -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Highlight -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Header.One -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Header.Two -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Header.Three -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Toggle -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Callout -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Code -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.Bookmark -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Upload.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Upload.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Upload.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.Bookmark -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.Bookmark -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Title.Basic -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Title.Todo -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Title.Profile -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Title.Archive -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Description -> view.copy(
            mode = BlockView.Mode.READ
        )
        else -> view.also {
            check(view !is BlockView.Permission) { "Actual type was: ${view.getViewType()}" }
        }
    }
}

fun List<BlockView>.enterSAM(
    targets: Set<Id>
): List<BlockView> = map { view ->
    val isSelected = targets.contains(view.id)
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Checkbox -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Bulleted -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Numbered -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Highlight -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Callout -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Header.One -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Header.Two -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Header.Three -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Text.Toggle -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected,
            isFocused = false,
            cursor = null
        )
        is BlockView.Code -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Error.Bookmark -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Upload.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Upload.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Upload.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.Bookmark -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.MediaPlaceholder.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.File -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.Video -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.Bookmark -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Media.Picture -> view.copy(
            mode = BlockView.Mode.READ,
            isSelected = isSelected
        )
        is BlockView.Title.Basic -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Title.Profile -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Title.Todo -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Title.Archive -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Description -> view.copy(
            mode = BlockView.Mode.READ
        )
        is BlockView.Relation.Placeholder -> view.copy(
            isSelected = isSelected
        )
        is BlockView.Relation.Related -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Default.Text -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Default.Card.SmallIcon -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Default.Card.MediumIcon -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Default.Card.SmallIconCover -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Default.Card.MediumIconCover -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Archived -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Deleted -> view.copy(
            isSelected = isSelected
        )
        is BlockView.LinkToObject.Loading -> view.copy(
            isSelected = isSelected
        )
        is BlockView.DividerDots -> view.copy(
            isSelected = isSelected
        )
        is BlockView.DividerLine -> view.copy(
            isSelected = isSelected
        )
        is BlockView.Latex -> view.copy(
            isSelected = isSelected
        )
        is BlockView.TableOfContents -> view.copy(
            isSelected = isSelected
        )
        is BlockView.Table -> view.copy(
            isSelected = isSelected,
            cells = view.cells.updateCellsMode(mode = BlockView.Mode.READ),
        )
        else -> view.also { check(view !is BlockView.Permission) }
    }
}

fun List<BlockView>.updateCursorAndEditMode(
    target: Id,
    cursor: Int?
): List<BlockView> = map { view ->
    val isTarget = view.id == target
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Checkbox -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Bulleted -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Numbered -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Highlight -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Header.One -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Header.Two -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Header.Three -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Toggle -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Text.Callout -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Code -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Error.File -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Error.Video -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Error.Picture -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Error.Bookmark -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Upload.File -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Upload.Video -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Upload.Picture -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.MediaPlaceholder.File -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.MediaPlaceholder.Video -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.MediaPlaceholder.Bookmark -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.MediaPlaceholder.Picture -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.Media.File -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Media.Video -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Media.Bookmark -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Media.Picture -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Description -> view.copy(
            mode = BlockView.Mode.EDIT,
            isFocused = isTarget,
            cursor = if (isTarget) cursor else null
        )
        is BlockView.Title.Basic -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Todo -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Profile -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Archive -> view.copy(mode = BlockView.Mode.EDIT)
        else -> view.also {
            check(view !is BlockView.Permission) { "Actual type is: ${view.getViewType()}" }
        }
    }
}

fun List<BlockView>.toReadMode(): List<BlockView> = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Checkbox -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Bulleted -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Numbered -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Highlight -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Header.One -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Header.Two -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Header.Three -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Toggle -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Text.Callout -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Title.Basic -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Title.Todo -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Title.Profile -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Title.Archive -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Description -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Code -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Error.File -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Error.Video -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Error.Picture -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Error.Bookmark -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Upload.File -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Upload.Video -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Upload.Picture -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.MediaPlaceholder.File -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.MediaPlaceholder.Video -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.MediaPlaceholder.Bookmark -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.MediaPlaceholder.Picture -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Media.File -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Media.Video -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Media.Bookmark -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Media.Picture -> view.copy(mode = BlockView.Mode.READ)
        else -> view.also { check(view !is BlockView.Permission) }
    }
}

fun List<BlockView>.toEditMode(): List<BlockView> = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Checkbox -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Bulleted -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Numbered -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Highlight -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Header.One -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Header.Two -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Header.Three -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Toggle -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Text.Callout -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Code -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Error.File -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Error.Video -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Error.Picture -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Error.Bookmark -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Upload.File -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Upload.Video -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Upload.Picture -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.MediaPlaceholder.File -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.MediaPlaceholder.Video -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.MediaPlaceholder.Bookmark -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.MediaPlaceholder.Picture -> view.copy(
            mode = BlockView.Mode.EDIT,
            isSelected = false
        )
        is BlockView.Media.File -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Media.Video -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Media.Bookmark -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Media.Picture -> view.copy(mode = BlockView.Mode.EDIT, isSelected = false)
        is BlockView.Description -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Basic -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Profile -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Todo -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Archive -> view.copy(mode = BlockView.Mode.EDIT)
        else -> view.also { check(view !is BlockView.Permission) }
    }
}

fun List<BlockView>.clearSearchHighlights(): List<BlockView> = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(searchFields = emptyList())
        is BlockView.Text.Numbered -> view.copy(searchFields = emptyList())
        is BlockView.Text.Bulleted -> view.copy(searchFields = emptyList())
        is BlockView.Text.Checkbox -> view.copy(searchFields = emptyList())
        is BlockView.Text.Toggle -> view.copy(searchFields = emptyList())
        is BlockView.Text.Header.One -> view.copy(searchFields = emptyList())
        is BlockView.Text.Header.Two -> view.copy(searchFields = emptyList())
        is BlockView.Text.Header.Three -> view.copy(searchFields = emptyList())
        is BlockView.Text.Highlight -> view.copy(searchFields = emptyList())
        is BlockView.Text.Callout -> view.copy(searchFields = emptyList())
        is BlockView.Title.Basic -> view.copy(searchFields = emptyList())
        is BlockView.Title.Profile -> view.copy(searchFields = emptyList())
        is BlockView.Title.Todo -> view.copy(searchFields = emptyList())
        is BlockView.Media.Bookmark -> view.copy(searchFields = emptyList())
        is BlockView.Media.File -> view.copy(searchFields = emptyList())
        is BlockView.LinkToObject.Default.Text -> view.copy(searchFields = emptyList())
        is BlockView.LinkToObject.Default.Card.SmallIcon -> view.copy(searchFields = emptyList())
        is BlockView.LinkToObject.Default.Card.MediumIcon -> view.copy(searchFields = emptyList())
        is BlockView.LinkToObject.Default.Card.SmallIconCover -> view.copy(searchFields = emptyList())
        is BlockView.LinkToObject.Default.Card.MediumIconCover -> view.copy(searchFields = emptyList())
        is BlockView.LinkToObject.Archived -> view.copy(searchFields = emptyList())
        is BlockView.Table -> view.copy(cells = view.cells.map { cell -> cell.clearHighlight() })
        else -> view.also { check(view !is BlockView.Searchable) }
    }
}

fun List<BlockView>.highlight(
    highlighter: (List<Pair<String, String>>) -> List<BlockView.Searchable.Field>
) = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Numbered -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Bulleted -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Checkbox -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Toggle -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Header.One -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Header.Two -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Header.Three -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Text.Highlight -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text)
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Title.Basic -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Title.Todo -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Title.Profile -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Media.Bookmark -> {
            val fields = listOf(
                SEARCH_FIELD_DESCRIPTION_KEY to view.description.orEmpty(),
                SEARCH_FIELD_TITLE_KEY to view.title.orEmpty(),
                SEARCH_FIELD_URL_KEY to view.url
            )
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Media.File -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.name.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.LinkToObject.Default.Text -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.LinkToObject.Default.Card.SmallIcon -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.LinkToObject.Default.Card.MediumIcon -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.LinkToObject.Default.Card.SmallIconCover -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.LinkToObject.Default.Card.MediumIconCover -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.LinkToObject.Archived -> {
            val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to view.text.orEmpty())
            view.copy(searchFields = highlighter(fields))
        }
        is BlockView.Table -> {
            val updatedCells = view.cells.map { it.addHighlight(highlighter) }
            view.copy(cells = updatedCells)
        }
        else -> view.also { check(view !is BlockView.Searchable) }
    }
}

fun BlockView.Table.Cell.addHighlight(
    highlighter: (List<Pair<String, String>>) -> List<BlockView.Searchable.Field>
): BlockView.Table.Cell {
    val paragraph = block
    return if (paragraph != null) {
        val fields = listOf(DEFAULT_SEARCH_FIELD_KEY to paragraph.text)
        this.copy(block = paragraph.copy(searchFields = highlighter(fields)))
    } else {
        this
    }
}

fun BlockView.Table.Cell.clearHighlight(): BlockView.Table.Cell {
    val paragraph = block
    return if (paragraph != null) {
        this.copy(block = paragraph.copy(searchFields = emptyList()))
    } else {
        this
    }
}

fun BlockView.setHighlight(
    highlights: List<BlockView.Searchable.Field>
): BlockView = when (this) {
    is BlockView.Text.Paragraph -> copy(searchFields = highlights)
    is BlockView.Text.Numbered -> copy(searchFields = highlights)
    is BlockView.Text.Bulleted -> copy(searchFields = highlights)
    is BlockView.Text.Checkbox -> copy(searchFields = highlights)
    is BlockView.Text.Toggle -> copy(searchFields = highlights)
    is BlockView.Text.Header.One -> copy(searchFields = highlights)
    is BlockView.Text.Header.Two -> copy(searchFields = highlights)
    is BlockView.Text.Header.Three -> copy(searchFields = highlights)
    is BlockView.Text.Highlight -> copy(searchFields = highlights)
    is BlockView.Text.Callout -> copy(searchFields = highlights)
    is BlockView.Title.Basic -> copy(searchFields = highlights)
    is BlockView.Title.Profile -> copy(searchFields = highlights)
    is BlockView.Title.Todo -> copy(searchFields = highlights)
    is BlockView.Media.Bookmark -> copy(searchFields = highlights)
    is BlockView.Media.File -> copy(searchFields = highlights)
    is BlockView.LinkToObject.Default.Text -> copy(searchFields = highlights)
    is BlockView.LinkToObject.Default.Card.SmallIcon -> copy(searchFields = highlights)
    is BlockView.LinkToObject.Default.Card.MediumIcon -> copy(searchFields = highlights)
    is BlockView.LinkToObject.Default.Card.SmallIconCover -> copy(searchFields = highlights)
    is BlockView.LinkToObject.Default.Card.MediumIconCover -> copy(searchFields = highlights)
    is BlockView.LinkToObject.Archived -> copy(searchFields = highlights)
    else -> this.also { check(this !is BlockView.Searchable) }
}

fun BlockView.setGhostEditorSelection(
    ghostEditorSelection: IntRange?
): BlockView = when (this) {
    is BlockView.Text.Paragraph -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Numbered -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Bulleted -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Checkbox -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Toggle -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Header.One -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Header.Two -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Header.Three -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Highlight -> copy(ghostEditorSelection = ghostEditorSelection)
    is BlockView.Text.Callout -> copy(ghostEditorSelection = ghostEditorSelection)
    else -> this.also { check(this !is BlockView.SupportGhostEditorSelection) }
}

fun List<BlockView>.nextSearchTarget(): List<BlockView> {
    val currentTargetView: BlockView? = findHighlightedTarget()
    if (currentTargetView == null) {
        val nextCandidate: BlockView? = findFirstHighlighted()
        if (nextCandidate == null) {
            return this
        } else {
            check(nextCandidate is BlockView.Searchable)
            val nextFieldCandidateIndex = nextCandidate.searchFields.indexOfFirst { field ->
                field.highlights.isNotEmpty()
            }
            val highlights = nextCandidate.searchFields.mapIndexed { index, field ->
                if (index == nextFieldCandidateIndex) {
                    field.copy(target = field.highlights.first())
                } else {
                    field
                }
            }
            return highlightBlockById(
                id = nextCandidate.id,
                highlights = highlights
            )
        }
    } else {
        check(currentTargetView is BlockView.Searchable)
        val currentField = currentTargetView.searchFields.first { it.isTargeted }
        val currentHighlightIndex = currentField.highlights.indexOf(currentField.target)
        if (currentHighlightIndex < currentField.highlights.size.dec()) {
            val highlights = currentTargetView.searchFields.map { field ->
                if (field.key == currentField.key) {
                    field.copy(target = currentField.highlights[currentHighlightIndex.inc()])
                } else {
                    field
                }
            }
            return highlightBlockById(
                id = currentTargetView.id,
                highlights = highlights
            )
        } else {
            val currentTargetFieldIndex = currentTargetView.searchFields.indexOf(currentField)
            val nextFields = currentTargetView.searchFields.subList(
                currentTargetFieldIndex.inc(),
                currentTargetView.searchFields.size
            )
            val nextFieldTargetCandidate = nextFields.find { it.highlights.isNotEmpty() }
            if (nextFieldTargetCandidate != null) {
                val highlights = currentTargetView.searchFields.map { field ->
                    when (field.key) {
                        currentField.key -> field.copy(target = IntRange.EMPTY)
                        nextFieldTargetCandidate.key -> {
                            field.copy(target = nextFieldTargetCandidate.highlights.first())
                        }
                        else -> field
                    }
                }
                return highlightBlockById(
                    id = currentTargetView.id,
                    highlights = highlights
                )
            } else {
                val nextViews = subListFromIdToEnd(id = currentTargetView.id)
                val nextCandidate = nextViews.findFirstHighlighted()
                if (nextCandidate == null) {
                    return this
                } else {
                    check(nextCandidate is BlockView.Searchable)
                    val nextFieldCandidateIndex = nextCandidate.searchFields.indexOfFirst { field ->
                        field.highlights.isNotEmpty()
                    }
                    return map { view ->
                        if (view is BlockView.Table) {
                            val cells = view.cells
                            val updatedCells = cells.map { cell ->
                                val block = cell.block
                                if (block == null) {
                                    cell
                                } else {
                                    val updatedBlock = when (block.id) {
                                        nextCandidate.id -> block.copy(
                                            searchFields = nextCandidate.searchFields.mapIndexed { index, field ->
                                                if (index == nextFieldCandidateIndex) {
                                                    field.copy(target = field.highlights.first())
                                                } else {
                                                    field
                                                }
                                            }
                                        )
                                        currentTargetView.id -> block.copy(
                                            searchFields = currentTargetView.searchFields.map { field ->
                                                field.copy(target = IntRange.EMPTY)
                                            }
                                        )
                                        else -> block
                                    }
                                    cell.copy(block = updatedBlock)
                                }
                            }
                            view.copy(cells = updatedCells)
                        } else {
                            when (view.id) {
                                nextCandidate.id -> view.setHighlight(
                                    nextCandidate.searchFields.mapIndexed { index, field ->
                                        if (index == nextFieldCandidateIndex) {
                                            field.copy(target = field.highlights.first())
                                        } else {
                                            field
                                        }
                                    }
                                )
                                currentTargetView.id -> view.setHighlight(
                                    currentTargetView.searchFields.map { field ->
                                        field.copy(target = IntRange.EMPTY)
                                    }
                                )
                                else -> view
                            }
                        }
                    }
                }
            }
        }
    }
}

fun List<BlockView>.previousSearchTarget(): List<BlockView> {
    val currentTargetView = findHighlightedTarget()
    if (currentTargetView == null) {
        return this
    } else {
        check(currentTargetView is BlockView.Searchable)
        val currentField = currentTargetView.searchFields.first { it.isTargeted }
        val currentHighlightIndex = currentField.highlights.indexOf(currentField.target)
        if (currentHighlightIndex > 0) {
            val highlights = currentTargetView.searchFields.map { field ->
                if (field.key == currentField.key) {
                    field.copy(target = currentField.highlights[currentHighlightIndex.dec()])
                } else {
                    field
                }
            }
            return highlightBlockById(
                id = currentTargetView.id,
                highlights = highlights
            )
        } else {
            val currentTargetFieldIndex = currentTargetView.searchFields.indexOf(currentField)
            val previousFields = currentTargetView.searchFields.subList(
                0,
                currentTargetFieldIndex
            )
            val previousFieldTargetCandidate =
                previousFields.findLast { it.highlights.isNotEmpty() }
            if (previousFieldTargetCandidate != null) {
                val highlights = currentTargetView.searchFields.map { field ->
                    when (field.key) {
                        currentField.key -> field.copy(target = IntRange.EMPTY)
                        previousFieldTargetCandidate.key -> {
                            field.copy(target = previousFieldTargetCandidate.highlights.last())
                        }
                        else -> field
                    }
                }
                return highlightBlockById(
                    id = currentTargetView.id,
                    highlights = highlights
                )
            } else {
                val previousViews = subListFrom0ToId(id = currentTargetView.id)
                val previousCandidate = previousViews.findLastHighlighted()
                if (previousCandidate == null) {
                    return this
                } else {
                    check(previousCandidate is BlockView.Searchable)
                    return map { view ->
                        if (view is BlockView.Table) {
                            val cells = view.cells
                            val updatedCells = cells.map { cell ->
                                val block = cell.block
                                if (block == null) {
                                    cell
                                } else {
                                    val updatedBlock = when (block.id) {
                                        previousCandidate.id -> block.copy(
                                            searchFields = previousCandidate.searchFields.mapIndexed { index, field ->
                                                if (index == previousCandidate.searchFields.size.dec()) {
                                                    field.copy(target = field.highlights.last())
                                                } else {
                                                    field
                                                }
                                            }
                                        )
                                        currentTargetView.id -> block.copy(
                                            searchFields = currentTargetView.searchFields.map { field ->
                                                field.copy(target = IntRange.EMPTY)
                                            }
                                        )
                                        else -> block
                                    }
                                    cell.copy(block = updatedBlock)
                                }
                            }

                            view.copy(cells = updatedCells)
                        } else {
                            when (view.id) {
                                previousCandidate.id -> view.setHighlight(
                                    previousCandidate.searchFields.mapIndexed { index, field ->
                                        if (index == previousCandidate.searchFields.size.dec()) {
                                            field.copy(target = field.highlights.last())
                                        } else {
                                            field
                                        }
                                    }
                                )
                                currentTargetView.id -> view.setHighlight(
                                    currentTargetView.searchFields.map { field ->
                                        field.copy(target = IntRange.EMPTY)
                                    }
                                )
                                else -> view
                            }
                        }
                    }
                }
            }
        }
    }
}

fun BlockView.updateSelection(newSelection: Boolean) = when (this) {
    is BlockView.Text.Paragraph -> copy(isSelected = newSelection)
    is BlockView.Text.Header.One -> copy(isSelected = newSelection)
    is BlockView.Text.Header.Two -> copy(isSelected = newSelection)
    is BlockView.Text.Header.Three -> copy(isSelected = newSelection)
    is BlockView.Text.Highlight -> copy(isSelected = newSelection)
    is BlockView.Text.Checkbox -> copy(isSelected = newSelection)
    is BlockView.Text.Bulleted -> copy(isSelected = newSelection)
    is BlockView.Text.Numbered -> copy(isSelected = newSelection)
    is BlockView.Text.Toggle -> copy(isSelected = newSelection)
    is BlockView.Text.Callout -> copy(isSelected = newSelection)
    is BlockView.Media.File -> copy(isSelected = newSelection)
    is BlockView.Upload.File -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.File -> copy(isSelected = newSelection)
    is BlockView.Error.File -> copy(isSelected = newSelection)
    is BlockView.Media.Video -> copy(isSelected = newSelection)
    is BlockView.Upload.Video -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.Video -> copy(isSelected = newSelection)
    is BlockView.Error.Video -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Default.Text -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Default.Card.SmallIcon -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Default.Card.MediumIcon -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Default.Card.SmallIconCover -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Default.Card.MediumIconCover -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Archived -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Deleted -> copy(isSelected = newSelection)
    is BlockView.LinkToObject.Loading -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.Bookmark -> copy(isSelected = newSelection)
    is BlockView.Media.Bookmark -> copy(isSelected = newSelection)
    is BlockView.Error.Bookmark -> copy(isSelected = newSelection)
    is BlockView.Media.Picture -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.Picture -> copy(isSelected = newSelection)
    is BlockView.Error.Picture -> copy(isSelected = newSelection)
    is BlockView.Upload.Picture -> copy(isSelected = newSelection)
    is BlockView.DividerLine -> copy(isSelected = newSelection)
    is BlockView.DividerDots -> copy(isSelected = newSelection)
    is BlockView.Code -> copy(isSelected = newSelection)
    is BlockView.Relation.Related -> copy(isSelected = newSelection)
    is BlockView.Relation.Placeholder -> copy(isSelected = newSelection)
    is BlockView.Latex -> copy(isSelected = newSelection)
    is BlockView.TableOfContents -> copy(isSelected = newSelection)
    is BlockView.Table -> copy(isSelected = newSelection)
    else -> this.also {
        if (this is BlockView.Selectable)
            Timber.e("Error when change selection for Selectable BlockView $this")
    }
}

/**
 *  Cut part of the text, shift marks in Text BlockView and set cursor position to {from}
 *  @param from cut text starting from this position, should be positive or zero
 *  @param partLength length of the cut text, should be positive or zero
 *  For tests see BlockViewCutTextTest
 */
fun BlockView.Text.cutPartOfText(
    from: Int,
    partLength: Int
): BlockView.Text {
    check(partLength >= 0) { Timber.e("partLength should be positive or zero") }
    val to = from + partLength
    val length = -partLength
    var updatedText = text
    var updatedMarks = marks
    var updatedCursor = cursor
    if (from <= to && from in 0..text.length && to in 0..text.length) {
        updatedText = text.removeRange(startIndex = from, endIndex = to)
        updatedMarks = marks.shift(from, length)
        updatedCursor = from
    } else {
        Timber.e("Error while trying to cut part of text, from:$from, partLength:$partLength, textSize:${text.length} ")
    }
    return when (this) {
        is BlockView.Text.Bulleted -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Checkbox -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Header.One -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Header.Three -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Header.Two -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Highlight -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Numbered -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Paragraph -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Toggle -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
        is BlockView.Text.Callout -> copy(
            text = updatedText,
            marks = updatedMarks,
            cursor = updatedCursor
        )
    }
}

fun List<BlockView>.update(blockView: BlockView) = this.map {
    if (it.id == blockView.id) blockView else it
}

fun Document.getLinkAppearanceMenu(
    blockId: Id,
    details: Block.Details
): BlockView.Appearance.Menu? {
    val block = this.find { it.id == blockId }
    val content = block?.content
    return if (block != null && content is Block.Content.Link) {
        val target = content.asLink().target
        val obj = ObjectWrapper.Basic(details.details[target]?.map ?: emptyMap())
        val factory = LinkAppearanceFactory(
            content = content,
            layout = obj.layout
        )
        return factory.createAppearanceMenuItems()
    } else {
        null
    }
}

fun List<BlockView>.updateTableOfContentsViews(header: BlockView.Text.Header): List<BlockView> {
    val updated = this.map { view ->
        if (view is BlockView.TableOfContents) {
            val items = view.items.map { item ->
                if (item.id == header.id) {
                    item.copy(name = header.text)
                } else {
                    item
                }
            }
            view.copy(items = items)
        } else {
            view
        }
    }
    return updated
}

fun List<BlockView>.fillTableOfContents(): List<BlockView> {
    val headers = this.filterIsInstance<BlockView.Text.Header>()
    var isH1Present = false
    var isH2Present = false
    val items = mutableListOf<BlockView.TableOfContentsItem>()
    headers.forEachIndexed { index, b ->
        var depth = 0
        when (b) {
            is BlockView.Text.Header.One -> {
                isH1Present = true
                isH2Present = false
            }
            is BlockView.Text.Header.Two -> {
                isH2Present = true
                if (isH1Present) depth += 1
            }
            is BlockView.Text.Header.Three -> {
                if (isH1Present) depth += 1
                if (isH2Present) depth += 1
            }
            else -> {}
        }
        val item = BlockView.TableOfContentsItem(
            id = b.id,
            name = b.text,
            depth = depth
        )
        items.add(item)
    }
    return this.map { view ->
        if (view is BlockView.TableOfContents) {
            view.copy(
                items = items
            )
        } else {
            view
        }
    }
}

fun BlockView.Text.isStyleClearable(): Boolean {
    return this.isListBlock || this is BlockView.Text.Highlight
}

fun List<BlockView>.findTableCellView(id: Id): BlockView.Table.Cell? {
    forEach { blockView ->
        if (blockView is BlockView.Table) {
            val cells = blockView.cells
            val cell = cells.find { it.getId() == id }
            if (cell != null) return cell
        }
    }
    return null
}

fun List<BlockView>.findHighlightedTarget(): BlockView? {
    forEach { block ->
        if (block is BlockView.Table) {
            val target =
                block.getTextCells().find { cell -> cell.searchFields.any { it.isTargeted } }
            if (target != null) return target
        } else {
            val isTargeted =
                block is BlockView.Searchable && block.searchFields.any { it.isTargeted }
            if (isTargeted) return block
        }
    }
    return null
}

fun List<BlockView>.findFirstHighlighted(): BlockView? {
    forEach { block ->
        if (block is BlockView.Table) {
            val target =
                block.getTextCells()
                    .find { cell -> cell.searchFields.any { it.highlights.isNotEmpty() } }
            if (target != null) return target
        } else {
            val isHighlighted =
                block is BlockView.Searchable && block.searchFields.any { it.highlights.isNotEmpty() }
            if (isHighlighted) return block
        }
    }
    return null
}

fun List<BlockView>.findLastHighlighted(): BlockView? {
    var lastHighlighted: BlockView? = null
    forEach { block ->
        if (block is BlockView.Table) {
            val target =
                block.getTextCells()
                    .findLast { cell -> cell.searchFields.any { it.highlights.isNotEmpty() } }
            if (target != null) lastHighlighted = target
        } else {
            val isHighlighted =
                block is BlockView.Searchable && block.searchFields.any { it.highlights.isNotEmpty() }
            if (isHighlighted) lastHighlighted = block
        }
    }
    return lastHighlighted
}

fun BlockView.Table.getTextCells(): List<BlockView.Text.Paragraph> {
    return cells.mapNotNull { cell -> cell.block }
}

fun List<BlockView>.highlightBlockById(
    id: Id,
    highlights: List<BlockView.Searchable.Field>
): List<BlockView> = map { view ->
    if (view is BlockView.Table) {
        view.copy(cells = view.cells.map { cell ->
            if (cell.getId() == id) {
                cell.addHighlight { highlights }
            } else {
                cell
            }
        })
    } else {
        if (view.id == id) {
            view.setHighlight(highlights)
        } else {
            view
        }
    }
}

fun List<BlockView>.subListFromIdToEnd(id: Id): List<BlockView> {
    val indexOfFirst = indexOfFirst { it.id == id }
    if (indexOfFirst != -1) {
        return subList(indexOfFirst.inc(), this.size)
    } else {
        val blockTable = findBlockTableByCellId(cellId = id)
        return if (blockTable != null) {
            val list = mutableListOf<BlockView>()
            val cells = blockTable.cells
            val indexOfTarget = cells.indexOfFirst { it.block?.id == id }
            val subListOfCells = if (indexOfTarget != -1 && indexOfTarget.inc() <= cells.size) {
                cells.subList(indexOfTarget.inc(), cells.size)
            } else {
                cells
            }
            list.add(blockTable.copy(cells = subListOfCells))
            val tableIndex = this.indexOfFirst { it.id == blockTable.id }
            list.addAll(subList(tableIndex.inc(), size))
            list
        } else {
            this
        }
    }
}

fun List<BlockView>.subListFrom0ToId(id: Id): List<BlockView> {
    val indexOfFirst = indexOfFirst { it.id == id }
    if (indexOfFirst != -1) {
        return subList(0, indexOfFirst)
    } else {
        val blockTable = findBlockTableByCellId(cellId = id)
        return if (blockTable != null) {
            val list = mutableListOf<BlockView>()
            val cells = blockTable.cells
            val indexOfTarget = cells.indexOfFirst { it.block?.id == id }
            val subListOfCells = if (indexOfTarget != -1 && indexOfTarget <= cells.size) {
                cells.subList(0, indexOfTarget)
            } else {
                cells
            }
            val tableIndex = this.indexOfFirst { it.id == blockTable.id }
            list.addAll(subList(0, tableIndex))
            list.add(blockTable.copy(cells = subListOfCells))
            list
        } else {
            this
        }
    }
}

fun List<BlockView>.findBlockTableByCellId(cellId: Id): BlockView.Table? {
    this.forEach { blockView ->
        if (blockView is BlockView.Table) {
            val cells = blockView.cells
            val index = cells.indexOfFirst { it.block?.id == cellId }
            if (index != -1) {
                return blockView
            }
        }
    }
    return null
}

/**
 * Find position of highlighted targeted text block or table block in case when cell is targeting
 */
fun List<BlockView>.findSearchResultPosition(): Int {
    val target = findHighlightedTarget() ?: return -1
    forEachIndexed { index, blockView ->
        if (blockView is BlockView.Table) {
            val textCells = blockView.getTextCells()
            val cellIndex = textCells.indexOfFirst { it.id == target.id }
            if (cellIndex != -1) return index
        } else {
            if (blockView.id == target.id) return index
        }
    }
    return -1
}

fun List<BlockView>.getOnFocusChangedEvent(blockId: Id): ControlPanelMachine.Event.OnFocusChanged? {
    val blockView = find { it.id == blockId }
    return if (blockView != null) {
        when (blockView) {
            is BlockView.Title -> ControlPanelMachine.Event.OnFocusChanged(
                id = blockId,
                type = ControlPanelState.Toolbar.Main.TargetBlockType.Title
            )
            else -> ControlPanelMachine.Event.OnFocusChanged(
                id = blockId,
                type = ControlPanelState.Toolbar.Main.TargetBlockType.Any
            )
        }

    } else {
        val cellView = findTableCellView(id = blockId)
        if (cellView != null) {
            ControlPanelMachine.Event.OnFocusChanged(
                id = blockId,
                type = ControlPanelState.Toolbar.Main.TargetBlockType.Cell
            )
        } else {
            null
        }
    }
}