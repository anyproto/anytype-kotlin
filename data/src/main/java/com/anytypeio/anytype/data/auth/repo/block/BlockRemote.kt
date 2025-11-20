package com.anytypeio.anytype.data.auth.repo.block

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
import com.anytypeio.anytype.core_models.publishing.Publishing

interface BlockRemote {

    suspend fun create(command: Command.Create): Pair<String, Payload>
    suspend fun replace(command: Command.Replace): Pair<String, Payload>
    suspend fun duplicate(command: Command.Duplicate): Pair<List<Id>, Payload>
    suspend fun split(command: Command.Split): Pair<Id, Payload>

    suspend fun merge(command: Command.Merge): Payload
    suspend fun unlink(command: Command.Unlink): Payload
    suspend fun updateTextColor(command: Command.UpdateTextColor): Payload
    suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor): Payload
    suspend fun updateAlignment(command: Command.UpdateAlignment): Payload

    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle): Payload
    suspend fun setTextIcon(command: Command.SetTextIcon): Payload

    suspend fun setLinkAppearance(command: Command.SetLinkAppearance): Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox): Payload
    suspend fun move(command: Command.Move): Payload
    suspend fun createBlockLinkWithObject(
        command: Command.CreateBlockLinkWithObject
    ): CreateBlockLinkWithObjectResult

    suspend fun openObject(id: Id, space: SpaceId): ObjectView
    suspend fun getObject(id: Id, space: SpaceId): ObjectView

    suspend fun openPage(id: String, space: SpaceId): Payload
    suspend fun openProfile(id: String, space: SpaceId): Payload
    suspend fun openObjectSet(id: String, space: SpaceId): Payload
    suspend fun openObjectPreview(id: Id, space: SpaceId): Payload
    suspend fun closeObject(id: String, space: Space)
    suspend fun setDocumentEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload
    suspend fun setDocumentImageIcon(command: Command.SetDocumentImageIcon): Payload
    suspend fun setDocumentCoverColor(ctx: String, color: String): Payload
    suspend fun setDocumentCoverGradient(ctx: String, gradient: String): Payload
    suspend fun setDocumentCoverImage(ctx: String, hash: String): Payload
    suspend fun removeDocumentCover(ctx: String): Payload
    suspend fun removeDocumentIcon(ctx: Id): Payload
    suspend fun uploadBlock(command: Command.UploadBlock): Payload
    suspend fun setupBookmark(command: Command.SetupBookmark): Payload
    suspend fun createAndFetchBookmarkBlock(command: Command.CreateBookmark): Payload
    suspend fun createBookmarkObject(space: Id, url: Url, details: Struct): Id
    suspend fun fetchBookmarkObject(ctx: Id, url: Url)
    suspend fun undo(command: Command.Undo): Payload
    suspend fun importGetStartedUseCase(space: Id) : Command.ImportUseCase.Result
    suspend fun redo(command: Command.Redo): Payload
    suspend fun turnIntoDocument(command: Command.TurnIntoDocument): List<Id>
    suspend fun paste(command: Command.Paste): Response.Clipboard.Paste
    suspend fun copy(command: Command.Copy): Response.Clipboard.Copy

    suspend fun uploadFile(command: Command.UploadFile): ObjectWrapper.File
    suspend fun preloadFile(command: Command.PreloadFile): Id
    suspend fun discardPreloadedFile(command: Command.DiscardPreloadedFile)
    suspend fun fileDrop(command: Command.FileDrop): Payload
    suspend fun downloadFile(command: Command.DownloadFile): String

    suspend fun setRelationKey(command: Command.SetRelationKey): Payload

    suspend fun updateDivider(command: Command.UpdateDivider): Payload

    suspend fun setFields(command: Command.SetFields): Payload

    suspend fun createSet(
        space: Id,
        objectType: String?,
        details: Struct?
    ): Response.Set.Create

    suspend fun setDataViewViewerPosition(
        ctx: Id,
        dv: Id,
        view: Id,
        pos: Int
    ): Payload

    suspend fun addRelationToDataView(ctx: Id, dv: Id, relation: Id): Payload
    suspend fun deleteRelationFromDataView(ctx: Id, dv: Id, relation: Id): Payload

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
    ): Pair<Id, Payload>

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

    suspend fun searchObjects(
        space: SpaceId,
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        keys: List<Id>
    ): List<Map<String, Any?>>

    suspend fun searchObjectWithMeta(
        command: Command.SearchWithMeta
    ): List<Command.SearchWithMeta.Result>

    suspend fun searchObjectsWithSubscription(
        space: SpaceId,
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
    ): SearchResult

    suspend fun searchObjectsByIdWithSubscription(
        space: SpaceId,
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult

    suspend fun crossSpaceSearchSubscribe(
        command: Command.CrossSpaceSearchSubscribe
    ): SearchResult

    suspend fun objectCrossSpaceUnsubscribe(subscription: String)

    suspend fun cancelObjectSearchSubscription(subscriptions: List<Id>)

    suspend fun addRelationToObject(ctx: Id, relation: Key): Payload?
    suspend fun deleteRelationFromObject(ctx: Id, relations: List<Key>): Payload

    suspend fun debugSpace(space: SpaceId): String

    suspend fun debugObject(objectId: Id, path: String): String

    suspend fun debugLocalStore(path: String): String

    suspend fun debugSubscriptions(): List<Id>

    suspend fun setObjectDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload

    suspend fun setObjectDetails(
        ctx: Id,
        details: Struct
    ): Payload

    suspend fun objectDateByTimestamp(command: Command.ObjectDateByTimestamp): Struct?

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
        rows: Int,
        columns: Int
    ): Payload

    suspend fun fillTableRow(ctx: String, targetIds: List<String>): Payload

    suspend fun objectToSet(ctx: Id, source: List<String>)
    suspend fun objectToCollection(ctx: Id)

    suspend fun blockDataViewSetSource(ctx: Id, block: Id, sources: List<String>): Payload

    suspend fun createRelation(
        space: Id,
        name: String,
        format: RelationFormat,
        formatObjectTypes: List<Id>,
        prefilled: Struct
    ): ObjectWrapper.Relation

    suspend fun createType(
        command: Command.CreateObjectType
    ): String

    suspend fun createRelationOption(
        space: Id,
        relation: Id,
        name: String,
        color: String
    ): ObjectWrapper.Option

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
        columnId: String, type: Block.Content.DataView.Sort.Type
    ): Payload

    suspend fun expandTable(
        ctx: Id,
        targetId: Id,
        columns: Int,
        rows: Int
    ): Payload

    suspend fun moveTableColumn(
        ctx: Id,
        target: Id,
        dropTarget: Id,
        position: Position
    ): Payload

    suspend fun setSpaceDetails(space: SpaceId, details: Struct)

    suspend fun deleteSpace(space: SpaceId)
    suspend fun spaceSetOrder(spaceViewId: Id, spaceViewOrder: List<Id>): List<Id>
    suspend fun spaceUnsetOrder(spaceViewId: Id)
    suspend fun createWorkspace(command: Command.CreateSpace): Command.CreateSpace.Result

    suspend fun spaceOpen(space: Id, withChat: Boolean): Config

    suspend fun addObjectListToSpace(objects: List<Id>, space: Id): List<Id>
    suspend fun addObjectToSpace(command: Command.AddObjectToSpace) : Pair<Id, Struct?>
    suspend fun removeObjectFromWorkspace(objects: List<Id>): List<Id>

    suspend fun createObject(command: Command.CreateObject): CreateObjectResult

    suspend fun createWidget(
        ctx: Id,
        source: Id,
        layout: WidgetLayout,
        target: Id?,
        position: Position
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
    suspend fun nodeUsage(): NodeUsageInfo
    suspend fun dataViewSetActiveView(command: Command.DataViewSetActiveView): Payload

    suspend fun setInternalFlags(command: Command.SetInternalFlags): Payload

    suspend fun duplicateObjectsList(ids: List<Id>): List<Id>
    suspend fun createTemplateFromObject(ctx: Id): Id
    suspend fun debugStackGoroutines(path: String)

    suspend fun deleteRelationOption(command: Command.DeleteRelationOptions)

    suspend fun makeSpaceShareable(space: SpaceId)
    suspend fun generateSpaceInviteLink(
        space: SpaceId,
        inviteType: InviteType?,
        permissions: SpaceMemberPermissions?
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

    suspend fun sendJoinSpaceRequest(command: Command.SendJoinSpaceRequest)
    suspend fun cancelJoinSpaceRequest(space: SpaceId)

    suspend fun getSpaceInviteView(
        inviteContentId: Id,
        inviteFileKey: String
    ): SpaceInviteView

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

    suspend fun objectTypesSetOrder(command: Command.ObjectTypesSetOrder): List<String>

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
    suspend fun setSpaceChatsNotifications(command: Command.SpaceChatsNotifications.SetForceModeIds): Payload
    suspend fun resetSpaceChatsNotifications(command: Command.SpaceChatsNotifications.ResetIds): Payload

    //endregion

    suspend fun objectRelationListWithValue(command: Command.RelationListWithValue): List<RelationListWithValueItem>

    suspend fun debugAccountSelectTrace(dir: String): String

    suspend fun setDeviceNetworkState(type: DeviceNetworkType)
    suspend fun setAppState(state: AppState)

    suspend fun objectTypeListConflictingRelations(command: ObjectTypeConflictingFields): List<Id>

    suspend fun objectTypeSetRecommendedHeaderFields(command: Command.ObjectTypeSetRecommendedHeaderFields)
    suspend fun objectTypeSetRecommendedFields(command: Command.ObjectTypeSetRecommendedFields)

    suspend fun setDataViewProperties(command: Command.SetDataViewProperties): Payload

    suspend fun getLinkPreview(url: Url): LinkPreview

    suspend fun createObjectFromUrl(space: SpaceId, url: Url): ObjectWrapper.Basic

    suspend fun setSpaceNotificationMode(spaceViewId: Id, mode: com.anytypeio.anytype.core_models.chats.NotificationState)

    suspend fun debugStats(): String

    suspend fun spaceChangeInvite(command: Command.SpaceChangeInvite)

    //region PUBLISHING

    suspend fun publishingGetStatus(command: Command.Publishing.GetStatus): Publishing.State?
    suspend fun publishingGetList(command: Command.Publishing.GetList): List<Publishing.State>
    suspend fun publishingCreate(command: Command.Publishing.Create): String
    suspend fun publishingRemove(command: Command.Publishing.Remove)

    //endregion
}
