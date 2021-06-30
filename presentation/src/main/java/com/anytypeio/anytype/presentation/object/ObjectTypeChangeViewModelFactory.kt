package com.anytypeio.anytype.presentation.`object`

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectTypeChangeViewModelFactory(
    private val getCompatibleObjectTypes: GetCompatibleObjectTypes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ObjectTypeChangeViewModel(
            getCompatibleObjectTypes = getCompatibleObjectTypes
        ) as T
    }
}