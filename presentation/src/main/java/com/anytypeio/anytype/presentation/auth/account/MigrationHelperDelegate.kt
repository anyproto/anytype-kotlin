package com.anytypeio.anytype.presentation.auth.account

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.interactor.CancelAccountMigration
import com.anytypeio.anytype.domain.auth.interactor.MigrateAccount
import com.anytypeio.anytype.domain.base.Resultat
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

interface MigrationHelperDelegate {

    val migrationState: Flow<State>

    suspend fun onStartMigrationRequested(account: Id)
    suspend fun onCancelMigrationRequested(account: Id)

    class Impl @Inject constructor(
        private val migrateAccount: MigrateAccount,
        private val cancelAccountMigrateAccount: CancelAccountMigration
    ) : MigrationHelperDelegate {

        private val events = MutableSharedFlow<Event>(replay = 0)

        override val migrationState = events.flatMapLatest { event ->
            when(event) {
                is Event.OnCancel -> {
                    cancelAccountMigrateAccount
                        .stream(CancelAccountMigration.Params.Other(event.account))
                        .map { result ->
                            when(result) {
                                is Resultat.Failure -> State.Failed(result.exception)
                                is Resultat.Loading -> State.InProgress
                                is Resultat.Success -> State.Cancelled
                            }
                        }
                }
                is Event.OnStart -> {
                    migrateAccount
                        .stream(MigrateAccount.Params.Other(event.account))
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

        override suspend fun onStartMigrationRequested(account: Id) {
            events.emit(Event.OnStart(account))
        }

        override suspend fun onCancelMigrationRequested(account: Id) {
            events.emit(Event.OnCancel(account))
        }
    }

    sealed class Event {
        data class OnStart(val account: Id) : Event()
        data class OnCancel(val account: Id) : Event()
    }

    sealed class State {
        data object Init: State()
        data object InProgress : State()
        data class Failed(val error: Throwable) : State()
        data object Migrated : State()
        data object Cancelled : State()
    }
}