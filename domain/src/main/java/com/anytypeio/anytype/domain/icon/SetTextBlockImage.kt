package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetTextBlockImage(
    private val repo: BlockRepository
) : SetImageIcon<TextBlockTarget>() {

    override suspend fun run(
        params: Params<TextBlockTarget>
    ) = safe {
        val hash = repo.uploadFile(
            command = Command.UploadFile(
                path = params.path,
                type = Block.Content.File.Type.IMAGE
            )
        )
        val payload = repo.setTextIcon(
            command = Command.SetTextIcon(
                icon = Command.SetTextIcon.Icon.Image(hash),
                context = params.target.context,
                blockId = params.target.blockId
            )
        )
        Pair(payload, hash)
    }
}