package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.gallery_experience.DownloadGalleryManifest
import com.anytypeio.anytype.domain.gallery_experience.ObjectImportExperience
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.GetSpaceViews
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationNavigation
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationSpacesState
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState
import com.anytypeio.anytype.gallery_experience.models.SpaceView
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class GalleryInstallationViewModel(
    private val viewModelParams: ViewModelParams,
    private val downloadGalleryManifest: DownloadGalleryManifest,
    private val objectImportExperience: ObjectImportExperience,
    private val analytics: Analytics,
    private val getSpaceViews: GetSpaceViews,
    private val createSpace: CreateSpace,
    private val urlBuilder: UrlBuilder,
    private val spaceGradientProvider: SpaceGradientProvider
) : ViewModel() {

    val mainState = MutableStateFlow<GalleryInstallationState>(GalleryInstallationState.Loading)
    val spacesViewState =
        MutableStateFlow(GalleryInstallationSpacesState(emptyList(), false))
    val command = MutableStateFlow<GalleryInstallationNavigation?>(null)

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
                    spacesViewState.value = GalleryInstallationSpacesState(
                        spaces = spaces.map {
                            it.toView(urlBuilder, spaceGradientProvider)
                        },
                        isNewButtonVisible = true
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
        command.value = GalleryInstallationNavigation.Dismiss
        val manifestInfo = (mainState.value as? GalleryInstallationState.Success)?.info ?: return
        val params = CreateSpace.Params(
            details = mapOf(
                Relations.NAME to manifestInfo.name,
                Relations.ICON_OPTION to spaceGradientProvider.randomId()
            )
        )
        viewModelScope.launch {
            createSpace.async(params).fold(
                onSuccess = { space ->
                    Timber.d("CreateSpace success, space: $space")
                    analytics.sendEvent(eventName = EventsDictionary.createSpace)
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

    fun onSpaceClick(space: SpaceView) {
        command.value = GalleryInstallationNavigation.Dismiss
        val manifestInfo = (mainState.value as? GalleryInstallationState.Success)?.info ?: return
        proceedWithInstallation(
            spaceId = SpaceId(space.obj.id),
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
        val params = ObjectImportExperience.Params(
            spaceId = spaceId,
            url = manifestInfo.downloadLink,
            title = manifestInfo.title,
            isNewSpace = isNewSpace
        )
        viewModelScope.launch {
            objectImportExperience.async(params).fold(
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

    data class ViewModelParams(
        val deepLinkType: String,
        val deepLinkSource: String
    )
}

private fun ObjectWrapper.SpaceView.toView(
    builder: UrlBuilder,
    spaceGradientProvider: SpaceGradientProvider
) = SpaceView(
    obj = this,
    icon = spaceIcon(builder, spaceGradientProvider)
)