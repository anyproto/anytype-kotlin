package com.anytypeio.anytype.middleware.interactor

import anytype.Events
import anytype.Events.Event
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.data.auth.model.EventEntity
import com.anytypeio.anytype.middleware.converters.blocks
import com.anytypeio.anytype.middleware.converters.entity
import com.anytypeio.anytype.middleware.converters.fields
import com.anytypeio.anytype.middleware.converters.marks

fun Event.Message.toEntity(
    context: String
): EventEntity.Command? = when (valueCase) {
    Event.Message.ValueCase.BLOCKADD -> {
        EventEntity.Command.AddBlock(
            context = context,
            blocks = blockAdd.blocksList.blocks()
        )
    }
    Event.Message.ValueCase.BLOCKSHOW -> {
        val type = when (blockShow.type) {
            Events.SmartBlockType.Page -> EventEntity.Command.ShowBlock.Type.PAGE
            Events.SmartBlockType.ProfilePage -> EventEntity.Command.ShowBlock.Type.PROFILE_PAGE
            Events.SmartBlockType.Home -> EventEntity.Command.ShowBlock.Type.HOME
            Events.SmartBlockType.Archive -> EventEntity.Command.ShowBlock.Type.ACHIVE
            Events.SmartBlockType.Set -> EventEntity.Command.ShowBlock.Type.SET
            Events.SmartBlockType.Breadcrumbs -> EventEntity.Command.ShowBlock.Type.BREADCRUMBS
            else -> throw IllegalStateException("Unexpected smart block type: ${blockShow.type}")
        }
        EventEntity.Command.ShowBlock(
            context = context,
            root = blockShow.rootId,
            blocks = blockShow.blocksList.blocks(
                types = mapOf(blockShow.rootId to blockShow.type)
            ),
            details = BlockEntity.Details(
                blockShow.detailsList.associate { details ->
                    details.id to details.details.fields()
                }
            ),
            type = type
        )
    }
    Event.Message.ValueCase.BLOCKSETTEXT -> {
        EventEntity.Command.GranularChange(
            context = context,
            id = blockSetText.id,
            text = if (blockSetText.hasText())
                blockSetText.text.value
            else null,
            style = if (blockSetText.hasStyle())
                blockSetText.style.value.entity()
            else
                null,
            color = if (blockSetText.hasColor())
                blockSetText.color.value
            else
                null,
            marks = if (blockSetText.hasMarks())
                blockSetText.marks.value.marksList.marks()
            else
                null
        )
    }
    Event.Message.ValueCase.BLOCKSETBACKGROUNDCOLOR -> {
        EventEntity.Command.GranularChange(
            context = context,
            id = blockSetBackgroundColor.id,
            backgroundColor = blockSetBackgroundColor.backgroundColor
        )
    }
    Event.Message.ValueCase.BLOCKDELETE -> {
        EventEntity.Command.DeleteBlock(
            context = context,
            targets = blockDelete.blockIdsList.toList()
        )
    }
    Event.Message.ValueCase.BLOCKSETCHILDRENIDS -> {
        EventEntity.Command.UpdateStructure(
            context = context,
            id = blockSetChildrenIds.id,
            children = blockSetChildrenIds.childrenIdsList.toList()
        )
    }
    Event.Message.ValueCase.BLOCKSETDETAILS -> {
        EventEntity.Command.UpdateDetails(
            context = context,
            target = blockSetDetails.id,
            details = blockSetDetails.details.fields()
        )
    }
    Event.Message.ValueCase.BLOCKSETLINK -> {
        EventEntity.Command.LinkGranularChange(
            context = context,
            id = blockSetLink.id,
            target = blockSetLink.targetBlockId.value,
            fields = if (blockSetLink.hasFields())
                blockSetLink.fields.value.fields()
            else
                null
        )
    }
    Event.Message.ValueCase.BLOCKSETALIGN -> {
        EventEntity.Command.GranularChange(
            context = context,
            id = blockSetAlign.id,
            alignment = blockSetAlign.align.entity()
        )
    }
    Event.Message.ValueCase.BLOCKSETFIELDS -> {
        EventEntity.Command.UpdateFields(
            context = context,
            target = blockSetFields.id,
            fields = blockSetFields.fields.fields()
        )
    }
    Event.Message.ValueCase.BLOCKSETFILE -> {
        with(blockSetFile) {
            EventEntity.Command.UpdateBlockFile(
                context = context,
                id = id,
                state = if (hasState()) state.value.entity() else null,
                type = if (hasType()) type.value.entity() else null,
                name = if (hasName()) name.value else null,
                hash = if (hasHash()) hash.value else null,
                mime = if (hasMime()) mime.value else null,
                size = if (hasSize()) size.value else null
            )
        }
    }
    Event.Message.ValueCase.BLOCKSETBOOKMARK -> {
        EventEntity.Command.BookmarkGranularChange(
            context = context,
            target = blockSetBookmark.id,
            url = if (blockSetBookmark.hasUrl())
                blockSetBookmark.url.value
            else null,
            title = if (blockSetBookmark.hasTitle())
                blockSetBookmark.title.value
            else
                null,
            description = if (blockSetBookmark.hasDescription())
                blockSetBookmark.description.value
            else
                null,
            imageHash = if (blockSetBookmark.hasImageHash())
                blockSetBookmark.imageHash.value
            else
                null,
            faviconHash = if (blockSetBookmark.hasFaviconHash())
                blockSetBookmark.faviconHash.value
            else
                null
        )
    }
    else -> null
}