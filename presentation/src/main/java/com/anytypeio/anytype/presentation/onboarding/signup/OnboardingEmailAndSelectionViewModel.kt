package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingScreenEvent
import com.anytypeio.anytype.presentation.extension.sendOpenAccountEvent
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingEmailAndSelectionViewModel @Inject constructor(
    private val configStorage: ConfigStorage,
    private val analytics: Analytics,
    private val setMembershipEmail: SetMembershipEmail,
    private val pendingIntentStore: PendingIntentStore
) : BaseViewModel() {

    val state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val navigation = MutableSharedFlow<Navigation>()

    init {
        Timber.i("EmailAndSelectionViewModel, init")
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

    fun onEmailContinueButtonClicked(
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
        proceedWithNavigationToSelectionScreen(space = space, startingObject = startingObject)
        state.value = ScreenState.Idle
    }

    fun onEmailSkippedButtonClicked(
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
        state.value = ScreenState.Idle
        proceedWithNavigationToSelectionScreen(space = space, startingObject = startingObject)
    }

    private fun proceedWithNavigationToSelectionScreen(space: Id, startingObject: String?) {
        viewModelScope.launch {
            sendOpenAccountAnalytics()
            navigation.emit(
                Navigation.NavigateToSelection(
                    spaceId = space,
                    startingObjectId = startingObject
                )
            )
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

    //region Selection & UseCase screen
    fun sendAnalyticsOnboardingSelectionScreen() {
        viewModelScope.launch {
            sendAnalyticsOnboardingScreenEvent(
                analytics = analytics,
                step = EventsDictionary.ScreenOnboardingStep.PERSONA
            )
        }
    }

    fun onSelectionContinueClicked(professionItem: ProfessionItem, spaceId: String, startingObjectId: String?) {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickOnboarding,
                props = Props(
                    mapOf(
                        EventsPropertiesKey.step to EventsDictionary.ScreenOnboardingStep.PERSONA,
                        EventsPropertiesKey.type to professionItem.prettyName
                    )
                )
            )
        }
        viewModelScope.launch {
            navigation.emit(
                Navigation.NavigateToUsecase(
                    spaceId = spaceId,
                    startingObjectId = startingObjectId
                )
            )
        }
    }

    fun onSelectionSkipClicked(spaceId: String, startingObjectId: String?) {
        viewModelScope.launch {
            navigation.emit(
                Navigation.NavigateToUsecase(
                    spaceId = spaceId,
                    startingObjectId = startingObjectId
                )
            )
        }
    }

    fun sendAnalyticsOnboardingUseCaseScreen() {
        viewModelScope.launch {
            sendAnalyticsOnboardingScreenEvent(
                analytics = analytics,
                step = EventsDictionary.ScreenOnboardingStep.USECASE
            )
        }
    }

    fun onUsecaseContinueClicked(usecaseItem: UsecaseItem, space: Id, startingObject: String?) {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickOnboarding,
                props = Props(
                    mapOf(
                        EventsPropertiesKey.step to EventsDictionary.ScreenOnboardingStep.USECASE,
                        EventsPropertiesKey.type to usecaseItem.prettyName
                    )
                )
            )
        }
        viewModelScope.launch {
            navigateNextStep(space = space, startingObject = startingObject)
        }
    }

    fun onUsecaseSkipClicked(space: Id, startingObject: String?) {
        viewModelScope.launch {
            navigateNextStep(space = space, startingObject = startingObject)
        }
    }
    //endregion

    private suspend fun navigateNextStep(space: Id, startingObject: Id?) {
        delay(LOADING_AFTER_SUCCESS_DELAY)
        val deeplink = pendingIntentStore.getDeepLinkInvite()
        when {
            !deeplink.isNullOrEmpty() -> navigation.emit(Navigation.OpenVault)
            !startingObject.isNullOrEmpty() -> navigation.emit(
                Navigation.OpenStartingObject(
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
                analytics = config.analytics,
                networkId = config.network
            )
        } else {
            Timber.w("config was missing before the end of onboarding")
        }
    }

    class Factory @Inject constructor(
        private val configStorage: ConfigStorage,
        private val analytics: Analytics,
        private val setMembershipEmail: SetMembershipEmail,
        private val pendingIntentStore: PendingIntentStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingEmailAndSelectionViewModel(
                configStorage = configStorage,
                analytics = analytics,
                setMembershipEmail = setMembershipEmail,
                pendingIntentStore = pendingIntentStore
            ) as T
        }
    }

    companion object {
        const val LOADING_MSG = "Loading, please wait."
        const val LOADING_AFTER_SUCCESS_DELAY = 600L
    }

    sealed class Navigation {
        data object GoBack : Navigation()
        data class OpenStartingObject(
            val space: SpaceId,
            val startingObject: Id
        ) : Navigation()

        data object OpenVault : Navigation()
        data class NavigateToSelection(
            val spaceId: String,
            val startingObjectId: String?,
        )  : Navigation()

        data class NavigateToUsecase(
            val spaceId: String,
            val startingObjectId: String?,
        ) : Navigation()
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


data class OnboardingSelectionItem(
    val emoji: String,
    val titleResId: Int,
    val prettyName: String
)

typealias ProfessionItem = OnboardingSelectionItem
typealias UsecaseItem = OnboardingSelectionItem