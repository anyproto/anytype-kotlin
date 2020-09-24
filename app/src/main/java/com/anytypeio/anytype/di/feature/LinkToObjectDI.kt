package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.interactor.CreateLinkToObject
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.anytypeio.anytype.presentation.linking.LinkToObjectViewModelFactory
import com.anytypeio.anytype.ui.linking.LinkToObjectFragment
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