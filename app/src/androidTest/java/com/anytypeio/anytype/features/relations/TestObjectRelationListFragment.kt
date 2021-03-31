package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.ui.relations.ObjectRelationListFragment

class TestObjectRelationListFragment : ObjectRelationListFragment() {
    override fun injectDependencies() {
        factory = testVmFactory
    }

    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ObjectRelationListViewModelFactory
    }
}