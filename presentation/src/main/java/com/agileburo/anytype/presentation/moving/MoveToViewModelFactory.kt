package com.agileburo.anytype.presentation.moving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks

class MoveToViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getPageInfoWithLinks: GetPageInfoWithLinks,
    private val getConfig: GetConfig,
    private val move: Move
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MoveToViewModel(
            urlBuilder = urlBuilder,
            getPageInfoWithLinks = getPageInfoWithLinks,
            getConfig = getConfig,
            move = move
        ) as T
    }
}