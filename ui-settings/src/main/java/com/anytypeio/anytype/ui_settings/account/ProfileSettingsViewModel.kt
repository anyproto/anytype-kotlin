package com.anytypeio.anytype.ui_settings.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.icon.SetImageIcon
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.search.PROFILE_SUBSCRIPTION_ID
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendScreenSettingsDeleteEvent
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileSettingsViewModel(
    private val analytics: Analytics,
    private val deleteAccount: DeleteAccount,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val setObjectDetails: SetObjectDetails,
    private val configStorage: ConfigStorage,
    private val urlBuilder: UrlBuilder,
    private val setImageIcon: SetDocumentImageIcon,
    private val membershipProvider: MembershipProvider
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()

    val isLoggingOut = MutableStateFlow(false)
    val debugSyncReportUri = MutableStateFlow<Uri?>(null)
    val membershipStatusState = MutableStateFlow<MembershipStatus?>(null)

    private val profileId = configStorage.get().profile

    val profileData = storelessSubscriptionContainer.subscribe(
        StoreSearchByIdsParams(
            subscription = PROFILE_SUBSCRIPTION_ID,
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.ICON_IMAGE,
                Relations.ICON_EMOJI,
                Relations.ICON_OPTION
            ),
            targets = listOf(profileId)
        )
    ).map { result ->
        val obj = result.firstOrNull()
        AccountProfile.Data(
            name = obj?.name.orEmpty(),
            icon = obj?.profileIcon(urlBuilder) ?: ProfileIconView.Placeholder(null)
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
            membershipProvider.status().collect { status ->
                membershipStatusState.value = status
            }
        }
    }

    fun onNameChange(name: String) {
        Timber.d("onNameChange, name:[$name]")
        viewModelScope.launch {
            setObjectDetails.execute(
                SetObjectDetails.Params(
                    ctx = profileId,
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
        }
    }

    fun onDeleteAccountClicked() {
        Timber.d("onDeleteAccountClicked, ")
        jobs += viewModelScope.launch {
            deleteAccount(BaseUseCase.None).process(
                success = {
                    sendEvent(
                        analytics = analytics,
                        eventName = EventsDictionary.deleteAccount
                    )
                    Timber.d("Successfully deleted account, status")
                },
                failure = {
                    Timber.e(it, "Error while deleting account").also {
                        sendToast("Error while deleting account")
                    }
                }
            )
        }
    }

    fun onStop() {
        Timber.d("onStop, ")
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                listOf(PROFILE_SUBSCRIPTION_ID)
            )
        }
    }

    fun onPickedImageFromDevice(path: String, space: Id) {
        viewModelScope.launch {
            setImageIcon(
                SetImageIcon.Params(
                    target = profileId,
                    path = path,
                    spaceId = SpaceId(space)
                )
            ).process(
                failure = {
                    Timber.e("Error while setting image icon")
                },
                success = {
                    // do nothing
                }
            )
        }
    }

    fun proceedWithAccountDeletion() {
        viewModelScope.launch {
            analytics.sendScreenSettingsDeleteEvent()
        }
    }

    sealed class AccountProfile {
        object Idle: AccountProfile()

        class Data(
            val name: String,
            val icon: ProfileIconView
        ): AccountProfile()
    }

    class Factory(
        private val deleteAccount: DeleteAccount,
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val setObjectDetails: SetObjectDetails,
        private val configStorage: ConfigStorage,
        private val urlBuilder: UrlBuilder,
        private val setDocumentImageIcon: SetDocumentImageIcon,
        private val membershipProvider: MembershipProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileSettingsViewModel(
                deleteAccount = deleteAccount,
                analytics = analytics,
                storelessSubscriptionContainer = storelessSubscriptionContainer,
                setObjectDetails = setObjectDetails,
                configStorage = configStorage,
                urlBuilder = urlBuilder,
                setImageIcon = setDocumentImageIcon,
                membershipProvider = membershipProvider
            ) as T
        }
    }
}

private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L