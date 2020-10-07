package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.event.model.Payload

class SetDocumentImageIcon(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetDocumentImageIcon.Params>() {

    override suspend fun run(params: Params) = safe {
        val hash = repo.uploadFile(
            command = Command.UploadFile(
                path = params.path,
                type = Block.Content.File.Type.IMAGE
            )
        )
        repo.setDocumentImageIcon(
            command = Command.SetDocumentImageIcon(
                hash = hash,
                context = params.context
            )
        )
    }

    /**
     * Params for for setting document's image icon
     * @property path image path in file system
     * @property context id of the context for this operation
     */
    class Params(
        val context: String,
        val path: String
    )
}