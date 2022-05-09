package com.anytypeio.anytype.presentation.extension

import FakeGateWay
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.dashboard.DashboardView
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import kotlin.test.assertEquals

class DashboardViewExtensionKtTest {

    @Test
    fun `should update emoji`() {

        val id1 = MockDataFactory.randomUuid()
        val target1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val target2 = MockDataFactory.randomUuid()
        val id3 = MockDataFactory.randomUuid()
        val target3 = MockDataFactory.randomUuid()

        val views = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),

            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),

            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            )
        )

        val builder = UrlBuilder(FakeGateWay)

        val result = views.updateDetails(
            target = views[1].target,
            details = Block.Fields(mapOf("iconEmoji" to "emoji777")),
            builder = builder,
            objectTypes = emptyList()
        )

        val expected = listOf<DashboardView.Document>(
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),

            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "",
                emoji = "emoji777"
            ),

            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should add blocks and sort by childrenIds`() {

        val id1 = MockDataFactory.randomUuid()
        val target1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val target2 = MockDataFactory.randomUuid()
        val id3 = MockDataFactory.randomUuid()
        val target3 = MockDataFactory.randomUuid()

        val views = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            )
        )

        val id4 = MockDataFactory.randomUuid()
        val target4 = MockDataFactory.randomUuid()
        val id5 = MockDataFactory.randomUuid()
        val target5 = MockDataFactory.randomUuid()
        val new = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id4,
                target = target4,
                title = "Title4",
                emoji = "emoji4"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id5,
                target = target5,
                title = "Title5",
                emoji = "emoji5"
            )
        )

        val result = views.addAndSortByIds(
            ids = listOf(id3, id1, id5, id2, id4),
            new = new
        )

        val expected = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id5,
                target = target5,
                title = "Title5",
                emoji = "emoji5"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id4,
                target = target4,
                title = "Title4",
                emoji = "emoji4"
            ),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should add blocks and sort by childrenIds and put not in list block to first place`() {

        val id1 = MockDataFactory.randomUuid()
        val target1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val target2 = MockDataFactory.randomUuid()
        val id3 = MockDataFactory.randomUuid()
        val target3 = MockDataFactory.randomUuid()

        val views = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            )
        )

        val id4 = MockDataFactory.randomUuid()
        val target4 = MockDataFactory.randomUuid()
        val id5 = MockDataFactory.randomUuid()
        val target5 = MockDataFactory.randomUuid()
        val new = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id4,
                target = target4,
                title = "Title4",
                emoji = "emoji4"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id5,
                target = target5,
                title = "Title5",
                emoji = "emoji5"
            )
        )

        val result = views.addAndSortByIds(
            ids = listOf(id3, id1, id5, id2, id4),
            new = new
        )

        val expected = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id5,
                target = target5,
                title = "Title5",
                emoji = "emoji5"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id4,
                target = target4,
                title = "Title4",
                emoji = "emoji4"
            ),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should sort by ids`() {

        val id1 = MockDataFactory.randomUuid()
        val target1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val target2 = MockDataFactory.randomUuid()
        val id3 = MockDataFactory.randomUuid()
        val target3 = MockDataFactory.randomUuid()

        val views = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            )
        )

        val result = views.sortByIds(
            ids = listOf(id3, id2, id1)
        )

        val expected = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id3,
                target = target3,
                title = "Title3",
                emoji = "emoji3"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should sort by ids without Set`() {

        val id1 = MockDataFactory.randomUuid()
        val target1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val target2 = MockDataFactory.randomUuid()
        val setId = MockDataFactory.randomUuid()

        val views = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            )
        )

        val result = views.sortByIds(
            ids = listOf(setId, id2, id1)
        )

        val expected = listOf(
            DashboardView.Document(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2",
                emoji = "emoji2"
            ),
            DashboardView.Document(
                isArchived = false,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should filter by not archived pages`() {

        val id1 = MockDataFactory.randomUuid()
        val target1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val target2 = MockDataFactory.randomUuid()

        val views = listOf(
            DashboardView.Document(
                isArchived = true,
                id = id1,
                target = target1,
                title = "Title1",
                emoji = "emoji1"
            ),
            DashboardView.Archive(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2"
            )
        )

        val result = views.filterByNotArchivedPages()

        val expected = listOf(
            DashboardView.Archive(
                isArchived = false,
                id = id2,
                target = target2,
                title = "Title2"
            )
        )

        assertEquals(expected, result)
    }
}