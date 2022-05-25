package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for remove icon from object.
 */
class RemoveDocumentIcon(
    private val repo: BlockRepository
) : BaseUseCase<Payload, RemoveDocumentIcon.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.removeDocumentIcon(params.ctx)
    }

    /**
     * Params for for setting document's emoji icon
     * @property [ctx] id of the object, whose icon we need to remove.
     */
    data class Params(
        val ctx: Id
    )
}