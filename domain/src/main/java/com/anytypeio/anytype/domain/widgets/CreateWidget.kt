package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultatInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class CreateWidget(
    private val dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultatInteractor<CreateWidget.Params, Unit>() {

    override suspend fun execute(params: Params) {
        repo.createWidget(
            ctx = params.ctx,
            source = params.source
        )
    }

    data class Params(
        val ctx: Id,
        val source: Id
    )
}