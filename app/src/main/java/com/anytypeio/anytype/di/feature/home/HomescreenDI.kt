package com.anytypeio.anytype.di.feature.home

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.presentation.home.HomeViewModel
import com.anytypeio.anytype.ui.home.HomeFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

@Subcomponent(
    modules = [HomescreenModule::class]
)
@PerScreen
interface HomescreenSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: HomescreenModule): Builder
        fun build(): HomescreenSubComponent
    }

    fun inject(fragment: HomeFragment)
}

@Module
object HomescreenModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun vmFactory(
        openObject: OpenObject,
        createWidget: CreateWidget,
        configStorage: ConfigStorage,
        objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer
    ): HomeViewModel.Factory = HomeViewModel.Factory(
        openObject = openObject,
        createWidget = createWidget,
        configStorage = configStorage,
        objectSearchSubscriptionContainer = objectSearchSubscriptionContainer
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