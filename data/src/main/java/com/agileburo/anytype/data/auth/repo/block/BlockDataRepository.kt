package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.domain.block.interactor.Clipboard
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.event.model.Payload

class BlockDataRepository(
    private val factory: BlockDataStoreFactory
) : BlockRepository {

    override suspend fun getConfig() = factory.remote.getConfig().toDomain()

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ) = factory.remote.openDashboard(id = id, contextId = contextId).toDomain()

    override suspend fun openPage(id: String): Payload = factory.remote.openPage(id).toDomain()
    override suspend fun openProfile(id: String): Payload = factory.remote.openProfile(id).toDomain()

    override suspend fun closeDashboard(id: String) {
        factory.remote.closeDashboard(id)
    }

    override suspend fun updateAlignment(
        command: Command.UpdateAlignment
    ) : Payload = factory.remote.updateAlignment(command.toEntity()).toDomain()

    override suspend fun createPage(parentId: String) = factory.remote.createPage(parentId)

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

    override suspend fun updateCheckbox(command: Command.UpdateCheckbox) {
        factory.remote.updateCheckbox(command.toEntity())
    }

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
    ) = factory.remote.createDocument(command.toEntity())

    override suspend fun dnd(command: Command.Dnd) {
        factory.remote.dnd(command.toEntity())
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

    override suspend fun setIconName(
        command: Command.SetIconName
    ) = factory.remote.setIconName(command.toEntity())

    override suspend fun setupBookmark(
        command: Command.SetupBookmark
    ): Payload = factory.remote.setupBookmark(command.toEntity()).toDomain()

    override suspend fun uploadUrl(command: Command.UploadVideoBlockUrl) {
        factory.remote.uploadUrl(command.toEntity())
    }

    override suspend fun undo(command: Command.Undo) = factory.remote.undo(command.toEntity())

    override suspend fun redo(command: Command.Redo) = factory.remote.redo(command.toEntity())

    override suspend fun archiveDocument(
        command: Command.ArchiveDocument
    ) = factory.remote.archiveDocument(command.toEntity())

    override suspend fun paste(
        command: Command.Paste
    ): Clipboard.Paste.Response = factory.remote.paste(command.toEntity()).toDomain()
}