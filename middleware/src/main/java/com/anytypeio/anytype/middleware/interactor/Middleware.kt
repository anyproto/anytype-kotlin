package com.anytypeio.anytype.middleware.interactor

import anytype.Rpc
import anytype.Rpc.BlockList
import anytype.Rpc.BlockList.Set.Fields.Request.BlockField
import anytype.model.Block
import anytype.model.ObjectInfo
import anytype.model.ObjectInfoWithLinks
import anytype.model.Range
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.middleware.BuildConfig
import com.anytypeio.anytype.middleware.const.Constants
import com.anytypeio.anytype.middleware.mappers.*
import com.anytypeio.anytype.middleware.model.CreateAccountResponse
import com.anytypeio.anytype.middleware.model.CreateWalletResponse
import com.anytypeio.anytype.middleware.model.SelectAccountResponse
import com.anytypeio.anytype.middleware.service.MiddlewareService
import timber.log.Timber
import java.util.*

class Middleware(
    private val service: MiddlewareService,
    private val factory: MiddlewareFactory
) {
    private val iconEmojiKey = "iconEmoji"
    private val iconImageKey = "iconImage"
    private val coverIdKey = "coverId"
    private val coverTypeKey = "coverType"
    private val nameKey = "name"
    private val typeKey = "type"
    private val layoutKey = "layout"
    private val isDraftKey = "isDraft"

    @Throws(Exception::class)
    fun getConfig(): Config {

        val request = Rpc.Config.Get.Request()

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.configGet(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return Config(
            home = response.homeBlockId,
            profile = response.profileBlockId,
            gateway = response.gatewayUrl
        )
    }

    @Throws(Exception::class)
    fun createWallet(path: String): CreateWalletResponse {

        val request = Rpc.Wallet.Create.Request(rootPath = path)

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.walletCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return CreateWalletResponse(response.mnemonic)
    }

    @Throws(Exception::class)
    fun createAccount(
        name: String,
        path: String?,
        invitationCode: String
    ): CreateAccountResponse {

        val request = Rpc.Account.Create.Request(
            name = name,
            alphaInviteCode = invitationCode,
            avatarLocalPath = path
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.accountCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

        val acc = response.account

        checkNotNull(acc)

        return CreateAccountResponse(
            acc.id,
            acc.name,
            acc.avatar
        )
    }

    @Throws(Exception::class)
    fun recoverWallet(path: String, mnemonic: String) {
        val request = Rpc.Wallet.Recover.Request(
            mnemonic = mnemonic,
            rootPath = path
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.walletRecover(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun convertWallet(entropy: String): String {
        val request = Rpc.Wallet.Convert.Request(entropy = entropy)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.walletConvert(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.mnemonic
    }

    @Throws(Exception::class)
    fun logout() {
        val request: Rpc.Account.Stop.Request = Rpc.Account.Stop.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountStop(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun recoverAccount() {
        val request = Rpc.Account.Recover.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.accountRecover(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun selectAccount(id: String, path: String): SelectAccountResponse {

        val request = Rpc.Account.Select.Request(
            id = id,
            rootPath = path
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.accountSelect(request)

        if (BuildConfig.DEBUG) logResponse(response)

        val acc = response.account
        val config = response.config

        checkNotNull(acc)

        return SelectAccountResponse(
            id = acc.id,
            name = acc.name,
            avatar = acc.avatar,
            enableDataView = config?.enableDataview,
            enableDebug = config?.enableDebug,
            enableChannelSwitch = config?.enableReleaseChannelSwitch,
            enableSpaces = config?.enableSpaces
        )
    }

    @Throws(Exception::class)
    fun openDashboard(contextId: String, id: String): Payload {
        val request: Rpc.Block.Open.Request = Rpc.Block.Open.Request(
            contextId = contextId,
            blockId = id
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun openBlock(id: String): Payload {
        val request = Rpc.Block.Open.Request(blockId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun createPage(ctx: Id?, emoji: String?, isDraft: Boolean?, type: String?): Id {

        val details: MutableMap<String, Any> = mutableMapOf()
        emoji?.let { details[iconEmojiKey] = it}
        isDraft?.let { details[isDraftKey] = it }
        type?.let { details[typeKey] = it }

        val request = Rpc.Block.CreatePage.Request(
            contextId = ctx.orEmpty(),
            details = details,
            position = Block.Position.Inner
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockCreatePage(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.targetId
    }

    @Throws(Exception::class)
    fun closePage(id: String) {
        val request = Rpc.Block.Close.Request(blockId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockClose(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun closeDashboard(id: String) {
        val request = Rpc.Block.Close.Request(blockId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockClose(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun updateDocumentTitle(command: Command.UpdateTitle) {

        val detail = Rpc.Block.Set.Details.Detail(
            key = nameKey,
            value = command.title
        )

        val request = Rpc.Block.Set.Details.Request(
            contextId = command.context,
            details = listOf(detail)
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockSetDetails(request)

        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun updateText(
        contextId: String,
        blockId: String,
        text: String,
        marks: List<Block.Content.Text.Mark>
    ) {
        val markup: Block.Content.Text.Marks = Block.Content.Text.Marks(marks)

        val request = Rpc.Block.Set.Text.TText.Request(
            contextId = contextId,
            blockId = blockId,
            text = text,
            marks = markup
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockSetTextText(request)

        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun updateCheckbox(
        context: String,
        target: String,
        isChecked: Boolean
    ): Payload {
        val request = Rpc.Block.Set.Text.Checked.Request(
            contextId = context,
            blockId = target,
            checked = isChecked
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetTextChecked(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun updateTextStyle(command: Command.UpdateStyle): Payload {

        val style = command.style.toMiddlewareModel()

        val request: BlockList.Set.Text.Style.Request = BlockList.Set.Text.Style.Request(
            style = style,
            blockIds = command.targets,
            contextId = command.context
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockListSetTextStyle(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun updateTextColor(command: Command.UpdateTextColor): Payload {
        val request = BlockList.Set.Text.Color.Request(
            contextId = command.context,
            color = command.color,
            blockIds = command.targets
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetTextColor(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun updateBackgroundColor(command: Command.UpdateBackgroundColor): Payload {
        val request = BlockList.Set.BackgroundColor.Request(
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
    fun updateAlignment(command: Command.UpdateAlignment): Payload {

        val align: Block.Align = command.alignment.toMiddlewareModel()

        val request = BlockList.Set.Align.Request(
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
    fun uploadBlock(command: Command.UploadBlock): Payload {
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

    @Throws(Exception::class)
    fun createBlock(
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
    fun replace(command: Command.Replace): Pair<String, Payload> {

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
    fun createDocument(command: Command.CreateDocument): Triple<String, String, Payload> {

        val details: MutableMap<String, Any> = mutableMapOf()

        command.emoji?.let { details[iconEmojiKey] = it }
        command.type?.let { details[typeKey] = it }
        command.layout?.let { details[layoutKey] = it.toMiddlewareModel().value.toDouble() }

        val position: Block.Position = command.position.toMiddlewareModel()

        val request = Rpc.Block.CreatePage.Request(
            contextId = command.context,
            targetId = command.target,
            position = position,
            details = details
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockCreatePage(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return Triple(
            response.blockId,
            response.targetId,
            response.event.toPayload()
        )
    }

    @Throws(Exception::class)
    fun createPage(command: Command.CreateNewDocument): String {

        val details: MutableMap<String, Any> = mutableMapOf()

        command.emoji?.let { details[iconEmojiKey] = it }
        command.name.let { details[nameKey] = it }
        command.type?.let { details[typeKey] = it }

        val request = Rpc.Page.Create.Request(details = details.toMap())

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.pageCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.pageId
    }

    @Throws(Exception::class)
    fun move(command: Command.Move): Payload {

        val position: Block.Position = command.position.toMiddlewareModel()

        val request: BlockList.Move.Request = BlockList.Move.Request(
            contextId = command.ctx,
            targetContextId = command.targetContextId,
            position = position,
            blockIds = command.blockIds,
            dropTargetId = command.targetId
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockListMove(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun duplicate(command: Command.Duplicate): Pair<List<Id>, Payload> {
        val request = BlockList.Duplicate.Request(
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
    fun unlink(command: Command.Unlink): Payload {
        val request = Rpc.Block.Unlink.Request(
            contextId = command.context,
            blockIds = command.targets
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockUnlink(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun merge(command: Command.Merge): Payload {
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
    fun split(command: Command.Split): Pair<String, Payload> {

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
    fun setDocumentEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload {

        val emojiDetail = Rpc.Block.Set.Details.Detail(
            key = iconEmojiKey,
            value = command.emoji
        )

        val imageDetail = Rpc.Block.Set.Details.Detail(
            key = iconImageKey,
            value = null
        )

        val request = Rpc.Block.Set.Details.Request(
            contextId = command.context,
            details = listOf(emojiDetail, imageDetail)
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockSetDetails(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setDocumentImageIcon(command: Command.SetDocumentImageIcon): Payload {

        val imageDetail = Rpc.Block.Set.Details.Detail(
            key = iconImageKey,
            value = command.hash
        )
        val emojiDetail = Rpc.Block.Set.Details.Detail(
            key = iconEmojiKey,
            value = null
        )

        val request = Rpc.Block.Set.Details.Request(
            contextId = command.context,
            details = listOf(imageDetail, emojiDetail)
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockSetDetails(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setDocumentCoverColor(
        ctx: String,
        color: String
    ): Payload {
        val coverIdDetail = Rpc.Block.Set.Details.Detail(
            key = coverIdKey,
            value = color
        )
        val coverTypeDetail = Rpc.Block.Set.Details.Detail(
            key = coverTypeKey,
            value = Constants.COVER_TYPE_COLOR.toDouble()
        )
        val request = Rpc.Block.Set.Details.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setDocumentCoverGradient(
        ctx: String,
        gradient: String
    ): Payload {
        val coverIdDetail = Rpc.Block.Set.Details.Detail(
            key = coverIdKey,
            value = gradient
        )
        val coverTypeDetail = Rpc.Block.Set.Details.Detail(
            key = coverTypeKey,
            value = Constants.COVER_TYPE_GRADIENT.toDouble()
        )
        val request = Rpc.Block.Set.Details.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setDocumentCoverImage(
        ctx: String,
        hash: String
    ): Payload {
        val coverIdDetail = Rpc.Block.Set.Details.Detail(
            key = coverIdKey,
            value = hash
        )
        val coverTypeDetail = Rpc.Block.Set.Details.Detail(
            key = coverTypeKey,
            value = Constants.COVER_TYPE_UPLOADED_IMAGE.toDouble()
        )
        val request = Rpc.Block.Set.Details.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun removeDocumentCover(ctx: String): Payload {
        val coverIdDetail = Rpc.Block.Set.Details.Detail(
            key = coverIdKey,
            value = null
        )
        val coverTypeDetail = Rpc.Block.Set.Details.Detail(
            key = coverTypeKey,
            value = Constants.COVER_TYPE_NONE.toDouble()
        )
        val request = Rpc.Block.Set.Details.Request(
            contextId = ctx,
            details = listOf(coverIdDetail, coverTypeDetail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun removeDocumentIcon(ctx: String): Payload {
        val imageDetail = Rpc.Block.Set.Details.Detail(
            key = iconImageKey,
            value = null
        )
        val emojiDetail = Rpc.Block.Set.Details.Detail(
            key = iconEmojiKey,
            value = null
        )

        val request = Rpc.Block.Set.Details.Request(
            contextId = ctx,
            details = listOf(imageDetail, emojiDetail)
        )

        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setupBookmark(command: Command.SetupBookmark): Payload {
        val request: Rpc.Block.Bookmark.Fetch.Request = Rpc.Block.Bookmark.Fetch.Request(
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
    fun undo(command: Command.Undo): Payload {
        val request = Rpc.Block.Undo.Request(contextId = command.context)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockUndo(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun redo(command: Command.Redo): Payload {
        val request = Rpc.Block.Redo.Request(contextId = command.context)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockRedo(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun turnIntoDocument(command: Command.TurnIntoDocument): List<String> {
        val request = BlockList.ConvertChildrenToPages.Request(
            contextId = command.context,
            blockIds = command.targets
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.convertChildrenToPages(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.linkIds
    }

    @Throws(Exception::class)
    fun paste(command: Command.Paste): Response.Clipboard.Paste {

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
            selectedBlockIds = command.selected
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

    @Throws(Exception::class)
    fun copy(command: Command.Copy): Response.Clipboard.Copy {

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
    fun uploadFile(command: Command.UploadFile): Response.Media.Upload {

        val type = command.type.toMiddlewareModel()

        val request = Rpc.UploadFile.Request(
            localPath = command.path,
            type = type
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.uploadFile(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return Response.Media.Upload(response.hash)
    }

    @Throws(Exception::class)
    fun getMiddlewareVersion(): Rpc.Version.Get.Response {
        val request = Rpc.Version.Get.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.versionGet(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response
    }

    @Throws(Exception::class)
    fun getObjectInfoWithLinks(pageId: String): ObjectInfoWithLinks {
        val request = Rpc.Navigation.GetObjectInfoWithLinks.Request(
            objectId = pageId
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectInfoWithLinks(request)
        if (BuildConfig.DEBUG) logResponse(response)

        val info = response.object_

        checkNotNull(info) { "Empty result" }

        return info
    }

    @Throws(Exception::class)
    fun listObjects(): List<ObjectInfo> {
        val request = Rpc.Navigation.ListObjects.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.listObjects(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.objects
    }

    @Throws(Exception::class)
    fun updateDividerStyle(command: Command.UpdateDivider): Payload {
        val style = command.style.toMiddlewareModel()

        val request: BlockList.Set.Div.Style.Request = BlockList.Set.Div.Style.Request(
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
    fun setFields(command: Command.SetFields): Payload {

        val fields: MutableList<BlockField> = ArrayList()

        for (i in command.fields.indices) {
            val (first, second) = command.fields[i]
            val field = BlockField(
                blockId = first,
                fields = second.map
            )
            fields.add(field)
        }

        val request = BlockList.Set.Fields.Request(
            contextId = command.context,
            blockFields = fields
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockListSetFields(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun getObjectTypes(): List<MObjectType> {
        val request = Rpc.ObjectType.List.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectTypeList(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.objectTypes
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
    fun createSet(
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

        val request = Rpc.Block.CreateSet.Request(
            contextId = contextId,
            targetId = targetId.orEmpty(),
            source = source,
            position = position?.toMiddlewareModel() ?: Block.Position.Bottom
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockCreateSet(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return Response.Set.Create(
            blockId = response.blockId.ifEmpty { null },
            targetId = response.targetId,
            payload = response.event.toPayload()
        )
    }

    @Throws(Exception::class)
    fun setActiveDataViewViewer(
        contextId: String,
        blockId: String,
        viewId: String,
        offset: Int,
        limit: Int
    ): Payload {
        val request = Rpc.Block.Dataview.ViewSetActive.Request(
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
    fun addNewRelationToDataView(
        context: String,
        target: String,
        name: String,
        format: Relation.Format
    ): Pair<Id, Payload> {

        val relation = MRelation(
            name = name,
            format = format.toMiddlewareModel()
        )

        val request = Rpc.Block.Dataview.RelationAdd.Request(
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
    fun addRelationToDataView(ctx: Id, dv: Id, relation: Id): Payload {
        val request = Rpc.Block.Dataview.RelationAdd.Request(
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
    fun deleteRelationFromDataView(ctx: Id, dv: Id, relation: Id): Payload {
        val request = Rpc.Block.Dataview.RelationDelete.Request(
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
    fun updateDataViewViewer(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload {
        val request = Rpc.Block.Dataview.ViewUpdate.Request(
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
    fun duplicateDataViewViewer(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload {
        val request = Rpc.Block.Dataview.ViewCreate.Request(
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
    fun createDataViewRecord(context: String, target: String): Map<String, Any?> {
        val request = Rpc.Block.Dataview.RecordCreate.Request(
            contextId = context,
            blockId = target
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockDataViewRecordCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.record?.toMap() ?: emptyMap()
    }

    @Throws(Exception::class)
    fun updateDataViewRecord(
        context: String,
        target: String,
        record: String,
        values: Map<String, Any?>
    ) {
        val request = Rpc.Block.Dataview.RecordUpdate.Request(
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
    fun addDataViewViewer(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload {
        val request = Rpc.Block.Dataview.ViewCreate.Request(
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
    fun removeDataViewViewer(
        ctx: String,
        dataview: String,
        viewer: String
    ): Payload {
        val request = Rpc.Block.Dataview.ViewDelete.Request(
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
    fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int
    ): List<Map<String, Any?>> {
        val request = Rpc.Object.Search.Request(
            sorts = sorts.map { it.toMiddlewareModel() },
            filters = filters.map { it.toMiddlewareModel() },
            fullText = fulltext,
            offset = offset,
            limit = limit
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSearch(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.records.map { it?.toMap() ?: emptyMap() }
    }

    @Throws(Exception::class)
    fun relationListAvailable(ctx: Id): List<MRelation> {
        val request = Rpc.Object.RelationListAvailable.Request(
            contextId = ctx
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.relationListAvailable(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.relations
    }

    //todo Add Relation mapping
    @Throws(Exception::class)
    fun addRelationToBlock(command: Command.AddRelationToBlock): Payload {
        val request = Rpc.Block.Relation.Add.Request(
            contextId = command.contextId,
            blockId = command.blockId,
            relation = null
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockAddRelation(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun setRelationKey(command: Command.SetRelationKey): Payload {
        Rpc.Block.Relation.Add
        val request = Rpc.Block.Relation.SetKey.Request(
            contextId = command.contextId,
            blockId = command.blockId,
            key = command.key
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.relationSetKey(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun debugSync(): String {
        val request = Rpc.Debug.Sync.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.debugSync(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.toString()
    }

    fun addRecordRelationOption(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?> {
        val request = Rpc.Block.Dataview.RecordRelationOptionAdd.Request(
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

    fun addObjectRelationOption(
        ctx: Id,
        relation: Id,
        name: Id,
        color: String
    ): Pair<Payload, Id?> {
        val request = Rpc.Object.RelationOptionAdd.Request(
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

    fun blockListTurnInto(
        context: String,
        targets: List<String>,
        style: CBTextStyle
    ): Payload {
        val request = BlockList.TurnInto.Request(
            contextId = context,
            blockIds = targets,
            style = style.toMiddlewareModel()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListTurnInto(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun updateDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload {
        val detail = Rpc.Block.Set.Details.Detail(
            key = key,
            value = value
        )
        val request = Rpc.Block.Set.Details.Request(
            contextId = ctx,
            details = listOf(detail)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetDetails(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun blockListSetTextMarkup(command: Command.UpdateBlocksMark): Payload {
        val context = command.context
        val mark = command.mark.toMiddlewareModel()
        val targets = command.targets

        val request = BlockList.Set.Text.Mark.Request(
            contextId = context,
            blockIds = targets,
            mark = mark
        )

        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListSetTextMark(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    @Throws(Exception::class)
    fun setObjectType(ctx: Id, typeId: Id): Payload {

        val request = Rpc.Block.ObjectType.Set.Request(
            contextId = ctx,
            objectTypeUrl = typeId
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetObjectType(request)
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

    fun addRelationToObject(ctx: Id, relation: Id): Payload {
        val request = Rpc.Object.RelationAdd.Request(
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

    fun addNewRelationToObject(ctx: Id, format: RelationFormat, name: String): Pair<Id, Payload> {
        val request = Rpc.Object.RelationAdd.Request(
            contextId = ctx,
            relation = MRelation(
                format = format.toMiddlewareModel(),
                name = name
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

    fun deleteRelationFromObject(ctx: Id, relation: Id) : Payload {
        val request = Rpc.Object.RelationDelete.Request(
            contextId = ctx,
            relationKey = relation
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectRelationDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun addToFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload {
        val request = Rpc.Object.FeaturedRelation.Add.Request(
            contextId = ctx,
            relations = relations
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.featuredRelationsAdd(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun removeFromFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload {
        val request = Rpc.Object.FeaturedRelation.Remove.Request(
            contextId = ctx,
            relations = relations
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.featuredRelationsRemove(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun setObjectIsFavorite(
        ctx: Id,
        isFavorite: Boolean
    ) : Payload {
        val request = Rpc.Object.SetIsFavorite.Request(
            contextId = ctx,
            isFavorite = isFavorite
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetIsFavorite(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun setObjectIsArchived(
        ctx: Id,
        isArchived: Boolean
    ) : Payload {
        val request = Rpc.Object.SetIsArchived.Request(
            contextId = ctx,
            isArchived = isArchived
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetIsArchived(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun setObjectListIsArchived(
        targets: List<Id>,
        isArchived: Boolean
    ) {
        val request = Rpc.ObjectList.Set.IsArchived.Request(
            objectIds = targets,
            isArchived = isArchived,
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectListSetIsArchived(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    fun deleteObjects(targets: List<Id>) {
        val request = Rpc.ObjectList.Delete.Request(objectIds = targets)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectListDelete(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    fun setObjectLayout(ctx: Id, layout: ObjectType.Layout) : Payload {
        val request = Rpc.Object.SetLayout.Request(
            contextId = ctx,
            layout = layout.toMiddlewareModel()
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.objectSetLayout(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.event.toPayload()
    }

    fun exportLocalStore(path: String): String {
        val request = Rpc.ExportLocalstore.Request(path = path)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.exportLocalStore(request)
        if (BuildConfig.DEBUG) logResponse(response)
        return response.path
    }

    fun fileListOffload() {
        val request = Rpc.FileList.Offload.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.fileListOffload(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }
}