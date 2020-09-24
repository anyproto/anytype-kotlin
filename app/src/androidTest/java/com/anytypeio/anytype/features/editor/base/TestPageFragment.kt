package com.anytypeio.anytype.features.editor.base

import com.anytypeio.anytype.presentation.page.PageViewModelFactory
import com.anytypeio.anytype.ui.page.PageFragment

class TestPageFragment : PageFragment() {
    init {
        factory = testViewModelFactory
    }
    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testViewModelFactory: PageViewModelFactory
    }
}