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
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.`object`.ImportGetStartedUseCase
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import com.anytypeio.anytype.presentation.extension.sendOpenAccountEvent
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel.Navigation.OpenStartingObject
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
    private val globalSubscriptionManager: GlobalSubscriptionManager,
    private val spaceManager: SpaceManager,
    private val stringProvider: StringResourceProvider,
    private val setMembershipEmail: SetMembershipEmail,
    private val pendingIntentStore: PendingIntentStore
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
        name: String
    ) {
        if (state.value !is ScreenState.Loading) {
            viewModelScope.launch {
                proceedWithCreatingWallet(name)
            }
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
                    proceedWithCreatingAccount(
                        name = name
                    )
                }
            )
        }
    }

    private fun proceedWithCreatingAccount(
        name: String
    ) {
        val spaceName = stringProvider.getDefaultSpaceName()
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
                        spaceManager.set(config.space).onFailure {
                            Timber.e(it, "Error while setting current space during sign-up onboarding")
                        }
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

    //region Email screen
    fun sendAnalyticsOnboardingEmailScreen() {
        viewModelScope.launch {
            sendAnalyticsOnboardingScreenEvent(
                analytics = analytics,
                step = EventsDictionary.ScreenOnboardingStep.EMAIL
            )
        }
    }

    fun onEmailContinueClicked(
        email: String,
        space: Id,
        startingObject: String?
    ) {
        if (state.value is ScreenState.Loading) {
            sendToast(LOADING_MSG)
            return
        }
        state.value = ScreenState.Loading
        proceedWithSettingEmail(email = email)
        proceedWithNavigation(space, startingObject)
    }

    fun onEmailSkippedClicked(
        space: Id,
        startingObject: String?
    ) {
        if (state.value is ScreenState.Loading) {
            sendToast(LOADING_MSG)
            return
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenOnboardingSkipEmail
            )
        }
        state.value = ScreenState.Loading
        proceedWithNavigation(space, startingObject)
    }

    private fun proceedWithNavigation(space: Id, startingObject: String?) {
        viewModelScope.launch {
            sendOpenAccountAnalytics()
            navigateNextStep(
                space = space,
                startingObject = startingObject
            )
        }
    }

    private suspend fun navigateNextStep(space: Id, startingObject: Id?) {
        delay(LOADING_AFTER_SUCCESS_DELAY)
        val deeplink = pendingIntentStore.getDeepLinkInvite()
        when {
            !deeplink.isNullOrEmpty() -> navigation.emit(Navigation.OpenVault)
            !startingObject.isNullOrEmpty() -> navigation.emit(
                OpenStartingObject(
                    space = SpaceId(space),
                    startingObject = startingObject
                )
            )

            else -> navigation.emit(Navigation.OpenVault)
        }
    }

    private suspend fun sendOpenAccountAnalytics() {
        val config = configStorage.getOrNull()
        if (config != null) {
            analytics.sendOpenAccountEvent(
                analytics = config.analytics
            )
        } else {
            Timber.w("config was missing before the end of onboarding")
        }
    }

    private fun proceedWithSettingEmail(email: String) {
        val params = SetMembershipEmail.Params(
            email = email,
            subscribeToNewsletter = false,
            isFromOnboarding = true
        )
        viewModelScope.launch {
            setMembershipEmail.async(params).fold(
                onSuccess = { 
                    Timber.d("Email set successfully")
                    analytics.sendEvent(
                        eventName = EventsDictionary.screenOnboardingEnterEmail
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "Error setting email")
                    if (BuildConfig.DEBUG) {
                        sendToast("Error setting email: ${error.message}")
                    }
                }
            )
        }
    }
    //endregion

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
        private val globalSubscriptionManager: GlobalSubscriptionManager,
        private val spaceManager: SpaceManager,
        private val stringProvider: StringResourceProvider,
        private val setMembershipEmail: SetMembershipEmail,
        private val pendingIntentStore: PendingIntentStore
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
                globalSubscriptionManager = globalSubscriptionManager,
                spaceManager = spaceManager,
                stringProvider = stringProvider,
                setMembershipEmail = setMembershipEmail,
                pendingIntentStore = pendingIntentStore
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
        data class OpenStartingObject(
            val space: SpaceId,
            val startingObject: Id
        ) : Navigation()
        data object OpenVault : Navigation()
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