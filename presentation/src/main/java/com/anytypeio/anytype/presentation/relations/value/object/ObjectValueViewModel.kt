package com.anytypeio.anytype.presentation.relations.value.`object`

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.getSpaces
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.toView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewState
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.toObjectView
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectValueViewModel(
    private val viewModelParams: ViewModelParams,
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val subscription: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val gradientProvider: SpaceGradientProvider
): BaseViewModel() {

    val viewState = MutableStateFlow<ObjectValueViewState>(ObjectValueViewState.Loading)
    private val query = MutableSharedFlow<String>(replay = 0)
    private var isRelationNotEditable = false
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart() {
        jobs += viewModelScope.launch {
            val relation = relations.get(relation = viewModelParams.relationKey)
            val searchParams = StoreSearchParams(
                subscription = SUB_RELATION_VALUE_OBJECTS,
                keys = ObjectSearchConstants.defaultKeys,
                filters = ObjectSearchConstants.filterAddObjectToRelation(
                    spaces = spaceManager.getSpaces(),
                    targetTypes = relation.relationFormatObjectTypes
                )
            )
            combine(
                values.subscribe(
                    ctx = viewModelParams.ctx,
                    target = viewModelParams.objectId
                ),
                query.onStart { emit("") },
                subscription.subscribe(searchParams)
            ) { record, query, objects ->
                Timber.d("objects: ${objects.size}")
            }.collect()
        }
    }

    private suspend fun initViewState(
        relation: ObjectWrapper.Relation,
        ids: List<Id>,
        objects: List<ObjectWrapper.Basic>,
        query: String
    ) {
        val result = mutableListOf<RelationsListItem>()
        result.addAll(mapObjects(ids, objects))

        viewState.value = if (result.isEmpty()) {
            ObjectValueViewState.Empty(
                isRelationEditable = !isRelationNotEditable,
                title = relation.name.orEmpty(),
            )
        } else {
            ObjectValueViewState.Content(
                isRelationEditable = !isRelationNotEditable,
                title = relation.name.orEmpty(),
                items = result
            )
        }
    }

    private suspend fun mapObjects(
        ids: List<Id>,
        objects: List<ObjectWrapper.Basic>
    ): List<RelationsListItem> = objects.mapNotNull { obj ->
        if (!obj.isValid) return@mapNotNull null
        val index = ids.indexOf(obj.id)
        val isSelected = index != -1
        RelationsListItem.Object(
            view = obj.toView(
                urlBuilder = urlBuilder,
                objectTypes = storeOfObjectTypes.getAll(),
                gradientProvider = gradientProvider
            ),
            isSelected = isSelected
        )
    }

    data class ViewModelParams(
        val ctx: Id,
        val objectId: Id,
        val relationKey: Key,
        val isLocked: Boolean,
        val relationContext: RelationContext
    )
}

sealed class ObjectValueViewState {

    object Loading : ObjectValueViewState()
    data class Empty(val title: String, val isRelationEditable: Boolean) :
        ObjectValueViewState()

    data class Content(
        val title: String,
        val items: List<RelationsListItem>,
        val isRelationEditable: Boolean,
        val showItemMenu: RelationsListItem? = null
    ) : ObjectValueViewState()
}

sealed class Command {
    object Dismiss : Command()
    object Expand : Command()
}

const val SUB_RELATION_VALUE_OBJECTS = "subscription.values.objects"