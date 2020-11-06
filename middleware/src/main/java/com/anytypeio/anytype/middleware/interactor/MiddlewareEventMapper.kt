package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import anytype.SmartBlockType
import anytype.model.Block
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.data.auth.model.EventEntity
import com.anytypeio.anytype.middleware.converters.blocks
import com.anytypeio.anytype.middleware.converters.entity
import com.anytypeio.anytype.middleware.converters.fields
import com.anytypeio.anytype.middleware.converters.marks

fun Event.Message.toEntity(
    context: String
): EventEntity.Command? = when {
    blockAdd != null -> {
        val event = blockAdd
        checkNotNull(event)
        EventEntity.Command.AddBlock(
            context = context,
            blocks = event.blocks.blocks()
        )
    }
    blockShow != null -> {
        val event = blockShow
        checkNotNull(event)
        val type = when (event.type) {
            SmartBlockType.Page -> EventEntity.Command.ShowBlock.Type.PAGE
            SmartBlockType.ProfilePage -> EventEntity.Command.ShowBlock.Type.PROFILE_PAGE
            SmartBlockType.Home -> EventEntity.Command.ShowBlock.Type.HOME
            SmartBlockType.Archive -> EventEntity.Command.ShowBlock.Type.ACHIVE
            SmartBlockType.Set -> EventEntity.Command.ShowBlock.Type.SET
            SmartBlockType.Breadcrumbs -> EventEntity.Command.ShowBlock.Type.BREADCRUMBS
            else -> throw IllegalStateException("Unexpected smart block type: ${event.type}")
        }
        EventEntity.Command.ShowBlock(
            context = context,
            root = event.rootId,
            blocks = event.blocks.blocks(
                types = mapOf(event.rootId to event.type)
            ),
            details = BlockEntity.Details(
                event.details.associate { details ->
                    details.id to details.details.fields()
                }
            ),
            type = type
        )
    }
    blockSetText != null -> {
        val event = blockSetText
        checkNotNull(event)
        EventEntity.Command.GranularChange(
            context = context,
            id = event.id,
            text = event.text?.value,
            style = event.style?.value?.entity(),
            color = event.color?.value,
            marks = event.marks?.value?.marks?.marks(),
            checked = event.checked?.value
        )
    }
    blockSetBackgroundColor != null -> {
        val event = blockSetBackgroundColor
        checkNotNull(event)
        EventEntity.Command.GranularChange(
            context = context,
            id = event.id,
            backgroundColor = event.backgroundColor
        )
    }
    blockDelete != null -> {
        val event = blockDelete
        checkNotNull(event)
        EventEntity.Command.DeleteBlock(
            context = context,
            targets = event.blockIds
        )
    }
    blockSetChildrenIds != null -> {
        val event = blockSetChildrenIds
        checkNotNull(event)
        EventEntity.Command.UpdateStructure(
            context = context,
            id = event.id,
            children = event.childrenIds
        )
    }
    blockSetDetails != null -> {
        val event = blockSetDetails
        checkNotNull(event)
        EventEntity.Command.UpdateDetails(
            context = context,
            target = event.id,
            details = event.details.fields()
        )
    }
    blockSetLink != null -> {
        val event = blockSetLink
        checkNotNull(event)
        EventEntity.Command.LinkGranularChange(
            context = context,
            id = event.id,
            target = event.targetBlockId?.value.orEmpty(),
            fields = event.fields?.value.fields()
        )
    }
    blockSetAlign != null -> {
        val event = blockSetAlign
        checkNotNull(event)
        EventEntity.Command.GranularChange(
            context = context,
            id = event.id,
            alignment = event.align.entity()
        )
    }
    blockSetFields != null -> {
        val event = blockSetFields
        checkNotNull(event)
        EventEntity.Command.UpdateFields(
            context = context,
            target = event.id,
            fields = event.fields.fields()
        )
    }
    blockSetFile != null -> {
        val event = blockSetFile
        checkNotNull(event)
        with(event) {
            EventEntity.Command.UpdateBlockFile(
                context = context,
                id = id,
                state = state?.value?.entity(),
                type = type?.value?.entity(),
                name = name?.value,
                hash = hash?.value,
                mime = mime?.value,
                size = size?.value
            )
        }
    }

    blockSetBookmark != null -> {
        val event = blockSetBookmark
        checkNotNull(event)
        EventEntity.Command.BookmarkGranularChange(
            context = context,
            target = event.id,
            url = event.url?.value,
            title = event.title?.value,
            description = event.description?.value,
            imageHash = event.imageHash?.value,
            faviconHash = event.faviconHash?.value
        )
    }
    blockSetDiv != null -> {
        val event = blockSetDiv
        checkNotNull(event)
        val style = when (event.style?.value) {
            Block.Content.Div.Style.Line -> BlockEntity.Content.Divider.Style.LINE
            Block.Content.Div.Style.Dots -> BlockEntity.Content.Divider.Style.DOTS
            else -> throw IllegalStateException("Unexpected divider block style: ${event.style?.value}")
        }
        EventEntity.Command.UpdateDivider(
            context = context,
            id = event.id,
            style = style
        )
    }
    else -> null
}