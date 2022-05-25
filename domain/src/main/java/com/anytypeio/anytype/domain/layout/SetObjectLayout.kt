package com.anytypeio.anytype.domain.layout

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetObjectLayout(private val repo: BlockRepository): BaseUseCase<Payload, SetObjectLayout.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setObjectLayout(
            ctx = params.ctx,
            layout = params.layout
        )
    }

    data class Params(
        val ctx: Id,
        val layout: ObjectType.Layout
    )
}