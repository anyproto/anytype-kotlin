package com.anytypeio.anytype.middleware.service

import anytype.Rpc
import com.anytypeio.anytype.core_models.exceptions.AccountIsDeletedException
import com.anytypeio.anytype.core_models.exceptions.CreateAccountException
import com.anytypeio.anytype.core_models.exceptions.MigrationNeededException
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.data.auth.exception.BackwardCompatilityNotSupportedException
import com.anytypeio.anytype.data.auth.exception.NotFoundObjectException
import com.anytypeio.anytype.data.auth.exception.UndoRedoExhaustedException
import com.anytypeio.anytype.middleware.BuildConfig
import javax.inject.Inject
import service.Service

class MiddlewareServiceImplementation @Inject constructor(
    featureToggles: FeatureToggles
) : MiddlewareService {

    init {
        if (!featureToggles.isLogFromMiddlewareLibrary) {
            Service.setEnv("ANYTYPE_LOG_LEVEL", "*=fatal;anytype*=error")
        }
    }

    init {
        // Comment these lines if you want to have more verbose go logs.
        if (BuildConfig.DEBUG) {
            Service.setEnv("ANYTYPE_LOG_LEVEL", "*=fatal;anytype*=error")
        }
    }

    override fun accountCreate(request: Rpc.Account.Create.Request): Rpc.Account.Create.Response {
        val encoded = Service.accountCreate(Rpc.Account.Create.Request.ADAPTER.encode(request))
        val response = Rpc.Account.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.Create.Response.Error.Code.NULL) {
            when (error.code) {
                Rpc.Account.Create.Response.Error.Code.NET_OFFLINE -> {
                    throw CreateAccountException.OfflineDevice
                }
                Rpc.Account.Create.Response.Error.Code.BAD_INVITE_CODE -> {
                    throw CreateAccountException.BadInviteCode
                }
                Rpc.Account.Create.Response.Error.Code.NET_ERROR -> {
                    throw CreateAccountException.NetworkError
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun accountDelete(request: Rpc.Account.Delete.Request): Rpc.Account.Delete.Response {
        val encoded = Service.accountDelete(Rpc.Account.Delete.Request.ADAPTER.encode(request))
        val response = Rpc.Account.Delete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.Delete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountRecover(request: Rpc.Account.Recover.Request): Rpc.Account.Recover.Response {
        val encoded = Service.accountRecover(Rpc.Account.Recover.Request.ADAPTER.encode(request))
        val response = Rpc.Account.Recover.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.Recover.Response.Error.Code.NULL) {
            when (error.code) {
                else -> {
                    throw Exception(error.description)
                }
            }
        } else {
            return response
        }
    }

    override fun accountSelect(request: Rpc.Account.Select.Request): Rpc.Account.Select.Response {
        val encoded = Service.accountSelect(Rpc.Account.Select.Request.ADAPTER.encode(request))
        val response = Rpc.Account.Select.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.Select.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Account.Select.Response.Error.Code.FAILED_TO_FIND_ACCOUNT_INFO -> {
                    throw MigrationNeededException()
                }
                Rpc.Account.Select.Response.Error.Code.ACCOUNT_IS_DELETED -> {
                    throw AccountIsDeletedException()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun accountStop(request: Rpc.Account.Stop.Request): Rpc.Account.Stop.Response {
        val encoded = Service.accountStop(Rpc.Account.Stop.Request.ADAPTER.encode(request))
        val response = Rpc.Account.Stop.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.Stop.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockBookmarkCreateAndFetch(request: Rpc.BlockBookmark.CreateAndFetch.Request): Rpc.BlockBookmark.CreateAndFetch.Response {
        val encoded = Service.blockBookmarkCreateAndFetch(
            Rpc.BlockBookmark.CreateAndFetch.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockBookmark.CreateAndFetch.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockBookmark.CreateAndFetch.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockBookmarkFetch(request: Rpc.BlockBookmark.Fetch.Request): Rpc.BlockBookmark.Fetch.Response {
        val encoded = Service.blockBookmarkFetch(
            Rpc.BlockBookmark.Fetch.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockBookmark.Fetch.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockBookmark.Fetch.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCopy(request: Rpc.Block.Copy.Request): Rpc.Block.Copy.Response {
        val encoded = Service.blockCopy(Rpc.Block.Copy.Request.ADAPTER.encode(request))
        val response = Rpc.Block.Copy.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.Copy.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCreate(request: Rpc.Block.Create.Request): Rpc.Block.Create.Response {
        val encoded = Service.blockCreate(Rpc.Block.Create.Request.ADAPTER.encode(request))
        val response = Rpc.Block.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCreateWidget(request: Rpc.Block.CreateWidget.Request): Rpc.Block.CreateWidget.Response {
        val encoded = Service.blockCreateWidget(
            Rpc.Block.CreateWidget.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Block.CreateWidget.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.CreateWidget.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewActiveSet(request: Rpc.BlockDataview.View.SetActive.Request): Rpc.BlockDataview.View.SetActive.Response {
        val encoded = Service.blockDataviewViewSetActive(
            Rpc.BlockDataview.View.SetActive.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.View.SetActive.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.View.SetActive.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    //todo relations refactoring
//    override fun blockDataViewRecordCreate(request: Rpc.BlockDataviewRecord.Create.Request): Rpc.BlockDataviewRecord.Create.Response {
//        val encoded = Service.blockDataviewRecordCreate(
//            Rpc.BlockDataviewRecord.Create.Request.ADAPTER.encode(request)
//        )
//        val response = Rpc.BlockDataviewRecord.Create.Response.ADAPTER.decode(encoded)
//        val error = response.error
//        if (error != null && error.code != Rpc.BlockDataviewRecord.Create.Response.Error.Code.NULL) {
//            throw Exception(error.description)
//        } else {
//            return response
//        }
//    }
//
//    override fun blockDataViewRecordRelationOptionAdd(
//        request: Rpc.BlockDataviewRecord.RelationOption.Add.Request
//    ): Rpc.BlockDataviewRecord.RelationOption.Add.Response {
//        val encoded = Service.blockDataviewRecordRelationOptionAdd(
//            Rpc.BlockDataviewRecord.RelationOption.Add.Request.ADAPTER.encode(request)
//        )
//        val response = Rpc.BlockDataviewRecord.RelationOption.Add.Response.ADAPTER.decode(encoded)
//        val error = response.error
//        if (error != null && error.code != Rpc.BlockDataviewRecord.RelationOption.Add.Response.Error.Code.NULL) {
//            throw Exception(error.description)
//        } else {
//            return response
//        }
//    }
//
//    override fun blockDataViewRecordUpdate(request: Rpc.BlockDataviewRecord.Update.Request): Rpc.BlockDataviewRecord.Update.Response {
//        val encoded = Service.blockDataviewRecordUpdate(
//            Rpc.BlockDataviewRecord.Update.Request.ADAPTER.encode(request)
//        )
//        val response = Rpc.BlockDataviewRecord.Update.Response.ADAPTER.decode(encoded)
//        val error = response.error
//        if (error != null && error.code != Rpc.BlockDataviewRecord.Update.Response.Error.Code.NULL) {
//            throw Exception(error.description)
//        } else {
//            return response
//        }
//    }

    override fun blockDataViewRelationAdd(request: Rpc.BlockDataview.Relation.Add.Request): Rpc.BlockDataview.Relation.Add.Response {
        val encoded = Service.blockDataviewRelationAdd(
            Rpc.BlockDataview.Relation.Add.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Relation.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Relation.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRelationDelete(request: Rpc.BlockDataview.Relation.Delete.Request): Rpc.BlockDataview.Relation.Delete.Response {
        val encoded = Service.blockDataviewRelationDelete(
            Rpc.BlockDataview.Relation.Delete.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Relation.Delete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Relation.Delete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewViewCreate(request: Rpc.BlockDataview.View.Create.Request): Rpc.BlockDataview.View.Create.Response {
        val encoded = Service.blockDataviewViewCreate(
            Rpc.BlockDataview.View.Create.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.View.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.View.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewViewDelete(request: Rpc.BlockDataview.View.Delete.Request): Rpc.BlockDataview.View.Delete.Response {
        val encoded = Service.blockDataviewViewDelete(
            Rpc.BlockDataview.View.Delete.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.View.Delete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.View.Delete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewViewUpdate(request: Rpc.BlockDataview.View.Update.Request): Rpc.BlockDataview.View.Update.Response {
        val encoded = Service.blockDataviewViewUpdate(
            Rpc.BlockDataview.View.Update.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.View.Update.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.View.Update.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockLinkCreateWithObject(request: Rpc.BlockLink.CreateWithObject.Request): Rpc.BlockLink.CreateWithObject.Response {
        val encoded = Service.blockLinkCreateWithObject(
            Rpc.BlockLink.CreateWithObject.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockLink.CreateWithObject.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockLink.CreateWithObject.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockLinkListSetAppearance(
        request: Rpc.BlockLink.ListSetAppearance.Request
    ): Rpc.BlockLink.ListSetAppearance.Response {
        val encoded = Service.blockLinkListSetAppearance(
            Rpc.BlockLink.ListSetAppearance.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockLink.ListSetAppearance.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockLink.ListSetAppearance.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListDelete(request: Rpc.Block.ListDelete.Request): Rpc.Block.ListDelete.Response {
        val encoded = Service.blockListDelete(Rpc.Block.ListDelete.Request.ADAPTER.encode(request))
        val response = Rpc.Block.ListDelete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListDelete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListDuplicate(request: Rpc.Block.ListDuplicate.Request): Rpc.Block.ListDuplicate.Response {
        val encoded = Service.blockListDuplicate(
            Rpc.Block.ListDuplicate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Block.ListDuplicate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListDuplicate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListMoveToExistingObject(request: Rpc.Block.ListMoveToExistingObject.Request): Rpc.Block.ListMoveToExistingObject.Response {
        val encoded = Service.blockListMoveToExistingObject(
            Rpc.Block.ListMoveToExistingObject.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Block.ListMoveToExistingObject.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListMoveToExistingObject.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListMoveToNewObject(request: Rpc.Block.ListMoveToNewObject.Request): Rpc.Block.ListMoveToNewObject.Response {
        val encoded = Service.blockListMoveToNewObject(
            Rpc.Block.ListMoveToNewObject.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Block.ListMoveToNewObject.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListMoveToNewObject.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetAlign(request: Rpc.Block.ListSetAlign.Request): Rpc.Block.ListSetAlign.Response {
        val encoded =
            Service.blockListSetAlign(Rpc.Block.ListSetAlign.Request.ADAPTER.encode(request))
        val response = Rpc.Block.ListSetAlign.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListSetAlign.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetBackgroundColor(request: Rpc.Block.ListSetBackgroundColor.Request): Rpc.Block.ListSetBackgroundColor.Response {
        val encoded = Service.blockListSetBackgroundColor(
            Rpc.Block.ListSetBackgroundColor.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Block.ListSetBackgroundColor.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListSetBackgroundColor.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetDivStyle(request: Rpc.BlockDiv.ListSetStyle.Request): Rpc.BlockDiv.ListSetStyle.Response {
        val encoded =
            Service.blockDivListSetStyle(Rpc.BlockDiv.ListSetStyle.Request.ADAPTER.encode(request))
        val response = Rpc.BlockDiv.ListSetStyle.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDiv.ListSetStyle.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetFields(request: Rpc.Block.ListSetFields.Request): Rpc.Block.ListSetFields.Response {
        val encoded = Service.blockListSetFields(
            Rpc.Block.ListSetFields.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Block.ListSetFields.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListSetFields.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListTurnInto(request: Rpc.Block.ListTurnInto.Request): Rpc.Block.ListTurnInto.Response {
        val encoded = Service.blockListTurnInto(
            Rpc.Block.ListTurnInto.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Block.ListTurnInto.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.ListTurnInto.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockMerge(request: Rpc.Block.Merge.Request): Rpc.Block.Merge.Response {
        val encoded = Service.blockMerge(Rpc.Block.Merge.Request.ADAPTER.encode(request))
        val response = Rpc.Block.Merge.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.Merge.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockPaste(request: Rpc.Block.Paste.Request): Rpc.Block.Paste.Response {
        val encoded = Service.blockPaste(Rpc.Block.Paste.Request.ADAPTER.encode(request))
        val response = Rpc.Block.Paste.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.Paste.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockRelationAdd(request: Rpc.BlockRelation.Add.Request): Rpc.BlockRelation.Add.Response {
        val encoded = Service.blockRelationAdd(
            Rpc.BlockRelation.Add.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockRelation.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockRelation.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockRelationSetKey(request: Rpc.BlockRelation.SetKey.Request): Rpc.BlockRelation.SetKey.Response {
        val encoded =
            Service.blockRelationSetKey(Rpc.BlockRelation.SetKey.Request.ADAPTER.encode(request))
        val response = Rpc.BlockRelation.SetKey.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockRelation.SetKey.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListClearContent(request: Rpc.BlockText.ListClearContent.Request): Rpc.BlockText.ListClearContent.Response {
        val encoded = Service.blockTextListClearContent(
            Rpc.BlockText.ListClearContent.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockText.ListClearContent.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.ListClearContent.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSplit(request: Rpc.Block.Split.Request): Rpc.Block.Split.Response {
        val encoded = Service.blockSplit(Rpc.Block.Split.Request.ADAPTER.encode(request))
        val response = Rpc.Block.Split.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.Split.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTextListSetColor(request: Rpc.BlockText.ListSetColor.Request): Rpc.BlockText.ListSetColor.Response {
        val encoded = Service.blockTextListSetColor(
            Rpc.BlockText.ListSetColor.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockText.ListSetColor.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.ListSetColor.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTextListSetMark(request: Rpc.BlockText.ListSetMark.Request): Rpc.BlockText.ListSetMark.Response {
        val encoded = Service.blockTextListSetMark(
            Rpc.BlockText.ListSetMark.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockText.ListSetMark.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.ListSetMark.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTextListSetStyle(request: Rpc.BlockText.ListSetStyle.Request): Rpc.BlockText.ListSetStyle.Response {
        val encoded =
            Service.blockTextListSetStyle(Rpc.BlockText.ListSetStyle.Request.ADAPTER.encode(request))
        val response = Rpc.BlockText.ListSetStyle.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.ListSetStyle.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTextSetIcon(request: Rpc.BlockText.SetIcon.Request): Rpc.BlockText.SetIcon.Response {
        val encoded =
            Service.blockTextSetIcon(Rpc.BlockText.SetIcon.Request.ADAPTER.encode(request))
        val response = Rpc.BlockText.SetIcon.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.SetIcon.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTextSetChecked(request: Rpc.BlockText.SetChecked.Request): Rpc.BlockText.SetChecked.Response {
        val encoded = Service.blockTextSetChecked(
            Rpc.BlockText.SetChecked.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockText.SetChecked.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.SetChecked.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTextSetText(request: Rpc.BlockText.SetText.Request): Rpc.BlockText.SetText.Response {
        val encoded =
            Service.blockTextSetText(Rpc.BlockText.SetText.Request.ADAPTER.encode(request))
        val response = Rpc.BlockText.SetText.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.SetText.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockUpload(request: Rpc.Block.Upload.Request): Rpc.Block.Upload.Response {
        val encoded = Service.blockUpload(Rpc.Block.Upload.Request.ADAPTER.encode(request))
        val response = Rpc.Block.Upload.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Block.Upload.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugExportLocalStore(request: Rpc.Debug.ExportLocalstore.Request): Rpc.Debug.ExportLocalstore.Response {
        val encoded = Service.debugExportLocalstore(
            Rpc.Debug.ExportLocalstore.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Debug.ExportLocalstore.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.ExportLocalstore.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugSpace(request: Rpc.Debug.SpaceSummary.Request): Rpc.Debug.SpaceSummary.Response {
        val encoded = Service.debugSpaceSummary(Rpc.Debug.SpaceSummary.Request.ADAPTER.encode(request))
        val response = Rpc.Debug.SpaceSummary.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.SpaceSummary.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugObject(request: Rpc.Debug.Tree.Request): Rpc.Debug.Tree.Response {
        val encoded = Service.debugTree(Rpc.Debug.Tree.Request.ADAPTER.encode(request))
        val response = Rpc.Debug.Tree.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.Tree.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun fileListOffload(request: Rpc.File.ListOffload.Request): Rpc.File.ListOffload.Response {
        val encoded = Service.fileListOffload(
            Rpc.File.ListOffload.Request.ADAPTER.encode(request)
        )
        val response = Rpc.File.ListOffload.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.File.ListOffload.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun fileUpload(request: Rpc.File.Upload.Request): Rpc.File.Upload.Response {
        val encoded = Service.fileUpload(Rpc.File.Upload.Request.ADAPTER.encode(request))
        val response = Rpc.File.Upload.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.File.Upload.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun fileDownload(request: Rpc.File.Download.Request): Rpc.File.Download.Response {
        val encoded = Service.fileDownload(Rpc.File.Download.Request.ADAPTER.encode(request))
        val response = Rpc.File.Download.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.File.Download.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun navigationListObjects(request: Rpc.Navigation.ListObjects.Request): Rpc.Navigation.ListObjects.Response {
        val encoded =
            Service.navigationListObjects(Rpc.Navigation.ListObjects.Request.ADAPTER.encode(request))
        val response = Rpc.Navigation.ListObjects.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Navigation.ListObjects.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectApplyTemplate(request: Rpc.Object.ApplyTemplate.Request): Rpc.Object.ApplyTemplate.Response {
        val encoded = Service.objectApplyTemplate(
            Rpc.Object.ApplyTemplate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.ApplyTemplate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ApplyTemplate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectClose(request: Rpc.Object.Close.Request): Rpc.Object.Close.Response {
        val encoded = Service.objectClose(Rpc.Object.Close.Request.ADAPTER.encode(request))
        val response = Rpc.Object.Close.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Close.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCreate(request: Rpc.Object.Create.Request): Rpc.Object.Create.Response {
        val encoded = Service.objectCreate(Rpc.Object.Create.Request.ADAPTER.encode(request))
        val response = Rpc.Object.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCreateBookmark(request: Rpc.Object.CreateBookmark.Request): Rpc.Object.CreateBookmark.Response {
        val encoded =
            Service.objectCreateBookmark(Rpc.Object.CreateBookmark.Request.ADAPTER.encode(request))
        val response = Rpc.Object.CreateBookmark.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CreateBookmark.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectBookmarkFetch(request: Rpc.Object.BookmarkFetch.Request): Rpc.Object.BookmarkFetch.Response {
        val encoded =
            Service.objectBookmarkFetch(Rpc.Object.BookmarkFetch.Request.ADAPTER.encode(request))
        val response = Rpc.Object.BookmarkFetch.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.BookmarkFetch.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCreateRelation(request: Rpc.Object.CreateRelation.Request): Rpc.Object.CreateRelation.Response {
        val encoded =
            Service.objectCreateRelation(Rpc.Object.CreateRelation.Request.ADAPTER.encode(request))
        val response = Rpc.Object.CreateRelation.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CreateRelation.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCreateObjectType(request: Rpc.Object.CreateObjectType.Request): Rpc.Object.CreateObjectType.Response {
        val encoded =
            Service.objectCreateObjectType(Rpc.Object.CreateObjectType.Request.ADAPTER.encode(request))
        val response = Rpc.Object.CreateObjectType.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CreateObjectType.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }


    override fun objectCreateRelationOption(request: Rpc.Object.CreateRelationOption.Request): Rpc.Object.CreateRelationOption.Response {
        val encoded = Service.objectCreateRelationOption(
            Rpc.Object.CreateRelationOption.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.CreateRelationOption.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CreateRelationOption.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCreateSet(request: Rpc.Object.CreateSet.Request): Rpc.Object.CreateSet.Response {
        val encoded = Service.objectCreateSet(Rpc.Object.CreateSet.Request.ADAPTER.encode(request))
        val response = Rpc.Object.CreateSet.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CreateSet.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectDuplicate(request: Rpc.Object.Duplicate.Request): Rpc.Object.Duplicate.Response {
        val encoded = Service.objectDuplicate(
            Rpc.Object.Duplicate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.Duplicate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Duplicate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectIdsSubscribe(request: Rpc.Object.SubscribeIds.Request): Rpc.Object.SubscribeIds.Response {
        val encoded =
            Service.objectSubscribeIds(Rpc.Object.SubscribeIds.Request.ADAPTER.encode(request))
        val response = Rpc.Object.SubscribeIds.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SubscribeIds.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectToSet(request: Rpc.Object.ToSet.Request): Rpc.Object.ToSet.Response {
        val encoded = Service.objectToSet(Rpc.Object.ToSet.Request.ADAPTER.encode(request))
        val response = Rpc.Object.ToSet.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ToSet.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectToCollection(request: Rpc.Object.ToCollection.Request): Rpc.Object.ToCollection.Response {
        val encoded = Service.objectToCollection(Rpc.Object.ToCollection.Request.ADAPTER.encode(request))
        val response = Rpc.Object.ToCollection.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ToCollection.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun addObjectToCollection(request: Rpc.ObjectCollection.Add.Request): Rpc.ObjectCollection.Add.Response {
        val encoded = Service.objectCollectionAdd(Rpc.ObjectCollection.Add.Request.ADAPTER.encode(request))
        val response = Rpc.ObjectCollection.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectCollection.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun setObjectSource(request: Rpc.Object.SetSource.Request): Rpc.Object.SetSource.Response {
        val encoded = Service.objectSetSource(Rpc.Object.SetSource.Request.ADAPTER.encode(request))
        val response = Rpc.Object.SetSource.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SetSource.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun navigationGetObjectInfoWithLinks(request: Rpc.Navigation.GetObjectInfoWithLinks.Request): Rpc.Navigation.GetObjectInfoWithLinks.Response {
        val encoded = Service.navigationGetObjectInfoWithLinks(
            Rpc.Navigation.GetObjectInfoWithLinks.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Navigation.GetObjectInfoWithLinks.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Navigation.GetObjectInfoWithLinks.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectListDelete(request: Rpc.Object.ListDelete.Request): Rpc.Object.ListDelete.Response {
        val encoded = Service.objectListDelete(
            Rpc.Object.ListDelete.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.ListDelete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ListDelete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectListSetIsArchived(request: Rpc.Object.ListSetIsArchived.Request): Rpc.Object.ListSetIsArchived.Response {
        val encoded = Service.objectListSetIsArchived(
            Rpc.Object.ListSetIsArchived.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.ListSetIsArchived.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ListSetIsArchived.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectOpen(request: Rpc.Object.Open.Request): Rpc.Object.Open.Response {
        val encoded = Service.objectOpen(Rpc.Object.Open.Request.ADAPTER.encode(request))
        val response = Rpc.Object.Open.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Open.Response.Error.Code.NULL) {
            when (error.code) {
                Rpc.Object.Open.Response.Error.Code.NOT_FOUND -> throw NotFoundObjectException()
                Rpc.Object.Open.Response.Error.Code.ANYTYPE_NEEDS_UPGRADE ->
                    throw BackwardCompatilityNotSupportedException()
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun objectRedo(request: Rpc.Object.Redo.Request): Rpc.Object.Redo.Response {
        val encoded = Service.objectRedo(Rpc.Object.Redo.Request.ADAPTER.encode(request))
        val response = Rpc.Object.Redo.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Redo.Response.Error.Code.NULL) {
            if (error.code == Rpc.Object.Redo.Response.Error.Code.CAN_NOT_MOVE)
                throw UndoRedoExhaustedException()
            else
                throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectRelationAdd(request: Rpc.ObjectRelation.Add.Request): Rpc.ObjectRelation.Add.Response {
        val encoded = Service.objectRelationAdd(
            Rpc.ObjectRelation.Add.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectRelation.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectRelation.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectRelationAddFeatured(request: Rpc.ObjectRelation.AddFeatured.Request): Rpc.ObjectRelation.AddFeatured.Response {
        val encoded = Service.objectRelationAddFeatured(
            Rpc.ObjectRelation.AddFeatured.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectRelation.AddFeatured.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectRelation.AddFeatured.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectRelationDelete(request: Rpc.ObjectRelation.Delete.Request): Rpc.ObjectRelation.Delete.Response {
        val encoded = Service.objectRelationDelete(
            Rpc.ObjectRelation.Delete.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectRelation.Delete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectRelation.Delete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectRelationListAvailable(request: Rpc.ObjectRelation.ListAvailable.Request): Rpc.ObjectRelation.ListAvailable.Response {
        val encoded = Service.objectRelationListAvailable(
            Rpc.ObjectRelation.ListAvailable.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectRelation.ListAvailable.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectRelation.ListAvailable.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    //todo relations refactoring
//    override fun objectRelationOptionAdd(request: Rpc.ObjectRelationOption.Add.Request): Rpc.ObjectRelationOption.Add.Response {
//        val encoded = Service.objectRelationOptionAdd(
//            Rpc.ObjectRelationOption.Add.Request.ADAPTER.encode(request)
//        )
//        val response = Rpc.ObjectRelationOption.Add.Response.ADAPTER.decode(encoded)
//        val error = response.error
//        if (error != null && error.code != Rpc.ObjectRelationOption.Add.Response.Error.Code.NULL) {
//            throw Exception(error.description)
//        } else {
//            return response
//        }
//    }

    override fun objectRelationRemoveFeatured(request: Rpc.ObjectRelation.RemoveFeatured.Request): Rpc.ObjectRelation.RemoveFeatured.Response {
        val encoded = Service.objectRelationRemoveFeatured(
            Rpc.ObjectRelation.RemoveFeatured.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectRelation.RemoveFeatured.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectRelation.RemoveFeatured.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSearch(request: Rpc.Object.Search.Request): Rpc.Object.Search.Response {
        val encoded = Service.objectSearch(Rpc.Object.Search.Request.ADAPTER.encode(request))
        val response = Rpc.Object.Search.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Search.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSearchSubscribe(request: Rpc.Object.SearchSubscribe.Request): Rpc.Object.SearchSubscribe.Response {
        val encoded =
            Service.objectSearchSubscribe(Rpc.Object.SearchSubscribe.Request.ADAPTER.encode(request))
        val response = Rpc.Object.SearchSubscribe.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SearchSubscribe.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSearchUnsubscribe(request: Rpc.Object.SearchUnsubscribe.Request): Rpc.Object.SearchUnsubscribe.Response {
        val encoded = Service.objectSearchUnsubscribe(
            Rpc.Object.SearchUnsubscribe.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.SearchUnsubscribe.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SearchUnsubscribe.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSetDetails(request: Rpc.Object.SetDetails.Request): Rpc.Object.SetDetails.Response {
        val encoded =
            Service.objectSetDetails(Rpc.Object.SetDetails.Request.ADAPTER.encode(request))
        val response = Rpc.Object.SetDetails.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SetDetails.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSetIsArchived(request: Rpc.Object.SetIsArchived.Request): Rpc.Object.SetIsArchived.Response {
        val encoded = Service.objectSetIsArchived(
            Rpc.Object.SetIsArchived.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.SetIsArchived.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SetIsArchived.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSetIsFavorite(request: Rpc.Object.SetIsFavorite.Request): Rpc.Object.SetIsFavorite.Response {
        val encoded = Service.objectSetIsFavorite(
            Rpc.Object.SetIsFavorite.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.SetIsFavorite.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SetIsFavorite.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectListSetIsFavorite(request: Rpc.Object.ListSetIsFavorite.Request): Rpc.Object.ListSetIsFavorite.Response {
        val encoded = Service.objectListSetIsFavorite(
            Rpc.Object.ListSetIsFavorite.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.ListSetIsFavorite.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ListSetIsFavorite.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSetLayout(request: Rpc.Object.SetLayout.Request): Rpc.Object.SetLayout.Response {
        val encoded = Service.objectSetLayout(
            Rpc.Object.SetLayout.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.SetLayout.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SetLayout.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectSetObjectType(request: Rpc.Object.SetObjectType.Request): Rpc.Object.SetObjectType.Response {
        val encoded = Service.objectSetObjectType(
            Rpc.Object.SetObjectType.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.SetObjectType.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SetObjectType.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectShow(request: Rpc.Object.Show.Request): Rpc.Object.Show.Response {
        val encoded = Service.objectShow(Rpc.Object.Show.Request.ADAPTER.encode(request))
        val response = Rpc.Object.Show.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Show.Response.Error.Code.NULL) {
            when (error.code) {
                Rpc.Object.Show.Response.Error.Code.NOT_FOUND -> throw NotFoundObjectException()
                Rpc.Object.Show.Response.Error.Code.ANYTYPE_NEEDS_UPGRADE ->
                    throw BackwardCompatilityNotSupportedException()
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun objectUndo(request: Rpc.Object.Undo.Request): Rpc.Object.Undo.Response {
        val encoded = Service.objectUndo(Rpc.Object.Undo.Request.ADAPTER.encode(request))
        val response = Rpc.Object.Undo.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.Undo.Response.Error.Code.NULL) {
            if (error.code == Rpc.Object.Undo.Response.Error.Code.CAN_NOT_MOVE)
                throw UndoRedoExhaustedException()
            else
                throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun unsplashDownload(request: Rpc.Unsplash.Download.Request): Rpc.Unsplash.Download.Response {
        val encoded = Service.unsplashDownload(
            Rpc.Unsplash.Download.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Unsplash.Download.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Unsplash.Download.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun unsplashSearch(request: Rpc.Unsplash.Search.Request): Rpc.Unsplash.Search.Response {
        val encoded = Service.unsplashSearch(
            Rpc.Unsplash.Search.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Unsplash.Search.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Unsplash.Search.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun versionGet(request: Rpc.App.GetVersion.Request): Rpc.App.GetVersion.Response {
        val encoded = Service.appGetVersion(Rpc.App.GetVersion.Request.ADAPTER.encode(request))
        val response = Rpc.App.GetVersion.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.App.GetVersion.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletConvert(request: Rpc.Wallet.Convert.Request): Rpc.Wallet.Convert.Response {
        val encoded = Service.walletConvert(Rpc.Wallet.Convert.Request.ADAPTER.encode(request))
        val response = Rpc.Wallet.Convert.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Wallet.Convert.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletCreate(request: Rpc.Wallet.Create.Request): Rpc.Wallet.Create.Response {
        val encoded = Service.walletCreate(Rpc.Wallet.Create.Request.ADAPTER.encode(request))
        val response = Rpc.Wallet.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Wallet.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletRecover(request: Rpc.Wallet.Recover.Request): Rpc.Wallet.Recover.Response {
        val encoded = Service.walletRecover(Rpc.Wallet.Recover.Request.ADAPTER.encode(request))
        val response = Rpc.Wallet.Recover.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Wallet.Recover.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun createTable(request: Rpc.BlockTable.Create.Request): Rpc.BlockTable.Create.Response {
        val encoded =
            Service.blockTableCreate(Rpc.BlockTable.Create.Request.ADAPTER.encode(request))
        val response = Rpc.BlockTable.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableRowListFill(request: Rpc.BlockTable.RowListFill.Request): Rpc.BlockTable.RowListFill.Response {
        val encoded =
            Service.blockTableRowListFill(Rpc.BlockTable.RowListFill.Request.ADAPTER.encode(request))
        val response = Rpc.BlockTable.RowListFill.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.RowListFill.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewSetSource(request: Rpc.BlockDataview.SetSource.Request): Rpc.BlockDataview.SetSource.Response {
        val encoded =
            Service.blockDataviewSetSource(
                Rpc.BlockDataview.SetSource.Request.ADAPTER.encode(
                    request
                )
            )
        val response = Rpc.BlockDataview.SetSource.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.SetSource.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListClearStyle(request: Rpc.BlockText.ListClearStyle.Request): Rpc.BlockText.ListClearStyle.Response {
        val encoded = Service.blockTextListClearStyle(
            Rpc.BlockText.ListClearStyle.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockText.ListClearStyle.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockText.ListClearStyle.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableColumnListFill(request: Rpc.BlockTable.ColumnListFill.Request): Rpc.BlockTable.ColumnListFill.Response {
        val encoded = Service.blockTableColumnListFill(
            Rpc.BlockTable.ColumnListFill.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.ColumnListFill.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.ColumnListFill.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableRowCreate(request: Rpc.BlockTable.RowCreate.Request): Rpc.BlockTable.RowCreate.Response {
        val encoded = Service.blockTableRowCreate(
            Rpc.BlockTable.RowCreate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.RowCreate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.RowCreate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableRowSetHeader(request: Rpc.BlockTable.RowSetHeader.Request): Rpc.BlockTable.RowSetHeader.Response {
        val encoded = Service.blockTableRowSetHeader(
            Rpc.BlockTable.RowSetHeader.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.RowSetHeader.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.RowSetHeader.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableColumnCreate(request: Rpc.BlockTable.ColumnCreate.Request): Rpc.BlockTable.ColumnCreate.Response {
        val encoded = Service.blockTableColumnCreate(
            Rpc.BlockTable.ColumnCreate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.ColumnCreate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.ColumnCreate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableColumnDelete(request: Rpc.BlockTable.ColumnDelete.Request): Rpc.BlockTable.ColumnDelete.Response {
        val encoded = Service.blockTableColumnDelete(
            Rpc.BlockTable.ColumnDelete.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.ColumnDelete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.ColumnDelete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableRowDelete(request: Rpc.BlockTable.RowDelete.Request): Rpc.BlockTable.RowDelete.Response {
        val encoded = Service.blockTableRowDelete(
            Rpc.BlockTable.RowDelete.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.RowDelete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.RowDelete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableColumnDuplicate(request: Rpc.BlockTable.ColumnDuplicate.Request): Rpc.BlockTable.ColumnDuplicate.Response {
        val encoded = Service.blockTableColumnDuplicate(
            Rpc.BlockTable.ColumnDuplicate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.ColumnDuplicate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.ColumnDuplicate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableRowDuplicate(request: Rpc.BlockTable.RowDuplicate.Request): Rpc.BlockTable.RowDuplicate.Response {
        val encoded = Service.blockTableRowDuplicate(
            Rpc.BlockTable.RowDuplicate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.RowDuplicate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.RowDuplicate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableSort(request: Rpc.BlockTable.Sort.Request): Rpc.BlockTable.Sort.Response {
        val encoded = Service.blockTableSort(
            Rpc.BlockTable.Sort.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.Sort.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.Sort.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableExpand(request: Rpc.BlockTable.Expand.Request): Rpc.BlockTable.Expand.Response {
        val encoded = Service.blockTableExpand(
            Rpc.BlockTable.Expand.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.Expand.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.Expand.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockTableColumnMove(request: Rpc.BlockTable.ColumnMove.Request): Rpc.BlockTable.ColumnMove.Response {
        val encoded = Service.blockTableColumnMove(
            Rpc.BlockTable.ColumnMove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockTable.ColumnMove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockTable.ColumnMove.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun workspaceObjectListAdd(request: Rpc.Workspace.Object.ListAdd.Request): Rpc.Workspace.Object.ListAdd.Response {
        val encoded = Service.workspaceObjectListAdd(
            Rpc.Workspace.Object.ListAdd.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Workspace.Object.ListAdd.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Workspace.Object.ListAdd.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun workspaceObjectListRemove(request: Rpc.Workspace.Object.ListRemove.Request): Rpc.Workspace.Object.ListRemove.Response {
        val encoded = Service.workspaceObjectListRemove(
            Rpc.Workspace.Object.ListRemove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Workspace.Object.ListRemove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Workspace.Object.ListRemove.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewAddFilter(request: Rpc.BlockDataview.Filter.Add.Request): Rpc.BlockDataview.Filter.Add.Response {
        val encoded = Service.blockDataviewFilterAdd(
            Rpc.BlockDataview.Filter.Add.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Filter.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Filter.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRemoveFilter(request: Rpc.BlockDataview.Filter.Remove.Request): Rpc.BlockDataview.Filter.Remove.Response {
        val encoded = Service.blockDataviewFilterRemove(
            Rpc.BlockDataview.Filter.Remove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Filter.Remove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Filter.Remove.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewReplaceFilter(request: Rpc.BlockDataview.Filter.Replace.Request): Rpc.BlockDataview.Filter.Replace.Response {
        val encoded = Service.blockDataviewFilterReplace(
            Rpc.BlockDataview.Filter.Replace.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Filter.Replace.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Filter.Replace.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewSortFilter(request: Rpc.BlockDataview.Filter.Sort.Request): Rpc.BlockDataview.Filter.Sort.Response {
        val encoded = Service.blockDataviewFilterSort(
            Rpc.BlockDataview.Filter.Sort.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Filter.Sort.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Filter.Sort.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewAddSort(request: Rpc.BlockDataview.Sort.Add.Request): Rpc.BlockDataview.Sort.Add.Response {
        val encoded = Service.blockDataviewSortAdd(
            Rpc.BlockDataview.Sort.Add.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Sort.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Sort.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRemoveSort(request: Rpc.BlockDataview.Sort.Remove.Request): Rpc.BlockDataview.Sort.Remove.Response {
        val encoded = Service.blockDataviewSortRemove(
            Rpc.BlockDataview.Sort.Remove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Sort.Remove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Sort.Remove.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewReplaceSort(request: Rpc.BlockDataview.Sort.Replace.Request): Rpc.BlockDataview.Sort.Replace.Response {
        val encoded = Service.blockDataviewSortReplace(
            Rpc.BlockDataview.Sort.Replace.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Sort.Replace.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Sort.Replace.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewSortSort(request: Rpc.BlockDataview.Sort.SSort.Request): Rpc.BlockDataview.Sort.SSort.Response {
        val encoded = Service.blockDataviewSortSort(
            Rpc.BlockDataview.Sort.SSort.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Sort.SSort.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Sort.SSort.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewAddViewRelation(request: Rpc.BlockDataview.ViewRelation.Add.Request): Rpc.BlockDataview.ViewRelation.Add.Response {
        val encoded = Service.blockDataviewViewRelationAdd(
            Rpc.BlockDataview.ViewRelation.Add.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.ViewRelation.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.ViewRelation.Add.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRemoveViewRelation(request: Rpc.BlockDataview.ViewRelation.Remove.Request): Rpc.BlockDataview.ViewRelation.Remove.Response {
        val encoded = Service.blockDataviewViewRelationRemove(
            Rpc.BlockDataview.ViewRelation.Remove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.ViewRelation.Remove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.ViewRelation.Remove.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewReplaceViewRelation(request: Rpc.BlockDataview.ViewRelation.Replace.Request): Rpc.BlockDataview.ViewRelation.Replace.Response {
        val encoded = Service.blockDataviewViewRelationReplace(
            Rpc.BlockDataview.ViewRelation.Replace.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.ViewRelation.Replace.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.ViewRelation.Replace.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewSortViewRelation(request: Rpc.BlockDataview.ViewRelation.Sort.Request): Rpc.BlockDataview.ViewRelation.Sort.Response {
        val encoded = Service.blockDataviewViewRelationSort(
            Rpc.BlockDataview.ViewRelation.Sort.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.ViewRelation.Sort.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.ViewRelation.Sort.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun spaceUsage(request: Rpc.File.SpaceUsage.Request): Rpc.File.SpaceUsage.Response {
        val encoded = Service.fileSpaceUsage(
            Rpc.File.SpaceUsage.Request.ADAPTER.encode(request)
        )
        val response = Rpc.File.SpaceUsage.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.File.SpaceUsage.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }
}