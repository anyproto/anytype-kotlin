package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a sort to a dataview's view.
 */
class AddDataViewViewerSort(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddDataViewViewerSort.Params>() {

    override suspend fun run(params: Params) = safe {
        val update = params.viewer.sorts.toMutableList().apply { add(params.sort) }
        repo.updateDataViewViewer(
            context = params.ctx,
            target = params.dataview,
            viewer = params.viewer.copy(sorts = update),
        )
    }

    data class Params(
        val ctx: Id,
        val dataview: Id,
        val viewer: DVViewer,
        val sort: DVSort
    )
}