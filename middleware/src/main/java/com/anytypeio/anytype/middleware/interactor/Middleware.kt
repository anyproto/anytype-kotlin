package com.anytypeio.anytype.middleware.interactor

import anytype.Rpc
import anytype.model.Block
import anytype.model.InternalFlag
import anytype.model.ObjectInfo
import anytype.model.ObjectInfoWithLinks
import anytype.model.Range
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.CBTextStyle
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.middleware.BuildConfig
import com.anytypeio.anytype.middleware.auth.toAccountSetup
import com.anytypeio.anytype.middleware.const.Constants
import com.anytypeio.anytype.middleware.mappers.MObjectType
import com.anytypeio.anytype.middleware.mappers.MRelation
import com.anytypeio.anytype.middleware.mappers.MRelationOption
import com.anytypeio.anytype.middleware.mappers.core
import com.anytypeio.anytype.middleware.mappers.parse
import com.anytypeio.anytype.middleware.mappers.toCoreModels
import com.anytypeio.anytype.middleware.mappers.toMiddlewareModel
import com.anytypeio.anytype.middleware.mappers.toPayload
import com.anytypeio.anytype.middleware.model.CreateWalletResponse
import com.anytypeio.anytype.middleware.service.MiddlewareService
import timber.log.Timber

class Middleware(
    private val service: MiddlewareService,
    private val factory: MiddlewareFactory
) {

    @Throws(Exception::class)
    fun accountCreate(
        name: String,
        path: String?,
        invitationCode: String
    ): AccountSetup {
        val request = Rpc.Account.Create.Request(
            name = name,
            alphaInviteCode = invitationCode,
            avatarLocalPath = path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.toAccountSetup()
    }

    @Throws(Exception::class)
    fun accountDelete(): AccountStatus {
        val request = Rpc.Account.Delete.Request(
            revert = false
        )
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
        val request = Rpc.Account.Delete.Request(
            revert = true
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
        val status = response.status
        checkNotNull(status) { "Account status was null" }
        return status.core()
    }

    @Throws(Exception::class)
    fun accountSelect(id: String, path: String): AccountSetup {
        val request = Rpc.Account.Select.Request(
            id = id,
            rootPath = path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountSelect(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.toAccountSetup()
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
    fun blockDataViewRecordCreate(
        context: String,
        target: String,
        template: Id?,
        prefilled: Map<Id, Any>
    ): Map<String, Any?> {
        val request = Rpc.BlockDataviewRecord.Create.Request(
            contextId = context,
            blockId = target,
            templateId = template.orEmpty(),
            record = prefilled
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRecordCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.record?.toMap() ?: emptyMap()
    }

    @Throws(Exception::class)
    fun blockDataViewRecordRelationOptionAdd(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?> {
        val request = Rpc.BlockDataviewRecord.RelationOption.Add.Request(
            contextId = ctx,
            blockId = dataview,
            relationKey = relation,
            recordId = record,
            option = MRelationOption(text = name, color = color)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRecordRelationOptionAdd(request)
        val option = response.option?.id
        if (BuildConfig.DEBUG) logResponse(response)
        return Pair(response.event.toPayload(), option)
    }

    @Throws(Exception::class)
    fun blockDataViewRecordUpdate(
        context: String,
        target: String,
        record: String,
        values: Map<String, Any?>
    ) {
        val request = Rpc.BlockDataviewRecord.Update.Request(
            contextId = context,
            blockId = target,
            recordId = record,
            record = values
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRecordUpdate(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun blockDataViewRelationAdd(
        context: String,
        target: String,
        name: String,
        format: Relation.Format,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload> {
        val relation = MRelation(
            name = name,
            format = format.toMiddlewareModel(),
            objectTypes = limitObjectTypes
        )
        val request = Rpc.BlockDataview.Relation.Add.Request(
            contextId = context,
            blockId = target,
            relation = relation
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRelationAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return Pair(response.relationKey, response.event.toPayload())
    }

    @Throws(Exception::class)
    fun blockDataViewRelationAdd(ctx: Id, dv: Id, relation: Id): Payload {
        val request = Rpc.BlockDataview.Relation.Add.Request(
            contextId = ctx,
            blockId = dv,
            relation = MRelation(
                key = relation
            )
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
            relationKey = relation
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
    ): Payload {
        val request = Rpc.BlockDataview.View.Create.Request(
            contextId = context,
            blockId = target,
            view = viewer.toMiddlewareModel()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewViewCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
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
    fun blockLinkCreateWithObject(
        ctx: Id?,
        emoji: String?,
        isDraft: Boolean?,
        type: String?,
        template: Id?
    ): Id {
        val details: MutableMap<String, Any> = mutableMapOf()
        emoji?.let { details[Relations.ICON_EMOJI] = it }
        isDraft?.let { details[Relations.IS_DRAFT] = it }
        type?.let { details[Relations.TYPE] = it }

        val flags = buildList {
            if (isDraft == true)
                add(InternalFlag(InternalFlag.Value.editorDeleteEmpty))
        }

        val request = Rpc.BlockLink.CreateWithObject.Request(
            contextId = ctx.orEmpty(),
            details = details,
            position = Block.Position.Inner,
            templateId = template.orEmpty(),
            internalFlags = flags
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockLinkCreateWithObject(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.targetId
    }

    @Throws(Exception::class)
    fun blockLinkCreateWithObject(command: Command.CreateDocument): Triple<String, String, Payload> {

        val details: MutableMap<String, Any> = mutableMapOf()

        command.emoji?.let { details[Relations.ICON_EMOJI] = it }
        command.type?.let { details[Relations.TYPE] = it }
        command.layout?.let { details[Relations.LAYOUT] = it.toMiddlewareModel().value.toDouble() }

        val position: Block.Position = command.position.toMiddlewareModel()

        val flags = buildList<InternalFlag> {
            // Add flags when needed
        }

        val request = Rpc.BlockLink.CreateWithObject.Request(
            contextId = command.context,
            targetId = command.target,
            position = position,
            details = details,
            templateId = command.template.orEmpty(),
            internalFlags = flags
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockLinkCreateWithObject(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return Triple(
            response.blockId,
            response.targetId,
            response.event.toPayload()
        )
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
        val request = Rpc.BlockRelation.Add.Request(
            contextId = command.contextId,
            blockId = command.blockId,
            relation = null
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockRelationAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
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
            Command.SetTextIcon.Icon.None -> "" to ""
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
            relations = content.relations.map { it.toMiddlewareModel() }
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
        val request = Rpc.Debug.ExportLocalstore.Request(path = path)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugExportLocalStore(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.path
    }

    @Throws(Exception::class)
    fun debugSync(): String {
        val request = Rpc.Debug.Sync.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugSync(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.toString()
    }

    @Throws(Exception::class)
    fun fileListOffload() {
        val request = Rpc.File.ListOffload.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.fileListOffload(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun fileUpload(command: Command.UploadFile): Response.Media.Upload {
        val type = command.type.toMiddlewareModel()
        val request = Rpc.File.Upload.Request(
            localPath = command.path,
            type = type
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.fileUpload(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return Response.Media.Upload(response.hash)
    }

    @Throws(Exception::class)
    fun fileDownload(command: Command.DownloadFile): Rpc.File.Download.Response {
        val request = Rpc.File.Download.Request(
            hash = command.hash,
            path = command.path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.fileDownload(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response
    }

    @Throws(Exception::class)
    fun getConfig(): Config {
        TODO()
//        val request = Rpc.Config.Get.Request()
//
//        if (BuildConfig.DEBUG) logRequest(request)
//
//        val response = service.configGet(request)
//
//        if (BuildConfig.DEBUG) logResponse(response)
//
//        return Config(
//            home = response.homeBlockId,
//            profile = response.profileBlockId,
//            gateway = response.gatewayUrl
//        )
    }

    @Throws(Exception::class)
    fun navigationGetObjectInfoWithLinks(pageId: String): ObjectInfoWithLinks {
        val request = Rpc.Navigation.GetObjectInfoWithLinks.Request(
            objectId = pageId
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.navigationGetObjectInfoWithLinks(request)
        if (BuildConfig.DEBUG) logResponse(response)

        val info = response.object_

        checkNotNull(info) { "Empty result" }

        return info
    }

    @Throws(Exception::class)
    fun navigationListObjects(): List<ObjectInfo> {
        val request = Rpc.Navigation.ListObjects.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.navigationListObjects(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.objects
    }

    @Throws(Exception::class)
    fun objectApplyTemplate(
        ctx: Id,
        template: Id
    ) {
        val request = Rpc.Object.ApplyTemplate.Request(
            contextId = ctx,
            templateId = template
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
    fun objectCreate(command: Command.CreateNewDocument): Id {
        val details: Map<String, Any> = buildMap {
            command.emoji?.let { put(Relations.ICON_EMOJI, it) }
            command.name.let { put(Relations.NAME, it) }
            command.type?.let { put(Relations.TYPE, it) }
        }
        val flags = buildList<InternalFlag> {

        }
        val request = Rpc.Object.Create.Request(
            details = details.toMap(),
            internalFlags = flags
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.pageId
    }

    @Throws(Exception::class)
    fun objectCreateBookmark(url: Url): Id {
        val request = Rpc.Object.CreateBookmark.Request(url = url)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectCreateBookmark(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.pageId
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
    fun objectCreateSet(
        contextId: String,
        targetId: String?,
        position: Position?,
        objectType: String?
    ): Response.Set.Create {

        val source = if (objectType != null) {
            listOf(objectType)
        } else {
            listOf()
        }

        val request = Rpc.Object.CreateSet.Request(
            source = source
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.objectCreateSet(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return Response.Set.Create(
            targetId = response.id,
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
    fun objectOpen(id: String): Payload {
        val request = Rpc.Object.Open.Request(objectId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.objectView?.toPayload()
            ?: throw IllegalStateException("Object view was null")
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
    fun objectRelationAdd(
        ctx: Id,
        format: RelationFormat,
        name: String,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload> {
        val request = Rpc.ObjectRelation.Add.Request(
            contextId = ctx,
            relation = MRelation(
                format = format.toMiddlewareModel(),
                name = name,
                objectTypes = limitObjectTypes
            )
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return Pair(
            first = response.relation?.key.orEmpty(),
            second = response.event.toPayload()
        )
    }

    @Throws(Exception::class)
    fun objectRelationAdd(ctx: Id, relation: Id): Payload {
        val request = Rpc.ObjectRelation.Add.Request(
            contextId = ctx,
            relation = MRelation(
                key = relation
            )
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
    fun objectRelationDelete(ctx: Id, relation: Id): Payload {
        val request = Rpc.ObjectRelation.Delete.Request(
            contextId = ctx,
            relationKey = relation
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun objectRelationListAvailable(ctx: Id): List<MRelation> {
        val request = Rpc.ObjectRelation.ListAvailable.Request(
            contextId = ctx
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationListAvailable(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.relations
    }

    @Throws(Exception::class)
    fun objectRelationOptionAdd(
        ctx: Id,
        relation: Id,
        name: Id,
        color: String
    ): Pair<Payload, Id?> {
        val request = Rpc.ObjectRelationOption.Add.Request(
            contextId = ctx,
            relationKey = relation,
            option = MRelationOption(text = name, color = color)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationOptionAdd(request)
        val option = response.option?.id
        if (BuildConfig.DEBUG) logResponse(response)
        return Pair(response.event.toPayload(), option)
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
            source = source
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
    fun objectSetDetails(
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
            value_ = command.hash
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

    @Throws(Exception::class)
    fun objectSetIsArchived(
        ctx: Id,
        isArchived: Boolean
    ): Payload {
        val request = Rpc.Object.SetIsArchived.Request(
            contextId = ctx,
            isArchived = isArchived
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetIsArchived(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

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
    fun objectSetObjectType(ctx: Id, typeId: Id): Payload {
        val request = Rpc.Object.SetObjectType.Request(
            contextId = ctx,
            objectTypeUrl = typeId
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
    fun objectShow(id: String): Payload {
        val request = Rpc.Object.Show.Request(objectId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectShow(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectView?.toPayload()
            ?: throw IllegalStateException("Object view was null")
    }

    @Throws(Exception::class)
    fun objectTypeCreate(prototype: ObjectType.Prototype): MObjectType {

        val layout = prototype.layout.toMiddlewareModel()

        val objectType = MObjectType(
            name = prototype.name,
            iconEmoji = prototype.emoji,
            layout = layout
        )

        val request = Rpc.ObjectType.Create.Request(
            objectType = objectType
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.objectTypeCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

        val result = response.objectType

        checkNotNull(result) { "Empty result" }

        return result
    }

    @Throws(Exception::class)
    fun objectTypeList(): List<MObjectType> {
        val request = Rpc.ObjectType.List.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectTypeList(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectTypes
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
    fun versionGet(): Rpc.App.GetVersion.Response {
        val request = Rpc.App.GetVersion.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.versionGet(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response
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
        rowCount: Int,
        columnCount: Int
    ): Payload {
        val request = Rpc.BlockTable.Create.Request(
            contextId = ctx,
            targetId = target,
            position = position.toMiddlewareModel(),
            rows = rowCount,
            columns = columnCount
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
    fun objectToSet(ctx: String, source: List<String>): String {
        val request = Rpc.Object.ToSet.Request(
            contextId = ctx,
            source = source
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectToSet(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.setId
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

    private fun logRequest(any: Any) {
        val message = "===> " + any::class.java.canonicalName + ":" + "\n" + any.toString()
        Timber.d(message)
    }

    private fun logResponse(any: Any) {
        val message = "<=== " + any::class.java.canonicalName + ":" + "\n" + any.toString()
        Timber.d(message)
    }
}