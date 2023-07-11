package com.anytypeio.anytype.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class OnboardingViewModel(
    private val analytics: Analytics
) : BaseViewModel() {

    class Factory @Inject constructor(
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(
                analytics = analytics
            ) as T
        }
    }
}