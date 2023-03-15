package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.objects.toRelationFileValueView
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.addIsHiddenFilter
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.searchObjectsFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class AddFileRelationViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    private val _views =
        MutableStateFlow<List<RelationValueView.File>>(listOf())
    private val _filter = MutableStateFlow("")
    private var _selected = MutableStateFlow<List<Id>>(listOf())
    private val _viewsFiltered = MutableStateFlow(FileValueAddView())
    private val jobs = mutableListOf<Job>()

    val commands = MutableSharedFlow<FileValueAddCommand>(0)
    val viewsFiltered: StateFlow<FileValueAddView> = _viewsFiltered

    fun onStart(
        ctx: Id,
        relationKey: Key,
        objectId: String
    ) {
        processingViewsSelectionsAndFilter()
        jobs += viewModelScope.launch {
            val pipeline = combine(
                relations.observe(relationKey),
                values.subscribe(ctx = ctx, target = objectId)
            ) { relation, value ->
                when (val ids = value[relation.key]) {
                    is List<*> -> {
                        proceedWithSearchFiles(
                            ids = ids.typeOf(),
                            relation = relation
                        )
                    }
                    is Id -> {
                        proceedWithSearchFiles(
                            ids = listOf(ids),
                            relation = relation
                        )
                    }
                    null -> {
                        proceedWithSearchFiles(
                            ids = emptyList(),
                            relation = relation
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
            }.collect { files ->
                val size = files.filter { it.isSelected == true }.size
                _viewsFiltered.value = FileValueAddView(
                    files = files,
                    count = size.toString()
                )
            }
        }
    }

    fun onActionButtonClicked() {
        viewModelScope.launch {
            commands.emit(FileValueAddCommand.DispatchResult(ids = _selected.value))
        }
    }

    fun onFilterTextChanged(filter: String) {
        _filter.value = filter
        _selected.value = listOf()
    }

    fun onFileClicked(objectId: Id) {
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

    private fun proceedWithSearchFiles(ids: List<String>, relation: ObjectWrapper.Relation) {
        val filters = relation
            .searchObjectsFilter()
            .addIsHiddenFilter()
        val sorts = arrayListOf(
            DVSort(
                relationKey = Relations.LAST_MODIFIED_DATE,
                type = Block.Content.DataView.Sort.Type.DESC
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
                success = { objects ->
                    _views.value = objects.toRelationFileValueView(
                        ids = ids,
                        urlBuilder = urlBuilder
                    )
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddFileRelationViewModel(
                relations = relations,
                values = values,
                searchObjects = searchObjects,
                urlBuilder = urlBuilder
            ) as T
        }
    }

}

data class FileValueAddView(
    val files: List<RelationValueView.File> = emptyList(),
    val count: String? = null
)

sealed class FileValueAddCommand {
    data class DispatchResult(val ids: List<Id>) : FileValueAddCommand()
}