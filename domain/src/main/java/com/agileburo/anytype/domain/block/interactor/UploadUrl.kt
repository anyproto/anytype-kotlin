package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository

class UploadUrl(private val repo: BlockRepository) : BaseUseCase<Unit, UploadUrl.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = try {
        repo.uploadUrl(
            command = Command.UploadVideoBlockUrl(
                contextId = params.contextId,
                blockId = params.blockId,
                url = params.url,
                filePath = params.filePath
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    data class Params(
        val contextId: String,
        val blockId: String,
        val url: String,
        val filePath: String
    )
}