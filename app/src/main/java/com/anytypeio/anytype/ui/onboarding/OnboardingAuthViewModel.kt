package com.anytypeio.anytype.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class OnboardingAuthViewModel @Inject constructor(): ViewModel() {

    fun onAction(action: OnboardingScreenContract) {

    }

    class Factory @Inject constructor(
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingAuthViewModel(
            ) as T
        }
    }

}