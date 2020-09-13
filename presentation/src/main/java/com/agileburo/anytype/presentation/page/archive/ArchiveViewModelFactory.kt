package com.agileburo.anytype.presentation.page.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.ArchiveDocument
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.page.editor.Orchestrator
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder

open class ArchiveViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val archiveDocument: ArchiveDocument,
    private val interceptEvents: InterceptEvents,
    private val renderer: DefaultBlockViewRenderer,
    private val reducer: StateReducer<List<Block>, Event>,
    private val orchestrator: Orchestrator,
    private val selectionStateHolder: SelectionStateHolder
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ArchiveViewModel(
            openPage = openPage,
            closePage = closePage,
            archiveDocument = archiveDocument,
            interceptEvents = interceptEvents,
            renderer = renderer,
            reducer = reducer,
            orchestrator = orchestrator,
            selectionStateHolder = selectionStateHolder
        ) as T
    }
}