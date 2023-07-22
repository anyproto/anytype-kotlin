package com.anytypeio.anytype.di.feature.templates

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.templates.TemplateBlankViewModel
import com.anytypeio.anytype.ui.templates.TemplateBlankFragment
import dagger.Binds
import dagger.Component
import dagger.Module
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

    @Module
    interface Declarations {

        @TemplateBlankScope
        @Binds
        fun bindViewModelFactory(
            factory: TemplateBlankViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface TemplateBlankDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun blockRenderer(): DefaultBlockViewRenderer
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class TemplateBlankScope