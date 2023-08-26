package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.templates.CreateTemplateFromObject
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProvider
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProviderImpl
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuViewModel
import com.anytypeio.anytype.presentation.objects.menu.ObjectSetMenuViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugTreeShareDownloader
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
    fun provideAddToFavoriteUseCase(
        repo: BlockRepository
    ): AddToFavorite = AddToFavorite(repo = repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun provideRemoveFromFavoriteUseCase(
        repo: BlockRepository
    ): RemoveFromFavorite = RemoveFromFavorite(repo = repo)

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
        setObjectIsArchived: SetObjectIsArchived,
        duplicateObject: DuplicateObject,
        debugTreeShareDownloader: DebugTreeShareDownloader,
        addToFavorite: AddToFavorite,
        removeFromFavorite: RemoveFromFavorite,
        addBackLinkToObject: AddBackLinkToObject,
        urlBuilder: UrlBuilder,
        storage: Editor.Storage,
        analytics: Analytics,
        dispatcher: Dispatcher<Payload>,
        updateFields: UpdateFields,
        featureToggles: FeatureToggles,
        delegator: Delegator<Action>,
        addObjectToCollection: AddObjectToCollection,
        createTemplateFromObject: CreateTemplateFromObject
    ): ObjectMenuViewModel.Factory = ObjectMenuViewModel.Factory(
        setObjectIsArchived = setObjectIsArchived,
        duplicateObject = duplicateObject,
        debugTreeShareDownloader = debugTreeShareDownloader,
        addToFavorite = addToFavorite,
        removeFromFavorite = removeFromFavorite,
        addBackLinkToObject = addBackLinkToObject,
        urlBuilder = urlBuilder,
        storage = storage,
        analytics = analytics,
        dispatcher = dispatcher,
        updateFields = updateFields,
        delegator = delegator,
        menuOptionsProvider = createMenuOptionsProvider(storage, featureToggles),
        addObjectToCollection = addObjectToCollection,
        createTemplateFromObject = createTemplateFromObject
    )

    @JvmStatic
    private fun createMenuOptionsProvider(storage: Editor.Storage, featureToggles: FeatureToggles) =
        ObjectMenuOptionsProviderImpl(
            details = storage.details.stream().map { it.details },
            restrictions = storage.objectRestrictions.stream(),
            featureToggles = featureToggles
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
}

@Module
object ObjectSetMenuModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        setObjectIsArchived: SetObjectIsArchived,
        addToFavorite: AddToFavorite,
        removeFromFavorite: RemoveFromFavorite,
        addBackLinkToObject: AddBackLinkToObject,
        duplicateObject: DuplicateObject,
        delegator: Delegator<Action>,
        urlBuilder: UrlBuilder,
        analytics: Analytics,
        state: MutableStateFlow<ObjectState>,
        featureToggles: FeatureToggles,
        dispatcher: Dispatcher<Payload>,
        addObjectToCollection: AddObjectToCollection
    ): ObjectSetMenuViewModel.Factory = ObjectSetMenuViewModel.Factory(
        setObjectIsArchived = setObjectIsArchived,
        addToFavorite = addToFavorite,
        removeFromFavorite = removeFromFavorite,
        addBackLinkToObject = addBackLinkToObject,
        duplicateObject = duplicateObject,
        urlBuilder = urlBuilder,
        delegator = delegator,
        analytics = analytics,
        objectState = state,
        dispatcher = dispatcher,
        menuOptionsProvider = createMenuOptionsProvider(state, featureToggles),
        addObjectToCollection = addObjectToCollection
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideOpenPage(
        repo: BlockRepository,
        auth: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): OpenPage = OpenPage(repo, auth, dispatchers)

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
    private fun createMenuOptionsProvider(
        state: StateFlow<ObjectState>,
        featureToggles: FeatureToggles
    ): ObjectMenuOptionsProvider {
        return when (val currentState = state.value) {
            is ObjectState.DataView -> ObjectMenuOptionsProviderImpl(
                details = state.map { currentState.details }.distinctUntilChanged(),
                restrictions = state.map { currentState.objectRestrictions }.distinctUntilChanged(),
                featureToggles = featureToggles
            )
            else -> ObjectMenuOptionsProviderImpl(
                details = emptyFlow(),
                restrictions = emptyFlow(),
                featureToggles = featureToggles
            )
        }
    }
}