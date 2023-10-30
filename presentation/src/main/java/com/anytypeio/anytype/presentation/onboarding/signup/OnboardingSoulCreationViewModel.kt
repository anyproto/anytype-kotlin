package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.exceptions.CreateAccountException
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import com.anytypeio.anytype.presentation.extension.sendOpenAccountEvent
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val crashReporter: CrashReporter,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
) : BaseViewModel() {

    val state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val navigation = MutableSharedFlow<Navigation>()

    fun onNextClicked(name: String) {
        if (state.value !is ScreenState.Loading) {
            proceedWithCreatingWallet(name)
        } else {
            sendToast(LOADING_MSG)
        }
    }

    private fun proceedWithCreatingWallet(
        name: String
    ) {
        state.value = ScreenState.Loading
        setupWallet.invoke(
            scope = viewModelScope,
            params = SetupWallet.Params(
                path = pathProvider.providePath()
            )
        ) { result ->
            result.either(
                fnL = {
                    Timber.e(it, "Error while setting up wallet")
                },
                fnR = {
                    proceedWithCreatingAccount(name)
                }
            )
        }
    }

    private fun proceedWithCreatingAccount(name: String) {
        val startTime = System.currentTimeMillis()
        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = "",
                avatarPath = null,
                iconGradientValue = spaceGradientProvider.randomId()
            )
        ) { result ->
            result.either(
                fnL = { error ->
                    Timber.d("Error while creating account: ${error.message ?: "Unknown error"}").also {
                        when(error) {
                            CreateAccountException.NetworkError -> {
                                sendToast(
                                    "Failed to create your account due to a network error: ${error.message ?: "Unknown error"}"
                                )
                            }
                            CreateAccountException.OfflineDevice -> {
                                sendToast("Your device seems to be offline. Please, check your connection and try again.")
                            }
                            else -> {
                                sendToast("Error while creating an account: ${error.message ?: "Unknown error"}")
                            }
                        }
                    }
                },
                fnR = {
                    createAccountAnalytics(startTime)
                    val config = configStorage.getOrNull()
                    if (config != null) {
                        crashReporter.setUser(config.analytics)
                        relationsSubscriptionManager.onStart()
                        objectTypesSubscriptionManager.onStart()
                        proceedWithSettingUpMobileUseCase(config.space, name)
                    }
                }
            )
        }
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
                        Timber.e(it, "Error while setting profile name details")
                        navigation.emit(Navigation.NavigateToMnemonic)
                        sendAnalyticsOnboardingScreen()
                        // Workaround for leaving screen in loading state to wait screen transition
                        delay(OnboardingVoidViewModel.LOADING_AFTER_SUCCESS_DELAY)
                        state.value = ScreenState.Success
                    },
                    onSuccess = {
                        navigation.emit(Navigation.NavigateToMnemonic)
                        sendAnalyticsOnboardingScreen()
                        // Workaround for leaving screen in loading state to wait screen transition
                        delay(OnboardingVoidViewModel.LOADING_AFTER_SUCCESS_DELAY)
                        state.value = ScreenState.Success
                    }
                )
            }
        } else {
            Timber.e(CONFIG_NOT_FOUND_ERROR).also {
                sendToast(CONFIG_NOT_FOUND_ERROR)
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
//                        _navigationFlow.emit(Navigation.OpenSoulCreationAnim(name))
                    }
                )
            }
        } else {
            Timber.e(CONFIG_NOT_FOUND_ERROR).also {
                sendToast(CONFIG_NOT_FOUND_ERROR)
            }
        }
    }

    private fun createAccountAnalytics(startTime: Long) {
        viewModelScope.launch {
            analytics.proceedWithAccountEvent(
                startTime = startTime,
                configStorage = configStorage,
                eventName = EventsDictionary.createAccount
            )
        }
    }

    private fun proceedWithSettingUpMobileUseCase(space: Id, name: String) {
        viewModelScope.launch {
            setupMobileUseCaseSkip.async(SetupMobileUseCaseSkip.Params(space)).fold(
                onFailure = {
                    proceedWithSettingAccountName(name)
                },
                onSuccess = {
                    proceedWithSettingAccountName(name)
                }
            )
        }
    }

    private fun sendAnalyticsOnboardingScreen() {
        viewModelScope.sendAnalyticsOnboardingScreenEvent(analytics,
            EventsDictionary.ScreenOnboardingStep.PHRASE
        )
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
        private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip,
        private val relationsSubscriptionManager: RelationsSubscriptionManager,
        private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        private val crashReporter: CrashReporter
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
                relationsSubscriptionManager = relationsSubscriptionManager,
                objectTypesSubscriptionManager = objectTypesSubscriptionManager,
                crashReporter = crashReporter
            ) as T
        }
    }

    companion object {
        const val CONFIG_NOT_FOUND_ERROR = "Something went wrong: config not found"
        const val LOADING_MSG = "Loading, please wait."
        const val EXITING_MSG = "Clearing resources, please wait."
        const val LOADING_AFTER_SUCCESS_DELAY = 600L
    }

    sealed class Navigation {
        object NavigateToMnemonic: Navigation()
        object GoBack: Navigation()
    }

    sealed class ScreenState {
        object Idle: ScreenState()
        object Loading: ScreenState()
        object Success: ScreenState()
        sealed class Exiting : ScreenState() {
            object Status : Exiting()
            object Logout: Exiting()
        }
    }
}