package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.page.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorMoveToTest : EditorPresentationTestSetup() {

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }


    @Test
    fun `should add link's target into list of excluded ids`() {

        // SETUP

        val target = MockDataFactory.randomUuid()

        val link = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Link(
                target = target,
                fields = Block.Fields.empty(),
                type = Block.Content.Link.Type.PAGE
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, link.id)
        )

        val doc = listOf(page, header, title, link)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        // TESTING

        val testCommandObserver = vm.navigation.test()

        vm.onStart(root)

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = link.id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onActionMenuItemClicked(
            id = link.id,
            action = ActionItemType.MoveTo
        )

        testCommandObserver.assertValue { value ->
            val command = value.peekContent()
            command == AppNavigation.Command.OpenMoveToScreen(
                targets = listOf(link.id),
                excluded = listOf(target),
                context = root
            )
        }
    }

    @Test
    fun `should have empty excluded-id-list for text block`() {

        // SETUP

        val txt = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, txt.id)
        )

        val doc = listOf(page, header, title, txt)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        // TESTING

        val testCommandObserver = vm.navigation.test()

        vm.onStart(root)

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = txt.id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onActionMenuItemClicked(
            id = txt.id,
            action = ActionItemType.MoveTo
        )

        testCommandObserver.assertValue { value ->
            val command = value.peekContent()
            command == AppNavigation.Command.OpenMoveToScreen(
                targets = listOf(txt.id),
                excluded = emptyList(),
                context = root
            )
        }
    }
}