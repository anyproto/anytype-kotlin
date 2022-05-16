package com.anytypeio.anytype.features.relations

import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationDVViewModel
import com.anytypeio.anytype.ui.relations.add.AddOptionsRelationDVFragment

class TestRelationOptionValueDVAddFragment : AddOptionsRelationDVFragment() {
    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: AddOptionsRelationDVViewModel.Factory
    }
}