package com.anytypeio.anytype.domain.block.repo

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Command.ObjectTypeConflictingFields
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.DeviceNetworkType
import com.anytypeio.anytype.core_models.AppState
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.LinkPreview
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.NodeUsageInfo
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationListWithValueItem
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.history.DiffVersionResponse
import com.anytypeio.anytype.core_models.history.ShowVersionResponse
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.membership.EmailVerificationStatus
import com.anytypeio.anytype.core_models.membership.GetPaymentUrlResponse
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo


interface BlockRepository {

    suspend fun uploadFile(command: Command.UploadFile): ObjectWrapper.File
    suspend fun fileDrop(command: Command.FileDrop): Payload
    suspend fun downloadFile(command: Command.DownloadFile): String

    suspend fun move(command: Command.Move): Payload
    suspend fun unlink(command: Command.Unlink): Payload

    suspend fun turnIntoDocument(command: Command.TurnIntoDocument): List<Id>

    /**
     * Duplicates target block
     * @return id of the new block and payload events.
     */
    suspend fun duplicate(command: Command.Duplicate): Pair<List<Id>, Payload>

    /**
     * Creates a new block.
     * @return id of the created block with event payload.
     */
    suspend fun create(command: Command.Create): Pair<Id, Payload>

    /**
     * Creates just the new page, without adding the link to it from some other page
     */
    suspend fun createObject(command: Command.CreateObject): CreateObjectResult

    suspend fun createBlockLinkWithObject(
        command: Command.CreateBlockLinkWithObject
    ): CreateBlockLinkWithObjectResult

    suspend fun merge(command: Command.Merge): Payload

    /**
     * Splits one block into two blocks.
     * @return id of the block, created as a result of splitting.
     */
    suspend fun split(command: Command.Split): Pair<Id, Payload>

    /**
     * Replaces target block by a new block (created from prototype).
     * @see Command.Replace for details
     * @return id of the new block
     */
    suspend fun replace(command: Command.Replace): Pair<Id, Payload>

    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle): Payload
    suspend fun setTextIcon(command: Command.SetTextIcon): Payload
    suspend fun setLinkAppearance(command: Command.SetLinkAppearance): Payload

    suspend fun updateTextColor(command: Command.UpdateTextColor): Payload
    suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor): Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox): Payload
    suspend fun updateAlignment(command: Command.UpdateAlignment): Payload

    suspend fun setRelationKey(command: Command.SetRelationKey): Payload

    suspend fun openObject(id: Id, space: SpaceId) : ObjectView
    suspend fun getObject(id: Id, space: SpaceId) : ObjectView

    @Deprecated("To be deleted")
    suspend fun openObjectPreview(id: Id, space: SpaceId): Result<Payload>
    @Deprecated("To be deleted")
    suspend fun openPage(id: String, space: SpaceId): Result<Payload>
    @Deprecated("To be deleted")
    suspend fun openProfile(id: String, space: SpaceId): Payload
    @Deprecated("To be deleted")
    suspend fun openObjectSet(id: String, space: SpaceId): Result<Payload>

    suspend fun closeObject(id: String, space: Space)

    /**
     * Upload media or file block by path or url.
     */
    suspend fun uploadBlock(command: Command.UploadBlock): Payload

    suspend fun setDocumentEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload
    suspend fun setDocumentImageIcon(command: Command.SetDocumentImageIcon): Payload
    suspend fun setDocumentCoverColor(ctx: String, color: String): Payload
    suspend fun setDocumentCoverGradient(ctx: String, gradient: String): Payload
    suspend fun setDocumentCoverImage(ctx: String, hash: String): Payload
    suspend fun removeDocumentCover(ctx: String): Payload
    suspend fun removeDocumentIcon(ctx: Id): Payload

    suspend fun setupBookmark(command: Command.SetupBookmark): Payload
    suspend fun createAndFetchBookmarkBlock(command: Command.CreateBookmark): Payload

    /**
     * Creates bookmark object from url and returns its id.
     */
    suspend fun createBookmarkObject(space: Id, url: Url, details: Struct): Id

    suspend fun fetchBookmarkObject(ctx: Id, url: Url)

    suspend fun undo(command: Command.Undo): Undo.Result

    suspend fun importGetStartedUseCase(space: Id): Command.ImportUseCase.Result

    suspend fun redo(command: Command.Redo): Redo.Result

    suspend fun copy(command: Command.Copy): Response.Clipboard.Copy
    suspend fun paste(command: Command.Paste): Response.Clipboard.Paste

    suspend fun updateDivider(command: Command.UpdateDivider): Payload

    suspend fun setFields(command: Command.SetFields): Payload

    suspend fun createSet(
        space: Id,
        objectType: String? = null,
        details: Struct? = null
    ): CreateObjectSet.Response

    suspend fun addRelationToDataView(ctx: Id, dv: Id, relation: Key): Payload
    suspend fun deleteRelationFromDataView(ctx: Id, dv: Id, relation: Key): Payload

    suspend fun updateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload

    suspend fun duplicateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Pair<Id, Payload>

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
        space: SpaceId,
        sorts: List<DVSort> = emptyList(),
        filters: List<DVFilter> = emptyList(),
        fulltext: String = "",
        offset: Int = 0,
        limit: Int = 0,
        keys: List<Id> = emptyList()
    ): List<Struct>

    suspend fun searchObjectWithMeta(
        command: Command.SearchWithMeta
    ): List<Command.SearchWithMeta.Result>

    suspend fun searchObjectsWithSubscription(
        space: SpaceId,
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
        noDepSubscription: Boolean?,
        collection: Id?
    ): SearchResult

    suspend fun searchObjectsByIdWithSubscription(
        space: SpaceId,
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult

    suspend fun cancelObjectSearchSubscription(subscriptions: List<Id>)

    suspend fun addRelationToObject(ctx: Id, relation: Key): Payload?
    suspend fun deleteRelationFromObject(ctx: Id, relations: List<Key>): Payload

    suspend fun debugSpace(space: SpaceId): String

    suspend fun debugObject(objectId: Id, path: String): String

    suspend fun debugLocalStore(path: String): String

    suspend fun debugSubscriptions(): List<Id>

    suspend fun turnInto(
        context: Id,
        targets: List<Id>,
        style: Block.Content.Text.Style
    ): Payload

    suspend fun setObjectDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload

    suspend fun setObjectDetails(
        ctx: Id,
        details: Struct
    ): Payload

    suspend fun setSpaceDetails(space: SpaceId, details: Struct)

    suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload

    suspend fun addRelationToBlock(command: Command.AddRelationToBlock): Payload

    suspend fun setObjectTypeToObject(ctx: Id, objectTypeKey: Key): Payload

    suspend fun addToFeaturedRelations(ctx: Id, relations: List<Id>): Payload
    suspend fun removeFromFeaturedRelations(ctx: Id, relations: List<Id>): Payload

    suspend fun setObjectListIsFavorite(objectIds: List<Id>, isFavorite: Boolean)
    suspend fun setObjectListIsArchived(targets: List<Id>, isArchived: Boolean)

    suspend fun deleteObjects(targets: List<Id>)

    suspend fun setObjectLayout(ctx: Id, layout: ObjectType.Layout): Payload

    suspend fun clearFileCache()

    suspend fun duplicateObject(id: Id): Id

    suspend fun applyTemplate(command: Command.ApplyTemplate)

    suspend fun createTable(
        ctx: String,
        target: String,
        position: Position,
        rowCount: Int,
        columnCount: Int
    ): Payload

    suspend fun fillTableRow(ctx: String, targetIds: List<String>): Payload

    suspend fun objectToSet(ctx: Id, source: List<String>)
    suspend fun objectToCollection(ctx: Id)

    suspend fun setDataViewViewerPosition(
        ctx: Id,
        dv: Id,
        view: Id,
        pos: Int
    ): Payload

    suspend fun blockDataViewSetSource(ctx: Id, block: Id, sources: List<String>): Payload

    suspend fun createRelation(
        space: Id,
        name: String,
        format: RelationFormat,
        formatObjectTypes: List<Id>,
        prefilled: Struct
    ) : ObjectWrapper.Relation

    suspend fun createType(
        command: Command.CreateObjectType
    ): String

    suspend fun createRelationOption(
        space: Id,
        relation: Id,
        name: String,
        color: String
    ) : ObjectWrapper.Option

    suspend fun clearBlockContent(ctx: Id, blockIds: List<Id>) : Payload

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
        columnId: String, type: Block.Content.DataView.Sort.Type
    ): Payload

    suspend fun expandTable(
        ctx: Id,
        targetId: String,
        columns: Int,
        rows: Int
    ): Payload

    suspend fun moveTableColumn(
        ctx: Id,
        target: Id,
        dropTarget: Id,
        position: Position
    ): Payload

    suspend fun deleteSpace(space: SpaceId)
    suspend fun spaceSetOrder(spaceViewId: Id, spaceViewOrder: List<Id>): List<Id>
    suspend fun spaceUnsetOrder(spaceViewId: Id)
    suspend fun createWorkspace(command: Command.CreateSpace): Command.CreateSpace.Result
    suspend fun getSpaceConfig(space: Id): Config
    suspend fun addObjectListToSpace(objects: List<Id>, space: Id) : List<Id>
    suspend fun addObjectToSpace(command: Command.AddObjectToSpace) : Pair<Id, Struct?>
    suspend fun removeObjectFromWorkspace(objects: List<Id>) : List<Id>

    suspend fun createWidget(
        ctx: Id,
        source: Id,
        layout: WidgetLayout,
        target: Id? = null,
        position: Position = Position.NONE
    ): Payload

    suspend fun updateWidget(
        ctx: Id,
        widget: Id,
        source: Id,
        type: Block.Content.Widget.Layout
    ): Payload

    suspend fun setWidgetViewId(
        ctx: Id,
        widget: Id,
        view: Id
    ): Payload

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
    suspend fun addObjectToCollection(command: Command.AddObjectToCollection): Payload
    suspend fun setQueryToSet(command: Command.SetQueryToSet): Payload
    suspend fun dataViewSetActiveView(command: Command.DataViewSetActiveView): Payload
    suspend fun nodeUsage(): NodeUsageInfo
    suspend fun setInternalFlags(command: Command.SetInternalFlags): Payload
    suspend fun duplicateObjectsList(ids: List<Id>): List<Id>
    suspend fun createTemplateFromObject(ctx: Id): Id
    suspend fun debugStackGoroutines(path: String)
    suspend fun deleteRelationOption(command: Command.DeleteRelationOptions)

    suspend fun makeSpaceShareable(space: SpaceId)
    suspend fun generateSpaceInviteLink(
        space: SpaceId,
        inviteType: InviteType,
        permissions: SpaceMemberPermissions
    ): SpaceInviteLink
    suspend fun revokeSpaceInviteLink(space: SpaceId)
    suspend fun approveSpaceRequest(
        space: SpaceId,
        identity: Id,
        permissions: SpaceMemberPermissions
    )
    suspend fun approveSpaceLeaveRequest(command: Command.ApproveSpaceLeaveRequest)
    suspend fun declineSpaceRequest(space: SpaceId, identity: Id)
    suspend fun removeSpaceMembers(space: SpaceId, identities: List<Id>)
    suspend fun changeSpaceMemberPermissions(
        space: SpaceId,
        identity: Id,
        permission: SpaceMemberPermissions
    )
    suspend fun getSpaceInviteView(
        inviteContentId: Id,
        inviteFileKey: String
    ): SpaceInviteView

    suspend fun sendJoinSpaceRequest(command: Command.SendJoinSpaceRequest)
    suspend fun cancelJoinSpaceRequest(space: SpaceId)

    suspend fun stopSharingSpace(space: SpaceId)

    suspend fun getSpaceInviteLink(spaceId: SpaceId): SpaceInviteLink

    suspend fun downloadGalleryManifest(command: Command.DownloadGalleryManifest): ManifestInfo?
    suspend fun importExperience(command: Command.ImportExperience)

    suspend fun replyNotifications(notifications: List<Id>)

    suspend fun membershipStatus(command: Command.Membership.GetStatus): Membership?
    suspend fun membershipIsNameValid(command: Command.Membership.IsNameValid)
    suspend fun membershipGetPaymentUrl(command: Command.Membership.GetPaymentUrl): GetPaymentUrlResponse
    suspend fun membershipGetPortalLinkUrl(): String
    suspend fun membershipFinalize(command: Command.Membership.Finalize)
    suspend fun membershipGetVerificationEmailStatus(): EmailVerificationStatus
    suspend fun membershipGetVerificationEmail(command: Command.Membership.GetVerificationEmail)
    suspend fun membershipVerifyEmailCode(command: Command.Membership.VerifyEmailCode)
    suspend fun membershipGetTiers(command: Command.Membership.GetTiers): List<MembershipTierData>

    suspend fun processCancel(command: Command.ProcessCancel)

    suspend fun getVersions(command: Command.VersionHistory.GetVersions): List<Version>
    suspend fun showVersion(command: Command.VersionHistory.ShowVersion): ShowVersionResponse
    suspend fun setVersion(command: Command.VersionHistory.SetVersion)
    suspend fun diffVersions(command: Command.VersionHistory.DiffVersions): DiffVersionResponse

    //region CHATS

    suspend fun addChatMessage(command: Command.ChatCommand.AddMessage): Pair<Id, List<Event.Command.Chats>>
    suspend fun editChatMessage(command: Command.ChatCommand.EditMessage)
    suspend fun readChatMessages(command: Command.ChatCommand.ReadMessages)
    suspend fun readAllMessages()
    suspend fun deleteChatMessage(command: Command.ChatCommand.DeleteMessage)
    suspend fun getChatMessages(command: Command.ChatCommand.GetMessages): Command.ChatCommand.GetMessages.Response
    suspend fun getChatMessagesByIds(command: Command.ChatCommand.GetMessagesByIds): List<Chat.Message>
    suspend fun subscribeLastChatMessages(command: Command.ChatCommand.SubscribeLastMessages): Command.ChatCommand.SubscribeLastMessages.Response
    suspend fun toggleChatMessageReaction(command: Command.ChatCommand.ToggleMessageReaction)
    suspend fun unsubscribeChat(chat: Id)
    suspend fun subscribeToMessagePreviews(subscription: Id): List<Chat.Preview>
    suspend fun unsubscribeFromMessagePreviews(subscription: Id)

    //endregion

    suspend fun objectRelationListWithValue(command: Command.RelationListWithValue): List<RelationListWithValueItem>

    suspend fun debugAccountSelectTrace(dir: String): String

    suspend fun objectDateByTimestamp(command: Command.ObjectDateByTimestamp): Struct?

    suspend fun setDeviceNetworkState(type: DeviceNetworkType)

    suspend fun setAppState(state: AppState)

    suspend fun objectTypeListConflictingRelations(command: ObjectTypeConflictingFields): List<Id>

    suspend fun objectTypeSetRecommendedHeaderFields(command: Command.ObjectTypeSetRecommendedHeaderFields)
    suspend fun objectTypeSetRecommendedFields(command: Command.ObjectTypeSetRecommendedFields)

    suspend fun setDataViewProperties(command: Command.SetDataViewProperties): Payload

    suspend fun getLinkPreview(url: Url): LinkPreview

    suspend fun createObjectFromUrl(space: SpaceId, url: Url): ObjectWrapper.Basic

    suspend fun setSpaceNotificationMode(spaceViewId: Id, mode: NotificationState)

    suspend fun debugStats(): String
}