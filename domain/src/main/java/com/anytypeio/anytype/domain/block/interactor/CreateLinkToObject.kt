package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.interactor.CreateLinkToObject.Params
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for creating a link to existing object.
 * @see [Params] for details.
 */
class CreateLinkToObject(
    private val repo: BlockRepository
) : BaseUseCase<Payload, Params>() {

    override suspend fun run(params: Params) = safe {
        repo.linkToObject(
            context = params.context,
            target = params.target,
            block = params.block,
            replace = params.replace,
            position = params.position
        )
    }

    /**
     * Params for creating a link to existing object
     * @property [target] represents blocks, relative to which a link will be inserted.
     * @property [context] operation's context
     * @property [block] id of the block being inserted
     * @property [replace] if true, a link will replace [target] block, if false, a link will be inserted below [target]
     */
    data class Params(
        val context: Id,
        val block: Id,
        val target: Id,
        val replace: Boolean,
        val position: Position
    )
}