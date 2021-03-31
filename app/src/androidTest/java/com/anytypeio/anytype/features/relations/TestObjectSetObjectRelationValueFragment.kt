package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.sets.ObjectSetObjectRelationValueViewModel
import com.anytypeio.anytype.ui.database.modals.ObjectSetObjectRelationValueFragment

class TestObjectSetObjectRelationValueFragment : ObjectSetObjectRelationValueFragment() {
    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ObjectSetObjectRelationValueViewModel.Factory
    }
}