package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.sets.ObjectRelationDateValueViewModel
import com.anytypeio.anytype.ui.relations.ObjectRelationDateValueFragment

class TestObjectRelationDateValueFragment: ObjectRelationDateValueFragment() {

    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ObjectRelationDateValueViewModel.Factory
    }
}