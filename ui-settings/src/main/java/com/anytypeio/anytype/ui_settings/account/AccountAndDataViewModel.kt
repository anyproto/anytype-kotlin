package com.anytypeio.anytype.ui_settings.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.ui_settings.account.repo.DebugSyncShareDownloader
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountAndDataViewModel(
    private val clearFileCache: ClearFileCache,
    private val analytics: Analytics,
    private val deleteAccount: DeleteAccount,
    private val debugSyncShareDownloader: DebugSyncShareDownloader,
) : ViewModel() {

    private val jobs = mutableListOf<Job>()

    val isClearFileCacheInProgress = MutableStateFlow(false)
    val isDebugSyncReportInProgress = MutableStateFlow(false)
    val isLoggingOut = MutableStateFlow(false)
    val debugSyncReportUri = MutableStateFlow<Uri?>(null)

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
    }

    class Factory(
        private val clearFileCache: ClearFileCache,
        private val deleteAccount: DeleteAccount,
        private val debugSyncShareDownloader: DebugSyncShareDownloader,
        private val analytics: Analytics,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountAndDataViewModel(
                clearFileCache = clearFileCache,
                deleteAccount = deleteAccount,
                debugSyncShareDownloader = debugSyncShareDownloader,
                analytics = analytics
            ) as T
        }
    }
}