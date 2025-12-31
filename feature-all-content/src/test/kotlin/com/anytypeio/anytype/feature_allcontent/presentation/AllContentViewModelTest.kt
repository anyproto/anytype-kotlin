package com.anytypeio.anytype.feature_allcontent.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.RemoveObjectsFromWorkspace
import com.anytypeio.anytype.feature_allcontent.DefaultCoroutineTestRule
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiContentItem
import com.anytypeio.anytype.feature_allcontent.models.UiItemsState
import com.anytypeio.anytype.feature_allcontent.models.UiSnackbarState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.Command.OpenTypeCreation
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.VmParams
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AllContentViewModelTest {


    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock lateinit var storeOfObjectTypes: StoreOfObjectTypes
    @Mock lateinit var urlBuilder: UrlBuilder
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    @Mock lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer
    @Mock lateinit var updateAllContentState: UpdateAllContentState
    @Mock lateinit var restoreAllContentState: RestoreAllContentState
    @Mock lateinit var searchObjects: SearchObjects
    @Mock lateinit var localeProvider: LocaleProvider
    @Mock lateinit var createObject: CreateObject
    @Mock lateinit var setObjectListIsArchived: SetObjectListIsArchived
    @Mock lateinit var removeObjectsFromWorkspace: RemoveObjectsFromWorkspace
    @Mock lateinit var userPermissionProvider: UserPermissionProvider
    @Mock lateinit var fieldParser: FieldParser
    @Mock lateinit var spaceViews: SpaceViewSubscriptionContainer

    private val testObjectWrapper = ObjectWrapper.Basic(mapOf(Relations.ID to "test-id"))

    private val params = VmParams(
        spaceId = SpaceId("test-spaceId"),
        useHistory = true
    )


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }


    @Test
    fun `should proceedWithMoveToBin`() = runTest {
        val testContentItem = UiContentItem.Item(
            id = "test-id",
            obj = testObjectWrapper,
            space = params.spaceId
        )
        val testParam = SetObjectListIsArchived.Params(
            targets = listOf(testContentItem.id),
            isArchived = true
        )
        mockInit()
        whenever(setObjectListIsArchived.async(testParam)).thenReturn(Resultat.success(Unit))
        val vm = createViewModel()

        vm.proceedWithMoveToBin(testContentItem)
        advanceUntilIdle()
        vm.uiSnackbarState.test {
            val item = awaitItem()
            assert(item is UiSnackbarState.Visible)
        }

        verifyBlocking(setObjectListIsArchived, times(1)) { async(any()) }
    }

    @Test
    fun `should proceedWithUndoMoveToBin`() = runTest {
        val testObjectId = "test-object-id"
        val testParam = SetObjectListIsArchived.Params(
            targets = listOf(testObjectId),
            isArchived = false
        )
        whenever(setObjectListIsArchived.async(testParam)).thenReturn(Resultat.success(Unit))

        mockInit()
        val vm = createViewModel()

        vm.proceedWithUndoMoveToBin(testObjectId)
        advanceUntilIdle()
        vm.uiSnackbarState.test {
            val item = awaitItem()
            assert(item is UiSnackbarState.Hidden)
        }

        verifyBlocking(setObjectListIsArchived, times(1)) { async(any()) }
    }

    @Test
    fun `should proceedWithDismissSnackbar`() = runTest {
        mockInit()
        val vm = createViewModel()
        vm.proceedWithDismissSnackbar()
        vm.uiSnackbarState.test {
            val item = awaitItem()
            assert(item is UiSnackbarState.Hidden)
        }
    }

    @Test
    fun `should change tab when tab clicked`() = runTest {
         val tab = AllContentTab.BOOKMARKS
        mockInit()
        val vm = createViewModel()
        vm.onTabClicked(tab)
        vm.uiTabsState.test {
            val item = awaitItem()
            assert(item.selectedTab == tab)
        }
    }

    @Test
    fun `should onAllContentModeClicked`() = runTest {
        var mode: AllContentMenuMode = AllContentMenuMode.AllContent()
        mockInit()
        val vm = createViewModel()

        vm.onAllContentModeClicked(mode)
        vm.uiItemsState.test {
            val item = awaitItem()
            assert(item is UiItemsState.Empty)
        }
        vm.uiTitleState.test {
            val item = awaitItem()
            assert(item is UiTitleState.AllContent)
        }

        mode = AllContentMenuMode.Unlinked()
        vm.onAllContentModeClicked(mode)
        vm.uiItemsState.test {
            val item = awaitItem()
            assert(item is UiItemsState.Empty)
        }
        vm.uiTitleState.test {
            val item = awaitItem()
            assert(item is UiTitleState.OnlyUnlinked)
        }
    }

    @Test
    fun `should sort on sort clicked`() = runTest {
        val sort = ObjectsListSort.ByName()
        mockInit()
        whenever(updateAllContentState.async(any())).thenReturn(Resultat.success(Unit))
        val vm = createViewModel()

        vm.onSortClicked(sort)
        advanceUntilIdle()
        verifyBlocking(updateAllContentState, times(1)) { async(any()) }
    }

    @Test
    fun `should navigate to bin`() = runTest {
        mockInit()
        val vm = createViewModel()
        vm.onViewBinClicked()
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.NavigateToBin)
        }
    }

    @Test
    fun `should navigate to home`() = runTest {
        mockInit()
        val vm = createViewModel()
        vm.onHomeClicked()
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.ExitToSpaceHome)
        }
    }

    @Test
    fun `should open global search`() = runTest {
        mockInit()
        val vm = createViewModel()
        vm.onGlobalSearchClicked()
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.OpenGlobalSearch)
        }
    }

    @Test
    fun `should proceed to create doc`() = runTest {
        val testResult = CreateObject.Result(
            objectId = "test-object-id",
            event = Payload(
                context = "test-object-id",
                events = listOf()
            ),
            typeKey = TypeKey("test-key"),
            obj = ObjectWrapper.Basic(mapOf(Relations.ID to "test-object-id"))
        )
        mockInit()
        whenever(createObject.async(any())).thenReturn(Resultat.success(testResult))
        whenever(analyticSpaceHelperDelegate.provideParams(any())).thenReturn(
            AnalyticSpaceHelperDelegate.Params("test-param-permission","test-param-spaceType", "test-param-spaceUxType"))
        val vm = createViewModel()
        vm.onAddDockClicked()
        advanceUntilIdle()
        verifyBlocking(createObject, times(1)) { async(any()) }

        val testObjectType = ObjectWrapper.Type(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.UNIQUE_KEY to MockDataFactory.randomUuid()
            )
        )
        clearInvocations(createObject)
        vm.onCreateObjectOfTypeClicked(testObjectType)
        advanceUntilIdle()
        verifyBlocking(createObject, times(1)) { async(any()) }
    }


    @Test
    fun `should emit correct commands`() = runTest {
        mockInit()
        val vm = createViewModel()
        vm.onBackClicked()
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.Back)
        }

        vm.onMemberButtonClicked()
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.OpenShareScreen)
        }

        vm.onTypeClicked(UiContentItem.NewType)
        vm.commands.test {
            val item = awaitItem()
            assert(item is OpenTypeCreation)
        }

        vm.onTypeClicked(UiContentItem.Type(id = "test-id", name = "test-name"))
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.OpenTypeEditing)
        }

        vm.onRelationClicked(UiContentItem.NewRelation)
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.OpenRelationCreation)
        }

        vm.onRelationClicked(
            UiContentItem.Relation(
                id = "test-id",
                name = "test-name",
                format = Relation.Format.RELATIONS
            )
        )
        vm.commands.test {
            val item = awaitItem()
            assert(item is AllContentViewModel.Command.OpenRelationEditing)
        }
    }


    private fun mockInit() {
        runBlocking {
            whenever(restoreAllContentState.run(RestoreAllContentState.Params(params.spaceId)))
                .thenReturn(RestoreAllContentState.Response.Success(isAsc = true, activeSort = "test"))
            whenever(searchObjects(any())).thenReturn(
                Either.Right(
                    listOf(testObjectWrapper)
                )
            )
        }
        whenever(userPermissionProvider.observe(params.spaceId)).thenReturn(
            flowOf(SpaceMemberPermissions.OWNER)
        )
    }



    private fun createViewModel(): AllContentViewModel {


        return AllContentViewModel(
            vmParams = params,
            storeOfObjectTypes = storeOfObjectTypes,
            urlBuilder = urlBuilder,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            updateAllContentState = updateAllContentState,
            restoreAllContentState = restoreAllContentState,
            searchObjects = searchObjects,
            localeProvider = localeProvider,
            createObject = createObject,
            setObjectListIsArchived = setObjectListIsArchived,
            removeObjectsFromWorkspace = removeObjectsFromWorkspace,
            userPermissionProvider = userPermissionProvider,
            fieldParser = fieldParser,
            spaceViews = spaceViews
        )
    }
}