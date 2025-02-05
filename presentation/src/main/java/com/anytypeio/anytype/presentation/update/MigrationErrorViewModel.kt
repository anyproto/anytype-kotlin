package com.anytypeio.anytype.presentation.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Login -> migration error
 * App start, authorized - migration error
 */
class MigrationErrorViewModel(
    private val analytics: Analytics,
    private val delegate: MigrationHelperDelegate,
    private val subscriptions: GlobalSubscriptionManager,
    private val launchAccount: LaunchAccount
) : ViewModel(), MigrationHelperDelegate by delegate {

    val commands = MutableSharedFlow<Command>()

    init {
        viewModelScope.launch {
            proceedWithMigration().collect { state ->
                when(state) {
                    MigrationHelperDelegate.State.Migrated -> {
                        launchAccount.invoke(BaseUseCase.None).proceed(
                            failure = {
                                // TODO
                            },
                            success = {
                                subscriptions.onStart()
                                commands.emit(Command.Restart)
                            }
                        )
                    }
                    else -> {
                        Timber.d("Migration state: $state")
                    }
                }
            }
        }
    }

    sealed interface Command {
        data object Restart: Command
    }

    class Factory @Inject constructor(
        private val analytics: Analytics,
        private val delegate: MigrationHelperDelegate,
        private val subscriptions: GlobalSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MigrationErrorViewModel(
                analytics = analytics,
                delegate = delegate,
                subscriptions = subscriptions
            ) as T
        }
    }

    companion object
}