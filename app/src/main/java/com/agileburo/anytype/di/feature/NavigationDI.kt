package com.agileburo.anytype.di.feature

import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
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