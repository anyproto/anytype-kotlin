package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
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
    @Provides
    @PerScreen
    fun getCreateObject(
        repo: BlockRepository,
        getTemplates: GetTemplates,
        getDefaultPageType: GetDefaultPageType,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager
    ): CreateObject = CreateObject(
        repo = repo,
        getTemplates = getTemplates,
        getDefaultPageType = getDefaultPageType,
        dispatchers = dispatchers,
        spaceManager = spaceManager
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(
        blockRepository: BlockRepository,
        userSettingsRepository: UserSettingsRepository,
        workspaceManager: WorkspaceManager,
        dispatchers: AppCoroutineDispatchers
    ): GetDefaultPageType = GetDefaultPageType(
        userSettingsRepository,
        blockRepository,
        workspaceManager,
        dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetTemplates(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetTemplates = GetTemplates(
        repo = repo,
        dispatchers = dispatchers
    )


    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        createObject: CreateObject
    ): CreateObjectViewModel.Factory = CreateObjectViewModel.Factory(createObject = createObject)
}