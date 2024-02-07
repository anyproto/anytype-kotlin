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
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.ObjectOrigin
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.spaces.GetSpaceViews
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class AddToAnytypeViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val createPrefilledNote: CreatePrefilledNote,
    private val spaceManager: SpaceManager,
    private val getSpaceViews: GetSpaceViews,
    private val urlBuilder: UrlBuilder,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val analytics: Analytics,
    private val uploadFile: UploadFile,
    private val fileSharer: FileSharer
) : BaseViewModel() {

    private val selectedSpaceId = MutableStateFlow(NO_VALUE)

    private val spaces: Flow<List<ObjectWrapper.SpaceView>> = getSpaceViews.asFlow(Unit)

    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    val spaceViews = MutableStateFlow<List<SpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>()

    val state = MutableStateFlow<ViewState>(ViewState.Init)

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
                .isStarted()
                .filter { isStarted -> isStarted }
                .flatMapLatest {
                    combine(
                        spaces,
                        selectedSpaceId
                    ) { spaces, selected ->
                        spaces.mapIndexed { index, space ->
                            SpaceView(
                                obj = space,
                                icon = space.spaceIcon(
                                    builder = urlBuilder,
                                    spaceGradientProvider = SpaceGradientProvider.Default
                                ),
                                isSelected = if (selected.isEmpty()) {
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

    fun onShareMedia(uris: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetSpaceView = spaceViews.value.firstOrNull { view ->
                view.isSelected
            }
            val targetSpaceId = targetSpaceView?.obj?.targetSpaceId!!
            val paths = uris.mapNotNull { uri ->
                fileSharer.getPath(uri)
            }
            val files = mutableListOf<Id>()
            paths.forEach { path ->
                uploadFile.async(
                    UploadFile.Params(
                        path = path,
                        space = SpaceId(targetSpaceId),
                        // Temporary workaround to fix issue on the MW side.
                        type = Block.Content.File.Type.NONE
                    )
                ).onSuccess { obj ->
                    files.add(obj.id)
                }
            }
            if (files.size == 1) {
                if (targetSpaceId == spaceManager.get()) {
                    navigation.emit(OpenObjectNavigation.OpenEditor(files.first()))
                } else {
                    with(commands) {
                        emit(Command.ObjectAddToSpaceToast(targetSpaceView.obj.name))
                        emit(Command.Dismiss)
                    }
                }
            } else {
                val startTime = System.currentTimeMillis()
                createPrefilledNote.async(
                    CreatePrefilledNote.Params(
                        text = EMPTY_STRING_VALUE,
                        space = targetSpaceId,
                        details = mapOf(
                            Relations.ORIGIN to ObjectOrigin.SHARING_EXTENSION.code.toDouble()
                        ),
                        attachments = files
                    )
                ).fold(
                    onSuccess = { result ->
                        sendAnalyticsObjectCreateEvent(
                            analytics = analytics,
                            objType = MarketplaceObjectTypeIds.NOTE,
                            route = EventsDictionary.Routes.sharingExtension,
                            startTime = startTime
                        )
                        if (targetSpaceId == spaceManager.get()) {
                            navigation.emit(OpenObjectNavigation.OpenEditor(result))
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
                            startTime = startTime
                        )
                        if (targetSpaceId == spaceManager.get()) {
                            navigation.emit(OpenObjectNavigation.OpenEditor(obj))
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
                            startTime = startTime
                        )
                        if (targetSpaceId == spaceManager.get()) {
                            navigation.emit(OpenObjectNavigation.OpenEditor(result))
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

    fun onSharedData(uris: List<String>) {
        viewModelScope.launch {
            state.value = ViewState.Default(
                uris.mapNotNull { uri -> fileSharer.getDisplayName(uri) }.toString()
            )
        }
    }

    class Factory @Inject constructor(
        private val createBookmarkObject: CreateBookmarkObject,
        private val createPrefilledNote: CreatePrefilledNote,
        private val spaceManager: SpaceManager,
        private val getSpaceViews: GetSpaceViews,
        private val urlBuilder: UrlBuilder,
        private val awaitAccountStartManager: AwaitAccountStartManager,
        private val analytics: Analytics,
        private val uploadFile: UploadFile,
        private val fileSharer: FileSharer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddToAnytypeViewModel(
                createBookmarkObject = createBookmarkObject,
                spaceManager = spaceManager,
                createPrefilledNote = createPrefilledNote,
                getSpaceViews = getSpaceViews,
                urlBuilder = urlBuilder,
                awaitAccountStartManager = awaitAccountStartManager,
                analytics = analytics,
                uploadFile = uploadFile,
                fileSharer = fileSharer
            ) as T
        }
    }

    data class SpaceView(
        val obj: ObjectWrapper.SpaceView,
        val isSelected: Boolean,
        val icon: SpaceIconView
    )

    sealed class Command {
        object Dismiss : Command()
        data class ObjectAddToSpaceToast(
            val spaceName: String?
        ) : Command()
    }

    sealed class ViewState {
        object Init : ViewState()
        data class Default(val content: String) : ViewState()
    }
}