package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.sets.RelationDateValueViewModel
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment

class TestRelationDateValueFragment: RelationDateValueFragment() {

    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: RelationDateValueViewModel.Factory
    }
}