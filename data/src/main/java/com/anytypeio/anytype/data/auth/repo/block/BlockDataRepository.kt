package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.data.auth.exception.BackwardCompatilityNotSupportedException
import com.anytypeio.anytype.data.auth.mapper.toDomain
import com.anytypeio.anytype.data.auth.mapper.toEntity
import com.anytypeio.anytype.data.auth.model.PositionEntity
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.page.navigation.DocumentInfo
import com.anytypeio.anytype.domain.page.navigation.PageInfoWithLinks

class BlockDataRepository(
    private val factory: BlockDataStoreFactory
) : BlockRepository {

    override suspend fun getConfig() = factory.remote.getConfig().toDomain()

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ) = factory.remote.openDashboard(id = id, contextId = contextId).toDomain()

    override suspend fun openPage(id: String): Result<Payload> = try {
        Result.Success(factory.remote.openPage(id).toDomain())
    } catch (e: BackwardCompatilityNotSupportedException) {
        Result.Failure(Error.BackwardCompatibility)
    }

    override suspend fun openProfile(id: String): Payload = factory.remote.openProfile(id).toDomain()

    override suspend fun closeDashboard(id: String) {
        factory.remote.closeDashboard(id)
    }

    override suspend fun updateAlignment(
        command: Command.UpdateAlignment
    ) : Payload = factory.remote.updateAlignment(command.toEntity()).toDomain()

    override suspend fun createPage(parentId: String, emoji: String?) =
        factory.remote.createPage(parentId, emoji)

    override suspend fun closePage(id: String) {
        factory.remote.closePage(id)
    }

    override suspend fun updateDocumentTitle(
        command: Command.UpdateTitle
    ) = factory.remote.updateDocumentTitle(command.toEntity())

    override suspend fun updateText(command: Command.UpdateText) {
        factory.remote.updateText(command.toEntity())
    }

    override suspend fun updateTextStyle(
        command: Command.UpdateStyle
    ) : Payload = factory.remote.updateTextStyle(command.toEntity()).toDomain()

    override suspend fun updateTextColor(
        command: Command.UpdateTextColor
    ): Payload = factory.remote.updateTextColor(command.toEntity()).toDomain()

    override suspend fun updateBackgroundColor(
        command: Command.UpdateBackgroundColor
    ): Payload = factory.remote.updateBackroundColor(command.toEntity()).toDomain()

    override suspend fun updateCheckbox(
        command: Command.UpdateCheckbox
    ): Payload = factory.remote.updateCheckbox(command.toEntity()).toDomain()

    override suspend fun create(command: Command.Create): Pair<Id, Payload> {
        return factory.remote.create(command.toEntity()).let { (id, payload) ->
            Pair(id, payload.toDomain())
        }
    }

    override suspend fun replace(
        command: Command.Replace
    ): Pair<Id, Payload> = factory.remote.replace(command.toEntity()).let { (id, payload) ->
        Pair(id, payload.toDomain())
    }

    override suspend fun duplicate(
        command: Command.Duplicate
    ): Pair<Id, Payload> = factory.remote.duplicate(command.toEntity()).let { (id, payload) ->
        Pair(id, payload.toDomain())
    }

    override suspend fun createDocument(
        command: Command.CreateDocument
    ): Triple<String, String, Payload> {
        return factory.remote.createDocument(
            command.toEntity()
        ).let { (id, target, payload) ->
            Triple(id, target, payload.toDomain())
        }
    }

    override suspend fun createNewDocument(
        command: Command.CreateNewDocument
    ): Id {
        return factory.remote.createNewDocument(command.toEntity())
    }

    override suspend fun move(command: Command.Move): Payload {
        return factory.remote.move(command.toEntity()).toDomain()
    }

    override suspend fun unlink(
        command: Command.Unlink
    ): Payload = factory.remote.unlink(command.toEntity()).toDomain()

    override suspend fun merge(
        command: Command.Merge
    ): Payload = factory.remote.merge(command.toEntity()).toDomain()

    override suspend fun split(
        command: Command.Split
    ): Pair<Id, Payload> = factory.remote.split(command.toEntity()).let { (id, payload) ->
        Pair(id, payload.toDomain())
    }

    override suspend fun setDocumentEmojiIcon(
        command: Command.SetDocumentEmojiIcon
    ): Payload = factory.remote.setDocumentEmojiIcon(command.toEntity()).toDomain()

    override suspend fun setDocumentImageIcon(
        command: Command.SetDocumentImageIcon
    ): Payload = factory.remote.setDocumentImageIcon(command.toEntity()).toDomain()

    override suspend fun setupBookmark(
        command: Command.SetupBookmark
    ): Payload = factory.remote.setupBookmark(command.toEntity()).toDomain()

    override suspend fun uploadBlock(command: Command.UploadBlock): Payload =
        factory.remote.uploadBlock(command.toEntity()).toDomain()

    override suspend fun undo(
        command: Command.Undo
    ) : Payload = factory.remote.undo(command.toEntity()).toDomain()

    override suspend fun redo(
        command: Command.Redo
    ) : Payload = factory.remote.redo(command.toEntity()).toDomain()

    override suspend fun archiveDocument(
        command: Command.ArchiveDocument
    ) = factory.remote.archiveDocument(command.toEntity())

    override suspend fun turnIntoDocument(
        command: Command.TurnIntoDocument
    ): List<Id> = factory.remote.turnIntoDocument(command.toEntity())

    override suspend fun paste(
        command: Command.Paste
    ): Paste.Response = factory.remote.paste(command.toEntity()).toDomain()

    override suspend fun copy(
        command: Command.Copy
    ): Copy.Response = factory.remote.copy(command.toEntity()).toDomain()

    override suspend fun uploadFile(
        command: Command.UploadFile
    ): Hash = factory.remote.uploadFile(command.toEntity())

    override suspend fun getPageInfoWithLinks(pageId: String): PageInfoWithLinks =
        factory.remote.getPageInfoWithLinks(pageId).toDomain()

    override suspend fun getListPages(): List<DocumentInfo> =
        factory.remote.getListPages().map { it.toDomain() }

    override suspend fun linkToObject(
        context: Id,
        target: Id,
        block: Id,
        replace: Boolean,
        position: Position
    ): Payload = factory.remote.linkToObject(
        context = context,
        target = target,
        block = block,
        replace = replace,
        position = PositionEntity.valueOf(position.name)
    ).toDomain()
}