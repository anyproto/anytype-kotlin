package com.anytypeio.anytype.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.ScreenOnboardingStep.VOID
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class OnboardingStartViewModel @Inject constructor(
    private val analytics: Analytics
) : ViewModel() {

    init {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.authScreenShow
        )
    }

    val sideEffects = MutableSharedFlow<SideEffect>()
    val navigation: MutableSharedFlow<AuthNavigation> = MutableSharedFlow()

    fun onJoinClicked() {
        navigateTo(AuthNavigation.ProceedWithSignUp)
    }

    fun onLoginClicked() {
        navigateTo(AuthNavigation.ProceedWithSignIn)
    }

    fun onPrivacyPolicyClicked() {
        viewModelScope.launch { sideEffects.emit(SideEffect.OpenPrivacyPolicy) }
    }

    fun onTermsOfUseClicked() {
        viewModelScope.launch { sideEffects.emit(SideEffect.OpenTermsOfUse) }
    }

    fun onSettingsClicked() {
        viewModelScope.launch { sideEffects.emit(SideEffect.OpenNetworkSettings) }
    }

    private fun navigateTo(destination: AuthNavigation) {
        viewModelScope.launch {
            navigation.emit(destination)
        }
    }

    fun sendAnalyticsOnboardingScreen() {
        viewModelScope.sendAnalyticsOnboardingScreenEvent(analytics, VOID)
    }

    interface AuthNavigation {
        object ProceedWithSignUp : AuthNavigation
        object ProceedWithSignIn : AuthNavigation
    }

    sealed class SideEffect {
        object OpenPrivacyPolicy : SideEffect()
        object OpenTermsOfUse : SideEffect()
        object OpenNetworkSettings : SideEffect()
    }

    class Factory @Inject constructor(
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingStartViewModel(
                analytics = analytics
            ) as T
        }
    }
}