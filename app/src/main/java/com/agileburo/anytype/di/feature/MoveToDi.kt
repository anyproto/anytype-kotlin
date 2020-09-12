package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.agileburo.anytype.presentation.moving.MoveToViewModelFactory
import com.agileburo.anytype.ui.moving.MoveToFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [MoveToModule::class]
)
@PerScreen
interface MoveToSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: MoveToModule): Builder
        fun build(): MoveToSubComponent
    }

    fun inject(fragment: MoveToFragment)
}

@Module
object MoveToModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetPageInfoWithLinks(
        repo: BlockRepository
    ): GetPageInfoWithLinks = GetPageInfoWithLinks(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetConfigUseCase(
        repo: BlockRepository
    ): GetConfig = GetConfig(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideMoveUseCase(
        repo: BlockRepository
    ): Move = Move(
        repo = repo
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideMoveToViewModelFactory(
        urlBuilder: UrlBuilder,
        getPageInfoWithLinks: GetPageInfoWithLinks,
        getConfig: GetConfig,
        move: Move
    ): MoveToViewModelFactory = MoveToViewModelFactory(
        urlBuilder = urlBuilder,
        getPageInfoWithLinks = getPageInfoWithLinks,
        getConfig = getConfig,
        move = move
    )
}