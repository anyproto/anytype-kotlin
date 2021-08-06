package com.anytypeio.anytype.presentation.moving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetObjectInfoWithLinks

class MoveToViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getObjectInfoWithLinks: GetObjectInfoWithLinks,
    private val getConfig: GetConfig,
    private val move: Move,
    private val objectTypesProvider: ObjectTypesProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MoveToViewModel(
            urlBuilder = urlBuilder,
            getObjectInfoWithLinks = getObjectInfoWithLinks,
            getConfig = getConfig,
            move = move,
            objectTypesProvider = objectTypesProvider
        ) as T
    }
}