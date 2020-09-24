package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.anytypeio.anytype.presentation.moving.MoveToViewModelFactory
import com.anytypeio.anytype.ui.moving.MoveToFragment
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