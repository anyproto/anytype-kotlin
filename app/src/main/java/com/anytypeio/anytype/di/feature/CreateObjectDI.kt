package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.objects.CreateObjectViewModel
import com.anytypeio.anytype.ui.editor.CreateObjectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

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
    @Provides
    @PerScreen
    fun getCreateObject(
        repo: BlockRepository,
        getTemplates: GetTemplates,
        getDefaultEditorType: GetDefaultEditorType
    ): CreateObject = CreateObject(
        repo = repo,
        getTemplates = getTemplates,
        getDefaultEditorType = getDefaultEditorType
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(repo: UserSettingsRepository): GetDefaultEditorType =
        GetDefaultEditorType(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetTemplates(repo: BlockRepository): GetTemplates = GetTemplates(
        repo = repo,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )


    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        createObject: CreateObject
    ): CreateObjectViewModel.Factory = CreateObjectViewModel.Factory(createObject = createObject)
}