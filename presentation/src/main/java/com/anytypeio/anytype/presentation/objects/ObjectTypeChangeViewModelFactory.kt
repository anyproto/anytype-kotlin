package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes

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