package com.agileburo.anytype.presentation.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.domain.block.interactor.RemoveLinkMark
import com.agileburo.anytype.domain.block.interactor.UpdateLinkMarks
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.domain.page.navigation.GetListPages
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.page.editor.Orchestrator
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer

open class PageViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val archiveDocument: ArchiveDocument,
    private val interceptEvents: InterceptEvents,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val documentEventReducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val interactor: Orchestrator,
    private val getListPages: GetListPages,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageViewModel(
            openPage = openPage,
            closePage = closePage,
            archiveDocument = archiveDocument,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            createPage = createPage,
            reducer = documentEventReducer,
            urlBuilder = urlBuilder,
            renderer = renderer,
            createDocument = createDocument,
            orchestrator = interactor,
            getListPages = getListPages,
            analytics = analytics
        ) as T
    }
}