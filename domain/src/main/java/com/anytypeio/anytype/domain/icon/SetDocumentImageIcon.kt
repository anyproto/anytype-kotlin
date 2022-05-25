package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetDocumentImageIcon(
    private val repo: BlockRepository
) : BaseUseCase<Pair<Payload, Hash>, SetDocumentImageIcon.Params>() {

    override suspend fun run(params: Params) = safe {
        val hash = repo.uploadFile(
            command = Command.UploadFile(
                path = params.path,
                type = Block.Content.File.Type.IMAGE
            )
        )
        val payload = repo.setDocumentImageIcon(
            command = Command.SetDocumentImageIcon(
                hash = hash,
                context = params.context
            )
        )
        Pair(payload, hash)
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