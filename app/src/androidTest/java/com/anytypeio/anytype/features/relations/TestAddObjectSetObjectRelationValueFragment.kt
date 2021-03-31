package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.relations.AddObjectSetObjectRelationValueViewModel
import com.anytypeio.anytype.ui.relations.AddObjectSetObjectRelationValueFragment

class TestAddObjectSetObjectRelationValueFragment : AddObjectSetObjectRelationValueFragment() {
    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
    override fun proceedWithExiting() {}

    companion object {
        lateinit var testVmFactory: AddObjectSetObjectRelationValueViewModel.Factory
    }
}