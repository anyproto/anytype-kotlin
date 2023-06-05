package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.FileLimitsEvent
import kotlinx.coroutines.flow.Flow

interface FileLimitsRemoteChannel {
    fun observe(): Flow<List<FileLimitsEvent>>
}