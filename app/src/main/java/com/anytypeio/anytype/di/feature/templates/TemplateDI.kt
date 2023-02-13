package com.anytypeio.anytype.di.feature.templates

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.templates.OpenTemplate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.templates.TemplateViewModel
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
import com.anytypeio.anytype.ui.templates.TemplateFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

@Subcomponent(
    modules = [TemplateModule::class, TemplateModule.Bindings::class]
)
@PerScreen
interface TemplateSubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TemplateSubComponent
    }

    fun inject(fragment: TemplateFragment)
}

@Module
object TemplateModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun openTemplate(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ) : OpenTemplate = OpenTemplate(
        repo = repo,
        dispatchers = dispatchers
    )

    @Module
    interface Bindings {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: TemplateViewModel.Factory
        ): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun bindCoverImageHashProvider(
            defaultProvider: DefaultCoverImageHashProvider
        ) : CoverImageHashProvider

        @PerScreen
        @Binds
        fun bindRenderer(
            defaultRenderer: DefaultBlockViewRenderer
        ) : BlockViewRenderer

        @PerScreen
        @Binds
        fun bindToggleHolder(
            defaultHolder: ToggleStateHolder.Default
        ): ToggleStateHolder
    }
}