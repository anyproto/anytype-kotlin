package com.anytypeio.anytype.domain.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.withContext

class ApplyTemplate(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ApplyTemplate.Params, Unit>() {

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        repo.applyTemplate(
            ctx = params.ctx,
            template = params.template
        )
    }

    data class Params(
        val ctx: Id,
        val template: Id
    )
}