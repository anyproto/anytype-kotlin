package com.anytypeio.anytype.feature_create_object.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import javax.inject.Inject

/**
 * Factory for creating CreateObjectViewModel instances with dependency injection.
 */
class CreateObjectViewModelFactory @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewCreateObjectViewModel(
            storeOfObjectTypes = storeOfObjectTypes
        ) as T
    }
}
