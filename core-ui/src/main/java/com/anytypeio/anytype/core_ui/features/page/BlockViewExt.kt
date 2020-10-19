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
        else -> view.also { check(view !is BlockView.Permission) }
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
        else -> view.also { check(view !is BlockView.Permission) }
    }
}

fun List<BlockView>.clearSearchHighlights(): List<BlockView> = map { view ->
    when (view) {
        is BlockView.Text.Paragraph -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Text.Numbered -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Text.Bulleted -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Text.Checkbox -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Text.Toggle -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Text.Header.One -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Text.Header.Two -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Text.Header.Three -> view.copy(
            highlights = emptyList(),
            target = IntRange.EMPTY
        )
        is BlockView.Text.Highlight -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Title.Document -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        is BlockView.Title.Profile -> view.copy(highlights = emptyList(), target = IntRange.EMPTY)
        else -> view.also { check(view !is BlockView.Searchable) }
    }
}

fun List<BlockView>.highlight(highlighter: (String) -> List<IntRange>) = map { view ->
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
        else -> view.also { check(view !is BlockView.Searchable) }
    }
}

fun List<BlockView>.nextSearchTarget(): List<BlockView> {
    val currentSearchTargetView = find { view ->
        view is BlockView.Searchable && !view.target.isEmpty()
    }
    if (currentSearchTargetView == null) {
        val nextTargetCandidate = find { view ->
            view is BlockView.Searchable && view.highlights.isNotEmpty()
        }
        if (nextTargetCandidate == null) {
            return this
        } else {
            check(nextTargetCandidate is BlockView.Searchable)
            val nextSearchTargetRange = nextTargetCandidate.highlights.first()
            return map { view ->
                if (view.id == nextTargetCandidate.id) {
                    when (view) {
                        is BlockView.Text.Paragraph -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Numbered -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Bulleted -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Checkbox -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Toggle -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Header.One -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Header.Two -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Header.Three -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Highlight -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Title.Document -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Title.Profile -> view.copy(target = nextSearchTargetRange)
                        else -> view.also { check(view !is BlockView.Searchable) }
                    }
                } else {
                    view
                }
            }
        }
    } else {
        check(currentSearchTargetView is BlockView.Searchable)
        val index = currentSearchTargetView.highlights.indexOf(currentSearchTargetView.target)
        if (index < currentSearchTargetView.highlights.size - 1) {
            val nextSearchTargetRange = currentSearchTargetView.highlights[index.inc()]
            return map { view ->
                if (view.id == currentSearchTargetView.id) {
                    when (view) {
                        is BlockView.Text.Paragraph -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Numbered -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Bulleted -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Checkbox -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Toggle -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Header.One -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Header.Two -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Header.Three -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Text.Highlight -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Title.Document -> view.copy(target = nextSearchTargetRange)
                        is BlockView.Title.Profile -> view.copy(target = nextSearchTargetRange)
                        else -> view.also { check(view !is BlockView.Searchable) }
                    }
                } else {
                    view
                }
            }
        } else {
            val nextViews = subList(indexOf(currentSearchTargetView).inc(), size)
            val nextTargetCandidate = nextViews.find { view ->
                view is BlockView.Searchable && view.highlights.isNotEmpty()
            }
            if (nextTargetCandidate == null) {
                return this
            } else {
                check(nextTargetCandidate is BlockView.Searchable)
                return map { view ->
                    val range: IntRange = if (view.id == nextTargetCandidate.id)
                        nextTargetCandidate.highlights.first()
                    else
                        IntRange.EMPTY
                    when (view) {
                        is BlockView.Text.Paragraph -> view.copy(target = range)
                        is BlockView.Text.Numbered -> view.copy(target = range)
                        is BlockView.Text.Bulleted -> view.copy(target = range)
                        is BlockView.Text.Checkbox -> view.copy(target = range)
                        is BlockView.Text.Toggle -> view.copy(target = range)
                        is BlockView.Text.Header.One -> view.copy(target = range)
                        is BlockView.Text.Header.Two -> view.copy(target = range)
                        is BlockView.Text.Header.Three -> view.copy(target = range)
                        is BlockView.Text.Highlight -> view.copy(target = range)
                        is BlockView.Title.Document -> view.copy(target = range)
                        is BlockView.Title.Profile -> view.copy(target = range)
                        else -> view.also { check(view !is BlockView.Searchable) }
                    }
                }
            }
        }
    }
}

fun List<BlockView>.previousSearchTarget(): List<BlockView> {
    val currentSearchTargetView = find { view ->
        view is BlockView.Searchable && !view.target.isEmpty()
    }
    if (currentSearchTargetView == null) {
        return this
    } else {
        check(currentSearchTargetView is BlockView.Searchable)
        val index = currentSearchTargetView.highlights.indexOf(currentSearchTargetView.target)
        if (index > 0) {
            val previousSearchTargetRange = currentSearchTargetView.highlights[index.dec()]
            return map { view ->
                if (view.id == currentSearchTargetView.id) {
                    when (view) {
                        is BlockView.Text.Paragraph -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Numbered -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Bulleted -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Checkbox -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Toggle -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Header.One -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Header.Two -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Header.Three -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Text.Highlight -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Title.Document -> view.copy(target = previousSearchTargetRange)
                        is BlockView.Title.Profile -> view.copy(target = previousSearchTargetRange)
                        else -> view.also { check(view !is BlockView.Searchable) }
                    }
                } else {
                    view
                }
            }
        } else {
            val previousViews = subList(0, indexOf(currentSearchTargetView))
            val previousTargetCandidate = previousViews.findLast { view ->
                view is BlockView.Searchable && view.highlights.isNotEmpty()
            }
            if (previousTargetCandidate == null) {
                return this
            } else {
                check(previousTargetCandidate is BlockView.Searchable)
                return map { view ->
                    val range: IntRange = if (view.id == previousTargetCandidate.id)
                        previousTargetCandidate.highlights.last()
                    else
                        IntRange.EMPTY
                    when (view) {
                        is BlockView.Text.Paragraph -> view.copy(target = range)
                        is BlockView.Text.Numbered -> view.copy(target = range)
                        is BlockView.Text.Bulleted -> view.copy(target = range)
                        is BlockView.Text.Checkbox -> view.copy(target = range)
                        is BlockView.Text.Toggle -> view.copy(target = range)
                        is BlockView.Text.Header.One -> view.copy(target = range)
                        is BlockView.Text.Header.Two -> view.copy(target = range)
                        is BlockView.Text.Header.Three -> view.copy(target = range)
                        is BlockView.Text.Highlight -> view.copy(target = range)
                        is BlockView.Title.Document -> view.copy(target = range)
                        is BlockView.Title.Profile -> view.copy(target = range)
                        else -> view.also { check(view !is BlockView.Searchable) }
                    }
                }
            }
        }
    }
}