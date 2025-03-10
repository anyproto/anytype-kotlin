package com.anytypeio.anytype.feature_object_type.properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_object_type.properties.add.AddPropertyVmParams
import javax.inject.Inject

class AddPropertyVmFactory @Inject constructor(
    private val addPropertyVmParams: AddPropertyVmParams,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider,
    private val createRelation: CreateRelation,
    private val setObjectDetails: SetObjectDetails,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val setObjectTypeRecommendedFields: SetObjectTypeRecommendedFields
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddOrEditTypePropertyViewModel(
            vmParams = addPropertyVmParams,
            storeOfRelations = storeOfRelations,
            stringResourceProvider = stringResourceProvider,
            createRelation = createRelation,
            setObjectDetails = setObjectDetails,
            storeOfObjectTypes = storeOfObjectTypes,
            setObjectTypeRecommendedFields = setObjectTypeRecommendedFields
        ) as T
}