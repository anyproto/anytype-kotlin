package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationObjectValueAddViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val objectTypesProvider: ObjectTypesProvider
) : ViewModel() {

    private val _views = MutableStateFlow<List<RelationValueView.Object>>(listOf())
    private val _filter = MutableStateFlow("")
    private var _selected = MutableStateFlow<List<Id>>(listOf())
    private val _viewsFiltered = MutableStateFlow(ObjectValueAddView())

    val commands = MutableSharedFlow<ObjectValueAddCommand>(0)
    val viewsFiltered: StateFlow<ObjectValueAddView> = _viewsFiltered

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
                _viewsFiltered.value = ObjectValueAddView(
                    objects = objects,
                    count = size.toString()
                )
            }
        }
    }

    fun onActionButtonClicked() {
        viewModelScope.launch {
            commands.emit(ObjectValueAddCommand.DispatchResult(ids = _selected.value))
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

        val filters = ObjectSearchConstants.filterAddObjectToRelation(relation.objectTypes)
        val sorts = ObjectSearchConstants.sortAddObjectToRelation
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
                success = { objects ->
                    _views.value = objects.mapNotNull { obj ->
                        val wrapped = ObjectWrapper.Basic(obj)
                        val id = wrapped.id
                        val type = wrapped.type
                        val targetType = objectTypesProvider.get().find { it.url == type.firstOrNull() }
                        val name = wrapped.name
                        val layout = wrapped.layout
                        if (id !in ids) {
                            RelationValueView.Object(
                                id = id,
                                typeName = targetType?.name,
                                type = type.firstOrNull(),
                                name = name.orEmpty(),
                                icon = ObjectIcon.from(
                                    obj = wrapped,
                                    layout = wrapped.layout,
                                    builder = urlBuilder
                                ),
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
        private val urlBuilder: UrlBuilder,
        private val objectTypesProvider: ObjectTypesProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationObjectValueAddViewModel(
                relations = relations,
                values = values,
                searchObjects = searchObjects,
                urlBuilder = urlBuilder,
                objectTypesProvider = objectTypesProvider
            ) as T
        }
    }
}

data class ObjectValueAddView(
    val objects: List<RelationValueView.Object> = emptyList(),
    val count: String? = null
)

sealed class ObjectValueAddCommand {
    data class DispatchResult(val ids: List<Id>) : ObjectValueAddCommand()
}