package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.relations.RelationOptionValueDVAddViewModel
import com.anytypeio.anytype.ui.relations.RelationOptionValueDVAddFragment

class TestRelationOptionValueDVAddFragment : RelationOptionValueDVAddFragment() {
    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: RelationOptionValueDVAddViewModel.Factory
    }
}