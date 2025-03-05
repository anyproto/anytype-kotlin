package com.anytypeio.anytype.feature_object_type.properties.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import javax.inject.Inject

class AddPropertyVmFactory @Inject constructor(
    private val addPropertyVmParams: AddPropertyVmParams,
    private val typePropertiesProvider: TypePropertiesProvider,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddPropertyViewModel(
            vmParams = addPropertyVmParams,
            provider = typePropertiesProvider,
            storeOfRelations = storeOfRelations,
            stringResourceProvider = stringResourceProvider
        ) as T
}