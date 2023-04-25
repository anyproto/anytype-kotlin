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
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.ClearFileCache
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

class AccountAndDataViewModel(
    private val clearFileCache: ClearFileCache,
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

    val isClearFileCacheInProgress = MutableStateFlow(false)
    val isDebugSyncReportInProgress = MutableStateFlow(false)
    val isLoggingOut = MutableStateFlow(false)
    val debugSyncReportUri = MutableStateFlow<Uri?>(null)

    private val profileId = configStorage.get().profile

    val accountData = storelessSubscriptionContainer.subscribe(
        StoreSearchByIdsParams(
            subscription = ACCOUNT_AND_DATA_SUBSCRIPTION_ID,
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

    fun onClearFileCacheAccepted() {
        Timber.d("onClearFileCacheAccepted, ")
        jobs += viewModelScope.launch {
            clearFileCache(BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        isClearFileCacheInProgress.value = true
                    }
                    is Interactor.Status.Error -> {
                        isClearFileCacheInProgress.value = false
                        Timber.e(status.throwable, "Error while clearing file cache")
                        // TODO send toast
                    }
                    Interactor.Status.Success -> {
                        viewModelScope.sendEvent(
                            analytics = analytics,
                            eventName = EventsDictionary.fileOffloadSuccess
                        )
                        isClearFileCacheInProgress.value = false
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
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

    fun onClearCacheButtonClicked() {
        jobs += viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.fileOffloadScreenShow
        )
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
                listOf(ACCOUNT_AND_DATA_SUBSCRIPTION_ID)
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
        private val clearFileCache: ClearFileCache,
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
            return AccountAndDataViewModel(
                clearFileCache = clearFileCache,
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
private const val ACCOUNT_AND_DATA_SUBSCRIPTION_ID = "account_and_data_subscription"