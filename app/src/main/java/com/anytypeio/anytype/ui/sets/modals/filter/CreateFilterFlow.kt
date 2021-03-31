package com.anytypeio.anytype.ui.sets.modals.filter

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView

interface CreateFilterFlow {
    fun onRelationSelected(ctx: Id, relation: SimpleRelationView)
    fun onFilterCreated()
}