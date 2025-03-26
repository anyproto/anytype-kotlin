package com.anytypeio.anytype.feature_properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_properties.add.EditTypePropertiesVmParams
import javax.inject.Inject

class EditTypePropertiesViewModelFactory @Inject constructor(
    private val vmParams: EditTypePropertiesVmParams,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider,
    private val createRelation: CreateRelation,
    private val setObjectDetails: SetObjectDetails,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val setObjectTypeRecommendedFields: SetObjectTypeRecommendedFields,
    private val urlBuilder: UrlBuilder
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EditTypePropertiesViewModel(
            vmParams = vmParams,
            storeOfRelations = storeOfRelations,
            stringResourceProvider = stringResourceProvider,
            createRelation = createRelation,
            setObjectDetails = setObjectDetails,
            storeOfObjectTypes = storeOfObjectTypes,
            setObjectTypeRecommendedFields = setObjectTypeRecommendedFields,
            urlBuilder = urlBuilder
        ) as T
}