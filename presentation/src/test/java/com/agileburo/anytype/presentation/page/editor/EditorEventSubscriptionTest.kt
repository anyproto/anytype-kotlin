package com.agileburo.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorEventSubscriptionTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should subscribe only on start`() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        stubOpenDocument(document = document)
        stubObserveEvents()

        val params = InterceptEvents.Params(context = root)

        interceptEvents.stub {
            onBlocking { build(params) } doReturn flowOf()
        }

        val vm = buildViewModel()

        // TESTING

        verifyZeroInteractions(interceptEvents)

        vm.onStart(root)

        verify(interceptEvents, times(1)).build(params)

        vm.onStop()

        vm.onStart(root)

        verify(interceptEvents, times(2)).build(params)
    }
}