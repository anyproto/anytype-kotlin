package com.agileburo.anytype.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.page.navigation.GetListPages
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks

class PageNavigationViewModelFactory(
    private val getPageInfoWithLinks: GetPageInfoWithLinks,
    private val getListPages: GetListPages
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageNavigationViewModel(
            getPageInfoWithLinks = getPageInfoWithLinks,
            getListPages = getListPages
        ) as T
    }
}