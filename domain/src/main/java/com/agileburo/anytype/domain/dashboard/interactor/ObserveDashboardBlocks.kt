package com.agileburo.anytype.domain.dashboard.interactor

import com.agileburo.anytype.domain.base.FlowUseCase
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository

class ObserveDashboardBlocks(
    private val repository: BlockRepository
) : FlowUseCase<List<Block>, Unit>() {

    override suspend fun build(
        params: Unit?
    ) = repository.observeBlocks()
}