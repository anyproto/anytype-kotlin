package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory.page
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory.profile
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorMenuTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @Test
    fun `should not dispatch command for opening profile menu if document is not started`() {

        // SETUP

        val doc = profile(root)

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        // TESTING

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertNoValue()
    }

    @Test
    fun `should dispatch command for opening page menu if document is started`() = runTest {

        // SETUP

        val doc = page(root)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace
                    )
                )
            )
        )

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = details,
            spaceId = SpaceId(defaultSpace)
        )
        stubSpaceManager(space = defaultSpace)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertValue { value ->
            value.peekContent() == Command.OpenDocumentMenu(
                isArchived = false,
                isFavorite = false,
                isLocked = false,
                isTemplate = false,
                space = defaultSpace,
                ctx = root
            )
        }
    }

    @Test
    fun `should dispatch command for opening page menu with restrictions if document is started`() = runTest {

        // SETUP

        val space = defaultSpace

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.SPACE_ID to defaultSpace,
                        Relations.ID to root,
                        Relations.SPACE_ID to space
                    )
                )
            )
        )

        val doc = page(root)

        val objectRestrictions = listOf(ObjectRestriction.LAYOUT_CHANGE, ObjectRestriction.DELETE)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = doc,
            objectRestrictions = objectRestrictions,
            spaceId = SpaceId(space),
            details = details
        )
        stubSpaceManager(space = space)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertValue { value ->
            value.peekContent() == Command.OpenDocumentMenu(
                isArchived = false,
                isFavorite = false,
                isLocked = false,
                isTemplate = false,
                space = space,
                ctx = root
            )
        }
    }

    @Test
    fun `should not dispatch command for opening page menu if document is not started`() {

        // SETUP

        val doc = page(root)

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        // TESTING

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertNoValue()
    }

    @Test
    fun `should send open profile menu event`() = runTest {

        // SETUP

        val space = defaultSpace

        val doc = page(root)

        val typeId = MockDataFactory.randomString()

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to space,
                        Relations.TYPE to typeId
                    )
                ),
                typeId to Block.Fields(
                    mapOf(Relations.ID to typeId, Relations.UNIQUE_KEY to ObjectTypeIds.PROFILE)
                )
            )
        )

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = details,
            spaceId = SpaceId(space)
        )
        stubSpaceManager(space)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertValue { value ->
            value.peekContent() == Command.OpenDocumentMenu(
                isFavorite = false,
                isLocked = false,
                isArchived = false,
                isTemplate = false,
                space = space,
                ctx = root
            )
        }
    }
}