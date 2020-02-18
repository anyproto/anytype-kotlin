package com.agileburo.anytype.presentation.home

import MockDataFactory
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.agileburo.anytype.presentation.mapper.toView
import org.junit.Test
import kotlin.test.assertEquals

class HomeDashboardViewMapperTest {

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
            type = Block.Content.Dashboard.Type.MAIN_SCREEN
        )

        val view = dashboard.toView()

        assertEquals(
            expected = emptyList(),
            actual = view
        )
    }

    @Test
    fun `should return one page link`() {

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
            type = Block.Content.Dashboard.Type.MAIN_SCREEN
        )

        val view = dashboard.toView()

        assertEquals(
            expected = listOf(
                DashboardView.Document(
                    id = child.content.asLink().target,
                    title = "Untitled"
                )
            ),
            actual = view
        )
    }
}