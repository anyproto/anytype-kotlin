package com.agileburo.anytype.domain.event.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.FlowUseCase
import com.agileburo.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

/**
 * Use-case for intercepting stream of [Event].
 * @property channel event channel
 * @property context coroutine context for this event channel
 */
class InterceptEvents(
    private val context: CoroutineContext,
    private val channel: EventChannel
) : FlowUseCase<List<Event>, BaseUseCase.None>() {

    override fun build(params: BaseUseCase.None?): Flow<List<Event>> {
        return channel.observeEvents().flowOn(context)
    }
}