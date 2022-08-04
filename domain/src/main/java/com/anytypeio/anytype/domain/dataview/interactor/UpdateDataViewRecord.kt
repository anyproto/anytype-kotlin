package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for updating data-view record.
 */
@Deprecated("Part of soon-to-be-deprecated API")
class UpdateDataViewRecord(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateDataViewRecord.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.updateDataViewRecord(
            context = params.context,
            target = params.target,
            record = params.record,
            values = params.values
        )
    }

    /**
     * @property [context] operation's context
     * @property [target] DV's block id.
     * @property [record] id of the specific record, which we need to update
     * @property [values] values, or raw data of this specific [record].
     */
    data class Params(
        val context: Id,
        val target: Id,
        val record: Id,
        val values: DVRecord
    )
}