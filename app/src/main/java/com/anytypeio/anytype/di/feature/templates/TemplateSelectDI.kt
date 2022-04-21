package com.anytypeio.anytype.di.feature.templates

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.templates.TemplateSelectViewModel
import com.anytypeio.anytype.ui.templates.TemplateSelectFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [TemplateSelectModule::class, TemplateSelectModule.Bindings::class]
)
@PerScreen
interface TemplateSelectSubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TemplateSelectSubComponent
    }

    fun inject(fragment: TemplateSelectFragment)
}

@Module
object TemplateSelectModule {
    @JvmStatic
    @Provides
    @PerScreen
    fun createPage(repo: BlockRepository): CreatePage = CreatePage(repo)

    @Module
    interface Bindings {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: TemplateSelectViewModel.Factory
        ): ViewModelProvider.Factory
    }
}