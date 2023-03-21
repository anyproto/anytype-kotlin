package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.domain.base.ResultatInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateWidget @Inject constructor(
    private val repo: BlockRepository
) : ResultatInteractor<CreateWidget.Params, Payload>() {

    override suspend fun execute(params: Params): Payload = repo.createWidget(
        ctx = params.ctx,
        source = params.source,
        layout = params.type,
        target = params.target,
        position = params.position
    )

    data class Params(
        val ctx: Id,
        val source: Id,
        val type: WidgetLayout,
        val target: Id? = null,
        val position: Position = Position.NONE
    )
}