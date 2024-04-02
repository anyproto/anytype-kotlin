package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorBlockActionsTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val title = MockBlockFactory.title()
    val header = MockBlockFactory.header(children = listOf(title.id))

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        stubInterceptEvents()
    }

    @Test
    fun `preview action should be in actions on second position`() {
        if (BuildConfig.ENABLE_LINK_APPERANCE_MENU) {
            val link = MockBlockFactory.link()

            val smart = Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, link.id)
            )

            val document = listOf(smart, header, link)

            stubDefaultMethods(document)

            val vm = buildViewModel()

            vm.onStart(id = root, space = defaultSpace)


            // Simulating long tap on link block, in order to enter multi-select mode.
            vm.onClickListener(
                ListenerType.LongClick(
                    target = link.id,
                    dimensions = BlockDimensions()
                )
            )
            assertContains(vm.actions.value, ActionItemType.Preview)
            assertTrue {
                vm.actions.value.indexOf(ActionItemType.Preview) == 1
            }
        }
    }

    @Test
    fun `style action should be in actions for divider block when entering multi-select mode`() {

        // SETUP

        val p1 = MockBlockFactory.paragraph()
        val divider =
            MockBlockFactory.divider(style = Block.Content.Divider.Style.values().random())
        val p2 = MockBlockFactory.paragraph()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, p1.id, divider.id, p2.id)
        )

        val document = listOf(smart, header, title, p1, divider, p2)

        stubDefaultMethods(document)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        // Simulating long tap on "divider" block, in order to enter multi-select mode.

        vm.onClickListener(
            ListenerType.LongClick(
                target = divider.id,
                dimensions = BlockDimensions()
            )
        )

        assertTrue {
            vm.actions.value.contains(ActionItemType.Style)
        }
    }

    // TODO Find out why this test does not succeed during CI workflow
    //@Test
    fun `style action should not be in actions after selecting a block other than text - a divider`() {

        // SETUP

        val p1 = MockBlockFactory.paragraph()
        val divider =
            MockBlockFactory.divider(style = Block.Content.Divider.Style.values().random())
        val p2 = MockBlockFactory.paragraph()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, p1.id, divider.id, p2.id)
        )

        val document = listOf(smart, header, title, p1, divider, p2)

        stubDefaultMethods(document)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        // Simulating long tap on "p1" block, in order to enter multi-select mode.

        vm.onClickListener(
            ListenerType.LongClick(
                target = p1.id,
                dimensions = BlockDimensions()
            )
        )

        assertTrue { vm.actions.value.isNotEmpty() }

        assertTrue { vm.actions.value.contains(ActionItemType.Style) }

        vm.onClickListener(ListenerType.DividerClick(target = divider.id))

        assertFalse { vm.actions.value.contains(ActionItemType.Style) }

        vm.onClickListener(ListenerType.DividerClick(target = divider.id))

        assertTrue { vm.actions.value.contains(ActionItemType.Style) }
    }

    @Test
    fun `add-below action should not be available if more than one block is selected`() {

        // SETUP

        val p1 = MockBlockFactory.paragraph()
        val divider =
            MockBlockFactory.divider(style = Block.Content.Divider.Style.values().random())
        val p2 = MockBlockFactory.paragraph()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, p1.id, divider.id, p2.id)
        )

        val document = listOf(smart, header, title, p1, divider, p2)

        stubDefaultMethods(document)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        // Simulating long tap on "p1" block, in order to enter multi-select mode.

        vm.onClickListener(
            ListenerType.LongClick(
                target = p1.id,
                dimensions = BlockDimensions()
            )
        )

        assertTrue { vm.actions.value.contains(ActionItemType.AddBelow) }

        vm.onClickListener(ListenerType.DividerClick(target = divider.id))

        assertFalse { vm.actions.value.contains(ActionItemType.AddBelow) }

        vm.onClickListener(ListenerType.DividerClick(target = divider.id))

        assertTrue { vm.actions.value.contains(ActionItemType.AddBelow) }

        vm.onTextInputClicked(target = p2.id)

        assertFalse { vm.actions.value.contains(ActionItemType.AddBelow) }
    }

    private fun stubDefaultMethods(document: List<Block>) {
        stubOpenDocument(document = document)
        stubInterceptEvents()
        stubInterceptThreadStatus()
    }
}