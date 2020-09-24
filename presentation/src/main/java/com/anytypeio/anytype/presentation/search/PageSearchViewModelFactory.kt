package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetListPages

class PageSearchViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getListPages: GetListPages,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageSearchViewModel(
            urlBuilder = urlBuilder,
            getListPages = getListPages,
            analytics = analytics
        ) as T
    }
}