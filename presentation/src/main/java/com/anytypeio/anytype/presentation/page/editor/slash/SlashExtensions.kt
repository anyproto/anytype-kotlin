package com.anytypeio.anytype.presentation.page.editor.slash

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.page.ControlPanelMachine
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import timber.log.Timber

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

object SlashExtensions {

    fun getSlashMainItems() = listOf(
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

    fun getStyleItems() = listOf(
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

    fun getMediaItems() = listOf(
        SlashItem.Media.File,
        SlashItem.Media.Picture,
        SlashItem.Media.Video,
        SlashItem.Media.Bookmark,
        SlashItem.Media.Code
    )

    fun getOtherItems() = listOf(
        SlashItem.Other.Line,
        SlashItem.Other.Dots
    )

    fun getActionItems() = listOf(
        SlashItem.Actions.Delete,
        SlashItem.Actions.Duplicate,
        SlashItem.Actions.Copy,
        SlashItem.Actions.Paste,
        SlashItem.Actions.Move,
        SlashItem.Actions.MoveTo,
        SlashItem.Actions.CleanStyle
    )

    fun getAlignmentItems() = listOf(
        SlashItem.Alignment.Left,
        SlashItem.Alignment.Center,
        SlashItem.Alignment.Right
    )

    fun getObjectTypeItems(objectTypes: List<ObjectType>): List<SlashItem> =
        listOf(SlashItem.Subheader.ObjectTypeWithBlack) + objectTypes.toView()

    fun getRelationItems(relations: List<RelationListViewModel.Model>): List<RelationListViewModel.Model> =
        listOf(RelationListViewModel.Model.Section.NoSection) + relations

    fun getControlPanelMachineEvent(event: SlashEvent): ControlPanelMachine.Event =
        when (event) {
            is SlashEvent.Start -> {
                ControlPanelMachine.Event.Slash.OnStart(
                    cursorCoordinate = event.cursorCoordinate,
                    slashFrom = event.slashStart
                )
            }
            is SlashEvent.Filter -> {
                ControlPanelMachine.Event.Slash.Update(
                    command = onGetFilterUpdate(filter = event.filter)
                )
            }
            SlashEvent.Stop -> {
                ControlPanelMachine.Event.Slash.OnStop
            }
        }

    fun getColorItems(code: String?): List<SlashItem.Color> =
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

    fun getBackgroundItems(code: String?): List<SlashItem.Color> =
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

    //TODO in progress
    fun onGetFilterUpdate(filter: CharSequence): SlashCommand.UpdateItems {
        if (filter.isEmpty() || filter.first() != '/') {
            return SlashCommand.UpdateItems.empty()
        }
        if (filter.length == 1 && filter.first() == '/') {
            return SlashCommand.UpdateItems.empty().copy(
                mainItems = getSlashMainItems()
            )
        }

        throw RuntimeException("Not implemented")
    }

    //TODO in progress
    fun filterSlashItems(filter: String): SlashCommand.UpdateItems {
        var count = 0
        val command = SlashCommand.UpdateItems(
            mainItems = emptyList(),
            styleItems = filterSlashItems(filter = filter, items = getStyleItems())
                .also {
                    if (it.isNotEmpty()) count++
                },
            mediaItems = filterSlashItems(filter = filter, items = getMediaItems())
                .also {
                    if (it.isNotEmpty()) count++
                },
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = filterSlashItems(filter = filter, items = getOtherItems()),
            actionsItems = filterSlashItems(filter = filter, items = getActionItems()),
            alignmentItems = filterSlashItems(filter = filter, items = getAlignmentItems()),
            colorItems = filterSlashItems(filter = filter, items = getColorItems(code = null)),
            backgroundItems = filterSlashItems(
                filter = filter,
                items = getBackgroundItems(code = null)
            )
        )
        Timber.d("Count:$count")
        return command
    }

    private fun filterSlashItems(filter: String, items: List<SlashItem>): List<SlashItem> {
        val style = items
            .filter {
                it.javaClass.simpleName.contains(filter, ignoreCase = true)
            }
        return updateWithSubheader(items = style)
    }

    private fun updateWithSubheader(items: List<SlashItem>): List<SlashItem> {
        return if (items.isNotEmpty()) {
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
    }
}