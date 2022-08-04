package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.relations.ObjectRelationList
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base view model for adding a relation either to an object or to a set.
 */
abstract class RelationAddViewModelBase(
    private val objectRelationList: ObjectRelationList,
    private val relationsProvider: ObjectRelationProvider,
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()
    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val views = MutableStateFlow<List<RelationView.Existing>>(emptyList())
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val isDismissed = MutableStateFlow(false)
    val results: Flow<List<RelationView.Existing>> = combine(searchQuery, views) { query, views ->
        if (query.isEmpty())
            views
        else
            views.filter { view -> view.name.contains(query, true) }
    }

    abstract fun sendAnalyticsEvent(length: Int)

    fun onStart(ctx: Id) {
        jobs += viewModelScope.launch {
            objectRelationList(ObjectRelationList.Params(ctx)).process(
                success = { relations ->
                    getVisibleRelations(relations)
                        .collect { visibleRelations ->
                            views.value = visibleRelations
                        }
                },
                failure = { Timber.e(it, "Error while fetching list of available relations") }
            )
        }
    }

    fun onStop() {
        jobs.forEach(Job::cancel)
        jobs.clear()
    }

    private fun getVisibleRelations(available: List<Relation>): Flow<List<RelationView.Existing>> {
        return relationsProvider.observeAll().map { addedRelations ->
            val addedRelationKeys = addedRelations.map { it.key }.toSet()
            available
                .filter { !it.isHidden }
                .filter { relation -> !addedRelationKeys.contains(relation.key) }
                .map {
                    RelationView.Existing(
                        id = it.key,
                        name = it.name,
                        format = it.format
                    )
                }
        }
    }

    fun onQueryChanged(input: String) {
        sendAnalyticsEvent(input.length)
        userInput.value = input
    }

    companion object {
        const val ERROR_MESSAGE = "Error while adding relation to object"
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }
}