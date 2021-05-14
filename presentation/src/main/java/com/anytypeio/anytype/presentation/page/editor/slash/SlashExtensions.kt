package com.anytypeio.anytype.presentation.page.editor.slash

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.relations.RelationListViewModel

fun List<ObjectType>.toView(): List<SlashItem.ObjectType> = map { oType ->
    SlashItem.ObjectType(
        url = oType.url,
        name = oType.name,
        emoji = oType.emoji,
        description = oType.description
    )
}

fun SlashItem.Style.Type.convertToUiBlock() = when (this) {
    SlashItem.Style.Type.Bulleted -> UiBlock.BULLETED
    SlashItem.Style.Type.Callout -> TODO()
    SlashItem.Style.Type.Checkbox -> UiBlock.CHECKBOX
    SlashItem.Style.Type.Heading -> UiBlock.HEADER_TWO
    SlashItem.Style.Type.Highlighted -> UiBlock.HIGHLIGHTED
    SlashItem.Style.Type.Numbered -> UiBlock.NUMBERED
    SlashItem.Style.Type.Subheading -> UiBlock.HEADER_THREE
    SlashItem.Style.Type.Text -> UiBlock.TEXT
    SlashItem.Style.Type.Title -> UiBlock.HEADER_ONE
    SlashItem.Style.Type.Toggle -> UiBlock.TOGGLE
}

fun SlashItem.Style.Markup.convertToMarkType() = when (this) {
    SlashItem.Style.Markup.Bold -> Block.Content.Text.Mark.Type.BOLD
    SlashItem.Style.Markup.Breakthrough -> Block.Content.Text.Mark.Type.STRIKETHROUGH
    SlashItem.Style.Markup.Code -> Block.Content.Text.Mark.Type.KEYBOARD
    SlashItem.Style.Markup.Italic -> Block.Content.Text.Mark.Type.ITALIC
}

object SlashExtensions {

    const val SLASH_CHAR = '/'
    private const val SLASH_ALIGN = "Align"
    const val SLASH_EMPTY_SEARCH_MAX = 3

    //region {SLASH ITEMS FOR WIDGET}
    fun getSlashWidgetMainItems() = listOf(
        SlashItem.Main.Style,
        SlashItem.Main.Media,
        SlashItem.Main.Objects,
        SlashItem.Main.Relations,
        SlashItem.Main.Other,
        SlashItem.Main.Actions,
        SlashItem.Main.Alignment,
        SlashItem.Main.Color,
        SlashItem.Main.Background,
    )

    fun getSlashWidgetStyleItems() = listOf(
        SlashItem.Style.Type.Text,
        SlashItem.Style.Type.Title,
        SlashItem.Style.Type.Heading,
        SlashItem.Style.Type.Subheading,
        SlashItem.Style.Type.Highlighted,
        SlashItem.Style.Type.Callout,
        SlashItem.Style.Type.Checkbox,
        SlashItem.Style.Type.Bulleted,
        SlashItem.Style.Type.Numbered,
        SlashItem.Style.Type.Toggle,
        SlashItem.Style.Markup.Bold,
        SlashItem.Style.Markup.Italic,
        SlashItem.Style.Markup.Breakthrough,
        SlashItem.Style.Markup.Code
    )

    fun getSlashWidgetMediaItems() = listOf(
        SlashItem.Media.File,
        SlashItem.Media.Picture,
        SlashItem.Media.Video,
        SlashItem.Media.Bookmark,
        SlashItem.Media.Code
    )

    fun getSlashWidgetOtherItems() = listOf(
        SlashItem.Other.Line,
        SlashItem.Other.Dots
    )

    fun getSlashWidgetActionItems() = listOf(
        SlashItem.Actions.Delete,
        SlashItem.Actions.Duplicate,
        SlashItem.Actions.Copy,
        SlashItem.Actions.Paste,
        SlashItem.Actions.Move,
        SlashItem.Actions.MoveTo,
        SlashItem.Actions.CleanStyle
    )

    fun getSlashWidgetAlignmentItems() = listOf(
        SlashItem.Alignment.Left,
        SlashItem.Alignment.Center,
        SlashItem.Alignment.Right
    )

    fun getSlashWidgetObjectTypeItems(objectTypes: List<ObjectType>): List<SlashItem> =
        listOf(SlashItem.Subheader.ObjectTypeWithBlack) + objectTypes.toView()

    fun getSlashWidgetRelationItems(relations: List<RelationListViewModel.Model>): List<RelationListViewModel.Model> =
        listOf(RelationListViewModel.Model.Section.NoSection) + relations

    fun getSlashWidgetColorItems(code: String?): List<SlashItem.Color.Text> =
        ThemeColor.values().map { themeColor ->
            val isSelected = if (themeColor.title == ThemeColor.DEFAULT.title && code == null) {
                true
            } else {
                themeColor.title == code
            }
            SlashItem.Color.Text(
                code = themeColor.title,
                isSelected = isSelected
            )
        }

    fun getSlashWidgetBackgroundItems(code: String?): List<SlashItem.Color.Background> =
        ThemeColor.values().map { themeColor ->
            val isSelected = if (themeColor.title == ThemeColor.DEFAULT.title && code == null) {
                true
            } else {
                themeColor.title == code
            }
            SlashItem.Color.Background(
                code = themeColor.title,
                isSelected = isSelected
            )
        }
    //endregion

    fun getUpdatedSlashWidgetState(
        text: CharSequence,
        objectTypes: List<SlashItem.ObjectType>,
        relations: List<RelationListViewModel.Model.Item>
    ): SlashWidgetState.UpdateItems {
        val filter = text.subSequence(1, text.length).toString()
        return SlashWidgetState.UpdateItems.empty().copy(
            styleItems = filterSlashItems(filter = filter, items = getSlashWidgetStyleItems()),
            mediaItems = filterSlashItems(filter = filter, items = getSlashWidgetMediaItems()),
            objectItems = filterObjectTypes(filter = filter, items = objectTypes),
            relationItems = filterRelations(filter = filter, items = relations),
            otherItems = filterSlashItems(filter = filter, items = getSlashWidgetOtherItems()),
            actionsItems = filterSlashItems(filter = filter, items = getSlashWidgetActionItems()),
            alignmentItems = filterAlignItems(filter = filter, items = getSlashWidgetAlignmentItems()),
            colorItems = filterColor(filter = filter, items = getSlashWidgetColorItems(code = null)),
            backgroundItems = filterBackground(
                filter = filter,
                items = getSlashWidgetBackgroundItems(code = null)
            )
        )
    }

    fun isSlashWidgetEmpty(widgetState: SlashWidgetState.UpdateItems): Boolean =
        widgetState == SlashWidgetState.UpdateItems.empty()

    //region {PRIVATE HELPING METHODS}
    private fun filterColor(filter: String, items: List<SlashItem.Color.Text>): List<SlashItem> {
        val filtered = items.filter {
            it.code.contains(filter, ignoreCase = true)
        }
        return updateWithSubheader(filtered)
    }

    private fun filterBackground(
        filter: String,
        items: List<SlashItem.Color.Background>
    ): List<SlashItem> {
        val filtered = items.filter {
            it.code.contains(filter, ignoreCase = true)
        }
        return updateWithSubheader(filtered)
    }

    private fun filterRelations(
        filter: String,
        items: List<RelationListViewModel.Model.Item>
    ): List<RelationListViewModel.Model> {
        val filtered = items.filter {
            it.view.name.contains(filter, ignoreCase = true)
        }
        return if (filtered.isEmpty()) {
            filtered
        } else {
            listOf(RelationListViewModel.Model.Section.NoSection) + filtered
        }
    }

    private fun filterObjectTypes(
        filter: String,
        items: List<SlashItem.ObjectType>
    ): List<SlashItem> {
        val filtered = items.filter {
            it.name.contains(filter, ignoreCase = true)
        }
        return updateWithSubheader(items = filtered)
    }

    private fun filterSlashItems(filter: String, items: List<SlashItem>): List<SlashItem> {
        val filtered = items
            .filter {
                it.javaClass.simpleName.contains(filter, ignoreCase = true)
            }
        return updateWithSubheader(items = filtered)
    }

    private fun filterAlignItems(filter: String, items: List<SlashItem>): List<SlashItem> {
        val filtered = items
            .filter {
                val name = "$SLASH_ALIGN ${it.javaClass.simpleName}"
                name.contains(filter, ignoreCase = true)
            }
        return updateWithSubheader(items = filtered)
    }

    private fun updateWithSubheader(items: List<SlashItem>): List<SlashItem> =
        if (items.isNotEmpty()) {
            when (items.first()) {
                is SlashItem.Actions -> listOf(SlashItem.Subheader.Actions) + items
                is SlashItem.Alignment -> listOf(SlashItem.Subheader.Alignment) + items
                is SlashItem.Color.Background -> listOf(SlashItem.Subheader.Background) + items
                is SlashItem.Color.Text -> listOf(SlashItem.Subheader.Color) + items
                is SlashItem.Media -> listOf(SlashItem.Subheader.Media) + items
                is SlashItem.ObjectType -> listOf(SlashItem.Subheader.ObjectType) + items
                is SlashItem.Other -> listOf(SlashItem.Subheader.Other) + items
                is SlashItem.Style -> listOf(SlashItem.Subheader.Style) + items
                else -> items
            }
        } else {
            items
        }
    //endregion
}