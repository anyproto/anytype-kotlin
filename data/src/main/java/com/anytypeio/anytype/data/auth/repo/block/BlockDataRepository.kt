package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectInfoWithLinks
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.Url
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
    private val remote: BlockDataStore
) : BlockRepository {

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

    override suspend fun createPage(
        ctx: Id?,
        emoji: String?,
        isDraft: Boolean?,
        type: String?,
        template: Id?
    ) = remote.createPage(
        ctx = ctx,
        emoji = emoji,
        isDraft = isDraft,
        type = type,
        template = template
    )

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
    ): Payload = remote.updateBackroundColor(command)

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

    override suspend fun createDocument(
        command: Command.CreateDocument
    ): Triple<String, String, Payload> {
        return remote.createDocument(
            command
        ).let { (id, target, payload) ->
            Triple(id, target, payload)
        }
    }

    override suspend fun createNewDocument(
        command: Command.CreateNewDocument
    ): Id {
        return remote.createNewDocument(command)
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

    override suspend fun getObjectInfoWithLinks(
        pageId: String
    ): ObjectInfoWithLinks = remote.getObjectInfoWithLinks(pageId)

    override suspend fun getListPages(): List<DocumentInfo> = remote.getListPages()

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

    override suspend fun getObjectTypes(): List<ObjectType> {
        return remote.getObjectTypes()
    }

    override suspend fun createObjectType(
        prototype: ObjectType.Prototype
    ): ObjectType = remote.createObjectType(
        ObjectType.Prototype(
            name = prototype.name,
            emoji = prototype.emoji,
            layout = prototype.layout
        )
    )

    override suspend fun createSet(
        context: Id,
        target: Id?,
        position: Position?,
        objectType: String?
    ): CreateObjectSet.Response {
        val result = remote.createSet(
            contextId = context,
            targetId = target,
            objectType = objectType,
            position = position
        )
        return CreateObjectSet.Response(
            target = result.targetId,
            block = result.blockId,
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

    override suspend fun addNewRelationToDataView(
        context: Id,
        target: Id,
        name: String,
        format: Relation.Format,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload> = remote.addNewRelationToDataView(
        context = context,
        target = target,
        name = name,
        format = format,
        limitObjectTypes = limitObjectTypes
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

    override suspend fun updateDataViewRecord(
        context: Id,
        target: Id,
        record: Id,
        values: Map<String, Any?>
    ) = remote.updateDataViewRecord(
        context = context,
        target = target,
        record = record,
        values = values
    )

    override suspend fun createDataViewRecord(
        context: Id,
        target: Id,
        template: Id?,
        prefilled: Map<Id, Any>
    ): Map<String, Any?> = remote.createDataViewRecord(
        context = context,
        target = target,
        template = template,
        prefilled = prefilled
    )

    override suspend fun addDataViewRelationOption(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: Id,
        color: String
    ): Pair<Payload, Id?> = remote.addDataViewRelationOption(
        ctx = ctx,
        dataview = dataview,
        relation = relation,
        record = record,
        name = name,
        color = color
    )

    override suspend fun addObjectRelationOption(
        ctx: Id,
        relation: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?> = remote.addObjectRelationOption(
        ctx = ctx,
        relation = relation,
        name = name,
        color = color
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
        offset: Long,
        limit: Long,
        beforeId: Id?,
        afterId: Id?
    ): SearchResult = remote.searchObjectsWithSubscription(
        subscription = subscription,
        sorts = sorts,
        filters = filters,
        keys = keys,
        offset = offset,
        limit = limit,
        afterId = afterId,
        beforeId = beforeId
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

    override suspend fun relationListAvailable(ctx: Id) = remote.relationListAvailable(ctx)

    override suspend fun addRelationToObject(
        ctx: Id, relation: Id
    ): Payload = remote.addRelationToObject(ctx, relation)

    override suspend fun deleteRelationFromObject(ctx: Id, relation: Id): Payload {
        return remote.deleteRelationFromObject(ctx = ctx, relation = relation)
    }

    override suspend fun addNewRelationToObject(
        ctx: Id,
        name: String,
        format: RelationFormat,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload> = remote.addNewRelationToObject(
        ctx = ctx,
        format = format,
        name = name,
        limitObjectTypes = limitObjectTypes
    )

    override suspend fun debugSync(): String = remote.debugSync()
    override suspend fun debugLocalStore(path: String): String =
        remote.debugLocalStore(path)

    override suspend fun updateDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload = remote.updateDetail(
        ctx = ctx,
        key = key,
        value = value
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
}