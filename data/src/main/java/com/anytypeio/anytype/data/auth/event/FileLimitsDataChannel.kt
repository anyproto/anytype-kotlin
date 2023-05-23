package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.domain.workspace.FileLimitsEventChannel
import kotlinx.coroutines.flow.Flow

class FileLimitsDataChannel(
    private val channel: FileLimitsRemoteChannel
) : FileLimitsEventChannel {

    override fun observe(): Flow<List<FileLimitsEvent>> {
        return channel.observe()
    }
}