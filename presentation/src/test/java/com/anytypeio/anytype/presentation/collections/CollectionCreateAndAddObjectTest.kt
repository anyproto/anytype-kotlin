
    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(false)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockObjectCollection = MockCollection(context = root, space = defaultSpace)
        proceedWithDefaultBeforeTestStubbing()
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
            logger = mock()
        )
        dataViewSubscriptionContainer = DataViewSubscriptionContainer(
            repo = repo,
            channel = subscriptionEventChannel,
            store = objectStore,
            dispatchers = dispatchers
        )
        dataViewSubscription = DefaultDataViewSubscription(dataViewSubscriptionContainer)
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        viewModel = ObjectSetViewModel(
            openObjectSet = openObjectSet,
            closeObject = closeObject,
            updateText = updateText,
            interceptEvents = interceptEvents,
            createDataViewObject = CreateDataViewObject(
                repo = repo,
                spaceManager = spaceManager,
                dispatchers = dispatchers
            ),
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
            removeObjectFromCollection = removeObjectFromCollection,
            setDataViewProperties = setDataViewProperties,
            emojiProvider = emojiProvider,
            emojiSuggester = emojiSuggester,
            createBlock = mock(),
            stringResourceProvider = stringResourceProvider
        )
    }

    @After
    fun after() {
        rule.advanceTime()
    }

//    @Test
//    fun `create pre-populated record in Collection`() = runTest {
//
//        // SETUP
//
//        val filters = mockObjectCollection.filters
//
//        stubSpaceManager(mockObjectCollection.spaceId)
//        stubInterceptEvents()
//
//        stubProfileIcon()
//        stubInterceptThreadStatus()
//        stubOpenObject(
//            doc = listOf(
//                mockObjectCollection.header,
//                mockObjectCollection.title,
//                mockObjectCollection.dataViewWithFilters
//            ),
//            details = mockObjectCollection.details
//        )
//        stubStoreOfRelations(mockObjectCollection)
//        stubSubscriptionResults(
//            subscription = mockObjectCollection.subscriptionId,
//            spaceId = mockObjectCollection.spaceId,
//            collection = root,
//            storeOfRelations = storeOfRelations,
//            keys = mockObjectCollection.dvKeys,
//            objects = listOf(mockObjectCollection.obj1, mockObjectCollection.obj2),
//            dvSorts = mockObjectCollection.sorts,
//            dvFilters = mockObjectCollection.filters,
//        )
//
//        // TESTING
//
//        proceedWithStartingViewModel()
//
//
//        // ASSERT DATA VIEW STATE
//
//        viewModel.currentViewer.test {
//            val first = awaitItem()
//
//            assertIs<DataViewViewState.Init>(first)
//
//            rule.advanceTime()
//
//            cancelAndIgnoreRemainingEvents()
//
//            rule.advanceTime()
//
//            val newObjectTypeKey = MockDataFactory.randomString()
//            val newObjectTemplate = MockDataFactory.randomString()
//            viewModel.proceedWithDataViewObjectCreate(
//                typeChosenBy = TypeKey(newObjectTypeKey),
//                templateId = newObjectTemplate
//            )
//
//            rule.advanceTime()
//
//            val spaceId = SpaceId(mockObjectCollection.spaceId)
//            val command = Command.CreateObject(
//                prefilled = mapOf(
//                    filters[0].relation to filters[0].value,
//                    filters[1].relation to filters[1].value
//                ),
//                internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
//                space = spaceId,
//                typeKey = TypeKey(newObjectTypeKey),
//                template = newObjectTemplate
//            )
//            verifyBlocking(repo, times(1)) {
//                createObject(command)
//            }
//        }
//    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}