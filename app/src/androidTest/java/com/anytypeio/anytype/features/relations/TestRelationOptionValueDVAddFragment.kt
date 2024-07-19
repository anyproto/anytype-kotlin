package com.anytypeio.anytype.features.relations

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