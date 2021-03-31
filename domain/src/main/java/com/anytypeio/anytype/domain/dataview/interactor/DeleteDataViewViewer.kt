package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer.Params
import com.anytypeio.anytype.core_models.Payload


/**
 * Use-case for deleting a view from data view.
 * @see [Params] for details.
 */
class DeleteDataViewViewer(
    private val repo: BlockRepository
) : BaseUseCase<Payload, Params>() {

    override suspend fun run(params: Params) = safe {
        repo.removeDataViewViewer(
            ctx = params.ctx,
            dataview = params.dataview,
            viewer = params.viewer
        )
    }

    /**
     * @property [context] operation context
     * @property [dataview] id of the data view containing a viewer, which we need to delete
     * @property [viewer] id of the viewer, which we need to delete
     */
    class Params(
        val ctx: Id,
        val dataview: Id,
        val viewer: Id
    )
}