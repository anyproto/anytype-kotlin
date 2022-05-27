package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.RelationValueView.Option.Tag
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider.Options
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class BaseAddOptionsRelationViewModel(
    private val optionsProvider: AddOptionsRelationProvider,
    protected val values: ObjectValueProvider,
    protected val relations: ObjectRelationProvider,
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()

    private val query = MutableStateFlow("")
    private val allRelationOptions = MutableStateFlow(Options(emptyList(), emptyList()))
    protected val choosingRelationOptions = MutableStateFlow(emptyList<RelationValueView>())

    val ui = MutableStateFlow(listOf<RelationValueView>())
    val isAddButtonVisible = MutableStateFlow(true)
    val counter = MutableStateFlow(0)
    val isDismissed = MutableStateFlow(false)
    val isParentDismissed = MutableStateFlow(false)

    val isMultiple = MutableStateFlow(true)

    private val logger = Timber.tag("BaseAddOptionsRelation")

    init {
        viewModelScope.launch {
            allRelationOptions.collect { all ->
                choosingRelationOptions.value = all.notSelected
            }
        }
        viewModelScope.launch {
            choosingRelationOptions.combine(query) { choosing, query ->
                filterRelationsBy(query, allRelationOptions.value.all, choosing)
            }.collect {
                logger.i("Update ui: $it")
                ui.value = it
            }
        }
    }

    private fun filterRelationsBy(
        query: String,
        all: List<RelationValueView>,
        choosing: List<RelationValueView>
    ): List<RelationValueView> {
        return if (query.isEmpty())
            choosing
        else {
            val result = mutableListOf<RelationValueView>()
            val queriedOptions = choosing.filterIsInstance<RelationValueView.Option>()
                .filter { it.name.contains(query, true) }
            val searchedExistent = all.filterIsInstance<RelationValueView.Option>()
                .any {
                    it.name == query
                }
            if (!searchedExistent) {
                result.add(RelationValueView.Create(query))
            }
            result.addAll(queriedOptions)
            result.ifEmpty { listOf(RelationValueView.Empty) }
        }
    }

    fun onStart(target: Id, relationId: Id) {
        val s1 = relations.observe(relationId)
        val s2 = values.subscribe(target)
        jobs += viewModelScope.launch {
            s1.combine(s2) { relation, record ->
                buildViews(relation, record, relationId)
                if (relation.format == Relation.Format.STATUS) {
                    isAddButtonVisible.value = false
                    isMultiple.value = false
                }
            }.collect()
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    private fun buildViews(relation: Relation, record: Map<String, Any?>, relationId: Id) {
        allRelationOptions.value =
            optionsProvider.provideOptions(relation, record, relationId)
    }

    fun onFilterInputChanged(input: String) {
        query.value = input
    }

    fun onTagClicked(tag: Tag) {
        val oldOptions = choosingRelationOptions.value
        val newOptions = oldOptions
            .map { view ->
                if (view is Tag && view.id == tag.id) {
                    view.copy(isSelected = !view.isSelected)
                } else {
                    view
                }
            }
        choosingRelationOptions.value = newOptions
        counter.value =
            newOptions.count { it is Tag && it.isSelected }
    }
}

