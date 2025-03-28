package com.anytypeio.anytype.presentation.auth.account

import com.anytypeio.anytype.core_models.exceptions.MigrationFailedException
import com.anytypeio.anytype.domain.auth.interactor.MigrateAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface MigrationHelperDelegate {

    suspend fun proceedWithMigration() : Flow<State>

    class Impl @Inject constructor(
        private val migrateAccount: MigrateAccount,
        private val dispatchers: AppCoroutineDispatchers
    ) : MigrationHelperDelegate {

        override suspend fun proceedWithMigration(): Flow<State> {
            return migrateAccount
                .stream(MigrateAccount.Params.Current)
                .map { result ->
                    when(result) {
                        is Resultat.Failure -> {
                            val exception = result.exception
                            if (exception is MigrationFailedException.NotEnoughSpace) {
                                State.Failed.NotEnoughSpace(
                                    requiredSpaceInMegabytes = (exception.requiredSpaceInBytes / 1_048_576)
                                )
                            } else {
                                State.Failed.UnknownError(result.exception)
                            }
                        }
                        is Resultat.Loading -> State.InProgress
                        is Resultat.Success -> State.Migrated
                    }
                }
                .flowOn(dispatchers.io)
        }
    }

    sealed class State {
        data object Init: State()
        data object InProgress : State()
        sealed class Failed : State() {
            data class UnknownError(val error: Throwable) : Failed()
            data class NotEnoughSpace(val requiredSpaceInMegabytes: Long) : Failed()
        }
        data object Migrated : State()
    }
}