package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.FileLimits
import com.anytypeio.anytype.core_models.Hash
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
import com.anytypeio.anytype.data.auth.exception.BackwardCompatilityNotSupportedException
import com.anytypeio.anytype.data.auth.exception.NotFoundObjectException
import com.anytypeio.anytype.data.auth.exception.UndoRedoExhaustedException
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo

class BlockDataRepository(
    private val remote: BlockRemote
) : BlockRepository {

    override suspend fun openObject(id: Id): ObjectView = remote.openObject(id = id)
    override suspend fun getObject(id: Id): ObjectView = remote.getObject(id = id)

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ) = remote.openDashboard(id = id, contextId = contextId)

    override suspend fun openObjectPreview(id: Id): Result<Payload> = try {
        Result.Success(remote.openObjectPreview(id))
    } catch (e: BackwardCompatilityNotSupportedException) {
        Result.Failure(Error.BackwardCompatibility)
    } catch (e: NotFoundObjectException) {
        Result.Failure(Error.NotFoundObject)
    }

    override suspend fun openPage(id: String): Result<Payload> = try {
        Result.Success(remote.openPage(id))
    } catch (e: BackwardCompatilityNotSupportedException) {
        Result.Failure(Error.BackwardCompatibility)
    } catch (e: NotFoundObjectException) {
        Result.Failure(Error.NotFoundObject)
    }

    override suspend fun openProfile(id: String): Payload =
        remote.openProfile(id)

    override suspend fun openObjectSet(id: String): Result<Payload> = try {
        Result.Success(remote.openObjectSet(id))
    } catch (e: BackwardCompatilityNotSupportedException) {
        Result.Failure(Error.BackwardCompatibility)
    } catch (e: NotFoundObjectException) {
        Result.Failure(Error.NotFoundObject)
    }

    override suspend fun closeDashboard(id: String) {
        remote.closeDashboard(id)
    }

    override suspend fun updateAlignment(
        command: Command.UpdateAlignment
    ): Payload = remote.updateAlignment(command)

    override suspend fun createObject(
        command: Command.CreateObject
    ): CreateObjectResult {
        return remote.createObject(command)
    }

    override suspend fun createBlockLinkWithObject(
        command: Command.CreateBlockLinkWithObject
    ): CreateBlockLinkWithObjectResult = remote.createBlockLinkWithObject(command)

    override suspend fun closePage(id: String) {
        remote.closePage(id)
    }

    override suspend fun updateDocumentTitle(
        command: Command.UpdateTitle
    ) = remote.updateDocumentTitle(command)

    override suspend fun updateText(command: Command.UpdateText) {
        remote.updateText(command)
    }

    override suspend fun updateTextStyle(
        command: Command.UpdateStyle
    ): Payload = remote.updateTextStyle(command)

    override suspend fun setTextIcon(command: Command.SetTextIcon): Payload =
        remote.setTextIcon(command)

    override suspend fun updateTextColor(
        command: Command.UpdateTextColor
    ): Payload = remote.updateTextColor(command)

    override suspend fun setLinkAppearance(command: Command.SetLinkAppearance): Payload {
        return remote.setLinkAppearance(command)
    }

    override suspend fun updateBackgroundColor(
        command: Command.UpdateBackgroundColor
    ): Payload = remote.updateBackgroundColor(command)

    override suspend fun updateCheckbox(
        command: Command.UpdateCheckbox
    ): Payload = remote.updateCheckbox(command)

    override suspend fun create(command: Command.Create): Pair<Id, Payload> {
        return remote.create(command).let { (id, payload) ->
            Pair(id, payload)
        }
    }

    override suspend fun replace(
        command: Command.Replace
    ): Pair<Id, Payload> = remote.replace(command).let { (id, payload) ->
        Pair(id, payload)
    }

    override suspend fun duplicate(
        command: Command.Duplicate
    ): Pair<List<Id>, Payload> = remote.duplicate(command).let { (ids, payload) ->
        Pair(ids, payload)
    }

    override suspend fun move(command: Command.Move): Payload {
        return remote.move(command)
    }

    override suspend fun unlink(
        command: Command.Unlink
    ): Payload = remote.unlink(command)

    override suspend fun merge(
        command: Command.Merge
    ): Payload = remote.merge(command)

    override suspend fun split(
        command: Command.Split
    ): Pair<Id, Payload> = remote.split(command).let { (id, payload) ->
        Pair(id, payload)
    }

    override suspend fun setDocumentEmojiIcon(
        command: Command.SetDocumentEmojiIcon
    ): Payload = remote.setDocumentEmojiIcon(command)

    override suspend fun setDocumentImageIcon(
        command: Command.SetDocumentImageIcon
    ): Payload = remote.setDocumentImageIcon(command)

    override suspend fun setDocumentCoverColor(
        ctx: String,
        color: String
    ): Payload = remote.setDocumentCoverColor(ctx = ctx, color = color)

    override suspend fun setDocumentCoverGradient(
        ctx: String,
        gradient: String
    ): Payload = remote.setDocumentCoverGradient(ctx = ctx, gradient = gradient)

    override suspend fun setDocumentCoverImage(
        ctx: String,
        hash: String
    ): Payload = remote.setDocumentCoverImage(ctx = ctx, hash = hash)

    override suspend fun removeDocumentCover(
        ctx: String
    ): Payload = remote.removeDocumentCover(ctx)

    override suspend fun removeDocumentIcon(
        ctx: Id
    ): Payload = remote.removeDocumentIcon(ctx)

    override suspend fun setupBookmark(
        command: Command.SetupBookmark
    ): Payload = remote.setupBookmark(command)

    override suspend fun createAndFetchBookmarkBlock(
        command: Command.CreateBookmark
    ): Payload = remote.createAndFetchBookmarkBlock(command)

    override suspend fun createBookmarkObject(url: Url): Id = remote.createBookmarkObject(
        url = url
    )

    override suspend fun fetchBookmarkObject(ctx: Id, url: Url) = remote.fetchBookmarkObject(
        ctx = ctx,
        url = url
    )

    override suspend fun uploadBlock(command: Command.UploadBlock): Payload =
        remote.uploadBlock(command)

    override suspend fun undo(
        command: Command.Undo
    ): Undo.Result = try {
        Undo.Result.Success(remote.undo(command))
    } catch (e: UndoRedoExhaustedException) {
        Undo.Result.Exhausted
    }

    override suspend fun importUseCaseSkip() = remote.importUseCaseSkip()

    override suspend fun redo(
        command: Command.Redo
    ): Redo.Result = try {
        Redo.Result.Success(remote.redo(command))
    } catch (e: UndoRedoExhaustedException) {
        Redo.Result.Exhausted
    }

    override suspend fun turnIntoDocument(
        command: Command.TurnIntoDocument
    ): List<Id> = remote.turnIntoDocument(command)

    override suspend fun paste(
        command: Command.Paste
    ): Response.Clipboard.Paste = remote.paste(command)

    override suspend fun copy(
        command: Command.Copy
    ): Response.Clipboard.Copy = remote.copy(command)

    override suspend fun uploadFile(
        command: Command.UploadFile
    ): Hash = remote.uploadFile(command)

    override suspend fun downloadFile(
        command: Command.DownloadFile
    ): String = remote.downloadFile(command)

    override suspend fun setRelationKey(command: Command.SetRelationKey): Payload =
        remote.setRelationKey(command)

    override suspend fun updateDivider(
        command: Command.UpdateDivider
    ): Payload = remote.updateDivider(command = command)

    override suspend fun setFields(
        command: Command.SetFields
    ): Payload = remote.setFields(
        command = Command.SetFields(
            context = command.context,
            fields = command.fields.map { (id, fields) ->
                id to Block.Fields(fields.map)
            }
        )
    )

    override suspend fun turnInto(
        context: Id,
        targets: List<Id>,
        style: Block.Content.Text.Style
    ): Payload = remote.turnInto(
        context = context,
        targets = targets,
        style = style
    )

    override suspend fun createSet(
        objectType: String?
    ): CreateObjectSet.Response {
        val result = remote.createSet(objectType = objectType)
        return CreateObjectSet.Response(
            target = result.targetId,
            payload = result.payload
        )
    }

    override suspend fun setActiveDataViewViewer(
        context: Id,
        block: Id,
        view: Id,
        offset: Int,
        limit: Int
    ): Payload = remote.setActiveDataViewViewer(
        context = context,
        block = block,
        view = view,
        offset = offset,
        limit = limit
    )

    override suspend fun addRelationToDataView(
        ctx: Id,
        dv: Id,
        relation: Id
    ): Payload = remote.addRelationToDataView(
        ctx = ctx,
        dv = dv,
        relation = relation
    )

    override suspend fun deleteRelationFromDataView(
        ctx: Id,
        dv: Id,
        relation: Id
    ): Payload = remote.deleteRelationFromDataView(
        ctx = ctx,
        dv = dv,
        relation = relation
    )

    override suspend fun updateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload = remote.updateDataViewViewer(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun duplicateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload = remote.duplicateDataViewViewer(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun addDataViewViewer(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload = remote.addDataViewViewer(
        ctx = ctx,
        target = target,
        name = name,
        type = type
    )

    override suspend fun removeDataViewViewer(
        ctx: Id,
        dataview: Id,
        viewer: Id
    ): Payload = remote.removeDataViewViewer(
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
    ): List<Map<String, Any?>> = remote.searchObjects(
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
    ): SearchResult = remote.searchObjectsWithSubscription(
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
    ): SearchResult = remote.searchObjectsByIdWithSubscription(
        subscription = subscription,
        ids = ids,
        keys = keys
    )

    override suspend fun cancelObjectSearchSubscription(
        subscriptions: List<Id>
    ) = remote.cancelObjectSearchSubscription(subscriptions)

    override suspend fun addRelationToObject(
        ctx: Id, relation: Id
    ): Payload = remote.addRelationToObject(ctx, relation)

    override suspend fun deleteRelationFromObject(ctx: Id, relation: Key): Payload {
        return remote.deleteRelationFromObject(ctx = ctx, relation = relation)
    }

    override suspend fun debugSpace(): String = remote.debugSpace()

    override suspend fun debugObject(objectId: Id, path: String): String =
        remote.debugObject(objectId = objectId, path = path)

    override suspend fun debugLocalStore(path: String): String =
        remote.debugLocalStore(path)

    override suspend fun setObjectDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload = remote.setObjectDetail(
        ctx = ctx,
        key = key,
        value = value
    )

    override suspend fun setObjectDetails(
        ctx: Id,
        details: Struct
    ): Payload = remote.setObjectDetails(
        ctx = ctx,
        details = details
    )

    override suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload =
        remote.updateBlocksMark(command)

    override suspend fun addRelationToBlock(command: Command.AddRelationToBlock): Payload =
        remote.addRelationToBlock(command)

    override suspend fun setObjectTypeToObject(ctx: Id, typeId: Id): Payload =
        remote.setObjectTypeToObject(ctx = ctx, typeId = typeId)

    override suspend fun addToFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload = remote.addToFeaturedRelations(ctx, relations)

    override suspend fun removeFromFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload = remote.removeFromFeaturedRelations(ctx, relations)

    override suspend fun setObjectIsFavorite(
        ctx: Id,
        isFavorite: Boolean
    ): Payload = remote.setObjectIsFavorite(ctx = ctx, isFavorite = isFavorite)

    override suspend fun setObjectListIsFavorite(
        objectIds: List<Id>,
        isFavorite: Boolean
    ) = remote.setObjectListIsFavorite(objectIds, isFavorite)

    override suspend fun setObjectIsArchived(
        ctx: Id,
        isArchived: Boolean
    ): Payload = remote.setObjectIsArchived(ctx = ctx, isArchived = isArchived)

    override suspend fun setObjectListIsArchived(
        targets: List<Id>,
        isArchived: Boolean
    ) = remote.setObjectListIsArchived(
        targets = targets,
        isArchived = isArchived
    )

    override suspend fun deleteObjects(targets: List<Id>) =
        remote.deleteObjects(targets = targets)

    override suspend fun setObjectLayout(ctx: Id, layout: ObjectType.Layout): Payload =
        remote.setObjectLayout(ctx, layout)

    override suspend fun clearFileCache() = remote.clearFileCache()

    override suspend fun applyTemplate(ctx: Id, template: Id) = remote.applyTemplate(
        ctx = ctx,
        template = template
    )

    override suspend fun duplicateObject(id: Id): Id {
        return remote.duplicateObject(id)
    }

    override suspend fun createTable(
        ctx: String,
        target: String,
        position: Position,
        rowCount: Int,
        columnCount: Int
    ): Payload = remote.createTable(
        ctx = ctx,
        target = target,
        position = position,
        rows = rowCount,
        columns = columnCount
    )

    override suspend fun fillTableRow(ctx: String, targetIds: List<String>): Payload =
        remote.fillTableRow(ctx, targetIds)

    override suspend fun objectToSet(ctx: Id, source: List<String>) {
        remote.objectToSet(ctx, source)
    }

    override suspend fun objectToCollection(ctx: Id) {
        remote.objectToCollection(ctx)
    }

    override suspend fun clearBlockContent(ctx: Id, blockIds: List<Id>): Payload {
        return remote.clearBlockContent(ctx, blockIds)
    }

    override suspend fun setDataViewViewerPosition(
        ctx: Id,
        dv: Id,
        view: Id,
        pos: Int
    ): Payload = remote.setDataViewViewerPosition(
        ctx = ctx,
        dv = dv,
        view = view,
        pos = pos
    )

    override suspend fun blockDataViewSetSource(
        ctx: Id,
        block: Id,
        sources: List<Id>
    ): Payload {
        return remote.blockDataViewSetSource(ctx, block, sources)
    }

    override suspend fun createRelation(
        name: String,
        format: RelationFormat,
        formatObjectTypes: List<Id>,
        prefilled: Struct
    ): ObjectWrapper.Relation = remote.createRelation(
        name = name,
        format = format,
        formatObjectTypes = formatObjectTypes,
        prefilled = prefilled
    )

    override suspend fun createType(
        name: String,
        emojiUnicode: String?,
    ): ObjectWrapper.Type = remote.createType(
        name = name,
        emojiUnicode = emojiUnicode
    )

    override suspend fun createRelationOption(
        relation: Key,
        name: String,
        color: String
    ): ObjectWrapper.Option = remote.createRelationOption(
        relation = relation,
        name = name,
        color = color
    )

    override suspend fun clearBlockStyle(ctx: Id, blockIds: List<Id>): Payload {
        return remote.clearBlockStyle(
            ctx = ctx,
            blockIds = blockIds
        )
    }

    override suspend fun fillTableColumn(ctx: Id, blockIds: List<Id>): Payload {
        return remote.fillTableColumn(
            ctx = ctx,
            blockIds = blockIds
        )
    }

    override suspend fun createTableRow(
        ctx: Id,
        targetId: Id,
        position: Position
    ): Payload {
        return remote.createTableRow(
            ctx = ctx,
            targetId = targetId,
            position = position
        )
    }

    override suspend fun setTableRowHeader(
        ctx: Id,
        targetId: Id,
        isHeader: Boolean
    ): Payload {
        return remote.setTableRowHeader(
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
        return remote.createTableColumn(
            ctx = ctx,
            targetId = targetId,
            position = position
        )
    }

    override suspend fun deleteTableColumn(ctx: Id, targetId: Id): Payload {
        return remote.deleteTableColumn(
            ctx = ctx,
            targetId = targetId
        )
    }

    override suspend fun deleteTableRow(ctx: Id, targetId: Id): Payload {
        return remote.deleteTableRow(
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
        return remote.duplicateTableColumn(
            ctx = ctx,
            targetId = targetId,
            blockId = blockId,
            position = position
        )
    }

    override suspend fun duplicateTableRow(
        ctx: Id,
        targetId: Id,
        blockId: Id,
        position: Position
    ): Payload {
        return remote.duplicateTableRow(
            ctx = ctx,
            targetId = targetId,
            blockId = blockId,
            position = position
        )
    }

    override suspend fun sortTable(
        ctx: Id,
        columnId: String,
        type: Block.Content.DataView.Sort.Type
    ): Payload {
        return remote.sortTable(
            ctx = ctx,
            columnId = columnId,
            type = type
        )
    }

    override suspend fun expandTable(
        ctx: Id,
        targetId: Id,
        columns: Int,
        rows: Int
    ): Payload {
        return remote.expandTable(
            ctx = ctx,
            targetId = targetId,
            columns = columns,
            rows = rows
        )
    }

    override suspend fun moveTableColumn(
        ctx: Id,
        target: Id,
        dropTarget: Id,
        position: Position
    ): Payload {
        return remote.moveTableColumn(
            ctx = ctx,
            target = target,
            dropTarget = dropTarget,
            position = position
        )
    }

    override suspend fun addObjectToWorkspace(objects: List<Id>): List<Id> {
        return remote.addObjectToWorkspace(
            objects = objects
        )
    }

    override suspend fun removeObjectFromWorkspace(objects: List<Id>): List<Id> {
        return remote.removeObjectFromWorkspace(
            objects = objects
        )
    }

    override suspend fun createWidget(
        ctx: Id,
        source: Id,
        layout: WidgetLayout,
        target: Id?,
        position: Position
    ): Payload = remote.createWidget(
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
    ): Payload = remote.updateWidget(
        ctx = ctx,
        widget = widget,
        source = source,
        type = type
    )

    override suspend fun setWidgetViewId(
        ctx: Id,
        widget: Id,
        view: Id
    ): Payload = remote.setWidgetViewId(
        ctx = ctx,
        widget = widget,
        view = view
    )

    override suspend fun addDataViewFilter(command: Command.AddFilter): Payload {
        return remote.addDataViewFilter(command = command)
    }

    override suspend fun removeDataViewFilter(command: Command.RemoveFilter): Payload {
        return remote.removeDataViewFilter(command)
    }

    override suspend fun replaceDataViewFilter(command: Command.ReplaceFilter): Payload {
        return remote.replaceDataViewFilter(command)
    }

    override suspend fun addDataViewSort(command: Command.AddSort): Payload {
        return remote.addDataViewSort(command)
    }

    override suspend fun removeDataViewSort(command: Command.RemoveSort): Payload {
        return remote.removeDataViewSort(command)
    }

    override suspend fun replaceDataViewSort(command: Command.ReplaceSort): Payload {
        return remote.replaceDataViewSort(command)
    }

    override suspend fun addDataViewViewRelation(command: Command.AddRelation): Payload {
        return remote.addDataViewViewRelation(command)
    }

    override suspend fun removeDataViewViewRelation(command: Command.DeleteRelation): Payload {
        return remote.removeDataViewViewRelation(command)
    }

    override suspend fun replaceDataViewViewRelation(command: Command.UpdateRelation): Payload {
        return remote.replaceDataViewViewRelation(command)
    }

    override suspend fun sortDataViewViewRelation(command: Command.SortRelations): Payload {
        return remote.sortDataViewViewRelation(command)
    }

    override suspend fun addObjectToCollection(command: Command.AddObjectToCollection): Payload {
        return remote.addObjectToCollection(command)
    }

    override suspend fun setQueryToSet(command: Command.SetQueryToSet): Payload {
        return remote.setQueryToSet(command)
    }

    override suspend fun fileSpaceUsage(): FileLimits {
        return remote.fileSpaceUsage()
    }
}