package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.core_models.*

interface BlockDataStore {

    suspend fun create(command: Command.Create): Pair<Id, Payload>
    suspend fun replace(command: Command.Replace): Pair<Id, Payload>
    suspend fun duplicate(command: Command.Duplicate): Pair<List<Id>, Payload>
    suspend fun split(command: Command.Split): Pair<Id, Payload>

    suspend fun merge(command: Command.Merge): Payload
    suspend fun updateTextColor(command: Command.UpdateTextColor): Payload
    suspend fun updateBackroundColor(command: Command.UpdateBackgroundColor): Payload
    suspend fun updateAlignment(command: Command.UpdateAlignment): Payload
    suspend fun openDashboard(contextId: String, id: String): Payload

    suspend fun createDocument(command: Command.CreateDocument): Triple<Id, Id, Payload>
    suspend fun createNewDocument(command: Command.CreateNewDocument): String
    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle): Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox): Payload
    suspend fun uploadBlock(command: Command.UploadBlock): Payload
    suspend fun move(command: Command.Move): Payload
    suspend fun unlink(command: Command.Unlink): Payload
    suspend fun getConfig(): Config
    suspend fun createPage(ctx: Id?, emoji: String?, isDraft: Boolean?): Id
    suspend fun openPage(id: String): Payload
    suspend fun openObjectSet(id: String): Payload
    suspend fun openProfile(id: String): Payload
    suspend fun closePage(id: String)
    suspend fun closeDashboard(id: String)
    suspend fun setDocumentEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload
    suspend fun setDocumentImageIcon(command: Command.SetDocumentImageIcon): Payload
    suspend fun setDocumentCoverColor(ctx: String, color: String): Payload
    suspend fun setDocumentCoverGradient(ctx: String, gradient: String): Payload
    suspend fun setDocumentCoverImage(ctx: String, hash: String): Payload
    suspend fun removeDocumentCover(ctx: String): Payload
    suspend fun removeDocumentIcon(ctx: Id): Payload
    suspend fun setupBookmark(command: Command.SetupBookmark): Payload
    suspend fun undo(command: Command.Undo): Payload
    suspend fun redo(command: Command.Redo): Payload
    suspend fun turnIntoDocument(command: Command.TurnIntoDocument): List<Id>
    suspend fun paste(command: Command.Paste): Response.Clipboard.Paste
    suspend fun copy(command: Command.Copy): Response.Clipboard.Copy
    suspend fun setRelationKey(command: Command.SetRelationKey): Payload

    suspend fun uploadFile(command: Command.UploadFile): String

    suspend fun getObjectInfoWithLinks(pageId: String): ObjectInfoWithLinks

    suspend fun getListPages(): List<DocumentInfo>

    suspend fun updateDivider(command: Command.UpdateDivider): Payload

    suspend fun setFields(command: Command.SetFields): Payload

    suspend fun getObjectTypes(): List<ObjectType>
    suspend fun createObjectType(prototype: ObjectType.Prototype): ObjectType

    suspend fun createSet(
        contextId: String,
        targetId: String?,
        position: Position,
        objectType: String?
    ): Response.Set.Create

    suspend fun setActiveDataViewViewer(
        context: Id,
        block: Id,
        view: Id,
        offset: Int,
        limit: Int
    ): Payload

    suspend fun addNewRelationToDataView(
        context: Id,
        target: Id,
        name: String,
        format: Relation.Format
    ): Pair<Id, Payload>

    suspend fun addRelationToDataView(ctx: Id, dv: Id, relation: Id): Payload

    suspend fun updateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload

    suspend fun duplicateDataViewViewer(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload

    suspend fun createDataViewRecord(context: String, target: String): Map<String, Any?>

    suspend fun updateDataViewRecord(
        context: Id,
        target: Id,
        record: Id,
        values: Map<String, Any?>
    )

    suspend fun addDataViewViewer(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload

    suspend fun removeDataViewViewer(
        ctx: Id,
        dataview: Id,
        viewer: Id
    ): Payload

    suspend fun addDataViewRelationOption(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?>

    suspend fun addObjectRelationOption(
        ctx: Id,
        relation: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?>

    suspend fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int
    ): List<Map<String, Any?>>

    suspend fun relationListAvailable(ctx: Id): List<Relation>
    suspend fun addRelationToObject(ctx: Id, relation: Id) : Payload
    suspend fun deleteRelationFromObject(ctx: Id, relation: Id): Payload
    suspend fun addNewRelationToObject(ctx: Id, name: String, format: RelationFormat) : Pair<Id, Payload>

    suspend fun debugSync(): String

    suspend fun turnInto(
        context: String,
        targets: List<String>,
        style: Block.Content.Text.Style
    ): Payload

    suspend fun updateDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload

    suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload

    suspend fun addRelationToBlock(command: Command.AddRelationToBlock): Payload

    suspend fun setObjectTypeToObject(ctx: Id, typeId: Id): Payload

    suspend fun addToFeaturedRelations(ctx: Id, relations: List<Id>): Payload
    suspend fun removeFromFeaturedRelations(ctx: Id, relations: List<Id>): Payload

    fun setObjectIsFavorite(ctx: Id, isFavorite: Boolean) : Payload
    fun setObjectIsArchived(ctx: Id, isArchived: Boolean) : Payload

    fun setObjectListIsArchived(targets: List<Id>, isArchived: Boolean)
    fun deleteObjects(targets: List<Id>)

    fun setObjectLayout(ctx: Id, layout: ObjectType.Layout) : Payload
}