package com.anytypeio.anytype.domain.status

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

class InterceptThreadStatus(
    private val context: CoroutineContext = Dispatchers.IO,
    private val channel: ThreadStatusChannel,
) : FlowUseCase<SyncStatus, InterceptThreadStatus.Params>() {

    override fun build(params: Params?): Flow<SyncStatus> {
        checkNotNull(params) { "Params are required for this use-case." }
        return channel.observe(params.ctx).flowOn(context)
    }

    class Params(val ctx: Id)
}
