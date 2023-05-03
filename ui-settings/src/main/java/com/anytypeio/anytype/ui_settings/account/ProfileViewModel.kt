package com.anytypeio.anytype.ui_settings.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Relations
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
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui_settings.account.repo.DebugSyncShareDownloader
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(
    private val analytics: Analytics,
    private val deleteAccount: DeleteAccount,
    private val debugSyncShareDownloader: DebugSyncShareDownloader,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val setObjectDetails: SetObjectDetails,
    private val configStorage: ConfigStorage,
    private val urlBuilder: UrlBuilder,
    private val setImageIcon: SetDocumentImageIcon,
    private val spaceGradientProvider: SpaceGradientProvider
) : ViewModel() {

    private val jobs = mutableListOf<Job>()

    val isDebugSyncReportInProgress = MutableStateFlow(false)
    val isLoggingOut = MutableStateFlow(false)
    val debugSyncReportUri = MutableStateFlow<Uri?>(null)

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
            name = obj?.name ?: "",
            icon = obj?.profileIcon(urlBuilder, spaceGradientProvider) ?: ProfileIconView.Placeholder
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
        AccountProfile.Idle
    )

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
                    Timber.e(it, "Error while deleting account")
                }
            )
        }
    }

    fun onDebugSyncReportClicked() {
        Timber.d("onDebugSyncReportClicked, ")
        jobs += viewModelScope.launch {
            debugSyncShareDownloader.stream(Unit).collect { result ->
                result.fold(
                    onSuccess = { report ->
                        isDebugSyncReportInProgress.value = false
                        debugSyncReportUri.value = report
                        Timber.d(report.toString())
                    },
                    onLoading = { isDebugSyncReportInProgress.value = true },
                    onFailure = { e ->
                        isDebugSyncReportInProgress.value = false
                        Timber.e(e, "Error while creating a debug sync report")
                    }
                )
            }
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

    fun onPickedImageFromDevice(path: String) {
        viewModelScope.launch {
            setImageIcon(
                SetImageIcon.Params(target = profileId, path = path)
            ).process(
                failure = {
                    Timber.e("Error while setting image icon")
                },
                success = { (payload, _) ->
                    // do nothing
                }
            )
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
        private val debugSyncShareDownloader: DebugSyncShareDownloader,
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val setObjectDetails: SetObjectDetails,
        private val configStorage: ConfigStorage,
        private val urlBuilder: UrlBuilder,
        private val setDocumentImageIcon: SetDocumentImageIcon,
        private val spaceGradientProvider: SpaceGradientProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(
                deleteAccount = deleteAccount,
                debugSyncShareDownloader = debugSyncShareDownloader,
                analytics = analytics,
                storelessSubscriptionContainer = storelessSubscriptionContainer,
                setObjectDetails = setObjectDetails,
                configStorage = configStorage,
                urlBuilder = urlBuilder,
                setImageIcon = setDocumentImageIcon,
                spaceGradientProvider = spaceGradientProvider
            ) as T
        }
    }
}

private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L
private const val PROFILE_SUBSCRIPTION_ID = "profile_subscription"