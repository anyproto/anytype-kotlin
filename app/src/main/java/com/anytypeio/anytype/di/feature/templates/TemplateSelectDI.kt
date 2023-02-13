package com.anytypeio.anytype.di.feature.templates

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.presentation.templates.TemplateSelectViewModel
import com.anytypeio.anytype.ui.templates.TemplateSelectFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

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
    fun applyTemplate(repo: BlockRepository, dispatchers: AppCoroutineDispatchers): ApplyTemplate =
        ApplyTemplate(
            repo = repo,
            dispatchers = dispatchers
        )

    @Module
    interface Bindings {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: TemplateSelectViewModel.Factory
        ): ViewModelProvider.Factory
    }
}