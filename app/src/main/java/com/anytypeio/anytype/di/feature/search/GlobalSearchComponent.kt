package com.anytypeio.anytype.di.feature.search

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.ui.search.GlobalSearchFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [GlobalSearchDependencies::class],
    modules = [
        GlobalSearchModule::class,
        GlobalSearchModule.Declarations::class
    ]
)
@PerScreen
interface GlobalSearchComponent {
    @Component.Factory
    interface Factory {
        fun create(dependencies: GlobalSearchDependencies): GlobalSearchComponent
    }
    fun inject(fragment: GlobalSearchFragment)
}

@Module
object GlobalSearchModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: GlobalSearchViewModel.Factory): ViewModelProvider.Factory
    }
}

interface GlobalSearchDependencies : ComponentDependencies {
    fun urlBuilder(): UrlBuilder
    fun repo(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun storeOfRelations(): StoreOfRelations
    fun spaceManager(): SpaceManager
    fun analytics(): Analytics
    fun analyticsHelper() : AnalyticSpaceHelperDelegate
}