package com.anytypeio.anytype.middleware.service

import anytype.Rpc.*

/**
 * Service for interacting with the backend.
 */
interface MiddlewareService {
    @Throws(Exception::class)
    fun configGet(request: Config.Get.Request): Config.Get.Response

    @Throws(Exception::class)
    fun walletCreate(request: Wallet.Create.Request): Wallet.Create.Response

    @Throws(Exception::class)
    fun walletConvert(request: Wallet.Convert.Request): Wallet.Convert.Response

    @Throws(Exception::class)
    fun walletRecover(request: Wallet.Recover.Request): Wallet.Recover.Response

    @Throws(Exception::class)
    fun accountCreate(request: Account.Create.Request): Account.Create.Response

    @Throws(Exception::class)
    fun accountSelect(request: Account.Select.Request): Account.Select.Response

    @Throws(Exception::class)
    fun accountRecover(request: Account.Recover.Request): Account.Recover.Response

    @Throws(Exception::class)
    fun accountStop(request: Account.Stop.Request): Account.Stop.Response

    @Throws(Exception::class)
    fun blockOpen(request: Block.Open.Request): Block.Open.Response

    @Throws(Exception::class)
    fun blockClose(request: Block.Close.Request): Block.Close.Response

    @Throws(Exception::class)
    fun blockCreate(request: Block.Create.Request): Block.Create.Response

    @Throws(Exception::class)
    fun blockCreatePage(request: Block.CreatePage.Request): Block.CreatePage.Response

    @Throws(Exception::class)
    fun blockSetTextText(request: Block.Set.Text.TText.Request): Block.Set.Text.TText.Response

    @Throws(Exception::class)
    fun blockSetTextChecked(request: Block.Set.Text.Checked.Request): Block.Set.Text.Checked.Response

    @Throws(Exception::class)
    fun blockSetTextColor(request: BlockList.Set.Text.Color.Request): BlockList.Set.Text.Color.Response

    @Throws(Exception::class)
    fun blockListSetBackgroundColor(request: BlockList.Set.BackgroundColor.Request): BlockList.Set.BackgroundColor.Response

    @Throws(Exception::class)
    fun blockListSetAlign(request: BlockList.Set.Align.Request): BlockList.Set.Align.Response

    @Throws(Exception::class)
    fun blockListSetTextStyle(request: BlockList.Set.Text.Style.Request): BlockList.Set.Text.Style.Response

    @Throws(Exception::class)
    fun blockListSetDivStyle(request: BlockList.Set.Div.Style.Request): BlockList.Set.Div.Style.Response

    @Throws(Exception::class)
    fun blockListMove(request: BlockList.Move.Request): BlockList.Move.Response

    @Throws(Exception::class)
    fun blockUnlink(request: Block.Unlink.Request): Block.Unlink.Response

    @Throws(Exception::class)
    fun blockMerge(request: Block.Merge.Request): Block.Merge.Response

    @Throws(Exception::class)
    fun blockSplit(request: Block.Split.Request): Block.Split.Response

    @Throws(Exception::class)
    fun blockListDuplicate(request: BlockList.Duplicate.Request): BlockList.Duplicate.Response

    @Throws(Exception::class)
    fun convertChildrenToPages(request: BlockList.ConvertChildrenToPages.Request): BlockList.ConvertChildrenToPages.Response

    @Throws(Exception::class)
    fun blockBookmarkFetch(request: Block.Bookmark.Fetch.Request): Block.Bookmark.Fetch.Response

    @Throws(Exception::class)
    fun blockUpload(request: Block.Upload.Request): Block.Upload.Response

    @Throws(Exception::class)
    fun blockUndo(request: Block.Undo.Request): Block.Undo.Response

    @Throws(Exception::class)
    fun blockRedo(request: Block.Redo.Request): Block.Redo.Response

    @Throws(Exception::class)
    fun blockSetDetails(request: Block.Set.Details.Request): Block.Set.Details.Response

    @Throws(Exception::class)
    fun blockPaste(request: Block.Paste.Request): Block.Paste.Response

    @Throws(Exception::class)
    fun blockCopy(request: Block.Copy.Request): Block.Copy.Response

    @Throws(Exception::class)
    fun uploadFile(request: UploadFile.Request): UploadFile.Response

    @Throws(Exception::class)
    fun objectInfoWithLinks(request: Navigation.GetObjectInfoWithLinks.Request): Navigation.GetObjectInfoWithLinks.Response

    @Throws(Exception::class)
    fun listObjects(request: Navigation.ListObjects.Request): Navigation.ListObjects.Response

    @Throws(Exception::class)
    fun pageCreate(request: Page.Create.Request): Page.Create.Response

    @Throws(Exception::class)
    fun versionGet(request: Version.Get.Request): Version.Get.Response

    @Throws(Exception::class)
    fun blockListSetFields(request: BlockList.Set.Fields.Request): BlockList.Set.Fields.Response

    @Throws(Exception::class)
    fun objectTypeList(request: ObjectType.List.Request): ObjectType.List.Response

    @Throws(Exception::class)
    fun objectTypeCreate(request: ObjectType.Create.Request): ObjectType.Create.Response

    @Throws(Exception::class)
    fun blockCreateSet(request: Block.CreateSet.Request): Block.CreateSet.Response

    @Throws(Exception::class)
    fun blockDataViewActiveSet(request: Block.Dataview.ViewSetActive.Request): Block.Dataview.ViewSetActive.Response

    @Throws(Exception::class)
    fun blockDataViewRelationAdd(request: Block.Dataview.RelationAdd.Request): Block.Dataview.RelationAdd.Response

    @Throws(Exception::class)
    fun blockDataViewRelationDelete(request: Block.Dataview.RelationDelete.Request): Block.Dataview.RelationDelete.Response

    @Throws(Exception::class)
    fun blockDataViewViewUpdate(request: Block.Dataview.ViewUpdate.Request): Block.Dataview.ViewUpdate.Response

    @Throws(Exception::class)
    fun blockDataViewViewDelete(request: Block.Dataview.ViewDelete.Request): Block.Dataview.ViewDelete.Response

    @Throws(Exception::class)
    fun blockDataViewRecordCreate(request: Block.Dataview.RecordCreate.Request): Block.Dataview.RecordCreate.Response

    @Throws(Exception::class)
    fun blockDataViewRecordUpdate(request: Block.Dataview.RecordUpdate.Request): Block.Dataview.RecordUpdate.Response

    @Throws(Exception::class)
    fun blockDataViewViewCreate(request: Block.Dataview.ViewCreate.Request): Block.Dataview.ViewCreate.Response

    @Throws(Exception::class)
    fun blockDataViewRecordRelationOptionAdd(request: Block.Dataview.RecordRelationOptionAdd.Request): Block.Dataview.RecordRelationOptionAdd.Response

    @Throws(Exception::class)
    fun objectRelationOptionAdd(request: Object.RelationOptionAdd.Request): Object.RelationOptionAdd.Response

    @Throws(Exception::class)
    fun objectSearch(request: Object.Search.Request): Object.Search.Response

    @Throws(Exception::class)
    fun objectSearchSubscribe(request: Object.SearchSubscribe.Request): Object.SearchSubscribe.Response

    @Throws(Exception::class)
    fun objectSearchUnsubscribe(request: Object.SearchUnsubscribe.Request): Object.SearchUnsubscribe.Response

    @Throws(Exception::class)
    fun relationListAvailable(request: Object.RelationListAvailable.Request): Object.RelationListAvailable.Response

    @Throws(Exception::class)
    fun objectRelationAdd(request: Object.RelationAdd.Request) : Object.RelationAdd.Response

    @Throws(Exception::class)
    fun objectRelationDelete(request: Object.RelationDelete.Request) : Object.RelationDelete.Response

    @Throws(Exception::class)
    fun debugSync(request: Debug.Sync.Request) : Debug.Sync.Response

    @Throws(Exception::class)
    fun relationSetKey(request: Block.Relation.SetKey.Request) : Block.Relation.SetKey.Response

    @Throws(Exception::class)
    fun blockAddRelation(request: Block.Relation.Add.Request) : Block.Relation.Add.Response

    @Throws(Exception::class)
    fun blockListTurnInto(request: BlockList.TurnInto.Request): BlockList.TurnInto.Response

    @Throws(Exception::class)
    fun blockListSetTextMark(request: BlockList.Set.Text.Mark.Request): BlockList.Set.Text.Mark.Response

    @Throws(Exception::class)
    fun blockSetObjectType(request: Block.ObjectType.Set.Request): Block.ObjectType.Set.Response

    @Throws(Exception::class)
    fun featuredRelationsAdd(request: Object.FeaturedRelation.Add.Request): Object.FeaturedRelation.Add.Response

    @Throws(Exception::class)
    fun featuredRelationsRemove(request: Object.FeaturedRelation.Remove.Request): Object.FeaturedRelation.Remove.Response

    @Throws(Exception::class)
    fun objectSetIsFavorite(request: Object.SetIsFavorite.Request): Object.SetIsFavorite.Response

    @Throws(Exception::class)
    fun objectSetIsArchived(request: Object.SetIsArchived.Request): Object.SetIsArchived.Response

    @Throws(Exception::class)
    fun objectListSetIsArchived(request: ObjectList.Set.IsArchived.Request): ObjectList.Set.IsArchived.Response

    @Throws(Exception::class)
    fun objectListDelete(request: ObjectList.Delete.Request): ObjectList.Delete.Response

    @Throws(Exception::class)
    fun objectSetLayout(request: Object.SetLayout.Request): Object.SetLayout.Response

    @Throws(Exception::class)
    fun exportLocalStore(request: ExportLocalstore.Request): ExportLocalstore.Response

    @Throws(Exception::class)
    fun fileListOffload(request: FileList.Offload.Request): FileList.Offload.Response
}