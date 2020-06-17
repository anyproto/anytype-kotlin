package com.agileburo.anytype.core_ui.extensions

import com.agileburo.anytype.core_ui.features.page.BlockView

fun BlockView.updateSelection(newSelection: Boolean) = when (this) {
    is BlockView.Paragraph -> copy(isSelected = newSelection)
    is BlockView.HeaderOne -> copy(isSelected = newSelection)
    is BlockView.HeaderTwo -> copy(isSelected = newSelection)
    is BlockView.HeaderThree -> copy(isSelected = newSelection)
    is BlockView.Highlight -> copy(isSelected = newSelection)
    is BlockView.Code -> copy(isSelected = newSelection)
    is BlockView.Checkbox -> copy(isSelected = newSelection)
    is BlockView.Bulleted -> copy(isSelected = newSelection)
    is BlockView.Numbered -> copy(isSelected = newSelection)
    is BlockView.Toggle -> copy(isSelected = newSelection)
    is BlockView.File.View -> copy(isSelected = newSelection)
    is BlockView.File.Upload -> copy(isSelected = newSelection)
    is BlockView.File.Placeholder -> copy(isSelected = newSelection)
    is BlockView.File.Error -> copy(isSelected = newSelection)
    is BlockView.Video.View -> copy(isSelected = newSelection)
    is BlockView.Video.Upload -> copy(isSelected = newSelection)
    is BlockView.Video.Placeholder -> copy(isSelected = newSelection)
    is BlockView.Video.Error -> copy(isSelected = newSelection)
    is BlockView.Page -> copy(isSelected = newSelection)
    is BlockView.Bookmark.Placeholder -> copy(isSelected = newSelection)
    is BlockView.Bookmark.View -> copy(isSelected = newSelection)
    is BlockView.Bookmark.Error -> copy(isSelected = newSelection)
    is BlockView.Picture.View -> copy(isSelected = newSelection)
    is BlockView.Picture.Placeholder -> copy(isSelected = newSelection)
    is BlockView.Picture.Error -> copy(isSelected = newSelection)
    is BlockView.Picture.Upload -> copy(isSelected = newSelection)
    else -> this
}