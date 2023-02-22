package com.anytypeio.anytype.di.feature.home

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.feature.widgets.SelectWidgetSourceSubcomponent
import com.anytypeio.anytype.di.feature.widgets.SelectWidgetTypeSubcomponent
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.CollapsedWidgetStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Component(
    dependencies = [HomeScreenDependencies::class],
    modules = [
        HomeScreenModule::class,
        HomeScreenModule.Declarations::class
    ]
)
@PerScreen
interface HomeScreenComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: HomeScreenDependencies): HomeScreenComponent
    }

    fun inject(fragment: HomeScreenFragment)

    fun selectWidgetSourceBuilder(): SelectWidgetSourceSubcomponent.Builder
    fun selectWidgetTypeBuilder(): SelectWidgetTypeSubcomponent.Builder
}

@Module
object HomeScreenModule {

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
    fun updateWidget(
        repo: BlockRepository,
    ): UpdateWidget = UpdateWidget(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetObject = GetObject(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun createObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        getDefaultEditorType: GetDefaultEditorType,
        getTemplates: GetTemplates
    ): CreateObject = CreateObject(
        repo = repo,
        dispatchers = dispatchers,
        getTemplates = getTemplates,
        getDefaultEditorType = getDefaultEditorType
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getDefaultEditorType(
        userSettingsRepository: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers,
    ) : GetDefaultEditorType = GetDefaultEditorType(
        userSettingsRepository = userSettingsRepository,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getTemplates(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
    ) : GetTemplates = GetTemplates(
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
    fun widgetEventDispatcher(): Dispatcher<WidgetDispatchEvent> = Dispatcher.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun objectPayloadDispatcher(): Dispatcher<Payload> = Dispatcher.Default()

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
        fun factory(factory: HomeScreenViewModel.Factory): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun container(container: StorelessSubscriptionContainer.Impl): StorelessSubscriptionContainer

        @PerScreen
        @Binds
        fun holder(holder: WidgetActiveViewStateHolder.Impl): WidgetActiveViewStateHolder

        @PerScreen
        @Binds
        fun collapsedWidgetStateHolder(
            holder: CollapsedWidgetStateHolder.Impl
        ): CollapsedWidgetStateHolder
    }
}

interface HomeScreenDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun authRepo(): AuthRepository
    fun userRepo(): UserSettingsRepository
    fun config(): ConfigStorage
    fun urlBuilder(): UrlBuilder
    fun objectStore(): ObjectStore
    fun subscriptionEventChannel(): SubscriptionEventChannel
    fun workspaceManager(): WorkspaceManager
    fun analytics(): Analytics
    fun eventChannel(): EventChannel
    fun dispatchers(): AppCoroutineDispatchers
}