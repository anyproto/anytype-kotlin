package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.StubFilter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
}
