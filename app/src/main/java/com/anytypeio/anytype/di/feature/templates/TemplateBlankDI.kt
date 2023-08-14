package com.anytypeio.anytype.di.feature.templates

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.templates.TemplateBlankViewModelFactory
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
import com.anytypeio.anytype.ui.templates.TemplateBlankFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    modules = [TemplateBlankModule::class],
    dependencies = [TemplateBlankDependencies::class]
)
@TemplateBlankScope
interface TemplateBlankComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: TemplateBlankDependencies): TemplateBlankComponent
    }

    fun inject(fragment: TemplateBlankFragment)
}

@Module
object TemplateBlankModule {

    @JvmStatic
    @TemplateBlankScope
    @Provides
    fun provideToggleHolder(): ToggleStateHolder = ToggleStateHolder.Default()

    @JvmStatic
    @TemplateBlankScope
    @Provides
    fun provideCoverImageHashProvider(): CoverImageHashProvider = DefaultCoverImageHashProvider()

    @JvmStatic
    @TemplateBlankScope
    @Provides
    fun provideViewModelFactory(
        renderer: DefaultBlockViewRenderer
    ): ViewModelProvider.Factory = TemplateBlankViewModelFactory(
        renderer = renderer
    )

    @Module
    interface Declarations {

        @TemplateBlankScope
        @Binds
        fun bindRenderer(
            defaultRenderer: DefaultBlockViewRenderer
        ): DefaultBlockViewRenderer
    }
}

interface TemplateBlankDependencies : ComponentDependencies {
    fun urlBuilder(): UrlBuilder
    fun storeOfRelations(): StoreOfRelations
    fun storeOfObjectTypes(): StoreOfObjectTypes
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class TemplateBlankScope