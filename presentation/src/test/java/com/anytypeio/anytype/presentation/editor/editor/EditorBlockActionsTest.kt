package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    }

    @Test
    fun `preview action should be in actions before style - when link block`() {

        val link = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = MockDataFactory.randomUuid(),
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = null
        )

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, link.id)
        )

        val document = listOf(smart, header, link)

        stubDefaultMethods(document)

        val vm = buildViewModel()

        vm.onStart(root)


        // Simulating long tap on link block, in order to enter multi-select mode.
        vm.onClickListener(
            ListenerType.LongClick(
                target = link.id,
                dimensions = BlockDimensions()
            )
        )

        assertTrue {
            vm.actions.value.indexOf(ActionItemType.Preview) == vm.actions.value.indexOf(ActionItemType.Style) - 1
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
            content = Block.Content.Smart(),
            children = listOf(header.id, p1.id, divider.id, p2.id)
        )

        val document = listOf(smart, header, title, p1, divider, p2)

        stubDefaultMethods(document)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

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
            content = Block.Content.Smart(),
            children = listOf(header.id, p1.id, divider.id, p2.id)
        )

        val document = listOf(smart, header, title, p1, divider, p2)

        stubDefaultMethods(document)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

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
            content = Block.Content.Smart(),
            children = listOf(header.id, p1.id, divider.id, p2.id)
        )

        val document = listOf(smart, header, title, p1, divider, p2)

        stubDefaultMethods(document)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

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