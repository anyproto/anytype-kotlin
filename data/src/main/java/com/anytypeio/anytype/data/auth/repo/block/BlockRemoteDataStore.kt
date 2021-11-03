package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.core_models.*

class BlockRemoteDataStore(private val remote: BlockRemote) : BlockDataStore {

    override suspend fun getConfig() = remote.getConfig()

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ) = remote.openDashboard(id = id, contextId = contextId)

    override suspend fun closeDashboard(id: String) {
        remote.closeDashboard(id = id)
    }

    override suspend fun createPage(
        ctx: Id?,
        emoji: String?,
        isDraft: Boolean?,
        type: String?
    ): Id = remote.createPage(ctx = ctx, emoji = emoji, isDraft = isDraft, type = type)

    override suspend fun openPage(id: String): Payload = remote.openPage(id)
    override suspend fun openProfile(id: String): Payload = remote.openProfile(id)
    override suspend fun openObjectSet(id: String): Payload = remote.openObjectSet(id)

    override suspend fun closePage(id: String) {
        remote.closePage(id)
    }

    override suspend fun updateDocumentTitle(command: Command.UpdateTitle) {
        remote.updateDocumentTitle(command)
    }

    override suspend fun updateText(command: Command.UpdateText) {
        remote.updateText(command)
    }

    override suspend fun updateTextStyle(
        command: Command.UpdateStyle
    ): Payload = remote.updateTextStyle(command)

    override suspend fun updateTextColor(
        command: Command.UpdateTextColor
    ): Payload = remote.updateTextColor(command)

    override suspend fun updateBackroundColor(
        command: Command.UpdateBackgroundColor
    ): Payload = remote.updateBackgroundColor(command)

    override suspend fun updateCheckbox(
        command: Command.UpdateCheckbox
    ): Payload = remote.updateCheckbox(command)

    override suspend fun updateAlignment(
        command: Command.UpdateAlignment
    ): Payload = remote.updateAlignment(command)

    override suspend fun uploadBlock(
        command: Command.UploadBlock
    ): Payload = remote.uploadBlock(command)

    override suspend fun create(
        command: Command.Create
    ): Pair<String, Payload> = remote.create(command)

    override suspend fun createDocument(
        command: Command.CreateDocument
    ): Triple<String, String, Payload> = remote.createDocument(command)

    override suspend fun createNewDocument(
        command: Command.CreateNewDocument
    ): String = remote.createPage(command)

    override suspend fun move(command: Command.Move): Payload {
        return remote.move(command)
    }

    override suspend fun duplicate(
        command: Command.Duplicate
    ): Pair<List<Id>, Payload> = remote.duplicate(command)

    override suspend fun unlink(
        command: Command.Unlink
    ): Payload = remote.unlink(command)

    override suspend fun merge(
        command: Command.Merge
    ): Payload = remote.merge(command)

    override suspend fun split(
        command: Command.Split
    ): Pair<String, Payload> = remote.split(command)

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

    override suspend fun undo(command: Command.Undo) = remote.undo(command)

    override suspend fun redo(command: Command.Redo) = remote.redo(command)

    override suspend fun turnIntoDocument(
        command: Command.TurnIntoDocument
    ): List<Id> = remote.turnIntoDocument(command)

    override suspend fun replace(
        command: Command.Replace
    ): Pair<Id, Payload> = remote.replace(command)

    override suspend fun paste(
        command: Command.Paste
    ): Response.Clipboard.Paste = remote.paste(command)

    override suspend fun copy(
        command: Command.Copy
    ): Response.Clipboard.Copy = remote.copy(command)

    override suspend fun uploadFile(
        command: Command.UploadFile
    ): String = remote.uploadFile(command)

    override suspend fun getObjectInfoWithLinks(pageId: String): ObjectInfoWithLinks =
        remote.getObjectInfoWithLinks(pageId)

    override suspend fun getListPages(): List<DocumentInfo> = remote.getListPages()

    override suspend fun setRelationKey(command: Command.SetRelationKey): Payload =
        remote.setRelationKey(command)

    override suspend fun updateDivider(
        command: Command.UpdateDivider
    ): Payload = remote.updateDivider(command)

    override suspend fun setFields(
        command: Command.SetFields
    ): Payload = remote.setFields(command)

    override suspend fun getObjectTypes(): List<ObjectType> = remote.getObjectTypes()

    override suspend fun createObjectType(
        prototype: ObjectType.Prototype
    ): ObjectType = remote.createObjectType(prototype)

    override suspend fun createSet(
        contextId: String,
        targetId: String?,
        position: Position?,
        objectType: String?
    ): Response.Set.Create = remote.createSet(
        contextId = contextId,
        targetId = targetId,
        objectType = objectType,
        position = position
    )

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
        format: Relation.Format
    ): Pair<Id, Payload> = remote.addNewRelationToDataView(
        context = context,
        target = target,
        name = name,
        format = format
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
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload = remote.duplicateDataViewViewer(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun createDataViewRecord(
        context: String,
        target: String
    ): Map<String, Any?> = remote.createDataViewRecord(context = context, target = target)

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

    override suspend fun addDataViewRelationOption(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: String,
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
        name: Id,
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
        limit: Int
    ): List<Map<String, Any?>> = remote.searchObjects(
        sorts = sorts,
        filters = filters,
        fulltext = fulltext,
        offset = offset,
        limit = limit
    )

    override suspend fun relationListAvailable(ctx: Id): List<Relation> =
        remote.relationListAvailable(ctx)

    override suspend fun addRelationToObject(
        ctx: Id,
        relation: Id
    ): Payload = remote.addRelationToObject(ctx = ctx, relation = relation)

    override suspend fun addNewRelationToObject(
        ctx: Id,
        name: String,
        format: RelationFormat
    ): Pair<Id, Payload> = remote.addNewRelationToObject(
        ctx = ctx,
        format = format,
        name = name
    )

    override suspend fun deleteRelationFromObject(
        ctx: Id,
        relation: Id
    ): Payload = remote.deleteRelationFromObject(ctx = ctx, relation = relation)

    override suspend fun debugSync(): String = remote.debugSync()
    override suspend fun debugLocalStore(path: String): String = remote.debugLocalStore(path)

    override suspend fun turnInto(
        context: String,
        targets: List<String>,
        style: Block.Content.Text.Style
    ): Payload = remote.turnInto(
        context = context,
        targets = targets,
        style = style
    )

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

    override fun setObjectIsFavorite(
        ctx: Id,
        isFavorite: Boolean
    ): Payload = remote.setObjectIsFavorite(ctx = ctx, isFavorite = isFavorite)

    override fun setObjectIsArchived(
        ctx: Id,
        isArchived: Boolean
    ): Payload = remote.setObjectIsArchived(ctx = ctx, isArchived = isArchived)

    override fun setObjectListIsArchived(
        targets: List<Id>,
        isArchived: Boolean
    ) = remote.setObjectListIsArchived(
        targets = targets,
        isArchived = isArchived
    )

    override fun deleteObjects(targets: List<Id>) = remote.deleteObjects(targets = targets)

    override fun setObjectLayout(ctx: Id, layout: ObjectType.Layout): Payload =
        remote.setObjectLayout(ctx, layout)
}