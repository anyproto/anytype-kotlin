package com.anytypeio.anytype.di.feature.home

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

@Subcomponent(
    modules = [HomeScreenModule::class]
)
@PerScreen
interface HomeScreenSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: HomeScreenModule): Builder
        fun build(): HomeScreenSubComponent
    }

    fun inject(fragment: HomeScreenFragment)
}

@Module
object HomeScreenModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun vmFactory(
        openObject: OpenObject,
        createWidget: CreateWidget,
        configStorage: ConfigStorage,
        objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
        dispatchers: AppCoroutineDispatchers
    ): HomeScreenViewModel.Factory = HomeScreenViewModel.Factory(
        openObject = openObject,
        createWidget = createWidget,
        configStorage = configStorage,
        objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun openObject(
        repo: BlockRepository,
        auth: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): OpenObject = OpenObject(
        repo = repo,
        dispatchers = dispatchers,
        auth = auth
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun createWidget(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateWidget = CreateWidget(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun objectSearchSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        store: ObjectStore,
        dispatchers: AppCoroutineDispatchers
    ): ObjectSearchSubscriptionContainer = ObjectSearchSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun dispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        main = Dispatchers.Main,
        computation = Dispatchers.Default
    )
}