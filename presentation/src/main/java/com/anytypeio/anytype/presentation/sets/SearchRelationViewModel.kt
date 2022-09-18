package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.relations.simpleRelations
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
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
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession
) : BaseListViewModel<SimpleRelationView>() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    private val query = Channel<String>()
    private val notAllowedRelationFormats = listOf(
        ColumnView.Format.RELATIONS,
        ColumnView.Format.EMOJI,
        ColumnView.Format.FILE
    )

    init {
        // Initializing views before any query.
        viewModelScope.launch {
            _views.value =
                filterRelationsFromAlreadyInUse(objectSetState.value, session.currentViewerId.value)
                    .filterNot { notAllowedRelations(it) }
        }
        // Searching and mapping views based on query changes.
        viewModelScope.launch {
            query
                .consumeAsFlow()
                .withLatestFrom(objectSetState) { query, state ->
                    val relations = filterRelationsFromAlreadyInUse(state, session.currentViewerId.value)
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

    protected open fun filterRelationsFromAlreadyInUse(
        set: ObjectSet, viewerId: String?
    ): List<SimpleRelationView> {
        return set.simpleRelations(viewerId)
    }

    private fun notAllowedRelations(relation: SimpleRelationView): Boolean =
        notAllowedRelationFormats.contains(relation.format)
                || (relation.key != Relations.NAME && relation.key != Relations.DONE && relation.isHidden)

    fun onSearchQueryChanged(txt: String) {
        viewModelScope.launch { query.send(txt) }
    }
}