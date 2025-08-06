package com.anytypeio.anytype.presentation.onboarding

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
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel.Companion.CONFIG_NOT_FOUND_ERROR
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel.Companion.LOADING_AFTER_SUCCESS_DELAY
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel.Navigation
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel.ScreenState
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlin.onFailure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingStartViewModel @Inject constructor(
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
) : ViewModel() {

    val isLoadingState = MutableStateFlow<Boolean>(false)
    val errorState: MutableStateFlow<ErrorState> = MutableStateFlow(ErrorState.Hidden)
    val sideEffects = MutableSharedFlow<SideEffect>()
    val navigation: MutableSharedFlow<AuthNavigation> = MutableSharedFlow()

    init {
        Timber.i("OnboardingStartViewModel, init")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.authScreenShow
        )
    }

    fun onJoinClicked() {
        proceedWithCreatingWallet()
        //navigateTo(AuthNavigation.ProceedWithSignUp)
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

    fun onErrorDismissed() {
        errorState.value = ErrorState.Hidden
    }

    private fun proceedWithCreatingWallet() {
        viewModelScope.launch {
            isLoadingState.value = true
            val params = SetupWallet.Params(
                path = pathProvider.providePath()
            )
            Timber.d("Setting up wallet with params: $params")
            setupWallet.async(params).fold(
                onSuccess = { result ->
                    Timber.d("Wallet setup successful: $result")
                    proceedWithCreatingAccount()
                },
                onFailure = {
                    Timber.e(it, "Error while setting up wallet")
                    isLoadingState.value = false
                    errorState.value = ErrorState.WalletSetupError(
                        message = it.message ?: "Unknown error while setting up wallet"
                    )
                }
            )
        }
    }

    private fun proceedWithCreatingAccount() {
        val spaceName = stringProvider.getDefaultSpaceName()
        val startTime = System.currentTimeMillis()
        val params = CreateAccount.Params(
            name = "",
            iconGradientValue = spaceGradientProvider.randomId()
        )
        viewModelScope.launch {
            createAccount.async(params = params).fold(
                onFailure = { error ->
                    Timber.e(error, "Error while creating account")
                    when (error) {
                        CreateAccountException.NetworkError -> {
                            errorState.value = ErrorState.NetworkError(
                                message = error.message ?: "Unknown network error"
                            )
                        }
                        CreateAccountException.OfflineDevice -> {
                            errorState.value = ErrorState.OfflineDevice(
                                message = error.message ?: "Your device seems to be offline"
                            )
                        }
                        else -> {
                            errorState.value = ErrorState.Generic(
                                message = error.message ?: "Unknown error while creating account"
                            )
                        }
                    }
                },
                onSuccess = {
                    createAccountAnalytics(startTime)
                    val config = configStorage.getOrNull()
                    if (config != null) {
                        crashReporter.setUser(config.analytics)
                        spaceManager.set(config.space).onFailure {
                            Timber.e(
                                it,
                                "Error while setting current space during sign-up onboarding"
                            )
                        }
                        setupGlobalSubscriptions()
                        proceedWithSettingUpMobileUseCase(
                            space = config.space,
                            name = "",
                            spaceName = spaceName
                        )
                    } else {
                        Timber.w("Config was missing after account creation")
                    }
                }
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
//                        navigation.emit(
//                            Navigation.NavigateToMnemonic(
//                                space = SpaceId(config.space),
//                                startingObject = startingObjectId
//                            )
//                        )
                        // Workaround for leaving screen in loading state to wait screen transition
                        delay(LOADING_AFTER_SUCCESS_DELAY)
                        //state.value = StartScreenState.Success
                    },
                    onSuccess = {
//                        navigation.emit(
//                            Navigation.NavigateToMnemonic(
//                                space = SpaceId(config.space),
//                                startingObject = startingObjectId
//                            )
//                        )
//                        // Workaround for leaving screen in loading state to wait screen transition
//                        delay(LOADING_AFTER_SUCCESS_DELAY)
//                        state.value = StartScreenState.Success
                    }
                )
            }
        } else {
            Timber.e(CONFIG_NOT_FOUND_ERROR).also {
                //sendToast(CONFIG_NOT_FOUND_ERROR)
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

    private fun setupGlobalSubscriptions() {
        globalSubscriptionManager.onStart()
    }

    private fun navigateTo(destination: AuthNavigation) {
        viewModelScope.launch {
            navigation.emit(destination)
        }
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

    sealed class ErrorState {
        data object Hidden : ErrorState()
        data class WalletSetupError(val message: String) : ErrorState()
        data class Generic(val message: String) : ErrorState()
        data class NetworkError(val message: String) : ErrorState()
        data class OfflineDevice(val message: String) : ErrorState()
    }

    class Factory @Inject constructor(
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
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingStartViewModel(
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
}