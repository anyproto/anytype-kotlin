package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Process
import com.anytypeio.anytype.domain.workspace.EventProcessDropFilesChannel
import com.anytypeio.anytype.domain.workspace.EventProcessImportChannel
import com.anytypeio.anytype.domain.workspace.EventProcessMigrationChannel
import kotlinx.coroutines.flow.Flow

interface EventProcessImportRemoteChannel {
    fun observe(): Flow<List<Process.Event.Import>>
}

class EventProcessImportDateChannel(
    private val channel: EventProcessImportRemoteChannel
) : EventProcessImportChannel {

    override fun observe(): Flow<List<Process.Event.Import>> {
        return channel.observe()
    }
}

interface EventProcessDropFilesRemoteChannel {
    fun observe(): Flow<List<Process.Event.DropFiles>>
}

class EventProcessDropFilesDateChannel(
    private val channel: EventProcessDropFilesRemoteChannel
) : EventProcessDropFilesChannel {

    override fun observe(): Flow<List<Process.Event.DropFiles>> {
        return channel.observe()
    }
}

interface EventProcessMigrationRemoteChannel {
    fun observe(): Flow<List<Process.Event.Migration>>
}

class EventProcessMigrationDateChannel(
    private val channel: EventProcessMigrationRemoteChannel
) : EventProcessMigrationChannel {

    override fun observe(): Flow<List<Process.Event.Migration>> {
        return channel.observe()
    }
}
