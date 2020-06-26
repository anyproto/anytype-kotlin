package com.agileburo.anytype.features.editor.base

import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.page.PageFragment

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