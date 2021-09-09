package com.anytypeio.anytype.middleware.service

import anytype.Rpc.*
import anytype.Rpc.Config
import com.anytypeio.anytype.data.auth.exception.BackwardCompatilityNotSupportedException
import com.anytypeio.anytype.data.auth.exception.UndoRedoExhaustedException
import service.Service

class MiddlewareServiceImplementation : MiddlewareService {

    override fun configGet(request: Config.Get.Request): Config.Get.Response {
        val encoded = Service.configGet(Config.Get.Request.ADAPTER.encode(request))
        val response = Config.Get.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Config.Get.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletCreate(request: Wallet.Create.Request): Wallet.Create.Response {
        val encoded = Service.walletCreate(Wallet.Create.Request.ADAPTER.encode(request))
        val response = Wallet.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Wallet.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletConvert(request: Wallet.Convert.Request): Wallet.Convert.Response {
        val encoded = Service.walletConvert(Wallet.Convert.Request.ADAPTER.encode(request))
        val response = Wallet.Convert.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Wallet.Convert.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletRecover(request: Wallet.Recover.Request): Wallet.Recover.Response {
        val encoded = Service.walletRecover(Wallet.Recover.Request.ADAPTER.encode(request))
        val response = Wallet.Recover.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Wallet.Recover.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountCreate(request: Account.Create.Request): Account.Create.Response {
        val encoded = Service.accountCreate(Account.Create.Request.ADAPTER.encode(request))
        val response = Account.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountSelect(request: Account.Select.Request): Account.Select.Response {
        val encoded = Service.accountSelect(Account.Select.Request.ADAPTER.encode(request))
        val response = Account.Select.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Select.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountRecover(request: Account.Recover.Request): Account.Recover.Response {
        val encoded = Service.accountRecover(Account.Recover.Request.ADAPTER.encode(request))
        val response = Account.Recover.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Recover.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountStop(request: Account.Stop.Request): Account.Stop.Response {
        val encoded = Service.accountStop(Account.Stop.Request.ADAPTER.encode(request))
        val response = Account.Stop.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Stop.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockOpen(request: Block.Open.Request): Block.Open.Response {
        val encoded = Service.blockOpen(Block.Open.Request.ADAPTER.encode(request))
        val response = Block.Open.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Open.Response.Error.Code.NULL) {
            if (error.code == Block.Open.Response.Error.Code.ANYTYPE_NEEDS_UPGRADE)
                throw BackwardCompatilityNotSupportedException()
            else
                throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockClose(request: Block.Close.Request): Block.Close.Response {
        val encoded = Service.blockClose(Block.Close.Request.ADAPTER.encode(request))
        val response = Block.Close.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Close.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCreate(request: Block.Create.Request): Block.Create.Response {
        val encoded = Service.blockCreate(Block.Create.Request.ADAPTER.encode(request))
        val response = Block.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCreatePage(request: Block.CreatePage.Request): Block.CreatePage.Response {
        val encoded = Service.blockCreatePage(Block.CreatePage.Request.ADAPTER.encode(request))
        val response = Block.CreatePage.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.CreatePage.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetTextText(request: Block.Set.Text.TText.Request): Block.Set.Text.TText.Response {
        val encoded = Service.blockSetTextText(Block.Set.Text.TText.Request.ADAPTER.encode(request))
        val response = Block.Set.Text.TText.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Set.Text.TText.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetTextChecked(request: Block.Set.Text.Checked.Request): Block.Set.Text.Checked.Response {
        val encoded = Service.blockSetTextChecked(
            Block.Set.Text.Checked.Request.ADAPTER.encode(request)
        )
        val response = Block.Set.Text.Checked.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Set.Text.Checked.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetTextColor(request: BlockList.Set.Text.Color.Request): BlockList.Set.Text.Color.Response {
        val encoded = Service.blockListSetTextColor(
            BlockList.Set.Text.Color.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Set.Text.Color.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Text.Color.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetBackgroundColor(request: BlockList.Set.BackgroundColor.Request): BlockList.Set.BackgroundColor.Response {
        val encoded = Service.blockListSetBackgroundColor(
            BlockList.Set.BackgroundColor.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Set.BackgroundColor.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.BackgroundColor.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetAlign(request: BlockList.Set.Align.Request): BlockList.Set.Align.Response {
        val encoded = Service.blockListSetAlign(BlockList.Set.Align.Request.ADAPTER.encode(request))
        val response = BlockList.Set.Align.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Align.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetTextStyle(request: BlockList.Set.Text.Style.Request): BlockList.Set.Text.Style.Response {
        val encoded =
            Service.blockListSetTextStyle(BlockList.Set.Text.Style.Request.ADAPTER.encode(request))
        val response = BlockList.Set.Text.Style.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Text.Style.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetDivStyle(request: BlockList.Set.Div.Style.Request): BlockList.Set.Div.Style.Response {
        val encoded =
            Service.blockListSetDivStyle(BlockList.Set.Div.Style.Request.ADAPTER.encode(request))
        val response = BlockList.Set.Div.Style.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Div.Style.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListMove(request: BlockList.Move.Request): BlockList.Move.Response {
        val encoded = Service.blockListMove(BlockList.Move.Request.ADAPTER.encode(request))
        val response = BlockList.Move.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Move.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockUnlink(request: Block.Unlink.Request): Block.Unlink.Response {
        val encoded = Service.blockUnlink(Block.Unlink.Request.ADAPTER.encode(request))
        val response = Block.Unlink.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Unlink.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockMerge(request: Block.Merge.Request): Block.Merge.Response {
        val encoded = Service.blockMerge(Block.Merge.Request.ADAPTER.encode(request))
        val response = Block.Merge.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Merge.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSplit(request: Block.Split.Request): Block.Split.Response {
        val encoded = Service.blockSplit(Block.Split.Request.ADAPTER.encode(request))
        val response = Block.Split.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Split.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListDuplicate(request: BlockList.Duplicate.Request): BlockList.Duplicate.Response {
        val encoded = Service.blockListDuplicate(
            BlockList.Duplicate.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Duplicate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Duplicate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun convertChildrenToPages(request: BlockList.ConvertChildrenToPages.Request): BlockList.ConvertChildrenToPages.Response {
        val encoded = Service.blockListConvertChildrenToPages(
            BlockList.ConvertChildrenToPages.Request.ADAPTER.encode(request)
        )
        val response = BlockList.ConvertChildrenToPages.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.ConvertChildrenToPages.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockBookmarkFetch(request: Block.Bookmark.Fetch.Request): Block.Bookmark.Fetch.Response {
        val encoded = Service.blockBookmarkFetch(
            Block.Bookmark.Fetch.Request.ADAPTER.encode(request)
        )
        val response = Block.Bookmark.Fetch.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Bookmark.Fetch.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockUpload(request: Block.Upload.Request): Block.Upload.Response {
        val encoded = Service.blockUpload(Block.Upload.Request.ADAPTER.encode(request))
        val response = Block.Upload.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Upload.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockUndo(request: Block.Undo.Request): Block.Undo.Response {
        val encoded = Service.blockUndo(Block.Undo.Request.ADAPTER.encode(request))
        val response = Block.Undo.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Undo.Response.Error.Code.NULL) {
            if (error.code == Block.Undo.Response.Error.Code.CAN_NOT_MOVE)
                throw UndoRedoExhaustedException()
            else
                throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockRedo(request: Block.Redo.Request): Block.Redo.Response {
        val encoded = Service.blockRedo(Block.Redo.Request.ADAPTER.encode(request))
        val response = Block.Redo.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Redo.Response.Error.Code.NULL) {
            if (error.code == Block.Redo.Response.Error.Code.CAN_NOT_MOVE)
                throw UndoRedoExhaustedException()
            else
                throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetPageIsArchived(request: BlockList.Set.Page.IsArchived.Request): BlockList.Set.Page.IsArchived.Response {
        val encoded = Service.blockListSetPageIsArchived(
            BlockList.Set.Page.IsArchived.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Set.Page.IsArchived.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Page.IsArchived.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetDetails(request: Block.Set.Details.Request): Block.Set.Details.Response {
        val encoded = Service.blockSetDetails(Block.Set.Details.Request.ADAPTER.encode(request))
        val response = Block.Set.Details.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Set.Details.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockPaste(request: Block.Paste.Request): Block.Paste.Response {
        val encoded = Service.blockPaste(Block.Paste.Request.ADAPTER.encode(request))
        val response = Block.Paste.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Paste.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCopy(request: Block.Copy.Request): Block.Copy.Response {
        val encoded = Service.blockCopy(Block.Copy.Request.ADAPTER.encode(request))
        val response = Block.Copy.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Copy.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun uploadFile(request: UploadFile.Request): UploadFile.Response {
        val encoded = Service.uploadFile(UploadFile.Request.ADAPTER.encode(request))
        val response = UploadFile.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != UploadFile.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectInfoWithLinks(request: Navigation.GetObjectInfoWithLinks.Request): Navigation.GetObjectInfoWithLinks.Response {
        val encoded = Service.navigationGetObjectInfoWithLinks(
            Navigation.GetObjectInfoWithLinks.Request.ADAPTER.encode(request)
        )
        val response = Navigation.GetObjectInfoWithLinks.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Navigation.GetObjectInfoWithLinks.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun listObjects(request: Navigation.ListObjects.Request): Navigation.ListObjects.Response {
        val encoded =
            Service.navigationListObjects(Navigation.ListObjects.Request.ADAPTER.encode(request))
        val response = Navigation.ListObjects.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Navigation.ListObjects.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun pageCreate(request: Page.Create.Request): Page.Create.Response {
        val encoded = Service.pageCreate(Page.Create.Request.ADAPTER.encode(request))
        val response = Page.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Page.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun versionGet(request: Version.Get.Request): Version.Get.Response {
        val encoded = Service.versionGet(Version.Get.Request.ADAPTER.encode(request))
        val response = Version.Get.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Version.Get.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetFields(request: BlockList.Set.Fields.Request): BlockList.Set.Fields.Response {
        val encoded = Service.blockListSetFields(
            BlockList.Set.Fields.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Set.Fields.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Fields.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectTypeList(request: ObjectType.List.Request): ObjectType.List.Response {
        val encoded = Service.objectTypeList(ObjectType.List.Request.ADAPTER.encode(request))
        val response = ObjectType.List.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != ObjectType.List.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectTypeCreate(request: ObjectType.Create.Request): ObjectType.Create.Response {
        val encoded = Service.objectTypeCreate(ObjectType.Create.Request.ADAPTER.encode(request))
        val response = ObjectType.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != ObjectType.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCreateSet(request: Block.CreateSet.Request): Block.CreateSet.Response {
        val encoded = Service.blockCreateSet(Block.CreateSet.Request.ADAPTER.encode(request))
        val response = Block.CreateSet.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.CreateSet.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewActiveSet(request: Block.Dataview.ViewSetActive.Request): Block.Dataview.ViewSetActive.Response {
        val encoded = Service.blockDataviewViewSetActive(
            Block.Dataview.ViewSetActive.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.ViewSetActive.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.ViewSetActive.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRelationAdd(request: Block.Dataview.RelationAdd.Request): Block.Dataview.RelationAdd.Response {
        val encoded = Service.blockDataviewRelationAdd(
            Block.Dataview.RelationAdd.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.RelationAdd.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.RelationAdd.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRecordCreate(request: Block.Dataview.RecordCreate.Request): Block.Dataview.RecordCreate.Response {
        val encoded = Service.blockDataviewRecordCreate(
            Block.Dataview.RecordCreate.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.RecordCreate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.RecordCreate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewViewUpdate(request: Block.Dataview.ViewUpdate.Request): Block.Dataview.ViewUpdate.Response {
        val encoded = Service.blockDataviewViewUpdate(
            Block.Dataview.ViewUpdate.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.ViewUpdate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.ViewUpdate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewViewDelete(request: Block.Dataview.ViewDelete.Request): Block.Dataview.ViewDelete.Response {
        val encoded = Service.blockDataviewViewDelete(
            Block.Dataview.ViewDelete.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.ViewDelete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.ViewDelete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRecordUpdate(request: Block.Dataview.RecordUpdate.Request): Block.Dataview.RecordUpdate.Response {
        val encoded = Service.blockDataviewRecordUpdate(
            Block.Dataview.RecordUpdate.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.RecordUpdate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.RecordUpdate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewViewCreate(request: Block.Dataview.ViewCreate.Request): Block.Dataview.ViewCreate.Response {
        val encoded = Service.blockDataviewViewCreate(
            Block.Dataview.ViewCreate.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.ViewCreate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.ViewCreate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRecordRelationOptionAdd(
        request: Block.Dataview.RecordRelationOptionAdd.Request
    ): Block.Dataview.RecordRelationOptionAdd.Response {
        val encoded = Service.blockDataviewRecordRelationOptionAdd(
            Block.Dataview.RecordRelationOptionAdd.Request.ADAPTER.encode(request)
        )
        val response = Block.Dataview.RecordRelationOptionAdd.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Dataview.RecordRelationOptionAdd.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectRelationOptionAdd(request: Object.RelationOptionAdd.Request): Object.RelationOptionAdd.Response {
        val encoded = Service.objectRelationOptionAdd(
            Object.RelationOptionAdd.Request.ADAPTER.encode(request)
        )
        val response = Object.RelationOptionAdd.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.RelationOptionAdd.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSearch(request: Object.Search.Request): Object.Search.Response {
        val encoded = Service.objectSearch(Object.Search.Request.ADAPTER.encode(request))
        val response = Object.Search.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.Search.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun relationListAvailable(request: Object.RelationListAvailable.Request): Object.RelationListAvailable.Response {
        val encoded = Service.objectRelationListAvailable(
            Object.RelationListAvailable.Request.ADAPTER.encode(request)
        )
        val response = Object.RelationListAvailable.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.RelationListAvailable.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectRelationAdd(request: Object.RelationAdd.Request): Object.RelationAdd.Response {
        val encoded = Service.objectRelationAdd(
            Object.RelationAdd.Request.ADAPTER.encode(request)
        )
        val response = Object.RelationAdd.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.RelationAdd.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectRelationDelete(request: Object.RelationDelete.Request): Object.RelationDelete.Response {
        val encoded = Service.objectRelationDelete(
            Object.RelationDelete.Request.ADAPTER.encode(request)
        )
        val response = Object.RelationDelete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.RelationDelete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockAddRelation(request: Block.Relation.Add.Request): Block.Relation.Add.Response {
        val encoded = Service.blockRelationAdd(
            Block.Relation.Add.Request.ADAPTER.encode(request)
        )
        val response = Block.Relation.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Relation.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugSync(request: Debug.Sync.Request): Debug.Sync.Response {
        val encoded = Service.debugSync(Debug.Sync.Request.ADAPTER.encode(request))
        val response = Debug.Sync.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Debug.Sync.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun relationSetKey(request: Block.Relation.SetKey.Request): Block.Relation.SetKey.Response {
        val encoded =
            Service.blockRelationSetKey(Block.Relation.SetKey.Request.ADAPTER.encode(request))
        val response = Block.Relation.SetKey.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Relation.SetKey.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListTurnInto(request: BlockList.TurnInto.Request): BlockList.TurnInto.Response {
        val encoded = Service.blockListTurnInto(
            BlockList.TurnInto.Request.ADAPTER.encode(request)
        )
        val response = BlockList.TurnInto.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.TurnInto.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetTextMark(request: BlockList.Set.Text.Mark.Request): BlockList.Set.Text.Mark.Response {
        val encoded = Service.blockListSetTextMark(
            BlockList.Set.Text.Mark.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Set.Text.Mark.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Text.Mark.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetObjectType(request: Block.ObjectType.Set.Request): Block.ObjectType.Set.Response {
        val encoded = Service.blockObjectTypeSet(
            Block.ObjectType.Set.Request.ADAPTER.encode(request)
        )
        val response = Block.ObjectType.Set.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.ObjectType.Set.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun featuredRelationsAdd(request: Object.FeaturedRelation.Add.Request): Object.FeaturedRelation.Add.Response {
        val encoded = Service.objectFeaturedRelationAdd(
            Object.FeaturedRelation.Add.Request.ADAPTER.encode(request)
        )
        val response = Object.FeaturedRelation.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.FeaturedRelation.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun featuredRelationsRemove(request: Object.FeaturedRelation.Remove.Request): Object.FeaturedRelation.Remove.Response {
        val encoded = Service.objectFeaturedRelationRemove(
            Object.FeaturedRelation.Remove.Request.ADAPTER.encode(request)
        )
        val response = Object.FeaturedRelation.Remove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.FeaturedRelation.Remove.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun setIsFavorite(request: Object.SetIsFavorite.Request): Object.SetIsFavorite.Response {
        val encoded = Service.objectSetIsFavorite(
            Object.SetIsFavorite.Request.ADAPTER.encode(request)
        )
        val response = Object.SetIsFavorite.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Object.SetIsFavorite.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }
}