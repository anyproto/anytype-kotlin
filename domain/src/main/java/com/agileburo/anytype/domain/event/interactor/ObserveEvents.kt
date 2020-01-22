package com.agileburo.anytype.domain.event.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.FlowUseCase
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

@Deprecated("Should use InterceptEvents")
class ObserveEvents(
    private val context: CoroutineContext,
    private val repo: BlockRepository
) : FlowUseCase<Event, BaseUseCase.None>() {

    override fun build(params: BaseUseCase.None?) = repo.observeEvents().flowOn(context)
}