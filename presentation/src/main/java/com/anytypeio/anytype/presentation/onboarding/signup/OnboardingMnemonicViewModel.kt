package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
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
    private val networkConnectionStatus: NetworkConnectionStatus
) : ViewModel() {

    val state = MutableStateFlow<State>(State.Idle(""))
    val commands = MutableSharedFlow<Command>()

    init {
        Timber.i("OnboardingMnemonicViewModel, init")
        viewModelScope.sendAnalyticsOnboardingScreenEvent(analytics,
            EventsDictionary.ScreenOnboardingStep.PHRASE
        )
        viewModelScope.launch {
            proceedWithMnemonicPhrase()
        }
    }

    fun openMnemonic() {
        if (state.value is State.Mnemonic) {
            state.value = State.MnemonicOpened((state.value as State.Mnemonic).mnemonicPhrase)
        }
        viewModelScope.sendAnalyticsOnboardingClickEvent(
            analytics = analytics,
            type = EventsDictionary.ClickOnboardingButton.SHOW_AND_COPY,
            step = EventsDictionary.ScreenOnboardingStep.PHRASE
        )
    }

    fun onCheckLaterClicked(
        space: Id,
        startingObject: Id?,
    ) {
        viewModelScope.sendAnalyticsOnboardingClickEvent(
            analytics = analytics,
            type = EventsDictionary.ClickOnboardingButton.CHECK_LATER,
            step = EventsDictionary.ScreenOnboardingStep.PHRASE
        )
        if (shouldShowEmail()) {
            viewModelScope.launch {
                commands.emit(
                    Command.NavigateToAddEmailScreen(
                        startingObject = startingObject,
                        space = space
                    )
                )
            }
        } else {
            viewModelScope.launch {
                val config = configStorage.getOrNull()
                if (config != null) {
                    analytics.sendOpenAccountEvent(
                        analytics = config.analytics
                    )
                } else {
                    Timber.w("config was missing before the end of onboarding")
                }
                if (!startingObject.isNullOrEmpty()) {
                    commands.emit(
                        Command.OpenStartingObject(
                            space = SpaceId(space),
                            startingObject = startingObject
                        )
                    )
                } else {
                    commands.emit(Command.OpenVault)
                }
            }
        }
    }

    fun onGoToTheAppClicked(
        space: Id,
        startingObject: Id?,
    ) {
        if (shouldShowEmail()) {
            viewModelScope.launch {
                commands.emit(
                    Command.NavigateToAddEmailScreen(
                        startingObject = startingObject,
                        space = space
                    )
                )
            }
        } else {
            viewModelScope.launch {
                val config = configStorage.getOrNull()
                if (config != null) {
                    analytics.sendOpenAccountEvent(
                        analytics = config.analytics
                    )
                } else {
                    Timber.w("config was missing before the end of onboarding")
                }
                if (!startingObject.isNullOrEmpty()) {
                    commands.emit(
                        Command.OpenStartingObject(
                            space = SpaceId(space),
                            startingObject = startingObject
                        )
                    )
                } else {
                    commands.emit(Command.OpenVault)
                }
            }
        }
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

    sealed interface State {

        val mnemonicPhrase: String

        class Idle(override val mnemonicPhrase: String): State
        class Mnemonic(override val mnemonicPhrase: String): State
        class MnemonicOpened(override val mnemonicPhrase: String): State
    }

    class Factory @Inject constructor(
        private val getMnemonic: GetMnemonic,
        private val analytics: Analytics,
        private val configStorage: ConfigStorage,
        private val networkModeProvider: NetworkModeProvider,
        private val networkConnectionStatus: NetworkConnectionStatus
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicViewModel(
                getMnemonic = getMnemonic,
                analytics = analytics,
                configStorage = configStorage,
                networkModeProvider = networkModeProvider,
                networkConnectionStatus = networkConnectionStatus
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