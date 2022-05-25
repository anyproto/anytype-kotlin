package com.anytypeio.anytype.features.sets.filter

import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterViewModel
import com.anytypeio.anytype.ui.sets.ViewerFilterFragment

class TestViewerFilterFragment : ViewerFilterFragment() {

    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ViewerFilterViewModel.Factory
    }
}