package com.anytypeio.anytype.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetObjectInfoWithLinks

class PageNavigationViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getObjectInfoWithLinks: GetObjectInfoWithLinks,
    private val getConfig: GetConfig,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageNavigationViewModel(
            urlBuilder = urlBuilder,
            getObjectInfoWithLinks = getObjectInfoWithLinks,
            getConfig = getConfig,
            analytics = analytics
        ) as T
    }
}