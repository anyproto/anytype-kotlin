package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModelFactory
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectSearchModule::class])
@PerScreen
interface ObjectSearchSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSearchModule): Builder
        fun build(): ObjectSearchSubComponent
    }

    fun inject(fragment: ObjectSearchFragment)
}

@Module
object ObjectSearchModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun getObjectTypes(repo: BlockRepository): GetObjectTypes = GetObjectTypes(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun searchObjects(repo: BlockRepository): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        urlBuilder: UrlBuilder,
        getObjectTypes: GetObjectTypes,
        searchObjects: SearchObjects,
        analytics: Analytics
    ): ObjectSearchViewModelFactory = ObjectSearchViewModelFactory(
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        getObjectTypes = getObjectTypes,
        analytics = analytics
    )
}