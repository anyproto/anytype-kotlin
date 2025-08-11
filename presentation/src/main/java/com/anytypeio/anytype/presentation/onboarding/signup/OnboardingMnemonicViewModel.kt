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
import com.anytypeio.anytype.domain.device.NetworkConnectionStatus
import com.anytypeio.anytype.domain.network.NetworkModeProvider
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingClickEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingMnemonicViewModel @Inject constructor(
    private val getMnemonic: GetMnemonic,
    private val analytics: Analytics,
    private val networkModeProvider: NetworkModeProvider,
    private val networkConnectionStatus: NetworkConnectionStatus,
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

    fun onCheckLaterClicked(space: Id, startingObject: Id?, profileId: Id) {
        sendClickAnalytics(ClickOnboardingButton.CHECK_LATER)
        viewModelScope.launch {
            navigateNextStep(space, startingObject, profileId)
        }
    }

    fun handleAppEntryClick(space: Id, startingObject: Id?, profileId: Id) {
        viewModelScope.launch {
            navigateNextStep(space, startingObject, profileId)
        }
    }

    private suspend fun navigateNextStep(space: Id, startingObject: Id?, profileId: Id) {
        viewModelScope.launch {
            sendAnalyticsOnboardingScreenEvent(
                analytics,
                EventsDictionary.ScreenOnboardingStep.SOUL
            )
        }
        emitCommand(
            Command.NavigateToSetProfileName(
                space = space,
                startingObject = startingObject,
                profileId = profileId
            )
        )
    }

    private suspend fun emitCommand(command: Command) {
        commands.emit(command)
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
        private val networkModeProvider: NetworkModeProvider,
        private val networkConnectionStatus: NetworkConnectionStatus,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicViewModel(
                getMnemonic = getMnemonic,
                analytics = analytics,
                networkModeProvider = networkModeProvider,
                networkConnectionStatus = networkConnectionStatus,
            ) as T
        }
    }

    sealed class Command {
        data object OpenVault : Command()
        data class OpenStartingObject(
            val space: SpaceId,
            val startingObject: Id
        ) : Command()

        data class NavigateToSetProfileName(
            val space: String,
            val startingObject: String?,
            val profileId: String
        ) : Command()

        data class NavigateToAddEmailScreen(
            val startingObject: String?,
            val space: String
        ) : Command()
    }
}