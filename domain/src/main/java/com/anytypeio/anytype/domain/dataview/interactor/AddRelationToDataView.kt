package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding an existing relation to a data view.
 */
class AddRelationToDataView(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddRelationToDataView.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.addRelationToDataView(
            ctx = params.ctx,
            dv = params.dv,
            relation = params.relation
        )
    }

    /**
     * @property [ctx] operation context
     * @property [dv] data view's block id
     * @property [relation] id or key of the existing relation
     */
    class Params(
        val ctx: Id,
        val dv: Id,
        val relation: Id
    )
}