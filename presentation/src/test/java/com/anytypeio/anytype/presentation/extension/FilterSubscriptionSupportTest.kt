package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.StubFilter
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.presentation.sets.updateFormatForSubscription
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FilterSubscriptionSupportTest {

    // region isSupportedForSubscription — conditions not requiring values

    @Test
    fun `EMPTY condition is always supported`() {
        val filter = StubFilter(condition = DVFilterCondition.EMPTY, value = null)
        assertTrue(filter.isSupportedForSubscription())
    }

    @Test
    fun `NOT_EMPTY condition is always supported`() {
        val filter = StubFilter(condition = DVFilterCondition.NOT_EMPTY, value = null)
        assertTrue(filter.isSupportedForSubscription())
    }

    @Test
    fun `NONE condition is always supported`() {
        val filter = StubFilter(condition = DVFilterCondition.NONE, value = null)
        assertTrue(filter.isSupportedForSubscription())
    }

    @Test
    fun `EXISTS condition is always supported`() {
        val filter = StubFilter(condition = DVFilterCondition.EXISTS, value = null)
        assertTrue(filter.isSupportedForSubscription())
    }

    // endregion

    // region isSupportedForSubscription — null value

    @Test
    fun `null value is unsupported for value-requiring condition`() {
        val filter = StubFilter(condition = DVFilterCondition.EQUAL, value = null)
        assertFalse(filter.isSupportedForSubscription())
    }

    @Test
    fun `null value with DATE format and non-EXACT_DATE quick option is supported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.LESS_OR_EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.TODAY,
            value = null
        )
        assertTrue(filter.isSupportedForSubscription())
    }

    @Test
    fun `null value with DATE format and EXACT_DATE quick option is unsupported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.EXACT_DATE,
            value = null
        )
        assertFalse(filter.isSupportedForSubscription())
    }

    @Test
    fun `null value with DATE format and DAYS_AGO quick option is unsupported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.LESS_OR_EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.DAYS_AGO,
            value = null
        )
        assertFalse(filter.isSupportedForSubscription())
    }

    @Test
    fun `null value with DATE format and DAYS_AHEAD quick option is unsupported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.LESS_OR_EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.DAYS_AHEAD,
            value = null
        )
        assertFalse(filter.isSupportedForSubscription())
    }

    // endregion

    // region isSupportedForSubscription — String values

    @Test
    fun `empty string is unsupported`() {
        val filter = StubFilter(condition = DVFilterCondition.EQUAL, value = "")
        assertFalse(filter.isSupportedForSubscription())
    }

    @Test
    fun `non-empty string is supported`() {
        val filter = StubFilter(condition = DVFilterCondition.EQUAL, value = "hello")
        assertTrue(filter.isSupportedForSubscription())
    }

    // endregion

    // region isSupportedForSubscription — List values

    @Test
    fun `empty list is unsupported`() {
        val filter = StubFilter(condition = DVFilterCondition.IN, value = emptyList<String>())
        assertFalse(filter.isSupportedForSubscription())
    }

    @Test
    fun `non-empty list is supported`() {
        val filter = StubFilter(condition = DVFilterCondition.IN, value = listOf("id1", "id2"))
        assertTrue(filter.isSupportedForSubscription())
    }

    // endregion

    // region isSupportedForSubscription — Date (Double) values

    @Test
    fun `zero Double with DATE format and EXACT_DATE is unsupported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.EXACT_DATE,
            value = 0.0
        )
        assertFalse(filter.isSupportedForSubscription())
    }

    @Test
    fun `non-zero Double with DATE format and EXACT_DATE is supported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.EXACT_DATE,
            value = 1700000000.0
        )
        assertTrue(filter.isSupportedForSubscription())
    }

    @Test
    fun `zero Double with DATE format and non-EXACT_DATE quick option is supported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.TODAY,
            value = 0.0
        )
        assertTrue(filter.isSupportedForSubscription())
    }

    // endregion

    // region isSupportedForSubscription — Date (Long) values

    @Test
    fun `zero Long with DATE format and EXACT_DATE is unsupported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.EXACT_DATE,
            value = 0L
        )
        assertFalse(filter.isSupportedForSubscription())
    }

    @Test
    fun `non-zero Long with DATE format and EXACT_DATE is supported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.DATE,
            quickOption = DVFilterQuickOption.EXACT_DATE,
            value = 1700000000L
        )
        assertTrue(filter.isSupportedForSubscription())
    }

    // endregion

    // region isSupportedForSubscription — Number values (non-DATE format)

    @Test
    fun `zero Double with NUMBER format is supported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.NUMBER,
            value = 0.0
        )
        assertTrue(filter.isSupportedForSubscription())
    }

    @Test
    fun `zero Long with NUMBER format is supported`() {
        val filter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            relationFormat = RelationFormat.NUMBER,
            value = 0L
        )
        assertTrue(filter.isSupportedForSubscription())
    }

    // endregion

    // region isSupportedForSubscription — Boolean values

    @Test
    fun `boolean true value is supported`() {
        val filter = StubFilter(condition = DVFilterCondition.EQUAL, value = true)
        assertTrue(filter.isSupportedForSubscription())
    }

    @Test
    fun `boolean false value is supported`() {
        val filter = StubFilter(condition = DVFilterCondition.EQUAL, value = false)
        assertTrue(filter.isSupportedForSubscription())
    }

    // endregion

    // region removeUnsupportedFilters — flat list

    @Test
    fun `mixed supported and unsupported filters - keeps only supported`() {
        val supported = StubFilter(condition = DVFilterCondition.EQUAL, value = "text")
        val unsupported = StubFilter(condition = DVFilterCondition.EQUAL, value = null)

        val result = listOf(supported, unsupported).removeUnsupportedFilters()

        assertEquals(1, result.size)
        assertEquals(supported, result.first())
    }

    @Test
    fun `all unsupported filters - returns empty list`() {
        val filters = listOf(
            StubFilter(condition = DVFilterCondition.EQUAL, value = null),
            StubFilter(condition = DVFilterCondition.IN, value = emptyList<String>()),
            StubFilter(condition = DVFilterCondition.LIKE, value = "")
        )

        val result = filters.removeUnsupportedFilters()

        assertTrue(result.isEmpty())
    }

    // endregion

    // region removeUnsupportedFilters — advanced (nested) filters

    @Test
    fun `advanced filter with partial nested cleanup`() {
        val supportedChild = StubFilter(condition = DVFilterCondition.EQUAL, value = "ok")
        val unsupportedChild = StubFilter(condition = DVFilterCondition.EQUAL, value = null)
        val advanced = StubFilter(
            operator = DVFilterOperator.AND,
            condition = DVFilterCondition.EQUAL,
            nestedFilters = listOf(supportedChild, unsupportedChild)
        )

        val result = listOf(advanced).removeUnsupportedFilters()

        assertEquals(1, result.size)
        assertEquals(1, result.first().nestedFilters.size)
        assertEquals(supportedChild, result.first().nestedFilters.first())
    }

    @Test
    fun `advanced filter with all nested removed - parent removed`() {
        val unsupported1 = StubFilter(condition = DVFilterCondition.EQUAL, value = null)
        val unsupported2 = StubFilter(condition = DVFilterCondition.LIKE, value = "")
        val advanced = StubFilter(
            operator = DVFilterOperator.AND,
            condition = DVFilterCondition.EQUAL,
            nestedFilters = listOf(unsupported1, unsupported2)
        )

        val result = listOf(advanced).removeUnsupportedFilters()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `nested advanced filters are cleaned recursively`() {
        val supportedLeaf = StubFilter(condition = DVFilterCondition.NOT_EMPTY, value = null)
        val unsupportedLeaf = StubFilter(condition = DVFilterCondition.EQUAL, value = null)

        val innerAdvanced = StubFilter(
            operator = DVFilterOperator.OR,
            condition = DVFilterCondition.EQUAL,
            nestedFilters = listOf(supportedLeaf, unsupportedLeaf)
        )
        val outerAdvanced = StubFilter(
            operator = DVFilterOperator.AND,
            condition = DVFilterCondition.EQUAL,
            nestedFilters = listOf(innerAdvanced)
        )

        val result = listOf(outerAdvanced).removeUnsupportedFilters()

        assertEquals(1, result.size)
        val outerResult = result.first()
        assertEquals(1, outerResult.nestedFilters.size)
        val innerResult = outerResult.nestedFilters.first()
        assertEquals(1, innerResult.nestedFilters.size)
        assertEquals(supportedLeaf, innerResult.nestedFilters.first())
    }

    // endregion

    // region updateFormatForSubscription — advanced (nested) filters

    @Test
    fun `updateFormatForSubscription recurses into nested filters of advanced filter groups`() = runBlocking {
        val dueDateRelationKey = "dueDate"

        val storeOfRelations = DefaultStoreOfRelations()
        storeOfRelations.merge(
            listOf(
                StubRelationObject(
                    key = dueDateRelationKey,
                    format = com.anytypeio.anytype.core_models.Relation.Format.DATE
                )
            )
        )

        val nestedDateFilter = StubFilter(
            relationKey = dueDateRelationKey,
            relationFormat = RelationFormat.LONG_TEXT, // wrong format, should be updated to DATE
            condition = DVFilterCondition.LESS_OR_EQUAL,
            quickOption = DVFilterQuickOption.TODAY,
            value = null
        )
        val nestedCheckboxFilter = StubFilter(
            condition = DVFilterCondition.EQUAL,
            value = false
        )
        val advancedGroup = StubFilter(
            operator = DVFilterOperator.AND,
            condition = DVFilterCondition.EQUAL,
            nestedFilters = listOf(nestedDateFilter, nestedCheckboxFilter)
        )

        val result = listOf(advancedGroup).updateFormatForSubscription(storeOfRelations)

        assertEquals(1, result.size)
        val updatedGroup = result.first()
        assertEquals(2, updatedGroup.nestedFilters.size)

        val updatedDateFilter = updatedGroup.nestedFilters[0]
        assertEquals(
            RelationFormat.DATE,
            updatedDateFilter.relationFormat,
            "Nested date filter should have its relationFormat updated to DATE"
        )

        // Verify the updated nested date filter is now supported for subscription
        assertTrue(
            updatedDateFilter.isSupportedForSubscription(),
            "Nested date filter with TODAY quick option and null value should be supported after format update"
        )

        // Verify the checkbox filter (with unrecognized relation key) is unchanged
        val updatedCheckboxFilter = updatedGroup.nestedFilters[1]
        assertEquals(
            nestedCheckboxFilter.relationFormat,
            updatedCheckboxFilter.relationFormat,
            "Checkbox filter with unknown relation key should retain its original format"
        )
    }

    @Test
    fun `updateFormatForSubscription recurses into deeply nested advanced filter groups`() = runBlocking {
        val dueDateRelationKey = "dueDate"

        val storeOfRelations = DefaultStoreOfRelations()
        storeOfRelations.merge(
            listOf(
                StubRelationObject(
                    key = dueDateRelationKey,
                    format = com.anytypeio.anytype.core_models.Relation.Format.DATE
                )
            )
        )

        val deepDateFilter = StubFilter(
            relationKey = dueDateRelationKey,
            relationFormat = RelationFormat.LONG_TEXT, // wrong format
            condition = DVFilterCondition.LESS_OR_EQUAL,
            quickOption = DVFilterQuickOption.YESTERDAY,
            value = null
        )
        val innerAdvanced = StubFilter(
            operator = DVFilterOperator.OR,
            condition = DVFilterCondition.EQUAL,
            nestedFilters = listOf(deepDateFilter)
        )
        val outerAdvanced = StubFilter(
            operator = DVFilterOperator.AND,
            condition = DVFilterCondition.EQUAL,
            nestedFilters = listOf(innerAdvanced)
        )

        val result = listOf(outerAdvanced).updateFormatForSubscription(storeOfRelations)

        assertEquals(1, result.size)
        val outerResult = result.first()
        assertEquals(1, outerResult.nestedFilters.size)
        val innerResult = outerResult.nestedFilters.first()
        assertEquals(1, innerResult.nestedFilters.size)

        val updatedDeepFilter = innerResult.nestedFilters.first()
        assertEquals(
            RelationFormat.DATE,
            updatedDeepFilter.relationFormat,
            "Deeply nested date filter should have its relationFormat updated to DATE"
        )
        assertTrue(
            updatedDeepFilter.isSupportedForSubscription(),
            "Deeply nested date filter with YESTERDAY quick option should be supported after format update"
        )
    }

    @Test
    fun `updateFormatForSubscription without recursion would cause nested date filter to be removed`() = runBlocking {
        // This test demonstrates the exact bug scenario: a filter group with a nested
        // date filter using TODAY quick option and null value. Without the recursive
        // updateFormatForSubscription fix, the nested filter's relationFormat stays
        // as its default, causing isSupportedForSubscription() to return false.
        val nestedDateFilter = StubFilter(
            condition = DVFilterCondition.LESS_OR_EQUAL,
            relationFormat = RelationFormat.LONG_TEXT, // simulates unpatched format
            quickOption = DVFilterQuickOption.TODAY,
            value = null
        )

        // Without DATE format, this filter is considered unsupported
        assertFalse(
            nestedDateFilter.isSupportedForSubscription(),
            "Date filter with non-DATE relationFormat and null value should be unsupported"
        )

        // With correct DATE format, it becomes supported
        val correctedFilter = nestedDateFilter.copy(relationFormat = RelationFormat.DATE)
        assertTrue(
            correctedFilter.isSupportedForSubscription(),
            "Date filter with DATE relationFormat and TODAY quick option should be supported"
        )
    }

    // endregion
}
