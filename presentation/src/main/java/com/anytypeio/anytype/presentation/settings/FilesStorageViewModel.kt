package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.FileLimits
import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.core_utils.ext.bytesToHumanReadableSizeLocal
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.core_utils.ext.readableFileSize
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.presentation.device.BuildProvider
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpacesUsageInfo
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendScreenSettingsDeleteEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsOffloadEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsStorageEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsStorageOffloadEvent
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class FilesStorageViewModel(
    private val analytics: Analytics,
    private val clearFileCache: ClearFileCache,
    private val spacesUsageInfo: SpacesUsageInfo,
    private val interceptFileLimitEvents: InterceptFileLimitEvents,
    private val buildProvider: BuildProvider,
    private val deleteAccount: DeleteAccount
) : BaseViewModel() {

    val events = MutableSharedFlow<Event>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val isClearFileCacheInProgress = MutableStateFlow(false)

    private val _fileLimitsState = MutableStateFlow(FileLimits.empty())

    private val _state = MutableStateFlow(ScreenState.empty())
    val state: StateFlow<ScreenState> = _state

    private val jobs = mutableListOf<Job>()

    init {
        subscribeToViewEvents()
    }

    fun onStart() {
        getSpaceUsageInfo()
        subscribeToFileLimits()
        subscribeToFileLimitEvents()
    }

    fun onStop() {
        jobs.cancel()
    }

    private fun getSpaceUsageInfo() {
        jobs += viewModelScope.launch {
            spacesUsageInfo
                .stream(Unit)
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _fileLimitsState.value = FileLimits(
                                bytesUsage = it.nodeUsage.bytesUsage,
                                bytesLimit = it.nodeUsage.bytesLimit,
                                localBytesUsage = it.nodeUsage.localBytesUsage
                            )
                        },
                        onFailure = {
                            Timber.e(it, "Error while getting file space usage")
                        }
                    )
                }
        }
    }

    private fun subscribeToViewEvents() {
        events
            .throttleFirst()
            .onEach { event ->
                dispatchCommand(event)
            }
            .launchIn(viewModelScope)
        viewModelScope.launch { analytics.sendSettingsStorageEvent() }
    }

    private fun subscribeToFileLimitEvents() {
        jobs += viewModelScope.launch {
            interceptFileLimitEvents.run(Unit)
                .onEach { events ->
                    val currentState = _fileLimitsState.value
                    val newState = currentState.updateState(events)
                    _fileLimitsState.value = newState
                }
                .collect()
        }
    }

    private fun FileLimits.updateState(events: List<FileLimitsEvent>): FileLimits {
        var newState = this
        events.forEach { event ->
            newState = when (event) {
                is FileLimitsEvent.LocalUsage -> newState.copy(
                    localBytesUsage = event.bytesUsage
                )
                is FileLimitsEvent.SpaceUsage -> newState.copy(
                    bytesUsage = event.bytesUsage
                )
                is FileLimitsEvent.FileLimitUpdated -> newState.copy(
                    bytesLimit = event.bytesLimit
                )
                else -> newState
            }
        }
        return newState
    }

    fun onClearFileCacheAccepted() {
        Timber.d("onClearFileCacheAccepted")
        jobs += viewModelScope.launch {
            analytics.sendSettingsStorageOffloadEvent()
        }
        viewModelScope.launch {
            clearFileCache(BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        isClearFileCacheInProgress.value = true
                    }
                    is Interactor.Status.Error -> {
                        isClearFileCacheInProgress.value = false
                        Timber.e(status.throwable, "Error while clearing file cache")
                        sendToast("Error while clearing the file cache")
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

    fun event(event: Event) {
        Timber.d("Event : [$event]")
        viewModelScope.launch { events.emit(event) }
    }

    private suspend fun dispatchCommand(event: Event) {
        when (event) {
            Event.OnOffloadFilesClicked -> {
                commands.emit(Command.OpenOffloadFilesScreen)
                analytics.sendSettingsOffloadEvent()
            }
        }
    }

    private fun subscribeToFileLimits() {
        viewModelScope.launch {
            _fileLimitsState.collect { spaceUsage ->
                val bytesUsage = spaceUsage.bytesUsage
                val bytesLimit = spaceUsage.bytesLimit
                val localeUsage = spaceUsage.localBytesUsage
                val percentUsage =
                    if (bytesUsage != null && bytesLimit != null && bytesLimit != 0L) {
                        (bytesUsage.toFloat() / bytesLimit.toFloat())
                    } else {
                        null
                    }
                val isShowGetMoreSpace = isNeedToShowGetMoreSpace(
                    percentUsage = percentUsage,
                    localUsage = localeUsage,
                    bytesLimit = bytesLimit
                )
                val isShowSpaceUsedWarning = isShowSpaceUsedWarning(
                    percentUsage = percentUsage
                )
                val state = ScreenState(
                    spaceUsage = bytesUsage?.readableFileSize()
                        .orEmpty(),
                    percentUsage = percentUsage,
                    device = getDeviceName(),
                    localUsage = spaceUsage.localBytesUsage?.let { bytesToHumanReadableSizeLocal(it) }
                        .orEmpty(),
                    spaceLimit = bytesLimit?.readableFileSize().orEmpty(),
                    isShowGetMoreSpace = isShowGetMoreSpace,
                    isShowSpaceUsedWarning = isShowSpaceUsedWarning
                )
                _state.value = state
            }
        }
    }

    private fun getDeviceName(): String {
        val manufacturer = buildProvider.getManufacturer().capitalize()
        val model = buildProvider.getModel().capitalize()
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    private fun isNeedToShowGetMoreSpace(
        percentUsage: Float?,
        localUsage: Long?,
        bytesLimit: Long?
    ): Boolean {
        val localPercentUsage =
            if (localUsage != null && bytesLimit != null && bytesLimit != 0L) {
                (localUsage.toFloat() / bytesLimit.toFloat())
            } else {
                null
            }
        return (percentUsage != null && percentUsage >= WARNING_PERCENT)
                || (localPercentUsage != null && localPercentUsage >= WARNING_PERCENT)
    }

    private fun isShowSpaceUsedWarning(
        percentUsage: Float?
    ): Boolean {
        return percentUsage != null && percentUsage >= WARNING_PERCENT
    }

    fun proceedWithAccountDeletion() {
        viewModelScope.launch {
            analytics.sendScreenSettingsDeleteEvent()
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
                failure = { e ->
                    Timber.e(e, "Error while deleting account").also {
                        sendToast("Error while deleting account: ${e.msg()}")
                    }
                }
            )
        }
    }

    sealed class Event {
        data object OnOffloadFilesClicked : Event()
    }

    sealed class Command {
        data object OpenOffloadFilesScreen : Command()
    }

    class Factory @Inject constructor(
        private val analytics: Analytics,
        private val clearFileCache: ClearFileCache,
        private val spacesUsageInfo: SpacesUsageInfo,
        private val interceptFileLimitEvents: InterceptFileLimitEvents,
        private val buildProvider: BuildProvider,
        private val deleteAccount: DeleteAccount
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = FilesStorageViewModel(
            analytics = analytics,
            clearFileCache = clearFileCache,
            spacesUsageInfo = spacesUsageInfo,
            interceptFileLimitEvents = interceptFileLimitEvents,
            buildProvider = buildProvider,
            deleteAccount = deleteAccount
        ) as T
    }

    data class ScreenState(
        val spaceLimit: String,
        val spaceUsage: String,
        val percentUsage: Float?,
        val device: String?,
        val localUsage: String,
        val isShowGetMoreSpace: Boolean,
        val isShowSpaceUsedWarning: Boolean
    ) {
        companion object {
            fun empty() = ScreenState(
                spaceLimit = "",
                spaceUsage = "",
                percentUsage = null,
                device = null,
                localUsage = "",
                isShowGetMoreSpace = false,
                isShowSpaceUsedWarning = false
            )
        }
    }

    companion object {
        const val WARNING_PERCENT = 0.9f
    }
}