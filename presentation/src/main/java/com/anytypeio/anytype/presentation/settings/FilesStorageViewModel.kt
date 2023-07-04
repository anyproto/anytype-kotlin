package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.FileLimits
import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.bytesToHumanReadableSize
import com.anytypeio.anytype.core_utils.ext.bytesToHumanReadableSizeFloatingPoint
import com.anytypeio.anytype.core_utils.ext.bytesToHumanReadableSizeLocal
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.device.BuildProvider
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.PROFILE_SUBSCRIPTION_ID
import com.anytypeio.anytype.domain.workspace.FileSpaceUsage
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.presentation.extension.sendSettingsOffloadEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsStorageEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsStorageManageEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsStorageOffloadEvent
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class FilesStorageViewModel(
    private val analytics: Analytics,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val configStorage: ConfigStorage,
    private val clearFileCache: ClearFileCache,
    private val urlBuilder: UrlBuilder,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileSpaceUsage: FileSpaceUsage,
    private val interceptFileLimitEvents: InterceptFileLimitEvents,
    private val buildProvider: BuildProvider,
    private val getAccount: GetAccount
) : ViewModel() {

    val events = MutableSharedFlow<Event>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val isClearFileCacheInProgress = MutableStateFlow(false)

    private val _fileLimitsState = MutableStateFlow(FileLimits.empty())

    private val _state = MutableStateFlow(ScreenState.empty())
    val state: StateFlow<ScreenState> = _state
    val toasts = MutableSharedFlow<String>(replay = 0)

    private val jobs = mutableListOf<Job>()

    init {
        subscribeToViewEvents()
    }

    fun onStart() {
        subscribeToFileLimits()
        subscribeToSpace()
        subscribeToFileLimitEvents()
    }

    fun onStop() {
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                listOf(SPACE_STORAGE_SUBSCRIPTION_ID, PROFILE_SUBSCRIPTION_ID)
            )
        }
        jobs.cancel()
    }

    private fun subscribeToFileLimits() {
        jobs += viewModelScope.launch {
            fileSpaceUsage
                .stream(Unit)
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _fileLimitsState.value = it
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
                        toasts.emit("Error while clearing the file cache")
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
            Event.OnManageFilesClicked -> {
                commands.emit(Command.OpenRemoteStorageScreen(Subscription.Files.id))
                analytics.sendSettingsStorageManageEvent()
            }
            Event.OnOffloadFilesClicked -> {
                commands.emit(Command.OpenOffloadFilesScreen)
                analytics.sendSettingsOffloadEvent()
            }
            Event.OnGetMoreSpaceClicked -> {
                onGetMoreSpaceClicked()
            }
        }
    }

    private fun subscribeToSpace() {
        val workspaceId = configStorage.get().workspace
        val profileId = configStorage.get().profile
        jobs += viewModelScope.launch {
            val subscribeParams = StoreSearchByIdsParams(
                subscription = SPACE_STORAGE_SUBSCRIPTION_ID,
                targets = listOf(workspaceId, profileId),
                keys = listOf(
                    Relations.ID,
                    Relations.NAME,
                    Relations.ICON_EMOJI,
                    Relations.ICON_IMAGE,
                    Relations.ICON_OPTION
                )
            )
            combine(
                _fileLimitsState,
                storelessSubscriptionContainer.subscribe(subscribeParams)
            ) { spaceUsage, result ->
                val workspace = result.find { it.id == workspaceId }
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
                ScreenState(
                    spaceName = workspace?.name.orEmpty(),
                    spaceIcon = workspace?.spaceIcon(urlBuilder, spaceGradientProvider),
                    spaceUsage = bytesUsage?.let { bytesToHumanReadableSizeFloatingPoint(it) }.orEmpty(),
                    percentUsage = percentUsage,
                    device = getDeviceName(),
                    localUsage = spaceUsage.localBytesUsage?.let { bytesToHumanReadableSizeLocal(it) }
                        .orEmpty(),
                    spaceLimit = bytesLimit?.let { bytesToHumanReadableSize(it) }.orEmpty(),
                    isShowGetMoreSpace = isShowGetMoreSpace,
                    isShowSpaceUsedWarning = isShowSpaceUsedWarning
                )
            }
                .flowOn(appCoroutineDispatchers.io)
                .collect { _state.value = it }
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

    private fun onGetMoreSpaceClicked() {
        viewModelScope.launch {
            val params = StoreSearchByIdsParams(
                subscription = PROFILE_SUBSCRIPTION_ID,
                keys = listOf(Relations.ID, Relations.NAME),
                targets = listOf(configStorage.get().profile)
            )
            combine(
                getAccount.asFlow(Unit),
                storelessSubscriptionContainer.subscribe(params)
            ) { account: Account, profileObj: List<ObjectWrapper.Basic> ->
                Command.SendGetMoreSpaceEmail(
                    account = account.id,
                    name = profileObj.firstOrNull()?.name.orEmpty(),
                    limit = _state.value.spaceLimit
                )
            }
                .catch { Timber.e(it, "onGetMoreSpaceClicked error") }
                .flowOn(appCoroutineDispatchers.io)
                .collect { commands.emit(it) }
        }
    }

    sealed class Event {
        object OnManageFilesClicked : Event()
        object OnOffloadFilesClicked : Event()
        object OnGetMoreSpaceClicked : Event()
    }

    sealed class Command {
        object OpenOffloadFilesScreen : Command()
        data class OpenRemoteStorageScreen(val subscription: Id) : Command()
        data class SendGetMoreSpaceEmail(val account: Id, val name: String, val limit: String) : Command()
    }

    class Factory @Inject constructor(
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val configStorage: ConfigStorage,
        private val clearFileCache: ClearFileCache,
        private val urlBuilder: UrlBuilder,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val fileSpaceUsage: FileSpaceUsage,
        private val interceptFileLimitEvents: InterceptFileLimitEvents,
        private val buildProvider: BuildProvider,
        private val getAccount: GetAccount
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = FilesStorageViewModel(
            analytics = analytics,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            configStorage = configStorage,
            clearFileCache = clearFileCache,
            urlBuilder = urlBuilder,
            spaceGradientProvider = spaceGradientProvider,
            appCoroutineDispatchers = appCoroutineDispatchers,
            fileSpaceUsage = fileSpaceUsage,
            interceptFileLimitEvents = interceptFileLimitEvents,
            buildProvider = buildProvider,
            getAccount = getAccount
        ) as T
    }

    data class ScreenState(
        val spaceIcon: SpaceIconView?,
        val spaceName: String,
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
                spaceIcon = null,
                spaceName = "",
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