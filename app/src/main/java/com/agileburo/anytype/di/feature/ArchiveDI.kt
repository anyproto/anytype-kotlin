package com.agileburo.anytype.di.feature

import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.page.ArchiveDocument
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.page.DocumentExternalEventReducer
import com.agileburo.anytype.presentation.page.archive.ArchiveViewModelFactory
import com.agileburo.anytype.presentation.page.editor.Orchestrator
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder
import com.agileburo.anytype.ui.archive.ArchiveFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [
        ArchiveModule::class,
        EditorUseCaseModule::class,
        EditorSessionModule::class
    ]
)
@PerScreen
interface ArchiveSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ArchiveModule): Builder
        fun build(): ArchiveSubComponent
    }

    fun inject(fragment: ArchiveFragment)
}

@Module
object ArchiveModule {

    @JvmStatic
    @Provides
    fun provideArchiveViewModelFactory(
        openPage: OpenPage,
        closePage: ClosePage,
        archiveDocument: ArchiveDocument,
        interceptEvents: InterceptEvents,
        renderer: DefaultBlockViewRenderer,
        selectionStateHolder: SelectionStateHolder,
        documentExternalEventReducer: DocumentExternalEventReducer,
        interactor: Orchestrator,
        analytics: Analytics
    ): ArchiveViewModelFactory = ArchiveViewModelFactory(
        openPage = openPage,
        closePage = closePage,
        archiveDocument = archiveDocument,
        interceptEvents = interceptEvents,
        renderer = renderer,
        selectionStateHolder = selectionStateHolder,
        reducer = documentExternalEventReducer,
        orchestrator = interactor,
        analytics = analytics
    )
}