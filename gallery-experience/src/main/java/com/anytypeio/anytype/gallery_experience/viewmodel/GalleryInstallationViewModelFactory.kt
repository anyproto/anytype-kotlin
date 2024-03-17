package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.gallery_experience.DownloadGalleryManifest
import com.anytypeio.anytype.domain.gallery_experience.ObjectImportExperience
import javax.inject.Inject

class GalleryInstallationViewModelFactory @Inject constructor(
    private val viewModelParams: GalleryInstallationViewModel.ViewModelParams,
    private val downloadGalleryManifest: DownloadGalleryManifest,
    private val objectImportExperience: ObjectImportExperience,
    private val analytics: Analytics,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryInstallationViewModel(
            viewModelParams = viewModelParams,
            downloadGalleryManifest = downloadGalleryManifest,
            objectImportExperience = objectImportExperience,
            analytics = analytics,
        ) as T
    }
}