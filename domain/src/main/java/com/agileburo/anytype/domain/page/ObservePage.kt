package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.FlowUseCase
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository

class ObservePage(private val repo: BlockRepository) :
    FlowUseCase<List<Block>, BaseUseCase.None>() {

    override suspend fun build(params: BaseUseCase.None?) = repo.observePages()
}