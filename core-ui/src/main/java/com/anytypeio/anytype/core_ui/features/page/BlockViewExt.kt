package com.anytypeio.anytype.core_ui.features.page

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
        is BlockView.Title.Document -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Title.Profile -> view.copy(mode = BlockView.Mode.READ)
        is BlockView.Title.Archive -> view.copy(mode = BlockView.Mode.READ)
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
        else -> view
    }
}

fun List<BlockView>.toEditMode(): List<BlockView> = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Checkbox -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Bulleted -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Numbered -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Highlight -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Header.One -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Header.Two -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Header.Three -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Text.Toggle -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Document -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Profile -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Title.Archive -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Code -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Error.File -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Error.Video -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Error.Picture -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Error.Bookmark -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Upload.File -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Upload.Video -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Upload.Picture -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.MediaPlaceholder.File -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.MediaPlaceholder.Video -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.MediaPlaceholder.Bookmark -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.MediaPlaceholder.Picture -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Media.File -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Media.Video -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Media.Bookmark -> view.copy(mode = BlockView.Mode.EDIT)
        is BlockView.Media.Picture -> view.copy(mode = BlockView.Mode.EDIT)
        else -> view
    }
}

fun List<BlockView>.clearSearchHighlights(): List<BlockView> = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(highlights = emptySet())
        is BlockView.Text.Numbered -> view.copy(highlights = emptySet())
        is BlockView.Text.Bulleted -> view.copy(highlights = emptySet())
        is BlockView.Text.Checkbox -> view.copy(highlights = emptySet())
        is BlockView.Text.Toggle -> view.copy(highlights = emptySet())
        is BlockView.Text.Header.One -> view.copy(highlights = emptySet())
        is BlockView.Text.Header.Two -> view.copy(highlights = emptySet())
        is BlockView.Text.Header.Three -> view.copy(highlights = emptySet())
        is BlockView.Text.Highlight -> view.copy(highlights = emptySet())
        is BlockView.Title.Document -> view.copy(highlights = emptySet())
        is BlockView.Title.Profile -> view.copy(highlights = emptySet())
        else -> view
    }
}

fun List<BlockView>.highlight(highlighter: (String) -> Set<IntRange>) = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Numbered -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Bulleted -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Checkbox -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Toggle -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Header.One -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Header.Two -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Header.Three -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Text.Highlight -> {
            view.copy(highlights = highlighter(view.text))
        }
        is BlockView.Title.Document -> {
            view.copy(highlights = highlighter(view.text ?: ""))
        }
        is BlockView.Title.Profile -> {
            view.copy(highlights = highlighter(view.text ?: ""))
        }
        else -> view
    }
}