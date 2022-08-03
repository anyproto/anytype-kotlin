package com.anytypeio.anytype.presentation.editor.editor.slash

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.ext.parseMatchedInt
import com.anytypeio.anytype.domain.table.CreateTable.Companion.DEFAULT_COLUMN_COUNT
import com.anytypeio.anytype.domain.table.CreateTable.Companion.DEFAULT_MAX_COLUMN_COUNT
import com.anytypeio.anytype.domain.table.CreateTable.Companion.DEFAULT_MAX_ROW_COUNT
import com.anytypeio.anytype.domain.table.CreateTable.Companion.DEFAULT_ROW_COUNT
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem.Other.Table.Companion.DEFAULT_PATTERN

fun List<ObjectType>.toSlashItemView(): List<SlashItem.ObjectType> = map { oType ->
    SlashItem.ObjectType(
        url = oType.url,
        name = oType.name,
        emoji = oType.emoji,
        layout = oType.layout,
        description = oType.description
    )
}

fun SlashItem.Style.Type.convertToUiBlock() = when (this) {
    SlashItem.Style.Type.Bulleted -> UiBlock.BULLETED
    SlashItem.Style.Type.Callout -> UiBlock.CALLOUT
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
    SlashItem.Style.Markup.Strikethrough -> Block.Content.Text.Mark.Type.STRIKETHROUGH
    SlashItem.Style.Markup.Code -> Block.Content.Text.Mark.Type.KEYBOARD
    SlashItem.Style.Markup.Italic -> Block.Content.Text.Mark.Type.ITALIC
}

object SlashExtensions {

    const val SLASH_CHAR = '/'
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

    fun getSlashWidgetStyleItems(viewType: Int) = listOf(
        SlashItem.Style.Type.Text,
        SlashItem.Style.Type.Title,
        SlashItem.Style.Type.Heading,
        SlashItem.Style.Type.Subheading,
        SlashItem.Style.Type.Highlighted,
        SlashItem.Style.Type.Callout,
        SlashItem.Style.Type.Checkbox,
        SlashItem.Style.Type.Bulleted,
        SlashItem.Style.Type.Numbered,
        SlashItem.Style.Type.Toggle
    ) + getSlashWidgetMarkupItems(viewType)

    private fun getSlashWidgetMarkupItems(viewType: Int) =
        when (viewType) {
            Types.HOLDER_HEADER_ONE,
            Types.HOLDER_HEADER_TWO,
            Types.HOLDER_HEADER_THREE -> listOf(
                SlashItem.Style.Markup.Strikethrough,
                SlashItem.Style.Markup.Code
            )
            else -> listOf(
                SlashItem.Style.Markup.Bold,
                SlashItem.Style.Markup.Italic,
                SlashItem.Style.Markup.Strikethrough,
                SlashItem.Style.Markup.Code
            )
        }

    fun getSlashWidgetMediaItems() = listOf(
        SlashItem.Media.File,
        SlashItem.Media.Picture,
        SlashItem.Media.Video,
        SlashItem.Media.Bookmark,
        SlashItem.Media.Code
    )

    fun getSlashWidgetOtherItems() = listOf(
        SlashItem.Other.Line,
        SlashItem.Other.Dots,
        SlashItem.Other.TOC,
        SlashItem.Other.Table()
    )

    fun getSlashWidgetActionItems() = listOf(
        SlashItem.Actions.Delete,
        SlashItem.Actions.Duplicate,
        SlashItem.Actions.Copy,
        SlashItem.Actions.Paste,
        SlashItem.Actions.Move,
        SlashItem.Actions.MoveTo,
        SlashItem.Actions.LinkTo
        //, SlashItem.Actions.CleanStyle
    )

    fun getSlashWidgetAlignmentItems(viewType: Int) =
        when (viewType) {
            Types.HOLDER_CHECKBOX,
            Types.HOLDER_BULLET,
            Types.HOLDER_NUMBERED,
            Types.HOLDER_TOGGLE -> listOf()
            Types.HOLDER_HIGHLIGHT -> listOf(
                SlashItem.Alignment.Left,
                SlashItem.Alignment.Right
            )
            else -> listOf(
                SlashItem.Alignment.Left,
                SlashItem.Alignment.Center,
                SlashItem.Alignment.Right
            )
        }

    fun getSlashWidgetObjectTypeItems(objectTypes: List<ObjectType>): List<SlashItem> =
        listOf(SlashItem.Subheader.ObjectTypeWithBlack) + objectTypes.toSlashItemView()

    fun getSlashWidgetRelationItems(relations: List<SlashRelationView>): List<SlashRelationView> =
        listOf(
            SlashRelationView.Section.SubheaderWithBack,
            SlashRelationView.RelationNew
        ) + relations

    fun getSlashWidgetColorItems(color: ThemeColor?): List<SlashItem.Color.Text> =
        ThemeColor.values().map { themeColor ->
            val isSelected = if (color == null) false else themeColor == color
            SlashItem.Color.Text(
                themeColor = themeColor,
                isSelected = isSelected
            )
        }

    fun getSlashWidgetBackgroundItems(color: ThemeColor?): List<SlashItem.Color.Background> =
        ThemeColor.values().map { themeColor ->
            val isSelected = if (color == null) false else themeColor == color
            SlashItem.Color.Background(
                themeColor = themeColor,
                isSelected = isSelected
            )
        }
    //endregion

    fun getUpdatedSlashWidgetState(
        viewType: Int,
        text: CharSequence,
        objectTypes: List<SlashItem.ObjectType>,
        relations: List<SlashRelationView.Item>
    ): SlashWidgetState.UpdateItems {
        val filter = text.subSequence(1, text.length).toString()
        val filteredStyle = filterSlashItems(
            filter = filter,
            subheading = SlashItem.Subheader.Style.getSearchName(),
            items = getSlashWidgetStyleItems(viewType = viewType)
        )
        val filteredMedia = filterSlashItems(
            filter = filter,
            subheading = SlashItem.Subheader.Media.getSearchName(),
            items = getSlashWidgetMediaItems()
        )
        val filteredActions = filterSlashItems(
            filter = filter,
            subheading = SlashItem.Subheader.Actions.getSearchName(),
            items = getSlashWidgetActionItems()
        )
        val filteredAlign = filterSlashItems(
            filter = filter,
            subheading = SlashItem.Subheader.Alignment.getSearchName(),
            items = getSlashWidgetAlignmentItems(viewType = viewType)
        )
        val filteredOther = filterSlashItems(
            filter = filter,
            subheading = SlashItem.Subheader.Other.getSearchName(),
            items = getSlashWidgetOtherItems()
        )
        val filteredColor = filterColor(
            filter = filter,
            items = getSlashWidgetColorItems(color = null)
        )
        val filteredBackground = filterBackground(
            filter = filter,
            items = getSlashWidgetBackgroundItems(color = null)
        )
        val filteredObjects = filterObjectTypes(
            filter = filter,
            items = objectTypes
        )
        val filteredRelations = filterRelations(
            filter = filter,
            items = relations
        )
        return SlashWidgetState.UpdateItems.empty().copy(
            styleItems = filteredStyle,
            mediaItems = filteredMedia,
            objectItems = filteredObjects,
            relationItems = filteredRelations,
            otherItems = filteredOther,
            actionsItems = filteredActions,
            alignmentItems = filteredAlign,
            colorItems = filteredColor,
            backgroundItems = filteredBackground
        )
    }

    fun isSlashWidgetEmpty(widgetState: SlashWidgetState.UpdateItems): Boolean =
        widgetState == SlashWidgetState.UpdateItems.empty()

    //region {PRIVATE HELPING METHODS}
    private fun filterColor(filter: String, items: List<SlashItem.Color.Text>): List<SlashItem> {
        val filtered = items.filter { item ->
            searchBySubheadingOrName(
                filter = filter,
                subheading = SlashItem.Main.Color.getSearchName(),
                name = item.themeColor.code
            )
        }
        return updateWithSubheader(filtered)
    }

    private fun filterBackground(
        filter: String,
        items: List<SlashItem.Color.Background>
    ): List<SlashItem> {
        val filtered = items.filter { item ->
            searchBySubheadingOrName(
                filter = filter,
                subheading = SlashItem.Main.Background.getSearchName(),
                name = item.themeColor.code
            )
        }
        return updateWithSubheader(filtered)
    }

    private fun filterRelations(
        filter: String,
        items: List<SlashRelationView.Item>
    ): List<SlashRelationView> {
        val filtered = items.filter { item ->
            searchBySubheadingOrName(
                filter = filter,
                subheading = SlashItem.Main.Relations.getSearchName(),
                name = item.view.name
            )
        }
        return if (filtered.isEmpty()) {
            filtered
        } else {
            listOf(SlashRelationView.Section.Subheader) + filtered
        }
    }

    private fun filterObjectTypes(
        filter: String,
        items: List<SlashItem.ObjectType>
    ): List<SlashItem> {
        val filtered = items.filter { item ->
            searchBySubheadingOrName(
                filter = filter,
                subheading = SlashItem.Main.Objects.getSearchName(),
                name = item.name
            )
        }
        return updateWithSubheader(items = filtered)
    }

    private fun filterSlashItems(
        filter: String,
        items: List<SlashItem>,
        subheading: String
    ): List<SlashItem> {
        val filtered = items.filter { item ->
            if (item is SlashItem.Other.Table) {
                val matchResults = DEFAULT_PATTERN.toRegex().findAll(filter)
                if (matchResults.none()) {
                    searchBySubheadingOrName(
                        filter = filter,
                        subheading = subheading,
                        name = item.getSearchName(),
                        abbreviation = item.getAbbreviation()
                    )
                } else {
                    val matchResult = matchResults.firstOrNull()
                    val rowCount = matchResult.parseMatchedInt(1) ?: DEFAULT_ROW_COUNT
                    val columnCount = matchResult.parseMatchedInt(2) ?: DEFAULT_COLUMN_COUNT
                    item.rowCount = 1.coerceAtLeast(DEFAULT_MAX_ROW_COUNT.coerceAtMost(rowCount))
                    item.columnCount =
                        1.coerceAtLeast(DEFAULT_MAX_COLUMN_COUNT.coerceAtMost(columnCount))
                    true
                }
            } else {
                searchBySubheadingOrName(
                    filter = filter,
                    subheading = subheading,
                    name = item.getSearchName(),
                    abbreviation = item.getAbbreviation()
                )
            }
        }
        return updateWithSubheader(items = filtered)
    }

    private fun searchBySubheadingOrName(
        filter: String,
        subheading: String,
        name: String,
        abbreviation: List<String>? = null
    ): Boolean = subheading.startsWith(filter, true) || name.contains(filter, true) ||
            abbreviation?.any { it.contains(filter, true) } ?: false

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