package com.agileburo.anytype.domain.event.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.FlowUseCase
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.collect

class ObserveEvents(
    private val repo: BlockRepository
) : FlowUseCase<Event, BaseUseCase.None>() {

    override suspend fun build(params: BaseUseCase.None?) = repo.observeEvents()

    override suspend fun stream(receiver: suspend (Event) -> Unit) {
        build().collect { receiver(it) }
    }
}