package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import com.anytypeio.anytype.presentation.extension.sendOpenAccountEvent
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingSoulCreationViewModel @Inject constructor(
    private val setObjectDetails: SetObjectDetails,
    private val setSpaceDetails: SetSpaceDetails,
    private val configStorage: ConfigStorage,
    private val analytics: Analytics,
    private val createAccount: CreateAccount,
    private val setupWallet: SetupWallet,
    private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip,
    private val pathProvider: PathProvider,
    private val spaceGradientProvider: SpaceGradientProvider,
) : ViewModel() {

    val toasts = MutableSharedFlow<String>()

    private val _navigationFlow = MutableSharedFlow<Navigation>()
    val navigationFlow: SharedFlow<Navigation> = _navigationFlow

    fun onGoToTheAppClicked(name: String) {
        proceedWithSettingAccountName(name)
    }

    private fun proceedWithSettingAccountName(name: String) {
        val config = configStorage.getOrNull()
        if (config != null) {
            viewModelScope.launch {
                sendAnalyticsOnboardingScreenEvent(analytics,
                    EventsDictionary.ScreenOnboardingStep.SOUL_CREATING
                )
                setObjectDetails.async(
                    SetObjectDetails.Params(
                        ctx = config.profile, details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while updating object details")
                    },
                    onSuccess = {
                        proceedWithSettingWorkspaceName(name)
                    }
                )
            }
        } else {
            Timber.e(CONFIG_NOT_FOUND_ERROR).also {
                toast(CONFIG_NOT_FOUND_ERROR)
            }
        }
    }

    private fun proceedWithSettingWorkspaceName(name: String) {
        val config = configStorage.getOrNull()
        if (config != null) {
            viewModelScope.launch {
                sendAnalyticsOnboardingScreenEvent(
                    analytics = analytics,
                    step = EventsDictionary.ScreenOnboardingStep.SPACE_CREATING
                )
                setSpaceDetails.async(
                    SetSpaceDetails.Params(
                        space = SpaceId(config.space),
                        details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while updating object details")
                    },
                    onSuccess = {
                        analytics.sendOpenAccountEvent(analytics = config.analytics)
                        _navigationFlow.emit(Navigation.OpenSoulCreationAnim(name))
                    }
                )
            }
        } else {
            Timber.e(CONFIG_NOT_FOUND_ERROR).also {
                toast(CONFIG_NOT_FOUND_ERROR)
            }
        }
    }

    private fun toast(msg: String) {
        viewModelScope.launch { toasts.emit(msg) }
    }

    sealed interface Navigation {
        class OpenSoulCreationAnim(val name: String): Navigation
    }

    class Factory @Inject constructor(
        private val setObjectDetails: SetObjectDetails,
        private val setSpaceDetails: SetSpaceDetails,
        private val configStorage: ConfigStorage,
        private val analytics: Analytics,
        private val pathProvider: PathProvider,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val createAccount: CreateAccount,
        private val setupWallet: SetupWallet,
        private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingSoulCreationViewModel(
                setObjectDetails = setObjectDetails,
                setSpaceDetails = setSpaceDetails,
                configStorage = configStorage,
                analytics = analytics,
                createAccount = createAccount,
                setupWallet = setupWallet,
                setupMobileUseCaseSkip = setupMobileUseCaseSkip,
                pathProvider = pathProvider,
                spaceGradientProvider = spaceGradientProvider,
            ) as T
        }
    }

    companion object {
        const val CONFIG_NOT_FOUND_ERROR = "Something went wrong: config not found"
    }
}