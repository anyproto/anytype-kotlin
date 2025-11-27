package com.anytypeio.anytype.middleware.service

import anytype.Rpc
import com.anytypeio.anytype.core_models.exceptions.AccountIsDeletedException
import com.anytypeio.anytype.core_models.exceptions.AccountMigrationNeededException
import com.anytypeio.anytype.core_models.exceptions.CreateAccountException
import com.anytypeio.anytype.core_models.exceptions.LoginException
import com.anytypeio.anytype.core_models.exceptions.MigrationFailedException
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.core_models.multiplayer.MultiplayerError
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteError
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.data.auth.exception.AnytypeNeedsUpgradeException
import com.anytypeio.anytype.data.auth.exception.NotFoundObjectException
import com.anytypeio.anytype.data.auth.exception.UndoRedoExhaustedException
import com.anytypeio.anytype.middleware.mappers.toCore
import javax.inject.Inject
import service.Service

class MiddlewareServiceImplementation @Inject constructor(
    featureToggles: FeatureToggles
) : MiddlewareService {

    override fun accountCreate(request: Rpc.Account.Create.Request): Rpc.Account.Create.Response {
        val encoded = Service.accountCreate(Rpc.Account.Create.Request.ADAPTER.encode(request))
        val response = Rpc.Account.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.Create.Response.Error.Code.NULL) {
            when (error.code) {
                Rpc.Account.Create.Response.Error.Code.ACCOUNT_CREATED_BUT_FAILED_TO_START_NODE -> {
                    throw CreateAccountException.AccountCreatedButFailedToStartNode
                }
                Rpc.Account.Create.Response.Error.Code.ACCOUNT_CREATED_BUT_FAILED_TO_SET_NAME -> {
                    throw CreateAccountException.AccountCreatedButFailedToSetName
                }
                Rpc.Account.Create.Response.Error.Code.FAILED_TO_STOP_RUNNING_NODE -> {
                    throw CreateAccountException.FailedToStopRunningNode
                }
                Rpc.Account.Create.Response.Error.Code.FAILED_TO_WRITE_CONFIG -> {
                    throw CreateAccountException.FailedToWriteConfig
                }
                Rpc.Account.Create.Response.Error.Code.FAILED_TO_CREATE_LOCAL_REPO -> {
                    throw CreateAccountException.FailedToCreateLocalRepo
                }
                Rpc.Account.Create.Response.Error.Code.ACCOUNT_CREATION_IS_CANCELED -> {
                    throw CreateAccountException.AccountCreationCanceled
                }
                Rpc.Account.Create.Response.Error.Code.CONFIG_FILE_NOT_FOUND -> {
                    throw CreateAccountException.ConfigFileNotFound
                }
                Rpc.Account.Create.Response.Error.Code.CONFIG_FILE_INVALID -> {
                    throw CreateAccountException.ConfigFileInvalid
                }
                Rpc.Account.Create.Response.Error.Code.CONFIG_FILE_NETWORK_ID_MISMATCH -> {
                    throw CreateAccountException.ConfigFileNetworkIdMismatch
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

    override fun accountRevertDeletion(request: Rpc.Account.RevertDeletion.Request): Rpc.Account.RevertDeletion.Response {
        val encoded = Service.accountRevertDeletion(Rpc.Account.RevertDeletion.Request.ADAPTER.encode(request))
        val response = Rpc.Account.RevertDeletion.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.RevertDeletion.Response.Error.Code.NULL) {
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
                    throw LoginException.FailedToFindAccountInfo()
                }
                Rpc.Account.Select.Response.Error.Code.ACCOUNT_IS_DELETED -> {
                    throw AccountIsDeletedException()
                }
                Rpc.Account.Select.Response.Error.Code.ACCOUNT_STORE_NOT_MIGRATED -> {
                    throw AccountMigrationNeededException()
                }
                Rpc.Account.Select.Response.Error.Code.FAILED_TO_FETCH_REMOTE_NODE_HAS_INCOMPATIBLE_PROTO_VERSION -> {
                    throw NeedToUpdateApplicationException()
                }
                Rpc.Account.Select.Response.Error.Code.CONFIG_FILE_NETWORK_ID_MISMATCH -> {
                    throw LoginException.NetworkIdMismatch()
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

    override fun accountMigrate(request: Rpc.Account.Migrate.Request): Rpc.Account.Migrate.Response {
        val encoded = Service.accountMigrate(Rpc.Account.Migrate.Request.ADAPTER.encode(request))
        val response = Rpc.Account.Migrate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.Migrate.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Account.Migrate.Response.Error.Code.NOT_ENOUGH_FREE_SPACE -> {
                    throw MigrationFailedException.NotEnoughSpace(error.requiredSpace)
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun accountMigrateCancel(request: Rpc.Account.MigrateCancel.Request): Rpc.Account.MigrateCancel.Response {
        val encoded = Service.accountMigrateCancel(Rpc.Account.MigrateCancel.Request.ADAPTER.encode(request))
        val response = Rpc.Account.MigrateCancel.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.MigrateCancel.Response.Error.Code.NULL) {
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

    override fun blockWidgetSetViewId(request: Rpc.BlockWidget.SetViewId.Request): Rpc.BlockWidget.SetViewId.Response {
        val encoded = Service.blockWidgetSetViewId(
            Rpc.BlockWidget.SetViewId.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockWidget.SetViewId.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockWidget.SetViewId.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectTypesSetOrder(request: Rpc.ObjectType.SetOrder.Request): Rpc.ObjectType.SetOrder.Response {
        val encoded = Service.objectTypeSetOrder(
            Rpc.ObjectType.SetOrder.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectType.SetOrder.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectType.SetOrder.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

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

    override fun blockDataViewViewSetPosition(request: Rpc.BlockDataview.View.SetPosition.Request): Rpc.BlockDataview.View.SetPosition.Response {
        val encoded = Service.blockDataviewViewSetPosition(
            Rpc.BlockDataview.View.SetPosition.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.View.SetPosition.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.View.SetPosition.Response.Error.Code.NULL) {
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

    override fun debugSubscriptions(request: Rpc.Debug.Subscriptions.Request): Rpc.Debug.Subscriptions.Response {
        val encoded = Service.debugSubscriptions(
            Rpc.Debug.Subscriptions.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Debug.Subscriptions.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.Subscriptions.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugSpaceSummary(request: Rpc.Debug.SpaceSummary.Request): Rpc.Debug.SpaceSummary.Response {
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

    override fun fileDiscardPreload(request: Rpc.File.DiscardPreload.Request): Rpc.File.DiscardPreload.Response {
        val encoded = Service.fileDiscardPreload(Rpc.File.DiscardPreload.Request.ADAPTER.encode(request))
        val response = Rpc.File.DiscardPreload.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.File.DiscardPreload.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun fileDrop(request: Rpc.File.Drop.Request): Rpc.File.Drop.Response {
        val encoded = Service.fileDrop(Rpc.File.Drop.Request.ADAPTER.encode(request))
        val response = Rpc.File.Drop.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.File.Drop.Response.Error.Code.NULL) {
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

    override fun removeObjectFromCollection(request: Rpc.ObjectCollection.Remove.Request): Rpc.ObjectCollection.Remove.Response {
        val encoded = Service.objectCollectionRemove(Rpc.ObjectCollection.Remove.Request.ADAPTER.encode(request))
        val response = Rpc.ObjectCollection.Remove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectCollection.Remove.Response.Error.Code.NULL) {
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
                Rpc.Object.Open.Response.Error.Code.NOT_FOUND ->{
                    throw NotFoundObjectException()
                }
                Rpc.Object.Open.Response.Error.Code.ANYTYPE_NEEDS_UPGRADE -> {
                    throw AnytypeNeedsUpgradeException()
                }
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

    override fun objectRelationListWithValue(request: Rpc.Relation.ListWithValue.Request): Rpc.Relation.ListWithValue.Response {
        val encoded = Service.relationListWithValue(
            Rpc.Relation.ListWithValue.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Relation.ListWithValue.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Relation.ListWithValue.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

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

    override fun objectSearchWithMeta(request: Rpc.Object.SearchWithMeta.Request): Rpc.Object.SearchWithMeta.Response {
        val encoded = Service.objectSearchWithMeta(Rpc.Object.SearchWithMeta.Request.ADAPTER.encode(request))
        val response = Rpc.Object.SearchWithMeta.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SearchWithMeta.Response.Error.Code.NULL) {
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
                    throw AnytypeNeedsUpgradeException()
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

    override fun objectImportUseCase(request: Rpc.Object.ImportUseCase.Request): Rpc.Object.ImportUseCase.Response {
        val encoded = Service.objectImportUseCase(Rpc.Object.ImportUseCase.Request.ADAPTER.encode(request))
        val response = Rpc.Object.ImportUseCase.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ImportUseCase.Response.Error.Code.NULL) {
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

    override fun setDeviceState(request: Rpc.App.SetDeviceState.Request): Rpc.App.SetDeviceState.Response {
        val encoded = Service.appSetDeviceState(Rpc.App.SetDeviceState.Request.ADAPTER.encode(request))
        val response = Rpc.App.SetDeviceState.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.App.SetDeviceState.Response.Error.Code.NULL) {
            when (error.code) {
                Rpc.App.SetDeviceState.Response.Error.Code.BAD_INPUT -> {
                    throw IllegalArgumentException(error.description)
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun setInitialParams(request: Rpc.Initial.SetParameters.Request): Rpc.Initial.SetParameters.Response {
        val encoded =
            Service.initialSetParameters(Rpc.Initial.SetParameters.Request.ADAPTER.encode(request))
        val response = Rpc.Initial.SetParameters.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Initial.SetParameters.Response.Error.Code.NULL) {
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
            when (error.code) {
                Rpc.Wallet.Convert.Response.Error.Code.BAD_INPUT -> {
                    throw LoginException.InvalidMnemonic()
                }
                else -> throw Exception(error.description)
            }
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
            when (error.code) {
                Rpc.Wallet.Recover.Response.Error.Code.BAD_INPUT -> {
                    throw LoginException.InvalidMnemonic()
                }
                else -> throw Exception(error.description)
            }
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

    override fun spaceDelete(request: Rpc.Space.Delete.Request): Rpc.Space.Delete.Response {
        val encoded = Service.spaceDelete(
            Rpc.Space.Delete.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.Delete.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.Delete.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun spaceSetOrder(request: Rpc.Space.SetOrder.Request): Rpc.Space.SetOrder.Response {
        val encoded = Service.spaceSetOrder(
            Rpc.Space.SetOrder.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.SetOrder.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.SetOrder.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun spaceUnsetOrder(request: Rpc.Space.UnsetOrder.Request): Rpc.Space.UnsetOrder.Response {
        val encoded = Service.spaceUnsetOrder(
            Rpc.Space.UnsetOrder.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.UnsetOrder.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.UnsetOrder.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun workspaceCreate(request: Rpc.Workspace.Create.Request): Rpc.Workspace.Create.Response {
        val encoded = Service.workspaceCreate(
            Rpc.Workspace.Create.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Workspace.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Workspace.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun workspaceOpen(request: Rpc.Workspace.Open.Request): Rpc.Workspace.Open.Response {
        val encoded = Service.workspaceOpen(
            Rpc.Workspace.Open.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Workspace.Open.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Workspace.Open.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun workspaceSetInfo(request: Rpc.Workspace.SetInfo.Request): Rpc.Workspace.SetInfo.Response {
        val encoded = Service.workspaceSetInfo(
            Rpc.Workspace.SetInfo.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Workspace.SetInfo.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Workspace.SetInfo.Response.Error.Code.NULL) {
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

    override fun workspaceObjectAdd(request: Rpc.Workspace.Object.Add.Request): Rpc.Workspace.Object.Add.Response {
        val encoded = Service.workspaceObjectAdd(
            Rpc.Workspace.Object.Add.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Workspace.Object.Add.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Workspace.Object.Add.Response.Error.Code.NULL) {
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

    override fun blockDataViewSetActiveView(request: Rpc.BlockDataview.View.SetActive.Request): Rpc.BlockDataview.View.SetActive.Response {
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

    override fun createTemplateFromObject(request: Rpc.Template.CreateFromObject.Request): Rpc.Template.CreateFromObject.Response {
        val encoded = Service.templateCreateFromObject(
            Rpc.Template.CreateFromObject.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Template.CreateFromObject.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Template.CreateFromObject.Response.Error.Code.NULL) {
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

    override fun processCancel(request: Rpc.Process.Cancel.Request): Rpc.Process.Cancel.Response {
        val encoded = Service.processCancel(
            Rpc.Process.Cancel.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Process.Cancel.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Process.Cancel.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun setInternalFlags(request: Rpc.Object.SetInternalFlags.Request): Rpc.Object.SetInternalFlags.Response {
        val encoded = Service.objectSetInternalFlags(
            Rpc.Object.SetInternalFlags.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.SetInternalFlags.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.SetInternalFlags.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectsListDuplicate(request: Rpc.Object.ListDuplicate.Request): Rpc.Object.ListDuplicate.Response {
        val encoded = Service.objectListDuplicate(
            Rpc.Object.ListDuplicate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.ListDuplicate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ListDuplicate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugStackGoroutines(request: Rpc.Debug.StackGoroutines.Request): Rpc.Debug.StackGoroutines.Response {
        val encoded = Service.debugStackGoroutines(
            Rpc.Debug.StackGoroutines.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Debug.StackGoroutines.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.StackGoroutines.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun nodeUsageInfo(request: Rpc.File.NodeUsage.Request): Rpc.File.NodeUsage.Response {
        val encoded = Service.fileNodeUsage(
            Rpc.File.NodeUsage.Request.ADAPTER.encode(request)
        )
        val response = Rpc.File.NodeUsage.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.File.NodeUsage.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun deleteRelationOptions(request: Rpc.Relation.ListRemoveOption.Request): Rpc.Relation.ListRemoveOption.Response {
        val encoded = Service.relationListRemoveOption(
            Rpc.Relation.ListRemoveOption.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Relation.ListRemoveOption.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Relation.ListRemoveOption.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun spaceInviteGenerate(request: Rpc.Space.InviteGenerate.Request): Rpc.Space.InviteGenerate.Response {
        val encoded = Service.spaceInviteGenerate(
            Rpc.Space.InviteGenerate.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.InviteGenerate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.InviteGenerate.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.InviteGenerate.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.InviteGenerate.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.InviteGenerate.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.InviteGenerate.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceInviteGetCurrent(request: Rpc.Space.InviteGetCurrent.Request): Rpc.Space.InviteGetCurrent.Response {
        val encoded = Service.spaceInviteGetCurrent(
            Rpc.Space.InviteGetCurrent.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.InviteGetCurrent.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.InviteGetCurrent.Response.Error.Code.NULL) {
            when (error.code) {
                Rpc.Space.InviteGetCurrent.Response.Error.Code.NO_ACTIVE_INVITE -> {
                    throw SpaceInviteError.InviteNotActive
                }
                else -> {
                    throw Exception(error.description)
                }
            }
        } else {
            return response
        }
    }

    override fun spaceInviteRevoke(request: Rpc.Space.InviteRevoke.Request): Rpc.Space.InviteRevoke.Response {
        val encoded = Service.spaceInviteRevoke(
            Rpc.Space.InviteRevoke.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.InviteRevoke.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.InviteRevoke.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.InviteRevoke.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.InviteRevoke.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.InviteRevoke.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.InviteRevoke.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceInviteView(request: Rpc.Space.InviteView.Request): Rpc.Space.InviteView.Response {
        val encoded = Service.spaceInviteView(Rpc.Space.InviteView.Request.ADAPTER.encode(request))
        val response = Rpc.Space.InviteView.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.InviteView.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.InviteView.Response.Error.Code.INVITE_NOT_FOUND -> {
                    throw SpaceInviteError.InvalidNotFound()
                }
                Rpc.Space.InviteView.Response.Error.Code.INVITE_BAD_CONTENT -> {
                    throw SpaceInviteError.InvalidInvite()
                }
                Rpc.Space.InviteView.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw SpaceInviteError.SpaceDeleted()
                }
                else -> {
                    throw Exception(error.description)
                }
            }
        } else {
            return response
        }
    }

    override fun spaceJoin(request: Rpc.Space.Join.Request): Rpc.Space.Join.Response {
        val encoded = Service.spaceJoin(Rpc.Space.Join.Request.ADAPTER.encode(request))
        val response = Rpc.Space.Join.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.Join.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.Join.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.Join.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.Join.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.Join.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceJoinCancel(request: Rpc.Space.JoinCancel.Request): Rpc.Space.JoinCancel.Response {
        val encoded = Service.spaceJoinCancel(Rpc.Space.JoinCancel.Request.ADAPTER.encode(request))
        val response = Rpc.Space.JoinCancel.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.JoinCancel.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.JoinCancel.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.JoinCancel.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.JoinCancel.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.JoinCancel.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceLeaveApprove(request: Rpc.Space.LeaveApprove.Request): Rpc.Space.LeaveApprove.Response {
        val encoded = Service.spaceLeaveApprove(Rpc.Space.LeaveApprove.Request.ADAPTER.encode(request))
        val response = Rpc.Space.LeaveApprove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.LeaveApprove.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.LeaveApprove.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.LeaveApprove.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.LeaveApprove.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.LeaveApprove.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceMakeShareable(request: Rpc.Space.MakeShareable.Request): Rpc.Space.MakeShareable.Response {
        val encoded = Service.spaceMakeShareable(Rpc.Space.MakeShareable.Request.ADAPTER.encode(request))
        val response = Rpc.Space.MakeShareable.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.MakeShareable.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.MakeShareable.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.MakeShareable.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.MakeShareable.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceParticipantPermissionsChange(request: Rpc.Space.ParticipantPermissionsChange.Request): Rpc.Space.ParticipantPermissionsChange.Response {
        val encoded = Service.spaceParticipantPermissionsChange(
            Rpc.Space.ParticipantPermissionsChange.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.ParticipantPermissionsChange.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.ParticipantPermissionsChange.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.ParticipantPermissionsChange.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.ParticipantPermissionsChange.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.ParticipantPermissionsChange.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.ParticipantPermissionsChange.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceParticipantRemove(request: Rpc.Space.ParticipantRemove.Request): Rpc.Space.ParticipantRemove.Response {
        val encoded = Service.spaceParticipantRemove(
            Rpc.Space.ParticipantRemove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.ParticipantRemove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.ParticipantRemove.Response.Error.Code.NULL) {
            throw Exception(error.description)
            when(error.code) {
                Rpc.Space.ParticipantRemove.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.ParticipantRemove.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.ParticipantRemove.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.ParticipantRemove.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceRequestApprove(request: Rpc.Space.RequestApprove.Request): Rpc.Space.RequestApprove.Response {
        val encoded = Service.spaceRequestApprove(
            Rpc.Space.RequestApprove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.RequestApprove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.RequestApprove.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.RequestApprove.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.RequestApprove.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.RequestApprove.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.RequestApprove.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceRequestDecline(request: Rpc.Space.RequestDecline.Request): Rpc.Space.RequestDecline.Response {
        val encoded = Service.spaceRequestDecline(
            Rpc.Space.RequestDecline.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.RequestDecline.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.RequestDecline.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.RequestDecline.Response.Error.Code.NOT_SHAREABLE -> {
                    throw MultiplayerError.Generic.NotShareable()
                }
                Rpc.Space.RequestDecline.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.RequestDecline.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.RequestDecline.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun spaceStopSharing(request: Rpc.Space.StopSharing.Request): Rpc.Space.StopSharing.Response {
        val encoded = Service.spaceStopSharing(
            Rpc.Space.StopSharing.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Space.StopSharing.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.StopSharing.Response.Error.Code.NULL) {
            when(error.code) {
                Rpc.Space.StopSharing.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.StopSharing.Response.Error.Code.LIMIT_REACHED -> {
                    throw MultiplayerError.Generic.LimitReached()
                }
                Rpc.Space.StopSharing.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun downloadManifest(request: Rpc.Gallery.DownloadManifest.Request): Rpc.Gallery.DownloadManifest.Response {
        val encoded = Service.galleryDownloadManifest(
            Rpc.Gallery.DownloadManifest.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Gallery.DownloadManifest.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Gallery.DownloadManifest.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectImportExperience(request: Rpc.Object.ImportExperience.Request): Rpc.Object.ImportExperience.Response {
        val encoded = Service.objectImportExperience(
            Rpc.Object.ImportExperience.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.ImportExperience.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.ImportExperience.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun notificationReply(request: Rpc.Notification.Reply.Request): Rpc.Notification.Reply.Response {
        val encoded = Service.notificationReply(
            Rpc.Notification.Reply.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Notification.Reply.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Notification.Reply.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun membershipStatus(request: Rpc.Membership.GetStatus.Request): Rpc.Membership.GetStatus.Response {
        val encoded = Service.membershipGetStatus(
            Rpc.Membership.GetStatus.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Membership.GetStatus.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.GetStatus.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun membershipIsNameValid(request: Rpc.Membership.IsNameValid.Request): Rpc.Membership.IsNameValid.Response {
        val encoded = Service.membershipIsNameValid(
            Rpc.Membership.IsNameValid.Request.ADAPTER.encode(request)
        ) ?: return Rpc.Membership.IsNameValid.Response(
            error = Rpc.Membership.IsNameValid.Response.Error(
                code = Rpc.Membership.IsNameValid.Response.Error.Code.NULL
            )
        )
        val response = Rpc.Membership.IsNameValid.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.IsNameValid.Response.Error.Code.NULL) {
            throw error.toCore()
        } else {
            return response
        }
    }

    override fun membershipRegisterPaymentRequest(request: Rpc.Membership.RegisterPaymentRequest.Request): Rpc.Membership.RegisterPaymentRequest.Response {
        val encoded = Service.membershipRegisterPaymentRequest(
            Rpc.Membership.RegisterPaymentRequest.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Membership.RegisterPaymentRequest.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.RegisterPaymentRequest.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun membershipGetPortalLinkUrl(request: Rpc.Membership.GetPortalLinkUrl.Request): Rpc.Membership.GetPortalLinkUrl.Response {
        val encoded = Service.membershipGetPortalLinkUrl(
            Rpc.Membership.GetPortalLinkUrl.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Membership.GetPortalLinkUrl.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.GetPortalLinkUrl.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun membershipFinalize(request: Rpc.Membership.Finalize.Request): Rpc.Membership.Finalize.Response {
        val encoded = Service.membershipFinalize(
            Rpc.Membership.Finalize.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Membership.Finalize.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.Finalize.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun membershipGetVerificationEmailStatus(request: Rpc.Membership.GetVerificationEmailStatus.Request): Rpc.Membership.GetVerificationEmailStatus.Response {
        val encoded = Service.membershipGetVerificationEmail(
            Rpc.Membership.GetVerificationEmailStatus.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Membership.GetVerificationEmailStatus.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.GetVerificationEmailStatus.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun membershipGetVerificationEmail(request: Rpc.Membership.GetVerificationEmail.Request): Rpc.Membership.GetVerificationEmail.Response {
        val encoded = Service.membershipGetVerificationEmail(
            Rpc.Membership.GetVerificationEmail.Request.ADAPTER.encode(request)
        ) ?: return Rpc.Membership.GetVerificationEmail.Response(
            error = Rpc.Membership.GetVerificationEmail.Response.Error(
                code = Rpc.Membership.GetVerificationEmail.Response.Error.Code.NULL
            )
        )
        val response = Rpc.Membership.GetVerificationEmail.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.GetVerificationEmail.Response.Error.Code.NULL) {
            throw error.toCore()
        } else {
            return response
        }
    }

    override fun membershipVerifyEmailCode(request: Rpc.Membership.VerifyEmailCode.Request): Rpc.Membership.VerifyEmailCode.Response {
        val encoded = Service.membershipVerifyEmailCode(
            Rpc.Membership.VerifyEmailCode.Request.ADAPTER.encode(request)
        ) ?: return Rpc.Membership.VerifyEmailCode.Response(
            error = Rpc.Membership.VerifyEmailCode.Response.Error(
                code = Rpc.Membership.VerifyEmailCode.Response.Error.Code.NULL
            )
        )
        val response = Rpc.Membership.VerifyEmailCode.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.VerifyEmailCode.Response.Error.Code.NULL) {
            throw error.toCore()
        } else {
            return response
        }
    }

    override fun membershipGetTiers(request: Rpc.Membership.GetTiers.Request): Rpc.Membership.GetTiers.Response {
        val encoded = Service.membershipGetTiers(
            Rpc.Membership.GetTiers.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Membership.GetTiers.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Membership.GetTiers.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun getVersions(request: Rpc.History.GetVersions.Request): Rpc.History.GetVersions.Response {
        val encoded = Service.historyGetVersions(
            Rpc.History.GetVersions.Request.ADAPTER.encode(request)
        )
        val response = Rpc.History.GetVersions.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.History.GetVersions.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun showVersion(request: Rpc.History.ShowVersion.Request): Rpc.History.ShowVersion.Response {
        val encoded = Service.historyShowVersion(
            Rpc.History.ShowVersion.Request.ADAPTER.encode(request)
        )
        val response = Rpc.History.ShowVersion.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.History.ShowVersion.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun setVersion(request: Rpc.History.SetVersion.Request): Rpc.History.SetVersion.Response {
        val encoded = Service.historySetVersion(
            Rpc.History.SetVersion.Request.ADAPTER.encode(request)
        )
        val response = Rpc.History.SetVersion.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.History.SetVersion.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun diffVersions(request: Rpc.History.DiffVersions.Request): Rpc.History.DiffVersions.Response {
        val encoded = Service.historyDiffVersions(
            Rpc.History.DiffVersions.Request.ADAPTER.encode(request)
        )
        val response = Rpc.History.DiffVersions.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.History.DiffVersions.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatAddMessage(request: Rpc.Chat.AddMessage.Request): Rpc.Chat.AddMessage.Response {
        val encoded = Service.chatAddMessage(
            Rpc.Chat.AddMessage.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.AddMessage.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.AddMessage.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatEditMessage(request: Rpc.Chat.EditMessageContent.Request): Rpc.Chat.EditMessageContent.Response {
        val encoded = Service.chatEditMessageContent(
            Rpc.Chat.EditMessageContent.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.EditMessageContent.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.EditMessageContent.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatDeleteMessage(request: Rpc.Chat.DeleteMessage.Request): Rpc.Chat.DeleteMessage.Response {
        val encoded = Service.chatDeleteMessage(
            Rpc.Chat.DeleteMessage.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.DeleteMessage.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.DeleteMessage.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatReadAll(request: Rpc.Chat.ReadAll.Request): Rpc.Chat.ReadAll.Response {
        val encoded = Service.chatReadAll(
            Rpc.Chat.ReadAll.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.ReadAll.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.ReadAll.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatReadMessages(request: Rpc.Chat.ReadMessages.Request): Rpc.Chat.ReadMessages.Response {
        val encoded = Service.chatReadMessages(
            Rpc.Chat.ReadMessages.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.ReadMessages.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.ReadMessages.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatGetMessages(request: Rpc.Chat.GetMessages.Request): Rpc.Chat.GetMessages.Response {
        val encoded = Service.chatGetMessages(
            Rpc.Chat.GetMessages.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.GetMessages.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.GetMessages.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatGetMessagesByIds(request: Rpc.Chat.GetMessagesByIds.Request): Rpc.Chat.GetMessagesByIds.Response {
        val encoded = Service.chatGetMessagesByIds(
            Rpc.Chat.GetMessagesByIds.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.GetMessagesByIds.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.GetMessagesByIds.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatSubscribeLastMessages(request: Rpc.Chat.SubscribeLastMessages.Request): Rpc.Chat.SubscribeLastMessages.Response {
        val encoded = Service.chatSubscribeLastMessages(
            Rpc.Chat.SubscribeLastMessages.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.SubscribeLastMessages.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.SubscribeLastMessages.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatToggleMessageReaction(
        request: Rpc.Chat.ToggleMessageReaction.Request
    ): Rpc.Chat.ToggleMessageReaction.Response {
        val encoded = Service.chatToggleMessageReaction(
            Rpc.Chat.ToggleMessageReaction.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.ToggleMessageReaction.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.ToggleMessageReaction.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatUnsubscribe(request: Rpc.Chat.Unsubscribe.Request): Rpc.Chat.Unsubscribe.Response {
        val encoded = Service.chatUnsubscribe(
            Rpc.Chat.Unsubscribe.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.Unsubscribe.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.Unsubscribe.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatSubscribeToMessagePreviews(request: Rpc.Chat.SubscribeToMessagePreviews.Request): Rpc.Chat.SubscribeToMessagePreviews.Response {
        val encoded = Service.chatSubscribeToMessagePreviews(
            Rpc.Chat.SubscribeToMessagePreviews.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.SubscribeToMessagePreviews.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.SubscribeToMessagePreviews.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun chatUnsubscribeToMessagePreviews(request: Rpc.Chat.UnsubscribeFromMessagePreviews.Request): Rpc.Chat.UnsubscribeFromMessagePreviews.Response {
        val encoded = Service.chatUnsubscribeFromMessagePreviews(
            Rpc.Chat.UnsubscribeFromMessagePreviews.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Chat.UnsubscribeFromMessagePreviews.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Chat.UnsubscribeFromMessagePreviews.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugAccountSelectTrace(request: Rpc.Debug.AccountSelectTrace.Request): Rpc.Debug.AccountSelectTrace.Response {
        val encoded = Service.debugAccountSelectTrace(
            Rpc.Debug.AccountSelectTrace.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Debug.AccountSelectTrace.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.AccountSelectTrace.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectDateByTimestamp(request: Rpc.Object.DateByTimestamp.Request): Rpc.Object.DateByTimestamp.Response {
        val encoded = Service.objectDateByTimestamp(
            Rpc.Object.DateByTimestamp.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.DateByTimestamp.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.DateByTimestamp.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun deviceNetworkStateSet(request: Rpc.Device.NetworkState.Set.Request): Rpc.Device.NetworkState.Set.Response {
        val encoded = Service.deviceNetworkStateSet(
            Rpc.Device.NetworkState.Set.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Device.NetworkState.Set.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Device.NetworkState.Set.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugExportLogs(request: Rpc.Debug.ExportLog.Request): Rpc.Debug.ExportLog.Response {
        val encoded = Service.debugExportLog(
            Rpc.Debug.ExportLog.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Debug.ExportLog.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.ExportLog.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectTypeListConflictingRelations(request: Rpc.ObjectType.ListConflictingRelations.Request): Rpc.ObjectType.ListConflictingRelations.Response {
        val encoded = Service.objectTypeListConflictingRelations(
            Rpc.ObjectType.ListConflictingRelations.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectType.ListConflictingRelations.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectType.ListConflictingRelations.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectTypeHeaderRecommendedFieldsSet(request: Rpc.ObjectType.Recommended.FeaturedRelationsSet.Request): Rpc.ObjectType.Recommended.FeaturedRelationsSet.Response {
        val encoded = Service.objectTypeRecommendedFeaturedRelationsSet(
            Rpc.ObjectType.Recommended.FeaturedRelationsSet.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectType.Recommended.FeaturedRelationsSet.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectType.Recommended.FeaturedRelationsSet.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectTypeRecommendedFieldsSet(request: Rpc.ObjectType.Recommended.RelationsSet.Request): Rpc.ObjectType.Recommended.RelationsSet.Response {
        val encoded = Service.objectTypeRecommendedRelationsSet(
            Rpc.ObjectType.Recommended.RelationsSet.Request.ADAPTER.encode(request)
        )
        val response = Rpc.ObjectType.Recommended.RelationsSet.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.ObjectType.Recommended.RelationsSet.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockDataViewRelationSet(request: Rpc.BlockDataview.Relation.Set.Request): Rpc.BlockDataview.Relation.Set.Response {
        val encoded = Service.blockDataviewRelationSet(
            Rpc.BlockDataview.Relation.Set.Request.ADAPTER.encode(request)
        )
        val response = Rpc.BlockDataview.Relation.Set.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.BlockDataview.Relation.Set.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun pushNotificationRegisterToken(request: Rpc.PushNotification.RegisterToken.Request): Rpc.PushNotification.RegisterToken.Response {
        val encoded = Service.pushNotificationRegisterToken(
            Rpc.PushNotification.RegisterToken.Request.ADAPTER.encode(request)
        )
        val response = Rpc.PushNotification.RegisterToken.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.PushNotification.RegisterToken.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun linkPreview(request: Rpc.LinkPreview.Request): Rpc.LinkPreview.Response {
        val encoded = Service.linkPreview(
            Rpc.LinkPreview.Request.ADAPTER.encode(request)
        )
        val response = Rpc.LinkPreview.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.LinkPreview.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCreateFromUrl(request: Rpc.Object.CreateFromUrl.Request): Rpc.Object.CreateFromUrl.Response {
        val encoded = Service.objectCreateFromUrl(
            Rpc.Object.CreateFromUrl.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.CreateFromUrl.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CreateFromUrl.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun setSpaceMode(request: Rpc.PushNotification.SetSpaceMode.Request): Rpc.PushNotification.SetSpaceMode.Response {
        val encoded = Service.pushNotificationSetSpaceMode(
            Rpc.PushNotification.SetSpaceMode.Request.ADAPTER.encode(request)
        )
        val response = Rpc.PushNotification.SetSpaceMode.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.PushNotification.SetSpaceMode.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun debugStats(request: Rpc.Debug.Stat.Request): Rpc.Debug.Stat.Response {
        val encoded = Service.debugStat(
            Rpc.Debug.Stat.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Debug.Stat.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Debug.Stat.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun spaceChangeInvite(request: Rpc.Space.InviteChange.Request): Rpc.Space.InviteChange.Response {
        val encoded = Service.spaceInviteChange(
            Rpc.Space.InviteChange.Request.ADAPTER.encode(request)
        ) ?: return Rpc.Space.InviteChange.Response()
        val response = Rpc.Space.InviteChange.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Space.InviteChange.Response.Error.Code.NULL) {
            when (error.code) {
                Rpc.Space.InviteChange.Response.Error.Code.NO_SUCH_SPACE -> {
                    throw MultiplayerError.Generic.NoSuchSpace()
                }
                Rpc.Space.InviteChange.Response.Error.Code.SPACE_IS_DELETED -> {
                    throw MultiplayerError.Generic.SpaceIsDeleted()
                }
                Rpc.Space.InviteChange.Response.Error.Code.INCORRECT_PERMISSIONS -> {
                    throw MultiplayerError.Generic.IncorrectPermissions()
                }
                Rpc.Space.InviteChange.Response.Error.Code.REQUEST_FAILED -> {
                    throw MultiplayerError.Generic.RequestFailed()
                }
                else -> throw Exception(error.description)
            }
        } else {
            return response
        }
    }

    override fun publishingGetStatus(request: Rpc.Publishing.GetStatus.Request): Rpc.Publishing.GetStatus.Response {
        val encoded = Service.publishingGetStatus(
            Rpc.Publishing.GetStatus.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Publishing.GetStatus.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Publishing.GetStatus.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun publishingCreate(request: Rpc.Publishing.Create.Request): Rpc.Publishing.Create.Response {
        val encoded = Service.publishingCreate(
            Rpc.Publishing.Create.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Publishing.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Publishing.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun publishingRemove(request: Rpc.Publishing.Remove.Request): Rpc.Publishing.Remove.Response {
        val encoded = Service.publishingRemove(
            Rpc.Publishing.Remove.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Publishing.Remove.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Publishing.Remove.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun publishingList(request: Rpc.Publishing.List.Request): Rpc.Publishing.List.Response {
        val encoded = Service.publishingList(
            Rpc.Publishing.List.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Publishing.List.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Publishing.List.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun appShutdown(request: Rpc.App.Shutdown.Request): Rpc.App.Shutdown.Response {
        val encoded = Service.appShutdown(
            Rpc.App.Shutdown.Request.ADAPTER.encode(request)
        )
        val response = Rpc.App.Shutdown.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.App.Shutdown.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun setForceModeIds(request: Rpc.PushNotification.SetForceModeIds.Request): Rpc.PushNotification.SetForceModeIds.Response {
        val encoded = Service.pushNotificationSetForceModeIds(
            Rpc.PushNotification.SetForceModeIds.Request.ADAPTER.encode(request)
        )
        val response = Rpc.PushNotification.SetForceModeIds.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.PushNotification.SetForceModeIds.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun resetIds(request: Rpc.PushNotification.ResetIds.Request): Rpc.PushNotification.ResetIds.Response {
        val encoded = Service.pushNotificationResetIds(
            Rpc.PushNotification.ResetIds.Request.ADAPTER.encode(request)
        )
        val response = Rpc.PushNotification.ResetIds.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.PushNotification.ResetIds.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCrossSpaceSubscribe(request: Rpc.Object.CrossSpaceSearchSubscribe.Request): Rpc.Object.CrossSpaceSearchSubscribe.Response {
        val encoded = Service.objectCrossSpaceSearchSubscribe(
            Rpc.Object.CrossSpaceSearchSubscribe.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.CrossSpaceSearchSubscribe.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CrossSpaceSearchSubscribe.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun objectCrossSpaceUnsubscribe(request: Rpc.Object.CrossSpaceSearchUnsubscribe.Request): Rpc.Object.CrossSpaceSearchUnsubscribe.Response {
        val encoded = Service.objectCrossSpaceSearchUnsubscribe(
            Rpc.Object.CrossSpaceSearchUnsubscribe.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Object.CrossSpaceSearchUnsubscribe.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Object.CrossSpaceSearchUnsubscribe.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }
}
