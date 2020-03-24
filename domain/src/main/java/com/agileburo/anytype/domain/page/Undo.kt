package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for un-doing latest changes in document.
 */
class Undo(
    private val repo: BlockRepository
) : BaseUseCase<Unit, Undo.Params>() {

    override suspend fun run(params: Params) = try {
        repo.undo(
            command = Command.Undo(context = params.context)
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for un-doing latest changes in document.
     * @property context id of the context
     */
    data class Params(
        val context: Id
    )
}