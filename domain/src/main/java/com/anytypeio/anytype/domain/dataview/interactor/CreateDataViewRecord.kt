package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for creating a new record inside data view's database.
 */
@Deprecated("Part of soon-to-be-deprecated API")
class CreateDataViewRecord(
    private val repo: BlockRepository
) : BaseUseCase<DVRecord, CreateDataViewRecord.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createDataViewRecord(
            context = params.context,
            target = params.target,
            template = params.template
        )
    }

    /**
     * @property [context] operation's context
     * @property [target] data-view's block id
     */
    data class Params(
        val context: Id,
        val target: Id,
        val template: Id?
    )
}