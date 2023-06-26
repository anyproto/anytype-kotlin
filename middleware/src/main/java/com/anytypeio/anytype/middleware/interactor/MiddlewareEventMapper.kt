package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.middleware.mappers.MWidgetLayout
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
            relations = event.relations?.value_?.toSet()
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
    blockDataviewRelationSet != null -> {
        val event = blockDataviewRelationSet
        checkNotNull(event)
        Event.Command.DataView.SetRelation(
            dv = event.id,
            context = context,
            links = event.relationLinks.map { it.toCoreModels() }
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
    blockSetWidget != null -> {
        val event = blockSetWidget
        checkNotNull(event)
        Event.Command.Widgets.SetWidget(
            context = context,
            widget = event.id,
            activeView = event.viewId?.value_,
            limit = event.limit?.value_,
            layout = when(event.layout?.value_) {
                MWidgetLayout.Link -> Block.Content.Widget.Layout.LINK
                MWidgetLayout.Tree -> Block.Content.Widget.Layout.TREE
                MWidgetLayout.List -> Block.Content.Widget.Layout.LIST
                MWidgetLayout.CompactList -> Block.Content.Widget.Layout.COMPACT_LIST
                else -> null
            }
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
            viewer = view.toCoreModels()
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
    blockDataviewViewOrder != null -> {
        val event = blockDataviewViewOrder
        checkNotNull(event)
        Event.Command.DataView.OrderViews(
            context = context,
            dv = event.id,
            order = event.viewIds
        )
    }
    blockDataviewTargetObjectIdSet != null -> {
        val event = blockDataviewTargetObjectIdSet
        checkNotNull(event)
        Event.Command.DataView.SetTargetObjectId(
            context = context,
            dv = event.id,
            targetObjectId = event.targetObjectId
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
    objectRelationsAmend != null -> {
        val event = objectRelationsAmend
        checkNotNull(event)
        Event.Command.ObjectRelationLinks.Amend(
            context = context,
            id = event.id,
            relationLinks = event.relationLinks.map { it.toCoreModels() }
        )
    }
    objectRelationsRemove != null -> {
        val event = objectRelationsRemove
        checkNotNull(event)
        Event.Command.ObjectRelationLinks.Remove(
            context = context,
            id = event.id,
            keys = event.relationKeys
        )
    }
    blockDataviewViewUpdate != null -> {
        val event = blockDataviewViewUpdate
        checkNotNull(event)
        Event.Command.DataView.UpdateView(
            context = context,
            block = event.id,
            viewerId = event.viewId,
            filterUpdates = event.filter.mapNotNull { filter ->
                filter.toCoreModels()
            },
            sortUpdates = event.sort.mapNotNull { sort ->
                sort.toCoreModels()
            },
            relationUpdates = event.relation.mapNotNull { relation ->
                relation.toCoreModels()
            },
            fields = event.fields?.toCoreModels()
        )
    }
    blockDataviewRelationDelete != null -> {
        val event = blockDataviewRelationDelete
        checkNotNull(event)
        Event.Command.DataView.DeleteRelation(
            context = context,
            dv = event.id,
            keys = event.relationKeys
        )
    }
    blockDataviewIsCollectionSet != null -> {
        val event = blockDataviewIsCollectionSet
        checkNotNull(event)
        Event.Command.DataView.SetIsCollection(
            context = context,
            dv = event.id,
            isCollection = event.value_
        )
    }
    else -> null
}