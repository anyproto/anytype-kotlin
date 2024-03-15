package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationNavigation
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class GalleryInstallationViewModel(
    private val analytics: Analytics,
) : ViewModel() {

    val mainState = MutableStateFlow<GalleryInstallationState>(GalleryInstallationState.Hidden)
    val command = MutableStateFlow<GalleryInstallationNavigation?>(null)

    init {
        Timber.d("GalleryInstallationViewModel init")
    }
}