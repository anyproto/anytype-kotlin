package com.anytypeio.anytype.presentation.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.bytesToHumanReadableSize
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.extension.sendSettingsOffloadEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsStorageEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsStorageManageEvent
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : ViewModel() {

    val events = MutableSharedFlow<Event>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val isClearFileCacheInProgress = MutableStateFlow(false)

    private val _state = MutableStateFlow<FilesStorageScreenState>(FilesStorageScreenState.Idle)
    val state: StateFlow<FilesStorageScreenState> = _state

    private val profileId = configStorage.get().profile
    private val workspaceId = configStorage.get().workspace

    init {
        events
            .throttleFirst()
            .onEach { event -> dispatchAnalyticsEvent(event) }
            .onEach { event -> dispatchCommand(event) }
            .launchIn(viewModelScope)
        subscribeToSpace()
        viewModelScope.launch { analytics.sendSettingsStorageEvent() }
    }

    fun onClearFileCacheAccepted() {
        Timber.d("onClearFileCacheAccepted")
        viewModelScope.launch {
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

    fun event(event: Event) {
        Timber.d("Event : [$event]")
        viewModelScope.launch { events.emit(event) }
    }

    private suspend fun dispatchCommand(event: Event) {
        when (event) {
            Event.OnManageFilesClicked -> {
                commands.emit(Command.OpenRemoteStorageScreen(subscription = Subscription.Files.id))
                analytics.sendSettingsStorageManageEvent()
            }
            Event.OnOffloadFilesClicked -> {
                commands.emit(Command.OpenOffloadFilesScreen)
                analytics.sendSettingsOffloadEvent()
            }
        }
    }

    private fun dispatchAnalyticsEvent(event: Event) {
        when (event) {
            Event.OnManageFilesClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.manageFilesScreenShow
                )
            }
            Event.OnOffloadFilesClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.fileOffloadScreenShow
                )
            }
        }
    }

    private fun subscribeToSpace() {
        viewModelScope.launch {
            storelessSubscriptionContainer.subscribe(
                StoreSearchByIdsParams(
                    subscription = SPACE_SUBSCRIPTION_ID,
                    targets = listOf(workspaceId, profileId),
                    keys = listOf(
                        Relations.ID,
                        Relations.NAME,
                        Relations.ICON_EMOJI,
                        Relations.ICON_IMAGE,
                        Relations.ICON_OPTION
                    )
                )
            ).map { result ->
                val workspace = result.find { it.id == workspaceId }
                val spaceUsage = (500 * 1024 * 1024).toDouble()
                val spaceLimit = (1024 * 1024 * 1024).toDouble()
                val localUsage = (104 * 1024 * 1024).toDouble()
                FilesStorageScreenState.SpaceData(
                    spaceName = workspace?.name,
                    spaceIcon = workspace?.spaceIcon(urlBuilder, spaceGradientProvider),
                    spaceUsage = bytesToHumanReadableSize(spaceUsage),
                    percentUsage = (spaceUsage / spaceLimit).toFloat(),
                    device = getDeviceName(),
                    localUsage = bytesToHumanReadableSize(localUsage),
                    spaceLimit = bytesToHumanReadableSize(spaceLimit)
                )
            }.flowOn(appCoroutineDispatchers.io).collect { _state.value = it }
        }
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.capitalize()
        val model = Build.MODEL.capitalize()
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    sealed class Event {
        object OnManageFilesClicked : Event()
        object OnOffloadFilesClicked : Event()
    }

    sealed class Command {
        object OpenOffloadFilesScreen : Command()
        data class OpenRemoteStorageScreen(val subscription: Id) : Command()
    }

    class Factory @Inject constructor(
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val configStorage: ConfigStorage,
        private val clearFileCache: ClearFileCache,
        private val urlBuilder: UrlBuilder,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val appCoroutineDispatchers: AppCoroutineDispatchers
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = FilesStorageViewModel(
            analytics = analytics,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            clearFileCache = clearFileCache,
            configStorage = configStorage,
            urlBuilder = urlBuilder,
            spaceGradientProvider = spaceGradientProvider,
            appCoroutineDispatchers = appCoroutineDispatchers
        ) as T
    }
}

sealed class FilesStorageScreenState {

    object Idle : FilesStorageScreenState()

    data class SpaceData(
        val spaceIcon: SpaceIconView?,
        val spaceName: String?,
        val spaceLimit: String,
        val spaceUsage: String,
        val percentUsage: Float,
        val device: String?,
        val localUsage: String
    ): FilesStorageScreenState()
}