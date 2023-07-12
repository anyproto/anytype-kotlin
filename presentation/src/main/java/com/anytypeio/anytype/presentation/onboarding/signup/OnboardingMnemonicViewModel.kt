package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingClickEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingMnemonicViewModel @Inject constructor(
    private val getMnemonic: GetMnemonic,
    private val analytics: Analytics
) : ViewModel() {

    val state = MutableStateFlow<State>(State.Idle(""))

    init {
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

    fun sendAnalyticsOnboardingScreen() {
        viewModelScope.sendAnalyticsOnboardingScreenEvent(analytics,
            EventsDictionary.ScreenOnboardingStep.SOUL
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
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicViewModel(
                getMnemonic = getMnemonic,
                analytics = analytics
            ) as T
        }
    }

}