package com.agileburo.anytype.presentation.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage

class PageViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val updateBlock: UpdateBlock,
    private val createBlock: CreateBlock,
    private val interceptEvents: InterceptEvents,
    private val updateCheckbox: UpdateCheckbox,
    private val unlinkBlocks: UnlinkBlocks,
    private val duplicateBlock: DuplicateBlock,
    private val updateTextStyle: UpdateTextStyle,
    private val updateTextColor: UpdateTextColor,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val mergeBlocks: MergeBlocks
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageViewModel(
            openPage = openPage,
            closePage = closePage,
            updateBlock = updateBlock,
            createBlock = createBlock,
            interceptEvents = interceptEvents,
            updateCheckbox = updateCheckbox,
            duplicateBlock = duplicateBlock,
            unlinkBlocks = unlinkBlocks,
            updateTextStyle = updateTextStyle,
            updateTextColor = updateTextColor,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            mergeBlocks = mergeBlocks
        ) as T
    }
}