package com.anytypeio.anytype.di.feature.library

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
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
import kotlinx.coroutines.Dispatchers

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
        container: StorelessSubscriptionContainer,
        workspaceManager: WorkspaceManager,
        urlBuilder: UrlBuilder
    ): LibraryListDelegate {
        return MyTypesDelegate(container, workspaceManager, urlBuilder)
    }

    @PerScreen
    @Provides
    fun provideLibTypesDelegate(
        container: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder
    ): LibraryListDelegate {
        return LibraryTypesDelegate(container, urlBuilder)
    }

    @PerScreen
    @Provides
    fun provideMyRelationsDelegate(
        container: StorelessSubscriptionContainer,
        workspaceManager: WorkspaceManager,
        urlBuilder: UrlBuilder
    ): LibraryListDelegate {
        return MyRelationsDelegate(container, workspaceManager, urlBuilder)
    }

    @PerScreen
    @Provides
    fun provideLibRelationsDelegate(
        container: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder
    ): LibraryListDelegate {
        return LibraryRelationsDelegate(container, urlBuilder)
    }

    @PerScreen
    @Provides
    fun provideAppCoroutineDispatchers() : AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: LibraryViewModel.Factory): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun bindContainer(container: StorelessSubscriptionContainer.Impl): StorelessSubscriptionContainer

    }

}

interface LibraryDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun workspaceManager(): WorkspaceManager
    fun urlBuilder(): UrlBuilder
    fun channel(): SubscriptionEventChannel
}