package com.anytypeio.anytype.middleware.block

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CBTextStyle
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.FileLimits
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.data.auth.repo.block.BlockRemote
import com.anytypeio.anytype.middleware.interactor.Middleware
import com.anytypeio.anytype.middleware.mappers.toMiddlewareModel

class BlockMiddleware(
    private val middleware: Middleware
) : BlockRemote {

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ): Payload = middleware.dashboardOpen(contextId, id)

    override suspend fun closeDashboard(id: String) {
        middleware.objectClose(id)
    }

    override suspend fun openObject(id: Id): ObjectView = middleware.objectOpen(id = id)
    override suspend fun getObject(id: Id): ObjectView = middleware.objectShow(id = id)

    override suspend fun openPage(id: String): Payload = middleware.objectOpenOld(id)
    override suspend fun openProfile(id: String): Payload = middleware.objectOpenOld(id)
    override suspend fun openObjectSet(id: String): Payload = middleware.objectOpenOld(id)
    override suspend fun openObjectPreview(id: Id): Payload = middleware.objectShowOld(id)

    override suspend fun closePage(id: String) {
        middleware.objectClose(id)
    }

    override suspend fun updateDocumentTitle(command: Command.UpdateTitle) {
        middleware.objectSetTitle(command)
    }

    override suspend fun updateText(command: Command.UpdateText) {
        middleware.blockTextSetText(
            command.contextId,
            command.blockId,
            command.text,
            command.marks.map { it.toMiddlewareModel() }
        )
    }

    override suspend fun setLinkAppearance(command: Command.SetLinkAppearance): Payload {
        return middleware.blockLinkSetAppearance(command)
    }

    override suspend fun uploadBlock(command: Command.UploadBlock): Payload =
        middleware.blockUpload(command)

    override suspend fun updateTextStyle(
        command: Command.UpdateStyle
    ): Payload = middleware.blockTextListSetStyle(command)

    override suspend fun setTextIcon(
        command: Command.SetTextIcon
    ): Payload = middleware.blockTextSetIcon(command)

    override suspend fun updateTextColor(
        command: Command.UpdateTextColor
    ): Payload = middleware.blockTextListSetColor(command)

    override suspend fun updateBackgroundColor(
        command: Command.UpdateBackgroundColor
    ): Payload = middleware.blockListSetBackgroundColor(command)

    override suspend fun updateAlignment(
        command: Command.UpdateAlignment
    ): Payload = middleware.blockListSetAlign(command)

    override suspend fun updateCheckbox(
        command: Command.UpdateCheckbox
    ): Payload = middleware.blockTextSetChecked(
        command.context,
        command.target,
        command.isChecked
    )

    override suspend fun create(
        command: Command.Create
    ): Pair<String, Payload> = middleware.blockCreate(
        command.context,
        command.target,
        command.position,
        command.prototype
    )

    override suspend fun duplicate(
        command: Command.Duplicate
    ): Pair<List<Id>, Payload> = middleware.blockListDuplicate(command)

    override suspend fun duplicateObject(id: Id) = middleware.objectDuplicate(id)

    override suspend fun move(command: Command.Move): Payload {
        return middleware.blockListMoveToExistingObject(command)
    }

    override suspend fun unlink(
        command: Command.Unlink
    ): Payload = middleware.blockListDelete(command)

    override suspend fun merge(
        command: Command.Merge
    ): Payload = middleware.blockMerge(command)

    override suspend fun split(
        command: Command.Split
    ): Pair<String, Payload> = middleware.blockSplit(command)

    override suspend fun setDocumentEmojiIcon(
        command: Command.SetDocumentEmojiIcon
    ): Payload = middleware.objectSetEmojiIcon(command)

    override suspend fun setDocumentImageIcon(
        command: Command.SetDocumentImageIcon
    ): Payload = middleware.objectSetImageIcon(command)

    override suspend fun setDocumentCoverColor(
        ctx: String,
        color: String
    ): Payload = middleware.objectSetCoverColor(ctx = ctx, color = color)

    override suspend fun setDocumentCoverGradient(
        ctx: String,
        gradient: String
    ): Payload = middleware.objectSetCoverGradient(ctx = ctx, gradient = gradient)

    override suspend fun setDocumentCoverImage(
        ctx: String,
        hash: String
    ): Payload = middleware.objectSetCoverImage(ctx = ctx, hash = hash)

    override suspend fun removeDocumentCover(
        ctx: String
    ): Payload = middleware.objectRemoveCover(ctx)

    override suspend fun removeDocumentIcon(
        ctx: Id
    ): Payload = middleware.objectRemoveIcon(ctx)

    override suspend fun setupBookmark(
        command: Command.SetupBookmark
    ): Payload = middleware.blockBookmarkFetch(command)

    override suspend fun createAndFetchBookmarkBlock(
        command: Command.CreateBookmark
    ): Payload = middleware.blockBookmarkCreateAndFetch(command)

    override suspend fun createBookmarkObject(
        url: Url
    ): Id = middleware.objectCreateBookmark(url = url)

    override suspend fun fetchBookmarkObject(
        ctx: Id,
        url: Url
    ) = middleware.objectBookmarkFetch(
        ctx = ctx,
        url = url
    )

    override suspend fun undo(
        command: Command.Undo
    ): Payload = middleware.objectUndo(command)

    override suspend fun importUseCaseSkip(
        space: Id
    ) = middleware.objectImportUseCaseSkip(
        space = space
    )

    override suspend fun redo(
        command: Command.Redo
    ): Payload = middleware.objectRedo(command)

    override suspend fun turnIntoDocument(
        command: Command.TurnIntoDocument
    ): List<String> = middleware.blockListMoveToNewObject(command)

    override suspend fun replace(
        command: Command.Replace
    ): Pair<String, Payload> = middleware.blockReplace(command)

    override suspend fun paste(
        command: Command.Paste
    ): Response.Clipboard.Paste = middleware.blockPaste(command)

    override suspend fun copy(
        command: Command.Copy
    ): Response.Clipboard.Copy = middleware.blockCopy(command)

    override suspend fun uploadFile(
        command: Command.UploadFile
    ): String = middleware.fileUpload(command).hash

    override suspend fun downloadFile(
        command: Command.DownloadFile
    ): String = middleware.fileDownload(command).localPath

    override suspend fun setRelationKey(command: Command.SetRelationKey): Payload {
        return middleware.blockRelationSetKey(command)
    }

    override suspend fun updateDivider(
        command: Command.UpdateDivider
    ): Payload = middleware.blockListSetDivStyle(command)

    override suspend fun setFields(
        command: Command.SetFields
    ): Payload = middleware.blockListSetFields(command)

    override suspend fun createSet(
        objectType: String?
    ): Response.Set.Create = middleware.objectCreateSet(
        objectType = objectType
    )

    override suspend fun setActiveDataViewViewer(
        context: String,
        block: String,
        view: String,
        offset: Int,
        limit: Int
    ): Payload = middleware.blockDataViewActiveSet(
        contextId = context,
        blockId = block,
        viewId = view,
        offset = offset,
        limit = limit
    )

    override suspend fun setDataViewViewerPosition(
        ctx: Id,
        dv: Id,
        view: Id,
        pos: Int
    ): Payload = middleware.blockDataViewViewSetPosition(
        ctx = ctx,
        dv = dv,
        view = view,
        pos = pos
    )

    override suspend fun addRelationToDataView(
        ctx: Id,
        dv: Id,
        relation: Id
    ): Payload = middleware.blockDataViewRelationAdd(
        ctx = ctx,
        dv = dv,
        relation = relation
    )

    override suspend fun deleteRelationFromDataView(
        ctx: Id,
        dv: Id,
        relation: Id
    ): Payload = middleware.blockDataViewRelationDelete(
        ctx = ctx,
        dv = dv,
        relation = relation
    )

    override suspend fun updateDataViewViewer(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload = middleware.blockDataViewViewUpdate(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun duplicateDataViewViewer(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload = middleware.blockDataViewViewCreate(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun addDataViewViewer(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload = middleware.blockDataViewViewCreate(
        ctx = ctx,
        target = target,
        name = name,
        type = type
    )

    override suspend fun removeDataViewViewer(
        ctx: String,
        dataview: String,
        viewer: String
    ): Payload = middleware.blockDataViewViewDelete(
        ctx = ctx,
        dataview = dataview,
        viewer = viewer
    )

    override suspend fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        keys: List<Id>
    ): List<Map<String, Any?>> = middleware.objectSearch(
        sorts = sorts,
        filters = filters,
        fulltext = fulltext,
        offset = offset,
        limit = limit,
        keys = keys
    )

    override suspend fun searchObjectsWithSubscription(
        subscription: Id,
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        keys: List<String>,
        source: List<String>,
        offset: Long,
        limit: Int,
        beforeId: Id?,
        afterId: Id?,
        ignoreWorkspace: Boolean?,
        noDepSubscription: Boolean?,
        collection: Id?
    ): SearchResult = middleware.objectSearchSubscribe(
        subscription = subscription,
        sorts = sorts,
        filters = filters,
        keys = keys,
        source = source,
        offset = offset,
        limit = limit,
        afterId = afterId,
        beforeId = beforeId,
        ignoreWorkspace = ignoreWorkspace,
        noDepSubscription = noDepSubscription,
        collection = collection
    )

    override suspend fun searchObjectsByIdWithSubscription(
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult = middleware.objectIdsSubscribe(
        subscription = subscription,
        ids = ids,
        keys = keys
    )

    override suspend fun cancelObjectSearchSubscription(
        subscriptions: List<Id>
    ) = middleware.objectSearchUnsubscribe(subscriptions = subscriptions)

    override suspend fun addRelationToObject(
        ctx: Id, relation: Key
    ): Payload = middleware.objectRelationAdd(ctx, relation)

    override suspend fun deleteRelationFromObject(
        ctx: Id,
        relation: Id
    ): Payload = middleware.objectRelationDelete(
        ctx = ctx,
        relation = relation
    )

    override suspend fun debugSpace(): String = middleware.debugSpace()

    override suspend fun debugObject(objectId: Id, path: String): String =
        middleware.debugObject(objectId = objectId, path = path)

    override suspend fun debugLocalStore(
        path: String
    ): String = middleware.debugExportLocalStore(path)

    override suspend fun debugSubscriptions(): List<Id> = middleware.debugSubscriptions()

    override suspend fun turnInto(
        context: String,
        targets: List<String>,
        style: CBTextStyle
    ): Payload = middleware.blockListTurnInto(
        context = context,
        targets = targets,
        style = style
    )

    override suspend fun setObjectDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload = middleware.setObjectDetail(
        ctx = ctx,
        key = key,
        value = value
    )

    override suspend fun setObjectDetails(
        ctx: Id,
        details: Struct
    ): Payload = middleware.setObjectDetails(
        ctx = ctx,
        details = details
    )

    override suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload =
        middleware.blockTextListSetMark(command)

    override suspend fun addRelationToBlock(command: Command.AddRelationToBlock): Payload =
        middleware.blockRelationAdd(command)

    override suspend fun setObjectTypeToObject(
        ctx: Id, objectTypeKey: Id
    ): Payload = middleware.objectSetObjectType(
        ctx = ctx,
        objectTypeKey = objectTypeKey
    )

    override suspend fun addToFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload = middleware.objectRelationAddFeatured(ctx, relations)

    override suspend fun removeFromFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload = middleware.objectRelationRemoveFeatured(ctx, relations)

    override suspend fun setObjectIsFavorite(
        ctx: Id,
        isFavorite: Boolean
    ): Payload = middleware.objectSetIsFavorite(ctx = ctx, isFavorite = isFavorite)

    override suspend fun setObjectListIsFavorite(
        objectIds: List<Id>,
        isFavorite: Boolean
    ) = middleware.objectListSetIsFavorite(objectIds, isFavorite)

    override suspend fun setObjectIsArchived(
        ctx: Id,
        isArchived: Boolean
    ) = middleware.objectSetIsArchived(ctx = ctx, isArchived = isArchived)

    override suspend fun deleteObjects(targets: List<Id>) = middleware.objectListDelete(
        targets = targets
    )

    override suspend fun setObjectListIsArchived(
        targets: List<Id>,
        isArchived: Boolean
    ) = middleware.objectListSetIsArchived(
        targets = targets,
        isArchived = isArchived
    )

    override suspend fun setObjectLayout(ctx: Id, layout: ObjectType.Layout): Payload =
        middleware.objectSetLayout(ctx, layout)

    override suspend fun clearFileCache() = middleware.fileListOffload()

    override suspend fun applyTemplate(ctx: Id, template: Id) = middleware.objectApplyTemplate(
        ctx = ctx,
        template = template
    )

    override suspend fun createTable(
        ctx: String,
        target: String,
        position: Position,
        rows: Int,
        columns: Int
    ): Payload = middleware.createTable(
        ctx = ctx,
        target = target,
        position = position,
        rows = rows,
        columns = columns
    )

    override suspend fun fillTableRow(ctx: String, targetIds: List<String>): Payload =
        middleware.fillTableRow(ctx, targetIds)

    override suspend fun objectToSet(ctx: Id, source: List<String>) {
        middleware.objectToSet(ctx, source)
    }

    override suspend fun objectToCollection(ctx: Id) {
        middleware.objectToCollection(ctx)
    }

    override suspend fun blockDataViewSetSource(
        ctx: Id,
        block: Id,
        sources: List<Id>
    ): Payload {
        return middleware.blockDataViewSetSource(ctx, block, sources)
    }

    override suspend fun clearBlockContent(ctx: Id, blockIds: List<Id>): Payload {
        return middleware.clearBlockContent(
            ctx = ctx,
            blockIds = blockIds
        )
    }

    override suspend fun clearBlockStyle(ctx: Id, blockIds: List<Id>): Payload {
        return middleware.clearBlockStyle(
            ctx = ctx,
            blockIds = blockIds
        )
    }

    override suspend fun fillTableColumn(ctx: Id, blockIds: List<Id>): Payload {
        return middleware.fillTableColumn(
            ctx = ctx,
            blockIds = blockIds
        )
    }

    override suspend fun createTableRow(
        ctx: Id,
        targetId: Id,
        position: Position
    ): Payload {
        return middleware.createTableRow(
            ctx = ctx,
            targetId = targetId,
            position = position.toMiddlewareModel()
        )
    }

    override suspend fun setTableRowHeader(
        ctx: Id,
        targetId: Id,
        isHeader: Boolean
    ): Payload {
        return middleware.setTableRowHeader(
            ctx = ctx,
            targetId = targetId,
            isHeader = isHeader
        )
    }

    override suspend fun createTableColumn(
        ctx: Id,
        targetId: Id,
        position: Position
    ): Payload {
        return middleware.createTableColumn(
            ctx = ctx,
            targetId = targetId,
            position = position.toMiddlewareModel()
        )
    }

    override suspend fun deleteTableColumn(ctx: Id, targetId: Id): Payload {
        return middleware.deleteTableColumn(
            ctx = ctx,
            targetId = targetId
        )
    }

    override suspend fun deleteTableRow(ctx: Id, targetId: Id): Payload {
        return middleware.deleteTableRow(
            ctx = ctx,
            targetId = targetId
        )
    }

    override suspend fun duplicateTableColumn(
        ctx: Id,
        targetId: Id,
        blockId: Id,
        position: Position
    ): Payload {
        return middleware.duplicateTableColumn(
            ctx = ctx,
            targetId = targetId,
            blockId = blockId,
            position = position.toMiddlewareModel()
        )
    }

    override suspend fun duplicateTableRow(
        ctx: Id,
        targetId: Id,
        blockId: Id,
        position: Position
    ): Payload {
        return middleware.duplicateTableRow(
            ctx = ctx,
            targetId = targetId,
            blockId = blockId,
            position = position.toMiddlewareModel()
        )
    }

    override suspend fun sortTable(
        ctx: Id,
        columnId: String,
        type: Block.Content.DataView.Sort.Type
    ): Payload {
        return middleware.sortTable(
            ctx = ctx,
            columnId = columnId,
            type = type.toMiddlewareModel()
        )
    }

    override suspend fun expandTable(
        ctx: Id,
        targetId: Id,
        columns: Int,
        rows: Int
    ): Payload {
        return middleware.expandTable(
            ctx = ctx,
            targetId = targetId,
            columns = columns,
            rows = rows
        )
    }

    override suspend fun createRelation(
        space: Id,
        name: String,
        format: RelationFormat,
        formatObjectTypes: List<Id>,
        prefilled: Struct
    ): ObjectWrapper.Relation = middleware.objectCreateRelation(
        space = space,
        name = name,
        format = format,
        formatObjectTypes = formatObjectTypes,
        prefilled = prefilled
    )

    override suspend fun createType(
        space: Id,
        name: String,
        emojiUnicode: String?
    ): ObjectWrapper.Type = middleware.objectCreateObjectType(
        space = space,
        name = name,
        emojiUnicode = emojiUnicode
    )

    override suspend fun createRelationOption(
        space: Id,
        relation: Key,
        name: String,
        color: String
    ): ObjectWrapper.Option = middleware.objectCreateRelationOption(
        space = space,
        relation = relation,
        name = name,
        color = color
    )

    override suspend fun moveTableColumn(
        ctx: Id,
        target: Id,
        dropTarget: Id,
        position: Position
    ): Payload {
        return middleware.moveTableColumn(
            ctx = ctx,
            target = target,
            dropTarget = dropTarget,
            position = position
        )
    }

    override suspend fun createWorkspace(details: Struct): Id = middleware.workspaceCreate(
        details = details
    )

    override suspend fun getSpaceConfig(space: Id): Config = middleware.workspaceInfo(
        space = space
    )

    override suspend fun addObjectListToSpace(objects: List<Id>, space: Id): List<Id> {
        return middleware.workspaceObjectListAdd(
            objects = objects,
            space = space
        )
    }

    override suspend fun addObjectToSpace(
        obj: Id,
        space: Id
    ): Id = middleware.workspaceObjectAdd(
        obj = obj,
        space = space
    )

    override suspend fun removeObjectFromWorkspace(objects: List<Id>): List<Id> {
        return middleware.workspaceObjectListRemove(objects)
    }

    override suspend fun createObject(
        command: Command.CreateObject
    ): CreateObjectResult = middleware.objectCreate(command)

    override suspend fun createBlockLinkWithObject(
        command: Command.CreateBlockLinkWithObject
    ): CreateBlockLinkWithObjectResult = middleware.blockLinkCreateWithObject(command)

    override suspend fun createWidget(
        ctx: Id,
        source: Id,
        layout: WidgetLayout,
        target: Id?,
        position: Position
    ): Payload = middleware.createWidgetBlock(
        ctx = ctx,
        source = source,
        layout = layout,
        target = target,
        position = position
    )

    override suspend fun updateWidget(
        ctx: Id,
        widget: Id,
        source: Id,
        type: Block.Content.Widget.Layout
    ): Payload = middleware.createWidgetByReplacingExistingWidget(
        ctx = ctx,
        widget = widget,
        source = source,
        type = type
    )

    override suspend fun setWidgetViewId(
        ctx: Id,
        widget: Id,
        view: Id
    ): Payload = middleware.setWidgetViewId(
        ctx = ctx,
        widget = widget,
        view = view
    )

    override suspend fun addDataViewFilter(command: Command.AddFilter): Payload {
        return middleware.addDataViewFilter(command)
    }

    override suspend fun removeDataViewFilter(command: Command.RemoveFilter): Payload {
        return middleware.removeDataViewFilter(command)
    }

    override suspend fun replaceDataViewFilter(command: Command.ReplaceFilter): Payload {
        return middleware.replaceDataViewFilter(command)
    }

    override suspend fun addDataViewSort(command: Command.AddSort): Payload {
        return middleware.addDataViewSort(command)
    }

    override suspend fun removeDataViewSort(command: Command.RemoveSort): Payload {
        return middleware.removeDataViewSort(command)
    }

    override suspend fun replaceDataViewSort(command: Command.ReplaceSort): Payload {
        return middleware.replaceDataViewSort(command)
    }

    override suspend fun addDataViewViewRelation(command: Command.AddRelation): Payload {
        return middleware.addDataViewViewRelation(command)
    }

    override suspend fun removeDataViewViewRelation(command: Command.DeleteRelation): Payload {
        return middleware.removeDataViewViewRelation(command)
    }

    override suspend fun replaceDataViewViewRelation(command: Command.UpdateRelation): Payload {
        return middleware.replaceDataViewViewRelation(command)
    }

    override suspend fun sortDataViewViewRelation(command: Command.SortRelations): Payload {
        return middleware.sortDataViewViewRelation(command)
    }

    override suspend fun addObjectToCollection(command: Command.AddObjectToCollection): Payload {
        return middleware.addObjectToCollection(command)
    }

    override suspend fun setQueryToSet(command: Command.SetQueryToSet): Payload {
        return middleware.setQueryToSet(command)
    }

    override suspend fun fileSpaceUsage(): FileLimits {
        return middleware.fileSpaceUsage()
    }

    override suspend fun setInternalFlags(command: Command.SetInternalFlags): Payload {
        return middleware.setInternalFlags(command)
    }

    override suspend fun duplicateObjectsList(ids: List<Id>): List<Id> {
        return middleware.duplicateObjectsList(ids)
    }
}