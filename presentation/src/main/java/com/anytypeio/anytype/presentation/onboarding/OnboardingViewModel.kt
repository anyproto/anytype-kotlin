package com.anytypeio.anytype.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val analytics: Analytics
) : BaseViewModel() {

    private val isInProgress = MutableStateFlow(false)

    val navigation = MutableSharedFlow<Navigation>()

    fun onBackPressed() {
        if (!isInProgress.value) {
            viewModelScope.launch {
                navigation.emit(Navigation.Back)
            }
        } else {
            sendToast(LOADING_MSG)
        }
    }

    fun onLoadingStateChanged(isLoading: Boolean) {
        isInProgress.value = isLoading
    }

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

    sealed class Navigation {
        object Back: Navigation()
    }

    companion object {
        const val LOADING_MSG = "Loading... please wait."
    }
}