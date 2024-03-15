package com.anytypeio.anytype.gallery_experience.models

sealed class GalleryInstallationState {
    object Hidden : GalleryInstallationState()
    object Loading : GalleryInstallationState()
    object Success : GalleryInstallationState()
}

sealed class GalleryInstallationNavigation(val route: String) {
    object Main : GalleryInstallationNavigation("main")
    object Spaces : GalleryInstallationNavigation("spaces")
    object Success : GalleryInstallationNavigation("success")
    object Error : GalleryInstallationNavigation("error")
    object Dismiss : GalleryInstallationNavigation("")
}