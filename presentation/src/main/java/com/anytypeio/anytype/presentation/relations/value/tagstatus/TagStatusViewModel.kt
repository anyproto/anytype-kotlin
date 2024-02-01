package com.anytypeio.anytype.presentation.relations.value.tagstatus

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.filterIdsById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class TagStatusViewModel(
    private val params: Params,
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val storage: Editor.Storage,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val getOptions: GetOptions,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val viewState = MutableStateFlow<TagStatusViewState>(TagStatusViewState.Loading)
    private val query = MutableSharedFlow<String>()
    private var isRelationNotEditable = false

    fun onStart() {
        val obj = storage.details.current().details[params.objectId]
        isRelationNotEditable = params.isLocked || storage.objectRestrictions.current()
            .contains(ObjectRestriction.RELATIONS)
        Timber.d("TagStatusViewModel onStart, params: $params, obj: $obj, isRelationNotEditable: $isRelationNotEditable")
        viewModelScope.launch {
            combine(
                relations.observe(
                    relation = params.relationKey
                ),
                values.subscribe(
                    ctx = params.ctx,
                    target = params.objectId
                ),
                query.onStart { emit("") }
            ) { relation, record, query ->
                setupIsRelationNotEditable(relation)
                getAllOptions(
                    relation = relation,
                    record = record,
                    query = query
                )
            }.collect()
        }
    }

    fun onQueryChanged(input: String) {
        viewModelScope.launch {
            query.emit(input)
        }
    }

    fun onAction(action: TagStatusAction) {
        Timber.d("TagStatusViewModel onAction, action: $action")
        if (isRelationNotEditable) {
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
            TagStatusAction.Plus -> TODO()
            is TagStatusAction.Delete -> TODO()
            is TagStatusAction.Duplicate -> TODO()
            is TagStatusAction.Edit -> TODO()
        }
    }

    private fun onActionClick(item: RelationsListItem) {
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
            is RelationsListItem.CreateItem.Status -> TODO()
            is RelationsListItem.CreateItem.Tag -> TODO()
        }
    }

    private suspend fun getAllOptions(
        relation: ObjectWrapper.Relation,
        record: Struct,
        query: String
    ) {
        val params = GetOptions.Params(
            space = spaceManager.get(),
            relation = relation.key,
            fulltext = query
        )
        getOptions(params).proceed(
            success = { options ->
                Timber.d("TagStatusViewModel getAllOptions, options: ${options.size}")
                initViewState(
                    relation = relation,
                    record = record,
                    options = options,
                    query = query
                )
            },
            failure = {
                Timber.e(it, "Error while getting options by id")
            }
        )
    }

    private fun initViewState(
        relation: ObjectWrapper.Relation,
        record: Map<String, Any?>,
        options: List<ObjectWrapper.Option>,
        query: String
    ) {
        val result = mutableListOf<RelationsListItem>()
        when (relation.format) {
            Relation.Format.STATUS -> {
                val ids: List<Id> = when (val value = record[params.relationKey]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                result.addAll(
                    mapStatusOptions(
                        ids = ids,
                        options = options
                    )
                )
            }
            Relation.Format.TAG -> {
                val ids: List<Id> = when (val value = record[params.relationKey]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                result.addAll(
                    mapTagOptions(
                        ids = ids,
                        options = options
                    )
                )
                if (query.isNotBlank()) {
                    result.add(RelationsListItem.CreateItem.Tag(query))
                }
            }
            else -> {
                Timber.w("Relation format should be Tag or Status but was: ${relation.format}")
            }
        }

        viewState.value = if (result.isEmpty()) {
            TagStatusViewState.Empty(
                isRelationEditable = !isRelationNotEditable,
                title = relation.name.orEmpty(),
            )
        } else {
            TagStatusViewState.Content(
                isRelationEditable = !isRelationNotEditable,
                title = relation.name.orEmpty(),
                items = result
            )
        }
    }

    private fun addTag(tag: Id) {
        viewModelScope.launch {
            val obj = values.get(ctx = params.ctx, target = params.objectId)
            val result = mutableListOf<Id>()
            val value = obj[params.relationKey]
            if (value is List<*>) {
                result.addAll(value.typeOf())
            } else if (value is Id) {
                result.add(value)
            }
            result.add(tag)
            setObjectDetails(
                UpdateDetail.Params(
                    target = params.objectId,
                    key = params.relationKey,
                    value = result
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    private fun removeTag(tag: Id) {
        viewModelScope.launch {
            val obj = values.get(ctx = params.ctx, target = params.objectId)
            val remaining = obj[params.relationKey].filterIdsById(tag)
            setObjectDetails(
                UpdateDetail.Params(
                    target = params.objectId,
                    key = params.relationKey,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                })
        }
    }

    private fun addStatus(status: Id) {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = params.objectId,
                    key = params.relationKey,
                    value = listOf(status)
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    private fun clearTagsOrStatus() {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = params.objectId,
                    key = params.relationKey,
                    value = null
                )
            ).process(
                failure = { Timber.e(it, "Error while clearing tags or select") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                })
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
    }.sortedBy { it.number }

    private fun mapStatusOptions(
        ids: List<Id>,
        options: List<ObjectWrapper.Option>
    ) = options.map { option ->
        val index = ids.indexOf(option.id)
        val isSelected = index != -1
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
        isRelationNotEditable = params.isLocked
                || storage.objectRestrictions.current().contains(ObjectRestriction.RELATIONS)
                || relation.isReadonlyValue
                || relation.isHidden == true
                || relation.isDeleted == true
                || relation.isArchived == true
                || !relation.isValid
    }

    data class Params(
        val ctx: Id,
        val objectId: Id,
        val relationKey: Id,
        val isLocked: Boolean,
        val relationContext: RelationContext
    )
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
    data class Click(val item: RelationsListItem) : TagStatusAction()
    data class LongClick(val item: RelationsListItem.Item) : TagStatusAction()
    object Clear : TagStatusAction()
    object Plus : TagStatusAction()
    data class Edit(val optionId: Id) : TagStatusAction()
    data class Delete(val optionId: Id) : TagStatusAction()
    data class Duplicate(val optionId: Id) : TagStatusAction()
}

sealed class OptionWidgetViewState {
    data class Edit(val optionId: Id, val text: String, val color: ThemeColor) :
        OptionWidgetViewState()

    data class Create(val optionId: Id, val text: String, val color: ThemeColor) :
        OptionWidgetViewState()
}

sealed class OptionWidgetAction {
    data class Apply(val optionId: Id, val text: String, val color: ThemeColor) :
        OptionWidgetAction()

    data class Create(val optionId: Id, val text: String, val color: ThemeColor) :
        OptionWidgetAction()
}

enum class RelationContext{ OBJECT, OBJECT_SET, DATA_VIEW }

sealed class RelationsListItem {

    sealed class Item : RelationsListItem() {

        abstract val optionId: Id
        data class Tag(
            override val optionId: Id,
            val name: String,
            val color: ThemeColor,
            val isSelected: Boolean,
            val number: Int = Int.MAX_VALUE,
            val showMenu: Boolean = false
        ) : Item()

        data class Status(
            override val optionId: Id,
            val name: String,
            val color: ThemeColor,
            val isSelected: Boolean
        ) : Item()
    }

    sealed class CreateItem(
        val text: String
    ) : RelationsListItem() {
        class Tag(text: String) : CreateItem(text)
        class Status(text: String) : CreateItem(text)
    }
}