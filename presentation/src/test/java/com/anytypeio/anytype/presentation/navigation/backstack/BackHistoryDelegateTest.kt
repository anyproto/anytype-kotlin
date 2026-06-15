package com.anytypeio.anytype.presentation.navigation.backstack

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BackHistoryDelegateTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var inspector: NavigationBackStackInspector

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var fieldParser: FieldParser

    private val dispatchers by lazy {
        AppCoroutineDispatchers(
            io = coroutineTestRule.dispatcher,
            main = coroutineTestRule.dispatcher,
            computation = coroutineTestRule.dispatcher
        )
    }

    private lateinit var delegate: BackHistoryDelegate

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        delegate = BackHistoryDelegate.Default(
            inspector = inspector,
            searchObjects = searchObjects,
            fieldParser = fieldParser,
            dispatchers = dispatchers
        )
    }

    private fun entry(entryId: String, objectId: String, space: String = SPACE) =
        BackStackObjectEntry(entryId = entryId, objectId = objectId, space = space)

    private fun wrapper(id: String, name: String) = ObjectWrapper.Basic(
        mapOf(
            Relations.ID to id,
            Relations.NAME to name
        )
    )

    private fun stubNamesFromWrapperName() {
        fieldParser.stub {
            on {
                getObjectName(objectWrapper = any<ObjectWrapper.Basic>(), useUntitled = any())
            } doAnswer { invocation ->
                (invocation.arguments[0] as ObjectWrapper.Basic).name.orEmpty()
            }
        }
    }

    @Test
    fun `should stay hidden when there are no candidates besides the current screen`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(entry("e1", "obj1"))
        }

        delegate.onBackButtonLongPressed()

        assertEquals(
            expected = BackHistoryMenuState.Hidden,
            actual = delegate.backHistoryMenu.value
        )
        verify(searchObjects, never()).invoke(any())
    }

    @Test
    fun `should show menu with only home entry when home is in back stack and no objects`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(entry("e1", "obj1"))
            on { homeScreenEntryId() } doReturn "home-entry"
        }

        delegate.onBackButtonLongPressed()

        assertEquals(
            expected = BackHistoryMenuState.Visible(
                homeEntryId = "home-entry",
                items = emptyList()
            ),
            actual = delegate.backHistoryMenu.value
        )
        verify(searchObjects, never()).invoke(any())
    }

    @Test
    fun `should include home entry alongside object history`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(
                entry("e1", "objA"),
                entry("e2", "objB"),
                entry("e3", "objC")
            )
            on { homeScreenEntryId() } doReturn "home-entry"
        }
        whenever(searchObjects.invoke(any())).thenReturn(
            Either.Right(
                listOf(
                    wrapper(id = "objA", name = "Alpha"),
                    wrapper(id = "objB", name = "Beta")
                )
            )
        )
        stubNamesFromWrapperName()

        delegate.onBackButtonLongPressed()

        assertEquals(
            expected = BackHistoryMenuState.Visible(
                homeEntryId = "home-entry",
                items = listOf(
                    BackHistoryMenuItem(entryId = "e2", objectId = "objB", space = SPACE, name = "Beta"),
                    BackHistoryMenuItem(entryId = "e1", objectId = "objA", space = SPACE, name = "Alpha")
                )
            ),
            actual = delegate.backHistoryMenu.value
        )
    }

    @Test
    fun `should show visible menu with resolved names`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(
                entry("e1", "objA"),
                entry("e2", "objB"),
                entry("e3", "objC")
            )
        }
        whenever(searchObjects.invoke(any())).thenReturn(
            Either.Right(
                listOf(
                    wrapper(id = "objA", name = "Alpha"),
                    wrapper(id = "objB", name = "Beta")
                )
            )
        )
        stubNamesFromWrapperName()

        delegate.onBackButtonLongPressed()

        assertEquals(
            expected = BackHistoryMenuState.Visible(
                items = listOf(
                    BackHistoryMenuItem(entryId = "e2", objectId = "objB", space = SPACE, name = "Beta"),
                    BackHistoryMenuItem(entryId = "e1", objectId = "objA", space = SPACE, name = "Alpha")
                )
            ),
            actual = delegate.backHistoryMenu.value
        )
    }

    @Test
    fun `should run one search per space when history spans multiple spaces`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(
                entry("e1", "objA", space = "space-1"),
                entry("e2", "objB", space = "space-2"),
                entry("e3", "objC", space = "space-2")
            )
        }
        whenever(searchObjects.invoke(any())).thenReturn(Either.Right(emptyList()))

        delegate.onBackButtonLongPressed()

        verify(searchObjects, times(1)).invoke(argThat { space.id == "space-1" })
        verify(searchObjects, times(1)).invoke(argThat { space.id == "space-2" })
    }

    @Test
    fun `should show visible menu with blank names when search fails`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(
                entry("e1", "objA"),
                entry("e2", "objB")
            )
        }
        whenever(searchObjects.invoke(any())).thenReturn(Either.Left(RuntimeException("boom")))

        delegate.onBackButtonLongPressed()

        assertEquals(
            expected = BackHistoryMenuState.Visible(
                items = listOf(
                    BackHistoryMenuItem(entryId = "e1", objectId = "objA", space = SPACE, name = "")
                )
            ),
            actual = delegate.backHistoryMenu.value
        )
    }

    @Test
    fun `should hide menu on dismiss`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(
                entry("e1", "objA"),
                entry("e2", "objB")
            )
        }
        whenever(searchObjects.invoke(any())).thenReturn(
            Either.Right(listOf(wrapper(id = "objA", name = "Alpha")))
        )
        stubNamesFromWrapperName()

        delegate.onBackButtonLongPressed()
        delegate.onBackHistoryMenuDismissed()

        assertEquals(
            expected = BackHistoryMenuState.Hidden,
            actual = delegate.backHistoryMenu.value
        )
    }

    @Test
    fun `should rebuild menu from a fresh snapshot on second long press`() = runTest {
        inspector.stub {
            on { objectScreenEntries() } doReturn listOf(
                entry("e1", "objA"),
                entry("e2", "objB")
            ) doReturn listOf(
                entry("e1", "objA"),
                entry("e2", "objB"),
                entry("e3", "objC")
            )
        }
        whenever(searchObjects.invoke(any())).thenReturn(
            Either.Right(
                listOf(
                    wrapper(id = "objA", name = "Alpha"),
                    wrapper(id = "objB", name = "Beta")
                )
            )
        )
        stubNamesFromWrapperName()

        delegate.onBackButtonLongPressed()
        delegate.onBackHistoryMenuDismissed()
        delegate.onBackButtonLongPressed()

        assertEquals(
            expected = BackHistoryMenuState.Visible(
                items = listOf(
                    BackHistoryMenuItem(entryId = "e2", objectId = "objB", space = SPACE, name = "Beta"),
                    BackHistoryMenuItem(entryId = "e1", objectId = "objA", space = SPACE, name = "Alpha")
                )
            ),
            actual = delegate.backHistoryMenu.value
        )
    }

    companion object {
        const val SPACE = "space-id"
    }
}
