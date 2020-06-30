package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.page.navigation.GetListPages
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.agileburo.anytype.presentation.navigation.PageNavigationViewModelFactory
import com.agileburo.anytype.ui.navigation.PageNavigationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [PageNavigationModule::class]
)
@PerScreen
interface PageNavigationSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun pageNavigationModule(module: PageNavigationModule): Builder
        fun build(): PageNavigationSubComponent
    }

    fun inject(fragment: PageNavigationFragment)
}

@Module
class PageNavigationModule {

    @PerScreen
    @Provides
    fun getListPages(repo: BlockRepository): GetListPages =
        GetListPages(repo = repo)

    @PerScreen
    @Provides
    fun provideGetPageInfoWithLinks(repo: BlockRepository): GetPageInfoWithLinks =
        GetPageInfoWithLinks(repo = repo)

    @PerScreen
    @Provides
    fun provideNavigationViewModelFactory(
        getPageInfoWithLinks: GetPageInfoWithLinks,
        getListPages: GetListPages
    ): PageNavigationViewModelFactory =
        PageNavigationViewModelFactory(
            getPageInfoWithLinks = getPageInfoWithLinks,
            getListPages = getListPages
        )
}