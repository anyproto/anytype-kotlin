package com.anytypeio.anytype.ui_settings.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.icon.RemoveObjectIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.icon.SetImageIcon
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.ui_settings.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileSettingsViewModel(
    private val analytics: Analytics,
    private val container: StorelessSubscriptionContainer,
    private val setObjectDetails: SetObjectDetails,
    private val configStorage: ConfigStorage,
    private val urlBuilder: UrlBuilder,
    private val setImageIcon: SetDocumentImageIcon,
    private val membershipProvider: MembershipProvider,
    private val getNetworkMode: GetNetworkMode,
    private val profileContainer: ProfileSubscriptionManager,
    private val removeObjectIcon: RemoveObjectIcon,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val userPermissionProvider: UserPermissionProvider
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()

    private var headerTitleClickCount = 0

    val isLoggingOut = MutableStateFlow(false)
    val debugSyncReportUri = MutableStateFlow<Uri?>(null)
    val membershipStatusState = MutableStateFlow<MembershipStatus?>(null)
    val showMembershipState = MutableStateFlow<ShowMembership?>(null)

    val isDebugEnabled = MutableStateFlow(BuildConfig.DEBUG)

    val notificationsDisabled: StateFlow<Boolean> =
        notificationPermissionManager.permissionState().map { state ->
            when (state) {
                is NotificationPermissionManagerImpl.PermissionState.Granted -> false
                else -> true
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT), true)

    val profileData = profileContainer
        .observe()
        .combine(userPermissionProvider.getCurrent()) { profile, spaceMember ->
            Timber.d("profileData, profile:[$profile], spaceMember:[$spaceMember]")
            AccountProfile.Data(
                name = profile.name.orEmpty(),
                icon = profile.profileIcon(urlBuilder),
                identity = spaceMember?.identity,
                globalName = spaceMember?.globalName
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
            AccountProfile.Idle
        )

    init {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenSettingsAccount
            )
        }
        viewModelScope.launch {
            getNetworkMode.async(Unit).fold(
                onSuccess = { result ->
                    showMembershipState.value = when (result.networkMode) {
                        NetworkMode.DEFAULT -> ShowMembership(true)
                        NetworkMode.LOCAL -> ShowMembership(false)
                        NetworkMode.CUSTOM -> ShowMembership(true)
                    }
                },
                onFailure = { Timber.e(it, "Error while getting network mode") }
            )
        }
        viewModelScope.launch {
            membershipProvider.status().collect { status ->
                membershipStatusState.value = status
            }
        }
    }

    fun onNameChange(name: String) {
        Timber.d("onNameChange, name:[$name]")
        viewModelScope.launch {
            val profile = configStorage.getOrNull()?.profile
            if (profile != null) {
                setObjectDetails.execute(
                    SetObjectDetails.Params(
                        ctx = profile,
                        details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while updating object details")
                    },
                    onSuccess = {
                        // do nothing
                    }
                )
            } else {
                Timber.w("Config storage missing")
            }
        }
    }

    fun onStop() {
        Timber.d("onStop")
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    fun onPickedImageFromDevice(path: String) {
        viewModelScope.launch {
            val config = configStorage.getOrNull()
            if (config != null) {
                setImageIcon(
                    SetImageIcon.Params(
                        target = config.profile,
                        path = path,
                        spaceId = SpaceId(config.techSpace)
                    )
                ).process(
                    failure = {
                        Timber.e("Error while setting image icon")
                    },
                    success = {
                        // do nothing
                    }
                )
            } else {
                Timber.e("Missing config while trying to set profile image")
            }
        }
    }

    fun onClearProfileImage() {
        viewModelScope.launch {
            val config = configStorage.getOrNull()
            if (config != null) {
                val params = RemoveObjectIcon.Params(objectId = config.profile)
                removeObjectIcon.async(
                    params = params
                ).fold(
                    onFailure = {
                        Timber.e("Error while removing profile image")
                    },
                    onSuccess = {
                        // do nothing
                    }
                )
            } else {
                Timber.e("Missing config while trying to unset profile image")
            }
        }
    }

    fun onHeaderTitleClicked() {
        headerTitleClickCount = headerTitleClickCount + 1
        if (headerTitleClickCount >= ENABLE_DEBUG_MENU_CLICK_COUNT && isDebugEnabled.value == false) {
            isDebugEnabled.value = true
        }
    }

    fun refreshPermissionState() {
        notificationPermissionManager.refreshPermissionState()
    }

    class Factory(
        private val analytics: Analytics,
        private val container: StorelessSubscriptionContainer,
        private val setObjectDetails: SetObjectDetails,
        private val configStorage: ConfigStorage,
        private val urlBuilder: UrlBuilder,
        private val setDocumentImageIcon: SetDocumentImageIcon,
        private val membershipProvider: MembershipProvider,
        private val getNetworkMode: GetNetworkMode,
        private val profileSubscriptionManager: ProfileSubscriptionManager,
        private val removeObjectIcon: RemoveObjectIcon,
        private val notificationPermissionManager: NotificationPermissionManager,
        private val userPermissionProvider: UserPermissionProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileSettingsViewModel(
                analytics = analytics,
                container = container,
                setObjectDetails = setObjectDetails,
                configStorage = configStorage,
                urlBuilder = urlBuilder,
                setImageIcon = setDocumentImageIcon,
                membershipProvider = membershipProvider,
                getNetworkMode = getNetworkMode,
                profileContainer = profileSubscriptionManager,
                removeObjectIcon = removeObjectIcon,
                notificationPermissionManager = notificationPermissionManager,
                userPermissionProvider = userPermissionProvider
            ) as T
        }
    }

    companion object {
        const val ENABLE_DEBUG_MENU_CLICK_COUNT = 5
    }
}

private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L

data class ShowMembership(val isShowing: Boolean)