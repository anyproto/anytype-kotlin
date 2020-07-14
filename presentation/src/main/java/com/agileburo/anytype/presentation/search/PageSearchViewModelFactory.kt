package com.agileburo.anytype.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetListPages

class PageSearchViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getListPages: GetListPages
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageSearchViewModel(
            urlBuilder = urlBuilder,
            getListPages = getListPages
        ) as T
    }
}