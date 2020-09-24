package com.anytypeio.anytype.domain.event.interactor

import com.anytypeio.anytype.domain.base.FlowUseCase
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

/**
 * Use-case for intercepting stream of [Event].
 * @property channel event channel
 * @property context coroutine context for this event channel
 */
open class InterceptEvents(
    private val context: CoroutineContext,
    private val channel: EventChannel
) : FlowUseCase<List<Event>, InterceptEvents.Params>() {

    override fun build(params: Params?): Flow<List<Event>> {
        return channel.observeEvents(params?.context).flowOn(context)
    }

    /**
     * @property context optional event's context used for filtering.
     * If a context is provided, only events related to this context will be intercepted.
     */
    data class Params(val context: Id? = null)
}