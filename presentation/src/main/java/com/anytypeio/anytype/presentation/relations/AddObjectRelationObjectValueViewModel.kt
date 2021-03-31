package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectRelationValueViewModel.ObjectRelationValueView
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class AddObjectRelationObjectValueViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    private val _views = MutableStateFlow<List<ObjectRelationValueView.Object>>(listOf())
    private val _filter = MutableStateFlow("")
    private var _selected = MutableStateFlow<List<Id>>(listOf())
    private val _viewsFiltered = MutableStateFlow(AddObjectValueView())

    val commands = MutableSharedFlow<AddObjectValueCommand>(0)
    val viewsFiltered: StateFlow<AddObjectValueView> = _viewsFiltered

    fun onStart(relationId: Id, objectId: String) {
        processingViewsSelectionsAndFilter()
        val relation = relations.get(relationId)
        val values = values.get(objectId)
        when (val ids = values[relation.key]) {
            is List<*> -> {
                proceedWithSearchObjects(
                    ids = ids.typeOf(),
                    relation = relation
                )
            }
            is Id -> {
                proceedWithSearchObjects(
                    ids = listOf(ids),
                    relation = relation
                )
            }
            null -> {
                proceedWithSearchObjects(
                    ids = emptyList(),
                    relation = relation
                )
            }
        }
    }

    private fun processingViewsSelectionsAndFilter() {
        viewModelScope.launch {
            combine(_views, _selected, _filter) { objects, selections, query ->
                objects
                    .map { obj ->
                        val index = selections.indexOf(obj.id)
                        if (index != -1) {
                            obj.copy(
                                isSelected = true,
                                selectedNumber = (index + 1).toString()
                            )
                        } else {
                            obj.copy(
                                isSelected = false,
                                selectedNumber = null
                            )
                        }
                    }
                    .filter { obj ->
                        obj.name.contains(query, true)
                    }
            }.collect { objects ->
                val size = objects.filter { it.isSelected == true }.size
                _viewsFiltered.value = AddObjectValueView(
                    objects = objects,
                    count = size.toString()
                )
            }
        }
    }

    fun onActionButtonClicked() {
        viewModelScope.launch {
            commands.emit(AddObjectValueCommand.DispatchResult(ids = _selected.value))
        }
    }

    fun onFilterTextChanged(filter: String) {
        _filter.value = filter
        _selected.value = listOf()
    }

    fun onObjectClicked(objectId: Id) {
        val selected = _selected.value.toMutableList()
        val index = selected.indexOf(objectId)
        if (index != -1) {
            selected.removeAt(index)
            _selected.value = selected

        } else {
            selected.add(objectId)
            _selected.value = selected
        }
    }

    private fun proceedWithSearchObjects(ids: List<String>, relation: Relation) {
        val filters = arrayListOf(
            DVFilter(
                relationKey = ObjectSetConfig.TYPE_KEY,
                operator = DVFilterOperator.AND,
                condition = DVFilterCondition.IN,
                value = relation.objectTypes
            )
        )
        val sorts = arrayListOf(
            DVSort(
                relationKey = ObjectSetConfig.NAME_KEY,
                type = Block.Content.DataView.Sort.Type.ASC
            )
        )
        viewModelScope.launch {
            searchObjects(
                SearchObjects.Params(
                    sorts = sorts,
                    filters = filters,
                    fulltext = SearchObjects.EMPTY_TEXT,
                    offset = SearchObjects.INIT_OFFSET,
                    limit = SearchObjects.LIMIT
                )
            ).process(
                failure = { Timber.e(it, "Error while getting objects") },
                success = {
                    _views.value = it.mapNotNull { record ->
                        val id = record[ObjectSetConfig.ID_KEY] as String
                        val type = record[ObjectSetConfig.TYPE_KEY] as? String
                        val name = record[ObjectSetConfig.NAME_KEY] as String?
                        val emoji = record[ObjectSetConfig.EMOJI_KEY] as String?
                        val image = record[ObjectSetConfig.IMAGE_KEY] as String?
                        val layout = record[ObjectSetConfig.LAYOUT_KEY] as? ObjectType.Layout
                        if (id !in ids) {
                            ObjectRelationValueView.Object(
                                id = id,
                                type = type?.substringAfterLast(
                                    delimiter = "/",
                                    missingDelimiterValue = ""
                                ) ?: "",
                                name = name.orEmpty(),
                                image = if (image.isNullOrBlank()) null else urlBuilder.thumbnail(
                                    image
                                ),
                                emoji = emoji,
                                isSelected = false,
                                layout = layout,
                                removeable = false
                            )
                        } else null
                    }
                }
            )
        }
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddObjectRelationObjectValueViewModel(
                relations = relations,
                values = values,
                searchObjects = searchObjects,
                urlBuilder = urlBuilder
            ) as T
        }
    }
}

data class AddObjectValueView(
    val objects: List<ObjectRelationValueView.Object> = emptyList(),
    val count: String? = null
)

sealed class AddObjectValueCommand {
    data class DispatchResult(val ids: List<Id>) : AddObjectValueCommand()
}