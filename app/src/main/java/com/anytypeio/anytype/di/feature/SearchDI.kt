package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.presentation.search.PageSearchViewModelFactory
import com.anytypeio.anytype.ui.search.PageSearchFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [PageSearchModule::class])
@PerScreen
interface PageSearchSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun pageSearchModule(module: PageSearchModule): Builder
        fun build(): PageSearchSubComponent
    }

    fun inject(fragment: PageSearchFragment)
}

@Module
object PageSearchModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun getListPages(repo: BlockRepository): GetListPages = GetListPages(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun providePageSearchViewModelFactory(
        urlBuilder: UrlBuilder,
        getListPages: GetListPages,
        analytics: Analytics
    ): PageSearchViewModelFactory = PageSearchViewModelFactory(urlBuilder, getListPages, analytics)
}