package com.anytypeio.anytype.gallery_experience.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import javax.inject.Inject

class GalleryInstallationViewModelFactory @Inject constructor(
    private val viewModelParams: GalleryInstallationViewModel.ViewModelParams,
    private val analytics: Analytics,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryInstallationViewModel(
            viewModelParams = viewModelParams,
            analytics = analytics,
        ) as T
    }
}