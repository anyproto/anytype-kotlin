package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use-case for setting active view id for widget with list or compact list layout.
 */
class SetWidgetActiveView @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetWidgetActiveView.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params) = repo.setWidgetViewId(
        ctx = params.ctx,
        widget = params.widget,
        view = params.view
    )

    data class Params(
        val ctx: Id,
        val widget: Id,
        val view: Id
    )
}