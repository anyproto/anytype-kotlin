package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchWithMeta
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class GlobalSearchViewModel(
    private val searchWithMeta: SearchWithMeta,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    // TODO



    class Factory @Inject constructor(
        private val searchWithMeta: SearchWithMeta,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalSearchViewModel(
                searchWithMeta = searchWithMeta,
                urlBuilder = urlBuilder
            ) as T
        }
    }
}