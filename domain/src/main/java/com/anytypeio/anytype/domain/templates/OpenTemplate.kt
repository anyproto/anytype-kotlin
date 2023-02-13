package com.anytypeio.anytype.domain.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.templates.OpenTemplate.Params
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use-case for opening a template object.
 * @see Params
 */
class OpenTemplate(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Params, Result<Payload>>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result<Payload> {
        return withContext(dispatchers.io) { repo.openObjectPreview(params.id) }
    }

    /**
     * @property [id] id of the template object.
     */
    class Params(val id: Id)
}