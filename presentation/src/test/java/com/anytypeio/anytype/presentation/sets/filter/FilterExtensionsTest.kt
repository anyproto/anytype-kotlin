package com.anytypeio.anytype.presentation.sets.filter

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.presentation.extension.checkboxFilter
import com.anytypeio.anytype.presentation.sets.model.Viewer
import org.junit.Assert
import org.junit.Test

class FilterExtensionsTest {

    @Test
    fun shouldReturnCheckedTrue() {

        val relationKey = MockDataFactory.randomUuid()
        val condition = Viewer.Filter.Condition.Checkbox.Equal()

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

        val result = views.checkboxFilter(relationKey, condition)

        val expected = DVFilter(
            relationKey = relationKey,
            operator = Block.Content.DataView.Filter.Operator.AND,
            condition = DVFilterCondition.EQUAL,
            value = true
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldReturnNullValueOnCheckboxNoneCondition() {

        val relationKey = MockDataFactory.randomUuid()
        val condition = Viewer.Filter.Condition.Checkbox.None()

        val views = listOf<CreateFilterView>()

        val result = views.checkboxFilter(relationKey, condition)

        val expected = DVFilter(
            relationKey = relationKey,
            operator = Block.Content.DataView.Filter.Operator.AND,
            condition = DVFilterCondition.NONE,
            value = null
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldReturnNotCheckedTrue() {

        val relationKey = MockDataFactory.randomUuid()
        val condition = Viewer.Filter.Condition.Checkbox.Equal()

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

        val result = views.checkboxFilter(relationKey, condition)

        val expected = DVFilter(
            relationKey = relationKey,
            operator = Block.Content.DataView.Filter.Operator.AND,
            condition = DVFilterCondition.EQUAL,
            value = false
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldReturnNotEqualNotCheckedTrue() {

        val relationKey = MockDataFactory.randomUuid()
        val condition = Viewer.Filter.Condition.Checkbox.NotEqual()

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

        val result = views.checkboxFilter(relationKey, condition)

        val expected = DVFilter(
            relationKey = relationKey,
            operator = Block.Content.DataView.Filter.Operator.AND,
            condition = DVFilterCondition.NOT_EQUAL,
            value = false
        )

        Assert.assertEquals(expected, result)
    }
}