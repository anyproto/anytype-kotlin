package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.objects.CreateObjectViewModel
import com.anytypeio.anytype.ui.editor.CreateObjectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [CreateObjectModule::class])
@PerScreen
interface CreateObjectSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: CreateObjectModule): Builder
        fun build(): CreateObjectSubComponent
    }

    fun inject(fragment: CreateObjectFragment)
}

@Module
object CreateObjectModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun createPage(repo: BlockRepository): CreatePage = CreatePage(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        createPage: CreatePage
    ): CreateObjectViewModel.Factory = CreateObjectViewModel.Factory(createPage)
}