package com.anytypeio.anytype.presentation.relations.value.tagstatus

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class TagStatusViewModel(
    private val params: Params,
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val details: ObjectDetailProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val getOptions: GetOptions,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val viewState = MutableStateFlow<TagStatusViewState>(TagStatusViewState.Loading)

    fun onStart() {
        Timber.d("TagStatusViewModel onStart, params: $params")
    }

    fun onAction(action: TagStatusAction) {
        Timber.d("TagStatusViewModel onAction, action: $action")
    }

    data class Params(
        val ctx: Id,
        val objectId: Id,
        val relationKey: Id
    )
}

sealed class TagStatusViewState {

    object Loading : TagStatusViewState()
    data class Empty(val title: String, val isRelationEditable: Boolean) :
        TagStatusViewState()

    data class Content(
        val title: String,
        val items: List<RelationValueView>,
        val isRelationEditable: Boolean
    ) : TagStatusViewState()
}

sealed class TagStatusAction {
    data class Click(val item: RelationValueView) : TagStatusAction()
    data class LongClick(val item: RelationValueView) : TagStatusAction()
    object Clear : TagStatusAction()
    object Plus : TagStatusAction()
}