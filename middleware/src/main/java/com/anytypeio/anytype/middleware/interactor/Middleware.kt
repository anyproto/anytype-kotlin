package com.anytypeio.anytype.middleware.interactor

import anytype.Rpc
import anytype.Rpc.Chat.ReadMessages.ReadType
import anytype.Rpc.PushNotification.RegisterToken.Platform
import anytype.model.Block
import anytype.model.ParticipantPermissionChange
import anytype.model.Range
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.CBTextStyle
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
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationListWithValueItem
import com.anytypeio.anytype.core_models.Relations
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
import com.anytypeio.anytype.core_utils.tools.ThreadInfo
import com.anytypeio.anytype.middleware.BuildConfig
import com.anytypeio.anytype.middleware.auth.toAccountSetup
import com.anytypeio.anytype.middleware.const.Constants
import com.anytypeio.anytype.middleware.interactor.events.payload
import com.anytypeio.anytype.middleware.mappers.MDVFilter
import com.anytypeio.anytype.middleware.mappers.MDetail
import com.anytypeio.anytype.middleware.mappers.MNetworkMode
import com.anytypeio.anytype.middleware.mappers.MRelationFormat
import com.anytypeio.anytype.middleware.mappers.config
import com.anytypeio.anytype.middleware.mappers.core
import com.anytypeio.anytype.middleware.mappers.mw
import com.anytypeio.anytype.middleware.mappers.parse
import com.anytypeio.anytype.middleware.mappers.toCore
import com.anytypeio.anytype.middleware.mappers.toCoreLinkPreview
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import com.anytypeio.anytype.middleware.mappers.toCoreModelSearchResults
import com.anytypeio.anytype.middleware.mappers.toCoreModels
import com.anytypeio.anytype.middleware.mappers.toMiddleware
import com.anytypeio.anytype.middleware.mappers.toMiddlewareModel
import com.anytypeio.anytype.middleware.mappers.toMw
import com.anytypeio.anytype.middleware.mappers.toPayload
import com.anytypeio.anytype.middleware.model.CreateWalletResponse
import com.anytypeio.anytype.middleware.service.MiddlewareService
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.measureTimedValue
import timber.log.Timber

class Middleware @Inject constructor(
    private val service: MiddlewareService,
    private val factory: MiddlewareFactory,
    private val logger: MiddlewareProtobufLogger,
    private val protobufConverter: ProtobufConverterProvider,
    private val threadInfo: ThreadInfo
) {

    @Throws(Exception::class)
    fun accountCreate(command: Command.AccountCreate): AccountSetup {

        val request = Rpc.Account.Create.Request(
            name = command.name,
            avatarLocalPath = command.avatarPath,
            icon = command.icon.toLong(),
            networkMode = command.networkMode.toMiddlewareModel(),
            networkCustomConfigFilePath = command.networkConfigFilePath.orEmpty()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountCreate(request) }
        logResponseIfDebug(response, time)
        return response.toAccountSetup()
    }

    @Throws(Exception::class)
    fun accountSelect(command: Command.AccountSelect): AccountSetup {

        val networkMode = command.networkMode.toMiddlewareModel()
        val networkCustomConfigFilePath = if (networkMode == MNetworkMode.CustomConfig) {
            command.networkConfigFilePath.orEmpty()
        } else ""
        val request = Rpc.Account.Select.Request(
            id = command.id,
            rootPath = command.path,
            networkMode = networkMode,
            networkCustomConfigFilePath = networkCustomConfigFilePath,
            preferYamuxTransport = command.preferYamuxTransport ?: false
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountSelect(request) }
        logResponseIfDebug(response, time)
        return response.toAccountSetup()
    }

    @Throws(Exception::class)
    fun accountDelete(): AccountStatus {
        val request = Rpc.Account.Delete.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountDelete(request) }
        logResponseIfDebug(response, time)
        val status = response.status
        checkNotNull(status) { "Account status was null" }
        return status.core()
    }

    @Throws(Exception::class)
    fun accountMigrate(account: Id, path: String) {
        val request = Rpc.Account.Migrate.Request(
            id = account,
            rootPath = path
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountMigrate(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun accountMigrateCancel(account: Id) {
        val request = Rpc.Account.MigrateCancel.Request(id = account)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountMigrateCancel(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun accountRecover() {
        val request = Rpc.Account.Recover.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountRecover(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun accountRestore(): AccountStatus {
        val request = Rpc.Account.RevertDeletion.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountRevertDeletion(request) }
        logResponseIfDebug(response, time)
        val status = response.status
        checkNotNull(status) { "Account status was null" }
        return status.core()
    }

    @Throws(Exception::class)
    fun accountStop(clearLocalRepositoryData: Boolean) {
        val request: Rpc.Account.Stop.Request = Rpc.Account.Stop.Request(
            removeData = clearLocalRepositoryData
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.accountStop(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun blockBookmarkCreateAndFetch(command: Command.CreateBookmark): Payload {
        val request = Rpc.BlockBookmark.CreateAndFetch.Request(
            contextId = command.context,
            targetId = command.target,
            url = command.url,
            position = command.position.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockBookmarkCreateAndFetch(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockBookmarkFetch(command: Command.SetupBookmark): Payload {
        val request = Rpc.BlockBookmark.Fetch.Request(
            contextId = command.context,
            blockId = command.target,
            url = command.url
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockBookmarkFetch(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockCopy(command: Command.Copy): Response.Clipboard.Copy {
        val range: Range? = command.range?.let {
            Range(
                from = it.first,
                to = it.last
            )
        }
        val blocks: List<Block> = command.blocks.map { it.toMiddlewareModel() }
        val request = Rpc.Block.Copy.Request(
            contextId = command.context,
            selectedTextRange = range,
            blocks = blocks
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockCopy(request) }
        logResponseIfDebug(response, time)
        return Response.Clipboard.Copy(
            response.textSlot,
            response.htmlSlot,
            response.anySlot.toCoreModels()
        )
    }

    @Throws(Exception::class)
    fun blockCreate(
        contextId: String,
        targetId: String,
        position: Position,
        prototype: com.anytypeio.anytype.core_models.Block.Prototype
    ): Pair<String, Payload> {
        val request = Rpc.Block.Create.Request(
            contextId = contextId,
            targetId = targetId,
            position = position.toMiddlewareModel(),
            block = factory.create(prototype)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockCreate(request) }
        logResponseIfDebug(response, time)

        return Pair(response.blockId, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockDataViewViewSetPosition(
        ctx: Id,
        dv: Id,
        view: Id,
        pos: Int
    ): Payload {
        val request = Rpc.BlockDataview.View.SetPosition.Request(
            contextId = ctx,
            blockId = dv,
            viewId = view,
            position = pos
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewViewSetPosition(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockDataViewRelationAdd(
        ctx: Id,
        dv: Id,
        relation: Id
    ): Payload {
        val request = Rpc.BlockDataview.Relation.Add.Request(
            contextId = ctx,
            blockId = dv,
            relationKeys = listOf(relation)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewRelationAdd(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockDataViewRelationDelete(ctx: Id, dv: Id, relation: Id): Payload {
        val request = Rpc.BlockDataview.Relation.Delete.Request(
            contextId = ctx,
            blockId = dv,
            relationKeys = listOf(relation)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewRelationDelete(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockDataViewViewCreate(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload {
        val request = Rpc.BlockDataview.View.Create.Request(
            contextId = ctx,
            blockId = target,
            view = Block.Content.Dataview.View(
                name = name,
                type = type.toMiddlewareModel()
            )
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewViewCreate(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockDataViewViewCreate(
        context: String,
        target: String,
        viewer: DVViewer
    ): Pair<Id, Payload> {
        val request = Rpc.BlockDataview.View.Create.Request(
            contextId = context,
            blockId = target,
            view = viewer.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewViewCreate(request) }
        logResponseIfDebug(response, time)
        return Pair(response.viewId, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockDataViewViewDelete(
        ctx: String,
        dataview: String,
        viewer: String
    ): Payload {
        val request = Rpc.BlockDataview.View.Delete.Request(
            contextId = ctx,
            blockId = dataview,
            viewId = viewer
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewViewDelete(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockDataViewViewUpdate(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload {
        val request = Rpc.BlockDataview.View.Update.Request(
            contextId = context,
            blockId = target,
            viewId = viewer.id,
            view = viewer.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewViewUpdate(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListDelete(command: Command.Unlink): Payload {
        val request = Rpc.Block.ListDelete.Request(
            contextId = command.context,
            blockIds = command.targets
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListDelete(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListDuplicate(command: Command.Duplicate): Pair<List<Id>, Payload> {
        val request = Rpc.Block.ListDuplicate.Request(
            contextId = command.context,
            targetId = command.target,
            blockIds = command.blocks,
            position = Block.Position.Bottom
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListDuplicate(request) }
        logResponseIfDebug(response, time)

        return Pair(response.blockIds, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockListMoveToExistingObject(command: Command.Move): Payload {
        val position: Block.Position = command.position.toMiddlewareModel()

        val request = Rpc.Block.ListMoveToExistingObject.Request(
            contextId = command.ctx,
            targetContextId = command.targetContextId,
            position = position,
            blockIds = command.blockIds,
            dropTargetId = command.targetId
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListMoveToExistingObject(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListMoveToNewObject(command: Command.TurnIntoDocument): List<String> {
        val request = Rpc.Block.ListMoveToNewObject.Request(
            contextId = command.context,
            blockIds = command.targets
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListMoveToNewObject(request) }
        logResponseIfDebug(response, time)
        return listOf(response.linkId)
    }

    @Throws(Exception::class)
    fun blockListSetAlign(command: Command.UpdateAlignment): Payload {

        val align: Block.Align = command.alignment.toMiddlewareModel()

        val request = Rpc.Block.ListSetAlign.Request(
            contextId = command.context,
            blockIds = command.targets,
            align = align
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListSetAlign(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListSetBackgroundColor(command: Command.UpdateBackgroundColor): Payload {
        val request = Rpc.Block.ListSetBackgroundColor.Request(
            contextId = command.context,
            blockIds = command.targets,
            color = command.color
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListSetBackgroundColor(request) }
        logResponseIfDebug(response, time)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListSetDivStyle(command: Command.UpdateDivider): Payload {
        val style = command.style.toMiddlewareModel()
        val request = Rpc.BlockDiv.ListSetStyle.Request(
            contextId = command.context,
            blockIds = command.targets,
            style = style
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListSetDivStyle(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListSetFields(command: Command.SetFields): Payload {
        val fields: MutableList<Rpc.Block.ListSetFields.Request.BlockField> = ArrayList()
        for (i in command.fields.indices) {
            val (first, second) = command.fields[i]
            val field = Rpc.Block.ListSetFields.Request.BlockField(
                blockId = first,
                fields = second.map
            )
            fields.add(field)
        }
        val request = Rpc.Block.ListSetFields.Request(
            contextId = command.context,
            blockFields = fields
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListSetFields(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListTurnInto(
        context: String,
        targets: List<String>,
        style: CBTextStyle
    ): Payload {
        val request = Rpc.Block.ListTurnInto.Request(
            contextId = context,
            blockIds = targets,
            style = style.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListTurnInto(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockMerge(command: Command.Merge): Payload {
        val request = Rpc.Block.Merge.Request(
            contextId = command.context,
            firstBlockId = command.pair.first,
            secondBlockId = command.pair.second
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockMerge(request) }
        logResponseIfDebug(response, time)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockPaste(command: Command.Paste): Response.Clipboard.Paste {

        val range = Range(
            from = command.range.first,
            to = command.range.last
        )

        val blocks: List<Block> = command.blocks.map { it.toMiddlewareModel() }

        val request: Rpc.Block.Paste.Request = Rpc.Block.Paste.Request(
            contextId = command.context,
            focusedBlockId = command.focus,
            textSlot = command.text,
            htmlSlot = command.html.orEmpty(),
            selectedTextRange = range,
            anySlot = blocks,
            selectedBlockIds = command.selected,
            isPartOfBlock = command.isPartOfBlock ?: false
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockPaste(request) }
        logResponseIfDebug(response, time)
        return Response.Clipboard.Paste(
            response.caretPosition,
            response.isSameBlockCaret,
            response.blockIds,
            response.event.toPayload()
        )
    }

    //todo Add Relation mapping
    @Throws(Exception::class)
    fun blockRelationAdd(command: Command.AddRelationToBlock): Payload {
        TODO("relations refactoring")
//        val request = Rpc.BlockRelation.Add.Request(
//            contextId = command.contextId,
//            blockId = command.blockId,
//            relation = null
//        )
//        if (BuildConfig.DEBUG) logRequest(request)
//        val response = service.blockRelationAdd(request)
//        if (BuildConfig.DEBUG) logResponse(response)
//        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockRelationSetKey(command: Command.SetRelationKey): Payload {
        val request = Rpc.BlockRelation.SetKey.Request(
            contextId = command.contextId,
            blockId = command.blockId,
            key = command.key
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockRelationSetKey(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockReplace(command: Command.Replace): Pair<String, Payload> {

        val model: Block = factory.create(command.prototype)

        val request = Rpc.Block.Create.Request(
            contextId = command.context,
            targetId = command.target,
            position = Block.Position.Replace,
            block = model
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockCreate(request) }
        logResponseIfDebug(response, time)

        return Pair(response.blockId, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockSplit(command: Command.Split): Pair<String, Payload> {

        val style = command.style.toMiddlewareModel()

        val range = Range(
            from = command.range.first,
            to = command.range.last
        )

        val mode = command.mode.toMiddlewareModel()

        val request: Rpc.Block.Split.Request = Rpc.Block.Split.Request(
            contextId = command.context,
            blockId = command.target,
            style = style,
            range = range,
            mode = mode
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockSplit(request) }
        logResponseIfDebug(response, time)

        return Pair(response.blockId, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockTextListSetColor(command: Command.UpdateTextColor): Payload {
        val request = Rpc.BlockText.ListSetColor.Request(
            contextId = command.context,
            color = command.color,
            blockIds = command.targets
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTextListSetColor(request) }
        logResponseIfDebug(response, time)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockTextListSetMark(command: Command.UpdateBlocksMark): Payload {
        val context = command.context
        val mark = command.mark.toMiddlewareModel()
        val targets = command.targets
        val request = Rpc.BlockText.ListSetMark.Request(
            contextId = context,
            blockIds = targets,
            mark = mark
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTextListSetMark(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockTextListSetStyle(command: Command.UpdateStyle): Payload {
        val style = command.style.toMiddlewareModel()
        val request = Rpc.BlockText.ListSetStyle.Request(
            style = style,
            blockIds = command.targets,
            contextId = command.context
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTextListSetStyle(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockTextSetIcon(command: Command.SetTextIcon): Payload {
        val (image, emoji) = when (val icon = command.icon) {
            is Command.SetTextIcon.Icon.Emoji -> "" to icon.unicode
            is Command.SetTextIcon.Icon.Image -> icon.hash to ""
            is Command.SetTextIcon.Icon.None -> "" to ""
        }
        val request = Rpc.BlockText.SetIcon.Request(
            contextId = command.context,
            blockId = command.blockId,
            iconImage = image,
            iconEmoji = emoji,
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTextSetIcon(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockTextSetChecked(
        context: String,
        target: String,
        isChecked: Boolean
    ): Payload {
        val request = Rpc.BlockText.SetChecked.Request(
            contextId = context,
            blockId = target,
            checked = isChecked
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTextSetChecked(request) }
        logResponseIfDebug(response, time)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockTextSetText(
        contextId: String,
        blockId: String,
        text: String,
        marks: List<Block.Content.Text.Mark>
    ) {
        val markup: Block.Content.Text.Marks = Block.Content.Text.Marks(marks)
        val request = Rpc.BlockText.SetText.Request(
            contextId = contextId,
            blockId = blockId,
            text = text,
            marks = markup
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTextSetText(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun blockLinkSetAppearance(
        command: Command.SetLinkAppearance,
    ): Payload {
        val content = command.content
        val request = Rpc.BlockLink.ListSetAppearance.Request(
            contextId = command.contextId,
            blockIds = listOf(command.blockId),
            iconSize = content.iconSize.toMiddlewareModel(),
            cardStyle = content.cardStyle.toMiddlewareModel(),
            description = content.description.toMiddlewareModel(),
            relations = content.relations.toList()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockLinkListSetAppearance(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockUpload(command: Command.UploadBlock): Payload {
        val request = Rpc.Block.Upload.Request(
            filePath = command.filePath,
            url = command.url,
            contextId = command.contextId,
            blockId = command.blockId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockUpload(request) }
        logResponseIfDebug(response, time)

        return response.event.toPayload()
    }

    private val coverIdKey = "coverId"
    private val coverTypeKey = "coverType"

    @Throws(Exception::class)
    fun debugExportLocalStore(path: String): String {
        val request = Rpc.Debug.ExportLocalstore.Request(
            path = path
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugExportLocalStore(request) }
        logResponseIfDebug(response, time)
        return response.path
    }

    @Throws(Exception::class)
    fun debugSpaceSummary(space: SpaceId): String {
        val request = Rpc.Debug.SpaceSummary.Request(spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugSpaceSummary(request) }
        logResponseIfDebug(response, time)
        return response.infos.toCoreModel()
    }

    @Throws(Exception::class)
    fun debugSubscriptions(): List<Id> {
        val request = Rpc.Debug.Subscriptions.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugSubscriptions(request) }
        logResponseIfDebug(response, time)
        return response.subscriptions
    }

    @Throws(Exception::class)
    fun debugObject(objectId: Id, path: String): String {
        val request = Rpc.Debug.Tree.Request(
            treeId = objectId,
            path = path
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugObject(request) }
        logResponseIfDebug(response, time)
        return response.filename
    }

    @Throws(Exception::class)
    fun fileListOffload() {
        val request = Rpc.File.ListOffload.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.fileListOffload(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun fileUpload(command: Command.UploadFile): ObjectWrapper.File {
        val type = command.type.toMiddlewareModel()
        val request = Rpc.File.Upload.Request(
            localPath = command.path,
            type = type,
            spaceId = command.space.id,
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.fileUpload(request) }
        logResponseIfDebug(response, time)
        return ObjectWrapper.File(response.details.orEmpty())
    }

    @Throws(Exception::class)
    fun fileDrop(command: Command.FileDrop): Payload {
        val request = Rpc.File.Drop.Request(
            contextId = command.ctx,
            dropTargetId = command.dropTarget,
            position = command.blockPosition.toMiddlewareModel(),
            localFilePaths = command.localFilePaths
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.fileDrop(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun fileDownload(command: Command.DownloadFile): Rpc.File.Download.Response {
        val request = Rpc.File.Download.Request(
            objectId = command.objectId,
            path = command.path
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.fileDownload(request) }
        logResponseIfDebug(response, time)
        return response
    }

    @Throws(Exception::class)
    fun objectApplyTemplate(
        command: Command.ApplyTemplate
    ) {
        val request = Rpc.Object.ApplyTemplate.Request(
            contextId = command.objectId,
            templateId = command.template.orEmpty()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectApplyTemplate(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectClose(id: String, space: Space) {
        val request = Rpc.Object.Close.Request(objectId = id, spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectClose(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectCreate(
        command: Command.CreateObject
    ): CreateObjectResult {
        val request = Rpc.Object.Create.Request(
            details = command.prefilled,
            templateId = command.template.orEmpty(),
            internalFlags = command.internalFlags.toMiddlewareModel(),
            spaceId = command.space.id,
            objectTypeUniqueKey = command.typeKey.key,
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectCreate(request) }
        logResponseIfDebug(response, time)
        return response.toCoreModel()
    }

    @Throws(Exception::class)
    fun blockLinkCreateWithObject(
        command: Command.CreateBlockLinkWithObject
    ): CreateBlockLinkWithObjectResult {

        val request = Rpc.BlockLink.CreateWithObject.Request(
            contextId = command.context,
            objectTypeUniqueKey = command.type.key,
            details = command.prefilled,
            templateId = command.template.orEmpty(),
            internalFlags = command.internalFlags.toMiddlewareModel(),
            targetId = command.target,
            position = command.position.toMiddlewareModel(),
            fields = null,
            spaceId = command.space,
            block = Block(
                link = Block.Content.Link(
                    style = Block.Content.Link.Style.Page,
                    cardStyle = Block.Content.Link.CardStyle.Card,
                    iconSize = Block.Content.Link.IconSize.SizeSmall
                )
            )
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockLinkCreateWithObject(request) }
        logResponseIfDebug(response, time)
        return response.toCoreModel()
    }

    @Throws(Exception::class)
    fun objectCreateBookmark(
        space: Id,
        url: Url,
        details: Struct
    ): Id {
        val request = Rpc.Object.CreateBookmark.Request(
            details = buildMap {
                put(Relations.SOURCE, url)
                putAll(details)
            },
            spaceId = space
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectCreateBookmark(request) }
        logResponseIfDebug(response, time)
        return response.objectId
    }

    @Throws(Exception::class)
    fun objectBookmarkFetch(ctx: Id, url: Url) {
        val request = Rpc.Object.BookmarkFetch.Request(
            contextId = ctx,
            url = url
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectBookmarkFetch(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectCreateRelation(
        space: Id,
        name: String,
        format: RelationFormat,
        formatObjectTypes: List<Id>,
        prefilled: Struct
    ): ObjectWrapper.Relation {
        val request = Rpc.Object.CreateRelation.Request(
            details = buildMap {
                put(Relations.NAME, name)
                val f = format.toMiddlewareModel()
                put(Relations.RELATION_FORMAT, f.value.toDouble())
                if (f == MRelationFormat.object_) {
                    put(Relations.RELATION_FORMAT_OBJECT_TYPES, formatObjectTypes)
                }
                if (prefilled.isNotEmpty()) {
                    putAll(prefilled)
                }
            },
            spaceId = space
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectCreateRelation(request) }
        logResponseIfDebug(response, time)
        return ObjectWrapper.Relation(
            response.details ?: throw IllegalStateException("Missing details")
        )
    }

    @Throws(Exception::class)
    fun objectCreateObjectType(
        command: Command.CreateObjectType
    ): String {
        val request = Rpc.Object.CreateObjectType.Request(
            details = command.details,
            spaceId = command.spaceId,
            internalFlags = command.internalFlags.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectCreateObjectType(request) }
        logResponseIfDebug(response, time)
        return response.objectId
    }

    @Throws(Exception::class)
    fun objectCreateRelationOption(
        space: Id,
        relation: Key,
        name: String,
        color: String
    ): ObjectWrapper.Option {
        val request = Rpc.Object.CreateRelationOption.Request(
            details = buildMap {
                put(Relations.RELATION_KEY, relation)
                put(Relations.NAME, name)
                put(Relations.RELATION_OPTION_COLOR, color)
            },
            spaceId = space
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectCreateRelationOption(request) }
        logResponseIfDebug(response, time)
        return ObjectWrapper.Option(
            response.details ?: throw IllegalStateException("Missing details")
        )
    }

    @Throws(Exception::class)
    fun objectCreateSet(
        space: Id,
        objectType: String?,
        details: Struct?
    ): Response.Set.Create {
        val source = if (objectType != null) {
            listOf(objectType)
        } else {
            listOf()
        }

        val request = Rpc.Object.CreateSet.Request(
            source = source,
            spaceId = space,
            details = details
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectCreateSet(request) }
        logResponseIfDebug(response, time)

        return Response.Set.Create(
            objectId = response.objectId,
            payload = response.event.toPayload(),
            details = response.details.orEmpty()
        )
    }

    @Throws(Exception::class)
    fun objectDuplicate(id: Id): Id {
        val request = Rpc.Object.Duplicate.Request(contextId = id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectDuplicate(request) }
        logResponseIfDebug(response, time)
        return response.id
    }

    @Throws(Exception::class)
    fun objectIdsSubscribe(
        space: SpaceId,
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult {
        val request = Rpc.Object.SubscribeIds.Request(
            spaceId = space.id,
            subId = subscription,
            keys = keys,
            ids = ids
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectIdsSubscribe(request) }
        logResponseIfDebug(response, time)
        return SearchResult(
            results = response.records.mapNotNull { record ->
                if (record != null && record.isNotEmpty())
                    ObjectWrapper.Basic(record)
                else
                    null
            },
            dependencies = response.dependencies.mapNotNull { record ->
                if (record != null && record.isNotEmpty())
                    ObjectWrapper.Basic(record)
                else
                    null
            },
            counter = null
        )
    }

    @Throws(Exception::class)
    fun objectListDelete(targets: List<Id>) {
        val request = Rpc.Object.ListDelete.Request(objectIds = targets)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectListDelete(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectListSetIsArchived(
        targets: List<Id>,
        isArchived: Boolean
    ) {
        val request = Rpc.Object.ListSetIsArchived.Request(
            objectIds = targets,
            isArchived = isArchived,
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectListSetIsArchived(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectOpenOld(id: String, space: SpaceId): Payload {
        val request = Rpc.Object.Open.Request(objectId = id, spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectOpen(request) }
        logResponseIfDebug(response, time)

        return response.objectView?.toPayload()
            ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectOpen(id: String, space: SpaceId): ObjectView {
        val request = Rpc.Object.Open.Request(objectId = id, spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectOpen(request) }
        logResponseIfDebug(response, time)
        return response.objectView?.toCore() ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectRedo(command: Command.Redo): Payload {
        val request = Rpc.Object.Redo.Request(contextId = command.context)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectRedo(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRelationAdd(ctx: Id, relation: Key): Payload? {
        val request = Rpc.ObjectRelation.Add.Request(
            contextId = ctx,
            relationKeys = listOf(relation)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectRelationAdd(request) }
        logResponseIfDebug(response, time)
        return response.event?.toPayload()
    }

    @Throws(Exception::class)
    fun objectRelationAddFeatured(
        ctx: Id,
        relations: List<Id>
    ): Payload {
        val request = Rpc.ObjectRelation.AddFeatured.Request(
            contextId = ctx,
            relations = relations
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectRelationAddFeatured(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRelationDelete(ctx: Id, relations: List<Key>): Payload {
        val request = Rpc.ObjectRelation.Delete.Request(
            contextId = ctx,
            relationKeys = relations
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectRelationDelete(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRelationListWithValue(command: Command.RelationListWithValue): List<RelationListWithValueItem> {
        val request = Rpc.Relation.ListWithValue.Request(
            spaceId = command.space.id,
            value_ = command.value
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectRelationListWithValue(request) }
        logResponseIfDebug(response, time)
        return response.list.map { it.toCoreModel() }
    }

    @Throws(Exception::class)
    fun objectRelationRemoveFeatured(
        ctx: Id,
        relations: List<Id>
    ): Payload {
        val request = Rpc.ObjectRelation.RemoveFeatured.Request(
            contextId = ctx,
            relations = relations
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectRelationRemoveFeatured(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRemoveCover(ctx: String): Payload {
        val coverIdDetail = MDetail(
            key = coverIdKey,
            value_ = null
        )
        val coverTypeDetail = MDetail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_NONE.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRemoveIcon(ctx: String): Payload {
        val imageDetail = MDetail(
            key = Relations.ICON_IMAGE,
            value_ = null
        )
        val emojiDetail = MDetail(
            key = Relations.ICON_EMOJI,
            value_ = null
        )

        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(imageDetail, emojiDetail)
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSearch(
        space: SpaceId,
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        keys: List<Id>
    ): List<Map<String, Any?>> {
        val request = Rpc.Object.Search.Request(
            spaceId = space.id,
            sorts = sorts.map { it.toMiddlewareModel() },
            filters = filters.map { it.toMiddlewareModel() },
            fullText = fulltext,
            offset = offset,
            limit = limit,
            keys = keys
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSearch(request) }
        logResponseIfDebug(response, time)
        return response.records.map { it?.toMap() ?: emptyMap() }
    }

    @Throws(Exception::class)
    fun objectSearchWithMeta(command: Command.SearchWithMeta): List<Command.SearchWithMeta.Result>  {
        val request = Rpc.Object.SearchWithMeta.Request(
            spaceId = command.space.id,
            sorts = command.sorts.map { it.toMiddlewareModel() },
            filters = command.filters.map { it.toMiddlewareModel() },
            fullText = command.query,
            offset = command.offset,
            limit = command.limit,
            keys = command.keys
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSearchWithMeta(request) }
        logResponseIfDebug(response, time)
        return response.toCoreModelSearchResults()
    }

    @Throws(Exception::class)
    fun objectSearchSubscribe(
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
    ): SearchResult {
        val request = Rpc.Object.SearchSubscribe.Request(
            spaceId = space.id,
            subId = subscription,
            sorts = sorts.map { it.toMiddlewareModel() },
            filters = filters.map { it.toMiddlewareModel() },
            keys = keys,
            offset = offset,
            limit = limit.toLong(),
            beforeId = beforeId.orEmpty(),
            afterId = afterId.orEmpty(),
            source = source,
            noDepSubscription = noDepSubscription ?: false,
            collectionId = collection.orEmpty()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSearchSubscribe(request) }
        logResponseIfDebug(response, time)
        return SearchResult(
            results = response.records.mapNotNull { record ->
                if (record != null && record.isNotEmpty())
                    ObjectWrapper.Basic(record)
                else
                    null
            },
            dependencies = response.dependencies.mapNotNull { record ->
                if (record != null && record.isNotEmpty())
                    ObjectWrapper.Basic(record)
                else
                    null
            },
            counter = response.counters?.parse()
        )
    }

    @Throws(Exception::class)
    fun objectSearchUnsubscribe(subscriptions: List<String>) {
        val request = Rpc.Object.SearchUnsubscribe.Request(
            subIds = subscriptions
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSearchUnsubscribe(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectSetCoverColor(
        ctx: String,
        color: String
    ): Payload {
        val coverIdDetail = MDetail(
            key = coverIdKey,
            value_ = color
        )
        val coverTypeDetail = MDetail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_COLOR.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetCoverGradient(
        ctx: String,
        gradient: String
    ): Payload {
        val coverIdDetail = MDetail(
            key = coverIdKey,
            value_ = gradient
        )
        val coverTypeDetail = MDetail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_GRADIENT.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetCoverImage(
        ctx: String,
        hash: String
    ): Payload {
        val coverIdDetail = MDetail(
            key = coverIdKey,
            value_ = hash
        )
        val coverTypeDetail = MDetail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_UPLOADED_IMAGE.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setObjectDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload {
        val detail = MDetail(
            key = key,
            value_ = value
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(detail)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setObjectDetails(
        ctx: Id,
        details: Struct
    ): Payload {
        val detailsList = details.map { entry ->
            MDetail(
                key = entry.key,
                value_ = entry.value
            )
        }
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = detailsList
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload {

        val emojiDetail = MDetail(
            key = Relations.ICON_EMOJI,
            value_ = command.emoji
        )

        val imageDetail = MDetail(
            key = Relations.ICON_IMAGE,
            value_ = null
        )

        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(emojiDetail, imageDetail)
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetImageIcon(command: Command.SetDocumentImageIcon): Payload {

        val imageDetail = MDetail(
            key = Relations.ICON_IMAGE,
            value_ = command.id
        )
        val emojiDetail = MDetail(
            key = Relations.ICON_EMOJI,
            value_ = null
        )

        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(imageDetail, emojiDetail)
        )

        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectListSetIsFavorite(
        objectIds: List<Id>,
        isFavorite: Boolean
    ) {
        Rpc.Object.ListDelete
        val request = Rpc.Object.ListSetIsFavorite.Request(
            objectIds = objectIds,
            isFavorite = isFavorite
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectListSetIsFavorite(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectSetLayout(ctx: Id, layout: ObjectType.Layout): Payload {
        val request = Rpc.Object.SetLayout.Request(
            contextId = ctx,
            layout = layout.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetLayout(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetObjectType(ctx: Id, objectTypeKey: Key): Payload {
        val request = Rpc.Object.SetObjectType.Request(
            contextId = ctx,
            objectTypeUniqueKey = objectTypeKey
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetObjectType(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetTitle(command: Command.UpdateTitle) {
        val detail = MDetail(
            key = Relations.NAME,
            value_ = command.title
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(detail)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectSetDetails(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectShowOld(id: String, space: SpaceId): Payload {
        val request = Rpc.Object.Show.Request(objectId = id, spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectShow(request) }
        logResponseIfDebug(response, time)
        return response.objectView?.toPayload()
            ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectShow(id: String, space: SpaceId): ObjectView {
        val request = Rpc.Object.Show.Request(objectId = id, spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectShow(request) }
        logResponseIfDebug(response, time)
        return response.objectView?.toCore() ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectUndo(command: Command.Undo): Payload {
        val request = Rpc.Object.Undo.Request(contextId = command.context)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectUndo(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectImportUseCaseGetStarted(space: Id) : Command.ImportUseCase.Result {
        val request = Rpc.Object.ImportUseCase.Request(
            spaceId = space,
            useCase = Rpc.Object.ImportUseCase.Request.UseCase.GET_STARTED_MOBILE
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectImportUseCase(request) }
        logResponseIfDebug(response, time)
        return Command.ImportUseCase.Result(
            startingObject = response.startingObjectId.ifEmpty { null }
        )
    }

    @Throws(Exception::class)
    fun versionGet(): Rpc.App.GetVersion.Response {
        val request = Rpc.App.GetVersion.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.versionGet(request) }
        logResponseIfDebug(response, time)
        return response
    }

    @Throws(Exception::class)
    fun setAppState(state: AppState): Rpc.App.SetDeviceState.Response {
        val request = Rpc.App.SetDeviceState.Request(
            deviceState = state.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.setDeviceState(request) }
        logResponseIfDebug(response, time)
        return response
    }

    @Throws(Exception::class)
    fun metricsSetParameters(
        command: Command.SetInitialParams
    ) {
        val request = Rpc.Initial.SetParameters.Request(
            platform = command.platform,
            version = command.version,
            logLevel = command.defaultLogLevel,
            workdir = command.workDir
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.setInitialParams(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun walletConvert(entropy: String): String {
        val request = Rpc.Wallet.Convert.Request(entropy = entropy)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.walletConvert(request) }
        logResponseIfDebug(response, time)
        return response.mnemonic
    }

    @Throws(Exception::class)
    fun walletCreate(path: String): CreateWalletResponse {
        val request = Rpc.Wallet.Create.Request(rootPath = path)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.walletCreate(request) }
        logResponseIfDebug(response, time)
        return CreateWalletResponse(response.mnemonic)
    }

    @Throws(Exception::class)
    fun walletRecover(path: String, mnemonic: String) {
        val request = Rpc.Wallet.Recover.Request(
            mnemonic = mnemonic,
            rootPath = path
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.walletRecover(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun createTable(
        ctx: String,
        target: String,
        position: Position,
        rows: Int,
        columns: Int
    ): Payload {
        val request = Rpc.BlockTable.Create.Request(
            contextId = ctx,
            targetId = target,
            position = position.toMiddlewareModel(),
            rows = rows,
            columns = columns
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.createTable(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun fillTableRow(ctx: String, targetIds: List<String>): Payload {
        val request = Rpc.BlockTable.RowListFill.Request(
            contextId = ctx,
            blockIds = targetIds
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableRowListFill(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectToSet(ctx: String, source: List<String>) {
        val request = Rpc.Object.ToSet.Request(
            contextId = ctx,
            source = source
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectToSet(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectToCollection(ctx: Id) {
        val request = Rpc.Object.ToCollection.Request(
            contextId = ctx
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectToCollection(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun blockDataViewSetSource(
        ctx: Id,
        blockId: Id,
        sources: List<Id>
    ): Payload {
        val request = Rpc.BlockDataview.SetSource.Request(
            contextId = ctx,
            blockId = blockId,
            source = sources
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewSetSource(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun clearBlockContent(
        ctx: Id,
        blockIds: List<Id>
    ): Payload {
        val request = Rpc.BlockText.ListClearContent.Request(
            contextId = ctx,
            blockIds = blockIds
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListClearContent(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun clearBlockStyle(
        ctx: Id,
        blockIds: List<Id>
    ): Payload {
        val request = Rpc.BlockText.ListClearStyle.Request(
            contextId = ctx,
            blockIds = blockIds
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockListClearStyle(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun fillTableColumn(
        ctx: Id,
        blockIds: List<Id>
    ): Payload {
        val request = Rpc.BlockTable.ColumnListFill.Request(
            contextId = ctx,
            blockIds = blockIds
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableColumnListFill(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun createTableRow(
        ctx: Id,
        targetId: Id,
        position: Block.Position
    ): Payload {
        val request = Rpc.BlockTable.RowCreate.Request(
            contextId = ctx,
            targetId = targetId,
            position = position
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableRowCreate(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setTableRowHeader(
        ctx: Id,
        targetId: Id,
        isHeader: Boolean
    ): Payload {
        val request = Rpc.BlockTable.RowSetHeader.Request(
            contextId = ctx,
            targetId = targetId,
            isHeader = isHeader
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableRowSetHeader(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun createTableColumn(
        ctx: Id,
        targetId: Id,
        position: Block.Position
    ): Payload {
        val request = Rpc.BlockTable.ColumnCreate.Request(
            contextId = ctx,
            targetId = targetId,
            position = position
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableColumnCreate(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun deleteTableColumn(
        ctx: Id,
        targetId: Id
    ): Payload {
        val request = Rpc.BlockTable.ColumnDelete.Request(
            contextId = ctx,
            targetId = targetId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableColumnDelete(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun deleteTableRow(
        ctx: Id,
        targetId: Id
    ): Payload {
        val request = Rpc.BlockTable.RowDelete.Request(
            contextId = ctx,
            targetId = targetId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableRowDelete(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun duplicateTableColumn(
        ctx: Id,
        targetId: Id,
        blockId: Id,
        position: Block.Position
    ): Payload {
        val request = Rpc.BlockTable.ColumnDuplicate.Request(
            contextId = ctx,
            targetId = targetId,
            blockId = blockId,
            position = position
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableColumnDuplicate(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun duplicateTableRow(
        ctx: Id,
        targetId: Id,
        blockId: Id,
        position: Block.Position
    ): Payload {
        val request = Rpc.BlockTable.RowDuplicate.Request(
            contextId = ctx,
            targetId = targetId,
            blockId = blockId,
            position = position
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableRowDuplicate(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun sortTable(
        ctx: Id,
        columnId: String,
        type: Block.Content.Dataview.Sort.Type
    ): Payload {
        val request = Rpc.BlockTable.Sort.Request(
            contextId = ctx,
            columnId = columnId,
            type = type
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableSort(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun expandTable(
        ctx: Id,
        targetId: Id,
        columns: Int,
        rows: Int
    ): Payload {
        val request = Rpc.BlockTable.Expand.Request(
            contextId = ctx,
            targetId = targetId,
            columns = columns,
            rows = rows
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableExpand(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun moveTableColumn(
        ctx: Id,
        target: Id,
        dropTarget: Id,
        position: Position
    ): Payload {
        val request = Rpc.BlockTable.ColumnMove.Request(
            contextId = ctx,
            targetId = target,
            dropTargetId = dropTarget,
            position = position.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockTableColumnMove(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun spaceDelete(space: SpaceId) {
        val request = Rpc.Space.Delete.Request(spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceDelete(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun spaceSetOrder(spaceViewId: Id, spaceViewOrder: List<Id>): List<Id> {
        val request = Rpc.Space.SetOrder.Request(
            spaceViewId = spaceViewId,
            spaceViewOrder = spaceViewOrder
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceSetOrder(request) }
        logResponseIfDebug(response, time)
        return response.spaceViewOrder
    }

    @Throws(Exception::class)
    fun spaceUnsetOrder(spaceViewId: Id) {
        val request = Rpc.Space.UnsetOrder.Request(
            spaceViewId = spaceViewId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceUnsetOrder(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun workspaceCreate(command: Command.CreateSpace): Command.CreateSpace.Result {
        val request = Rpc.Workspace.Create.Request(
            details = command.details,
            useCase = command.useCase.toMiddlewareModel(),
            withChat = command.withChat
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.workspaceCreate(request) }
        logResponseIfDebug(response, time)
        return Command.CreateSpace.Result(
            space = SpaceId(response.spaceId),
            startingObject = response.startingObjectId.ifEmpty { null }
        )
    }

    @Throws(Exception::class)
    fun workspaceOpen(space: Id): Config {
        val request = Rpc.Workspace.Open.Request(
            spaceId = space,
            withChat = false
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.workspaceOpen(request) }
        logResponseIfDebug(response, time)
        val info = response.info
        if (info != null) {
            return info.config()
        } else {
            throw IllegalStateException("Workspace info is empty")
        }
    }

    @Throws(Exception::class)
    fun workspaceSetInfo(
        space: SpaceId,
        struct: Struct
    ) {
        val request = Rpc.Workspace.SetInfo.Request(
            spaceId = space.id,
            details = struct
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.workspaceSetInfo(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun workspaceObjectListAdd(objects: List<Id>, space: Id): List<Id> {
        val request = Rpc.Workspace.Object.ListAdd.Request(
            objectIds = objects,
            spaceId = space
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.workspaceObjectListAdd(request) }
        logResponseIfDebug(response, time)
        return response.objectIds
    }

    @Throws(Exception::class)
    fun workspaceObjectAdd(command: Command.AddObjectToSpace): Pair<Id, Struct?> {
        val request = Rpc.Workspace.Object.Add.Request(
            objectId = command.objectId,
            spaceId = command.space
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.workspaceObjectAdd(request) }
        logResponseIfDebug(response, time)
        return Pair(response.objectId, response.details)
    }

    @Throws(Exception::class)
    fun workspaceObjectListRemove(objects: List<Id>): List<Id> {
        val request = Rpc.Workspace.Object.ListRemove.Request(
            objectIds = objects
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.workspaceObjectListRemove(request) }
        logResponseIfDebug(response, time)
        return response.ids
    }

    @Throws(Exception::class)
    fun createWidgetBlock(
        ctx: Id,
        source: Id,
        layout: WidgetLayout,
        target: Id?,
        position: Position
    ): Payload {
        val request = Rpc.Block.CreateWidget.Request(
            contextId = ctx,
            widgetLayout = layout.mw(),
            block = Block(
                link = Block.Content.Link(
                    targetBlockId = source
                )
            ),
            targetId = target.orEmpty(),
            position = position.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockCreateWidget(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    fun setWidgetViewId(
        ctx: Id,
        widget: Id,
        view: Id
    ): Payload {
        val request = Rpc.BlockWidget.SetViewId.Request(
            contextId = ctx,
            blockId = widget,
            viewId = view
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockWidgetSetViewId(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun createWidgetByReplacingExistingWidget(
        ctx: Id,
        widget: Id,
        source: Id,
        type: WidgetLayout
    ): Payload {
        val request = Rpc.Block.CreateWidget.Request(
            contextId = ctx,
            targetId = widget,
            widgetLayout = type.mw(),
            position = Block.Position.Replace,
            block = Block(
                link = Block.Content.Link(
                    targetBlockId = source
                )
            )
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockCreateWidget(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun addDataViewFilter(
        command: Command.AddFilter
    ): Payload {
        val filter = MDVFilter(
            RelationKey = command.relationKey,
            operator_ = command.operator.toMiddlewareModel(),
            condition = command.condition.toMiddlewareModel(),
            quickOption = command.quickOption.toMiddlewareModel(),
            value_ = command.value,
            format = command.relationFormat?.toMiddlewareModel()
                ?: anytype.model.RelationFormat.longtext
        )
        val request = Rpc.BlockDataview.Filter.Add.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            filter = filter
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewAddFilter(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun replaceDataViewFilter(
        command: Command.ReplaceFilter
    ): Payload {
        val request = Rpc.BlockDataview.Filter.Replace.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            id = command.id,
            filter = command.filter.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewReplaceFilter(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun removeDataViewFilter(
        command: Command.RemoveFilter
    ): Payload {
        val request = Rpc.BlockDataview.Filter.Remove.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            ids = command.ids
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewRemoveFilter(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun addDataViewSort(command: Command.AddSort): Payload {
        val request = Rpc.BlockDataview.Sort.Add.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            sort = Block.Content.Dataview.Sort(
                RelationKey = command.relationKey,
                type = command.type.toMiddlewareModel(),
                customOrder = command.customOrder,
                format = command.relationFormat?.toMiddlewareModel()
                    ?: anytype.model.RelationFormat.longtext,
                includeTime = command.includeTime ?: false
            )
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewAddSort(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun replaceDataViewSort(command: Command.ReplaceSort): Payload {
        val request = Rpc.BlockDataview.Sort.Replace.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            id = command.sort.id,
            sort = command.sort.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewReplaceSort(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun removeDataViewSort(
        command: Command.RemoveSort
    ): Payload {
        val request = Rpc.BlockDataview.Sort.Remove.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            ids = command.ids
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewRemoveSort(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun addDataViewViewRelation(
        command: Command.AddRelation
    ): Payload {
        val request = Rpc.BlockDataview.ViewRelation.Replace.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            relation = command.relation.toMiddlewareModel(),
            relationKey = command.relation.key
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewReplaceViewRelation(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun replaceDataViewViewRelation(
        command: Command.UpdateRelation
    ): Payload {
        val request = Rpc.BlockDataview.ViewRelation.Replace.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            relationKey = command.relation.key,
            relation = command.relation.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewReplaceViewRelation(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun removeDataViewViewRelation(
        command: Command.DeleteRelation
    ): Payload {
        val request = Rpc.BlockDataview.ViewRelation.Remove.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            relationKeys = command.keys
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewRemoveViewRelation(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun sortDataViewViewRelation(
        command: Command.SortRelations
    ): Payload {
        val request = Rpc.BlockDataview.ViewRelation.Sort.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            relationKeys = command.keys
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewSortViewRelation(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setInternalFlags(
        command: Command.SetInternalFlags
    ): Payload {
        val request = Rpc.Object.SetInternalFlags.Request(
            contextId = command.ctx,
            internalFlags = command.flags.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.setInternalFlags(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    fun addObjectToCollection(command: Command.AddObjectToCollection): Payload {
        val request = Rpc.ObjectCollection.Add.Request(
            contextId = command.ctx,
            afterId = command.afterId,
            objectIds = command.ids
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.addObjectToCollection(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    fun setQueryToSet(command: Command.SetQueryToSet): Payload {
        val request = Rpc.Object.SetSource.Request(
            contextId = command.ctx,
            source = listOf(command.query)
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.setObjectSource(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun nodeUsage(): NodeUsageInfo {
        val request = Rpc.File.NodeUsage.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.nodeUsageInfo(request) }
        logResponseIfDebug(response, time)
        return response.toCoreModel()
    }

    @Throws(Exception::class)
    fun duplicateObjectsList(
        objects: List<Id>
    ): List<Id> {
        val request = Rpc.Object.ListDuplicate.Request(
            objectIds = objects
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectsListDuplicate(request) }
        logResponseIfDebug(response, time)
        return response.ids
    }

    @Throws(Exception::class)
    fun createTemplateFromObject(
        ctx: Id
    ): Id {
        val request = Rpc.Template.CreateFromObject.Request(
            contextId = ctx
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.createTemplateFromObject(request) }
        logResponseIfDebug(response, time)
        return response.id
    }

    @Throws
    fun deleteRelationOptions(
        command: Command.DeleteRelationOptions
    ) {
        val request = Rpc.Relation.ListRemoveOption.Request(
            optionIds = command.optionIds
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.deleteRelationOptions(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun debugStackGoroutines(path: String) {
        val request = Rpc.Debug.StackGoroutines.Request(
            path = path
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugStackGoroutines(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun generateSpaceInviteLink(
        space: SpaceId,
        inviteType: InviteType?,
        permissions: SpaceMemberPermissions?
    ): SpaceInviteLink {
        val request = if (inviteType != null && permissions != null) {
            Rpc.Space.InviteGenerate.Request(
                spaceId = space.id,
                inviteType = inviteType.toMiddleware(),
                permissions = permissions.toMw()
            )
        } else if (inviteType != null) {
            Rpc.Space.InviteGenerate.Request(
                spaceId = space.id,
                inviteType = inviteType.toMiddleware()
            )
        } else if (permissions != null) {
            Rpc.Space.InviteGenerate.Request(
                spaceId = space.id,
                permissions = permissions.toMw()
            )
        } else {
            Rpc.Space.InviteGenerate.Request(
                spaceId = space.id
            )
        }
        
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceInviteGenerate(request) }
        logResponseIfDebug(response, time)
        return SpaceInviteLink(
            contentId = response.inviteCid,
            fileKey = response.inviteFileKey,
            permissions = response.permissions.toCore(),
            inviteType = response.inviteType.toCoreModel()
        )
    }

    @Throws(Exception::class)
    fun approveSpaceRequest(
        space: SpaceId,
        identity: Id,
        permissions: SpaceMemberPermissions
    ) {
        val request = Rpc.Space.RequestApprove.Request(
            spaceId = space.id,
            identity = identity,
            permissions = permissions.toMw()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceRequestApprove(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun declineSpaceRequest(
        space: SpaceId,
        identity: Id
    ) {
        val request = Rpc.Space.RequestDecline.Request(
            spaceId = space.id,
            identity = identity
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceRequestDecline(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun removeSpaceMembers(
        space: SpaceId,
        identities: List<Id>
    ) {
        val request = Rpc.Space.ParticipantRemove.Request(
            spaceId = space.id,
            identities = identities
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceParticipantRemove(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun changeSpaceMemberPermissions(space: SpaceId, identity: Id, permission: SpaceMemberPermissions) {
        val request = Rpc.Space.ParticipantPermissionsChange.Request(
            spaceId = space.id,
            changes = listOf(
                ParticipantPermissionChange(
                    identity = identity,
                    perms = permission.toMw()
                )
            )
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceParticipantPermissionsChange(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun sendJoinSpaceRequest(command: Command.SendJoinSpaceRequest) {
        val request = Rpc.Space.Join.Request(
            networkId = command.network.orEmpty(),
            inviteCid = command.inviteContentId,
            inviteFileKey = command.inviteFileKey,
            spaceId = command.space.id
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceJoin(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun cancelJoinRequest(space: SpaceId) {
        val request = Rpc.Space.JoinCancel.Request(
            spaceId = space.id
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceJoinCancel(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun approveSpaceLeaveRequest(command: Command.ApproveSpaceLeaveRequest) {
        val request = Rpc.Space.LeaveApprove.Request(
            spaceId = command.space.id,
            identities = command.identities
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceLeaveApprove(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun getSpaceInviteView(
        inviteContentId: Id,
        inviteFileKey: String
    ): SpaceInviteView {
        val request = Rpc.Space.InviteView.Request(
            inviteCid = inviteContentId,
            inviteFileKey = inviteFileKey,
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceInviteView(request) }
        logResponseIfDebug(response, time)
        return SpaceInviteView(
            space = SpaceId(response.spaceId),
            creatorName = response.creatorName,
            spaceName = response.spaceName,
            spaceIconContentId = response.spaceIconCid,
            withoutApprove = response.inviteType == anytype.model.InviteType.WithoutApprove
        )
    }

    @Throws(Exception::class)
    fun stopSharingSpace(space: SpaceId) {
        val request = Rpc.Space.StopSharing.Request(
            spaceId = space.id
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceStopSharing(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun getCurrentSpaceInvite(space: SpaceId): SpaceInviteLink {
        val request = Rpc.Space.InviteGetCurrent.Request(
            spaceId = space.id
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceInviteGetCurrent(request) }
        logResponseIfDebug(response, time)
        return SpaceInviteLink(
            fileKey = response.inviteFileKey,
            contentId = response.inviteCid,
            permissions = response.permissions.toCore(),
            inviteType = response.inviteType.toCoreModel()
        )
    }

    @Throws(Exception::class)
    fun makeSpaceShareable(space: SpaceId) {
        val request = Rpc.Space.MakeShareable.Request(
            spaceId = space.id
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceMakeShareable(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun revokeSpaceInvite(space: SpaceId) {
        val request = Rpc.Space.InviteRevoke.Request(spaceId = space.id)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceInviteRevoke(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun downloadGalleryManifest(command: Command.DownloadGalleryManifest): ManifestInfo? {
        val request = Rpc.Gallery.DownloadManifest.Request(
            url = command.url
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.downloadManifest(request) }
        logResponseIfDebug(response, time)
        return response.info?.toCoreModel()
    }

    @Throws(Exception::class)
    fun importExperience(
        command: Command.ImportExperience
    ) {
        val request = Rpc.Object.ImportExperience.Request(
            spaceId = command.space.id,
            url = command.url,
            title = command.title,
            isNewSpace = command.isNewSpace
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectImportExperience(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun replyNotifications(notifications: List<Id>) {
        val request = Rpc.Notification.Reply.Request(ids = notifications)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.notificationReply(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun membershipStatus(command: Command.Membership.GetStatus): Membership? {
        val request = Rpc.Membership.GetStatus.Request(
            noCache = command.noCache
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipStatus(request) }
        logResponseIfDebug(response, time)
        return response.data_?.toCoreModel()
    }

    @Throws
    fun membershipIsNameValid(command: Command.Membership.IsNameValid) {
        val request = Rpc.Membership.IsNameValid.Request(
            requestedTier = command.tier,
            nsName = command.name,
            nsNameType = command.nameType.toMw()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipIsNameValid(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun membershipGetPaymentUrl(command: Command.Membership.GetPaymentUrl): GetPaymentUrlResponse {
        val request = Rpc.Membership.RegisterPaymentRequest.Request(
            requestedTier = command.tier,
            nsName = command.name,
            nsNameType = command.nameType.toMw(),
            paymentMethod = command.paymentMethod.toMw()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipRegisterPaymentRequest(request) }
        logResponseIfDebug(response, time)
        return GetPaymentUrlResponse(
            billingId = response.billingId
        )
    }

    @Throws
    fun membershipGetPortalLinkUrl(): String {
        val request = Rpc.Membership.GetPortalLinkUrl.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipGetPortalLinkUrl(request) }
        logResponseIfDebug(response, time)
        return response.portalUrl
    }

    @Throws
    fun membershipFinalize(command: Command.Membership.Finalize) {
        val request = Rpc.Membership.Finalize.Request(
            nsName = command.name,
            nsNameType = command.nameType.toMw(),
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipFinalize(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun membershipGetVerificationEmailStatus(): EmailVerificationStatus {
        val request = Rpc.Membership.GetVerificationEmailStatus.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipGetVerificationEmailStatus(request) }
        logResponseIfDebug(response, time)
        return response.status.toCoreModel()
    }

    @Throws
    fun membershipGetVerificationEmail(command: Command.Membership.GetVerificationEmail) {
        val request = Rpc.Membership.GetVerificationEmail.Request(
            email = command.email,
            subscribeToNewsletter = command.subscribeToNewsletter,
            isOnboardingList = command.isFromOnboarding
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipGetVerificationEmail(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun membershipVerifyEmailCode(command: Command.Membership.VerifyEmailCode) {
        val request = Rpc.Membership.VerifyEmailCode.Request(
            code = command.code
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipVerifyEmailCode(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun membershipGetTiers(command: Command.Membership.GetTiers): List<MembershipTierData> {
        val request = Rpc.Membership.GetTiers.Request(
            noCache = command.noCache,
            locale = command.locale
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.membershipGetTiers(request) }
        logResponseIfDebug(response, time)
        return response.tiers.map { it.toCoreModel() }
    }

    @Throws
    fun processCancel(command: Command.ProcessCancel) {
        val request = Rpc.Process.Cancel.Request(
            id = command.processId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.processCancel(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun getVersions(command: Command.VersionHistory.GetVersions): List<Version> {
        val request = Rpc.History.GetVersions.Request(
            lastVersionId = command.lastVersion.orEmpty(),
            objectId = command.objectId,
            limit = command.limit

        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.getVersions(request) }
        logResponseIfDebug(response, time)
        return response.versions.map { it.toCoreModel() }
    }

    @Throws
    fun showVersion(command: Command.VersionHistory.ShowVersion): ShowVersionResponse {
        val request = Rpc.History.ShowVersion.Request(
            objectId = command.objectId,
            versionId = command.versionId,
            traceId = command.traceId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.showVersion(request) }
        logResponseIfDebug(response, time)
        return response.toCoreModel()
    }

    @Throws
    fun setVersion(command: Command.VersionHistory.SetVersion) {
        val request = Rpc.History.SetVersion.Request(
            objectId = command.objectId,
            versionId = command.versionId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.setVersion(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun diffVersions(command: Command.VersionHistory.DiffVersions): DiffVersionResponse {
        val request = Rpc.History.DiffVersions.Request(
            objectId = command.objectId,
            spaceId = command.spaceId,
            currentVersion = command.currentVersion,
            previousVersion = command.previousVersion
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.diffVersions(request) }
        logResponseIfDebug(response, time)
        return response.toCoreModel(context = command.objectId)
    }

    @Throws
    fun chatAddMessage(command: Command.ChatCommand.AddMessage) : Pair<Id, List<Event.Command.Chats>> {
        val request = Rpc.Chat.AddMessage.Request(
            chatObjectId = command.chat,
            message = command.message.mw()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatAddMessage(request) }
        logResponseIfDebug(response, time)
        val events = response
            .event
            ?.messages
            ?.mapNotNull { msg ->
                msg.payload(contextId = command.chat)
            }
            .orEmpty()
        return response.messageId to events
    }

    @Throws
    fun chatEditMessageContent(command: Command.ChatCommand.EditMessage) {
        val request = Rpc.Chat.EditMessageContent.Request(
            chatObjectId = command.chat,
            messageId = command.message.id,
            editedMessage = command.message.mw()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatEditMessage(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun chatGetMessages(command: Command.ChatCommand.GetMessages) : Command.ChatCommand.GetMessages.Response {
        val request = Rpc.Chat.GetMessages.Request(
            chatObjectId = command.chat,
            beforeOrderId = command.beforeOrderId.orEmpty(),
            afterOrderId = command.afterOrderId.orEmpty(),
            limit = command.limit,
            includeBoundary = command.includeBoundary
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatGetMessages(request) }
        logResponseIfDebug(response, time)
        return Command.ChatCommand.GetMessages.Response(
            messages = response.messages.map { it.core() },
            state = response.chatState?.core()
        )
    }

    @Throws
    fun chatGetMessagesByIds(command: Command.ChatCommand.GetMessagesByIds) : List<Chat.Message> {
        val request = Rpc.Chat.GetMessagesByIds.Request(
            chatObjectId = command.chat,
            messageIds = command.messages
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatGetMessagesByIds(request) }
        logResponseIfDebug(response, time)
        return response.messages.map { it.core() }
    }

    @Throws
    fun chatReadMessages(command: Command.ChatCommand.ReadMessages) {
        val request = Rpc.Chat.ReadMessages.Request(
            chatObjectId = command.chat,
            afterOrderId = command.afterOrderId.orEmpty(),
            beforeOrderId = command.beforeOrderId.orEmpty(),
            lastStateId = command.lastStateId.orEmpty(),
            type = if (command.isMention) ReadType.Mentions else ReadType.Messages
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatReadMessages(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun chatReadAllMessages() {
        val request = Rpc.Chat.ReadAll.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatReadAll(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun chatDeleteMessage(command: Command.ChatCommand.DeleteMessage) {
        val request = Rpc.Chat.DeleteMessage.Request(
            chatObjectId = command.chat,
            messageId = command.msg
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatDeleteMessage(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun chatSubscribeLastMessages(
        command: Command.ChatCommand.SubscribeLastMessages
    ): Command.ChatCommand.SubscribeLastMessages.Response {
        val request = Rpc.Chat.SubscribeLastMessages.Request(
            chatObjectId = command.chat,
            limit = command.limit,
            subId = command.chat
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatSubscribeLastMessages(request) }
        logResponseIfDebug(response, time)
        return Command.ChatCommand.SubscribeLastMessages.Response(
            messages = response.messages.map { it.core() },
            messageCountBefore = response.numMessagesBefore,
            chatState = response.chatState?.core()
        )
    }

    @Throws
    fun chatToggleMessageReaction(
        command: Command.ChatCommand.ToggleMessageReaction
    ) {
        val request = Rpc.Chat.ToggleMessageReaction.Request(
            chatObjectId = command.chat,
            messageId = command.msg,
            emoji = command.emoji
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatToggleMessageReaction(request) }
        logResponseIfDebug(response, time)
    }

    @Throws
    fun dataViewSetActiveView(command: Command.DataViewSetActiveView): Payload {
        val request = Rpc.BlockDataview.View.SetActive.Request(
            contextId = command.ctx,
            blockId = command.dataViewId,
            viewId = command.viewerId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewSetActiveView(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun chatUnsubscribe(chat: Id) {
        val request = Rpc.Chat.Unsubscribe.Request(chatObjectId = chat)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatUnsubscribe(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun chatSubscribeToMessagePreviews(subscription: Id): List<Chat.Preview> {
        val request = Rpc.Chat.SubscribeToMessagePreviews.Request(subId = subscription)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatSubscribeToMessagePreviews(request) }
        logResponseIfDebug(response, time)
        return response.previews.map { it.core() }
    }

    @Throws(Exception::class)
    fun chatUnsubscribeFromMessagePreviews(subscription: Id) {
        val request = Rpc.Chat.UnsubscribeFromMessagePreviews.Request(subId = subscription)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.chatUnsubscribeToMessagePreviews(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun debugAccountSelectTrace(dir: String): String {
        val request = Rpc.Debug.AccountSelectTrace.Request(dir = dir)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugAccountSelectTrace(request) }
        logResponseIfDebug(response, time)
        return response.path
    }

    @Throws(Exception::class)
    fun objectDateByTimestamp(command: Command.ObjectDateByTimestamp): Struct? {
        val request = Rpc.Object.DateByTimestamp.Request(
            timestamp = command.timeInSeconds,
            spaceId = command.space.id
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectDateByTimestamp(request) }
        logResponseIfDebug(response, time)
        return response.details
    }

    @Throws(Exception::class)
    fun setDeviceNetworkState(type: DeviceNetworkType) {
        val request = Rpc.Device.NetworkState.Set.Request(
            deviceNetworkType = type.mw()
        )
        //logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.deviceNetworkStateSet(request) }
        //logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun debugStats(): String {
        val request = Rpc.Debug.Stat.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugStats(request) }
        logResponseIfDebug(response, time)
        return response.jsonStat
    }

    @Throws(Exception::class)
    fun debugExportLogs(dir: String): String {
        val request = Rpc.Debug.ExportLog.Request(dir = dir)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.debugExportLogs(request) }
        logResponseIfDebug(response, time)
        return response.path
    }

    @Throws(Exception::class)
    fun objectTypeListConflictingRelations(command: ObjectTypeConflictingFields): List<Id> {
        val request = Rpc.ObjectType.ListConflictingRelations.Request(
            spaceId = command.spaceId,
            typeObjectId = command.objectTypeId
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectTypeListConflictingRelations(request) }
        logResponseIfDebug(response, time)
        return response.relationIds
    }

    @Throws(Exception::class)
    fun objectTypeSetRecommendedHeaderFields(command: Command.ObjectTypeSetRecommendedHeaderFields) {
        val request = Rpc.ObjectType.Recommended.FeaturedRelationsSet.Request(
            typeObjectId = command.objectTypeId,
            relationObjectIds = command.fields
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectTypeHeaderRecommendedFieldsSet(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun objectTypeSetRecommendedFields(command: Command.ObjectTypeSetRecommendedFields) {
        val request = Rpc.ObjectType.Recommended.RelationsSet.Request(
            typeObjectId = command.objectTypeId,
            relationObjectIds = command.fields
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectTypeRecommendedFieldsSet(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun setDataViewProperties(
        command: Command.SetDataViewProperties
    ): Payload {
        val request = Rpc.BlockDataview.Relation.Set.Request(
            contextId = command.objectId,
            blockId = command.blockId,
            relationKeys = command.properties
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.blockDataViewRelationSet(request) }
        logResponseIfDebug(response, time)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun registerDeviceToken(command: Command.RegisterDeviceToken) {
        val request = Rpc.PushNotification.RegisterToken.Request(
            token = command.token,
            platform = Platform.Android
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.pushNotificationRegisterToken(request) }
        logResponseIfDebug(response, time)
    }

    @Throws(Exception::class)
    fun getLinkPreview(url: Url): LinkPreview {
        val request = Rpc.LinkPreview.Request(url = url)
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.linkPreview(request) }
        logResponseIfDebug(response, time)
        return response.linkPreview?.toCoreLinkPreview() ?: throw Exception("MW return empty link preview")
    }

    @Throws(Exception::class)
    fun createObjectFromUrl(space: SpaceId, url: Url) : ObjectWrapper.Basic {
        val request = Rpc.Object.CreateFromUrl.Request(
            url = url,
            spaceId = space.id,
            objectTypeUniqueKey = ObjectTypeUniqueKeys.BOOKMARK
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.objectCreateFromUrl(request) }
        logResponseIfDebug(response, time)
        return ObjectWrapper.Basic(response.details.orEmpty())
    }

    @Throws(Exception::class)
    fun setSpaceMode(spaceViewId: Id, mode: NotificationState): Rpc.PushNotification.SetSpaceMode.Response {
        val request = Rpc.PushNotification.SetSpaceMode.Request(
            spaceId = spaceViewId,
            mode = mode.toMiddlewareModel()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.setSpaceMode(request) }
        logResponseIfDebug(response, time)
        return response
    }

    @Throws(Exception::class)
    fun spaceChangeInvite(
        command: Command.SpaceChangeInvite
    ) {
        val request = Rpc.Space.InviteChange.Request(
            spaceId = command.space.id,
            permissions = command.permissions.toMw()
        )
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue { service.spaceChangeInvite(request) }
        logResponseIfDebug(response, time)
    }

    private fun logRequestIfDebug(request: Any) {
        if (BuildConfig.DEBUG) {
            logger.logRequest(request).also {
                if (BuildConfig.DEBUG && threadInfo.isOnMainThread()) {
                    Timber.w("Main thread is used for operation: ${request::class.qualifiedName}")
                }
            }
        }
    }

    private fun logResponseIfDebug(response: Any, time: Duration? = null) {
        if (BuildConfig.DEBUG) {
            logger.logResponse(response, time)
        }
    }
}