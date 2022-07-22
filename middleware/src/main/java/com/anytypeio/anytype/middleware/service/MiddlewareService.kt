package com.anytypeio.anytype.middleware.service

import anytype.Rpc

/**
 * Service for interacting with the backend.
 */
interface MiddlewareService {

    //region APP commands

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
    fun objectCreateSet(request: Rpc.Object.CreateSet.Request): Rpc.Object.CreateSet.Response

    @Throws(Exception::class)
    fun objectSearch(request: Rpc.Object.Search.Request): Rpc.Object.Search.Response

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
    fun objectSetIsFavorite(request: Rpc.Object.SetIsFavorite.Request): Rpc.Object.SetIsFavorite.Response

    @Throws(Exception::class)
    fun objectSetIsArchived(request: Rpc.Object.SetIsArchived.Request): Rpc.Object.SetIsArchived.Response

    @Throws(Exception::class)
    fun objectListSetIsArchived(request: Rpc.Object.ListSetIsArchived.Request): Rpc.Object.ListSetIsArchived.Response

    @Throws(Exception::class)
    fun objectListDelete(request: Rpc.Object.ListDelete.Request): Rpc.Object.ListDelete.Response

    @Throws(Exception::class)
    fun objectApplyTemplate(request: Rpc.Object.ApplyTemplate.Request): Rpc.Object.ApplyTemplate.Response

    @Throws(Exception::class)
    fun objectUndo(request: Rpc.Object.Undo.Request): Rpc.Object.Undo.Response

    @Throws(Exception::class)
    fun objectRedo(request: Rpc.Object.Redo.Request): Rpc.Object.Redo.Response

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

    @Throws(Exception::class)
    fun objectRelationOptionAdd(request: Rpc.ObjectRelationOption.Add.Request): Rpc.ObjectRelationOption.Add.Response

    //endregion

    //region OBJECT TYPE commands

    @Throws(Exception::class)
    fun objectTypeCreate(request: Rpc.ObjectType.Create.Request): Rpc.ObjectType.Create.Response

    @Throws(Exception::class)
    fun objectTypeList(request: Rpc.ObjectType.List.Request): Rpc.ObjectType.List.Response

    //endregion

    //region FILES commands

    @Throws(Exception::class)
    fun fileListOffload(request: Rpc.File.ListOffload.Request): Rpc.File.ListOffload.Response

    @Throws(Exception::class)
    fun fileUpload(request: Rpc.File.Upload.Request): Rpc.File.Upload.Response

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

    //endregion

    //region NAVIGATION commands

    @Throws(Exception::class)
    fun navigationGetObjectInfoWithLinks(request: Rpc.Navigation.GetObjectInfoWithLinks.Request): Rpc.Navigation.GetObjectInfoWithLinks.Response

    @Throws(Exception::class)
    fun navigationListObjects(request: Rpc.Navigation.ListObjects.Request): Rpc.Navigation.ListObjects.Response

    //endregion

    //region DATA VIEW commands

    @Throws(Exception::class)
    fun blockDataViewActiveSet(request: Rpc.BlockDataview.View.SetActive.Request): Rpc.BlockDataview.View.SetActive.Response

    @Throws(Exception::class)
    fun blockDataViewViewCreate(request: Rpc.BlockDataview.View.Create.Request): Rpc.BlockDataview.View.Create.Response

    @Throws(Exception::class)
    fun blockDataViewViewUpdate(request: Rpc.BlockDataview.View.Update.Request): Rpc.BlockDataview.View.Update.Response

    @Throws(Exception::class)
    fun blockDataViewViewDelete(request: Rpc.BlockDataview.View.Delete.Request): Rpc.BlockDataview.View.Delete.Response

    @Throws(Exception::class)
    fun blockDataViewRecordCreate(request: Rpc.BlockDataviewRecord.Create.Request): Rpc.BlockDataviewRecord.Create.Response

    @Throws(Exception::class)
    fun blockDataViewRecordUpdate(request: Rpc.BlockDataviewRecord.Update.Request): Rpc.BlockDataviewRecord.Update.Response

    @Throws(Exception::class)
    fun blockDataViewRecordRelationOptionAdd(request: Rpc.BlockDataviewRecord.RelationOption.Add.Request): Rpc.BlockDataviewRecord.RelationOption.Add.Response

    @Throws(Exception::class)
    fun blockDataViewRelationAdd(request: Rpc.BlockDataview.Relation.Add.Request): Rpc.BlockDataview.Relation.Add.Response

    @Throws(Exception::class)
    fun blockDataViewRelationDelete(request: Rpc.BlockDataview.Relation.Delete.Request): Rpc.BlockDataview.Relation.Delete.Response

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

    //region DEBUG commands

    @Throws(Exception::class)
    fun debugSync(request: Rpc.Debug.Sync.Request): Rpc.Debug.Sync.Response

    @Throws(Exception::class)
    fun debugExportLocalStore(request: Rpc.Debug.ExportLocalstore.Request): Rpc.Debug.ExportLocalstore.Response

    //endregion
}