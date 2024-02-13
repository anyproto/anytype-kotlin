package com.anytypeio.anytype.presentation.relations.value.`object`

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.getSpaces
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.toView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.filterIdsById
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val gradientProvider: SpaceGradientProvider,
    private val objectListIsArchived: SetObjectListIsArchived,
    private val duplicateObject: DuplicateObject
) : BaseViewModel() {

    val viewState = MutableStateFlow<ObjectValueViewState>(ObjectValueViewState.Loading)
    private val query = MutableSharedFlow<String>(replay = 0)
    private var isEditableRelation = false
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val jobs = mutableListOf<Job>()

    private val initialIds = mutableListOf<Id>()
    private var isInitialSortDone = false

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
                setupIsRelationNotEditable(relation)
                val ids = getRecordValues(record)
                if (!isInitialSortDone) {
                    initialIds.clear()
                    if (ids.isNotEmpty()) {
                        initialIds.addAll(ids)
                    } else {
                        emitCommand(Command.Expand)
                    }
                }
                initViewState(
                    relation = relation,
                    ids = ids,
                    objects = objects,
                    query = query
                )
            }.collect()
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        isInitialSortDone = false
        initialIds.clear()
    }

    private fun emitCommand(command: Command, delay: Long = 0L) {
        viewModelScope.launch {
            delay(delay)
            commands.emit(command)
        }
    }

    fun onQueryChanged(input: String) {
        viewModelScope.launch {
            query.emit(input)
        }
    }

    private fun setupIsRelationNotEditable(relation: ObjectWrapper.Relation) {
        isEditableRelation = !(viewModelParams.isLocked
                || relation.isReadonlyValue
                || relation.isHidden == true
                || relation.isDeleted == true
                || relation.isArchived == true
                || !relation.isValid)
    }

    private suspend fun initViewState(
        relation: ObjectWrapper.Relation,
        ids: List<Id>,
        objects: List<ObjectWrapper.Basic>,
        query: String
    ) {
        val views = mapObjects(ids, objects, query)
        viewState.value = if (views.isNotEmpty()) {
            ObjectValueViewState.Content(
                isRelationEditable = !isEditableRelation,
                title = relation.name.orEmpty(),
                items = buildList {
                    val typeNames = mutableListOf<String>()
                    relation.relationFormatObjectTypes.forEach { it ->
                        storeOfObjectTypes.get(it)?.let { type ->
                            val name = type.name
                            if (!name.isNullOrBlank()) typeNames.add(name)
                        }
                    }
                    val objectTypeNames = typeNames.joinToString(", ")
                    add(ObjectValueItem.ObjectType(name = objectTypeNames))
                    addAll(views)
                }
            )
        } else {
            ObjectValueViewState.Empty(
                isRelationEditable = !isEditableRelation,
                title = relation.name.orEmpty(),
            )
        }
    }

    private suspend fun mapObjects(
        ids: List<Id>,
        objects: List<ObjectWrapper.Basic>,
        query: String
    ): List<ObjectValueItem.Object> = objects.mapNotNull { obj ->
        if (!obj.isValid) return@mapNotNull null
        if (query.isNotBlank() && obj.name?.contains(query, true) == false) return@mapNotNull null
        val index = ids.indexOf(obj.id)
        val isSelected = index != -1
        val number = if (isSelected) index + 1 else Int.MAX_VALUE
        ObjectValueItem.Object(
            view = obj.toView(
                urlBuilder = urlBuilder,
                objectTypes = storeOfObjectTypes.getAll(),
                gradientProvider = gradientProvider
            ),
            isSelected = isSelected,
            number = number
        )
    }.let { mappedOptions ->
        if (!isInitialSortDone) {
            isInitialSortDone = true
            mappedOptions.sortedWith(
                compareBy(
                    { !initialIds.contains(it.view.id) },
                    { it.number })
            )
        } else {
            mappedOptions.sortedWith(
                compareBy(
                    { !initialIds.contains(it.view.id) },
                    { initialIds.indexOf(it.view.id) })
            )
        }
    }

    private fun getRecordValues(record: Map<String, Any?>): List<Id> {
        return when (val value = record[viewModelParams.relationKey]) {
            is Id -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
    }

    //region ACTIONS
    fun onAction(action: ObjectValueItemAction) {
        Timber.d("onAction, action: $action")
        if (!isEditableRelation) {
            Timber.d("ObjectValueViewModel onAction, relation is not editable")
            sendToast("Relation is not editable")
            return
        }
        when (action) {
            ObjectValueItemAction.Clear -> onClearAction()
            is ObjectValueItemAction.Click -> onClickAction(action.item)
            is ObjectValueItemAction.Delete -> onDeleteAction(action.item)
            is ObjectValueItemAction.Duplicate -> onDuplicateAction(action.item)
            is ObjectValueItemAction.Open -> onOpenObjectAction(action.item)
        }
    }

    private fun onDuplicateAction(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            duplicateObject(item.view.id).process(
                success = { Timber.d("Object ${item.view.id} duplicated") },
                failure = { Timber.e(it, "Error while duplicating object") }
            )
        }
    }

    private fun onDeleteAction(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            val isSelected = item.isSelected
            if (isSelected) {
                removeObjectValue(item)  {
                   proceedWithObjectDeletion(item)
                }
            } else {
                proceedWithObjectDeletion(item)
            }
        }
    }

    private suspend fun proceedWithObjectDeletion(item: ObjectValueItem.Object) {
        val params = SetObjectListIsArchived.Params(
            targets = listOf(item.view.id),
            isArchived = true
        )
        objectListIsArchived.async(params).fold(
            onSuccess = { Timber.d("Object ${item.view.id} archived") },
            onFailure = { Timber.e(it, "Error while archiving object") }
        )
    }

    private fun onClearAction() {
        viewModelScope.launch {
            val params = UpdateDetail.Params(
                target = viewModelParams.objectId,
                key = viewModelParams.relationKey,
                value = null
            )
            setObjectDetails(params).process(
                failure = { Timber.e(it, "Error while clearing objects") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                })
        }
    }

    private fun onOpenObjectAction(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            val layout = item.view.layout
            if (layout.isDataView()) {
                commands.emit(Command.OpenSet(item.view.id))
            } else {
                commands.emit(Command.OpenObject(item.view.id))
            }
        }
    }

    private fun onClickAction(item: ObjectValueItem.Object) {
        if (item.isSelected) {
            viewModelScope.launch { removeObjectValue(item) }
        } else {
            addObjectValue(item)
        }
    }

    private fun addObjectValue(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            val obj = values.get(ctx = viewModelParams.ctx, target = viewModelParams.objectId)
            val result = mutableListOf<Id>()
            val value = obj[viewModelParams.relationKey]
            if (value is List<*>) {
                result.addAll(value.typeOf())
            } else if (value is Id) {
                result.add(value)
            }
            result.add(item.view.id)
            setObjectDetails(
                UpdateDetail.Params(
                    target = viewModelParams.objectId,
                    key = viewModelParams.relationKey,
                    value = result
                )
            ).process(
                failure = { Timber.e(it, "Error while adding object") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    private suspend fun removeObjectValue(item: ObjectValueItem.Object, action: suspend () -> Unit = {}) {
        val obj = values.get(ctx = viewModelParams.ctx, target = viewModelParams.objectId)
        val value = obj[viewModelParams.relationKey].filterIdsById(item.view.id)
        setObjectDetails(
            UpdateDetail.Params(
                target = viewModelParams.objectId,
                key = viewModelParams.relationKey,
                value = value
            )
        ).process(
            failure = { Timber.e(it, "Error while removing object ${item.view.id}") },
            success = {
                dispatcher.send(it)
                viewModelScope.sendAnalyticsRelationValueEvent(analytics)
                action()
            }
        )
    }
    //endregion

    data class ViewModelParams(
        val ctx: Id,
        val objectId: Id,
        val relationKey: Key,
        val isLocked: Boolean,
        val relationContext: RelationContext
    )

    sealed class Command {
        object Dismiss : Command()
        object Expand : Command()
        data class OpenObject(val id: Id) : Command()
        data class OpenSet(val id: Id) : Command()
    }
}

sealed class ObjectValueViewState {

    object Loading : ObjectValueViewState()
    data class Empty(val title: String, val isRelationEditable: Boolean) :
        ObjectValueViewState()

    data class Content(
        val title: String,
        val items: List<ObjectValueItem>,
        val isRelationEditable: Boolean,
        val showItemMenu: RelationsListItem? = null
    ) : ObjectValueViewState()
}

sealed class ObjectValueItemAction {
    data class Click(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    data class Delete(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    data class Duplicate(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    data class Open(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    object Clear : ObjectValueItemAction()
}

sealed class ObjectValueItem {
    data class ObjectType(val name: String) : ObjectValueItem()
    data class Object(
        val view: DefaultObjectView,
        val isSelected: Boolean,
        val number: Int = Int.MAX_VALUE
    ) : ObjectValueItem()
}

const val SUB_RELATION_VALUE_OBJECTS = "subscription.values.objects"