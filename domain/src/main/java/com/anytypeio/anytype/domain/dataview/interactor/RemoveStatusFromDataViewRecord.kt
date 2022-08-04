package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

@Deprecated("Part of soon-to-be-deprecated API")
class RemoveStatusFromDataViewRecord(
    private val repo: BlockRepository
) : BaseUseCase<Unit, RemoveStatusFromDataViewRecord.Params>() {

    override suspend fun run(params: Params) = safe {
        val status = mutableListOf<String>()
        params.record[params.relation]?.let { currentStatus ->
            if (currentStatus is List<*>) {
                currentStatus.forEach { statusItem ->
                    if (statusItem is String && statusItem != params.status) status.add(statusItem)
                }
            }
        }
        val updated = mapOf(params.relation to status.toSet().toList())

        repo.updateDataViewRecord(
            context = params.ctx,
            target = params.dataview,
            record = params.target,
            values = updated
        )
    }

    /**
     * @property [ctx] operation context
     * @property [dataview] data view id
     * @property [viewer] active viewer id
     * @property [relation] relation id or relation key
     * @property [status] status id or selection option id
     * @property [target] id of the object that this [record] represents
     * @property [record] record, whose tag value we need to update
     */
    data class Params(
        val ctx: Id,
        val dataview: Id,
        val viewer: Id,
        val relation: Id,
        val record: DVRecord,
        val status: Id,
        val target: Id
    )
}