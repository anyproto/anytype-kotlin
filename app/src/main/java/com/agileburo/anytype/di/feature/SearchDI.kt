package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetListPages
import com.agileburo.anytype.presentation.search.PageSearchViewModelFactory
import com.agileburo.anytype.ui.search.PageSearchFragment
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
class PageSearchModule {

    @PerScreen
    @Provides
    fun getListPages(repo: BlockRepository): GetListPages = GetListPages(repo = repo)

    @Provides
    @PerScreen
    fun providePageSearchViewModelFactory(
        urlBuilder: UrlBuilder,
        getListPages: GetListPages
    ): PageSearchViewModelFactory = PageSearchViewModelFactory(urlBuilder, getListPages)
}