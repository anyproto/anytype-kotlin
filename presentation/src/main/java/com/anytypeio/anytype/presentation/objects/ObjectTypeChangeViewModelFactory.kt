package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes

class ObjectTypeChangeViewModelFactory(
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ObjectTypeChangeViewModel(
            storeOfObjectTypes = storeOfObjectTypes
        ) as T
    }
}