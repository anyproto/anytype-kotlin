package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
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
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.spaceIcon
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
    val command = MutableStateFlow<GalleryInstallationNavigation?>(null)

    private val MAX_SPACES = 10

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
                    } else {
                        Timber.e("DownloadGalleryManifest failed, manifestInfo is null")
                        command.value = GalleryInstallationNavigation.Error
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "DownloadGalleryManifest failed")
                    command.value = GalleryInstallationNavigation.Dismiss
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
                        isNewButtonVisible = filteredSpaces.size < MAX_SPACES
                    )
                    command.value = GalleryInstallationNavigation.Spaces
                },
                onFailure = { error ->
                    Timber.e(error, "GetSpaceViews failed")
                }
            )
        }
    }

    fun onNewSpaceClick() {
        subscribeToEventProcessChannel()
        command.value = GalleryInstallationNavigation.Dismiss
        val manifestInfo = (mainState.value as? GalleryInstallationState.Success)?.info ?: return
        mainState.value =
            (mainState.value as? GalleryInstallationState.Success)?.copy(isLoading = true) ?: return
        val params = CreateSpace.Params(
            details = mapOf(
                Relations.NAME to manifestInfo.name,
                Relations.ICON_OPTION to spaceGradientProvider.randomId().toDouble()
            )
        )
        viewModelScope.launch {
            createSpace.async(params).fold(
                onSuccess = { space ->
                    Timber.d("CreateSpace success, space: $space")
                    proceedWithInstallation(
                        spaceId = SpaceId(space),
                        isNewSpace = true,
                        manifestInfo = manifestInfo
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "CreateSpace failed")
                }
            )
        }
    }

    fun onSpaceClick(space: GallerySpaceView) {
        subscribeToEventProcessChannel()
        Timber.d("onSpaceClick, space: $space")
        command.value = GalleryInstallationNavigation.Dismiss
        mainState.value =
            (mainState.value as? GalleryInstallationState.Success)?.copy(isLoading = true) ?: return
        val manifestInfo = (mainState.value as? GalleryInstallationState.Success)?.info ?: return
        val spaceId = space.obj.targetSpaceId
        if (spaceId == null) {
            Timber.e("onSpaceClick, spaceId is null")
            return
        }
        proceedWithInstallation(
            spaceId = SpaceId(spaceId),
            isNewSpace = false,
            manifestInfo = manifestInfo
        )
    }

    fun onDismiss() {
        command.value = GalleryInstallationNavigation.Dismiss
    }

    private fun proceedWithInstallation(
        spaceId: SpaceId,
        isNewSpace: Boolean,
        manifestInfo: ManifestInfo
    ) {
        val params = ImportExperience.Params(
            spaceId = spaceId,
            url = manifestInfo.downloadLink,
            title = manifestInfo.title,
            isNewSpace = isNewSpace
        )
        viewModelScope.launch {
            importExperience.async(params).fold(
                onSuccess = {
                    Timber.d("ObjectImportExperience success")
                    command.value = GalleryInstallationNavigation.Success
                },
                onFailure = { error ->
                    Timber.e(error, "ObjectImportExperience failed")
                    command.value = GalleryInstallationNavigation.Error
                }
            )
        }
    }

    private fun subscribeToEventProcessChannel() {
        viewModelScope.launch {
            eventProcessChannel.observe().collect { events ->
                Timber.d("EventProcessChannel events: $events")
                if (events.any { it is Process.Event.Done && it.process?.type == Process.Type.IMPORT }) {
                    command.value = GalleryInstallationNavigation.Exit
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
}

private fun ObjectWrapper.SpaceView.toView(
    builder: UrlBuilder,
    spaceGradientProvider: SpaceGradientProvider
) = GallerySpaceView(
    obj = this,
    icon = spaceIcon(builder, spaceGradientProvider)
)