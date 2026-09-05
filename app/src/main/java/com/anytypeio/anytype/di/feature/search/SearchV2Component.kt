package com.anytypeio.anytype.di.feature.search

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.CrossSpaceObjectTypesContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.workspace.DeepLinkToObjectDelegate
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_search.presentation.SearchViewModel
import com.anytypeio.anytype.ui.search.v2.SearchV2Fragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [SearchV2Dependencies::class],
    modules = [
        SearchV2Module::class,
        SearchV2Module.Declarations::class
    ]
)
@PerScreen
interface SearchV2Component {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: SearchViewModel.VmParams,
            dependencies: SearchV2Dependencies
        ): SearchV2Component
    }

    fun inject(fragment: SearchV2Fragment)

    /** For embedded use (the chat attach-object sheet). */
    fun searchViewModel(): SearchViewModel
}

@Module
object SearchV2Module {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDeepLinkToObjectDelegate(
        default: DeepLinkToObjectDelegate.Default
    ): DeepLinkToObjectDelegate = default

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchViewModel(
        factory: SearchViewModel.Factory
    ): SearchViewModel = factory.create(SearchViewModel::class.java)

    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: SearchViewModel.Factory): ViewModelProvider.Factory
    }
}

interface SearchV2Dependencies : ComponentDependencies {
    fun repo(): BlockRepository
    fun userSettingsRepository(): UserSettingsRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun urlBuilder(): UrlBuilder
    fun fieldParser(): FieldParser
    fun configStorage(): ConfigStorage
    fun spaceManager(): SpaceManager
    fun userPermissionProvider(): UserPermissionProvider
    fun spaceViews(): SpaceViewSubscriptionContainer
    fun participants(): ParticipantSubscriptionContainer
    fun chatsDetails(): ChatsDetailsSubscriptionContainer
    fun crossSpaceObjectTypes(): CrossSpaceObjectTypesContainer
    fun storeOfObjectTypes(): StoreOfObjectTypes
}
