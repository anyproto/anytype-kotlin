package com.anytypeio.anytype.features.sets.sort

import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel
import com.anytypeio.anytype.ui.sets.modals.sort.ViewerSortFragment

class TestViewerSortFragment : ViewerSortFragment() {

    init {
        factory = testVmFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ViewerSortViewModel.Factory
    }
}