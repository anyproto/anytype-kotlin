package com.agileburo.anytype.presentation.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.block.interactor.CreateLinkToObject
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks

class LinkToObjectViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getPageInfoWithLinks: GetPageInfoWithLinks,
    private val createLinkToObject: CreateLinkToObject,
    private val getConfig: GetConfig
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LinkToObjectViewModel(
            urlBuilder = urlBuilder,
            getPageInfoWithLinks = getPageInfoWithLinks,
            createLinkToObject = createLinkToObject,
            getConfig = getConfig
        ) as T
    }
}