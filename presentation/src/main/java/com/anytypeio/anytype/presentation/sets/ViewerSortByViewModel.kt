package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.page.editor.Proxy
import com.anytypeio.anytype.presentation.relations.simpleRelations
import com.anytypeio.anytype.presentation.relations.sortingExpression
import com.anytypeio.anytype.presentation.sets.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ViewerSortByViewModel(private val state: StateFlow<ObjectSet>) : ViewModel() {

    private val _viewState: MutableStateFlow<ViewerSortByViewState> =
        MutableStateFlow(ViewerSortByViewState.Init)
    var relations: ArrayList<SimpleRelationView> = arrayListOf()

    val viewState: StateFlow<ViewerSortByViewState> = _viewState
    val sorts: MutableStateFlow<List<SortingExpression>> = MutableStateFlow(emptyList())
    val commands = Proxy.Subject<ViewerSortByCommand>()

    init {
        startProcessingSorting()
    }

    fun onBackClicked() {
        viewModelScope.launch {
            commands.send(ViewerSortByCommand.BackToCustomize)
        }
    }

    private fun startProcessingSorting() {
        viewModelScope.launch {
            sorts.collect { value: List<SortingExpression> ->
                _viewState.value =
                    ViewerSortByViewState.Success(items = initSortingScreen(relations, value))
            }
        }
    }

    fun onViewCreated(viewerId: Id) {
        val set = state.value
        relations = set.simpleRelations(viewerId)
        sorts.value = set.sortingExpression(viewerId)
    }

    fun itemClicked(sortClick: SortClick) {
        when (sortClick) {
            is SortClick.ItemKey -> {
                viewModelScope.launch {
                    commands.send(
                        ViewerSortByCommand.Modal.ShowSortingKeyList(
                            old = sortClick.key,
                            sortingExpression = ArrayList(sorts.value),
                            relations = relations
                        )
                    )
                }
            }
            is SortClick.ItemType -> {
                viewModelScope.launch {
                    commands.send(
                        ViewerSortByCommand.Modal.ShowSortingTypeList(
                            key = sortClick.item.key,
                            selected = sortClick.item.type.ordinal
                        )
                    )
                }
            }
            is SortClick.Remove -> {
                removeSort(sortClick.key)
            }
            SortClick.Add -> {
                viewModelScope.launch {
                    commands.send(
                        ViewerSortByCommand.Modal.ShowSortingKeyList(
                            old = null,
                            sortingExpression = ArrayList(sorts.value),
                            relations = relations
                        )
                    )
                }
            }
            SortClick.Apply -> {
                viewModelScope.launch {
                    commands.send(ViewerSortByCommand.Apply(sorts.value))
                }
            }
        }
    }

    fun onAddSortKey(key: String) {
        sorts.value = sorts.value.plusElement(
            SortingExpression(
                key = key
            )
        )
    }

    fun onReplaceSortKey(keySelected: String, keyNew: String) {
        if (!isFieldInSorting(keyNew)) {
            sorts.value = sorts.value.map { sortingExpression ->
                if (sortingExpression.key == keySelected) {
                    sortingExpression.copy(key = keyNew)
                } else {
                    sortingExpression
                }
            }
        }
    }

    fun onPickSortType(key: String, type: Viewer.SortType) {
        sorts.value = sorts.value.map { sorting ->
            if (sorting.key == key) {
                sorting.copy(type = type)
            } else {
                sorting
            }
        }
    }

    private fun removeSort(key: String) {
        sorts.value = sorts.value.filter { it.key != key }
    }

    private fun initSortingScreen(
        relations: List<SimpleRelationView>,
        sorts: List<SortingExpression>
    ): List<SortingView> {
        val list = mutableListOf<SortingView>()
        sorts.forEachIndexed { index, expression ->
            val field = relations.first { it.key == expression.key }
            list.add(
                SortingView.Set(
                    key = expression.key,
                    title = field.title,
                    type = expression.type,
                    isWithPrefix = index > 0,
                    format = field.format
                )
            )
        }
        if (list.size < relations.size || list.size == 0) {
            list.add(SortingView.Add)
        }
        list.add(SortingView.Apply)
        return list
    }

    private fun isFieldInSorting(key: String): Boolean = sorts.value.any { it.key == key }

    class Factory(private val state: StateFlow<ObjectSet>) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewerSortByViewModel(state = state) as T
        }
    }
}

sealed class SortClick {
    data class ItemKey(val key: String) : SortClick()
    data class ItemType(val item: SortingView.Set) : SortClick()
    data class Remove(val key: String) : SortClick()
    object Add : SortClick()
    object Apply : SortClick()
}