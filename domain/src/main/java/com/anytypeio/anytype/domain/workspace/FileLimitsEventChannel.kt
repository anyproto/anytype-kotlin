package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.FileLimitsEvent
import kotlinx.coroutines.flow.Flow

interface FileLimitsEventChannel {
    fun observe() : Flow<List<FileLimitsEvent>>
}