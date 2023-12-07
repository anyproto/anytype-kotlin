package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.launch.SetDefaultObjectType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.settings.PersonalizationSettingsViewModel
import com.anytypeio.anytype.ui.settings.PersonalizationSettingsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@PerScreen
@Subcomponent(modules = [PersonalizationSettingsModule::class])
interface PersonalizationSettingsSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: PersonalizationSettingsModule): Builder
        fun build(): PersonalizationSettingsSubComponent
    }

    fun inject(fragment: PersonalizationSettingsFragment)
}

@Module
object PersonalizationSettingsModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        configStorage: ConfigStorage
    ): GetDefaultObjectType = GetDefaultObjectType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        configStorage = configStorage
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetDefaultPageType(
        repo: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetDefaultObjectType = SetDefaultObjectType(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideClearFileCache(repo: BlockRepository): ClearFileCache = ClearFileCache(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOtherSettingsFactory(
        getDefaultObjectType: GetDefaultObjectType,
        setDefaultObjectType: SetDefaultObjectType,
        clearFileCache: ClearFileCache,
        appActionManager: AppActionManager,
        analytics: Analytics,
        spaceManager: SpaceManager,
        searchObjects: SearchObjects
    ): PersonalizationSettingsViewModel.Factory = PersonalizationSettingsViewModel.Factory(
        getDefaultObjectType = getDefaultObjectType,
        setDefaultObjectType = setDefaultObjectType,
        clearFileCache = clearFileCache,
        appActionManager = appActionManager,
        analytics = analytics,
        spaceManager = spaceManager,
        searchObjects = searchObjects
    )
}