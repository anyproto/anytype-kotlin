package com.anytypeio.anytype.presentation.common

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

/**
 * Might be used for cases where object payload received in use-case response can be delegated to some other component, which should handle it.
 * Basically, an event bus, which should be used as singleton.
 */
interface PayloadDelegator {
    suspend fun dispatch(payload: Payload)
    fun intercept(ctx: Id) : Flow<Payload>

    class Default @Inject constructor() : PayloadDelegator {
        val shared = MutableSharedFlow<Payload>(replay = 0)

        override suspend fun dispatch(payload: Payload) {
            shared.emit(payload)
        }

        override fun intercept(ctx: Id): Flow<Payload> {
            return shared.filter { it.context == ctx }
        }
    }
}