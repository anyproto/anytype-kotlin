package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DataViewGroup
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for [boardColumnQueries] — the per-column record-subscription filters. Each column's
 * filter must select exactly the records that belong to that column's group value.
 */
class BoardColumnQueryTest {

    private val key = "tag"

    @Test
    fun `status groups become IN filters plus a synthesized No value column`() {
        val queries = boardColumnQueries(
            groups = listOf(
                DataViewGroup(id = "g-a", value = DataViewGroup.Value.Status("a")),
                DataViewGroup(id = "g-b", value = DataViewGroup.Value.Status("b"))
            ),
            groupRelationKey = key
        )

        assertEquals(listOf("g-a", "g-b", BOARD_EMPTY_GROUP_ID), queries.map { it.columnId })
        assertEquals(DVFilterCondition.IN, queries[0].filter.condition)
        assertEquals(listOf("a"), queries[0].filter.value)
        assertEquals(key, queries[0].filter.relation)
        assertEquals(DVFilterCondition.EMPTY, queries.last().filter.condition)
    }

    @Test
    fun `tag groups become EXACT_IN filters of the option-id combination`() {
        val queries = boardColumnQueries(
            groups = listOf(
                DataViewGroup(id = "g-combo", value = DataViewGroup.Value.Tag(listOf("x", "y")))
            ),
            groupRelationKey = key
        )

        val combo = queries.first { it.columnId == "g-combo" }
        assertEquals(DVFilterCondition.EXACT_IN, combo.filter.condition)
        assertEquals(listOf("x", "y"), combo.filter.value)
        // Non-checkbox board still gets the empty column.
        assertEquals(DVFilterCondition.EMPTY, queries.first { it.columnId == BOARD_EMPTY_GROUP_ID }.filter.condition)
    }

    @Test
    fun `checkbox groups become EQUAL filters with no No-value column`() {
        val queries = boardColumnQueries(
            groups = listOf(
                DataViewGroup(id = "true", value = DataViewGroup.Value.Checkbox(true)),
                DataViewGroup(id = "false", value = DataViewGroup.Value.Checkbox(false))
            ),
            groupRelationKey = key
        )

        assertEquals(listOf("true", "false"), queries.map { it.columnId })
        assertEquals(DVFilterCondition.EQUAL, queries[0].filter.condition)
        assertEquals(true, queries[0].filter.value)
        assertEquals(false, queries[1].filter.value)
    }

    @Test
    fun `a backend empty group is used instead of synthesizing one`() {
        val queries = boardColumnQueries(
            groups = listOf(
                DataViewGroup(id = "be-empty", value = DataViewGroup.Value.Empty),
                DataViewGroup(id = "g-a", value = DataViewGroup.Value.Status("a"))
            ),
            groupRelationKey = key
        )

        assertEquals(listOf("be-empty", "g-a"), queries.map { it.columnId })
        assertEquals(DVFilterCondition.EMPTY, queries.first { it.columnId == "be-empty" }.filter.condition)
        assertEquals(1, queries.count { it.filter.condition == DVFilterCondition.EMPTY })
    }

    @Test
    fun `the empty group at the empty column id is queried as EMPTY and not duplicated`() {
        // The backend's "No value" group arrives at group id BOARD_EMPTY_GROUP_ID; the middleware
        // group mapper normalizes its (empty-id Status) value to Value.Empty, so here it maps to a
        // single EMPTY column and is NOT also synthesized as a duplicate (same subscription id),
        // which previously double-subscribed the "No value" column.
        val queries = boardColumnQueries(
            groups = listOf(
                DataViewGroup(id = "g-a", value = DataViewGroup.Value.Status("a")),
                DataViewGroup(id = BOARD_EMPTY_GROUP_ID, value = DataViewGroup.Value.Empty)
            ),
            groupRelationKey = key
        )

        assertEquals(listOf("g-a", BOARD_EMPTY_GROUP_ID), queries.map { it.columnId })
        assertEquals(1, queries.count { it.columnId == BOARD_EMPTY_GROUP_ID })
        assertEquals(
            DVFilterCondition.EMPTY,
            queries.first { it.columnId == BOARD_EMPTY_GROUP_ID }.filter.condition
        )
    }

    @Test
    fun `date groups are skipped`() {
        val queries = boardColumnQueries(
            groups = listOf(DataViewGroup(id = "g-date", value = DataViewGroup.Value.Date)),
            groupRelationKey = key
        )

        assertEquals(emptyList(), queries.filter { it.columnId == "g-date" })
    }
}
