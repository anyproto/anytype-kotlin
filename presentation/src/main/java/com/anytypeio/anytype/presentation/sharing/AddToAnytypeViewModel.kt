package com.anytypeio.anytype.presentation.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.CLICK_ONBOARDING_TOOLTIP_ID_SHARING_EXTENSION
import com.anytypeio.anytype.analytics.base.EventsDictionary.CLICK_ONBOARDING_TOOLTIP_TYPE_CLOSE
import com.anytypeio.anytype.analytics.base.EventsDictionary.CLICK_ONBOARDING_TOOLTIP_TYPE_SHARING_MENU
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.ObjectOrigin
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Process.Event
import com.anytypeio.anytype.core_models.Process.State
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.download.ProcessCancel
import com.anytypeio.anytype.domain.media.FileDrop
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.Permissions
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.spaces.GetSpaceViews
import com.anytypeio.anytype.domain.workspace.EventProcessDropFilesChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber

class AddToAnytypeViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val createPrefilledNote: CreatePrefilledNote,
    private val spaceManager: SpaceManager,
    private val urlBuilder: UrlBuilder,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val analytics: Analytics,
    private val fileSharer: FileSharer,
    private val permissions: Permissions,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fileDrop: FileDrop,
    private val eventProcessChannel: EventProcessDropFilesChannel,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val selectedSpaceId = MutableStateFlow(NO_VALUE)

    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    val spaceViews = MutableStateFlow<List<SpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    val progressState = MutableStateFlow<ProgressState>(ProgressState.Init)

    val state = MutableStateFlow<ViewState>(ViewState.Init)

    private var progressJob: Job? = null

    init {
        viewModelScope.launch {
            analytics.registerEvent(
                EventAnalytics.Anytype(
                    name = EventsDictionary.CLICK_ONBOARDING_TOOLTIP,
                    props = Props(
                        mapOf(
                            EventsPropertiesKey.id to CLICK_ONBOARDING_TOOLTIP_ID_SHARING_EXTENSION,
                            EventsPropertiesKey.type to CLICK_ONBOARDING_TOOLTIP_TYPE_SHARING_MENU,
                        )
                    )
                )
            )
        }
        viewModelScope.launch {
            selectedSpaceId.value = spaceManager.get()
        }
        viewModelScope.launch {
            awaitAccountStartManager
                .awaitStart()
                .flatMapLatest {
                    combine(
                        spaceViewSubscriptionContainer.observe().map { items -> items.distinctBy { it.id } },
                        selectedSpaceId,
                        permissions.all()
                    ) { spaces, selected, currPermissions ->
                        val isSelectedSpaceAvailable = if (selected.isEmpty()) {
                            false
                        } else {
                            currPermissions[selected]?.isOwnerOrEditor() == true
                        }
                        spaces.filter { wrapper ->
                            val space = wrapper.targetSpaceId
                            if (space.isNullOrEmpty())
                                false
                            else {
                                currPermissions[space]?.isOwnerOrEditor() == true
                            }
                        }.mapIndexed { index, space ->
                            SpaceView(
                                obj = space,
                                icon = space.spaceIcon(urlBuilder),
                                isSelected = if (!isSelectedSpaceAvailable) {
                                    index == 0
                                } else {
                                    space.targetSpaceId == selected
                                }
                            )
                        }
                    }
                }.catch {
                    Timber.e(it, "Error while searching for spaces")
                }.collect { views ->
                    spaceViews.value = views
                }
        }
    }

    private fun subscribeToEventProcessChannel(
        wrapperObjId: Id,
        filePaths: List<String>,
        targetSpaceId: String
    ) {
        if (progressJob?.isActive == true) {
            Timber.d("Progress job is already active")
            progressJob?.cancel()
        }
        progressJob = viewModelScope.launch {
            eventProcessChannel.observe()
                .shareIn(
                    viewModelScope,
                    replay = 0,
                    started = SharingStarted.WhileSubscribed()
                )
                .onSubscription {
                    proceedWithFilesDrop(
                        wrapperObjId = wrapperObjId,
                        filePaths = filePaths,
                        targetSpaceId = targetSpaceId
                    )
                }
                .collect { events ->
                    events.forEach { event ->
                        when (event) {
                            is Event.DropFiles.New -> {
                                val currentProgressState = progressState.value
                                if (currentProgressState is ProgressState.Init
                                    && event.process.state == State.RUNNING
                                ) {
                                    progressState.value = ProgressState.Progress(
                                        processId = event.process.id,
                                        progress = 0f,
                                        wrapperObjId = wrapperObjId
                                    )
                                } else {
                                    //some process is already running
                                }
                            }

                            is Event.DropFiles.Update -> {
                                val currentProgressState = progressState.value
                                val newProcess = event.process
                                if (currentProgressState is ProgressState.Progress
                                    && currentProgressState.processId == event.process.id
                                    && newProcess.state == State.RUNNING
                                ) {
                                    val progress = newProcess.progress
                                    val total = progress?.total
                                    val done = progress?.done
                                    progressState.value =
                                        if (total != null && total != 0L && done != null) {
                                            currentProgressState.copy(progress = done.toFloat() / total)
                                        } else {
                                            currentProgressState.copy(progress = 0f)
                                        }
                                }
                            }

                            is Event.DropFiles.Done -> {
                                val currentProgressState = progressState.value
                                val newProcess = event.process
                                if (currentProgressState is ProgressState.Progress
                                    && event.process.state == State.DONE
                                    && newProcess.id == currentProgressState.processId
                                ) {
                                    progressState.value = currentProgressState.copy(progress = 1f)
                                    delay(300)
                                    progressState.value = ProgressState.Done(
                                        wrapperObjId = currentProgressState.wrapperObjId
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    fun onShareFiles(uris: List<String>, wrapperObjTitle: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetSpaceView = spaceViews.value.firstOrNull { view ->
                view.isSelected
            }
            val targetSpaceId = targetSpaceView?.obj?.targetSpaceId

            if (targetSpaceView != null && targetSpaceId != null) {
                val paths = uris.mapNotNull { uri ->
                    try {
                        fileSharer.getPath(uri)
                    } catch (e: Exception) {
                        Timber.e(e, "Error getting path for URI: $uri")
                        null
                    }
                }

                when (paths.size) {
                    0 -> sendToast("Could not get file paths")
                    else -> proceedWithCreatingWrapperObject(
                        filePaths = paths,
                        targetSpaceId = targetSpaceId,
                        wrapperObjTitle = wrapperObjTitle,
                    )
                }
            }
        }
    }

    private suspend fun proceedWithCreatingWrapperObject(
        filePaths: List<String>,
        targetSpaceId: String,
        wrapperObjTitle: String? = null
    ) {
        val startTime = System.currentTimeMillis()
        createPrefilledNote.async(
            CreatePrefilledNote.Params(
                text = wrapperObjTitle ?: EMPTY_STRING_VALUE,
                space = targetSpaceId,
                details = mapOf(
                    Relations.ORIGIN to ObjectOrigin.SHARING_EXTENSION.code.toDouble(),
                ),
            )
        ).fold(
            onSuccess = { wrapperObjId ->
                viewModelScope.sendAnalyticsObjectCreateEvent(
                    analytics = analytics,
                    objType = MarketplaceObjectTypeIds.PAGE,
                    route = EventsDictionary.Routes.sharingExtension,
                    startTime = startTime,
                    spaceParams = provideParams(spaceManager.get())
                )
                subscribeToEventProcessChannel(
                    wrapperObjId = wrapperObjId,
                    filePaths = filePaths,
                    targetSpaceId = targetSpaceId
                )
            },
            onFailure = {
                Timber.d(it, "Error while creating page")
                sendToast("Error while creating page: ${it.msg()}")
            }
        )
    }

    private suspend fun proceedWithFilesDrop(
        wrapperObjId: Id,
        filePaths: List<String>,
        targetSpaceId: String,
    ) {
        val params = FileDrop.Params(
            ctx = wrapperObjId,
            space = SpaceId(targetSpaceId),
            localFilePaths = filePaths
        )
        fileDrop.async(params).fold(
            onSuccess = { _ -> Timber.d("Files dropped successfully") },
            onFailure = { e ->
                Timber.e(e, "Error while dropping files").also {
                    sendToast(e.msg())
                }
            }
        )
    }

    fun proceedWithNavigation(wrapperObjId: Id) {
        val targetSpaceView = spaceViews.value.firstOrNull { view ->
            view.isSelected
        }
        val targetSpaceId = targetSpaceView?.obj?.targetSpaceId
        viewModelScope.launch {
            Timber.d("proceedWithNavigation: $wrapperObjId, $targetSpaceId")
            if (targetSpaceId == spaceManager.get()) {
                Timber.d("proceedWithNavigation: OpenEditor")
                delay(300)
                navigation.emit(
                    OpenObjectNavigation.OpenEditor(
                        target = wrapperObjId,
                        space = targetSpaceId
                    )
                )
            } else {
                Timber.d("proceedWithNavigation: ObjectAddToSpaceToast")
                delay(300)
                with(commands) {
                    emit(Command.ObjectAddToSpaceToast(targetSpaceView?.obj?.name))
                    emit(Command.Dismiss)
                }
            }
        }
    }

    fun onCreateBookmark(url: String) {
        viewModelScope.launch {
            val targetSpaceView = spaceViews.value.firstOrNull { view ->
                view.isSelected
            }
            val targetSpaceId = targetSpaceView?.obj?.targetSpaceId
            if (targetSpaceView != null && targetSpaceId != null) {
                val startTime = System.currentTimeMillis()
                createBookmarkObject(
                    CreateBookmarkObject.Params(
                        space = targetSpaceId,
                        url = url,
                        details = mapOf(
                            Relations.ORIGIN to ObjectOrigin.SHARING_EXTENSION.code.toDouble()
                        )
                    )
                ).process(
                    success = { obj ->
                        sendAnalyticsObjectCreateEvent(
                            analytics = analytics,
                            objType = MarketplaceObjectTypeIds.BOOKMARK,
                            route = EventsDictionary.Routes.sharingExtension,
                            startTime = startTime,
                            spaceParams = provideParams(spaceManager.get())
                        )
                        if (targetSpaceId == spaceManager.get()) {
                            navigation.emit(
                                OpenObjectNavigation.OpenEditor(
                                    target = obj,
                                    space = targetSpaceId
                                )
                            )
                        } else {
                            with(commands) {
                                emit(Command.ObjectAddToSpaceToast(targetSpaceView.obj.name))
                                emit(Command.Dismiss)
                            }
                        }
                    },
                    failure = {
                        Timber.d(it, "Error while creating bookmark")
                        sendToast("Error while creating bookmark: ${it.msg()}")
                    }
                )
            }
        }
    }

    fun onCreateNote(text: String) {
        viewModelScope.launch {
            val targetSpaceView = spaceViews.value.firstOrNull { view ->
                view.isSelected
            }
            val targetSpaceId = targetSpaceView?.obj?.targetSpaceId
            if (targetSpaceView != null && targetSpaceId != null) {
                val startTime = System.currentTimeMillis()
                createPrefilledNote.async(
                    CreatePrefilledNote.Params(
                        text = text,
                        space = targetSpaceId,
                        details = mapOf(
                            Relations.ORIGIN to ObjectOrigin.SHARING_EXTENSION.code.toDouble()
                        )
                    )
                ).fold(
                    onSuccess = { result ->
                        sendAnalyticsObjectCreateEvent(
                            analytics = analytics,
                            objType = MarketplaceObjectTypeIds.NOTE,
                            route = EventsDictionary.Routes.sharingExtension,
                            startTime = startTime,
                            spaceParams = provideParams(spaceManager.get())
                        )
                        if (targetSpaceId == spaceManager.get()) {
                            navigation.emit(
                                OpenObjectNavigation.OpenEditor(
                                    target = result,
                                    space = targetSpaceId
                                )
                            )
                        } else {
                            with(commands) {
                                emit(Command.ObjectAddToSpaceToast(targetSpaceView.obj.name))
                                emit(Command.Dismiss)
                            }
                        }
                    },
                    onFailure = {
                        Timber.d(it, "Error while creating note")
                        sendToast("Error while creating note: ${it.msg()}")
                    }
                )
            }
        }
    }

    fun onSelectSpaceClicked(view: SpaceView) {
        Timber.d("onSelectSpaceClicked: ${view.obj.targetSpaceId}")
        viewModelScope.launch {
            val targetSpaceId = view.obj.targetSpaceId
            if (targetSpaceId != null) {
                selectedSpaceId.value = targetSpaceId
            }
        }
    }

    fun onCancelClicked() {
        viewModelScope.launch {
            analytics.registerEvent(
                EventAnalytics.Anytype(
                    name = EventsDictionary.CLICK_ONBOARDING_TOOLTIP,
                    props = Props(
                        mapOf(
                            EventsPropertiesKey.id to CLICK_ONBOARDING_TOOLTIP_ID_SHARING_EXTENSION,
                            EventsPropertiesKey.type to CLICK_ONBOARDING_TOOLTIP_TYPE_CLOSE,
                        )
                    )
                )
            )
        }
    }

    fun onSharedMediaData(uris: List<String>) {
        viewModelScope.launch {
            state.value = ViewState.Default(
                uris.mapNotNull { uri ->
                    fileSharer.getDisplayName(uri)
                }.joinToString(separator = FILE_NAME_SEPARATOR)
            )
        }
    }

    fun onSharedTextData(text: String) {
        viewModelScope.launch {
            state.value = ViewState.Default(text)
        }
    }

    class Factory @Inject constructor(
        private val createBookmarkObject: CreateBookmarkObject,
        private val createPrefilledNote: CreatePrefilledNote,
        private val spaceManager: SpaceManager,
        private val urlBuilder: UrlBuilder,
        private val awaitAccountStartManager: AwaitAccountStartManager,
        private val analytics: Analytics,
        private val fileSharer: FileSharer,
        private val permissions: Permissions,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val fileDrop: FileDrop,
        private val eventProcessChannel: EventProcessDropFilesChannel,
        private val processCancel: ProcessCancel,
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddToAnytypeViewModel(
                createBookmarkObject = createBookmarkObject,
                spaceManager = spaceManager,
                createPrefilledNote = createPrefilledNote,
                urlBuilder = urlBuilder,
                awaitAccountStartManager = awaitAccountStartManager,
                analytics = analytics,
                fileSharer = fileSharer,
                permissions = permissions,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                fileDrop = fileDrop,
                eventProcessChannel = eventProcessChannel,
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer
            ) as T
        }
    }

    data class SpaceView(
        val obj: ObjectWrapper.SpaceView,
        val isSelected: Boolean,
        val icon: SpaceIconView
    )

    sealed class Command {
        data object Dismiss : Command()
        data class ObjectAddToSpaceToast(
            val spaceName: String?
        ) : Command()
    }

    sealed class ViewState {
        data object Init : ViewState()
        data class Default(val content: String) : ViewState()
    }

    sealed class ProgressState {
        data object Init : ProgressState()
        data class Progress(val wrapperObjId: Id, val processId: Id, val progress: Float) :
            ProgressState()

        data class Done(val wrapperObjId: Id) : ProgressState()
        data class Error(val error: String) : ProgressState()
    }

    companion object {
        const val FILE_NAME_SEPARATOR = ", "
    }
}