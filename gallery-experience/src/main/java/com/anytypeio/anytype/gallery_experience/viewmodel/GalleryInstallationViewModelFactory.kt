package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.gallery_experience.DownloadGalleryManifest
import com.anytypeio.anytype.domain.gallery_experience.ImportExperience
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.GetSpaceViews
import com.anytypeio.anytype.domain.workspace.EventProcessChannel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject

class GalleryInstallationViewModelFactory @Inject constructor(
    private val viewModelParams: GalleryInstallationViewModel.ViewModelParams,
    private val downloadGalleryManifest: DownloadGalleryManifest,
    private val importExperience: ImportExperience,
    private val analytics: Analytics,
    private val getSpaceViews: GetSpaceViews,
    private val createSpace: CreateSpace,
    private val urlBuilder: UrlBuilder,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val  userPermissionProvider: UserPermissionProvider,
    private val eventProcessChannel: EventProcessChannel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryInstallationViewModel(
            viewModelParams = viewModelParams,
            downloadGalleryManifest = downloadGalleryManifest,
            importExperience = importExperience,
            analytics = analytics,
            getSpaceViews = getSpaceViews,
            urlBuilder = urlBuilder,
            spaceGradientProvider = spaceGradientProvider,
            createSpace = createSpace,
            userPermissionProvider = userPermissionProvider,
            eventProcessChannel = eventProcessChannel
        ) as T
    }
}