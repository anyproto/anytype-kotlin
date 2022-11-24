package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import kotlinx.coroutines.flow.StateFlow

class SelectFilterRelationViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val storeOfRelations: StoreOfRelations
) : SearchRelationViewModel(
    objectSetState = objectSetState,
    session = session,
    storeOfRelations = storeOfRelations
) {

    fun onRelationClicked(ctx: Id, relation: SimpleRelationView) {
        // TODO
    }

    class Factory(
        private val state: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val storeOfRelations: StoreOfRelations
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectFilterRelationViewModel(
                objectSetState = state,
                session = session,
                storeOfRelations = storeOfRelations
            ) as T
        }
    }
}