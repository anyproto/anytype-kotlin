package com.anytypeio.anytype.presentation.relations.option

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import javax.inject.Inject

class CreateOrEditOptionViewModelFactory  @Inject constructor(
    private val params: CreateOrEditOptionViewModel.ViewModelParams,
    private val values: ObjectValueProvider,
    private val createOption: CreateRelationOption,
    private val setObjectDetails: SetObjectDetails,
    private val dispatcher: Dispatcher<Payload>,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
    private val storeOfRelations: StoreOfRelations
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ) = CreateOrEditOptionViewModel(
        viewModelParams = params,
        values = values,
        createOption = createOption,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        spaceManager = spaceManager,
        analytics = analytics,
        storeOfRelations = storeOfRelations
    ) as T
}