package com.anytypeio.anytype.gallery_experience.models

import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

sealed class GalleryInstallationState {
    object Hidden : GalleryInstallationState()
    object Loading : GalleryInstallationState()
    data class Success(val info: ManifestInfo) : GalleryInstallationState()
}

data class GalleryInstallationSpacesState(
    val spaces: List<SpaceView>,
    val isNewButtonVisible: Boolean
)

sealed class GalleryInstallationNavigation(val route: String) {
    object Main : GalleryInstallationNavigation("main")
    object Spaces : GalleryInstallationNavigation("spaces")
    object Success : GalleryInstallationNavigation("success")
    object Error : GalleryInstallationNavigation("error")
    object Dismiss : GalleryInstallationNavigation("")
}

data class SpaceView(
    val obj: ObjectWrapper.SpaceView,
    val icon: SpaceIconView
)