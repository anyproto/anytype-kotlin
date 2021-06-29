package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.core_models.*

interface BlockRemote {

    suspend fun create(command: Command.Create): Pair<String, Payload>
    suspend fun replace(command: Command.Replace): Pair<String, Payload>
    suspend fun duplicate(command: Command.Duplicate): Pair<String, Payload>
    suspend fun split(command: Command.Split): Pair<Id, Payload>

    suspend fun merge(command: Command.Merge): Payload
    suspend fun unlink(command: Command.Unlink): Payload
    suspend fun updateTextColor(command: Command.UpdateTextColor): Payload
    suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor): Payload
    suspend fun updateAlignment(command: Command.UpdateAlignment) : Payload

    suspend fun createDocument(command: Command.CreateDocument): Triple<String, String, Payload>
    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle) : Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox): Payload
    suspend fun move(command: Command.Move): Payload
    suspend fun getConfig(): Config
    suspend fun createPage(parentId: String, emoji: String?): String
    suspend fun createPage(command: Command.CreateNewDocument): String
    suspend fun openPage(id: String): Payload
    suspend fun openProfile(id: String): Payload
    suspend fun openObjectSet(id: String): Payload
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String): Payload
    suspend fun closeDashboard(id: String)
    suspend fun setDocumentEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload
    suspend fun setDocumentImageIcon(command: Command.SetDocumentImageIcon): Payload
    suspend fun setDocumentCoverColor(ctx: String, color: String): Payload
    suspend fun setDocumentCoverGradient(ctx: String, gradient: String): Payload
    suspend fun setDocumentCoverImage(ctx: String, hash: String): Payload
    suspend fun removeDocumentCover(ctx: String): Payload
    suspend fun uploadBlock(command: Command.UploadBlock): Payload
    suspend fun setupBookmark(command: Command.SetupBookmark) : Payload
    suspend fun undo(command: Command.Undo) : Payload
    suspend fun redo(command: Command.Redo) : Payload
    suspend fun archiveDocument(command: Command.ArchiveDocument)
    suspend fun turnIntoDocument(command: Command.TurnIntoDocument): List<Id>
    suspend fun paste(command: Command.Paste) : Response.Clipboard.Paste
    suspend fun copy(command: Command.Copy) : Response.Clipboard.Copy

    suspend fun uploadFile(command: Command.UploadFile): String

    suspend fun getPageInfoWithLinks(pageId: String): PageInfoWithLinks

    suspend fun getListPages(): List<DocumentInfo>

    suspend fun linkToObject(
        context: String,
        target: String,
        block: String,
        replace: Boolean,
        position: Position
    ): Payload

    suspend fun setRelationKey(command: Command.SetRelationKey): Payload

    suspend fun updateDivider(command: Command.UpdateDivider): Payload

    suspend fun setFields(command: Command.SetFields): Payload

    suspend fun getTemplates(): List<ObjectType>
    suspend fun createTemplate(prototype: ObjectType.Prototype): ObjectType

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
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload

    suspend fun turnInto(
        context: String,
        targets: List<String>,
        style: Block.Content.Text.Style
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
        ctx: String,
        dataview: String,
        viewer: String
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
        name: Id,
        color: String
    ): Pair<Payload, Id?>

    suspend fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        objectTypeFilter: List<Id> = emptyList()
    ): List<Map<String, Any?>>

    suspend fun relationListAvailable(ctx: Id): List<Relation>
    suspend fun addRelationToObject(ctx: Id, relation: Id) : Payload
    suspend fun addNewRelationToObject(ctx: Id, name: String, format: RelationFormat) : Payload

    suspend fun debugSync(): String

    suspend fun updateDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload

    suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload

    suspend fun addRelationToBlock(command: Command.AddRelationToBlock): Payload
}