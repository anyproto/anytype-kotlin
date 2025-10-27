package com.anytypeio.anytype.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.analytics.props.UserProperty.Companion.ACCOUNT_ID_KEY
import com.anytypeio.anytype.analytics.props.UserProperty.Companion.NETWORK_ID_KEY
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
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingStartViewModel @Inject constructor(
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
    private val setSpaceDetails: SetSpaceDetails,
    private val stringResourceProvider: StringResourceProvider
) : ViewModel() {

    val isLoadingState = MutableStateFlow(false)
    val errorState: MutableStateFlow<ErrorState> = MutableStateFlow(ErrorState.Hidden)
    val sideEffects = MutableSharedFlow<SideEffect>()
    val navigation: MutableSharedFlow<AuthNavigation> = MutableSharedFlow()
    
    private var walletSetupJob: Job? = null

    init {
        Timber.i("OnboardingStartViewModel, init")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.authScreenShow
        )
    }

    fun onJoinClicked() {
        proceedWithCreatingWallet()
    }

    fun onLoginClicked() {
        viewModelScope.launch { navigateTo(AuthNavigation.ProceedWithSignIn) }
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
        if (walletSetupJob?.isActive == true) walletSetupJob?.cancel()
        walletSetupJob = viewModelScope.launch {
            isLoadingState.value = true
            val params = SetupWallet.Params(
                path = pathProvider.providePath()
            )
            Timber.d("Setting up wallet with params: $params")
            setupWallet.async(params).fold(
                onSuccess = { result ->
                    Timber.d("Wallet setup successful")
                    proceedWithCreatingAccount()
                },
                onFailure = {
                    Timber.e(it, "Error while setting up wallet")
                    isLoadingState.value = false
                    errorState.value = ErrorState.WalletSetupError
                }
            )
        }
    }

    private suspend fun proceedWithCreatingAccount() {
        Timber.d("Proceeding with creating account")
        val startTime = System.currentTimeMillis()
        
        // Send StartCreateAccount event before starting account creation
        analytics.sendEvent(eventName = EventsDictionary.startCreateAccount)
        
        val params = CreateAccount.Params(
            name = DEFAULT_ACCOUNT_NAME,
            iconGradientValue = spaceGradientProvider.randomId()
        )
        createAccount.async(params = params).fold(
            onFailure = { error -> handleCreateAccountError(error) },
            onSuccess = { result ->
                handleCreateAccountSuccess(startTime, result)
            }
        )
    }

    private fun handleCreateAccountError(error: Throwable) {
        Timber.e(error, "Error while creating account")
        isLoadingState.value = false
        errorState.value = when (error) {
            CreateAccountException.AccountCreatedButFailedToStartNode -> ErrorState.AccountCreatedButFailedToStartNode
            CreateAccountException.AccountCreatedButFailedToSetName -> ErrorState.AccountCreatedButFailedToSetName
            CreateAccountException.FailedToStopRunningNode -> ErrorState.FailedToStopRunningNode
            CreateAccountException.FailedToWriteConfig -> ErrorState.FailedToWriteConfig
            CreateAccountException.FailedToCreateLocalRepo -> ErrorState.FailedToCreateLocalRepo
            CreateAccountException.AccountCreationCanceled -> ErrorState.AccountCreationCanceled
            CreateAccountException.ConfigFileNotFound -> ErrorState.ConfigFileNotFound
            CreateAccountException.ConfigFileInvalid -> ErrorState.ConfigFileInvalid
            CreateAccountException.ConfigFileNetworkIdMismatch -> ErrorState.ConfigFileNetworkIdMismatch
            else -> ErrorState.Generic(
                message = error.message ?: "Unknown error while creating account"
            )
        }
    }

    private suspend fun handleCreateAccountSuccess(startTime: Long, result: CreateAccount.Result) {
        Timber.d("handleCreateAccountSuccess, Account created successfully: $result")
        val profileId = result.config.profile
        val spaceId = result.config.space
        createAccountAnalytics(startTime)
        crashReporter.setUser(result.config.analytics)
        spaceManager.set(spaceId).onFailure {
            Timber.e(it, "Error while setting current space during sign-up onboarding")
        }
        setupGlobalSubscriptions()
        proceedWithUpdatingSpaceName(spaceId = spaceId)
        proceedWithSettingUpMobileUseCase(space = spaceId, profileId = profileId)
    }

    private suspend fun proceedWithUpdatingSpaceName(spaceId: Id) {
        val defaultFirstSpaceName = stringResourceProvider.getInitialSpaceName()
        setSpaceDetails.async(
            SetSpaceDetails.Params(
                space = SpaceId(spaceId),
                details = mapOf(Relations.NAME to defaultFirstSpaceName)
            )
        ).fold(
            onFailure = {
                Timber.e(it, "Error while setting space details")
                isLoadingState.value = false
                errorState.value = ErrorState.Generic(
                    message = it.message ?: "Unknown error while setting space details"
                )
            }
        )
    }

    private suspend fun proceedWithSettingUpMobileUseCase(space: Id, profileId: Id) {
        importGetStartedUseCase.async(ImportGetStartedUseCase.Params(space = space)).fold(
            onFailure = {
                Timber.e(it, "Error while setting up mobile use case")
                navigateTo(
                    AuthNavigation.ProceedWithSignUp(
                        spaceId = space,
                        startingObjectId = null,
                        profileId = profileId
                    )
                )
            },
            onSuccess = { result ->
                Timber.d("Mobile use case setup successful: $result")
                navigateTo(
                    AuthNavigation.ProceedWithSignUp(
                        spaceId = space,
                        startingObjectId = result.startingObject,
                        profileId = profileId
                    )
                )
            }
        )
    }

    fun cancelLoadingState() {
        isLoadingState.value = false
    }

    private suspend fun createAccountAnalytics(startTime: Long) {
        val config = configStorage.getOrNull()
        if (config != null) {
            val analyticsId = config.analytics
            val userProperty = UserProperty.AccountId(analyticsId)
            analytics.updateUserProperty(userProperty)
            val networkId = config.network
            analytics.updateUserProperty(UserProperty.NetworkId(networkId))
            val lang = localeProvider.language()
            analytics.updateUserProperty(UserProperty.InterfaceLanguage(lang))
            
            // Send CreateAccount event with all required props
            analytics.sendEvent(
                startTime = startTime,
                middleTime = System.currentTimeMillis(),
                eventName = EventsDictionary.createAccount,
                props = Props(mapOf(ACCOUNT_ID_KEY to analyticsId, NETWORK_ID_KEY to networkId))
            )
            analytics.sendEvent(eventName = EventsDictionary.createSpace)
        }
    }

    private fun setupGlobalSubscriptions() {
        globalSubscriptionManager.onStart()
    }

    private suspend fun navigateTo(destination: AuthNavigation) {
        navigation.emit(destination)
    }
    
    override fun onCleared() {
        super.onCleared()
        walletSetupJob?.cancel()
    }

    interface AuthNavigation {
        data class ProceedWithSignUp(
            val spaceId: String,
            val startingObjectId: String?,
            val profileId: String
        ) : AuthNavigation

        object ProceedWithSignIn : AuthNavigation
    }

    sealed class SideEffect {
        object OpenPrivacyPolicy : SideEffect()
        object OpenTermsOfUse : SideEffect()
        object OpenNetworkSettings : SideEffect()
    }

    sealed class ErrorState {
        data object Hidden : ErrorState()
        data object WalletSetupError : ErrorState()
        data class Generic(val message: String) : ErrorState()
        data object NetworkError : ErrorState()
        data object OfflineDevice : ErrorState()
        data object AccountCreatedButFailedToStartNode : ErrorState()
        data object AccountCreatedButFailedToSetName : ErrorState()
        data object FailedToStopRunningNode : ErrorState()
        data object FailedToWriteConfig : ErrorState()
        data object FailedToCreateLocalRepo : ErrorState()
        data object AccountCreationCanceled : ErrorState()
        data object ConfigFileNotFound : ErrorState()
        data object ConfigFileInvalid : ErrorState()
        data object ConfigFileNetworkIdMismatch : ErrorState()
    }

    companion object {
        const val DEFAULT_ACCOUNT_NAME = ""
    }

    class Factory @Inject constructor(
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
        private val setSpaceDetails: SetSpaceDetails,
        private val stringResourceProvider: StringResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingStartViewModel(
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
                setSpaceDetails = setSpaceDetails,
                stringResourceProvider = stringResourceProvider
            ) as T
        }
    }
}