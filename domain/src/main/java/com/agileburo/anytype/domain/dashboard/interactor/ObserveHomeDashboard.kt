package com.agileburo.anytype.domain.dashboard.interactor

import com.agileburo.anytype.domain.base.FlowUseCase
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class ObserveHomeDashboard(
    private val context: CoroutineContext,
    private val repo: BlockRepository
) : FlowUseCase<HomeDashboard, ObserveHomeDashboard.Param>() {

    override suspend fun build(params: Param?) = repo
        .observeEvents()
        .filter { it is Event.Command.ShowBlock }
        .map { it as Event.Command.ShowBlock }
        .filter { isDashboardBlock(it) }
        .map { event -> event.blocks.toHomeDashboard(event.rootId) }
        .flowOn(context)

    private fun isDashboardBlock(
        event: Event.Command.ShowBlock
    ): Boolean {
        val target = event.blocks.find { it.id == event.rootId }
        if (target != null)
            return target.content is Block.Content.Dashboard
        else
            throw IllegalStateException("Could not found any block corresponding to the root id")
    }

    data class Param(val id: String)

}