package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.interactor.CreateLinkToObject
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.agileburo.anytype.presentation.linking.LinkToObjectViewModelFactory
import com.agileburo.anytype.ui.linking.LinkToObjectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [LinkToObjectModule::class]
)
@PerScreen
interface LinkToObjectSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: LinkToObjectModule): Builder
        fun build(): LinkToObjectSubComponent
    }

    fun inject(fragment: LinkToObjectFragment)
}

@Module
object LinkToObjectModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetPageInfoWithLinks(
        repo: BlockRepository
    ): GetPageInfoWithLinks = GetPageInfoWithLinks(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLinkToObjectViewModelFactory(
        urlBuilder: UrlBuilder,
        getPageInfoWithLinks: GetPageInfoWithLinks,
        createLinkToObject: CreateLinkToObject,
        getConfig: GetConfig
    ): LinkToObjectViewModelFactory = LinkToObjectViewModelFactory(
        urlBuilder = urlBuilder,
        getPageInfoWithLinks = getPageInfoWithLinks,
        createLinkToObject = createLinkToObject,
        getConfig = getConfig
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCreateLinkToObjectUseCase(
        repo: BlockRepository
    ): CreateLinkToObject = CreateLinkToObject(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetConfigUseCase(
        repo: BlockRepository
    ): GetConfig = GetConfig(repo)
}