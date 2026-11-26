package com.anytypeio.anytype.feature_search.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.chats.SearchChatMessages
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CrossSpaceObjectTypesContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.CrossSpaceSearchObjects
import com.anytypeio.anytype.domain.search.SearchWithMeta
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.DeepLinkToObjectDelegate
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_search.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

/**
 * Regression tests for #3364: a bookmark in search must open as an object
 * from the long-press menu, not only as its URL.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelOpenAsObjectTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private lateinit var searchWithMeta: SearchWithMeta
    private lateinit var crossSpaceSearch: CrossSpaceSearchObjects
    private lateinit var searchChatMessages: SearchChatMessages
    private lateinit var spaceViews: SpaceViewSubscriptionContainer
    private lateinit var participants: ParticipantSubscriptionContainer
    private lateinit var chatsDetails: ChatsDetailsSubscriptionContainer
    private lateinit var crossSpaceTypes: CrossSpaceObjectTypesContainer
    private lateinit var storeOfObjectTypes: StoreOfObjectTypes
    private lateinit var configStorage: ConfigStorage
    private lateinit var urlBuilder: UrlBuilder
    private lateinit var fieldParser: FieldParser
    private lateinit var deepLinkToObject: DeepLinkToObjectDelegate
    private lateinit var spaceManager: SpaceManager
    private lateinit var saveCurrentSpace: SaveCurrentSpace

    private val entrySpace = "space-1"
    private val otherSpace = "space-2"
    private val bookmarkId = "bookmark-1"
    private val url = "https://anytype.io"

    @Before
    fun setup() {
        searchWithMeta = mock()
        crossSpaceSearch = mock()
        searchChatMessages = mock()
        spaceViews = mock()
        participants = mock()
        chatsDetails = mock()
        crossSpaceTypes = mock()
        storeOfObjectTypes = mock()
        configStorage = mock()
        urlBuilder = mock()
        fieldParser = mock()
        deepLinkToObject = mock()
        spaceManager = mock()
        saveCurrentSpace = mock()

        // The view model combines these flows while it is constructed.
        whenever(spaceViews.observe()).thenReturn(flowOf(emptyList()))
        whenever(participants.observe()).thenReturn(flowOf(emptyList()))
        whenever(chatsDetails.observe()).thenReturn(flowOf(emptyList()))
        whenever(crossSpaceTypes.observe()).thenReturn(flowOf(emptyList()))
        whenever(storeOfObjectTypes.observe()).thenReturn(flowOf(emptyList()))
    }

    @Test
    fun `open as object on a bookmark opens the editor, not the url`() = runTest {
        val vm = buildViewModel()

        vm.commands.test {
            vm.onOpenAsObject(bookmarkRow(space = entrySpace))
            assertEquals(
                SearchNavigation.OpenObject(
                    OpenObjectNavigation.OpenEditor(target = bookmarkId, space = entrySpace)
                ),
                awaitItem()
            )
        }
        verifyNoInteractions(deepLinkToObject)
    }

    @Test
    fun `open as object on a bookmark from another space switches space and opens the editor`() =
        runTest {
            deepLinkToObject.stub {
                // Exact arguments: any() returns null for the SpaceId value class.
                onBlocking {
                    onDeepLinkToObject(
                        obj = bookmarkId,
                        space = SpaceId(otherSpace),
                        switchSpaceIfObjectFound = true
                    )
                } doReturn
                        DeepLinkToObjectDelegate.Result.Success(
                            StubObject(
                                id = bookmarkId,
                                space = otherSpace,
                                layout = ObjectType.Layout.BOOKMARK.code.toDouble(),
                                extraFields = mapOf(Relations.SOURCE to url)
                            )
                        )
            }
            val vm = buildViewModel()

            vm.commands.test {
                vm.onOpenAsObject(bookmarkRow(space = otherSpace))
                assertEquals(
                    SearchNavigation.OpenObject(
                        OpenObjectNavigation.OpenEditor(target = bookmarkId, space = otherSpace)
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `open in browser on a bookmark opens its url`() = runTest {
        val vm = buildViewModel()

        vm.commands.test {
            vm.onOpenInBrowser(bookmarkRow(space = otherSpace))
            assertEquals(
                SearchNavigation.OpenObject(OpenObjectNavigation.OpenBookmarkUrl(url)),
                awaitItem()
            )
        }
        verifyNoInteractions(deepLinkToObject)
    }

    @Test
    fun `navigation search shows the object menu`() {
        assertTrue(buildViewModel().showsObjectMenu)
    }

    @Test
    fun `attach picker has no object menu and never navigates`() = runTest {
        val vm = buildViewModel(purpose = SearchPurpose.ATTACH_TO_CHAT)

        assertFalse(vm.showsObjectMenu)
        vm.commands.test {
            vm.onOpenAsObject(bookmarkRow(space = entrySpace))
            vm.onOpenInBrowser(bookmarkRow(space = entrySpace))
            coroutineTestRule.advanceUntilIdle()
            expectNoEvents()
        }
    }

    private fun bookmarkRow(space: String) = SearchResultView.ObjectRow(
        id = bookmarkId,
        space = space,
        icon = ObjectIcon.None,
        name = "Bookmark",
        layout = ObjectType.Layout.BOOKMARK,
        navigation = OpenObjectNavigation.OpenBookmarkUrl(url)
    )

    private fun buildViewModel(
        purpose: SearchPurpose = SearchPurpose.NAVIGATION
    ): SearchViewModel {
        val dispatcher = coroutineTestRule.dispatcher
        return SearchViewModel(
            vmParams = SearchViewModel.VmParams(
                entrySpace = SpaceId(entrySpace),
                purpose = purpose
            ),
            searchWithMeta = searchWithMeta,
            crossSpaceSearch = crossSpaceSearch,
            searchChatMessages = searchChatMessages,
            spaceViews = spaceViews,
            participants = participants,
            chatsDetails = chatsDetails,
            crossSpaceTypes = crossSpaceTypes,
            storeOfObjectTypes = storeOfObjectTypes,
            configStorage = configStorage,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            dispatchers = AppCoroutineDispatchers(
                io = dispatcher,
                computation = dispatcher,
                main = dispatcher
            ),
            deepLinkToObject = deepLinkToObject,
            spaceManager = spaceManager,
            saveCurrentSpace = saveCurrentSpace
        )
    }
}
