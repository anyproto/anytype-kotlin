package com.anytypeio.anytype.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class MediaViewModel(
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    class Factory @Inject constructor(
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MediaViewModel(
                urlBuilder = urlBuilder
            ) as T
        }
    }
}