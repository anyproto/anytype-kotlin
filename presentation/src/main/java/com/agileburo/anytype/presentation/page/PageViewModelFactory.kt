package com.agileburo.anytype.presentation.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.block.interactor.CreateBlock
import com.agileburo.anytype.domain.block.interactor.UpdateBlock
import com.agileburo.anytype.domain.block.interactor.UpdateCheckbox
import com.agileburo.anytype.domain.event.interactor.ObserveEvents
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage

class PageViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val updateBlock: UpdateBlock,
    private val createBlock: CreateBlock,
    private val observeEvents: ObserveEvents,
    private val updateCheckbox: UpdateCheckbox
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageViewModel(
            openPage = openPage,
            closePage = closePage,
            updateBlock = updateBlock,
            createBlock = createBlock,
            observeEvents = observeEvents,
            updateCheckbox = updateCheckbox
        ) as T
    }
}