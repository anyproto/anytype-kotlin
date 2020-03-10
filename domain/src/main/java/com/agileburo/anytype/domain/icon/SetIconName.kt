package com.agileburo.anytype.domain.icon

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for setting emoji icon's short name
 */
class SetIconName(private val repo: BlockRepository) : BaseUseCase<Any, SetIconName.Params>() {

    override suspend fun run(params: Params) = try {
        repo.setIconName(
            command = Command.SetIconName(
                context = params.context,
                target = params.target,
                name = params.name
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for setting icon name
     * @property name emoji's short-name code
     * @property target id of the target block (icon)
     * @property context id of the context for this operation
     */
    data class Params(
        val name: String,
        val target: Id,
        val context: Id
    )
}