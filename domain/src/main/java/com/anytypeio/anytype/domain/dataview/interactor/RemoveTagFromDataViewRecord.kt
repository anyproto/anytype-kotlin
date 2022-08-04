package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

@Deprecated("Part of soon-to-be-deprecated API")
class RemoveTagFromDataViewRecord(
    private val repo: BlockRepository
) : BaseUseCase<Unit, RemoveTagFromDataViewRecord.Params>() {

    override suspend fun run(params: Params) = safe {
        val tags = mutableListOf<String>()
        params.record[params.relation]?.let { currentTags ->
            if (currentTags is List<*>) {
                currentTags.forEach { tagItem ->
                    if (tagItem is String && tagItem != params.tag) tags.add(tagItem)
                }
            }
        }
        val updated = mapOf(params.relation to tags.toSet().toList())

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
     * @property [tag] tag id or selection option id
     * @property [target] id of the object that this [record] represents
     * @property [record] record, whose tag value we need to update
     */
    data class Params(
        val ctx: Id,
        val dataview: Id,
        val viewer: Id,
        val relation: Id,
        val record: DVRecord,
        val tag: Id,
        val target: Id
    )
}