package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetDocumentImageIcon(
    private val repo: BlockRepository
) : SetImageIcon<Id>() {

    override suspend fun run(params: Params<Id>) = safe {
        val file = repo.uploadFile(
            command = Command.UploadFile(
                path = params.path,
                type = Block.Content.File.Type.IMAGE,
                space = params.spaceId
            )
        )
        val payload = repo.setDocumentImageIcon(
            command = Command.SetDocumentImageIcon(
                id = file.id,
                context = params.target
            )
        )
        Pair(payload, file.id)
    }
}