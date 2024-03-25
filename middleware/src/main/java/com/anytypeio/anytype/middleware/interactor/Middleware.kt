package com.anytypeio.anytype.middleware.interactor

import anytype.Rpc
import anytype.model.Block
import anytype.model.ParticipantPermissionChange
import anytype.model.Range
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.CBTextStyle
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.NodeUsageInfo
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.tools.ThreadInfo
import com.anytypeio.anytype.middleware.BuildConfig
import com.anytypeio.anytype.middleware.auth.toAccountSetup
import com.anytypeio.anytype.middleware.const.Constants
import com.anytypeio.anytype.middleware.mappers.MDVFilter
import com.anytypeio.anytype.middleware.mappers.MNetworkMode
import com.anytypeio.anytype.middleware.mappers.MRelationFormat
import com.anytypeio.anytype.middleware.mappers.config
import com.anytypeio.anytype.middleware.mappers.core
import com.anytypeio.anytype.middleware.mappers.mw
import com.anytypeio.anytype.middleware.mappers.parse
import com.anytypeio.anytype.middleware.mappers.toCore
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import com.anytypeio.anytype.middleware.mappers.toCoreModels
import com.anytypeio.anytype.middleware.mappers.toMiddlewareModel
import com.anytypeio.anytype.middleware.mappers.toMw
import com.anytypeio.anytype.middleware.mappers.toPayload
import com.anytypeio.anytype.middleware.model.CreateWalletResponse
import com.anytypeio.anytype.middleware.service.MiddlewareService
import javax.inject.Inject
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
            networkCustomConfigFilePath = command.networkConfigFilePath.orEmpty(),
            preferYamuxTransport = command.preferYamuxTransport ?: false
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.toAccountSetup()
    }

    @Throws(Exception::class)
    fun accountSelect(command: Command.AccountSelect): AccountSetup {

        val networkMode = command.networkMode?.toMiddlewareModel() ?: MNetworkMode.DefaultConfig
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountSelect(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.toAccountSetup()
    }

    @Throws(Exception::class)
    fun accountDelete(): AccountStatus {
        val request = Rpc.Account.Delete.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
        val status = response.status
        checkNotNull(status) { "Account status was null" }
        return status.core()
    }

    @Throws(Exception::class)
    fun accountRecover() {
        val request = Rpc.Account.Recover.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountRecover(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun accountRestore(): AccountStatus {
        val request = Rpc.Account.RevertDeletion.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountRevertDeletion(request)
        if (BuildConfig.DEBUG) logResponse(response)
        val status = response.status
        checkNotNull(status) { "Account status was null" }
        return status.core()
    }

    @Throws(Exception::class)
    fun accountStop(clearLocalRepositoryData: Boolean) {
        val request: Rpc.Account.Stop.Request = Rpc.Account.Stop.Request(
            removeData = clearLocalRepositoryData
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountStop(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun blockBookmarkCreateAndFetch(command: Command.CreateBookmark): Payload {
        val request = Rpc.BlockBookmark.CreateAndFetch.Request(
            contextId = command.context,
            targetId = command.target,
            url = command.url,
            position = command.position.toMiddlewareModel()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockBookmarkCreateAndFetch(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockBookmarkFetch(command: Command.SetupBookmark): Payload {
        val request = Rpc.BlockBookmark.Fetch.Request(
            contextId = command.context,
            blockId = command.target,
            url = command.url
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockBookmarkFetch(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockCopy(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return Pair(response.blockId, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockDataViewActiveSet(
        contextId: String,
        blockId: String,
        viewId: String,
        offset: Int,
        limit: Int
    ): Payload {
        val request = Rpc.BlockDataview.View.SetActive.Request(
            contextId = contextId,
            blockId = blockId,
            viewId = viewId,
            offset = offset,
            limit = limit
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewActiveSet(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewViewSetPosition(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRelationAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockDataViewRelationDelete(ctx: Id, dv: Id, relation: Id): Payload {
        val request = Rpc.BlockDataview.Relation.Delete.Request(
            contextId = ctx,
            blockId = dv,
            relationKeys = listOf(relation)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRelationDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewViewCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewViewCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewViewDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewViewUpdate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListDelete(command: Command.Unlink): Payload {
        val request = Rpc.Block.ListDelete.Request(
            contextId = command.context,
            blockIds = command.targets
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListDuplicate(request)
        if (BuildConfig.DEBUG) logResponse(response)

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

        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListMoveToExistingObject(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListMoveToNewObject(command: Command.TurnIntoDocument): List<String> {
        val request = Rpc.Block.ListMoveToNewObject.Request(
            contextId = command.context,
            blockIds = command.targets
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListMoveToNewObject(request)
        if (BuildConfig.DEBUG) logResponse(response)
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

        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListSetAlign(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListSetBackgroundColor(command: Command.UpdateBackgroundColor): Payload {
        val request = Rpc.Block.ListSetBackgroundColor.Request(
            contextId = command.context,
            blockIds = command.targets,
            color = command.color
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListSetBackgroundColor(request)
        if (BuildConfig.DEBUG) logResponse(response)

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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListSetDivStyle(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListSetFields(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListTurnInto(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockMerge(command: Command.Merge): Payload {
        val request = Rpc.Block.Merge.Request(
            contextId = command.context,
            firstBlockId = command.pair.first,
            secondBlockId = command.pair.second
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockMerge(request)
        if (BuildConfig.DEBUG) logResponse(response)

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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockPaste(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockRelationSetKey(request)
        if (BuildConfig.DEBUG) logResponse(response)
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

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

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

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockSplit(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return Pair(response.blockId, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockTextListSetColor(command: Command.UpdateTextColor): Payload {
        val request = Rpc.BlockText.ListSetColor.Request(
            contextId = command.context,
            color = command.color,
            blockIds = command.targets
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTextListSetColor(request)
        if (BuildConfig.DEBUG) logResponse(response)

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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTextListSetMark(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTextListSetStyle(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTextSetIcon(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTextSetChecked(request)
        if (BuildConfig.DEBUG) logResponse(response)

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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTextSetText(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockLinkListSetAppearance(
            request
        )
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockUpload(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    private val coverIdKey = "coverId"
    private val coverTypeKey = "coverType"

    @Deprecated("Should deleted. Use objectOpen()")
    @Throws(Exception::class)
    fun dashboardOpen(contextId: String, id: String): Payload {
        val request: Rpc.Object.Open.Request = Rpc.Object.Open.Request(
            contextId = contextId,
            objectId = id
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.objectView?.toPayload()
            ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun debugExportLocalStore(path: String): String {
        val request = Rpc.Debug.ExportLocalstore.Request(
            path = path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugExportLocalStore(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.path
    }

    @Throws(Exception::class)
    fun debugSpaceSummary(space: SpaceId): String {
        val request = Rpc.Debug.SpaceSummary.Request(spaceId = space.id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugSpaceSummary(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.infos.toCoreModel()
    }

    @Throws(Exception::class)
    fun debugSubscriptions(): List<Id> {
        val request = Rpc.Debug.Subscriptions.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugSubscriptions(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.subscriptions
    }

    @Throws(Exception::class)
    fun debugObject(objectId: Id, path: String): String {
        val request = Rpc.Debug.Tree.Request(
            treeId = objectId,
            path = path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugObject(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.filename
    }

    @Throws(Exception::class)
    fun fileListOffload() {
        val request = Rpc.File.ListOffload.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.fileListOffload(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun fileUpload(command: Command.UploadFile): ObjectWrapper.File {
        val type = command.type.toMiddlewareModel()
        val request = Rpc.File.Upload.Request(
            localPath = command.path,
            type = type,
            spaceId = command.space?.id.orEmpty()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.fileUpload(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return ObjectWrapper.File(response.details.orEmpty())
    }

    @Throws(Exception::class)
    fun fileDownload(command: Command.DownloadFile): Rpc.File.Download.Response {
        val request = Rpc.File.Download.Request(
            objectId = command.objectId,
            path = command.path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.fileDownload(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectApplyTemplate(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun objectClose(id: String) {
        val request = Rpc.Object.Close.Request(objectId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectClose(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
            objectTypeUniqueKey = command.typeKey.key
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
            spaceId = command.space
        )

        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockLinkCreateWithObject(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectCreateBookmark(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectId
    }

    @Throws(Exception::class)
    fun objectBookmarkFetch(ctx: Id, url: Url) {
        val request = Rpc.Object.BookmarkFetch.Request(
            contextId = ctx,
            url = url
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectBookmarkFetch(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectCreateRelation(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return ObjectWrapper.Relation(
            response.details ?: throw IllegalStateException("Missing details")
        )
    }

    @Throws(Exception::class)
    fun objectCreateObjectType(
        space: Id,
        name: String,
        emojiUnicode: String?
    ): Struct {
        val request = Rpc.Object.CreateObjectType.Request(
            details = buildMap {
                put(Relations.NAME, name)
                emojiUnicode?.let {
                    put(Relations.ICON_EMOJI, it)
                }
            },
            spaceId = space
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectCreateObjectType(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.details ?: throw IllegalStateException("Null object type struct")
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectCreateRelationOption(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return ObjectWrapper.Option(
            response.details ?: throw IllegalStateException("Missing details")
        )
    }

    @Throws(Exception::class)
    fun objectCreateSet(
        space: Id,
        objectType: String?
    ): Response.Set.Create {
        val source = if (objectType != null) {
            listOf(objectType)
        } else {
            listOf()
        }

        val request = Rpc.Object.CreateSet.Request(
            source = source,
            spaceId = space
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.objectCreateSet(request)

        if (BuildConfig.DEBUG) logResponse(response)
        return Response.Set.Create(
            targetId = response.objectId,
            payload = response.event.toPayload(),
            blockId = null
        )
    }

    @Throws(Exception::class)
    fun objectDuplicate(id: Id): Id {
        val request = Rpc.Object.Duplicate.Request(contextId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectDuplicate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.id
    }

    @Throws(Exception::class)
    fun objectIdsSubscribe(
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult {
        val request = Rpc.Object.SubscribeIds.Request(
            subId = subscription,
            keys = keys,
            ids = ids
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectIdsSubscribe(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectListDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectListSetIsArchived(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun objectOpenOld(id: String): Payload {
        val request = Rpc.Object.Open.Request(objectId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.objectView?.toPayload()
            ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectOpen(id: String): ObjectView {
        val request = Rpc.Object.Open.Request(objectId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectView?.toCore() ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectRedo(command: Command.Redo): Payload {
        val request = Rpc.Object.Redo.Request(contextId = command.context)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRedo(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRelationAdd(ctx: Id, relation: Key): Payload {
        val request = Rpc.ObjectRelation.Add.Request(
            contextId = ctx,
            relationKeys = listOf(relation)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationAddFeatured(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRelationDelete(ctx: Id, relation: Key): Payload {
        val request = Rpc.ObjectRelation.Delete.Request(
            contextId = ctx,
            relationKeys = listOf(relation)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationRemoveFeatured(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRemoveCover(ctx: String): Payload {
        val coverIdDetail = Rpc.Object.SetDetails.Detail(
            key = coverIdKey,
            value_ = null
        )
        val coverTypeDetail = Rpc.Object.SetDetails.Detail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_NONE.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRemoveIcon(ctx: String): Payload {
        val imageDetail = Rpc.Object.SetDetails.Detail(
            key = Relations.ICON_IMAGE,
            value_ = null
        )
        val emojiDetail = Rpc.Object.SetDetails.Detail(
            key = Relations.ICON_EMOJI,
            value_ = null
        )

        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(imageDetail, emojiDetail)
        )

        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSearch(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        keys: List<Id>
    ): List<Map<String, Any?>> {
        val request = Rpc.Object.Search.Request(
            sorts = sorts.map { it.toMiddlewareModel() },
            filters = filters.map { it.toMiddlewareModel() },
            fullText = fulltext,
            offset = offset,
            limit = limit,
            keys = keys
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSearch(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.records.map { it?.toMap() ?: emptyMap() }
    }

    @Throws(Exception::class)
    fun objectSearchSubscribe(
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
            subId = subscription,
            sorts = sorts.map { it.toMiddlewareModel() },
            filters = filters.map { it.toMiddlewareModel() },
            keys = keys,
            offset = offset,
            limit = limit.toLong(),
            beforeId = beforeId.orEmpty(),
            afterId = afterId.orEmpty(),
            source = source,
            ignoreWorkspace = ignoreWorkspace?.toString() ?: "",
            noDepSubscription = noDepSubscription ?: false,
            collectionId = collection.orEmpty()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSearchSubscribe(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSearchUnsubscribe(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun objectSetCoverColor(
        ctx: String,
        color: String
    ): Payload {
        val coverIdDetail = Rpc.Object.SetDetails.Detail(
            key = coverIdKey,
            value_ = color
        )
        val coverTypeDetail = Rpc.Object.SetDetails.Detail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_COLOR.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetCoverGradient(
        ctx: String,
        gradient: String
    ): Payload {
        val coverIdDetail = Rpc.Object.SetDetails.Detail(
            key = coverIdKey,
            value_ = gradient
        )
        val coverTypeDetail = Rpc.Object.SetDetails.Detail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_GRADIENT.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetCoverImage(
        ctx: String,
        hash: String
    ): Payload {
        val coverIdDetail = Rpc.Object.SetDetails.Detail(
            key = coverIdKey,
            value_ = hash
        )
        val coverTypeDetail = Rpc.Object.SetDetails.Detail(
            key = coverTypeKey,
            value_ = Constants.COVER_TYPE_UPLOADED_IMAGE.toDouble()
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setObjectDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload {
        val detail = Rpc.Object.SetDetails.Detail(
            key = key,
            value_ = value
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = listOf(detail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setObjectDetails(
        ctx: Id,
        details: Struct
    ): Payload {
        val detailsList = details.map { entry ->
            Rpc.Object.SetDetails.Detail(
                key = entry.key,
                value_ = entry.value
            )
        }
        val request = Rpc.Object.SetDetails.Request(
            contextId = ctx,
            details = detailsList
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload {

        val emojiDetail = Rpc.Object.SetDetails.Detail(
            key = Relations.ICON_EMOJI,
            value_ = command.emoji
        )

        val imageDetail = Rpc.Object.SetDetails.Detail(
            key = Relations.ICON_IMAGE,
            value_ = null
        )

        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(emojiDetail, imageDetail)
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.objectSetDetails(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetImageIcon(command: Command.SetDocumentImageIcon): Payload {

        val imageDetail = Rpc.Object.SetDetails.Detail(
            key = Relations.ICON_IMAGE,
            value_ = command.id
        )
        val emojiDetail = Rpc.Object.SetDetails.Detail(
            key = Relations.ICON_EMOJI,
            value_ = null
        )

        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(imageDetail, emojiDetail)
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.objectSetDetails(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Deprecated(
        "Use objectListSetIsArchived instead",
        replaceWith = ReplaceWith("objectListSetIsArchived")
    )
    @Throws(Exception::class)
    fun objectSetIsArchived(
        ctx: Id,
        isArchived: Boolean
    ) {
        val request = Rpc.Object.SetIsArchived.Request(
            contextId = ctx,
            isArchived = isArchived
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetIsArchived(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Deprecated(
        "Use objectListSetIsFavorite instead",
        replaceWith = ReplaceWith("objectListSetIsFavorite")
    )
    @Throws(Exception::class)
    fun objectSetIsFavorite(
        ctx: Id,
        isFavorite: Boolean
    ): Payload {
        val request = Rpc.Object.SetIsFavorite.Request(
            contextId = ctx,
            isFavorite = isFavorite
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetIsFavorite(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectListSetIsFavorite(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun objectSetLayout(ctx: Id, layout: ObjectType.Layout): Payload {
        val request = Rpc.Object.SetLayout.Request(
            contextId = ctx,
            layout = layout.toMiddlewareModel()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetLayout(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetObjectType(ctx: Id, objectTypeKey: Key): Payload {
        val request = Rpc.Object.SetObjectType.Request(
            contextId = ctx,
            objectTypeUniqueKey = objectTypeKey
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetObjectType(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectSetTitle(command: Command.UpdateTitle) {
        val detail = Rpc.Object.SetDetails.Detail(
            key = Relations.NAME,
            value_ = command.title
        )
        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(detail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun objectShowOld(id: String): Payload {
        val request = Rpc.Object.Show.Request(objectId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectShow(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectView?.toPayload()
            ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectShow(id: String): ObjectView {
        val request = Rpc.Object.Show.Request(objectId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectShow(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectView?.toCore() ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectUndo(command: Command.Undo): Payload {
        val request = Rpc.Object.Undo.Request(contextId = command.context)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectUndo(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectImportUseCaseGetStarted(space: Id) {
        val request = Rpc.Object.ImportUseCase.Request(
            spaceId = space,
            useCase = Rpc.Object.ImportUseCase.Request.UseCase.GET_STARTED
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectImportUseCase(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun versionGet(): Rpc.App.GetVersion.Response {
        val request = Rpc.App.GetVersion.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.versionGet(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response
    }

    @Throws(Exception::class)
    fun metricsSetParameters(
        platform: String,
        version: String
    ) {
        val request = Rpc.Metrics.SetParameters.Request(
            platform = platform,
            version = version
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.metricsSetParameters(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun walletConvert(entropy: String): String {
        val request = Rpc.Wallet.Convert.Request(entropy = entropy)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.walletConvert(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.mnemonic
    }

    @Throws(Exception::class)
    fun walletCreate(path: String): CreateWalletResponse {

        val request = Rpc.Wallet.Create.Request(rootPath = path)

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.walletCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return CreateWalletResponse(response.mnemonic)
    }

    @Throws(Exception::class)
    fun walletRecover(path: String, mnemonic: String) {
        val request = Rpc.Wallet.Recover.Request(
            mnemonic = mnemonic,
            rootPath = path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.walletRecover(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.createTable(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun fillTableRow(ctx: String, targetIds: List<String>): Payload {
        val request = Rpc.BlockTable.RowListFill.Request(
            contextId = ctx,
            blockIds = targetIds
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableRowListFill(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectToSet(ctx: String, source: List<String>) {
        val request = Rpc.Object.ToSet.Request(
            contextId = ctx,
            source = source
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectToSet(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun objectToCollection(ctx: Id) {
        val request = Rpc.Object.ToCollection.Request(
            contextId = ctx
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectToCollection(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewSetSource(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListClearContent(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListClearStyle(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableColumnListFill(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableRowCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableRowSetHeader(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableColumnCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableColumnDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableRowDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableColumnDuplicate(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableRowDuplicate(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableSort(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableExpand(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockTableColumnMove(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun spaceDelete(space: SpaceId) {
        val request = Rpc.Space.Delete.Request(spaceId = space.id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun workspaceCreate(details: Struct): Id {
        val request = Rpc.Workspace.Create.Request(
            details = details,
            useCase = Rpc.Object.ImportUseCase.Request.UseCase.EMPTY
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.workspaceCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.spaceId
    }

    @Throws(Exception::class)
    fun workspaceOpen(space: Id): Config {
        val request = Rpc.Workspace.Open.Request(
            spaceId = space
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.workspaceOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.workspaceSetInfo(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun workspaceObjectListAdd(objects: List<Id>, space: Id): List<Id> {
        val request = Rpc.Workspace.Object.ListAdd.Request(
            objectIds = objects,
            spaceId = space
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.workspaceObjectListAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectIds
    }

    @Throws(Exception::class)
    fun workspaceObjectAdd(command: Command.AddObjectToSpace): Pair<Id, Struct?> {
        val request = Rpc.Workspace.Object.Add.Request(
            objectId = command.objectId,
            spaceId = command.space
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.workspaceObjectAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return Pair(response.objectId, response.details)
    }

    @Throws(Exception::class)
    fun workspaceObjectListRemove(objects: List<Id>): List<Id> {
        val request = Rpc.Workspace.Object.ListRemove.Request(
            objectIds = objects
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.workspaceObjectListRemove(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockCreateWidget(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockWidgetSetViewId(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockCreateWidget(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewAddFilter(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewReplaceFilter(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRemoveFilter(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewAddSort(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewReplaceSort(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRemoveSort(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun addDataViewViewRelation(
        command: Command.AddRelation
    ): Payload {
        val request = Rpc.BlockDataview.ViewRelation.Add.Request(
            contextId = command.ctx,
            blockId = command.dv,
            viewId = command.view,
            relation = command.relation.toMiddlewareModel()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewAddViewRelation(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewReplaceViewRelation(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRemoveViewRelation(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewSortViewRelation(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.setInternalFlags(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun addObjectToCollection(command: Command.AddObjectToCollection): Payload {
        val request = Rpc.ObjectCollection.Add.Request(
            contextId = command.ctx,
            afterId = command.afterId,
            objectIds = command.ids
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.addObjectToCollection(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun setQueryToSet(command: Command.SetQueryToSet): Payload {
        val request = Rpc.Object.SetSource.Request(
            contextId = command.ctx,
            source = listOf(command.query)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.setObjectSource(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun nodeUsage(): NodeUsageInfo {
        val request = Rpc.File.NodeUsage.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.nodeUsageInfo(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.toCoreModel()
    }

    @Throws(Exception::class)
    fun duplicateObjectsList(
        objects: List<Id>
    ): List<Id> {
        val request = Rpc.Object.ListDuplicate.Request(
            objectIds = objects
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectsListDuplicate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.ids
    }

    @Throws(Exception::class)
    fun createTemplateFromObject(
        ctx: Id
    ): Id {
        val request = Rpc.Template.CreateFromObject.Request(
            contextId = ctx
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.createTemplateFromObject(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.id
    }

    @Throws
    fun deleteRelationOptions(
        command: Command.DeleteRelationOptions
    ) {
        val request = Rpc.Relation.ListRemoveOption.Request(
            optionIds = command.optionIds
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.deleteRelationOptions(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun debugStackGoroutines(path: String) {
        val request = Rpc.Debug.StackGoroutines.Request(
            path = path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugStackGoroutines(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun generateSpaceInviteLink(space: SpaceId) : SpaceInviteLink {
        val request = Rpc.Space.InviteGenerate.Request(
            spaceId = space.id
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceInviteGenerate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return SpaceInviteLink(
            contentId = response.inviteCid,
            fileKey= response.inviteFileKey
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceRequestApprove(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceRequestDecline(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceParticipantRemove(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceParticipantPermissionsChange(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun sendJoinSpaceRequest(command: Command.SendJoinSpaceRequest) {
        val request = Rpc.Space.Join.Request(
            networkId = command.network.orEmpty(),
            inviteCid = command.inviteContentId,
            inviteFileKey = command.inviteFileKey,
            spaceId = command.space.id
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceJoin(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceInviteView(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return SpaceInviteView(
            space = SpaceId(response.spaceId),
            creatorName = response.creatorName,
            spaceName = response.spaceName,
            spaceIconContentId = response.spaceIconCid
        )
    }

    @Throws(Exception::class)
    fun stopSharingSpace(space: SpaceId) {
        val request = Rpc.Space.StopSharing.Request(
            spaceId = space.id
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceStopSharing(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun getCurrentSpaceInvite(space: SpaceId): SpaceInviteLink {
        val request = Rpc.Space.InviteGetCurrent.Request(
            spaceId = space.id
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.spaceInviteGetCurrent(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return SpaceInviteLink(
            fileKey = response.inviteFileKey,
            contentId = response.inviteCid
        )
    }

    @Throws(Exception::class)
    fun downloadGalleryManifest(command: Command.DownloadGalleryManifest): ManifestInfo? {
        val request = Rpc.Gallery.DownloadManifest.Request(
            url = command.url
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.downloadManifest(request)
        if (BuildConfig.DEBUG) logResponse(response)
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
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectImportExperience(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    private fun logRequest(any: Any) {
        logger.logRequest(any).also {
            if (BuildConfig.DEBUG && threadInfo.isOnMainThread()) {
                Timber.w("Main thread is used for operation: ${any::class.qualifiedName}")
            }
        }
    }

    private fun logResponse(any: Any) {
        logger.logResponse(any)
    }
}