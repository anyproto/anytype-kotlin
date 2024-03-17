package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.gallery_experience.DownloadGalleryManifest
import com.anytypeio.anytype.domain.gallery_experience.ObjectImportExperience
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationNavigation
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class GalleryInstallationViewModel(
    private val viewModelParams: ViewModelParams,
    private val downloadGalleryManifest: DownloadGalleryManifest,
    private val objectImportExperience: ObjectImportExperience,
    private val analytics: Analytics,
) : ViewModel() {

    val mainState = MutableStateFlow<GalleryInstallationState>(GalleryInstallationState.Hidden)
    val command = MutableStateFlow<GalleryInstallationNavigation?>(null)

    init {
        Timber.d("GalleryInstallationViewModel init, viewModelParams: $viewModelParams")
    }

    data class ViewModelParams(
        val deepLinkType: String,
        val deepLinkSource: String
    )
}