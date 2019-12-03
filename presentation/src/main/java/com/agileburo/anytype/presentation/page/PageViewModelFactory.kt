package com.agileburo.anytype.presentation.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.ObservePage
import com.agileburo.anytype.domain.page.OpenPage

class PageViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val observePage: ObservePage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageViewModel(
            openPage = openPage,
            observePage = observePage,
            closePage = closePage
        ) as T
    }
}