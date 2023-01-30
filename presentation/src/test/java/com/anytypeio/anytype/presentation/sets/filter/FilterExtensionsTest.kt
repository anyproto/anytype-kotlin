package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.presentation.extension.checkboxFilterValue
import org.junit.Assert
import org.junit.Test

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
}