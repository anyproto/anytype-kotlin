package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
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

fun Block.Fields.getLinkToObjectAppearanceParams(layout: ObjectType.Layout?): BlockView.Appearance.Params {

    var canHaveIcon = true
    //todo Cover menu option is off. No proper design yet.
    var canHaveCover = false
    var canHaveDescription = true

    var iconSize = this.iconSize ?: LINK_ICON_SIZE_MEDIUM
    val style = this.style ?: BlockView.Appearance.LINK_STYLE_TEXT
    var withIcon = this.withIcon ?: true
    val withName = this.withName ?: true
    var withCover = this.withCover
    var withDescription = this.withDescription

    if (this.style == BlockView.Appearance.LINK_STYLE_TEXT) {
        //canHaveCover = false
        canHaveDescription = false
    }

    if (layout == ObjectType.Layout.TODO) {
        canHaveIcon = false
        withIcon = true
        iconSize = LINK_ICON_SIZE_MEDIUM
    }

    if (layout == ObjectType.Layout.NOTE) {
        canHaveIcon = false
        //canHaveCover = false
        canHaveDescription = false
        withIcon = false
        withCover = false
        withDescription = false
        iconSize = LINK_ICON_SIZE_MEDIUM
    }

    return BlockView.Appearance.Params(
        canHaveIcon = canHaveIcon,
        canHaveCover = canHaveCover,
        canHaveDescription = canHaveDescription,
        iconSize = iconSize,
        style = style,
        withIcon = withIcon,
        withName = withName,
        withCover = withCover,
        withDescription = withDescription
    )
}