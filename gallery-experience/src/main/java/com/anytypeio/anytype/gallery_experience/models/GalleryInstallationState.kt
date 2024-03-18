package com.anytypeio.anytype.gallery_experience.models

import com.anytypeio.anytype.core_models.ManifestInfo

sealed class GalleryInstallationState {
    object Hidden : GalleryInstallationState()
    object Loading : GalleryInstallationState()
    data class Success(val info: ManifestInfo) : GalleryInstallationState()
}

sealed class GalleryInstallationNavigation(val route: String) {
    object Main : GalleryInstallationNavigation("main")
    object Spaces : GalleryInstallationNavigation("spaces")
    object Success : GalleryInstallationNavigation("success")
    object Error : GalleryInstallationNavigation("error")
    object Dismiss : GalleryInstallationNavigation("")
}