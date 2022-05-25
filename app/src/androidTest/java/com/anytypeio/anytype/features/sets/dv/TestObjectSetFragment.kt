package com.anytypeio.anytype.features.sets.dv

import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.ui.sets.ObjectSetFragment

class TestObjectSetFragment : ObjectSetFragment() {
    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ObjectSetViewModelFactory
    }
}