package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.ProcessEvent
import kotlinx.coroutines.flow.Flow

interface EventProcessChannel {
    fun observe(): Flow<List<ProcessEvent>>
}