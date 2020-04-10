package com.agileburo.anytype.presentation.mapper

import MockDataFactory
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.domain.ext.content
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class HomeDashboardViewMapperTest {

    @Mock
    lateinit var emojifier: Emojifier

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should not map archived links`() {

        val archived = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Link(
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields(
                    mapOf(
                        Block.Fields.IS_ARCHIVED_KEY to true
                    )
                ),
                target = MockDataFactory.randomUuid()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val active = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Link(
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields(
                    mapOf(
                        Block.Fields.IS_ARCHIVED_KEY to false
                    )
                ),
                target = MockDataFactory.randomUuid()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val dashboard = HomeDashboard(
            id = MockDataFactory.randomUuid(),
            blocks = listOf(archived, active),
            children = listOf(archived.id, active.id),
            fields = Block.Fields.empty(),
            type = Block.Content.Dashboard.Type.MAIN_SCREEN
        )

        val result = runBlocking {
            dashboard.toView(
                emojifier = emojifier
            )
        }

        assertTrue {
            result.size == 1 && result.first().id == active.content<Block.Content.Link>().target
        }
    }
}