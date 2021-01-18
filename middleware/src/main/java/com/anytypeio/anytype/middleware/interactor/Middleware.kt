package com.anytypeio.anytype.middleware.interactor

import anytype.Rpc
import anytype.Rpc.BlockList
import anytype.Rpc.BlockList.Set.Fields.Request.BlockField
import anytype.model.Block
import anytype.model.PageInfo
import anytype.model.PageInfoWithLinks
import anytype.model.Range
import com.anytypeio.anytype.data.auth.model.*
import com.anytypeio.anytype.middleware.BuildConfig
import com.anytypeio.anytype.middleware.const.Constants
import com.anytypeio.anytype.middleware.model.CreateAccountResponse
import com.anytypeio.anytype.middleware.model.CreateWalletResponse
import com.anytypeio.anytype.middleware.model.SelectAccountResponse
import com.anytypeio.anytype.middleware.service.MiddlewareService
import timber.log.Timber
import java.util.*

class Middleware(
    private val service: MiddlewareService,
    private val factory: MiddlewareFactory,
    private val mapper: MiddlewareMapper
) {
    private val iconEmojiKey = "iconEmoji"
    private val iconImageKey = "iconImage"
    private val coverIdKey = "coverId"
    private val coverTypeKey = "coverType"
    private val nameKey = "name"

    @Throws(Exception::class)
    fun getConfig(): ConfigEntity {

        val request = Rpc.Config.Get.Request()

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.configGet(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return ConfigEntity(
            response.homeBlockId,
            response.profileBlockId,
            response.gatewayUrl
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

        checkNotNull(acc)

        return SelectAccountResponse(
            acc.id,
            acc.name,
            acc.avatar
        )
    }

    @Throws(Exception::class)
    fun openDashboard(contextId: String, id: String): PayloadEntity {
        val request: Rpc.Block.Open.Request = Rpc.Block.Open.Request(
            contextId = contextId,
            blockId = id
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun openBlock(id: String): PayloadEntity {
        val request = Rpc.Block.Open.Request(blockId = id)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockOpen(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun createPage(parentId: String, emoji: String?): String {

        val details = if (emoji != null)
            mapOf(iconEmojiKey to emoji)
        else
            emptyMap()

        val request = Rpc.Block.CreatePage.Request(
            contextId = parentId,
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
    fun updateDocumentTitle(command: CommandEntity.UpdateTitle) {

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
    ): PayloadEntity {
        val request = Rpc.Block.Set.Text.Checked.Request(
            contextId = context,
            blockId = target,
            checked = isChecked
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetTextChecked(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun updateTextStyle(command: CommandEntity.UpdateStyle): PayloadEntity {

        val style = mapper.toMiddleware(command.style)

        val request: BlockList.Set.Text.Style.Request = BlockList.Set.Text.Style.Request(
            style = style,
            blockIds = command.targets,
            contextId = command.context
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockListSetTextStyle(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun updateTextColor(command: CommandEntity.UpdateTextColor): PayloadEntity {
        val request = Rpc.Block.Set.Text.Color.Request(
            contextId = command.context,
            color = command.color,
            blockId = command.target
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockSetTextColor(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun updateBackgroundColor(command: CommandEntity.UpdateBackgroundColor): PayloadEntity {
        val request = BlockList.Set.BackgroundColor.Request(
            contextId = command.context,
            blockIds = command.targets,
            color = command.color
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListSetBackgroundColor(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun updateAlignment(command: CommandEntity.UpdateAlignment): PayloadEntity {

        val align: Block.Align = mapper.toMiddleware(command.alignment)

        val request = BlockList.Set.Align.Request(
            contextId = command.context,
            blockIds = command.targets,
            align = align
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockListSetAlign(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun uploadBlock(command: CommandEntity.UploadBlock): PayloadEntity {
        val request = Rpc.Block.Upload.Request(
            filePath = command.filePath,
            url = command.url,
            contextId = command.contextId,
            blockId = command.blockId
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockUpload(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun createBlock(
        contextId: String,
        targetId: String,
        position: PositionEntity,
        prototype: BlockEntity.Prototype
    ): Pair<String, PayloadEntity> {
        val request = Rpc.Block.Create.Request(
            contextId = contextId,
            targetId = targetId,
            position = mapper.toMiddleware(position),
            block = factory.create(prototype)
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockCreate(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return Pair(response.blockId, mapper.toPayload(response.event))
    }

    @Throws(Exception::class)
    fun replace(command: CommandEntity.Replace): Pair<String, PayloadEntity> {

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

        return Pair(response.blockId, mapper.toPayload(response.event))
    }

    @Throws(Exception::class)
    fun createDocument(command: CommandEntity.CreateDocument): Triple<String, String, PayloadEntity> {

        val details = if (command.emoji != null) {
            mapOf(iconEmojiKey to command.emoji)
        } else {
            mapOf()
        }

        val position: Block.Position = mapper.toMiddleware(command.position)

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
            mapper.toPayload(response.event)
        )
    }

    @Throws(Exception::class)
    fun createPage(command: CommandEntity.CreatePage): String {

        val details: MutableMap<String, Any> = mutableMapOf()

        command.emoji?.let { details[iconEmojiKey] = it }
        command.name?.let { details[nameKey] = it }

        val request = Rpc.Page.Create.Request(details = details.toMap())

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.pageCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return response.pageId
    }

    @Throws(Exception::class)
    fun move(command: CommandEntity.Move): PayloadEntity {

        val position: Block.Position = mapper.toMiddleware(command.position)

        val request: BlockList.Move.Request = BlockList.Move.Request(
            contextId = command.contextId,
            targetContextId = command.dropTargetContextId,
            position = position,
            blockIds = command.blockIds,
            dropTargetId = command.dropTargetId
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockListMove(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun duplicate(command: CommandEntity.Duplicate): Pair<String, PayloadEntity> {
        val request = BlockList.Duplicate.Request(
            contextId = command.context,
            targetId = command.original,
            blockIds = listOf(command.original),
            position = Block.Position.Bottom
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListDuplicate(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return Pair(response.blockIds.first(), mapper.toPayload(response.event))
    }

    @Throws(Exception::class)
    fun unlink(command: CommandEntity.Unlink): PayloadEntity {
        val request = Rpc.Block.Unlink.Request(
            contextId = command.context,
            blockIds = command.targets
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockUnlink(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun merge(command: CommandEntity.Merge): PayloadEntity {
        val request = Rpc.Block.Merge.Request(
            contextId = command.context,
            firstBlockId = command.pair.first,
            secondBlockId = command.pair.second
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockMerge(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun split(command: CommandEntity.Split): Pair<String, PayloadEntity> {

        val style = mapper.toMiddleware(command.style)

        val range = Range(
            from = command.range.first,
            to = command.range.last
        )

        val mode = mapper.toMiddleware(command.mode)

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

        return Pair(response.blockId, mapper.toPayload(response.event))
    }

    @Throws(Exception::class)
    fun setDocumentEmojiIcon(command: CommandEntity.SetDocumentEmojiIcon): PayloadEntity {

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

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun setDocumentImageIcon(command: CommandEntity.SetDocumentImageIcon): PayloadEntity {

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

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun setDocumentCoverColor(
        ctx: String,
        color: String
    ): PayloadEntity {
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
        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun setDocumentCoverGradient(
        ctx: String,
        gradient: String
    ): PayloadEntity {
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
        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun setDocumentCoverImage(
        ctx: String,
        hash: String
    ): PayloadEntity {
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
        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun removeDocumentCover(ctx: String): PayloadEntity {
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
        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun setupBookmark(command: CommandEntity.SetupBookmark): PayloadEntity {
        val request: Rpc.Block.Bookmark.Fetch.Request = Rpc.Block.Bookmark.Fetch.Request(
            contextId = command.context,
            blockId = command.target,
            url = command.url
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockBookmarkFetch(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun undo(command: CommandEntity.Undo): PayloadEntity {
        val request = Rpc.Block.Undo.Request(contextId = command.context)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockUndo(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun redo(command: CommandEntity.Redo): PayloadEntity {
        val request = Rpc.Block.Redo.Request(contextId = command.context)
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockRedo(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun archiveDocument(command: CommandEntity.ArchiveDocument) {
        val request: BlockList.Set.Page.IsArchived.Request = BlockList.Set.Page.IsArchived.Request(
            contextId = command.context,
            blockIds = command.target,
            isArchived = command.isArchived
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.blockListSetPageIsArchived(request)
        if (BuildConfig.DEBUG) logResponse(response)
    }

    @Throws(Exception::class)
    fun turnIntoDocument(command: CommandEntity.TurnIntoDocument): List<String> {
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
    fun paste(command: CommandEntity.Paste): Response.Clipboard.Paste {

        val range = Range(
            from = command.range.first,
            to = command.range.last
        )

        val blocks: List<Block> = mapper.toMiddleware(command.blocks)

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
            mapper.toPayload(response.event)
        )
    }

    @Throws(Exception::class)
    fun copy(command: CommandEntity.Copy): Response.Clipboard.Copy {

        val range: Range? = command.range?.let {
            Range(
                from = it.first,
                to = it.last
            )
        }

        val blocks: List<Block> = mapper.toMiddleware(command.blocks)

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
            mapper.toEntity(response.anySlot)
        )
    }

    @Throws(Exception::class)
    fun uploadFile(command: CommandEntity.UploadFile): Response.Media.Upload {

        val type = when (command.type) {
            BlockEntity.Content.File.Type.FILE -> Block.Content.File.Type.File
            BlockEntity.Content.File.Type.IMAGE -> Block.Content.File.Type.Image
            BlockEntity.Content.File.Type.VIDEO -> Block.Content.File.Type.Video
            BlockEntity.Content.File.Type.NONE -> Block.Content.File.Type.None
        }

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
    fun getPageInfoWithLinks(pageId: String): PageInfoWithLinks {
        val request = Rpc.Navigation.GetPageInfoWithLinks.Request(
            pageId = pageId
        )
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.pageInfoWithLinks(request)
        if (BuildConfig.DEBUG) logResponse(response)

        val info = response.page

        checkNotNull(info) { "Empty result" }

        return info
    }

    fun getListPages(): List<PageInfo> {
        val request = Rpc.Navigation.ListPages.Request()
        if (BuildConfig.DEBUG) logRequest(request)
        val response = service.listPages(request)
        if (BuildConfig.DEBUG) logResponse(response)

        return response.pages
    }

    @Throws(Exception::class)
    fun linkToObject(
        contextId: String,
        targetId: String,
        blockId: String,
        replace: Boolean,
        positionEntity: PositionEntity
    ): PayloadEntity {

        val position: Block.Position = if (replace) {
            Block.Position.Replace
        } else {
            mapper.toMiddleware(positionEntity)
        }

        val link = Block.Content.Link(targetBlockId = blockId)

        val model = Block(link = link)

        val request: Rpc.Block.Create.Request = Rpc.Block.Create.Request(
            contextId = contextId,
            targetId = targetId,
            position = position,
            block = model
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockCreate(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun updateDividerStyle(command: CommandEntity.UpdateDivider): PayloadEntity {
        val style = when (command.style) {
            BlockEntity.Content.Divider.Style.LINE -> Block.Content.Div.Style.Line
            BlockEntity.Content.Divider.Style.DOTS -> Block.Content.Div.Style.Dots
        }

        val request: BlockList.Set.Div.Style.Request = BlockList.Set.Div.Style.Request(
            contextId = command.context,
            blockIds = command.targets,
            style = style
        )

        if (BuildConfig.DEBUG) logRequest(request)

        val response = service.blockListSetDivStyle(request)

        if (BuildConfig.DEBUG) logResponse(response)

        return mapper.toPayload(response.event)
    }

    @Throws(Exception::class)
    fun setFields(command: CommandEntity.SetFields): PayloadEntity {

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

        return mapper.toPayload(response.event)
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