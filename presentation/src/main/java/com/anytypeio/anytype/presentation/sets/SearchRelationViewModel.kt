package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.relations.simpleRelations
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

/**
 * Inherit this class in order to enable search-for-relations feature.
 */
abstract class SearchRelationViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val storeOfRelations: StoreOfRelations
) : BaseListViewModel<SimpleRelationView>() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    private val query = Channel<String>()
    private val notAllowedRelationFormats = listOf(
        ColumnView.Format.RELATIONS,
        ColumnView.Format.EMOJI,
        ColumnView.Format.FILE
    )
    private val jobs = mutableListOf<Job>()

    fun onStart(viewerId: Id) {
        // Initializing views before any query.
        jobs += viewModelScope.launch {
            _views.value =
                filterRelationsFromAlreadyInUse(
                    objectState = objectState.value,
                    viewerId = viewerId,
                    storeOfRelations = storeOfRelations
                )
                    .filterNot { notAllowedRelations(it) }
        }
        // Searching and mapping views based on query changes.
        jobs += viewModelScope.launch {
            query
                .consumeAsFlow()
                .withLatestFrom(objectState) { query, state ->
                    val relations = filterRelationsFromAlreadyInUse(
                        objectState = state,
                        viewerId = viewerId,
                        storeOfRelations = storeOfRelations
                    )
                    if (query.isEmpty()) {
                        relations
                    } else {
                        relations.filter { relation ->
                            relation.title.contains(query, ignoreCase = true)
                        }
                    }
                }
                .mapLatest { relations ->
                    relations.filterNot { notAllowedRelations(it) }
                }
                .collect { _views.value = it }
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    protected open suspend fun filterRelationsFromAlreadyInUse(
        objectState: ObjectState,
        viewerId: String?,
        storeOfRelations: StoreOfRelations
    ): List<SimpleRelationView> {
        return objectState.simpleRelations(
            viewerId = viewerId,
            storeOfRelations = storeOfRelations
        )
    }

    private fun notAllowedRelations(relation: SimpleRelationView): Boolean =
        notAllowedRelationFormats.contains(relation.format)
                || (relation.key != Relations.NAME && relation.key != Relations.DONE && relation.isHidden)

    fun onSearchQueryChanged(txt: String) {
        viewModelScope.launch { query.send(txt) }
    }
}