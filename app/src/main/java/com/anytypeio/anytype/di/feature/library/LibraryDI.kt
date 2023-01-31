package com.anytypeio.anytype.di.feature.library

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.library.LibraryInteractor
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.presentation.library.delegates.LibraryRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.LibraryTypesDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyTypesDelegate
import com.anytypeio.anytype.ui.library.LibraryFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [LibraryDependencies::class],
    modules = [
        LibraryModule::class,
        LibraryModule.Declarations::class
    ]
)
@PerScreen
interface LibraryComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: LibraryDependencies): LibraryComponent
    }

    fun inject(fragment: LibraryFragment)
}

@Module
object LibraryModule {

    @PerScreen
    @Provides
    fun provideMyTypesDelegate(
        interactor: LibraryInteractor,
        workspaceManager: WorkspaceManager
    ): LibraryListDelegate {
        return MyTypesDelegate(interactor, workspaceManager)
    }

    @PerScreen
    @Provides
    fun provideLibTypesDelegate(interactor: LibraryInteractor): LibraryListDelegate {
        return LibraryTypesDelegate(interactor)
    }

    @PerScreen
    @Provides
    fun provideMyRelationsDelegate(
        interactor: LibraryInteractor,
        workspaceManager: WorkspaceManager
    ): LibraryListDelegate {
        return MyRelationsDelegate(interactor, workspaceManager)
    }

    @PerScreen
    @Provides
    fun provideLibRelationsDelegate(interactor: LibraryInteractor): LibraryListDelegate {
        return LibraryRelationsDelegate(interactor)
    }

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: LibraryViewModel.Factory): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun bindInteractor(interactor: LibraryInteractor.Impl): LibraryInteractor

    }

}

interface LibraryDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun workspaceManager(): WorkspaceManager
    fun urlBuilder(): UrlBuilder
}