package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock

object TurnIntoConstants {

    fun excludedCategoriesForTextExperimental() : List<String> {
        return listOf()
    }

    fun excludedCategoriesForTextStable() : List<String> {
        return listOf(UiBlock.Category.RELATION.name)
    }

    fun excludeTypesForText() = listOf(
        UiBlock.FILE.name,
        UiBlock.IMAGE.name,
        UiBlock.VIDEO.name,
        UiBlock.BOOKMARK.name,
        UiBlock.LINE_DIVIDER.name,
        UiBlock.THREE_DOTS.name,
        UiBlock.LINK_TO_OBJECT.name,
        UiBlock.RELATION.name,
    )

    fun excludeTypesForLineDivider() = listOf(
        UiBlock.CODE.name,
        UiBlock.LINE_DIVIDER.name
    )

    fun excludeTypesForDotsDivider() = listOf(
        UiBlock.CODE.name,
        UiBlock.THREE_DOTS.name
    )

    fun excludeCategoriesForDivider() = listOf(
        UiBlock.Category.TEXT.name,
        UiBlock.Category.LIST.name,
        UiBlock.Category.OBJECT.name,
        UiBlock.Category.RELATION.name
    )
}