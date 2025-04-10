package com.anytypeio.anytype.presentation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Process.Event
import com.anytypeio.anytype.core_models.Process.State
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.workspace.EventProcessMigrationChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan

sealed class GenericProcessProgressState {
    data object Init : GenericProcessProgressState()
    data class Progress(val processId: Id, val progress: Float) : GenericProcessProgressState()
    data object Done : GenericProcessProgressState()
    data class Error(val error: Throwable) : GenericProcessProgressState()

    class MigrationProcessReducer @Inject constructor(
        private val dispatchers: AppCoroutineDispatchers,
        private val channel: EventProcessMigrationChannel
    ) {
        val state = channel
            .observe()
            .scan<List<Event.Migration>, GenericProcessProgressState>(
                initial = Init
            ) { state, events ->
                var result = state
                events.forEach { event ->
                    when (event) {
                        is Event.Migration.New -> {
                            val currentProgressState = result
                            if (currentProgressState is Init && event.process.state == State.RUNNING) {
                                result = Progress(
                                    processId = event.process.id,
                                    progress = 0f
                                )
                            } else {
                                // Some process is already running
                            }
                        }
                        is Event.Migration.Update -> {
                            val currentProgressState = result
                            val newProcess = event.process
                            if (currentProgressState is Progress
                                && currentProgressState.processId == event.process.id
                                && newProcess.state == State.RUNNING
                            ) {
                                val progress = newProcess.progress
                                val total = progress?.total
                                val done = progress?.done
                                result =
                                    if (total != null && total != 0L && done != null) {
                                        currentProgressState.copy(progress = done.toFloat() / total)
                                    } else {
                                        currentProgressState.copy(progress = 0f)
                                    }
                            }
                        }
                        is Event.Migration.Done -> {
                            val currentProgressState = result
                            if (currentProgressState is Progress
                                && event.process.state == State.DONE
                                && event.process.id == currentProgressState.processId
                            ) {
                                result = Done
                            }
                        }
                    }
                }
                result
            }
            .flowOn(dispatchers.io)
            .catch {
                emit(Error(it))
            }
    }
}