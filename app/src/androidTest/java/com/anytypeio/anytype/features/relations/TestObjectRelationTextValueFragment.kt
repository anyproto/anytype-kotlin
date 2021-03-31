package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.sets.ObjectRelationTextValueViewModel
import com.anytypeio.anytype.ui.relations.ObjectRelationTextValueFragment

class TestObjectRelationTextValueFragment : ObjectRelationTextValueFragment() {

    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ObjectRelationTextValueViewModel.Factory
    }
}