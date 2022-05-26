package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetObjectInfoWithLinks
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
    fun provideGetPageInfoWithLinks(repo: BlockRepository): GetObjectInfoWithLinks =
        GetObjectInfoWithLinks(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideNavigationViewModelFactory(
        urlBuilder: UrlBuilder,
        getObjectInfoWithLinks: GetObjectInfoWithLinks,
        getConfig: GetConfig,
        analytics: Analytics,
        objectTypesProvider: ObjectTypesProvider
    ): PageNavigationViewModelFactory =
        PageNavigationViewModelFactory(
            urlBuilder = urlBuilder,
            getObjectInfoWithLinks = getObjectInfoWithLinks,
            getConfig = getConfig,
            analytics = analytics,
            objectTypesProvider = objectTypesProvider
        )

    @JvmStatic
    @Provides
    @PerScreen
    fun getConfigUseCase(
        provider: ConfigStorage
    ): GetConfig = GetConfig(
        provider = provider
    )
}