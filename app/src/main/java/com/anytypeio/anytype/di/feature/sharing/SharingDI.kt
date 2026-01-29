package com.anytypeio.anytype.di.feature.sharing

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreateObjectFromUrl
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.sharing.SharingViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

/**
 * DI Component for the redesigned sharing extension.
 * Provides dependencies for SharingViewModel which handles three flows:
 * - Flow 1: Chat Space (direct message sending)
 * - Flow 2: Data Space without chat (object creation)
 * - Flow 3: Data Space with chat (hybrid)
 */
@Component(
    dependencies = [SharingDependencies::class],
    modules = [
        SharingModule::class,
        SharingModule.Declarations::class
    ]
)
@PerModal
interface SharingComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: SharingDependencies): SharingComponent
    }

    /**
     * Provides the ViewModel factory for creating SharingViewModel instances.
     * Used by MainActivity's Compose overlay for the pure Compose modal sheet.
     */
    fun viewModelFactory(): ViewModelProvider.Factory
}

@Module
object SharingModule {

    @Provides
    @PerModal
    fun provideCreateBookmarkObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateBookmarkObject = CreateBookmarkObject(repo)

    @Provides
    @PerModal
    fun provideCreatePrefilledNote(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreatePrefilledNote = CreatePrefilledNote(repo, dispatchers)

    @Provides
    @PerModal
    fun provideCreateObjectFromUrl(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateObjectFromUrl = CreateObjectFromUrl(repo, dispatchers)

    @Provides
    @PerModal
    fun provideAddChatMessage(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): AddChatMessage = AddChatMessage(repo, dispatchers)

    @Provides
    @PerModal
    fun provideUploadFile(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): UploadFile = UploadFile(repo, dispatchers)

    @Provides
    @PerModal
    fun provideSearchObjects(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo)

    @Provides
    @PerModal
    fun provideOpenPage(
        repo: BlockRepository,
        settings: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): OpenPage = OpenPage(
        repo = repo,
        settings = settings,
        dispatchers = dispatchers
    )

    @Provides
    @PerModal
    fun provideCloseObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CloseObject = CloseObject(
        repo = repo,
        dispatchers = dispatchers
    )

    @Provides
    @PerModal
    fun provideCreateBlock(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateBlock = CreateBlock(
        repo = repo,
        dispatchers = dispatchers
    )

    @Provides
    @PerModal
    fun provideAddBackLinkToObject(
        openPage: OpenPage,
        createBlock: CreateBlock,
        closeObject: CloseObject,
        dispatchers: AppCoroutineDispatchers
    ): AddBackLinkToObject = AddBackLinkToObject(
        openPage = openPage,
        createBlock = createBlock,
        closeObject = closeObject,
        dispatchers = dispatchers
    )

    @Provides
    @PerModal
    fun provideAddObjectToCollection(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): AddObjectToCollection = AddObjectToCollection(repo, dispatchers)

    @Module
    interface Declarations {
        @PerModal
        @Binds
        fun factory(factory: SharingViewModel.Factory): ViewModelProvider.Factory
    }
}

/**
 * Dependencies required by the SharingComponent.
 * These are provided by the parent component (MainComponent).
 */
interface SharingDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun spaceManager(): SpaceManager
    fun dispatchers(): AppCoroutineDispatchers
    fun urlBuilder(): UrlBuilder
    fun awaitAccountStartedManager(): AwaitAccountStartManager
    fun analytics(): Analytics
    fun fileSharer(): FileSharer
    fun permissions(): UserPermissionProvider
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun fieldParser(): FieldParser
    fun userSettingsRepository(): UserSettingsRepository
}
