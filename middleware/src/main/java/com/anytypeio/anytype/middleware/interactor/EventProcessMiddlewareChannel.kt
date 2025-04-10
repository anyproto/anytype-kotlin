package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Process
import com.anytypeio.anytype.data.auth.event.EventProcessDropFilesRemoteChannel
import com.anytypeio.anytype.data.auth.event.EventProcessImportRemoteChannel
import com.anytypeio.anytype.data.auth.event.EventProcessMigrationRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class EventProcessDropFilesMiddlewareChannel(
    private val events: EventProxy
) : EventProcessDropFilesRemoteChannel {

    override fun observe(): Flow<List<Process.Event.DropFiles>> {
        return events.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    val eventProcessNew = message.processNew
                    val eventProcessUpdate = message.processUpdate
                    val eventProcessDone = message.processDone

                    when {
                        eventProcessNew != null -> {
                            val process = eventProcessNew.process
                            val processType = process?.dropFiles
                            if (processType != null) {
                                Process.Event.DropFiles.New(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }

                        eventProcessUpdate != null -> {
                            val process = eventProcessUpdate.process
                            val processType = process?.dropFiles
                            if (processType != null) {
                                Process.Event.DropFiles.Update(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }

                        eventProcessDone != null -> {
                            val process = eventProcessDone.process
                            val processType = process?.dropFiles
                            if (processType != null) {
                                Process.Event.DropFiles.Done(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }

                        else -> null
                    }
                }
            }
    }
}

class EventProcessImportMiddlewareChannel(
    private val events: EventProxy
) : EventProcessImportRemoteChannel {

    override fun observe(): Flow<List<Process.Event.Import>> {
        return events.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    val eventProcessNew = message.processNew
                    val eventProcessUpdate = message.processUpdate
                    val eventProcessDone = message.processDone

                    when {
                        eventProcessNew != null -> {
                            val process = eventProcessNew.process
                            val processType = process?.import_
                            if (processType != null) {
                                Process.Event.Import.New(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }

                        eventProcessUpdate != null -> {
                            val process = eventProcessUpdate.process
                            val processType = process?.import_
                            if (processType != null) {
                                Process.Event.Import.Update(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }

                        eventProcessDone != null -> {
                            val process = eventProcessDone.process
                            val processType = process?.import_
                            if (processType != null) {
                                Process.Event.Import.Done(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }

                        else -> null
                    }
                }
            }
    }
}

class EventProcessMigrationMiddlewareChannel(
    private val events: EventProxy
) : EventProcessMigrationRemoteChannel {

    override fun observe(): Flow<List<Process.Event.Migration>> {
        return events.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    val eventProcessNew = message.processNew
                    val eventProcessUpdate = message.processUpdate
                    val eventProcessDone = message.processDone

                    when {
                        eventProcessNew != null -> {
                            val process = eventProcessNew.process
                            val processType = process?.migration
                            if (processType != null) {
                                Process.Event.Migration.New(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }
                        eventProcessUpdate != null -> {
                            val process = eventProcessUpdate.process
                            val processType = process?.migration
                            if (processType != null) {
                                Process.Event.Migration.Update(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }

                        eventProcessDone != null -> {
                            val process = eventProcessDone.process
                            val processType = process?.migration
                            if (processType != null) {
                                Process.Event.Migration.Done(
                                    process = process.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }
                        else -> null
                    }
                }
            }
    }
}