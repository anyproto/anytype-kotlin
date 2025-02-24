package com.anytypeio.anytype.di.feature

import android.content.Context
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.dashboard.interactor.SetObjectListIsFavorite
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.domain.templates.CreateTemplateFromObject
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProvider
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProviderImpl
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuViewModel
import com.anytypeio.anytype.presentation.objects.menu.ObjectSetMenuViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugGoroutinesShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.DebugTreeShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuFragment
import com.anytypeio.anytype.ui.sets.ObjectSetMenuFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map


@Subcomponent(modules = [ObjectMenuModuleBase::class, ObjectMenuModule::class])
@PerDialog
interface ObjectMenuComponent {
    @Subcomponent.Builder
    interface Builder {
        fun base(module: ObjectMenuModuleBase): Builder
        fun module(module: ObjectMenuModule): Builder
        fun build(): ObjectMenuComponent
    }

    fun inject(fragment: ObjectMenuFragment)
}

@Subcomponent(modules = [ObjectMenuModuleBase::class, ObjectSetMenuModule::class])
@PerDialog
interface ObjectSetMenuComponent {
    @Subcomponent.Builder
    interface Builder {
        fun base(module: ObjectMenuModuleBase): Builder
        fun module(module: ObjectSetMenuModule): Builder
        fun build(): ObjectSetMenuComponent
    }

    fun inject(fragment: ObjectSetMenuFragment)
}

@Module
object ObjectMenuModuleBase {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddBackLinkToObject(
        openPage: OpenPage,
        createBlock: CreateBlock,
        closeBlock: CloseBlock,
        dispatchers: AppCoroutineDispatchers
    ): AddBackLinkToObject = AddBackLinkToObject(openPage, createBlock, closeBlock, dispatchers)
}

@Module
object ObjectMenuModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        duplicateObject: DuplicateObject,
        debugTreeShareDownloader: DebugTreeShareDownloader,
        addBackLinkToObject: AddBackLinkToObject,
        urlBuilder: UrlBuilder,
        storage: Editor.Storage,
        analytics: Analytics,
        dispatcher: Dispatcher<Payload>,
        updateFields: UpdateFields,
        featureToggles: FeatureToggles,
        delegator: Delegator<Action>,
        addObjectToCollection: AddObjectToCollection,
        createTemplateFromObject: CreateTemplateFromObject,
        setObjectDetails: SetObjectDetails,
        debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader,
        createWidget: CreateWidget,
        spaceManager: SpaceManager,
        deepLinkResolver: DeepLinkResolver,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        payloadDelegator: PayloadDelegator,
        setObjectListIsFavorite: SetObjectListIsFavorite,
        setObjectIsArchived: SetObjectListIsArchived,
        fieldParser: FieldParser,
        spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        getSpaceInviteLink: GetSpaceInviteLink,
        addToFeaturedRelations: AddToFeaturedRelations,
        removeFromFeaturedRelations: RemoveFromFeaturedRelations
    ): ObjectMenuViewModel.Factory = ObjectMenuViewModel.Factory(
        setObjectIsArchived = setObjectIsArchived,
        duplicateObject = duplicateObject,
        debugTreeShareDownloader = debugTreeShareDownloader,
        addBackLinkToObject = addBackLinkToObject,
        urlBuilder = urlBuilder,
        storage = storage,
        analytics = analytics,
        dispatcher = dispatcher,
        updateFields = updateFields,
        delegator = delegator,
        menuOptionsProvider = createMenuOptionsProvider(storage),
        addObjectToCollection = addObjectToCollection,
        createTemplateFromObject = createTemplateFromObject,
        setObjectDetails = setObjectDetails,
        debugGoroutinesShareDownloader = debugGoroutinesShareDownloader,
        createWidget = createWidget,
        spaceManager = spaceManager,
        deepLinkResolver = deepLinkResolver,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        payloadDelegator = payloadDelegator,
        setObjectListIsFavorite = setObjectListIsFavorite,
        fieldParser = fieldParser,
        getSpaceInviteLink = getSpaceInviteLink,
        spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
        addToFeaturedRelations = addToFeaturedRelations,
        removeFromFeaturedRelations = removeFromFeaturedRelations
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideSetObjectDetails(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(
        repo,
        dispatchers
    )

    @JvmStatic
    private fun createMenuOptionsProvider(storage: Editor.Storage): ObjectMenuOptionsProvider =
        ObjectMenuOptionsProviderImpl(
            objectViewDetailsFlow = storage.details.stream(),
            restrictions = storage.objectRestrictions.stream()
        )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddObjectToCollection(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): AddObjectToCollection = AddObjectToCollection(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun debugGoRoutines(
        repo: BlockRepository,
        context: Context,
        fileProvider: UriFileProvider,
        dispatchers: AppCoroutineDispatchers
    ): DebugGoroutinesShareDownloader = DebugGoroutinesShareDownloader(
        repo = repo,
        context = context.applicationContext,
        uriFileProvider = fileProvider,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideDeeplinkResolver() : DeepLinkResolver = DefaultDeepLinkResolver

    @JvmStatic
    @Provides
    @PerDialog
    fun provideFavoriteUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsFavorite = SetObjectListIsFavorite(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerDialog
    fun provideArchiveUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsArchived = SetObjectListIsArchived(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerDialog
    fun addToFeaturedRelations(repo: BlockRepository): AddToFeaturedRelations =
        AddToFeaturedRelations(repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun removeFromFeaturedRelations(repo: BlockRepository): RemoveFromFeaturedRelations =
        RemoveFromFeaturedRelations(repo)
}

@Module
object ObjectSetMenuModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        addBackLinkToObject: AddBackLinkToObject,
        duplicateObject: DuplicateObject,
        delegator: Delegator<Action>,
        urlBuilder: UrlBuilder,
        analytics: Analytics,
        state: MutableStateFlow<ObjectState>,
        featureToggles: FeatureToggles,
        dispatcher: Dispatcher<Payload>,
        addObjectToCollection: AddObjectToCollection,
        debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader,
        createWidget: CreateWidget,
        spaceManager: SpaceManager,
        deepLinkResolver: DeepLinkResolver,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        payloadDelegator: PayloadDelegator,
        setObjectListIsFavorite: SetObjectListIsFavorite,
        setObjectIsArchived: SetObjectListIsArchived,
        fieldParser: FieldParser,
        getSpaceInviteLink: GetSpaceInviteLink,
        spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        addToFeaturedRelations: AddToFeaturedRelations,
        removeFromFeaturedRelations: RemoveFromFeaturedRelations
    ): ObjectSetMenuViewModel.Factory = ObjectSetMenuViewModel.Factory(
        setObjectListIsArchived = setObjectIsArchived,
        addBackLinkToObject = addBackLinkToObject,
        duplicateObject = duplicateObject,
        urlBuilder = urlBuilder,
        delegator = delegator,
        analytics = analytics,
        objectState = state,
        dispatcher = dispatcher,
        menuOptionsProvider = createMenuOptionsProvider(state),
        addObjectToCollection = addObjectToCollection,
        debugGoroutinesShareDownloader = debugGoroutinesShareDownloader,
        createWidget = createWidget,
        spaceManager = spaceManager,
        deepLinkResolver = deepLinkResolver,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        payloadDelegator = payloadDelegator,
        setObjectListIsFavorite = setObjectListIsFavorite,
        fieldParser = fieldParser,
        getSpaceInviteLink = getSpaceInviteLink,
        spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
        addToFeaturedRelations = addToFeaturedRelations,
        removeFromFeaturedRelations = removeFromFeaturedRelations
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideOpenPage(
        repo: BlockRepository,
        settings: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): OpenPage = OpenPage(
        repo = repo,
        settings = settings,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideCreateBlock(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateBlock = CreateBlock(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun debugGoRoutines(
        repo: BlockRepository,
        context: Context,
        fileProvider: UriFileProvider,
        dispatchers: AppCoroutineDispatchers
    ): DebugGoroutinesShareDownloader = DebugGoroutinesShareDownloader(
        repo = repo,
        context = context.applicationContext,
        uriFileProvider = fileProvider,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideDeeplinkResolver() : DeepLinkResolver = DefaultDeepLinkResolver

    @JvmStatic
    @Provides
    @PerDialog
    fun provideFavoriteUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsFavorite = SetObjectListIsFavorite(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    private fun createMenuOptionsProvider(
        state: StateFlow<ObjectState>,
    ): ObjectMenuOptionsProvider {
        return when (val currentState = state.value) {
            is ObjectState.DataView -> ObjectMenuOptionsProviderImpl(
                objectViewDetailsFlow = state.map { currentState.details }.distinctUntilChanged(),
                restrictions = state.map { currentState.objectRestrictions }.distinctUntilChanged(),
            )
            else -> ObjectMenuOptionsProviderImpl(
                objectViewDetailsFlow = emptyFlow(),
                restrictions = emptyFlow(),
            )
        }
    }

    @JvmStatic
    @Provides
    @PerDialog
    fun addToFeaturedRelations(repo: BlockRepository): AddToFeaturedRelations =
        AddToFeaturedRelations(repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun removeFromFeaturedRelations(repo: BlockRepository): RemoveFromFeaturedRelations =
        RemoveFromFeaturedRelations(repo)
}