package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.dataview.interactor.RenameDataViewViewer.Params
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor

/**
 * Use-case for renaming data view's view.
 * @see [Params] for details.
 */
class RenameDataViewViewer(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        return repo.updateDataViewViewer(
            context = params.context,
            target = params.target,
            viewer = params.viewer
        )
    }

    /**
     * @property [context] operation context (can be object-set's id or something else)
     * @property [target] id of the data-view block
     * @property [viewer] specific viewer of this [target], whose name is considered updated.
     */
    data class Params(
        val context: Id,
        val target: Id,
        val viewer: DVViewer
    )
}