package com.anytypeio.anytype.presentation.auth.account

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Process
import com.anytypeio.anytype.core_models.Process.Event
import com.anytypeio.anytype.core_models.exceptions.MigrationFailedException
import com.anytypeio.anytype.domain.auth.interactor.MigrateAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.workspace.EventProcessMigrationChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan

interface MigrationHelperDelegate {

    suspend fun proceedWithMigration() : Flow<State>

    class Impl @Inject constructor(
        private val migrateAccount: MigrateAccount,
        private val dispatchers: AppCoroutineDispatchers,
        private val processProgressObserver: MigrationProgressObserver
    ) : MigrationHelperDelegate {

        override suspend fun proceedWithMigration(): Flow<State> {
            return migrateAccount
                .stream(MigrateAccount.Params.Current)
                .flatMapLatest { result ->
                    flow {
                        when(result) {
                            is Resultat.Failure -> {
                                val exception = result.exception
                                if (exception is MigrationFailedException.NotEnoughSpace) {
                                    emit(
                                        State.Failed.NotEnoughSpace(
                                            requiredSpaceInMegabytes = (exception.requiredSpaceInBytes / 1_048_576)
                                        )
                                    )
                                } else {
                                    emit(State.Failed.UnknownError(result.exception))
                                }
                            }
                            is Resultat.Loading -> {
                                emitAll(processProgressObserver.state)
                            }
                            is Resultat.Success -> {
                                emit(State.Migrated)
                            }
                        }

                    }
                }
                .flowOn(dispatchers.io)
        }
    }

    sealed class State {
        data object Init: State()
        sealed class InProgress : State() {
            data object Idle : InProgress()
            data class Progress(val processId: Id, val progress: Float) : InProgress()
        }
        sealed class Failed : State() {
            data class UnknownError(val error: Throwable) : Failed()
            data class NotEnoughSpace(val requiredSpaceInMegabytes: Long) : Failed()
        }
        data object Migrated : State()
    }
}

typealias MigrationEvents = List<Event.Migration>

class MigrationProgressObserver @Inject constructor(
    channel: EventProcessMigrationChannel
) {
    val state : Flow<MigrationHelperDelegate.State> = channel
        .observe()
        .scan<MigrationEvents, MigrationHelperDelegate.State>(
            initial = MigrationHelperDelegate.State.InProgress.Idle
        ) { state, events ->
            var result = state
            events.forEach { event ->
                when (event) {
                    is Event.Migration.New -> {
                        if (result is  MigrationHelperDelegate.State.InProgress.Idle && event.process.state == Process.State.RUNNING) {
                            result = MigrationHelperDelegate.State.InProgress.Progress(
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
                        if (currentProgressState is MigrationHelperDelegate.State.InProgress.Progress
                            && currentProgressState.processId == event.process.id
                            && newProcess.state == Process.State.RUNNING
                        ) {
                            val progress = newProcess.progress
                            val total = progress?.total
                            val done = progress?.done
                            result =
                                if (total != null && total != 0L && done != null) {
                                    currentProgressState.copy(
                                        progress = (done.toFloat() / total).coerceIn(0f, 1f)
                                    )
                                } else {
                                    currentProgressState.copy(progress = 0f)
                                }
                        }
                    }
                    is Event.Migration.Done -> {
                        val currentProgressState = result
                        if (currentProgressState is MigrationHelperDelegate.State.InProgress.Progress
                            && event.process.state == Process.State.DONE
                            && event.process.id == currentProgressState.processId
                        ) {
                            result = MigrationHelperDelegate.State.Migrated
                        }
                    }
                }
            }
            result
        }
        .distinctUntilChanged()
        .catch {
            emit(MigrationHelperDelegate.State.Failed.UnknownError(it))
        }
}