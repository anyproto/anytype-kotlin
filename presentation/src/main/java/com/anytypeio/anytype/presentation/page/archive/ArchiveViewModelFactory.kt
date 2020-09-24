package com.anytypeio.anytype.presentation.page.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.domain.page.ClosePage
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.page.editor.Orchestrator
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder

open class ArchiveViewModelFactory(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val archiveDocument: ArchiveDocument,
    private val interceptEvents: InterceptEvents,
    private val renderer: DefaultBlockViewRenderer,
    private val reducer: StateReducer<List<Block>, Event>,
    private val orchestrator: Orchestrator,
    private val selectionStateHolder: SelectionStateHolder,
    private val analytics: Analytics
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
            selectionStateHolder = selectionStateHolder,
            analytics = analytics
        ) as T
    }
}