package com.agileburo.anytype.core_ui.extensions

import com.agileburo.anytype.core_ui.features.page.BlockView

fun BlockView.updateSelection(newSelection: Boolean) = when (this) {
    is BlockView.Paragraph -> copy(isSelected = newSelection)
    is BlockView.Header.One -> copy(isSelected = newSelection)
    is BlockView.Header.Two -> copy(isSelected = newSelection)
    is BlockView.Header.Three -> copy(isSelected = newSelection)
    is BlockView.Highlight -> copy(isSelected = newSelection)
    is BlockView.Code -> copy(isSelected = newSelection)
    is BlockView.Checkbox -> copy(isSelected = newSelection)
    is BlockView.Bulleted -> copy(isSelected = newSelection)
    is BlockView.Numbered -> copy(isSelected = newSelection)
    is BlockView.Toggle -> copy(isSelected = newSelection)
    is BlockView.Media.File -> copy(isSelected = newSelection)
    is BlockView.Upload.File -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.File -> copy(isSelected = newSelection)
    is BlockView.Error.File -> copy(isSelected = newSelection)
    is BlockView.Media.Video -> copy(isSelected = newSelection)
    is BlockView.Upload.Video -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.Video -> copy(isSelected = newSelection)
    is BlockView.Error.Video -> copy(isSelected = newSelection)
    is BlockView.Page -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.Bookmark -> copy(isSelected = newSelection)
    is BlockView.Media.Bookmark -> copy(isSelected = newSelection)
    is BlockView.Error.Bookmark -> copy(isSelected = newSelection)
    is BlockView.Media.Picture -> copy(isSelected = newSelection)
    is BlockView.MediaPlaceholder.Picture -> copy(isSelected = newSelection)
    is BlockView.Error.Picture -> copy(isSelected = newSelection)
    is BlockView.Upload.Picture -> copy(isSelected = newSelection)
    else -> this
}