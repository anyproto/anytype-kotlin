package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.ClickOnboardingButton
import com.anytypeio.anytype.core_models.DeviceNetworkType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.NetworkConnectionStatus
import com.anytypeio.anytype.domain.network.NetworkModeProvider
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingClickEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import com.anytypeio.anytype.presentation.extension.sendOpenAccountEvent
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingMnemonicViewModel @Inject constructor(
    private val getMnemonic: GetMnemonic,
    private val analytics: Analytics,
    private val configStorage: ConfigStorage,
    private val networkModeProvider: NetworkModeProvider,
    private val networkConnectionStatus: NetworkConnectionStatus,
    private val pendingIntentStore: PendingIntentStore
) : ViewModel() {

    val state = MutableStateFlow<State>(State.Idle(""))
    val commands = MutableSharedFlow<Command>()

    init {
        Timber.i("OnboardingMnemonicViewModel, init")
        sendScreenViewAnalytics()
        viewModelScope.launch {
            proceedWithMnemonicPhrase()
        }
    }

    fun openMnemonic() {
        if (state.value is State.Mnemonic) {
            state.value = State.MnemonicOpened((state.value as State.Mnemonic).mnemonicPhrase)
        }
        sendClickAnalytics(ClickOnboardingButton.SHOW_AND_COPY)
    }

    fun onCheckLaterClicked(space: Id, startingObject: Id?) {
        sendClickAnalytics(ClickOnboardingButton.CHECK_LATER)
        viewModelScope.launch {
            navigateNextStep(space, startingObject)
        }
    }

    fun handleAppEntryClick(space: Id, startingObject: Id?) {
        viewModelScope.launch {
            navigateNextStep(space, startingObject)
        }
    }

    private suspend fun navigateNextStep(space: Id, startingObject: Id?) {
        if (shouldShowEmail()) {
            emitNavigateToAddEmail(space, startingObject)
            return
        }

        logOpenAccountIfAvailable()

        val deeplink = pendingIntentStore.getDeepLinkInvite()
        when {
            !deeplink.isNullOrEmpty() -> emitCommand(Command.OpenVault)
            !startingObject.isNullOrEmpty() -> emitCommand(
                Command.OpenStartingObject(
                    space = SpaceId(space),
                    startingObject = startingObject
                )
            )

            else -> emitCommand(Command.OpenVault)
        }
    }

    private suspend fun emitNavigateToAddEmail(space: Id, startingObject: Id?) {
        emitCommand(
            Command.NavigateToAddEmailScreen(
                space = space,
                startingObject = startingObject
            )
        )
    }

    private suspend fun emitCommand(command: Command) {
        commands.emit(command)
    }

    private suspend fun logOpenAccountIfAvailable() {
        val config = configStorage.getOrNull()
        if (config != null) {
            analytics.sendOpenAccountEvent(config.analytics)
        } else {
            Timber.w("Missing config during onboarding")
        }
    }

    private fun sendScreenViewAnalytics() {
        viewModelScope.sendAnalyticsOnboardingScreenEvent(
            analytics,
            EventsDictionary.ScreenOnboardingStep.PHRASE
        )
    }

    fun shouldShowEmail(): Boolean {
        val networkStatus = networkConnectionStatus.getCurrentNetworkType()
        if (networkStatus == DeviceNetworkType.NOT_CONNECTED) {
            Timber.i("Network is not connected, skipping email screen")
            return false
        }
        return networkModeProvider.get().networkMode != NetworkMode.LOCAL
    }

    private suspend fun proceedWithMnemonicPhrase() {
        getMnemonic.invoke(Unit).proceed(
            failure = { e -> Timber.e(e, "Error while getting mnemonic") },
            success = { mnemonic ->
                state.value = State.Mnemonic(mnemonic)
                Any()
            }
        )
    }

    private fun sendClickAnalytics(type: ClickOnboardingButton) {
        viewModelScope.sendAnalyticsOnboardingClickEvent(
            analytics = analytics,
            type = type,
            step = EventsDictionary.ScreenOnboardingStep.PHRASE
        )
    }

    sealed interface State {

        val mnemonicPhrase: String

        class Idle(override val mnemonicPhrase: String) : State
        class Mnemonic(override val mnemonicPhrase: String) : State
        class MnemonicOpened(override val mnemonicPhrase: String) : State
    }

    class Factory @Inject constructor(
        private val getMnemonic: GetMnemonic,
        private val analytics: Analytics,
        private val configStorage: ConfigStorage,
        private val networkModeProvider: NetworkModeProvider,
        private val networkConnectionStatus: NetworkConnectionStatus,
        private val pendingIntentStore: PendingIntentStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicViewModel(
                getMnemonic = getMnemonic,
                analytics = analytics,
                configStorage = configStorage,
                networkModeProvider = networkModeProvider,
                networkConnectionStatus = networkConnectionStatus,
                pendingIntentStore = pendingIntentStore
            ) as T
        }
    }

    sealed class Command {
        data object OpenVault : Command()
        data class OpenStartingObject(
            val space: SpaceId,
            val startingObject: Id
        ) : Command()

        data class NavigateToAddEmailScreen(
            val startingObject: String?,
            val space: String
        ) : Command()
    }
}