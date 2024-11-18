package com.anytypeio.anytype.middleware.service

import anytype.Rpc

/**
 * Service for interacting with the backend.
 */
interface MiddlewareService {

    //region APP commands

    @Throws(Exception::class)
    fun setInitialParams(request: Rpc.Initial.SetParameters.Request): Rpc.Initial.SetParameters.Response

    @Throws(Exception::class)
    fun versionGet(request: Rpc.App.GetVersion.Request): Rpc.App.GetVersion.Response

    //endregion

    //region WALLET commands

    @Throws(Exception::class)
    fun walletCreate(request: Rpc.Wallet.Create.Request): Rpc.Wallet.Create.Response

    @Throws(Exception::class)
    fun walletRecover(request: Rpc.Wallet.Recover.Request): Rpc.Wallet.Recover.Response

    @Throws(Exception::class)
    fun walletConvert(request: Rpc.Wallet.Convert.Request): Rpc.Wallet.Convert.Response

    //endregion com

    //region ACCOUNT commands

    @Throws(Exception::class)
    fun accountRecover(request: Rpc.Account.Recover.Request): Rpc.Account.Recover.Response

    @Throws(Exception::class)
    fun accountCreate(request: Rpc.Account.Create.Request): Rpc.Account.Create.Response

    @Throws(Exception::class)
    fun accountDelete(request: Rpc.Account.Delete.Request): Rpc.Account.Delete.Response

    @Throws(Exception::class)
    fun accountRevertDeletion(request: Rpc.Account.RevertDeletion.Request): Rpc.Account.RevertDeletion.Response

    @Throws(Exception::class)
    fun accountSelect(request: Rpc.Account.Select.Request): Rpc.Account.Select.Response

    @Throws(Exception::class)
    fun accountStop(request: Rpc.Account.Stop.Request): Rpc.Account.Stop.Response

    //endregion

    //region OBJECT commands

    @Throws(Exception::class)
    fun objectOpen(request: Rpc.Object.Open.Request): Rpc.Object.Open.Response

    @Throws(Exception::class)
    fun objectClose(request: Rpc.Object.Close.Request): Rpc.Object.Close.Response

    @Throws(Exception::class)
    fun objectShow(request: Rpc.Object.Show.Request): Rpc.Object.Show.Response

    @Throws(Exception::class)
    fun objectCreate(request: Rpc.Object.Create.Request): Rpc.Object.Create.Response

    @Throws(Exception::class)
    fun objectCreateBookmark(request: Rpc.Object.CreateBookmark.Request) : Rpc.Object.CreateBookmark.Response

    @Throws(Exception::class)
    fun objectBookmarkFetch(request: Rpc.Object.BookmarkFetch.Request) : Rpc.Object.BookmarkFetch.Response

    @Throws(Exception::class)
    fun objectCreateRelation(request: Rpc.Object.CreateRelation.Request): Rpc.Object.CreateRelation.Response

    @Throws(Exception::class)
    fun objectCreateObjectType(request: Rpc.Object.CreateObjectType.Request): Rpc.Object.CreateObjectType.Response

    @Throws(Exception::class)
    fun objectCreateRelationOption(request: Rpc.Object.CreateRelationOption.Request): Rpc.Object.CreateRelationOption.Response

    @Throws(Exception::class)
    fun deleteRelationOptions(request: Rpc.Relation.ListRemoveOption.Request): Rpc.Relation.ListRemoveOption.Response

    @Throws(Exception::class)
    fun objectCreateSet(request: Rpc.Object.CreateSet.Request): Rpc.Object.CreateSet.Response

    @Throws(Exception::class)
    fun objectSearch(request: Rpc.Object.Search.Request): Rpc.Object.Search.Response

    @Throws(Exception::class)
    fun objectSearchWithMeta(request: Rpc.Object.SearchWithMeta.Request): Rpc.Object.SearchWithMeta.Response

    @Throws(Exception::class)
    fun objectSearchSubscribe(request: Rpc.Object.SearchSubscribe.Request): Rpc.Object.SearchSubscribe.Response

    @Throws(Exception::class)
    fun objectSearchUnsubscribe(request: Rpc.Object.SearchUnsubscribe.Request): Rpc.Object.SearchUnsubscribe.Response

    @Throws(Exception::class)
    fun objectIdsSubscribe(request: Rpc.Object.SubscribeIds.Request): Rpc.Object.SubscribeIds.Response

    @Throws(Exception::class)
    fun objectSetDetails(request: Rpc.Object.SetDetails.Request): Rpc.Object.SetDetails.Response

    @Throws(Exception::class)
    fun objectDuplicate(request: Rpc.Object.Duplicate.Request): Rpc.Object.Duplicate.Response

    @Throws(Exception::class)
    fun objectSetObjectType(request: Rpc.Object.SetObjectType.Request): Rpc.Object.SetObjectType.Response

    @Throws(Exception::class)
    fun objectSetLayout(request: Rpc.Object.SetLayout.Request): Rpc.Object.SetLayout.Response

    @Throws(Exception::class)
    fun objectListSetIsFavorite(request: Rpc.Object.ListSetIsFavorite.Request): Rpc.Object.ListSetIsFavorite.Response

    @Throws(Exception::class)
    fun objectListSetIsArchived(request: Rpc.Object.ListSetIsArchived.Request): Rpc.Object.ListSetIsArchived.Response

    @Throws(Exception::class)
    fun objectListDelete(request: Rpc.Object.ListDelete.Request): Rpc.Object.ListDelete.Response

    @Throws(Exception::class)
    fun objectApplyTemplate(request: Rpc.Object.ApplyTemplate.Request): Rpc.Object.ApplyTemplate.Response

    @Throws(Exception::class)
    fun objectUndo(request: Rpc.Object.Undo.Request): Rpc.Object.Undo.Response

    @Throws(Exception::class)
    fun objectImportUseCase(request: Rpc.Object.ImportUseCase.Request): Rpc.Object.ImportUseCase.Response

    @Throws(Exception::class)
    fun objectRedo(request: Rpc.Object.Redo.Request): Rpc.Object.Redo.Response

    @Throws(Exception::class)
    fun objectToSet(request: Rpc.Object.ToSet.Request): Rpc.Object.ToSet.Response

    @Throws(Exception::class)
    fun objectToCollection(request: Rpc.Object.ToCollection.Request): Rpc.Object.ToCollection.Response

    @Throws(Exception::class)
    fun addObjectToCollection(request : Rpc.ObjectCollection.Add.Request): Rpc.ObjectCollection.Add.Response

    @Throws(Exception::class)
    fun setObjectSource(request: Rpc.Object.SetSource.Request): Rpc.Object.SetSource.Response

    @Throws(Exception::class)
    fun setInternalFlags(request: Rpc.Object.SetInternalFlags.Request): Rpc.Object.SetInternalFlags.Response

    @Throws(Exception::class)
    fun objectsListDuplicate(request: Rpc.Object.ListDuplicate.Request): Rpc.Object.ListDuplicate.Response

    @Throws(Exception::class)
    fun createTemplateFromObject(request: Rpc.Template.CreateFromObject.Request): Rpc.Template.CreateFromObject.Response

    //endregion

    //region OBJECT'S RELATIONS command

    @Throws(Exception::class)
    fun objectRelationAdd(request: Rpc.ObjectRelation.Add.Request): Rpc.ObjectRelation.Add.Response

    @Throws(Exception::class)
    fun objectRelationDelete(request: Rpc.ObjectRelation.Delete.Request): Rpc.ObjectRelation.Delete.Response

    @Throws(Exception::class)
    fun objectRelationAddFeatured(request: Rpc.ObjectRelation.AddFeatured.Request): Rpc.ObjectRelation.AddFeatured.Response

    @Throws(Exception::class)
    fun objectRelationRemoveFeatured(request: Rpc.ObjectRelation.RemoveFeatured.Request): Rpc.ObjectRelation.RemoveFeatured.Response

    @Throws(Exception::class)
    fun objectRelationListAvailable(request: Rpc.ObjectRelation.ListAvailable.Request): Rpc.ObjectRelation.ListAvailable.Response

//    @Throws(Exception::class)
//    fun objectRelationOptionAdd(request: Rpc.ObjectRelationOption.Add.Request): Rpc.ObjectRelationOption.Add.Response

    //endregion

    //region FILES commands

    @Throws(Exception::class)
    fun fileListOffload(request: Rpc.File.ListOffload.Request): Rpc.File.ListOffload.Response

    @Throws(Exception::class)
    fun fileUpload(request: Rpc.File.Upload.Request): Rpc.File.Upload.Response

    @Throws(Exception::class)
    fun fileDownload(request: Rpc.File.Download.Request): Rpc.File.Download.Response

    @Throws(Exception::class)
    fun spaceUsage(request: Rpc.File.SpaceUsage.Request): Rpc.File.SpaceUsage.Response

    @Throws(Exception::class)
    fun fileDrop(request: Rpc.File.Drop.Request): Rpc.File.Drop.Response

    @Throws(Exception::class)
    fun processCancel(request: Rpc.Process.Cancel.Request): Rpc.Process.Cancel.Response

    //endregion

    //region UNSPLASH commands

    @Throws(Exception::class)
    fun unsplashSearch(request: Rpc.Unsplash.Search.Request): Rpc.Unsplash.Search.Response

    @Throws(Exception::class)
    fun unsplashDownload(request: Rpc.Unsplash.Download.Request): Rpc.Unsplash.Download.Response

    //endregion

    //region BLOCK commands

    @Throws(Exception::class)
    fun blockCreate(request: Rpc.Block.Create.Request): Rpc.Block.Create.Response

    @Throws(Exception::class)
    fun blockPaste(request: Rpc.Block.Paste.Request): Rpc.Block.Paste.Response

    @Throws(Exception::class)
    fun blockCopy(request: Rpc.Block.Copy.Request): Rpc.Block.Copy.Response

    @Throws(Exception::class)
    fun blockUpload(request: Rpc.Block.Upload.Request): Rpc.Block.Upload.Response

    @Throws(Exception::class)
    fun blockMerge(request: Rpc.Block.Merge.Request): Rpc.Block.Merge.Response

    @Throws(Exception::class)
    fun blockSplit(request: Rpc.Block.Split.Request): Rpc.Block.Split.Response

    @Throws(Exception::class)
    fun blockListDelete(request: Rpc.Block.ListDelete.Request): Rpc.Block.ListDelete.Response

    @Throws(Exception::class)
    fun blockListMoveToExistingObject(request: Rpc.Block.ListMoveToExistingObject.Request): Rpc.Block.ListMoveToExistingObject.Response

    @Throws(Exception::class)
    fun blockListMoveToNewObject(request: Rpc.Block.ListMoveToNewObject.Request): Rpc.Block.ListMoveToNewObject.Response

    @Throws(Exception::class)
    fun blockListSetFields(request: Rpc.Block.ListSetFields.Request): Rpc.Block.ListSetFields.Response

    @Throws(Exception::class)
    fun blockListSetBackgroundColor(request: Rpc.Block.ListSetBackgroundColor.Request): Rpc.Block.ListSetBackgroundColor.Response

    @Throws(Exception::class)
    fun blockListSetAlign(request: Rpc.Block.ListSetAlign.Request): Rpc.Block.ListSetAlign.Response

    @Throws(Exception::class)
    fun blockListDuplicate(request: Rpc.Block.ListDuplicate.Request): Rpc.Block.ListDuplicate.Response

    @Throws(Exception::class)
    fun blockListTurnInto(request: Rpc.Block.ListTurnInto.Request): Rpc.Block.ListTurnInto.Response

    @Throws(Exception::class)
    fun blockListSetDivStyle(request: Rpc.BlockDiv.ListSetStyle.Request): Rpc.BlockDiv.ListSetStyle.Response

    @Throws(Exception::class)
    fun blockBookmarkFetch(request: Rpc.BlockBookmark.Fetch.Request): Rpc.BlockBookmark.Fetch.Response

    @Throws(Exception::class)
    fun blockBookmarkCreateAndFetch(request: Rpc.BlockBookmark.CreateAndFetch.Request): Rpc.BlockBookmark.CreateAndFetch.Response

    @Throws(Exception::class)
    fun blockLinkCreateWithObject(request: Rpc.BlockLink.CreateWithObject.Request): Rpc.BlockLink.CreateWithObject.Response

    @Throws(Exception::class)
    fun blockRelationAdd(request: Rpc.BlockRelation.Add.Request): Rpc.BlockRelation.Add.Response

    @Throws(Exception::class)
    fun blockRelationSetKey(request: Rpc.BlockRelation.SetKey.Request): Rpc.BlockRelation.SetKey.Response

    @Throws(Exception::class)
    fun blockListClearContent(request: Rpc.BlockText.ListClearContent.Request)
            : Rpc.BlockText.ListClearContent.Response

    @Throws(Exception::class)
    fun blockListClearStyle(request: Rpc.BlockText.ListClearStyle.Request)
            : Rpc.BlockText.ListClearStyle.Response

    //endregion

    //region NAVIGATION commands

    @Throws(Exception::class)
    fun navigationGetObjectInfoWithLinks(request: Rpc.Navigation.GetObjectInfoWithLinks.Request): Rpc.Navigation.GetObjectInfoWithLinks.Response

    @Throws(Exception::class)
    fun navigationListObjects(request: Rpc.Navigation.ListObjects.Request): Rpc.Navigation.ListObjects.Response

    //endregion

    //region DATA VIEW commands

    @Throws(Exception::class)
    fun blockDataViewViewCreate(request: Rpc.BlockDataview.View.Create.Request): Rpc.BlockDataview.View.Create.Response

    @Throws(Exception::class)
    fun blockDataViewViewUpdate(request: Rpc.BlockDataview.View.Update.Request): Rpc.BlockDataview.View.Update.Response

    @Throws(Exception::class)
    fun blockDataViewViewDelete(request: Rpc.BlockDataview.View.Delete.Request): Rpc.BlockDataview.View.Delete.Response

    @Throws(Exception::class)
    fun blockDataViewRelationAdd(request: Rpc.BlockDataview.Relation.Add.Request): Rpc.BlockDataview.Relation.Add.Response

    @Throws(Exception::class)
    fun blockDataViewRelationDelete(request: Rpc.BlockDataview.Relation.Delete.Request): Rpc.BlockDataview.Relation.Delete.Response

    @Throws(Exception::class)
    fun blockDataViewViewSetPosition(request: Rpc.BlockDataview.View.SetPosition.Request): Rpc.BlockDataview.View.SetPosition.Response

    @Throws(Exception::class)
    fun blockDataViewSetSource(request: Rpc.BlockDataview.SetSource.Request): Rpc.BlockDataview.SetSource.Response

    @Throws(Exception::class)
    fun blockDataViewAddFilter(request: Rpc.BlockDataview.Filter.Add.Request): Rpc.BlockDataview.Filter.Add.Response

    @Throws(Exception::class)
    fun blockDataViewRemoveFilter(request: Rpc.BlockDataview.Filter.Remove.Request): Rpc.BlockDataview.Filter.Remove.Response

    @Throws(Exception::class)
    fun blockDataViewReplaceFilter(request: Rpc.BlockDataview.Filter.Replace.Request): Rpc.BlockDataview.Filter.Replace.Response

    @Throws(Exception::class)
    fun blockDataViewSortFilter(request: Rpc.BlockDataview.Filter.Sort.Request): Rpc.BlockDataview.Filter.Sort.Response

    @Throws(Exception::class)
    fun blockDataViewAddSort(request: Rpc.BlockDataview.Sort.Add.Request): Rpc.BlockDataview.Sort.Add.Response

    @Throws(Exception::class)
    fun blockDataViewRemoveSort(request: Rpc.BlockDataview.Sort.Remove.Request): Rpc.BlockDataview.Sort.Remove.Response

    @Throws(Exception::class)
    fun blockDataViewReplaceSort(request: Rpc.BlockDataview.Sort.Replace.Request): Rpc.BlockDataview.Sort.Replace.Response

    @Throws(Exception::class)
    fun blockDataViewSortSort(request: Rpc.BlockDataview.Sort.SSort.Request): Rpc.BlockDataview.Sort.SSort.Response

    @Deprecated("To be deleted. Use blockDataViewReplaceViewRelation")
    @Throws(Exception::class)
    fun blockDataViewAddViewRelation(request: Rpc.BlockDataview.ViewRelation.Add.Request): Rpc.BlockDataview.ViewRelation.Add.Response

    @Throws(Exception::class)
    fun blockDataViewRemoveViewRelation(request: Rpc.BlockDataview.ViewRelation.Remove.Request): Rpc.BlockDataview.ViewRelation.Remove.Response

    @Throws(Exception::class)
    fun blockDataViewReplaceViewRelation(request: Rpc.BlockDataview.ViewRelation.Replace.Request): Rpc.BlockDataview.ViewRelation.Replace.Response

    @Throws(Exception::class)
    fun blockDataViewSortViewRelation(request: Rpc.BlockDataview.ViewRelation.Sort.Request): Rpc.BlockDataview.ViewRelation.Sort.Response

    @Throws(Exception::class)
    fun blockDataViewSetActiveView(request: Rpc.BlockDataview.View.SetActive.Request): Rpc.BlockDataview.View.SetActive.Response

    //endregion

    //region TEXT BLOCK commands

    @Throws(Exception::class)
    fun blockTextSetText(request: Rpc.BlockText.SetText.Request): Rpc.BlockText.SetText.Response

    @Throws(Exception::class)
    fun blockTextSetChecked(request: Rpc.BlockText.SetChecked.Request): Rpc.BlockText.SetChecked.Response

    @Throws(Exception::class)
    fun blockTextListSetColor(request: Rpc.BlockText.ListSetColor.Request): Rpc.BlockText.ListSetColor.Response

    @Throws(Exception::class)
    fun blockTextListSetMark(request: Rpc.BlockText.ListSetMark.Request): Rpc.BlockText.ListSetMark.Response

    @Throws(Exception::class)
    fun blockTextListSetStyle(request: Rpc.BlockText.ListSetStyle.Request): Rpc.BlockText.ListSetStyle.Response

    @Throws(Exception::class)
    fun blockTextSetIcon(request: Rpc.BlockText.SetIcon.Request): Rpc.BlockText.SetIcon.Response

    //endregion

    //region LINK BLOCK commands

    @Throws(Exception::class)
    fun blockLinkListSetAppearance(request: Rpc.BlockLink.ListSetAppearance.Request): Rpc.BlockLink.ListSetAppearance.Response

    //endregion

    //region SIMPLE TABLE commands

    @Throws(Exception::class)
    fun createTable(request: Rpc.BlockTable.Create.Request) : Rpc.BlockTable.Create.Response

    @Throws(Exception::class)
    fun blockTableRowListFill(request: Rpc.BlockTable.RowListFill.Request): Rpc.BlockTable.RowListFill.Response

    @Throws(Exception::class)
    fun blockTableColumnListFill(request: Rpc.BlockTable.ColumnListFill.Request): Rpc.BlockTable.ColumnListFill.Response

    @Throws(Exception::class)
    fun blockTableRowCreate(request: Rpc.BlockTable.RowCreate.Request): Rpc.BlockTable.RowCreate.Response

    @Throws(Exception::class)
    fun blockTableRowSetHeader(request: Rpc.BlockTable.RowSetHeader.Request): Rpc.BlockTable.RowSetHeader.Response

    @Throws(Exception::class)
    fun blockTableColumnCreate(request: Rpc.BlockTable.ColumnCreate.Request): Rpc.BlockTable.ColumnCreate.Response

    @Throws(Exception::class)
    fun blockTableColumnDelete(request: Rpc.BlockTable.ColumnDelete.Request): Rpc.BlockTable.ColumnDelete.Response

    @Throws(Exception::class)
    fun blockTableRowDelete(request: Rpc.BlockTable.RowDelete.Request): Rpc.BlockTable.RowDelete.Response

    @Throws(Exception::class)
    fun blockTableColumnDuplicate(request: Rpc.BlockTable.ColumnDuplicate.Request): Rpc.BlockTable.ColumnDuplicate.Response

    @Throws(Exception::class)
    fun blockTableRowDuplicate(request: Rpc.BlockTable.RowDuplicate.Request): Rpc.BlockTable.RowDuplicate.Response

    @Throws(Exception::class)
    fun blockTableSort(request: Rpc.BlockTable.Sort.Request): Rpc.BlockTable.Sort.Response

    @Throws(Exception::class)
    fun blockTableExpand(request: Rpc.BlockTable.Expand.Request): Rpc.BlockTable.Expand.Response

    @Throws(Exception::class)
    fun blockTableColumnMove(request: Rpc.BlockTable.ColumnMove.Request): Rpc.BlockTable.ColumnMove.Response

    //endregion

    //region DEBUG commands
    @Throws(Exception::class)
    fun debugSpaceSummary(request: Rpc.Debug.SpaceSummary.Request): Rpc.Debug.SpaceSummary.Response

    @Throws(Exception::class)
    fun debugObject(request: Rpc.Debug.Tree.Request): Rpc.Debug.Tree.Response

    @Throws(Exception::class)
    fun debugExportLocalStore(request: Rpc.Debug.ExportLocalstore.Request): Rpc.Debug.ExportLocalstore.Response

    @Throws(Exception::class)
    fun debugSubscriptions(request: Rpc.Debug.Subscriptions.Request): Rpc.Debug.Subscriptions.Response

    @Throws(Exception::class)
    fun debugStackGoroutines(request: Rpc.Debug.StackGoroutines.Request): Rpc.Debug.StackGoroutines.Response

    //endregion

    //region WIDGETS commands

    @Throws(Exception::class)
    fun blockCreateWidget(request: Rpc.Block.CreateWidget.Request): Rpc.Block.CreateWidget.Response

    @Throws(Exception::class)
    fun blockWidgetSetViewId(request: Rpc.BlockWidget.SetViewId.Request) : Rpc.BlockWidget.SetViewId.Response

    //endregion

    //region WORKSPACE

    fun spaceDelete(request: Rpc.Space.Delete.Request): Rpc.Space.Delete.Response

    @Throws(Exception::class)
    fun workspaceCreate(request: Rpc.Workspace.Create.Request): Rpc.Workspace.Create.Response

    @Throws(Exception::class)
    fun workspaceOpen(request: Rpc.Workspace.Open.Request): Rpc.Workspace.Open.Response

    fun workspaceSetInfo(request: Rpc.Workspace.SetInfo.Request): Rpc.Workspace.SetInfo.Response

    @Throws(Exception::class)
    fun workspaceObjectListAdd(request: Rpc.Workspace.Object.ListAdd.Request): Rpc.Workspace.Object.ListAdd.Response

    @Throws(Exception::class)
    fun workspaceObjectAdd(request: Rpc.Workspace.Object.Add.Request): Rpc.Workspace.Object.Add.Response

    @Throws(Exception::class)
    fun workspaceObjectListRemove(request: Rpc.Workspace.Object.ListRemove.Request): Rpc.Workspace.Object.ListRemove.Response

    //endregion

    //region NODE
    @Throws(Exception::class)
    fun nodeUsageInfo(request: Rpc.File.NodeUsage.Request): Rpc.File.NodeUsage.Response
    //endregion

    //region MULTIPLAYER

    @Throws(Exception::class)
    fun spaceInviteGenerate(request: Rpc.Space.InviteGenerate.Request): Rpc.Space.InviteGenerate.Response

    @Throws(Exception::class)
    fun spaceInviteGetCurrent(request: Rpc.Space.InviteGetCurrent.Request): Rpc.Space.InviteGetCurrent.Response

    @Throws(Exception::class)
    fun spaceInviteRevoke(request: Rpc.Space.InviteRevoke.Request): Rpc.Space.InviteRevoke.Response

    @Throws(Exception::class)
    fun spaceInviteView(request: Rpc.Space.InviteView.Request): Rpc.Space.InviteView.Response

    @Throws(Exception::class)
    fun spaceJoin(request: Rpc.Space.Join.Request): Rpc.Space.Join.Response

    @Throws(Exception::class)
    fun spaceJoinCancel(request: Rpc.Space.JoinCancel.Request): Rpc.Space.JoinCancel.Response

    @Throws(Exception::class)
    fun spaceLeaveApprove(request: Rpc.Space.LeaveApprove.Request): Rpc.Space.LeaveApprove.Response

    @Throws(Exception::class)
    fun spaceMakeShareable(request: Rpc.Space.MakeShareable.Request): Rpc.Space.MakeShareable.Response

    @Throws(Exception::class)
    fun spaceParticipantPermissionsChange(request: Rpc.Space.ParticipantPermissionsChange.Request): Rpc.Space.ParticipantPermissionsChange.Response

    @Throws(Exception::class)
    fun spaceParticipantRemove(request: Rpc.Space.ParticipantRemove.Request): Rpc.Space.ParticipantRemove.Response

    @Throws(Exception::class)
    fun spaceRequestApprove(request: Rpc.Space.RequestApprove.Request): Rpc.Space.RequestApprove.Response

    @Throws(Exception::class)
    fun spaceRequestDecline(request: Rpc.Space.RequestDecline.Request): Rpc.Space.RequestDecline.Response

    @Throws(Exception::class)
    fun spaceStopSharing(request: Rpc.Space.StopSharing.Request): Rpc.Space.StopSharing.Response

    //endregion

    //region GALLERY EXPERIENCE
    @Throws(Exception::class)
    fun downloadManifest(request: Rpc.Gallery.DownloadManifest.Request): Rpc.Gallery.DownloadManifest.Response

    @Throws(Exception::class)
    fun objectImportExperience(request: Rpc.Object.ImportExperience.Request): Rpc.Object.ImportExperience.Response
    //endregion

    //region NOTIFICATIONS

    @Throws(Exception::class)
    fun notificationReply(request: Rpc.Notification.Reply.Request):  Rpc.Notification.Reply.Response

    //endregion

    //region MEMBERSHIP
    @Throws(Exception::class)
    fun membershipStatus(request: Rpc.Membership.GetStatus.Request): Rpc.Membership.GetStatus.Response

    @Throws(Exception::class)
    fun membershipIsNameValid(request: Rpc.Membership.IsNameValid.Request): Rpc.Membership.IsNameValid.Response

    @Throws(Exception::class)
    fun membershipRegisterPaymentRequest(request: Rpc.Membership.RegisterPaymentRequest.Request): Rpc.Membership.RegisterPaymentRequest.Response

    @Throws(Exception::class)
    fun membershipGetPortalLinkUrl(request: Rpc.Membership.GetPortalLinkUrl.Request): Rpc.Membership.GetPortalLinkUrl.Response

    @Throws(Exception::class)
    fun membershipFinalize(request: Rpc.Membership.Finalize.Request): Rpc.Membership.Finalize.Response

    @Throws(Exception::class)
    fun membershipGetVerificationEmailStatus(request: Rpc.Membership.GetVerificationEmailStatus.Request): Rpc.Membership.GetVerificationEmailStatus.Response

    @Throws(Exception::class)
    fun membershipGetVerificationEmail(request: Rpc.Membership.GetVerificationEmail.Request): Rpc.Membership.GetVerificationEmail.Response

    @Throws(Exception::class)
    fun membershipVerifyEmailCode(request: Rpc.Membership.VerifyEmailCode.Request): Rpc.Membership.VerifyEmailCode.Response

    @Throws(Exception::class)
    fun membershipGetTiers(request: Rpc.Membership.GetTiers.Request): Rpc.Membership.GetTiers.Response
    //endregion

    //region VERSION HISTORY
    @Throws(Exception::class)
    fun getVersions(request: Rpc.History.GetVersions.Request): Rpc.History.GetVersions.Response

    @Throws(Exception::class)
    fun showVersion(request: Rpc.History.ShowVersion.Request): Rpc.History.ShowVersion.Response

    @Throws(Exception::class)
    fun setVersion(request: Rpc.History.SetVersion.Request): Rpc.History.SetVersion.Response

    @Throws(Exception::class)
    fun diffVersions(request: Rpc.History.DiffVersions.Request): Rpc.History.DiffVersions.Response
    //endregion

    //region CHATS

    fun chatAddMessage(request: Rpc.Chat.AddMessage.Request): Rpc.Chat.AddMessage.Response
    fun chatEditMessage(request:  Rpc.Chat.EditMessageContent.Request): Rpc.Chat.EditMessageContent.Response
    fun chatGetMessages(request: Rpc.Chat.GetMessages.Request): Rpc.Chat.GetMessages.Response
    fun chatDeleteMessage(request: Rpc.Chat.DeleteMessage.Request): Rpc.Chat.DeleteMessage.Response
    fun chatSubscribeLastMessages(request: Rpc.Chat.SubscribeLastMessages.Request): Rpc.Chat.SubscribeLastMessages.Response
    fun chatToggleMessageReaction(request: Rpc.Chat.ToggleMessageReaction.Request): Rpc.Chat.ToggleMessageReaction.Response
    fun chatUnsubscribe(request: Rpc.Chat.Unsubscribe.Request): Rpc.Chat.Unsubscribe.Response

    //endregion

    @Throws(Exception::class)
    fun debugAccountSelectTrace(request: Rpc.Debug.AccountSelectTrace.Request): Rpc.Debug.AccountSelectTrace.Response
}