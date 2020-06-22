package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.model.Payload

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