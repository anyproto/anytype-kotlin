package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.anytypeio.anytype.presentation.navigation.PageNavigationViewModelFactory
import com.anytypeio.anytype.ui.navigation.PageNavigationFragment
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
object PageNavigationModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetPageInfoWithLinks(repo: BlockRepository): GetPageInfoWithLinks =
        GetPageInfoWithLinks(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideNavigationViewModelFactory(
        urlBuilder: UrlBuilder,
        getPageInfoWithLinks: GetPageInfoWithLinks,
        getConfig: GetConfig,
        analytics: Analytics
    ): PageNavigationViewModelFactory =
        PageNavigationViewModelFactory(
            urlBuilder = urlBuilder,
            getPageInfoWithLinks = getPageInfoWithLinks,
            getConfig = getConfig,
            analytics = analytics
        )

    @JvmStatic
    @Provides
    @PerScreen
    fun getConfigUseCase(
        repo: BlockRepository
    ): GetConfig = GetConfig(
        repo = repo
    )
}