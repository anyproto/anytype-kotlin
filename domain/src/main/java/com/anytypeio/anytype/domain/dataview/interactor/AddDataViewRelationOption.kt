package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a option to a relation.
 */
class AddDataViewRelationOption(
    private val repo: BlockRepository
) : BaseUseCase<Pair<Payload, Id?>, AddDataViewRelationOption.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.addDataViewRelationOption(
            ctx = params.ctx,
            dataview = params.dataview,
            relation = params.relation,
            record = params.record,
            name = params.name,
            color = params.color
        )
    }

    /**
     * @property [ctx] operation context
     * @property [dataview] dataview id
     * @property [relation] relation id or relation key
     * @property [name] name for new option
     * @property [color] color code
     */
    class Params(
        val ctx: Id,
        val dataview: Id,
        val relation: Id,
        val record: Id,
        val name: String,
        val color: String
    )
}