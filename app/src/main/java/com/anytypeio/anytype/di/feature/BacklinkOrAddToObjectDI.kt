package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.linking.BackLinkOrAddToObjectViewModelFactory
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
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
        fun withParams(params: ObjectSearchViewModel.VmParams): Builder

        fun build(): BacklinkOrAddToObjectComponent
    }

    fun inject(fragment: BacklinkOrAddToObjectFragment)
}

@Module
object BackLinkToObjectModule {

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
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
    fun searchObjects(): SearchObjects
    fun dateProvider(): DateProvider
    fun fieldParser(): FieldParser
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun spaceViews(): SpaceViewSubscriptionContainer
}
