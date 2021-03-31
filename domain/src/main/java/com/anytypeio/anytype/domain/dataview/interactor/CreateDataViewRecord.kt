package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id

/**
 * Use-case for creating a new record inside data view's database.
 */
class CreateDataViewRecord(
    private val repo: BlockRepository
) : BaseUseCase<DVRecord, CreateDataViewRecord.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createDataViewRecord(
            context = params.context,
            target = params.target
        )
    }

    /**
     * @property [context] operation's context
     * @property [target] data-view's block id
     */
    class Params(
        val context: Id,
        val target: Id,
    )
}