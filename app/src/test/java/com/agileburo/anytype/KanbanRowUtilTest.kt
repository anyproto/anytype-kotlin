package com.agileburo.anytype

import com.agileburo.anytype.domain.database.model.KanbanRowView
import com.agileburo.anytype.ui.database.kanban.helpers.KanbanRowDiffUtil
import org.junit.Assert
import org.junit.Test

class KanbanRowUtilTest {

    @Test
    fun `areItemsTheSame should return false if views are different by id`() {

        val first = KanbanRowView.KanbanPeopleView(
            id = DataFactory.randomUuid(),
            category = DataFactory.randomString(),
            icon = DataFactory.randomString(),
            name = DataFactory.randomString()
        )

        val firstList = listOf(first)

        val second = KanbanRowView.KanbanPeopleView(
            id = DataFactory.randomUuid(),
            category = DataFactory.randomString(),
            icon = DataFactory.randomString(),
            name = DataFactory.randomString()
        )

        val secondList = listOf(second)

        val util = KanbanRowDiffUtil(firstList, secondList)

        Assert.assertEquals(
            false,
            util.areItemsTheSame(0, 0)
        )
    }

    @Test
    fun `areItemsTheSame should return true if two view have the same id`() {

        val id = DataFactory.randomUuid()

        val first = KanbanRowView.KanbanPeopleView(
            id = id,
            category = DataFactory.randomString(),
            icon = DataFactory.randomString(),
            name = DataFactory.randomString()
        )

        val firstList = listOf(first)

        val second = KanbanRowView.KanbanPeopleView(
            id = id,
            category = DataFactory.randomString(),
            icon = DataFactory.randomString(),
            name = DataFactory.randomString()
        )

        val secondList = listOf(second)

        val util = KanbanRowDiffUtil(firstList, secondList)

        Assert.assertEquals(
            true,
            util.areItemsTheSame(0, 0)
        )
    }

    @Test
    fun `areContentTheSame should return true if two data classes are equal`() {
        val first = KanbanRowView.KanbanPeopleView(
            id = DataFactory.randomUuid(),
            category = DataFactory.randomString(),
            icon = DataFactory.randomString(),
            name = DataFactory.randomString()
        )

        val firstList = listOf(first)

        val second = first.copy()

        val secondList = listOf(second)

        val util = KanbanRowDiffUtil(firstList, secondList)

        Assert.assertEquals(
            true,
            util.areContentsTheSame(0, 0)
        )
    }

    @Test
    fun `areContentTheSame should return false if two data classes are not equal`() {
        val first = KanbanRowView.KanbanPeopleView(
            id = DataFactory.randomUuid(),
            category = DataFactory.randomString(),
            icon = DataFactory.randomString(),
            name = DataFactory.randomString()
        )

        val firstList = listOf(first)

        val second = first.copy(category = DataFactory.randomString())

        val secondList = listOf(second)

        val util = KanbanRowDiffUtil(firstList, secondList)

        Assert.assertEquals(
            false,
            util.areContentsTheSame(0, 0)
        )
    }
}