package com.anytypeio.anytype.presentation.relations.value.attachment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import javax.inject.Inject

class AttachmentValueViewModelFactory @Inject constructor(
    private val params: AttachmentValueViewModel.ViewModelParams,
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ) = AttachmentValueViewModel(
        viewModelParams = params,
        relations = relations,
        values = values,
        dispatcher = dispatcher,
        setObjectDetails = setObjectDetails,
        analytics = analytics,
        spaceManager = spaceManager
    ) as T
}