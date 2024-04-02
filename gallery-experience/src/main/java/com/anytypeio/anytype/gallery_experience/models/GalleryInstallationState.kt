package com.anytypeio.anytype.gallery_experience.models

import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

sealed class GalleryInstallationState {
    object Hidden : GalleryInstallationState()
    object Loading : GalleryInstallationState()
    data class Success(val info: ManifestInfo, val isLoading: Boolean = false) : GalleryInstallationState()
}

data class GalleryInstallationSpacesState(
    val spaces: List<GallerySpaceView>,
    val isNewButtonVisible: Boolean
)

sealed class GalleryInstallationNavigation(val route: String) {
    object Main : GalleryInstallationNavigation("main")
    object Spaces : GalleryInstallationNavigation("spaces")
    object CloseSpaces : GalleryInstallationNavigation("closeSpaces")
    object Dismiss : GalleryInstallationNavigation("")
}

data class GallerySpaceView(
    val obj: ObjectWrapper.SpaceView,
    val icon: SpaceIconView
)