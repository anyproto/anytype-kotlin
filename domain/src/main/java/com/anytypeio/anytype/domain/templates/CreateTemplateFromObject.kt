package com.anytypeio.anytype.domain.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateTemplateFromObject @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateTemplateFromObject.Params, Id>(
    dispatchers.io
) {

    override suspend fun doWork(params: Params): Id {
        return repo.createTemplateFromObject(params.obj)
    }

    data class Params(val ctx: Id, val obj: Id)
}