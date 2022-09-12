package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.middleware.mappers.toCoreLinkRelationModel
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import com.anytypeio.anytype.middleware.mappers.toCoreModels
import com.anytypeio.anytype.middleware.mappers.toCoreModelsAlign
import com.anytypeio.anytype.middleware.mappers.toCoreModelsBookmarkState

fun anytype.Event.Message.toCoreModels(
    context: String
): Event.Command? = when {
    blockAdd != null -> {
        val event = blockAdd
        checkNotNull(event)
        Event.Command.AddBlock(
            context = context,
            blocks = event.blocks.toCoreModels()
        )
    }
    blockSetText != null -> {
        val event = blockSetText
        checkNotNull(event)
        Event.Command.GranularChange(
            context = context,
            id = event.id,
            text = event.text?.value_,
            style = event.style?.value_?.toCoreModels(),
            color = event.color?.value_,
            marks = event.marks?.value_?.marks?.map { it.toCoreModels() },
            checked = event.checked?.value_,
            emojiIcon = event.iconEmoji?.value_,
            imageIcon = event.iconImage?.value_,
        )
    }
    blockSetBackgroundColor != null -> {
        val event = blockSetBackgroundColor
        checkNotNull(event)
        Event.Command.GranularChange(
            context = context,
            id = event.id,
            backgroundColor = event.backgroundColor
        )
    }
    blockDelete != null -> {
        val event = blockDelete
        checkNotNull(event)
        Event.Command.DeleteBlock(
            context = context,
            targets = event.blockIds
        )
    }
    blockSetChildrenIds != null -> {
        val event = blockSetChildrenIds
        checkNotNull(event)
        Event.Command.UpdateStructure(
            context = context,
            id = event.id,
            children = event.childrenIds
        )
    }
    objectDetailsSet != null -> {
        val event = objectDetailsSet
        checkNotNull(event)
        Event.Command.Details.Set(
            context = context,
            target = event.id,
            details = event.details.toCoreModel()
        )
    }
    objectDetailsAmend != null -> {
        val event = objectDetailsAmend
        checkNotNull(event)
        Event.Command.Details.Amend(
            context = context,
            target = event.id,
            details = event.details.associate { it.key to it.value_ }
        )
    }
    objectDetailsUnset != null -> {
        val event = objectDetailsUnset
        checkNotNull(event)
        Event.Command.Details.Unset(
            context = context,
            target = event.id,
            keys = event.keys
        )
    }
    blockSetLink != null -> {
        val event = blockSetLink
        checkNotNull(event)
        Event.Command.LinkGranularChange(
            context = context,
            id = event.id,
            target = event.targetBlockId?.value_.orEmpty(),
            iconSize = event.iconSize?.value_?.toCoreModel(),
            cardStyle = event.cardStyle?.value_?.toCoreModel(),
            description = event.description?.value_?.toCoreModel(),
            relations = event.relations?.value_?.map { it.toCoreLinkRelationModel() }?.toSet()
        )
    }
    blockSetAlign != null -> {
        val event = blockSetAlign
        checkNotNull(event)
        Event.Command.GranularChange(
            context = context,
            id = event.id,
            alignment = event.align.toCoreModelsAlign()
        )
    }
    blockSetFields != null -> {
        val event = blockSetFields
        checkNotNull(event)
        Event.Command.UpdateFields(
            context = context,
            target = event.id,
            fields = event.fields.toCoreModel()
        )
    }
    blockSetFile != null -> {
        val event = blockSetFile
        checkNotNull(event)
        with(event) {
            Event.Command.UpdateFileBlock(
                context = context,
                id = id,
                state = state?.value_?.toCoreModels(),
                type = type?.value_?.toCoreModels(),
                name = name?.value_,
                hash = hash?.value_,
                mime = mime?.value_,
                size = size?.value_
            )
        }
    }
    blockSetBookmark != null -> {
        val event = blockSetBookmark
        checkNotNull(event)
        Event.Command.BookmarkGranularChange(
            context = context,
            target = event.id,
            url = event.url?.value_,
            title = event.title?.value_,
            description = event.description?.value_,
            image = event.imageHash?.value_,
            favicon = event.faviconHash?.value_,
            targetObjectId = event.targetObjectId?.value_,
            state = event.state?.value_?.toCoreModelsBookmarkState()
        )
    }
    blockDataviewRecordsSet != null -> {
        val event = blockDataviewRecordsSet
        checkNotNull(event)
        Event.Command.DataView.SetRecords(
            id = event.id,
            view = event.viewId,
            records = event.records.map { it?.toMap().orEmpty() },
            total = event.total,
            context = context
        )
    }
    blockDataviewRelationSet != null -> {
        val event = blockDataviewRelationSet
        checkNotNull(event)
        val relation = event.relation
        checkNotNull(relation)
        Event.Command.DataView.SetRelation(
            id = event.id,
            context = context,
            key = event.relationKey,
            relation = relation.toCoreModels()
        )
    }
    blockDataviewRecordsUpdate != null -> {
        val event = blockDataviewRecordsUpdate
        checkNotNull(event)
        Event.Command.DataView.UpdateRecord(
            context = context,
            viewer = event.viewId,
            target = event.id,
            records = event.records.map { it?.toMap().orEmpty() },
        )
    }
    blockDataviewRecordsDelete != null -> {
        val event = blockDataviewRecordsDelete
        checkNotNull(event)
        Event.Command.DataView.DeleteRecord(
            context = context,
            dataViewId = event.id,
            viewerId = event.viewId,
            recordIds = event.removed
        )
    }
    blockSetDiv != null -> {
        val event = blockSetDiv
        checkNotNull(event)
        val style = event.style
        checkNotNull(style)
        Event.Command.UpdateDividerBlock(
            context = context,
            id = event.id,
            style = style.value_.toCoreModels()
        )
    }
    blockDataviewViewSet != null -> {
        val event = blockDataviewViewSet
        checkNotNull(event)
        val view = event.view
        checkNotNull(view)
        Event.Command.DataView.SetView(
            context = context,
            target = event.id,
            viewerId = event.viewId,
            viewer = view.toCoreModels(),
            offset = event.offset,
            limit = event.limit
        )
    }
    blockDataviewViewDelete != null -> {
        val event = blockDataviewViewDelete
        checkNotNull(event)
        Event.Command.DataView.DeleteView(
            context = context,
            target = event.id,
            viewer = event.viewId
        )
    }
    blockSetRelation != null -> {
        val event = blockSetRelation
        checkNotNull(event)
        Event.Command.BlockEvent.SetRelation(
            context = context,
            id = event.id,
            key = event.key?.value_
        )
    }
    objectRelationsSet != null -> {
        val event = objectRelationsSet
        checkNotNull(event)
        Event.Command.ObjectRelations.Set(
            context = context,
            id = event.id,
            relations = event.relations.map { it.toCoreModels() }
        )
    }
    objectRelationsAmend != null -> {
        val event = objectRelationsAmend
        checkNotNull(event)
        Event.Command.ObjectRelations.Amend(
            context = context,
            id = event.id,
            relations = event.relations.map { it.toCoreModels() }
        )
    }
    objectRelationsRemove != null -> {
        val event = objectRelationsRemove
        checkNotNull(event)
        Event.Command.ObjectRelations.Remove(
            context = context,
            id = event.id,
            keys = event.keys
        )
    }
    else -> null
}