package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.DataView.Sort
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.collections.MockCollection
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.domain.collections.RemoveObjectFromCollection
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.presentation.home.UserPermissionProviderStub
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.sets.updateFormatForSubscription
import com.anytypeio.anytype.presentation.sets.subscription.updateWithRelationFormat
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
open class ObjectSetViewModelTestSetup {

    val root: Id = "context-${RandomString.make()}"
    val defaultSpace = "DEFAULT_SPACE_ID"

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var openObjectSet: OpenObjectSet

    @Mock
    lateinit var closeObject: CloseObject

    @Mock
    lateinit var updateText: UpdateText

    @Mock
    lateinit var createDataViewObject: CreateDataViewObject

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var downloadUnsplashImage: DownloadUnsplashImage

    @Mock
    lateinit var setDocCoverImage: SetDocCoverImage

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var objectToCollection: ConvertObjectToCollection

    @Mock
    lateinit var setQueryToObjectSet: SetQueryToObjectSet

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    //@Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var subscriptionEventChannel: SubscriptionEventChannel

    @Mock
    lateinit var removeObjectFromCollection: RemoveObjectFromCollection

    @Mock
    lateinit var addObjectToCollection: AddObjectToCollection

    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var getObjectTypes: GetObjectTypes

    @Mock
    lateinit var getDefaultObjectType: GetDefaultObjectType

    @Mock
    lateinit var duplicateObjects: DuplicateObjects

    @Mock
    lateinit var setObjectListIsArchived: SetObjectListIsArchived

    @Mock
    lateinit var templatesContainer: ObjectTypeTemplatesContainer

    @Mock
    lateinit var viewerDelegate: ViewerDelegate

    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var createTemplate: CreateTemplate

    @Mock
    lateinit var getNetworkMode: GetNetworkMode

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    @Mock
    lateinit var spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider

    @Mock
    lateinit var localeProvider : LocaleProvider

    lateinit var fieldParser: FieldParser

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var spacedViews: SpaceViewSubscriptionContainer

    @Mock
    lateinit var deepLinkResolver: DeepLinkResolver

    var permissions: UserPermissionProvider = UserPermissionProviderStub()

    lateinit var spaceConfig: Config

    var stateReducer = DefaultObjectStateReducer()

    lateinit var dataViewSubscriptionContainer: DataViewSubscriptionContainer
    lateinit var dataViewSubscription: DataViewSubscription

    val dispatcher = Dispatcher.Default<Payload>()
    val delegator = Delegator.Default<Action>()
    val session = ObjectSetSession()
    val paginator = ObjectSetPaginator()

    val objectStore: ObjectStore = DefaultObjectStore()
    protected val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    val database = ObjectSetDatabase(objectStore)

    val urlBuilder: UrlBuilder get() = UrlBuilder(gateway)

    val defaultObjectPageType = TypeKey(MockDataFactory.randomString())
    val defaultObjectPageTypeName = MockDataFactory.randomString()

    lateinit var dispatchers: AppCoroutineDispatchers

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    fun proceedWithDefaultBeforeTestStubbing() = runTest {
        repo = mock(verboseLogging = true)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        spaceConfig = StubConfig()
        spaceManager = SpaceManager.Impl(
            repo = repo,
            dispatchers = dispatchers,
            logger = logger
        )
        stubSpaceManager(defaultSpace)
        dataViewSubscriptionContainer = DataViewSubscriptionContainer(
            repo = repo,
            channel = subscriptionEventChannel,
            store = objectStore,
            dispatchers = dispatchers
        )
        dataViewSubscription = DefaultDataViewSubscription(dataViewSubscriptionContainer)
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        stubLocalProvider()
        fieldParser = FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
        stubGetDefaultPageType()
        stubObservePermissions()
        stubAnalyticSpaceHelperDelegate()
        stubInterceptEvents()
        stubNetworkMode()
        givenNetworkNodeMocked()
        stubInterceptThreadStatus()
        val spaceView = StubSpaceView(
            id = defaultSpace,
            spaceUxType = SpaceUxType.DATA
        )
        spacedViews.stub {
            onBlocking { get(space = SpaceId(defaultSpace)) } doReturn spaceView
        }
    }

    fun givenViewModel(): ObjectSetViewModel {
        return ObjectSetViewModel(
            openObjectSet = openObjectSet,
            closeObject = closeObject,
            updateText = updateText,
            interceptEvents = interceptEvents,
            createDataViewObject = createDataViewObject,
            dispatcher = dispatcher,
            delegator = delegator,
            coverImageHashProvider = coverImageHashProvider,
            urlBuilder = urlBuilder,
            session = session,
            analytics = analytics,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            createObject = createObject,
            setObjectDetails = setObjectDetails,
            paginator = paginator,
            database = database,
            dataViewSubscriptionContainer = dataViewSubscriptionContainer,
            storeOfRelations = storeOfRelations,
            stateReducer = stateReducer,
            dataViewSubscription = dataViewSubscription,
            objectStore = objectStore,
            addObjectToCollection = addObjectToCollection,
            objectToCollection = objectToCollection,
            setQueryToObjectSet = setQueryToObjectSet,
            storeOfObjectTypes = storeOfObjectTypes,
            templatesContainer = templatesContainer,
            setObjectListIsArchived = setObjectListIsArchived,
            duplicateObjects = duplicateObjects,
            viewerDelegate = viewerDelegate,
            spaceManager = spaceManager,
            createTemplate = createTemplate,
            getObjectTypes = getObjectTypes,
            dateProvider = dateProvider,
            vmParams = ObjectSetViewModel.Params(
                ctx = root,
                space = SpaceId(defaultSpace)
            ),
            permissions = permissions,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
            fieldParser = fieldParser,
            spaceViews = spacedViews,
            deepLinkResolver = deepLinkResolver,
            removeObjectFromCollection = removeObjectFromCollection
        )
    }

    fun stubInterceptEvents(
        params: InterceptEvents.Params = InterceptEvents.Params(context = root),
        flow: Flow<List<Event>> = flowOf()
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
        }
    }

    fun stubInterceptThreadStatus() {
        spaceSyncAndP2PStatusProvider.stub {
            onBlocking { observe() } doReturn emptyFlow()
        }
    }

    fun stubOpenObject(
        doc: List<Block> = emptyList(),
        details: ObjectViewDetails = ObjectViewDetails.EMPTY,
        additionalEvents: List<Event> = emptyList(),
        dataViewRestrictions: List<DataViewRestrictions> = emptyList()
    ) {
        openObjectSet.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                blocks = doc,
                                details = details.details,
                                dataViewRestrictions = dataViewRestrictions
                            )
                        ) + additionalEvents
                    )
                )
            )
        }
    }

    fun stubAddObjectToCollection() {
        addObjectToCollection.stub {
            onBlocking {
                async(any())
            } doReturn Resultat.success(Payload("", emptyList()))
        }
    }

    fun stubCloseBlock() {
        closeObject.stub {
            onBlocking {
                async(any())
            } doReturn Resultat.success(Unit)
        }
    }

    suspend fun stubSpaceManager(space: Id) {
        repo.stub {
            onBlocking { spaceOpen(space, false) } doReturn spaceConfig.copy(
                space = space
            )
            spaceManager.set(space)
        }
    }

    suspend fun stubSubscriptionResults(
        subscription: Id = MockDataFactory.randomString(),
        spaceId: Id,
        collection: Id? = null,
        objects: List<ObjectWrapper.Basic> = emptyList(),
        dependencies: List<ObjectWrapper.Basic> = listOf(),
        dvFilters: List<Block.Content.DataView.Filter> = emptyList(),
        dvSorts: List<Sort> = listOf(
            DVSort(
                relationKey = Relations.CREATED_DATE,
                type = Sort.Type.DESC,
                relationFormat = RelationFormat.DATE,
                includeTime = true
            )
        ),
        keys: List<Key> = emptyList(),
        sources: List<Id> = emptyList(),
        dvRelationLinks: List<RelationLink> = emptyList()
    ) {
        val dvKeys = (ObjectSearchConstants.defaultDataViewKeys + keys).toSet().toList()
        doReturn(
            SearchResult(
                results = objects,
                dependencies = dependencies
            )
        ).`when`(repo).searchObjectsWithSubscription(
            space = SpaceId(spaceId),
            subscription = subscription,
            collection = collection,
            filters = dvFilters.updateFormatForSubscription(storeOfRelations) + ObjectSearchConstants.defaultDataViewFilters(),
            sorts = dvSorts.updateWithRelationFormat(storeOfRelations),
            keys = dvKeys,
            source = sources,
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = 0L,
            afterId = null,
            beforeId = null,
            noDepSubscription = null,
            ignoreWorkspace = null
        )
        subscriptionEventChannel.stub {
            on {
                subscribe(listOf(subscription))
            } doReturn flow {
                emit(listOf())
            }
        }
    }

    suspend fun stubStoreOfRelations(mockObjectCollection: MockCollection) {
        storeOfRelations.merge(
            listOf(
                mockObjectCollection.relationObject1,
                mockObjectCollection.relationObject2,
                mockObjectCollection.relationObject3,
                mockObjectCollection.relationObject4,
                mockObjectCollection.relationObject5,
                mockObjectCollection.relationObject6,
                mockObjectCollection.createdDateRelation
            )
        )
    }

    suspend fun stubStoreOfRelations(mockObjectSet: MockSet) {
        storeOfRelations.merge(
            listOf(
                mockObjectSet.relationObject1,
                mockObjectSet.relationObject2,
                mockObjectSet.relationObject3,
                mockObjectSet.relationObject4,
                mockObjectSet.relationObject5,
                mockObjectSet.createdDateRelation
            )
        )
    }

    suspend fun stubStoreOfObjectTypes(id: String = "", map: Map<String, Any?> = emptyMap()) {
        storeOfObjectTypes.set(id, map)
    }

    fun stubGetDefaultPageType(
        type: TypeKey = defaultObjectPageType,
        name: String = defaultObjectPageTypeName,
        id: TypeId = TypeId(MockDataFactory.randomString()),
        template: Id? = null
    ) {
        getDefaultObjectType.stub {
            onBlocking { run(SpaceId(spaceConfig.space)) } doReturn GetDefaultObjectType.Response(
                type = type,
                name = name,
                id = id,
                defaultTemplate = template
            )
        }
    }

    fun stubTemplatesForTemplatesContainer(
        type: String = MockDataFactory.randomString(),
        templates: List<ObjectWrapper.Basic> = emptyList(),
        subId: Id = MockDataFactory.randomString()
    ) {
        templatesContainer.stub {
            onBlocking {
                subscribeToTemplates(
                    type = type,
                    space = SpaceId(defaultSpace),
                    subscription = subId
                )
            }.thenReturn(flowOf(templates))
        }
    }

    fun stubCreateDataViewObject(
        objectId: Id = MockDataFactory.randomString(),
        // TODO pass key here, not id
        objectType: Key = MockDataFactory.randomString()
    ) {
        createDataViewObject.stub {
            onBlocking { async(any()) }.thenReturn(
                Resultat.success(
                    CreateDataViewObject.Result(
                        objectId = objectId,
                        objectType = TypeKey(objectType),
                        struct = mapOf<Id, Any?>("spaceId" to "spaceId")
                    )
                )
            )
        }
    }

    fun stubNetworkMode() {
        getNetworkMode.stub {
            onBlocking { run(Unit) } doReturn NetworkModeConfig()
        }
    }

    fun stubObservePermissions(
        permission: SpaceMemberPermissions = SpaceMemberPermissions.OWNER
    ) {
        (permissions as UserPermissionProviderStub).stubObserve(
            SpaceId(""), permission
        )
    }

    fun stubObjectToCollection() {
        objectToCollection.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
    }

    fun givenNetworkNodeMocked() {
        getNetworkMode.stub {
            onBlocking {
                run(any())
            } doReturn NetworkModeConfig()
        }
    }

    fun stubAnalyticSpaceHelperDelegate() {
        Mockito.`when`(analyticSpaceHelperDelegate.provideParams(defaultSpace))
            .thenReturn(AnalyticSpaceHelperDelegate.Params.EMPTY)
    }

    fun stubLocalProvider() {
        localeProvider.stub {
            on { locale() } doReturn Locale.getDefault()
            on { language() } doReturn "en"
        }
    }
}