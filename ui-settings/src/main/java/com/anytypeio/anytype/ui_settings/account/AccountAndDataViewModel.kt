package com.anytypeio.anytype.ui_settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.device.ClearFileCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountAndDataViewModel(
    private val clearFileCache: ClearFileCache,
    private val analytics: Analytics,
    private val deleteAccount: DeleteAccount
) : ViewModel() {

    val isClearFileCacheInProgress = MutableStateFlow(false)
    val isLoggingOut = MutableStateFlow(false)

    fun onClearFileCacheAccepted() {
        viewModelScope.launch {
            clearFileCache(BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        isClearFileCacheInProgress.value = true
                    }
                    is Interactor.Status.Error -> {
                        isClearFileCacheInProgress.value = false
                        val msg = "Error while clearing file cache: ${status.throwable.message}"
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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.fileOffloadScreenShow
        )
    }

    fun onDeleteAccountClicked() {
        viewModelScope.launch {
            deleteAccount(BaseUseCase.None).process(
                success = {
                    Timber.d("Successfully deleted account, status")
                },
                failure = {
                    Timber.e(it, "Error while deleting account")
                }
            )
        }
    }

    class Factory(
        private val clearFileCache: ClearFileCache,
        private val deleteAccount: DeleteAccount,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountAndDataViewModel(
                clearFileCache = clearFileCache,
                deleteAccount = deleteAccount,
                analytics = analytics
            ) as T
        }
    }
}