package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.primitives.Field
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.extension.checkboxFilterValue
import com.anytypeio.anytype.presentation.relations.toCreateFilterDateView
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FilterExtensionsTest {

    @Test
    fun shouldReturnCheckedTrue() {

        val views = listOf(
            CreateFilterView.Checkbox(
                isChecked = true,
                isSelected = true
            ),
            CreateFilterView.Checkbox(
                isChecked = false,
                isSelected = false
            )
        )

        val result = views.checkboxFilterValue()

        val expected = true

        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldReturnNullValueOnCheckboxNoneCondition() {

        val views = listOf<CreateFilterView>()

        val result = views.checkboxFilterValue()

        val expected = null

        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldReturnNotCheckedTrue() {

        val views = listOf(
            CreateFilterView.Checkbox(
                isChecked = true,
                isSelected = false
            ),
            CreateFilterView.Checkbox(
                isChecked = false,
                isSelected = true
            )
        )

        val result = views.checkboxFilterValue()

        val expected = false

        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldReturnNotEqualNotCheckedTrue() {

        val views = listOf(
            CreateFilterView.Checkbox(
                isChecked = true,
                isSelected = false
            ),
            CreateFilterView.Checkbox(
                isChecked = false,
                isSelected = true
            )
        )

        val result = views.checkboxFilterValue()

        val expected = false

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `toCreateFilterDateView should return correct date filter views for EQUAL condition with TODAY option`() {
        // Given
        val relation = StubRelationObject(
            key = "testDateRelation",
            format = Relation.Format.DATE
        )
        val condition = DVFilterCondition.EQUAL
        val value = 1640995200000L // Sample timestamp
        val fieldParser: FieldParser = mock()

        val mockDateField = mock<Field.Date>()
        whenever(fieldParser.toDate(eq(value))).thenReturn(mockDateField)

        // When
        val result = relation.toCreateFilterDateView(
            quickOption = DVFilterQuickOption.TODAY,
            condition = condition,
            value = value,
            fieldParser = fieldParser
        )

        // Then
        Assert.assertTrue(
            "Should contain TODAY option",
            result.any { it.type == DVFilterQuickOption.TODAY })
        Assert.assertTrue(
            "Should contain YESTERDAY option",
            result.any { it.type == DVFilterQuickOption.YESTERDAY })
        Assert.assertTrue(
            "Should contain TOMORROW option",
            result.any { it.type == DVFilterQuickOption.TOMORROW })
        Assert.assertTrue(
            "Should contain EXACT_DATE option",
            result.any { it.type == DVFilterQuickOption.EXACT_DATE })

        val todayOption = result.first { it.type == DVFilterQuickOption.TODAY }
        Assert.assertTrue("TODAY option should be selected", todayOption.isSelected)
        Assert.assertEquals("Relation key should match", "testDateRelation", todayOption.id)
        Assert.assertEquals("Condition should match", condition, todayOption.condition)
        Assert.assertEquals("Value should match when selected", value, todayOption.value)
    }

    @Test
    fun `toCreateFilterDateView should return correct date filter views for GREATER condition with extended options`() {
        // Given
        val relation = StubRelationObject(
            key = "testDateRelation",
            format = Relation.Format.DATE
        )
        val condition = DVFilterCondition.GREATER
        val value = 1640995200000L
        val fieldParser: FieldParser = mock()

        whenever(fieldParser.toDate(eq(value))).thenReturn(null)

        // When
        val result = relation.toCreateFilterDateView(
            quickOption = DVFilterQuickOption.CURRENT_YEAR,
            condition = condition,
            value = value,
            fieldParser = fieldParser
        )

        // Then
        Assert.assertTrue(
            "Should contain CURRENT_YEAR option",
            result.any { it.type == DVFilterQuickOption.CURRENT_YEAR })
        Assert.assertTrue(
            "Should contain LAST_YEAR option",
            result.any { it.type == DVFilterQuickOption.LAST_YEAR })
        Assert.assertTrue(
            "Should contain NEXT_YEAR option",
            result.any { it.type == DVFilterQuickOption.NEXT_YEAR })
        Assert.assertTrue(
            "Should contain EXACT_DATE option",
            result.any { it.type == DVFilterQuickOption.EXACT_DATE })

        val currentYearOption = result.first { it.type == DVFilterQuickOption.CURRENT_YEAR }
        Assert.assertTrue("CURRENT_YEAR option should be selected", currentYearOption.isSelected)
        Assert.assertEquals("Value should match when selected", value, currentYearOption.value)
    }

    @Test
    fun `toCreateFilterDateView should handle EXACT_DATE option correctly`() {
        // Given
        val relation = StubRelationObject(
            key = "testDateRelation",
            format = Relation.Format.DATE
        )
        val condition = DVFilterCondition.EQUAL
        val value = 1640995200000L
        val fieldParser: FieldParser = mock()

        whenever(fieldParser.toDate(eq(value))).thenReturn(null)

        // When
        val result = relation.toCreateFilterDateView(
            quickOption = DVFilterQuickOption.EXACT_DATE,
            condition = condition,
            value = value,
            fieldParser = fieldParser
        )

        // Then
        val exactDateOption = result.first { it.type == DVFilterQuickOption.EXACT_DATE }
        Assert.assertTrue(
            "EXACT_DATE option should be selected when value > 0",
            exactDateOption.isSelected
        )
        Assert.assertEquals("Value should match", value, exactDateOption.value)
    }

    @Test
    fun `toCreateFilterDateView should handle zero value with EXACT_DATE option`() {
        // Given
        val relation = StubRelationObject(
            key = "testDateRelation",
            format = Relation.Format.DATE
        )
        val condition = DVFilterCondition.EQUAL
        val value = 0L
        val fieldParser: FieldParser = mock()

        whenever(fieldParser.toDate(eq(value))).thenReturn(null)

        // When
        val result = relation.toCreateFilterDateView(
            quickOption = DVFilterQuickOption.EXACT_DATE,
            condition = condition,
            value = value,
            fieldParser = fieldParser
        )

        // Then
        val exactDateOption = result.first { it.type == DVFilterQuickOption.EXACT_DATE }
        Assert.assertFalse(
            "EXACT_DATE option should not be selected when value is 0",
            exactDateOption.isSelected
        )
        Assert.assertEquals(
            "Value should be NO_VALUE when not selected",
            CreateFilterView.Date.NO_VALUE,
            exactDateOption.value
        )
    }

    @Test
    fun `toCreateFilterDateView should handle IN condition with extended options only`() {
        // Given
        val relation = StubRelationObject(
            key = "testDateRelation",
            format = Relation.Format.DATE
        )
        val condition = DVFilterCondition.IN
        val value = 1640995200000L
        val fieldParser: FieldParser = mock()

        whenever(fieldParser.toDate(eq(value))).thenReturn(null)

        // When
        val result = relation.toCreateFilterDateView(
            quickOption = DVFilterQuickOption.CURRENT_MONTH,
            condition = condition,
            value = value,
            fieldParser = fieldParser
        )

        // Then
        Assert.assertTrue(
            "Should contain CURRENT_MONTH option",
            result.any { it.type == DVFilterQuickOption.CURRENT_MONTH })
        Assert.assertTrue(
            "Should contain CURRENT_YEAR option",
            result.any { it.type == DVFilterQuickOption.CURRENT_YEAR })
        Assert.assertFalse(
            "Should not contain EXACT_DATE option for IN condition",
            result.any { it.type == DVFilterQuickOption.EXACT_DATE })
        Assert.assertFalse(
            "Should not contain DAYS_AGO option for IN condition",
            result.any { it.type == DVFilterQuickOption.DAYS_AGO })

        val currentMonthOption = result.first { it.type == DVFilterQuickOption.CURRENT_MONTH }
        Assert.assertTrue("CURRENT_MONTH option should be selected", currentMonthOption.isSelected)
    }

    @Test
    fun `toCreateFilterDateView should handle EMPTY condition with no options`() {
        // Given
        val relation = StubRelationObject(
            key = "testDateRelation",
            format = Relation.Format.DATE
        )
        val condition = DVFilterCondition.EMPTY
        val value = 1640995200000L
        val fieldParser: FieldParser = mock()

        // When
        val result = relation.toCreateFilterDateView(
            quickOption = null,
            condition = condition,
            value = value,
            fieldParser = fieldParser
        )

        // Then
        Assert.assertTrue("Should return empty list for EMPTY condition", result.isEmpty())
    }
}