package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingClickEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import com.anytypeio.anytype.presentation.extension.sendOpenAccountEvent
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingMnemonicViewModel @Inject constructor(
    private val getMnemonic: GetMnemonic,
    private val analytics: Analytics,
    private val configStorage: ConfigStorage
) : ViewModel() {

    val state = MutableStateFlow<State>(State.Idle(""))

    init {
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

    fun onCheckLaterClicked() {
        viewModelScope.sendAnalyticsOnboardingClickEvent(
            analytics = analytics,
            type = EventsDictionary.ClickOnboardingButton.CHECK_LATER,
            step = EventsDictionary.ScreenOnboardingStep.PHRASE
        )
        viewModelScope.launch {
            val config = configStorage.getOrNull()
            if (config != null) {
                analytics.sendOpenAccountEvent(
                    analytics = config.analytics
                )
            }
        }
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
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicViewModel(
                getMnemonic = getMnemonic,
                analytics = analytics,
                configStorage = configStorage
            ) as T
        }
    }

}