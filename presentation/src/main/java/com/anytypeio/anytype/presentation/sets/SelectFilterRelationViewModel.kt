package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlinx.coroutines.flow.StateFlow

class SelectFilterRelationViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val session: ObjectSetSession,
    private val storeOfRelations: StoreOfRelations
) : SearchRelationViewModel(
    objectState = objectState,
    session = session,
    storeOfRelations = storeOfRelations
) {

    fun onRelationClicked(ctx: Id, relation: SimpleRelationView) {
        // TODO
    }

    class Factory(
        private val objectState: StateFlow<ObjectState>,
        private val session: ObjectSetSession,
        private val storeOfRelations: StoreOfRelations
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectFilterRelationViewModel(
                objectState = objectState,
                session = session,
                storeOfRelations = storeOfRelations
            ) as T
        }
    }
}