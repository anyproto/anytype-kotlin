package com.agileburo.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.UpdateCheckbox
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorCheckboxTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should start updating checkbox as checked when it is clicked`() {

        // SETUP

        val child = MockDataFactory.randomUuid()

        val checkbox = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                isChecked = false,
                marks = emptyList(),
                style = Block.Content.Text.Style.CHECKBOX
            ),
            children = emptyList()
        )

        val view = BlockView.Text.Checkbox(
            id = checkbox.id,
            text = checkbox.content<Block.Content.Text>().text,
            isChecked = true
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(child)
            ),
            checkbox
        )

        stubInterceptEvents()
        stubOpenDocument(document = page)
        stubUpdateCheckbox()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.onCheckboxClicked(view)

        verifyBlocking(updateCheckbox, times(1)) {
            invoke(
                eq(
                    UpdateCheckbox.Params(
                        context = root,
                        target = child,
                        isChecked = true
                    )
                )
            )
        }
    }

    @Test
    fun `should start updating checkbox as not checked when it is clicked`() {

        // SETUP

        val child = MockDataFactory.randomUuid()

        val checkbox = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                isChecked = true,
                marks = emptyList(),
                style = Block.Content.Text.Style.CHECKBOX
            ),
            children = emptyList()
        )

        val view = BlockView.Text.Checkbox(
            id = checkbox.id,
            text = checkbox.content<Block.Content.Text>().text,
            isChecked = false
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(child)
            ),
            checkbox
        )

        stubInterceptEvents()
        stubOpenDocument(document = page)
        stubUpdateCheckbox()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.onCheckboxClicked(view)

        verifyBlocking(updateCheckbox, times(1)) {
            invoke(
                eq(
                    UpdateCheckbox.Params(
                        context = root,
                        target = child,
                        isChecked = false
                    )
                )
            )
        }
    }

    fun stubUpdateCheckbox(
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        updateCheckbox.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(payload)
        }
    }
}