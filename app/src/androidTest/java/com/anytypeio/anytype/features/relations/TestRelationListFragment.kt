package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.ui.relations.RelationListFragment

class TestRelationListFragment : RelationListFragment() {
    override fun injectDependencies() {
        factory = testVmFactory
    }

    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ObjectRelationListViewModelFactory
    }
}