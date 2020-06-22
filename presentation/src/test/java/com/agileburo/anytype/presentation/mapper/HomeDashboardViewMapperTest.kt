package com.agileburo.anytype.presentation.mapper

import MockDataFactory
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.misc.UrlBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class HomeDashboardViewMapperTest {

    val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomUuid(),
        profile = MockDataFactory.randomUuid()
    )

    val builder = UrlBuilder(config)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should not map archived links`() {

        val target = MockDataFactory.randomUuid()

        val archived = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Link(
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty(),
                target = target
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val active = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Link(
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty(),
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
            type = Block.Content.Smart.Type.HOME,
            details = Block.Details(
                details = mapOf(
                    target to Block.Fields(
                        mapOf(
                            Block.Fields.IS_ARCHIVED_KEY to true
                        )
                    )
                )
            )
        )

        val result = runBlocking {
            dashboard.toView(builder)
        }

        assertTrue {
            result.size == 1 && result.first().id == active.id
        }
    }
}