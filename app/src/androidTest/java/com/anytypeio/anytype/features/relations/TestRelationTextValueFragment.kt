package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.sets.RelationTextValueViewModel
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment

class TestRelationTextValueFragment : RelationTextValueFragment() {

    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: RelationTextValueViewModel.Factory
    }
}