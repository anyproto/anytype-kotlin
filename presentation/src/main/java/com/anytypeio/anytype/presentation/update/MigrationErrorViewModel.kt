package com.anytypeio.anytype.presentation.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscription
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Login -> migration error
 * App start, authorized - migration error
 */
class MigrationErrorViewModel(
    private val analytics: Analytics,
    private val delegate: MigrationHelperDelegate,
    private val subscriptions: GlobalSubscriptionManager
) : ViewModel(), MigrationHelperDelegate by delegate {

    val commands = MutableSharedFlow<Command>()

    init {
        viewModelScope.launch {
            onStartMigrationRequested()
        }
    }

    sealed interface Command {
        object Exit: Command
        data class Browse(val url: Url): Command
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