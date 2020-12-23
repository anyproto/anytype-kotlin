package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Payload

/**
 * Use-case for un-doing latest changes in document.
 */
class Undo(
    private val repo: BlockRepository
) : BaseUseCase<Undo.Result, Undo.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.undo(
            command = Command.Undo(
                context = params.context
            )
        )
    }

    /**
     * Params for un-doing latest changes in document.
     * @property context id of the context
     */
    data class Params(
        val context: Id
    )

    sealed class Result {
        data class Success(val payload: Payload) : Result()
        object Exhausted : Result()
    }
}