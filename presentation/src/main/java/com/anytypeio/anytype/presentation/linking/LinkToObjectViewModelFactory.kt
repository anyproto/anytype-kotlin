package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.block.interactor.CreateLinkToObject
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetObjectInfoWithLinks

class LinkToObjectViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getObjectInfoWithLinks: GetObjectInfoWithLinks,
    private val createLinkToObject: CreateLinkToObject,
    private val getConfig: GetConfig
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LinkToObjectViewModel(
            urlBuilder = urlBuilder,
            getObjectInfoWithLinks = getObjectInfoWithLinks,
            createLinkToObject = createLinkToObject,
            getConfig = getConfig
        ) as T
    }
}