package com.agileburo.anytype.presentation.home

import MockDataFactory
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.emoji.Emoji
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.agileburo.anytype.presentation.mapper.toView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class HomeDashboardViewMapperTest {

    @Mock
    lateinit var emojifier: Emojifier

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should return empty list if home dashboard contains only data view`() {

        val child = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Link(
                target = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                type = Block.Content.Link.Type.DATA_VIEW
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val dashboard = HomeDashboard(
            id = MockDataFactory.randomUuid(),
            blocks = listOf(child),
            children = listOf(child.id),
            fields = Block.Fields.empty(),
            type = Block.Content.Smart.Type.HOME
        )

        val view = runBlocking {
            dashboard.toView()
        }

        assertEquals(
            expected = emptyList(),
            actual = view
        )
    }

    @Test
    fun `should return one page link`() {

        val emoji = Emoji(
            unicode = MockDataFactory.randomString(),
            alias = MockDataFactory.randomString()
        )

        val child = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Link(
                target = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                type = Block.Content.Link.Type.PAGE
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val dashboard = HomeDashboard(
            id = MockDataFactory.randomUuid(),
            blocks = listOf(child),
            children = listOf(child.id),
            fields = Block.Fields.empty(),
            type = Block.Content.Smart.Type.HOME
        )

        emojifier.stub {
            onBlocking { fromShortName(any()) } doReturn emoji
        }

        val view: List<DashboardView> = runBlocking { dashboard.toView() }

        assertEquals(
            expected = listOf(
                DashboardView.Document(
                    id = child.id,
                    target = child.content.asLink().target,
                    title = null,
                    emoji = null
                )
            ),
            actual = view
        )
    }
}