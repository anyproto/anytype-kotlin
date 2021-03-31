package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for updating data view's viewer.
 */
class UpdateDataViewViewer(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateDataViewViewer.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.updateDataViewViewer(
            context = params.context,
            target = params.target,
            viewer = params.viewer
        )
    }

    /**
     * @property [context] operation context (can be object-set's id or something else)
     * @property [target] id of the data-view block
     * @property [target] specific viewer of this [target]
     */
    data class Params(
        val context: Id,
        val target: Id,
        val viewer: DVViewer
    )
}