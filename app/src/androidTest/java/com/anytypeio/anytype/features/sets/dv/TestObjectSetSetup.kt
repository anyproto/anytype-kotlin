package com.anytypeio.anytype.features.sets.dv

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.device.providers.DateProviderImpl
import com.anytypeio.anytype.domain.auth.interactor.ClearLastOpenedObject
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.SetDataViewQuery
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

abstract class TestObjectSetSetup {

    private lateinit var openObjectSet: OpenObjectSet
    private lateinit var updateDataViewViewer: UpdateDataViewViewer
    private lateinit var setObjectDetails: UpdateDetail
    private lateinit var updateText: UpdateText
    private lateinit var createDataViewObject: CreateDataViewObject
    private lateinit var closeBlock: CloseBlock
    private lateinit var setDocCoverImage: SetDocCoverImage
    private lateinit var downloadUnsplashImage: DownloadUnsplashImage
    private lateinit var setDataViewQuery: SetDataViewQuery

    private val defaultSpace: Id = MockDataFactory.randomString()

    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var permissions: UserPermissionProvider

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var unsplashRepo: UnsplashRepository

    @Mock
    lateinit var auth: AuthRepository

    @Mock
    lateinit var settingsRepository: UserSettingsRepository

    @Mock
    lateinit var userSettingsRepository: UserSettingsRepository

    @Mock
    lateinit var gateway: Gateway
    @Mock
    lateinit var interceptEvents: InterceptEvents
    @Mock
    lateinit var spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider
    @Mock
    lateinit var subscriptionEventChannel: SubscriptionEventChannel
    @Mock
    lateinit var analytics: Analytics
    @Mock
    lateinit var cancelSearchSubscription: CancelSearchSubscription
    @Mock
    lateinit var convertObjectToCollection: ConvertObjectToCollection
    @Mock
    lateinit var setQueryToObjectSet: SetQueryToObjectSet

    @Mock
    lateinit var addObjectToCollection: AddObjectToCollection

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var duplicateObjects: DuplicateObjects

    @Mock
    lateinit var setObjectListIsArchived: SetObjectListIsArchived

    @Mock
    lateinit var templatesContainer: ObjectTypeTemplatesContainer

    @Mock
    lateinit var viewerDelegate: ViewerDelegate

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var configStorage: ConfigStorage

    @Mock
    lateinit var getObjectTypes: GetObjectTypes

    @Mock
    lateinit var createTemplate: CreateTemplate

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var appCoroutineDispatchers: AppCoroutineDispatchers

    @Mock
    lateinit var getNetworkMode: GetNetworkMode

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    @Mock
    lateinit var clearLastOpenedObject: ClearLastOpenedObject

    private lateinit var getTemplates: GetTemplates
    private lateinit var getDefaultObjectType: GetDefaultObjectType

    private val session = ObjectSetSession()
    private val dispatcher: Dispatcher<Payload> = Dispatcher.Default()
    private val paginator = ObjectSetPaginator()
    private val store: ObjectStore = DefaultObjectStore()
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()

    private lateinit var database: ObjectSetDatabase
    private lateinit var dataViewSubscriptionContainer: DataViewSubscriptionContainer

    val ctx : Id = MockDataFactory.randomUuid()

    abstract val title : Block

    val header get() = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    val defaultDetails = Block.Details(
        mapOf(
            ctx to Block.Fields(
                mapOf(
                    "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
                )
            )
        )
    )

    private val objectStore: ObjectStore = DefaultObjectStore()

    private val delegator = Delegator.Default<Action>()

    @Mock
    lateinit var localeProvider : LocaleProvider

    private val dateProvider = DateProviderImpl(ZoneId.systemDefault(), localeProvider)

    open fun setup() {
        MockitoAnnotations.openMocks(this)

        val dispatchers = AppCoroutineDispatchers(
            io = StandardTestDispatcher(),
            main = StandardTestDispatcher(),
            computation = StandardTestDispatcher()
        )

        setDataViewQuery = SetDataViewQuery(repo)
        updateText = UpdateText(repo)
        openObjectSet = OpenObjectSet(repo, userSettingsRepository)
        getDefaultObjectType = GetDefaultObjectType(
            userSettingsRepository = userSettingsRepository,
            blockRepository = repo,
            dispatchers = dispatchers
        )
        createDataViewObject = CreateDataViewObject(
            repo = repo,
            dispatchers = dispatchers,
            spaceManager = spaceManager
        )
        setObjectDetails = UpdateDetail(repo)
        updateDataViewViewer = UpdateDataViewViewer(repo, dispatchers)
        closeBlock = CloseBlock(repo, dispatchers)
        urlBuilder = UrlBuilder(gateway)
        downloadUnsplashImage = DownloadUnsplashImage(unsplashRepo)
        setDocCoverImage = SetDocCoverImage(repo)
        getTemplates = GetTemplates(
            repo = repo,
            dispatchers = dispatchers,
            spaceManager = spaceManager
        )
        database = ObjectSetDatabase(store)
        dataViewSubscriptionContainer = DataViewSubscriptionContainer(
            repo = repo,
            store = store,
            channel = subscriptionEventChannel,
            dispatchers = dispatchers
        )
        TestObjectSetFragment.testVmFactory = ObjectSetViewModelFactory(
            openObjectSet = openObjectSet,
            closeBlock = closeBlock,
            interceptEvents = interceptEvents,
            createDataViewObject = createDataViewObject,
            setObjectDetails = setObjectDetails,
            updateText = updateText,
            urlBuilder = urlBuilder,
            coverImageHashProvider = coverImageHashProvider,
            session = session,
            dispatcher = dispatcher,
            analytics = analytics,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            delegator = delegator,
            createObject = createObject,
            paginator = paginator,
            database = database,
            dataViewSubscriptionContainer = dataViewSubscriptionContainer,
            storeOfRelations = storeOfRelations,
            objectStateReducer = DefaultObjectStateReducer(),
            dataViewSubscription = DefaultDataViewSubscription(dataViewSubscriptionContainer),
            objectToCollection = convertObjectToCollection,
            setQueryToObjectSet = setQueryToObjectSet,
            objectStore = objectStore,
            addObjectToCollection = addObjectToCollection,
            storeOfObjectTypes = storeOfObjectTypes,
            templatesContainer = templatesContainer,
            setObjectListIsArchived = setObjectListIsArchived,
            duplicateObjects = duplicateObjects,
            viewerDelegate = viewerDelegate,
            spaceManager = spaceManager,
            createTemplate = createTemplate,
            getObjectTypes = getObjectTypes,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            dispatchers = appCoroutineDispatchers,
            dateProvider = dateProvider,
            params = ObjectSetViewModel.Params(
                ctx = ctx,
                space = SpaceId(defaultSpace)
            ),
            permissions = permissions,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
            clearLastOpenedObject = clearLastOpenedObject
        )

        Mockito.`when`(localeProvider.locale()).thenReturn(Locale.getDefault())
    }

    fun stubInterceptEvents() {
        interceptEvents.stub {
            onBlocking { build(any()) } doReturn emptyFlow()
        }
    }

    fun stubInterceptThreadStatus() {
        spaceSyncAndP2PStatusProvider.stub {
            onBlocking { observe() } doReturn emptyFlow()
        }
    }

    fun stubOpenObjectSet(
        set: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList()
    ) {
        repo.stub {
            onBlocking { openObjectSet(ctx, SpaceId(defaultSpace)) } doReturn Result.Success(
                Payload(
                    context = ctx,
                    events = listOf(
                        Event.Command.ShowObject(
                            context = ctx,
                            root = ctx,
                            details = details,
                            blocks = set,
                            relations = relations
                        )
                    )
                )
            )
        }
    }

    fun stubOpenObjectSetWithRecord(
        set: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList()
    ) {
        repo.stub {
            onBlocking { openObjectSet(ctx, SpaceId(defaultSpace)) } doReturn Result.Success(
                Payload(
                    context = ctx,
                    events = listOf(
                        Event.Command.ShowObject(
                            context = ctx,
                            root = ctx,
                            details = details,
                            blocks = set,
                            relations = relations,
                        )
                    )
                )
            )
        }
    }

    fun stubSearchWithSubscription() {
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    space = any(),
                    subscription = any(),
                    filters = any(),
                    sorts = any(),
                    afterId = any(),
                    beforeId = any(),
                    source = any(),
                    keys = any(),
                    limit = any(),
                    offset = any(),
                    ignoreWorkspace = any(),
                    noDepSubscription = any(),
                    collection = any()
                )
            } doReturn SearchResult(
                results = emptyList(),
                dependencies = emptyList(),
                counter = null
            )
        }
    }

    fun stubSubscriptionEventChannel(
        flow: Flow<List<SubscriptionEvent>> = emptyFlow()
    ) {
        subscriptionEventChannel.stub {
            onBlocking {
                subscribe(listOf(ctx))
            } doReturn flow
        }
    }

    fun launchFragment(args: Bundle): FragmentScenario<TestObjectSetFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}