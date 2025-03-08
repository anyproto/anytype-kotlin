package com.anytypeio.anytype.feature_object_type.properties.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeStore
import javax.inject.Inject

class AddPropertyVmFactory @Inject constructor(
    private val addPropertyVmParams: AddPropertyVmParams,
    private val typePropertiesProvider: TypePropertiesProvider,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider,
    private val createRelation: CreateRelation,
    private val setObjectDetails: SetObjectDetails,
    private val objectTypesStore: ObjectTypeStore,
    private val setObjectTypeRecommendedFields: SetObjectTypeRecommendedFields
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddPropertyViewModel(
            vmParams = addPropertyVmParams,
            provider = typePropertiesProvider,
            storeOfRelations = storeOfRelations,
            stringResourceProvider = stringResourceProvider,
            createRelation = createRelation,
            setObjectDetails = setObjectDetails,
            objectTypesStore = objectTypesStore,
            setObjectTypeRecommendedFields = setObjectTypeRecommendedFields
        ) as T
}