package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.presentation.page.DocumentExternalEventReducer
import com.anytypeio.anytype.presentation.page.archive.ArchiveViewModelFactory
import com.anytypeio.anytype.presentation.page.editor.Orchestrator
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder
import com.anytypeio.anytype.ui.archive.ArchiveFragment
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
        closePage: CloseBlock,
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