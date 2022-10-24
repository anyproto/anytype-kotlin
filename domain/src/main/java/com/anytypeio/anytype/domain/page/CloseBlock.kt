package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for closing a smart block by id.
 * @see Params
 */
open class CloseBlock(
    private val repo: BlockRepository
) : ResultInteractor<Id, Unit>() {

    override suspend fun doWork(params: Id) = repo.closePage(params)
}