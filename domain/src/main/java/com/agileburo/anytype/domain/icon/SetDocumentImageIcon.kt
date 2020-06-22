package com.agileburo.anytype.domain.icon

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository

class SetDocumentImageIcon(
    private val repo: BlockRepository
) : BaseUseCase<Unit, SetDocumentImageIcon.Params>() {

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