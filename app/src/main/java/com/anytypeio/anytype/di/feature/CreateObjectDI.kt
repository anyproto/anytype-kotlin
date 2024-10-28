package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.workspace.SpaceManager
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
        dispatchers: AppCoroutineDispatchers,
        logger: Logger
    ): CreateObjectByTypeAndTemplate = CreateObjectByTypeAndTemplate(
        repo = repo,
        dispatchers = dispatchers,
        logger = logger
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(
        blockRepository: BlockRepository,
        userSettingsRepository: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetDefaultObjectType = GetDefaultObjectType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetTemplates(
        repo: BlockRepository,
        spaceManager: SpaceManager,
        dispatchers: AppCoroutineDispatchers
    ): GetTemplates = GetTemplates(
        repo = repo,
        spaceManager = spaceManager,
        dispatchers = dispatchers
    )


    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        createObject: CreateObjectByTypeAndTemplate,
        spaceManager: SpaceManager,
        awaitAccountStartManager: AwaitAccountStartManager
    ): CreateObjectViewModel.Factory = CreateObjectViewModel.Factory(
        createObject = createObject,
        spaceManager = spaceManager,
        awaitAccountStart = awaitAccountStartManager
    )
}