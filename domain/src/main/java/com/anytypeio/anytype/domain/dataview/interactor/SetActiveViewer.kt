package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for selecting active view on data-view.
 */
class SetActiveViewer(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetActiveViewer.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setActiveDataViewViewer(
            context = params.context,
            block = params.block,
            view = params.view,
            offset = params.offset,
            limit = params.limit
        )
    }

    /**
     * @property [context] operation context
     * @property [block] id of the data view block
     * @property [view] id of the view to set as active view on data-view
     * @property [offset] query offset
     * @property [limit] number of records returned for this [view],
     */
    data class Params(
        val context: Id,
        val block: Id,
        val view: Id,
        val offset: Int = 0,
        val limit: Int = 0
    )
}