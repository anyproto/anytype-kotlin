package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetListPages
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
    fun getListPages(repo: BlockRepository): GetListPages = GetListPages(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        urlBuilder: UrlBuilder,
        getListPages: GetListPages,
        analytics: Analytics
    ): ObjectSearchViewModelFactory = ObjectSearchViewModelFactory(urlBuilder, getListPages, analytics)
}