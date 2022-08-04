package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a new status value to record.
 */
@Deprecated("Part of soon-to-be-deprecated API")
class AddStatusToDataViewRecord(
    private val repo: BlockRepository
) : BaseUseCase<Unit, AddStatusToDataViewRecord.Params>() {

    override suspend fun run(params: Params) = safe {
        val updated = mapOf(params.relation to listOf(params.status))
        repo.updateDataViewRecord(
            context = params.ctx,
            target = params.dataview,
            record = params.obj,
            values = updated
        )
    }

    /**
     * @property [ctx] operation context
     * @property [dataview] data view id
     * @property [viewer] active viewer id
     * @property [relation] relation id or relation key
     * @property [status] status id or selection option id
     * @property [obj] id of the object that this [record] represents
     * @property [record] record, whose tag value we need to update
     */
    data class Params(
        val ctx: Id,
        val dataview: Id,
        val viewer: Id,
        val relation: Id,
        val record: DVRecord,
        val status: Id,
        val obj: Id
    )
}