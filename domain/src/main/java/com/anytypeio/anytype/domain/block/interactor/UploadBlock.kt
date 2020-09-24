package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.event.model.Payload

class UploadBlock(private val repo: BlockRepository) : BaseUseCase<Payload, UploadBlock.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.uploadBlock(
            command = Command.UploadBlock(
                contextId = params.contextId,
                blockId = params.blockId,
                url = params.url,
                filePath = params.filePath
            )
        )
    }

    data class Params(
        val contextId: String,
        val blockId: String,
        val url: String,
        val filePath: String
    )
}