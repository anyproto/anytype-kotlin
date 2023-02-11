package com.anytypeio.anytype.ui.widgets.collection

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Component(
    dependencies = [CollectionDependencies::class],
    modules = [
        CollectionModule::class
    ]
)
@PerScreen
interface CollectionComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: CollectionDependencies): CollectionComponent
    }

    fun inject(fragment: CollectionFragment)

}

@Module(includes = [CollectionModule.Declarations::class])
object CollectionModule {

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
        repo: BlockRepository
    ): CreateWidget = CreateWidget(
        repo = repo,
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun deleteWidget(
        repo: BlockRepository,
    ): DeleteWidget = DeleteWidget(
        repo = repo
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
    fun appCoroutineDispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        main = Dispatchers.Main,
        computation = Dispatchers.Default
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun widgetEventDispatcher(): Dispatcher<WidgetDispatchEvent> = Dispatcher.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun objectPayloadDispatcher(): Dispatcher<Payload> = Dispatcher.Default()

    @JvmStatic
    @PerScreen
    @Provides
    fun getObjectTypes(repo: BlockRepository): GetObjectTypes = GetObjectTypes(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun interceptEvents(channel: EventChannel): InterceptEvents = InterceptEvents(
        context = Dispatchers.IO,
        channel = channel
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun factory(factory: CollectionViewModel.Factory): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun bindContainer(container: StorelessSubscriptionContainer.Impl): StorelessSubscriptionContainer
    }
}

interface CollectionDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun authRepo(): AuthRepository
    fun config(): ConfigStorage
    fun urlBuilder(): UrlBuilder
    fun objectStore(): ObjectStore
    fun subscriptionEventChannel(): SubscriptionEventChannel
    fun workspaceManager(): WorkspaceManager
    fun analytics(): Analytics
    fun eventChannel(): EventChannel
}