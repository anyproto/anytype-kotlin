package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for creating a new record inside data view's database.
 */
class CreateDataViewRecord(
    private val repo: BlockRepository
) : BaseUseCase<DVRecord, CreateDataViewRecord.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createDataViewRecord(
            context = params.context,
            target = params.target,
            template = params.template,
            prefilled = params.prefilled
        )
    }

    /**
     * @property [context] operation's context
     * @property [target] data-view's block id
     * @property [template] optional template for dv record
     * @property [prefilled] prefilled or pre-populated data for dv record
     */
    data class Params(
        val context: Id,
        val target: Id,
        val template: Id?,
        val prefilled: Map<Id, Any> = emptyMap()
    )
}