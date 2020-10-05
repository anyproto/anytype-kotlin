package com.anytypeio.anytype.presentation.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.*
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.page.editor.Orchestrator
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer

open class PageViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val createNewDocument: CreateNewDocument,
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
            createNewDocument = createNewDocument,
            orchestrator = interactor,
            getListPages = getListPages,
            analytics = analytics
        ) as T
    }
}