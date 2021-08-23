package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.moving.MoveToViewModelFactory
import com.anytypeio.anytype.ui.moving.MoveToFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [MoveToModule::class]
)
@PerScreen
interface MoveToSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: MoveToModule): Builder
        fun build(): MoveToSubComponent
    }

    fun inject(fragment: MoveToFragment)
}

@Module
object MoveToModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun getObjectTypes(repo: BlockRepository): GetObjectTypes = GetObjectTypes(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun searchObjects(repo: BlockRepository): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideMoveToViewModelFactory(
        urlBuilder: UrlBuilder,
        getObjectTypes: GetObjectTypes,
        searchObjects: SearchObjects,
        getFlavourConfig: GetFlavourConfig,
        analytics: Analytics
    ): MoveToViewModelFactory = MoveToViewModelFactory(
        urlBuilder = urlBuilder,
        getObjectTypes = getObjectTypes,
        searchObjects = searchObjects,
        analytics = analytics,
        getFlavourConfig = getFlavourConfig
    )
}