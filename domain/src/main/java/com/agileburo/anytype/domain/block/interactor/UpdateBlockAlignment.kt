package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

open class UpdateBlockAlignment(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateBlockAlignment.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = try {
        repo.updateAlignment(
            command = Command.UpdateAlignment(
                context = params.context,
                targets = params.targets,
                alignment = params.alignment
            )
        ).let {
            Either.Right(it)
        }
    } catch (e: Exception) {
        Either.Left(e)
    }

    /**
     * Params for updating alignment of the whole block.
     * @property context context id
     * @property targets id of the target block, whose alignment we need to update.
     * @property alignment new alignment
     */
    data class Params(
        val context: Id,
        val targets: List<Id>,
        val alignment: Block.Align
    )
}