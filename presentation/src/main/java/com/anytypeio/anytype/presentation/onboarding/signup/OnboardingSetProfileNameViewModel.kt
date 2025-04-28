package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.exceptions.CreateAccountException
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.`object`.ImportGetStartedUseCase
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingSetProfileNameViewModel @Inject constructor(
    private val setObjectDetails: SetObjectDetails,
    private val setSpaceDetails: SetSpaceDetails,
    private val configStorage: ConfigStorage,
    private val analytics: Analytics,
    private val createAccount: CreateAccount,
    private val setupWallet: SetupWallet,
    private val importGetStartedUseCase: ImportGetStartedUseCase,
    private val pathProvider: PathProvider,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val crashReporter: CrashReporter,
    private val localeProvider: LocaleProvider,
    private val globalSubscriptionManager: GlobalSubscriptionManager
) : BaseViewModel() {

    init {
        Timber.i("OnboardingSetProfileNameViewModel, init")
        viewModelScope.launch {
            sendAnalyticsOnboardingScreenEvent(analytics,
                EventsDictionary.ScreenOnboardingStep.SOUL
            )
        }
    }

    val state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val navigation = MutableSharedFlow<Navigation>()

    fun onNextClicked(
        name: String,
        spaceName: String
    ) {
        if (state.value !is ScreenState.Loading) {
            proceedWithCreatingWallet(
                name = name,
                spaceName = spaceName
            )
        } else {
            sendToast(LOADING_MSG)
        }
    }

    private fun proceedWithCreatingWallet(
        name: String,
        spaceName: String
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
                    proceedWithCreatingAccount(
                        name = name,
                        spaceName = spaceName
                    )
                }
            )
        }
    }

    private fun proceedWithCreatingAccount(
        name: String,
        spaceName: String
    ) {
        val startTime = System.currentTimeMillis()
        val params = CreateAccount.Params(
            name = name,
            avatarPath = null,
            iconGradientValue = spaceGradientProvider.randomId()
        )
        viewModelScope.launch {
            createAccount.async(params = params).fold(
                onFailure = { error ->
                    Timber.d("Error while creating account: ${error.message ?: "Unknown error"}").also {
                            when (error) {
                                CreateAccountException.NetworkError -> {
                                    sendToast(
                                        "Failed to create your account due to a network error: ${error.message ?: "Unknown error"}"
                                    )
                                }
                                CreateAccountException.OfflineDevice -> {
                                    sendToast("Your device seems to be offline. Please, check your connection and try again.")
                                }
                                else -> {
                                    sendToast("Error: ${error.message ?: "Unknown error"}")
                                }
                            }
                        }
                    state.value = ScreenState.Idle
                },
                onSuccess = {
                    createAccountAnalytics(startTime)
                    val config = configStorage.getOrNull()
                    if (config != null) {
                        crashReporter.setUser(config.analytics)
                        setupGlobalSubscriptions()
                        proceedWithSettingUpMobileUseCase(
                            space = config.space,
                            name = name,
                            spaceName = spaceName
                        )
                    } else {
                        Timber.w("Config was missing after account creation")
                    }
                }
            )
        }
    }

    private fun setupGlobalSubscriptions() {
        globalSubscriptionManager.onStart()
    }

    private fun proceedWithSettingAccountName(
        name: String,
        spaceName: String,
        startingObjectId: Id?
    ) {
        val config = configStorage.getOrNull()
        if (config != null) {
            viewModelScope.launch {
                analytics.sendEvent(eventName = EventsDictionary.createSpace)
                setSpaceDetails.async(
                    SetSpaceDetails.Params(
                        space = SpaceId(config.space),
                        details = mapOf(Relations.NAME to spaceName)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while setting space details")
                    }
                )
                setObjectDetails.async(
                    SetObjectDetails.Params(
                        ctx = config.profile, details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while setting profile name details")
                        navigation.emit(
                            Navigation.NavigateToMnemonic(
                                space = SpaceId(config.space),
                                startingObject = startingObjectId
                            )
                        )
                        // Workaround for leaving screen in loading state to wait screen transition
                        delay(LOADING_AFTER_SUCCESS_DELAY)
                        state.value = ScreenState.Success
                    },
                    onSuccess = {
                        navigation.emit(
                            Navigation.NavigateToMnemonic(
                                space = SpaceId(config.space),
                                startingObject = startingObjectId
                            )
                        )
                        // Workaround for leaving screen in loading state to wait screen transition
                        delay(LOADING_AFTER_SUCCESS_DELAY)
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

    private fun createAccountAnalytics(startTime: Long) {
        viewModelScope.launch {
            analytics.proceedWithAccountEvent(
                startTime = startTime,
                configStorage = configStorage,
                eventName = EventsDictionary.createAccount,
                lang = localeProvider.language()
            )
        }
    }

    private fun proceedWithSettingUpMobileUseCase(
        space: Id,
        name: String,
        spaceName: String
    ) {
        viewModelScope.launch {
            importGetStartedUseCase.async(ImportGetStartedUseCase.Params(space)).fold(
                onFailure = {
                    proceedWithSettingAccountName(
                        name = name,
                        spaceName = spaceName,
                        startingObjectId = null
                    )
                },
                onSuccess = { result ->
                    proceedWithSettingAccountName(
                        name = name,
                        spaceName = spaceName,
                        startingObjectId = result.startingObject
                    )
                }
            )
        }
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
        private val importGetStartedUseCase: ImportGetStartedUseCase,
        private val crashReporter: CrashReporter,
        private val localeProvider: LocaleProvider,
        private val globalSubscriptionManager: GlobalSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingSetProfileNameViewModel(
                setObjectDetails = setObjectDetails,
                setSpaceDetails = setSpaceDetails,
                configStorage = configStorage,
                analytics = analytics,
                createAccount = createAccount,
                setupWallet = setupWallet,
                importGetStartedUseCase = importGetStartedUseCase,
                pathProvider = pathProvider,
                spaceGradientProvider = spaceGradientProvider,
                crashReporter = crashReporter,
                localeProvider = localeProvider,
                globalSubscriptionManager = globalSubscriptionManager
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
        data class NavigateToMnemonic(val space: SpaceId, val startingObject: Id?): Navigation()
        data object GoBack: Navigation()
    }

    sealed class ScreenState {
        data object Idle: ScreenState()
        data object Loading: ScreenState()
        data object Success: ScreenState()
        sealed class Exiting : ScreenState() {
            data object Status : Exiting()
            data object Logout: Exiting()
        }
    }
}