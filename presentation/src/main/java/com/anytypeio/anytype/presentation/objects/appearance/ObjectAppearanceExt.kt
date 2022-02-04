package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_LARGE
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_MEDIUM
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_SMALL
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_STYLE_CARD

enum class ObjectAppearanceIconState { NONE, SMALL, MEDIUM, LARGE, UNKNOWN }
enum class ObjectAppearanceCoverState { NONE, VISIBLE }
enum class ObjectAppearancePreviewLayoutState { TEXT, CARD }

fun BlockView.Appearance.Params.getObjectAppearanceIconState(): ObjectAppearanceIconState {
    return when {
        !withIcon -> ObjectAppearanceIconState.NONE
        iconSize == LINK_ICON_SIZE_SMALL -> ObjectAppearanceIconState.SMALL
        iconSize == LINK_ICON_SIZE_MEDIUM -> ObjectAppearanceIconState.MEDIUM
        iconSize == LINK_ICON_SIZE_LARGE -> ObjectAppearanceIconState.LARGE
        else -> ObjectAppearanceIconState.UNKNOWN
    }
}

fun BlockView.Appearance.Params.getObjectAppearanceCoverState(): ObjectAppearanceCoverState {
    return if (withCover == true) {
        ObjectAppearanceCoverState.VISIBLE
    } else {
        ObjectAppearanceCoverState.NONE
    }
}

fun BlockView.Appearance.Params.getObjectAppearancePreviewLayoutState(): ObjectAppearancePreviewLayoutState {
    return if (style == LINK_STYLE_CARD) {
        ObjectAppearancePreviewLayoutState.CARD
    } else {
        ObjectAppearancePreviewLayoutState.TEXT
    }
}