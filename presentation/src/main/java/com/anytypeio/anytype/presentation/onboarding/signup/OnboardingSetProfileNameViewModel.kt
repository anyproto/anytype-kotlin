package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingSetProfileNameViewModel @Inject constructor(
    private val setObjectDetails: SetObjectDetails,
    private val configStorage: ConfigStorage,
    private val analytics: Analytics,
    private val setMembershipEmail: SetMembershipEmail,
    private val pendingIntentStore: PendingIntentStore,
    private val profileContainer: ProfileSubscriptionManager
) : BaseViewModel() {

    val profileView = profileContainer.observe().map { obj ->
        AccountProfile.Data(
            name = obj.name.orEmpty(),
            icon = ProfileIconView.Placeholder(name = null)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountProfile.Idle)

    val state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val navigation = MutableSharedFlow<Navigation>()

    init {
        Timber.i("OnboardingSetProfileNameViewModel, init")
    }

    //region Name Screen
    fun onNextClicked(
        name: String,
        spaceId: String,
        startingObjectId: String?,
        profileId: String
    ) {
        state.value = ScreenState.Loading
        proceedWithSettingAccountName(
            name = name,
            spaceId = spaceId,
            startingObjectId = startingObjectId,
            profileId = profileId
        )
    }

    private fun proceedWithSettingAccountName(
        name: String,
        spaceId: Id,
        startingObjectId: Id?,
        profileId: Id
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                navigation.emit(
                    Navigation.NavigateToSetEmail(
                        spaceId = spaceId,
                        startingObjectId = startingObjectId,
                        profileId = profileId
                    )
                )
                // Workaround for leaving screen in loading state to wait screen transition
                delay(LOADING_AFTER_SUCCESS_DELAY)
                state.value = ScreenState.Success
            } else {
                setObjectDetails.async(
                    SetObjectDetails.Params(
                        ctx = profileId, details = mapOf(Relations.NAME to name.trim())
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while setting profile name details")
                        navigation.emit(
                            Navigation.NavigateToSetEmail(
                                spaceId = spaceId,
                                startingObjectId = startingObjectId,
                                profileId = profileId
                            )
                        )
                        // Workaround for leaving screen in loading state to wait screen transition
                        delay(LOADING_AFTER_SUCCESS_DELAY)
                        state.value = ScreenState.Success
                    },
                    onSuccess = {
                        navigation.emit(
                            Navigation.NavigateToSetEmail(
                                spaceId = spaceId,
                                startingObjectId = startingObjectId,
                                profileId = profileId
                            )
                        )
                        // Workaround for leaving screen in loading state to wait screen transition
                        delay(LOADING_AFTER_SUCCESS_DELAY)
                        state.value = ScreenState.Success
                    }
                )
            }
        }
    }

    class Factory @Inject constructor(
        private val setObjectDetails: SetObjectDetails,
        private val configStorage: ConfigStorage,
        private val analytics: Analytics,
        private val setMembershipEmail: SetMembershipEmail,
        private val pendingIntentStore: PendingIntentStore,
        private val profileContainer: ProfileSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingSetProfileNameViewModel(
                setObjectDetails = setObjectDetails,
                configStorage = configStorage,
                analytics = analytics,
                setMembershipEmail = setMembershipEmail,
                pendingIntentStore = pendingIntentStore,
                profileContainer = profileContainer
            ) as T
        }
    }

    companion object {
        const val LOADING_MSG = "Loading, please wait."
        const val LOADING_AFTER_SUCCESS_DELAY = 600L
    }

    sealed class Navigation {
        data class NavigateToSetEmail(
            val spaceId: String,
            val startingObjectId: String?,
            val profileId: String
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