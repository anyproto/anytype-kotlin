package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.linking.BackLinkOrAddToObjectViewModelFactory
import com.anytypeio.anytype.ui.linking.BacklinkOrAddToObjectFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides


@PerScreen
@Component(
    dependencies = [BacklinkOrAddToObjectDependencies::class],
    modules = [
        BackLinkToObjectModule::class,
        BackLinkToObjectModule.Declarations::class
    ]
)
interface BacklinkOrAddToObjectComponent {

    @Component.Builder
    interface Builder {

        fun withDependencies(dependency: BacklinkOrAddToObjectDependencies): Builder

        @BindsInstance
        fun withContext(context: Id): Builder

        fun build(): BacklinkOrAddToObjectComponent
    }

    fun inject(fragment: BacklinkOrAddToObjectFragment)
}

@Module
object BackLinkToObjectModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun searchObjects(repo: BlockRepository): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetObjectTypesUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetObjectTypes = GetObjectTypes(repository, dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: BackLinkOrAddToObjectViewModelFactory): ViewModelProvider.Factory
    }
}

interface BacklinkOrAddToObjectDependencies : ComponentDependencies {
    fun authRepository(): AuthRepository
    fun blockRepository(): BlockRepository
    fun workspaceManager(): WorkspaceManager
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun spaceManager(): SpaceManager
}