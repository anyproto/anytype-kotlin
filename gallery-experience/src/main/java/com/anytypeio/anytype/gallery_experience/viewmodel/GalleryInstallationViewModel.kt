package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.galleryInstallSuccess
import com.anytypeio.anytype.analytics.base.EventsDictionary.galleryParamExisting
import com.anytypeio.anytype.analytics.base.EventsDictionary.galleryParamNew
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenGalleryInstall
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Process
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.gallery_experience.DownloadGalleryManifest
import com.anytypeio.anytype.domain.gallery_experience.ImportExperience
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.GetSpaceViews
import com.anytypeio.anytype.domain.workspace.EventProcessChannel
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationNavigation
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationSpacesState
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState
import com.anytypeio.anytype.gallery_experience.models.GallerySpaceView
import com.anytypeio.anytype.presentation.extension.getTypePropName
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel.Companion.MAX_SPACE_COUNT
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class GalleryInstallationViewModel(
    private val viewModelParams: ViewModelParams,
    private val downloadGalleryManifest: DownloadGalleryManifest,
    private val importExperience: ImportExperience,
    private val analytics: Analytics,
    private val getSpaceViews: GetSpaceViews,
    private val createSpace: CreateSpace,
    private val urlBuilder: UrlBuilder,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val userPermissionProvider: UserPermissionProvider,
    private val eventProcessChannel: EventProcessChannel
) : ViewModel() {

    val mainState = MutableStateFlow<GalleryInstallationState>(GalleryInstallationState.Loading)
    val spacesViewState =
        MutableStateFlow(GalleryInstallationSpacesState(emptyList(), false))
    val command = MutableSharedFlow<GalleryInstallationNavigation>(replay = 0)
    val errorState = MutableSharedFlow<String?>(replay = 0)

    init {
        Timber.d("GalleryInstallationViewModel init, viewModelParams: $viewModelParams")
        downloadGalleryManifest()
    }

    private fun downloadGalleryManifest() {
        viewModelScope.launch {
            val params = DownloadGalleryManifest.Params(url = viewModelParams.deepLinkSource)
            downloadGalleryManifest.async(params).fold(
                onSuccess = { manifestInfo ->
                    if (manifestInfo != null) {
                        Timber.d("DownloadGalleryManifest success, manifestInfo: $manifestInfo")
                        mainState.value = GalleryInstallationState.Success(manifestInfo)
                        sendScreenEvent(name = manifestInfo.title)
                    } else {
                        Timber.e("DownloadGalleryManifest failed, manifestInfo is null")
                        errorState.emit("Download manifest error: manifestInfo is null")
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "DownloadGalleryManifest failed")
                    errorState.emit("Download manifest error: ${error.message}")
                }
            )
        }
    }

    fun onInstallClicked() {
        viewModelScope.launch {
            getSpaceViews.async(Unit).fold(
                onSuccess = { spaces ->
                    Timber.d("GetSpaceViews success, spaceViews: $spaces")
                    val filteredSpaces = filterSpacesByPermissions(spaces)
                    spacesViewState.value = GalleryInstallationSpacesState(
                        spaces = filteredSpaces.map {
                            it.toView(urlBuilder, spaceGradientProvider)
                        },
                        isNewButtonVisible = filteredSpaces.size < MAX_SPACE_COUNT
                    )
                    command.emit(GalleryInstallationNavigation.Spaces)
                },
                onFailure = { error ->
                    Timber.e(error, "GetSpaceViews failed")
                    errorState.emit("Get Spaces error: ${error.message}")
                }
            )
            analytics.sendEvent(
                eventName = EventsDictionary.clickGalleryInstall
            )
        }
    }

    fun onNewSpaceClick() {
        Timber.d("onNewSpaceClick")
        viewModelScope.launch {
            command.emit(GalleryInstallationNavigation.CloseSpaces)
            val state = (mainState.value as? GalleryInstallationState.Success) ?: return@launch
            val manifestInfo = state.info
            mainState.value = state.copy(isLoading = true)
            val params = CreateSpace.Params(
                details = mapOf(
                    Relations.NAME to manifestInfo.title,
                    Relations.ICON_OPTION to spaceGradientProvider.randomId().toDouble()
                )
            )
            createSpace.async(params).fold(
                onSuccess = { space ->
                    Timber.d("CreateSpace success, space: $space")
                    analytics.sendEvent(
                        eventName = EventsDictionary.clickGalleryInstallSpace,
                        props = Props(
                            mapOf(EventsPropertiesKey.type to galleryParamNew)
                        )
                    )
                    analytics.sendEvent(
                        eventName = EventsDictionary.createSpace,
                        props = Props(
                            mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.gallery)
                        )
                    )
                    proceedWithInstallation(
                        spaceId = SpaceId(space),
                        isNewSpace = true,
                        manifestInfo = manifestInfo,
                    )
                },
                onFailure = { error ->
                    mainState.value = state.copy(isLoading = false)
                    errorState.emit("Space creation error: ${error.message}")
                    Timber.e(error, "CreateSpace failed")
                }
            )
        }
    }

    fun onSpaceClick(space: GallerySpaceView) {
        Timber.d("onSpaceClick, space: $space")
        viewModelScope.launch {
            command.emit(GalleryInstallationNavigation.CloseSpaces)
            val state = (mainState.value as? GalleryInstallationState.Success) ?: return@launch
            mainState.value = state.copy(isLoading = true)
            val spaceId = space.obj.targetSpaceId
            if (spaceId == null) {
                Timber.e("onSpaceClick, spaceId is null")
                return@launch
            }
            analytics.sendEvent(
                eventName = EventsDictionary.clickGalleryInstallSpace,
                props = Props(
                    mapOf(EventsPropertiesKey.type to galleryParamExisting)
                )
            )
            proceedWithInstallation(
                spaceId = SpaceId(spaceId),
                isNewSpace = false,
                manifestInfo = state.info,
            )
        }
    }

    fun onDismiss() {
        Timber.d("onDismiss")
        viewModelScope.launch {
            command.emit(GalleryInstallationNavigation.Dismiss)
        }
    }

    fun onCloseSpaces() {
        Timber.d("onCloseSpaces")
        viewModelScope.launch {
            command.emit(GalleryInstallationNavigation.CloseSpaces)
        }
    }

    private suspend fun proceedWithInstallation(
        spaceId: SpaceId,
        isNewSpace: Boolean,
        manifestInfo: ManifestInfo
    ) {
        analytics.sendEvent(
            eventName = galleryInstallSuccess,
            props = Props(
                mapOf(EventsPropertiesKey.name to manifestInfo.title)
            ),
        )
        val params = ImportExperience.Params(
            spaceId = spaceId,
            url = manifestInfo.downloadLink,
            title = manifestInfo.title,
            isNewSpace = isNewSpace
        )
        importExperience.stream(params).collect { result ->
            result.fold(
                onLoading = {
                    //We immediately close the screen after sending the importExperience command,
                    // as either an error or success will be returned in the form of
                    // a Notification Event, which should be handled in the MainViewModel.
                    command.emit(GalleryInstallationNavigation.Dismiss)
                },
                onSuccess = { Timber.d("ObjectImportExperience success") },
                onFailure = { error -> Timber.e(error, "ObjectImportExperience failed") }
            )
        }
    }

    private fun subscribeToEventProcessChannel() {
        viewModelScope.launch {
            eventProcessChannel.observe().collect { events ->
                Timber.d("EventProcessChannel events: $events")
                if (events.any { it is Process.Event.Done && it.process?.type == Process.Type.IMPORT }) {
                    command.emit(GalleryInstallationNavigation.Dismiss)
                }
            }
        }
    }

    private fun filterSpacesByPermissions(spaces: List<ObjectWrapper.SpaceView>): List<ObjectWrapper.SpaceView> {
        return spaces.filter {
            val targetSpaceId = it.targetSpaceId ?: return@filter false
            val userPermissions = userPermissionProvider.get(SpaceId(targetSpaceId))
            userPermissions?.isOwnerOrEditor() == true
        }
    }

    data class ViewModelParams(
        val deepLinkType: String,
        val deepLinkSource: String
    )

    private suspend fun sendScreenEvent(name: String) {
        analytics.sendEvent(
            eventName = screenGalleryInstall,
            props = Props(
                mapOf(EventsPropertiesKey.name to name)
            ),
        )
    }
}

private fun ObjectWrapper.SpaceView.toView(
    builder: UrlBuilder,
    spaceGradientProvider: SpaceGradientProvider
) = GallerySpaceView(
    obj = this,
    icon = spaceIcon(builder, spaceGradientProvider)
)