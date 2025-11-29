package com.anytypeio.anytype.presentation.relations.value.tagstatus

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationOptions
import com.anytypeio.anytype.domain.relations.SetRelationOptionOrder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationEvent
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.filterIdsById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class TagOrStatusValueViewModel(
    private val viewModelParams: ViewModelParams,
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val subscription: StorelessSubscriptionContainer,
    private val deleteRelationOptions: DeleteRelationOptions,
    private val setRelationOptionOrder: SetRelationOptionOrder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storeOfRelations: StoreOfRelations
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val viewState = MutableStateFlow<TagStatusViewState>(TagStatusViewState.Loading)
    private val query = MutableSharedFlow<String>(replay = 0)
    private var isEditableRelation = false
    val commands = MutableSharedFlow<Command>(replay = 0)

    private val initialIds = mutableListOf<Id>()
    private var isInitialSortDone = false

    init {
        viewModelScope.launch {
            val relation = relations.getOrNull(relation = viewModelParams.relationKey) ?: return@launch
            setupIsRelationNotEditable(relation)
            val searchParams = StoreSearchParams(
                // TODO DROID-2916 Provide space id to vm params
                space = SpaceId(spaceManager.get()),
                subscription = SUB_MY_OPTIONS,
                keys = ObjectSearchConstants.keysRelationOptions,
                filters = ObjectSearchConstants.filterRelationOptions(
                    relationKey = viewModelParams.relationKey
                )
            )
            combine(
                values.subscribe(
                    ctx = viewModelParams.ctx,
                    target = viewModelParams.objectId
                ),
                query.onStart { emit("") },
                subscription.subscribe(searchParams)
            ) { record, query, options ->
                val ids = getRecordValues(record)
                if (!isInitialSortDone) {
                    initialIds.clear()
                    if (ids.isNotEmpty()) {
                        initialIds.addAll(ids)
                    } else {
                        if (isEditableRelation) {
                            emitCommand(Command.Expand)
                        }
                    }
                }
                initViewState(
                    relation = relation,
                    options = filterOptions(query, options, ids),
                    query = query,
                    ids = ids
                )
            }
                .catch {
                    Timber.e(it, "TagOrStatusValue, Error while combining flows")
                }
                .collect()
        }
    }

    private fun filterOptions(
        query: String,
        options: List<ObjectWrapper.Basic>,
        ids: List<Id>
    ): List<ObjectWrapper.Option> {
        return if (isEditableRelation) {
            options.map { ObjectWrapper.Option(map = it.map) }
                .filter { it.name?.contains(query, true) == true }
        } else {
            options.map { ObjectWrapper.Option(map = it.map) }
                .filter { ids.contains(it.id) }
        }
    }

    fun onQueryChanged(input: String) {
        viewModelScope.launch {
            query.emit(input)
        }
    }

    fun proceedWithDeleteOptions(optionId: Id) {
        viewModelScope.launch {
            val params = DeleteRelationOptions.Params(listOf(optionId))
            deleteRelationOptions.execute(params).fold(
                onSuccess = {
                    Timber.d("Options deleted successfully")
                    removeTag(optionId)
                },
                onFailure = { Timber.e(it, "Error while deleting options") }
            )
        }
    }

    fun onAction(action: TagStatusAction) {
        Timber.d("TagStatusViewModel onAction, action: $action")
        if (!isEditableRelation) {
            Timber.d("TagStatusViewModel onAction, relation is not editable")
            sendToast("Relation is not editable")
            return
        }
        when (action) {
            TagStatusAction.Clear -> clearTagsOrStatus()
            is TagStatusAction.Click -> onActionClick(action.item)
            is TagStatusAction.LongClick -> {
                val currentState = viewState.value
                if (currentState is TagStatusViewState.Content) {
                    viewState.value = currentState.copy(showItemMenu = action.item)
                }
            }

            TagStatusAction.Plus -> openOptionScreen(
                Command.OpenOptionScreen(
                    color = ThemeColor.values().drop(1).random().code,
                    relationKey = viewModelParams.relationKey,
                    ctx = viewModelParams.ctx,
                    objectId = viewModelParams.objectId
                )
            )
            is TagStatusAction.Delete -> {
                emitCommand(Command.DeleteOption(action.optionId))
            }
            is TagStatusAction.Duplicate -> {
                val item = action.item
                openOptionScreen(
                    Command.OpenOptionScreen(
                        color = item.color.code,
                        text = item.name,
                        relationKey = viewModelParams.relationKey,
                        ctx = viewModelParams.ctx,
                        objectId = viewModelParams.objectId
                    )
                )
            }
            is TagStatusAction.Edit -> {
                val item = action.item
                openOptionScreen(
                    Command.OpenOptionScreen(
                        optionId = item.optionId,
                        color = item.color.code,
                        text = item.name,
                        relationKey = viewModelParams.relationKey,
                        ctx = viewModelParams.ctx,
                        objectId = viewModelParams.objectId
                    )
                )
            }
            TagStatusAction.Create -> {
                openOptionScreen(
                    Command.OpenOptionScreen(
                        text = "",
                        relationKey = viewModelParams.relationKey,
                        ctx = viewModelParams.ctx,
                        objectId = viewModelParams.objectId
                    )
                )
            }
            is TagStatusAction.OnMove -> {
                Timber.d("OnMove from ${action.from} to ${action.to}")
                viewModelScope.launch {
                    val currentState = viewState.value
                    if (currentState !is TagStatusViewState.Content) return@launch
                    val reorderedIds = currentState.items
                        .filterIsInstance<RelationsListItem.Item>()
                        .toMutableList()
                        .apply { add(action.to, removeAt(action.from)) }
                        .map { it.optionId }
                    setRelationOptionOrder.async(
                        SetRelationOptionOrder.Params(
                            spaceId = viewModelParams.space,
                            relationKey = RelationKey(viewModelParams.relationKey),
                            orderedIds = reorderedIds
                        )
                    ).fold(
                        onSuccess = {
                            Timber.d("Option order saved successfully")
                        },
                        onFailure = { e ->
                            Timber.e(e, "Failed to save option order")
                            sendToast("Failed to save order")
                        }
                    )
                }
            }
        }
    }

    private fun emitCommand(command: Command, delay: Long = 0L) {
        viewModelScope.launch {
            delay(delay)
            commands.emit(command)
        }
    }

    private fun openOptionScreen(command: Command.OpenOptionScreen) {
        viewModelScope.launch {
            query.emit("")
            commands.emit(command)
        }
    }

    private fun onActionClick(item: RelationsListItem.Item) {
        when (item) {
            is RelationsListItem.Item.Status -> {
                if (item.isSelected) {
                    clearTagsOrStatus()
                } else {
                    addStatus(item.optionId)
                }
            }
            is RelationsListItem.Item.Tag -> {
                if (item.isSelected) {
                    removeTag(item.optionId)
                } else {
                    addTag(item.optionId)
                }
            }
        }
    }

    private fun initViewState(
        relation: ObjectWrapper.Relation,
        ids: List<Id>,
        options: List<ObjectWrapper.Option>,
        query: String
    ) {
        val result = mutableListOf<RelationsListItem>()
        when (relation.format) {
            Relation.Format.STATUS -> {
                result.addAll(
                    mapStatusOptions(
                        ids = ids,
                        options = options
                    )
                )
            }
            Relation.Format.TAG -> {
                result.addAll(
                    mapTagOptions(
                        ids = ids,
                        options = options
                    )
                )
            }
            else -> {
                Timber.w("Relation format should be Tag or Status but was: ${relation.format}")
            }
        }

        viewState.value = if (result.isEmpty()) {
            TagStatusViewState.Empty(
                isRelationEditable = isEditableRelation,
                title = relation.name.orEmpty(),
            )
        } else {
            TagStatusViewState.Content(
                isRelationEditable = isEditableRelation,
                title = relation.name.orEmpty(),
                items = result
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

    private fun addTag(tag: Id) {
        viewModelScope.launch {
            val obj = values.get(ctx = viewModelParams.ctx, target = viewModelParams.objectId)
            val result = mutableListOf<Id>()
            val value = obj[viewModelParams.relationKey]
            if (value is List<*>) {
                result.addAll(value.typeOf())
            } else if (value is Id) {
                result.add(value)
            }
            result.add(tag)
            setObjectDetails(
                UpdateDetail.Params(
                    target = viewModelParams.objectId,
                    key = viewModelParams.relationKey,
                    value = result
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
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

    private fun removeTag(tag: Id) {
        viewModelScope.launch {
            val obj = values.get(ctx = viewModelParams.ctx, target = viewModelParams.objectId)
            val remaining = obj[viewModelParams.relationKey].filterIdsById(tag)
            setObjectDetails(
                UpdateDetail.Params(
                    target = viewModelParams.objectId,
                    key = viewModelParams.relationKey,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = if (remaining.isEmpty()) EventsDictionary.relationDeleteValue
                        else EventsDictionary.relationChangeValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = viewModelParams.relationKey,
                        spaceParams = provideParams(spaceManager.get())
                    )
                })
        }
    }

    private fun addStatus(status: Id) {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = viewModelParams.objectId,
                    key = viewModelParams.relationKey,
                    value = listOf(status)
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = EventsDictionary.relationChangeValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = viewModelParams.relationKey,
                        spaceParams = provideParams(spaceManager.get())
                    )
                    emitCommand(command = Command.Dismiss, delay = DELAY_UNTIL_CLOSE)
                }
            )
        }
    }

    private fun clearTagsOrStatus() {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = viewModelParams.objectId,
                    key = viewModelParams.relationKey,
                    value = null
                )
            ).process(
                failure = { Timber.e(it, "Error while clearing tags or select") },
                success = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = EventsDictionary.relationDeleteValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = viewModelParams.relationKey,
                        spaceParams = provideParams(spaceManager.get())
                    )
                }
            )
        }
    }

    private fun mapTagOptions(
        ids: List<Id>,
        options: List<ObjectWrapper.Option>
    ) = options.map { option ->
        val index = ids.indexOf(option.id)
        val isSelected = index != -1
        val number = if (isSelected) index + 1 else Int.MAX_VALUE
        RelationsListItem.Item.Tag(
            optionId = option.id,
            name = option.name.orEmpty(),
            color = getOrDefault(option.color),
            isSelected = isSelected,
            number = number
        )
    }.let { mappedOptions ->
        if (!isInitialSortDone) {
            isInitialSortDone = true
            mappedOptions.sortedWith(
                compareBy(
                    { !initialIds.contains(it.optionId) },
                    { it.number })
            )
        } else {
            mappedOptions.sortedWith(
                compareBy(
                    { !initialIds.contains(it.optionId) },
                    { initialIds.indexOf(it.optionId) })
            )
        }
    }

    private fun mapStatusOptions(
        ids: List<Id>,
        options: List<ObjectWrapper.Option>
    ) = options.map { option ->
        val index = ids.indexOf(option.id)
        val isSelected = index != -1
        isInitialSortDone = true
        RelationsListItem.Item.Status(
            optionId = option.id,
            name = option.name.orEmpty(),
            color = getOrDefault(option.color),
            isSelected = isSelected
        )
    }

    private fun getOrDefault(code: String?): ThemeColor {
        return ThemeColor.values().find { it.code == code } ?: ThemeColor.DEFAULT
    }

    private fun setupIsRelationNotEditable(relation: ObjectWrapper.Relation) {
        isEditableRelation = !(viewModelParams.isLocked
                || relation.isReadonlyValue
                || relation.isHidden == true
                || relation.isDeleted == true
                || relation.isArchived == true
                || !relation.isValid)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            subscription.unsubscribe(listOf(SUB_MY_OPTIONS))
        }
    }

    data class ViewModelParams(
        val ctx: Id,
        val space: SpaceId,
        val objectId: Id,
        val relationKey: Key,
        val isLocked: Boolean,
        val relationContext: RelationContext
    )
}

sealed class Command {
    data class OpenOptionScreen(
        val ctx: Id,
        val objectId: Id,
        val relationKey: Key,
        val optionId: Id? = null,
        val color: String? = null,
        val text: String? = null,
    ) : Command()

    data class DeleteOption(val optionId: Id) : Command()

    object Dismiss : Command()

    object Expand : Command()
}

sealed class TagStatusViewState {

    object Loading : TagStatusViewState()
    data class Empty(val title: String, val isRelationEditable: Boolean) :
        TagStatusViewState()

    data class Content(
        val title: String,
        val items: List<RelationsListItem>,
        val isRelationEditable: Boolean,
        val showItemMenu: RelationsListItem.Item? = null
    ) : TagStatusViewState()
}

sealed class TagStatusAction {
    data class Click(val item: RelationsListItem.Item) : TagStatusAction()
    data class LongClick(val item: RelationsListItem.Item) : TagStatusAction()
    object Clear : TagStatusAction()
    object Plus : TagStatusAction()
    data class Edit(val item: RelationsListItem.Item) : TagStatusAction()
    data class Delete(val optionId: Id) : TagStatusAction()
    data class Duplicate(val item: RelationsListItem.Item) : TagStatusAction()
    object Create : TagStatusAction()
    data class OnMove(val from: Int, val to: Int) : TagStatusAction()
}

enum class RelationContext { OBJECT, OBJECT_SET, DATA_VIEW }

sealed class RelationsListItem {

    sealed class Item : RelationsListItem() {

        abstract val optionId: Id
        abstract val name: String
        abstract val color: ThemeColor
        abstract val isSelected: Boolean

        data class Tag(
            override val optionId: Id,
            override val name: String,
            override val color: ThemeColor,
            override val isSelected: Boolean,
            val number: Int = Int.MAX_VALUE,
            val showMenu: Boolean = false
        ) : Item()

        data class Status(
            override val optionId: Id,
            override val name: String,
            override val color: ThemeColor,
            override val isSelected: Boolean
        ) : Item()
    }
}

const val SUB_MY_OPTIONS = "subscription.relation-options"
const val DELAY_UNTIL_CLOSE = 300L
