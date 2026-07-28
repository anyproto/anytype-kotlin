package com.anytypeio.anytype.presentation.relations.value.`object`

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation.Format.FILE
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.mapLimitObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationEvent
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.toView
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.filterIdsById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectValueViewModel(
    private val viewModelParams: ViewModelParams,
    private val values: ObjectValueProvider,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val objectSearch: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val objectListIsArchived: SetObjectListIsArchived,
    private val duplicateObject: DuplicateObject,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storeOfRelations: StoreOfRelations,
    private val fieldParser: FieldParser,
    private val spaceViews: SpaceViewSubscriptionContainer
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val viewState = MutableStateFlow<ObjectValueViewState>(ObjectValueViewState.Loading())
    private val query = MutableSharedFlow<String>(replay = 1)
    private var isEditableRelation = false
    /**
     * One-shot commands. Backed by an unbounded [Channel] (not a `replay = 0`
     * [MutableSharedFlow]) so the `Command.Expand` emitted from the `init` pipeline
     * on first load — before the bottom sheet has attached its collector — is buffered
     * and delivered to the next subscriber instead of being silently dropped, which
     * left the sheet stuck unexpanded (DROID-4523). The single collecting fragment
     * makes [receiveAsFlow]'s single-consumer semantics correct.
     */
    private val commandsChannel = Channel<Command>(Channel.UNLIMITED)
    val commands: Flow<Command> = commandsChannel.receiveAsFlow()

    override fun onCleared() {
        // viewModelScope is already cancelled by the time onCleared() runs, so no
        // send() can race this close(). Closing is explicit about intent: nothing
        // will consume the channel once the ViewModel is gone.
        commandsChannel.close()
        super.onCleared()
    }

    private val initialIds = mutableListOf<Id>()
    private var isInitialSortDone = false

    val navigation = MutableSharedFlow<OpenObjectNavigation>()

    init {
        Timber.d("ObjectValueViewModel init, params: $viewModelParams")
        viewModelScope.launch {
            val relation = storeOfRelations.getByKey(viewModelParams.relationKey) ?: return@launch
            setupIsRelationNotEditable(relation)
            combine(
                values.subscribe(
                    ctx = viewModelParams.ctx,
                    target = viewModelParams.objectId
                ),
                query.onStart { emit("") },
            ) { record, query ->
                val ids = getRecordValues(record)
                if (!isInitialSortDone) {
                    initialIds.clear()
                    if (ids.isNotEmpty()) {
                        initialIds.addAll(ids)
                    } else {
                        emitCommand(Command.Expand)
                    }
                }
                // Resolved once per emission and reused for both the search filter and the
                // header/empty-state labels, so the query and the UI can never disagree about
                // which types this property actually accepts.
                val limitObjectTypes = storeOfObjectTypes.mapLimitObjectTypes(relation)
                Triple(
                    ids, limitObjectTypes, getSearchParams(
                        relation = relation,
                        query = query,
                        ids = ids,
                        limitObjectTypes = limitObjectTypes
                    )
                )
            }.onEach { (ids, limitObjectTypes, searchParams) ->
                objectSearch(params = searchParams).proceed(
                    success = { objects ->
                        initViewState(
                            relation = relation,
                            ids = ids,
                            objects = objects,
                            limitObjectTypes = limitObjectTypes
                        )
                    },
                    failure = { Timber.e(it, "Error while searching objects") }
                )
            }.collect()
        }
    }

    private fun getSearchParams(
        relation: ObjectWrapper.Relation,
        query: String,
        ids: List<Id>,
        limitObjectTypes: List<Id>
    ): SearchObjects.Params {
        val isFileRelation = relation.format == FILE
        val searchKeys =
            if (isFileRelation) ObjectSearchConstants.defaultFilesKeys else ObjectSearchConstants.defaultKeys
        val searchFilters = when {
            isFileRelation -> {
                if (isEditableRelation) {
                    ObjectSearchConstants.filesFilters(space = viewModelParams.space.id)
                } else {
                    ObjectSearchConstants.filterObjectsByIds(
                        space = viewModelParams.space.id,
                        ids = ids
                    )
                }
            }
            else -> {
                if (isEditableRelation) {
                    val isOneToOneSpace = spaceViews.get(viewModelParams.space)?.isOneToOneSpace == true
                    // Deliberately the resolved list, not relation.relationFormatObjectTypes:
                    // ids that no longer exist as types in this space (deleted/recreated types,
                    // ids carried in from another space) would otherwise produce
                    // `type IN [<dead ids>]`, which matches nothing while every other screen
                    // reports the property as unrestricted (DROID-4554).
                    ObjectSearchConstants.filterAddObjectToRelation(
                        space = viewModelParams.space.id,
                        targetTypes = limitObjectTypes,
                        isOneToOneSpace = isOneToOneSpace
                    )
                } else {
                    ObjectSearchConstants.filterObjectsByIds(
                        space = viewModelParams.space.id,
                        ids = ids
                    )
                }
            }
        }
        return SearchObjects.Params(
            space = viewModelParams.space,
            keys = searchKeys,
            filters = searchFilters,
            fulltext = if (isEditableRelation) query else SearchObjects.EMPTY_TEXT
        )
    }

    private fun emitCommand(command: Command, delay: Long = 0L) {
        viewModelScope.launch {
            delay(delay)
            commandsChannel.send(command)
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
        limitObjectTypes: List<Id>,
        query: String = ""
    ) {
        val views = mapObjects(ids, objects, query, fieldParser, storeOfObjectTypes)
        val objectTypeNames = getFormatObjectTypeNames(relation, limitObjectTypes)
        viewState.value = if (views.isNotEmpty()) {
            ObjectValueViewState.Content(
                isEditableRelation = isEditableRelation,
                title = relation.name.orEmpty(),
                items = buildList {
                    if (isEditableRelation) add(ObjectValueItem.ObjectType(name = objectTypeNames))
                    addAll(views)
                }
            )
        } else {
            ObjectValueViewState.Empty(
                isEditableRelation = isEditableRelation,
                title = relation.name.orEmpty(),
                // Non-null only when the property genuinely restricts to types that still
                // exist, so the empty screen can say why nothing is listed instead of the
                // bare "Objects not found".
                limitedToTypeNames = objectTypeNames.takeIf { it.isNotBlank() }
            )
        }
    }

    private suspend fun getFormatObjectTypeNames(
        relation: ObjectWrapper.Relation,
        limitObjectTypes: List<Id>
    ): String {
        val objectTypeKeys =
            if (relation.format == FILE) {
                ObjectTypeIds.getFileTypes().mapNotNull { key ->
                    storeOfObjectTypes.getByKey(key)?.name?.takeIf { it.isNotBlank() }
                }
            } else {
                limitObjectTypes.mapNotNull { id ->
                    storeOfObjectTypes.get(id)?.name?.takeIf { it.isNotBlank() }
                }
            }
        return objectTypeKeys.joinToString(", ")
    }

    private suspend fun mapObjects(
        ids: List<Id>,
        objects: List<ObjectWrapper.Basic>,
        query: String,
        fieldParser: FieldParser,
        storeOfObjectTypes : StoreOfObjectTypes
    ): List<ObjectValueItem.Object> = objects.mapNotNull { obj ->
        if (!obj.isValid) return@mapNotNull null
        if (query.isNotBlank() && obj.name?.contains(query, true) == false) return@mapNotNull null
        val index = ids.indexOf(obj.id)
        val isSelected = index != -1
        val number = if (isSelected) index + 1 else Int.MAX_VALUE
        ObjectValueItem.Object(
            view = obj.toView(
                urlBuilder = urlBuilder,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes,
                usePluralNames = false
            ),
            isSelected = isSelected,
            number = number,
            restrictions = obj.restrictions,
            obj = obj
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

    private fun refreshObjects() {
        val currentQuery = query.replayCache.lastOrNull().orEmpty()
        onQueryChanged(currentQuery)
    }

    //region ACTIONS
    fun onAction(action: ObjectValueItemAction) {
        Timber.d("onAction, action: $action")
        if (!isEditableRelation && action is ObjectValueItemAction.Click) {
            onOpenObjectAction(action.item)
            return
        }
        if (!isEditableRelation && action !is ObjectValueItemAction.Open) {
            Timber.d("ObjectValueViewModel onAction, relation is not editable")
            sendToast("Relation is not editable")
            return
        }
        when (action) {
            ObjectValueItemAction.Clear -> onClearAction()
            is ObjectValueItemAction.Click -> onClickAction(action.item)
            is ObjectValueItemAction.Delete -> emitCommand(Command.DeleteObject(action.item.view.id))
            is ObjectValueItemAction.Duplicate -> onDuplicateAction(action.item)
            is ObjectValueItemAction.Open -> onOpenObjectAction(action.item)
        }
    }

    private fun onDuplicateAction(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            duplicateObject(item.view.id).process(
                success = {
                    Timber.d("Object ${item.view.id} duplicated")
                    refreshObjects()
                },
                failure = { Timber.e(it, "Error while duplicating object") }
            )
        }
    }

    fun onDeleteAction(objectId: Id) {
        val state = viewState.value as? ObjectValueViewState.Content ?: return
        val item = state.items.filterIsInstance<ObjectValueItem.Object>()
            .firstOrNull { it.view.id == objectId } ?: return
        viewModelScope.launch {
            val isSelected = item.isSelected
            if (isSelected) {
                removeObjectValue(item) {
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
            onSuccess = {
                Timber.d("Object ${item.view.id} archived")
                refreshObjects()
            },
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
                    analytics.sendAnalyticsRelationEvent(
                        eventName = EventsDictionary.relationDeleteValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = viewModelParams.relationKey,
                        spaceParams = provideParams(spaceManager.get())
                    )
                })
        }
    }

    private fun onOpenObjectAction(item: ObjectValueItem.Object) {
        val nav = item.obj.navigation()
        viewModelScope.launch {
            navigation.emit(nav)
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
                    analytics.sendAnalyticsRelationEvent(
                        eventName = if (result.isEmpty()) EventsDictionary.relationDeleteValue
                        else EventsDictionary.relationChangeValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = viewModelParams.relationKey,
                        spaceParams = provideParams(spaceManager.get())
                    )
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
                analytics.sendAnalyticsRelationEvent(
                    eventName = if (value.isEmpty()) EventsDictionary.relationDeleteValue
                    else EventsDictionary.relationChangeValue,
                    storeOfRelations = storeOfRelations,
                    relationKey = viewModelParams.relationKey,
                    spaceParams = provideParams(spaceManager.get())
                )
                action()
            }
        )
    }
    //endregion

    data class ViewModelParams(
        val ctx: Id,
        val space: SpaceId,
        val objectId: Id,
        val relationKey: Key,
        val isLocked: Boolean,
        val relationContext: RelationContext
    )

    sealed class Command {
        object Dismiss : Command()
        object Expand : Command()
        data class DeleteObject(val id: Id) : Command()
    }
}

sealed class ObjectValueViewState {
    abstract val isEditableRelation: Boolean

    data class Loading(
        override val isEditableRelation: Boolean = false
    ) : ObjectValueViewState()

    data class Empty(
        val title: String,
        override val isEditableRelation: Boolean,
        /**
         * Comma-separated names of the object types this property is limited to, or null when
         * the property accepts anything. Drives the explanatory empty state (DROID-4554).
         */
        val limitedToTypeNames: String? = null
    ) : ObjectValueViewState()

    data class Content(
        val title: String,
        override val isEditableRelation: Boolean,
        val items: List<ObjectValueItem>,
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
        val number: Int = Int.MAX_VALUE,
        val restrictions: List<ObjectRestriction> = emptyList(),
        val obj: ObjectWrapper.Basic
    ) : ObjectValueItem()
}