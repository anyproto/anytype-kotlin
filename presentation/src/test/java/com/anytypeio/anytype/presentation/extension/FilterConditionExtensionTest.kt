package com.anytypeio.anytype.presentation.extension

import MockDataFactory
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.sets.model.Viewer
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.asserter

class FilterConditionExtensionTest {

    @Test
    fun `should be proper condition type`() {

        val conditionEqual = Viewer.Filter.Condition.Text.Equal()
        val conditionLike = Viewer.Filter.Condition.Text.Like()
        val conditionIn = Viewer.Filter.Condition.Selected.In()
        val conditionAllIn = Viewer.Filter.Condition.Selected.AllIn()
        val conditionLess = Viewer.Filter.Condition.Number.Less()
        val conditionGreaterOrEqual = Viewer.Filter.Condition.Number.GreaterOrEqual()
        val conditionNotEqual = Viewer.Filter.Condition.Checkbox.NotEqual()

        val resultEqual = conditionEqual.type()
        val resultLike = conditionLike.type()
        val resultIn = conditionIn.type()
        val resultAllIn = conditionAllIn.type()
        val resultLess = conditionLess.type()
        val resultGreaterOrEqual = conditionGreaterOrEqual.type()
        val resultNotEqual = conditionNotEqual.type()

        asserter.assertEquals(
            message = "Should be TEXT type",
            expected = Viewer.Filter.Type.TEXT,
            actual = resultEqual
        )
        asserter.assertEquals(
            message = "Should be TEXT type",
            expected = Viewer.Filter.Type.TEXT,
            actual = resultLike
        )
        asserter.assertEquals(
            message = "Should be SELECTED type",
            expected = Viewer.Filter.Type.SELECTED,
            actual = resultIn
        )
        asserter.assertEquals(
            message = "Should be SELECTED type",
            expected = Viewer.Filter.Type.SELECTED,
            actual = resultAllIn
        )
        asserter.assertEquals(
            message = "Should be NUMBER type",
            expected = Viewer.Filter.Type.NUMBER,
            actual = resultLess
        )
        asserter.assertEquals(
            message = "Should be NUMBER type",
            expected = Viewer.Filter.Type.NUMBER,
            actual = resultGreaterOrEqual
        )
        asserter.assertEquals(
            message = "Should be CHECKBOX type",
            expected = Viewer.Filter.Type.CHECKBOX,
            actual = resultNotEqual
        )
    }

    @Test
    fun `should return proper condition when initial condition is null`() {

        val relationText = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationText.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationText.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationText.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationText.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationText.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationText.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationText.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationText.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationText.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationText.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedText = Viewer.Filter.Condition.Text.Equal()
        val expectedNumber = Viewer.Filter.Condition.Number.Equal()
        val expectedSelected = Viewer.Filter.Condition.Selected.In()
        val expectedCheckbox = Viewer.Filter.Condition.Checkbox.Equal()

        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationText.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationTextShort.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationPhone.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationEmail.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationUrl.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Number Equal",
            expected = expectedNumber,
            actual = relationNumber.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Number Equal",
            expected = expectedNumber,
            actual = relationDate.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Selected In",
            expected = expectedSelected,
            actual = relationTag.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Selected In",
            expected = expectedSelected,
            actual = relationStatus.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Selected In",
            expected = expectedSelected,
            actual = relationObject.toConditionView(condition = null)
        )
        asserter.assertEquals(
            message = "Condition should be Checkbox Equal",
            expected = expectedCheckbox,
            actual = relationCheckbox.toConditionView(condition = null)
        )
    }

    @Test
    fun `test equal condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedText = Viewer.Filter.Condition.Text.Equal()
        val expectedNumber = Viewer.Filter.Condition.Number.Equal()
        val expectedSelected = Viewer.Filter.Condition.Selected.Equal()
        val expectedCheckbox = Viewer.Filter.Condition.Checkbox.Equal()

        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationTextLong.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationTextShort.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationPhone.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationEmail.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Equal",
            expected = expectedText,
            actual = relationUrl.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Number Equal",
            expected = expectedNumber,
            actual = relationNumber.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Number Equal",
            expected = expectedNumber,
            actual = relationDate.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Equal",
            expected = expectedSelected,
            actual = relationTag.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Equal",
            expected = expectedSelected,
            actual = relationStatus.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Equal",
            expected = expectedSelected,
            actual = relationObject.toConditionView(condition = DVFilterCondition.EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Checkbox Equal",
            expected = expectedCheckbox,
            actual = relationCheckbox.toConditionView(condition = DVFilterCondition.EQUAL)
        )
    }

    @Test
    fun `test not equal condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedText = Viewer.Filter.Condition.Text.NotEqual()
        val expectedNumber = Viewer.Filter.Condition.Number.NotEqual()
        val expectedCheckbox = Viewer.Filter.Condition.Checkbox.NotEqual()

        asserter.assertEquals(
            message = "Condition should be Text Not Equal",
            expected = expectedText,
            actual = relationTextLong.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Equal",
            expected = expectedText,
            actual = relationTextShort.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Equal",
            expected = expectedText,
            actual = relationPhone.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Equal",
            expected = expectedText,
            actual = relationEmail.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Equal",
            expected = expectedText,
            actual = relationUrl.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Number Not Equal",
            expected = expectedNumber,
            actual = relationNumber.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Number Not Equal",
            expected = expectedNumber,
            actual = relationDate.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        }
        asserter.assertEquals(
            message = "Condition should be Checkbox Not Equal",
            expected = expectedCheckbox,
            actual = relationCheckbox.toConditionView(condition = DVFilterCondition.NOT_EQUAL)
        )
    }

    @Test
    fun `test greater condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedNumber = Viewer.Filter.Condition.Number.Greater()

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.GREATER)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.GREATER)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.GREATER)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.GREATER)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.GREATER)
        }
        asserter.assertEquals(
            message = "Condition should be Number Greater",
            expected = expectedNumber,
            actual = relationNumber.toConditionView(condition = DVFilterCondition.GREATER)
        )
        asserter.assertEquals(
            message = "Condition should be Number Greater",
            expected = expectedNumber,
            actual = relationDate.toConditionView(condition = DVFilterCondition.GREATER)
        )
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.GREATER)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.GREATER)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.GREATER)
        }
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.GREATER)
        }
    }

    @Test
    fun `test less condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedNumber = Viewer.Filter.Condition.Number.Less()

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.LESS)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.LESS)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.LESS)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.LESS)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.LESS)
        }
        asserter.assertEquals(
            message = "Condition should be Number Less",
            expected = expectedNumber,
            actual = relationNumber.toConditionView(condition = DVFilterCondition.LESS)
        )
        asserter.assertEquals(
            message = "Condition should be Number Less",
            expected = expectedNumber,
            actual = relationDate.toConditionView(condition = DVFilterCondition.LESS)
        )
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.LESS)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.LESS)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.LESS)
        }
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.LESS)
        }
    }

    @Test
    fun `test greater or equal condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedNumber = Viewer.Filter.Condition.Number.GreaterOrEqual()

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        asserter.assertEquals(
            message = "Condition should be Number Greater or Equal",
            expected = expectedNumber,
            actual = relationNumber.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Number Greater or Equal",
            expected = expectedNumber,
            actual = relationDate.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        )
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.GREATER_OR_EQUAL)
        }
    }

    @Test
    fun `test less or equal condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedNumber = Viewer.Filter.Condition.Number.LessOrEqual()

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        asserter.assertEquals(
            message = "Condition should be Number Less or Equal",
            expected = expectedNumber,
            actual = relationNumber.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        )
        asserter.assertEquals(
            message = "Condition should be Number Less or Equal",
            expected = expectedNumber,
            actual = relationDate.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        )
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.LESS_OR_EQUAL)
        }
    }

    @Test
    fun `test like condition`() {

        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedText = Viewer.Filter.Condition.Text.Like()

        asserter.assertEquals(
            message = "Condition should be Text Like",
            expected = expectedText,
            actual = relationTextLong.toConditionView(condition = DVFilterCondition.LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Like",
            expected = expectedText,
            actual = relationTextShort.toConditionView(condition = DVFilterCondition.LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Like",
            expected = expectedText,
            actual = relationPhone.toConditionView(condition = DVFilterCondition.LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Like",
            expected = expectedText,
            actual = relationEmail.toConditionView(condition = DVFilterCondition.LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Like",
            expected = expectedText,
            actual = relationUrl.toConditionView(condition = DVFilterCondition.LIKE)
        )
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.LIKE)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.LIKE)
        }
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.LIKE)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.LIKE)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.LIKE)
        }
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.LIKE)
        }
    }

    @Test
    fun `test not like condition`() {

        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedText = Viewer.Filter.Condition.Text.NotLike()

        asserter.assertEquals(
            message = "Condition should be Text Not Like",
            expected = expectedText,
            actual = relationTextLong.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Like",
            expected = expectedText,
            actual = relationTextShort.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Like",
            expected = expectedText,
            actual = relationPhone.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Like",
            expected = expectedText,
            actual = relationEmail.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Like",
            expected = expectedText,
            actual = relationUrl.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        )
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        }
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        }
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.NOT_LIKE)
        }
    }

    @Test
    fun `test in condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedSelected = Viewer.Filter.Condition.Selected.In()

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.IN)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.IN)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.IN)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.IN)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.IN)
        }
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.IN)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.IN)
        }
        asserter.assertEquals(
            message = "Condition should be Selected In",
            expected = expectedSelected,
            actual = relationTag.toConditionView(condition = DVFilterCondition.IN)
        )
        asserter.assertEquals(
            message = "Condition should be Selected In",
            expected = expectedSelected,
            actual = relationStatus.toConditionView(condition = DVFilterCondition.IN)
        )
        asserter.assertEquals(
            message = "Condition should be Selected In",
            expected = expectedSelected,
            actual = relationObject.toConditionView(condition = DVFilterCondition.IN)
        )
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.IN)
        }
    }

    @Test
    fun `test not in condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedSelected = Viewer.Filter.Condition.Selected.NotIn()

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
        asserter.assertEquals(
            message = "Condition should be Selected Not In",
            expected = expectedSelected,
            actual = relationTag.toConditionView(condition = DVFilterCondition.NOT_IN)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Not In",
            expected = expectedSelected,
            actual = relationStatus.toConditionView(condition = DVFilterCondition.NOT_IN)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Not In",
            expected = expectedSelected,
            actual = relationObject.toConditionView(condition = DVFilterCondition.NOT_IN)
        )
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.NOT_IN)
        }
    }

    @Test
    fun `test all in condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedSelected = Viewer.Filter.Condition.Selected.AllIn()

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
        asserter.assertEquals(
            message = "Condition should be Selected All In",
            expected = expectedSelected,
            actual = relationTag.toConditionView(condition = DVFilterCondition.ALL_IN)
        )
        asserter.assertEquals(
            message = "Condition should be Selected All In",
            expected = expectedSelected,
            actual = relationStatus.toConditionView(condition = DVFilterCondition.ALL_IN)
        )
        asserter.assertEquals(
            message = "Condition should be Selected All In",
            expected = expectedSelected,
            actual = relationObject.toConditionView(condition = DVFilterCondition.ALL_IN)
        )
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.ALL_IN)
        }
    }

    @Test
    fun `test not all in condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        assertFails {
            relationTextLong.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationTextShort.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationPhone.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationEmail.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationUrl.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationTag.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationStatus.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationObject.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.NOT_ALL_IN)
        }
    }

    @Test
    fun `test empty condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedText = Viewer.Filter.Condition.Text.Empty()
        val expectedSelected = Viewer.Filter.Condition.Selected.Empty()

        asserter.assertEquals(
            message = "Condition should be Text Empty",
            expected = expectedText,
            actual = relationTextLong.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Empty",
            expected = expectedText,
            actual = relationTextShort.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Empty",
            expected = expectedText,
            actual = relationPhone.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Empty",
            expected = expectedText,
            actual = relationEmail.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Empty",
            expected = expectedText,
            actual = relationUrl.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.EMPTY)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.EMPTY)
        }
        asserter.assertEquals(
            message = "Condition should be Selected Empty",
            expected = expectedSelected,
            actual = relationTag.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Empty",
            expected = expectedSelected,
            actual = relationStatus.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Empty",
            expected = expectedSelected,
            actual = relationObject.toConditionView(condition = DVFilterCondition.EMPTY)
        )
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.EMPTY)
        }
    }

    @Test
    fun `test not empty condition`() {
        val relationTextLong = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = false,
            selections = emptyList(),
            defaultValue = MockDataFactory.randomString()
        )
        val relationTextShort = relationTextLong.copy(
            format = Relation.Format.SHORT_TEXT
        )
        val relationUrl = relationTextLong.copy(
            format = Relation.Format.URL
        )
        val relationPhone = relationTextLong.copy(
            format = Relation.Format.PHONE
        )
        val relationEmail = relationTextLong.copy(
            format = Relation.Format.EMAIL
        )
        val relationNumber = relationTextLong.copy(
            format = Relation.Format.NUMBER
        )
        val relationDate = relationTextLong.copy(
            format = Relation.Format.DATE
        )
        val relationTag = relationTextLong.copy(
            format = Relation.Format.TAG
        )
        val relationStatus = relationTextLong.copy(
            format = Relation.Format.STATUS
        )
        val relationObject = relationTextLong.copy(
            format = Relation.Format.OBJECT
        )
        val relationCheckbox = relationTextLong.copy(
            format = Relation.Format.CHECKBOX
        )

        val expectedText = Viewer.Filter.Condition.Text.NotEmpty()
        val expectedSelected = Viewer.Filter.Condition.Selected.NotEmpty()

        asserter.assertEquals(
            message = "Condition should be Text Not Empty",
            expected = expectedText,
            actual = relationTextLong.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Empty",
            expected = expectedText,
            actual = relationTextShort.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Empty",
            expected = expectedText,
            actual = relationPhone.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Empty",
            expected = expectedText,
            actual = relationEmail.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Text Not Empty",
            expected = expectedText,
            actual = relationUrl.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        assertFails {
            relationNumber.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        }
        assertFails {
            relationDate.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        }
        asserter.assertEquals(
            message = "Condition should be Selected Not Empty",
            expected = expectedSelected,
            actual = relationTag.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Not Empty",
            expected = expectedSelected,
            actual = relationStatus.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        asserter.assertEquals(
            message = "Condition should be Selected Not Empty",
            expected = expectedSelected,
            actual = relationObject.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        )
        assertFails {
            relationCheckbox.toConditionView(condition = DVFilterCondition.NOT_EMPTY)
        }
    }
}