package com.agileburo.anytype.domain.dashboard.interactor

import com.agileburo.anytype.domain.base.FlowUseCase
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.flow.collect

class ObserveDashboardBlocks(
    private val repository: BlockRepository
) : FlowUseCase<List<Block>, Unit>() {

    override suspend fun build(
        params: Unit?
    ) = repository.observeBlocks()

    override suspend fun stream(receiver: suspend (List<Block>) -> Unit) {
        build().collect(receiver)
    }
}