package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for creating a new record inside data view's database.
 */
class CreateDataViewObject(
    private val repo: BlockRepository
) : BaseUseCase<Id, CreateDataViewObject.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createDataViewObject(
            template = params.template,
            prefilled = params.prefilled,
            type = params.type
        )
    }

    /**
     * @property [type] type of the new object
     * @property [template] optional template for dv record
     * @property [prefilled] prefilled or pre-populated data for dv record
     */
    data class Params(
        val type: Id,
        val template: Id?,
        val prefilled: Map<Id, Any> = emptyMap()
    )
}