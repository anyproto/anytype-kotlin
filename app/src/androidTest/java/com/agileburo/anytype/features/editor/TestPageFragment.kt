package com.agileburo.anytype.features.editor

import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.page.PageFragment

class TestPageFragment : PageFragment() {

    init {
        this.factory =
            testViewModelFactory
    }

    override fun injectDependencies() {}

    companion object {
        lateinit var testViewModelFactory: PageViewModelFactory
    }
}