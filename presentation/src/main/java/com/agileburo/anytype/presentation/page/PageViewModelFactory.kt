package com.agileburo.anytype.presentation.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.core_ui.features.page.pattern.Matcher
import com.agileburo.anytype.core_ui.features.page.pattern.Pattern
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.download.DownloadFile
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer

open class PageViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val archiveDocument: ArchiveDocument,
    private val redo: Redo,
    private val undo: Undo,
    private val updateText: UpdateText,
    private val createBlock: CreateBlock,
    private val replaceBlock: ReplaceBlock,
    private val interceptEvents: InterceptEvents,
    private val updateCheckbox: UpdateCheckbox,
    private val unlinkBlocks: UnlinkBlocks,
    private val duplicateBlock: DuplicateBlock,
    private val updateTextStyle: UpdateTextStyle,
    private val updateTextColor: UpdateTextColor,
    private val updateBackgroundColor: UpdateBackgroundColor,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val mergeBlocks: MergeBlocks,
    private val uploadUrl: UploadUrl,
    private val splitBlock: SplitBlock,
    private val documentEventReducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val downloadFile: DownloadFile,
    private val renderer: DefaultBlockViewRenderer,
    private val counter: Counter,
    private val patternMatcher: Matcher<Pattern>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageViewModel(
            openPage = openPage,
            closePage = closePage,
            undo = undo,
            redo = redo,
            updateText = updateText,
            createBlock = createBlock,
            archiveDocument = archiveDocument,
            interceptEvents = interceptEvents,
            updateCheckbox = updateCheckbox,
            duplicateBlock = duplicateBlock,
            unlinkBlocks = unlinkBlocks,
            updateTextStyle = updateTextStyle,
            updateTextColor = updateTextColor,
            updateBackgroundColor = updateBackgroundColor,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            mergeBlocks = mergeBlocks,
            splitBlock = splitBlock,
            uploadUrl = uploadUrl,
            createPage = createPage,
            documentExternalEventReducer = documentEventReducer,
            urlBuilder = urlBuilder,
            downloadFile = downloadFile,
            renderer = renderer,
            counter = counter,
            createDocument = createDocument,
            replaceBlock = replaceBlock,
            patternMatcher = patternMatcher
        ) as T
    }
}