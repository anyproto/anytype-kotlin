package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectInfoWithLinks
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

    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle): Payload
    suspend fun setTextIcon(command: Command.SetTextIcon): Payload

    suspend fun setLinkAppearance(command: Command.SetLinkAppearance): Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox): Payload
    suspend fun uploadBlock(command: Command.UploadBlock): Payload
    suspend fun move(command: Command.Move): Payload
    suspend fun unlink(command: Command.Unlink): Payload

    suspend fun openObject(id: Id) : ObjectView
    suspend fun openPage(id: String): Payload
    suspend fun openObjectSet(id: String): Payload
    suspend fun openProfile(id: String): Payload
    suspend fun openObjectPreview(id: Id): Payload
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
    suspend fun createAndFetchBookmarkBlock(command: Command.CreateBookmark): Payload
    suspend fun createBookmarkObject(url: Url): Id
    suspend fun fetchBookmarkObject(ctx: Id, url: Url)
    suspend fun undo(command: Command.Undo): Payload
    suspend fun redo(command: Command.Redo): Payload
    suspend fun turnIntoDocument(command: Command.TurnIntoDocument): List<Id>
    suspend fun paste(command: Command.Paste): Response.Clipboard.Paste
    suspend fun copy(command: Command.Copy): Response.Clipboard.Copy
    suspend fun setRelationKey(command: Command.SetRelationKey): Payload

    suspend fun uploadFile(command: Command.UploadFile): String
    suspend fun downloadFile(command: Command.DownloadFile): String

    suspend fun getObjectInfoWithLinks(pageId: String): ObjectInfoWithLinks

    suspend fun getListPages(): List<DocumentInfo>

    suspend fun updateDivider(command: Command.UpdateDivider): Payload

    suspend fun setFields(command: Command.SetFields): Payload

    suspend fun createSet(objectType: String?): Response.Set.Create

    suspend fun setActiveDataViewViewer(
        context: Id,
        block: Id,
        view: Id,
        offset: Int,
        limit: Int
    ): Payload

    suspend fun addRelationToDataView(ctx: Id, dv: Id, relation: Id): Payload
    suspend fun deleteRelationFromDataView(ctx: Id, dv: Id, relation: Id): Payload

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

    suspend fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        keys: List<Id>
    ): List<Map<String, Any?>>

    suspend fun searchObjectsWithSubscription(
        subscription: Id,
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        keys: List<Key>,
        source: List<String>,
        offset: Long,
        limit: Int,
        beforeId: Id?,
        afterId: Id?,
        ignoreWorkspace: Boolean?,
        noDepSubscription: Boolean?
    ): SearchResult

    suspend fun searchObjectsByIdWithSubscription(
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult

    suspend fun cancelObjectSearchSubscription(subscriptions: List<Id>)

    suspend fun addRelationToObject(ctx: Id, relation: Key) : Payload
    suspend fun deleteRelationFromObject(ctx: Id, relation: Key): Payload

    suspend fun debugSync(): String

    suspend fun debugTree(objectId: Id, path: String): String

    suspend fun debugLocalStore(path: String): String

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

    suspend fun setObjectIsFavorite(ctx: Id, isFavorite: Boolean): Payload
    suspend fun setObjectIsArchived(ctx: Id, isArchived: Boolean): Payload

    suspend fun setObjectListIsArchived(targets: List<Id>, isArchived: Boolean)
    suspend fun deleteObjects(targets: List<Id>)

    suspend fun setObjectLayout(ctx: Id, layout: ObjectType.Layout): Payload

    suspend fun clearFileCache()

    suspend fun duplicateObject(id: Id): Id

    suspend fun applyTemplate(ctx: Id, template: Id)

    suspend fun createTable(
        ctx: String,
        target: String,
        position: Position,
        rows: Int,
        columns: Int
    ): Payload

    suspend fun fillTableRow(ctx: String, targetIds: List<String>): Payload

    suspend fun objectToSet(ctx: Id, source: List<String>): Id

    suspend fun blockDataViewSetSource(ctx: Id, block: Id, sources: List<String>): Payload

    suspend fun clearBlockContent(ctx: Id, blockIds: List<Id>): Payload

    suspend fun clearBlockStyle(ctx: Id, blockIds: List<Id>): Payload

    suspend fun fillTableColumn(ctx: Id, blockIds: List<Id>): Payload

    suspend fun createTableRow(
        ctx: Id,
        targetId: Id,
        position: Position
    ): Payload

    suspend fun setTableRowHeader(
        ctx: Id,
        targetId: Id,
        isHeader: Boolean
    ): Payload

    suspend fun createTableColumn(
        ctx: Id,
        targetId: Id,
        position: Position
    ): Payload

    suspend fun deleteTableColumn(
        ctx: Id,
        targetId: Id
    ): Payload

    suspend fun deleteTableRow(
        ctx: Id,
        targetId: Id
    ): Payload

    suspend fun duplicateTableColumn(
        ctx: Id,
        targetId: Id,
        blockId: Id,
        position: Position
    ): Payload

    suspend fun duplicateTableRow(
        ctx: Id,
        targetId: Id,
        blockId: Id,
        position: Position
    ): Payload

    suspend fun sortTable(
        ctx: Id,
        columnId: Id,
        type: Block.Content.DataView.Sort.Type
    ): Payload

    suspend fun expandTable(
        ctx: Id,
        targetId: Id,
        columns: Int,
        rows: Int
    ): Payload

    suspend fun createRelation(
        name: String,
        format: RelationFormat,
        formatObjectTypes: List<Id>,
        prefilled: Struct
    ) : ObjectWrapper.Relation

    suspend fun createRelationOption(
        relation: Key,
        name: String,
        color: String
    ) : ObjectWrapper.Option

    suspend fun moveTableColumn(
        ctx: Id,
        target: Id,
        dropTarget: Id,
        position: Position
    ): Payload

    suspend fun addObjectToWorkspace(objects: List<Id>) : List<Id>

    suspend fun createObject(command: Command.CreateObject): CreateObjectResult

    suspend fun createBlockLinkWithObject(
        command: Command.CreateBlockLinkWithObject
    ): CreateBlockLinkWithObjectResult

    suspend fun createWidget(ctx: Id, source: Id): Payload

    suspend fun addDataViewFilter(command: Command.AddFilter): Payload
    suspend fun removeDataViewFilter(command: Command.RemoveFilter): Payload
    suspend fun replaceDataViewFilter(command: Command.ReplaceFilter): Payload

    suspend fun addDataViewSort(command: Command.AddSort): Payload
    suspend fun removeDataViewSort(command: Command.RemoveSort): Payload
    suspend fun replaceDataViewSort(command: Command.ReplaceSort): Payload

    suspend fun addDataViewViewRelation(command: Command.AddRelation): Payload
    suspend fun removeDataViewViewRelation(command: Command.DeleteRelation): Payload
    suspend fun replaceDataViewViewRelation(command: Command.UpdateRelation): Payload
    suspend fun sortDataViewViewRelation(command: Command.SortRelations): Payload
}