package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetLinkAppearance(
    private val repository: BlockRepository,
) : BaseUseCase<Payload, SetLinkAppearance.Params>() {

    data class Params(
        val contextId: String,
        val blockId: String,
        val content: Block.Content.Link
    )

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repository.setLinkAppearance(
            Command.SetLinkAppearance(
                contextId = params.contextId,
                blockId = params.blockId,
                content = params.content
            )
        )
    }
}