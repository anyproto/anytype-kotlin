package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.objects.toRelationObjectValueView
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class AddObjectRelationViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModel() {

    private val _views = MutableStateFlow<List<RelationValueView.Object>>(listOf())
    private val _filter = MutableStateFlow("")
    private var _selected = MutableStateFlow<List<Id>>(listOf())
    private val _viewsFiltered = MutableStateFlow(ObjectValueAddView())
    private val jobs = mutableListOf<Job>()

    val commands = MutableSharedFlow<ObjectValueAddCommand>(0)
    val viewsFiltered: StateFlow<ObjectValueAddView> = _viewsFiltered

    fun onStart(relationKey: Key, objectId: String, targetTypes: List<Id>) {
        Timber.d("key: $relationKey, object: ${objectId}, types: $targetTypes")
        processingViewsSelectionsAndFilter()
        jobs += viewModelScope.launch {
            val pipeline = combine(
                relations.observe(relationKey),
                values.subscribe(objectId)
            ) { relation, value ->
                when (val ids = value[relation.key]) {
                    is List<*> -> {
                        proceedWithSearchObjects(
                            excluded = ids.typeOf(),
                            targetTypes = targetTypes
                        )
                    }
                    is Id -> {
                        proceedWithSearchObjects(
                            excluded = listOf(ids),
                            targetTypes = targetTypes
                        )
                    }
                    null -> {
                        proceedWithSearchObjects(
                            excluded = emptyList(),
                            targetTypes = targetTypes
                        )
                    }
                }
            }
            pipeline.collect()
        }
    }

    fun onStop() {
        jobs.cancel()
    }

    private fun processingViewsSelectionsAndFilter() {
        viewModelScope.launch {
            combine(_views, _selected, _filter) { objects, selections, query ->
                objects
                    .map { obj ->
                        val index = selections.indexOf(obj.id)
                        if (index != -1) {
                            when (obj) {
                                is RelationValueView.Object.Default -> obj.copy(
                                    isSelected = true,
                                    selectedNumber = (index + 1).toString()
                                )
                                is RelationValueView.Object.NonExistent -> obj.copy(
                                    isSelected = true
                                )
                            }
                        } else {
                            when (obj) {
                                is RelationValueView.Object.Default -> obj.copy(
                                    isSelected = false,
                                    selectedNumber = null
                                )
                                is RelationValueView.Object.NonExistent -> obj.copy(
                                    isSelected = false
                                )
                            }
                        }
                    }
                    .filter { obj ->
                        obj is RelationValueView.Object.Default && obj.name.contains(query, true)
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

    private fun proceedWithSearchObjects(
        excluded: List<Id>,
        targetTypes: List<Id>
    ) {
        viewModelScope.launch {
            val filters = mutableListOf<DVFilter>()
            filters.addAll(ObjectSearchConstants.filterAddObjectToRelation)
            if (targetTypes.isEmpty()) {
                filters.add(
                    DVFilter(
                        relationKey = Relations.TYPE,
                        condition = DVFilterCondition.NOT_IN,
                        value = listOf(
                            ObjectTypeIds.OBJECT_TYPE,
                            ObjectTypeIds.RELATION,
                            ObjectTypeIds.TEMPLATE
                        )
                    )
                )
            } else {
                filters.add(
                    DVFilter(
                        relationKey = Relations.TYPE,
                        condition = DVFilterCondition.IN,
                        value = targetTypes
                    )
                )
            }
            searchObjects(
                SearchObjects.Params(
                    sorts = ObjectSearchConstants.sortAddObjectToRelation,
                    filters = filters,
                    fulltext = SearchObjects.EMPTY_TEXT,
                    offset = SearchObjects.INIT_OFFSET,
                    limit = SearchObjects.LIMIT,
                    keys = ObjectSearchConstants.defaultKeys
                )
            ).process(
                failure = { Timber.e(it, "Error while getting objects") },
                success = { objects ->
                    _views.value = objects.toRelationObjectValueView(
                        excluded = excluded,
                        urlBuilder = urlBuilder,
                        objectTypes = storeOfObjectTypes.getAll()
                    )
                }
            )
        }
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder,
        private val storeOfObjectTypes: StoreOfObjectTypes
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddObjectRelationViewModel(
                relations = relations,
                values = values,
                searchObjects = searchObjects,
                urlBuilder = urlBuilder,
                storeOfObjectTypes = storeOfObjectTypes
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