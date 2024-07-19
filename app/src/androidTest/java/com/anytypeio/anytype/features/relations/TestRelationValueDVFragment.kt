package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel

class TestRelationValueDVFragment : RelationValueDVFragment() {
    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: RelationValueDVViewModel.Factory
    }
}