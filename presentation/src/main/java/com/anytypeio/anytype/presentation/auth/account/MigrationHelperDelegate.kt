package com.anytypeio.anytype.presentation.auth.account

import com.anytypeio.anytype.domain.auth.interactor.CancelAccountMigration
import com.anytypeio.anytype.domain.auth.interactor.MigrateAccount
import com.anytypeio.anytype.domain.base.Resultat
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

interface MigrationHelperDelegate {

    val migrationState: Flow<State>

    suspend fun onStartMigrationRequested()
    suspend fun onCancelMigrationRequested()

    class Impl @Inject constructor(
        private val migrateAccount: MigrateAccount,
        private val cancelAccountMigrateAccount: CancelAccountMigration
    ) : MigrationHelperDelegate {

        private val events = MutableSharedFlow<Event>(replay = 0)

        override val migrationState = events.flatMapLatest { event ->
            when(event) {
                Event.OnCancel -> {
                    cancelAccountMigrateAccount
                        .stream(CancelAccountMigration.Params.Current)
                        .map { result ->
                            when(result) {
                                is Resultat.Failure -> State.Failed(result.exception)
                                is Resultat.Loading -> State.InProgress
                                is Resultat.Success -> State.Cancelled
                            }
                        }
                }
                Event.OnStart -> {
                    migrateAccount
                        .stream(MigrateAccount.Params.Current)
                        .map { result ->
                            when(result) {
                                is Resultat.Failure -> State.Failed(result.exception)
                                is Resultat.Loading -> State.InProgress
                                is Resultat.Success -> State.Migrated
                            }
                        }
                }
            }
        }

        override suspend fun onStartMigrationRequested() {
            events.emit(Event.OnStart)
        }

        override suspend fun onCancelMigrationRequested() {
            events.emit(Event.OnCancel)
        }
    }

    sealed class Event {
        data object OnStart : Event()
        data object OnCancel : Event()
    }

    sealed class State {
        data object Init: State()
        data object InProgress : State()
        data class Failed(val error: Throwable) : State()
        data object Migrated : State()
        data object Cancelled : State()
    }
}