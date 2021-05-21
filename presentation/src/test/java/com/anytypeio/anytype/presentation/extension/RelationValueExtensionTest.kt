package com.anytypeio.anytype.presentation.extension

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.presentation.relations.convertToRelationDateValue
import com.anytypeio.anytype.presentation.relations.getDateRelationFormat
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.test.assertEquals

class RelationValueExtensionTest {

    //region [CONVERT RELATION DATE VALUE]
    @Test
    fun `should return null value when Any is null`() {

        val value: Any? = null

        val result = value.convertToRelationDateValue()

        assertNull(result)
    }

    @Test
    fun `should return null value when Any is non numbered string`() {

        val value: Any = "t123"

        val result = value.convertToRelationDateValue()

        assertNull(result)
    }

    @Test
    fun `should return time in millis when Any is numbered string`() {

        val value: Any = "1621596602"

        val result: Long? = value.convertToRelationDateValue()

        val expected: Long = 1621596602000L

        assertNotNull(result)

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return time in millis when Any is Double`() {

        val value: Any = 1621596602.0

        val result: Long? = value.convertToRelationDateValue()

        val expected: Long = 1621596602000L

        assertNotNull(result)

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return time in millis when Any is Long`() {

        val value: Any = 1621596602L

        val result: Long? = value.convertToRelationDateValue()

        val expected: Long = 1621596602000L

        assertNotNull(result)

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return null when Any is Integer`() {

        val value: Any = 1621596602

        val result: Long? = value.convertToRelationDateValue()

        assertNull(result)
    }
    //endregion

    //region [GET DATE RELATION DATE FORMAT]
    @Test
    fun `should return default date format when relation format is null`() {

        val columnView = ColumnView(
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            format = ColumnView.Format.DATE,
            width = 0,
            isVisible = true,
            isHidden = false,
            isReadOnly = false,
            dateFormat = null,
            timeFormat = Block.Content.DataView.TimeFormat.H12,
            isDateIncludeTime = true
        )

        val result = columnView.getDateRelationFormat()

        val expected = DateConst.DEFAULT_DATE_FORMAT

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return MONTH_ABBR_BEFORE_DAY when isDateIncludeTime false`() {

        val columnView = ColumnView(
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            format = ColumnView.Format.DATE,
            width = 0,
            isVisible = true,
            isHidden = false,
            isReadOnly = false,
            dateFormat = Block.Content.DataView.DateFormat.MONTH_ABBR_BEFORE_DAY,
            timeFormat = Block.Content.DataView.TimeFormat.H12,
            isDateIncludeTime = false
        )

        val result = columnView.getDateRelationFormat()

        val expected = Block.Content.DataView.DateFormat.MONTH_ABBR_BEFORE_DAY.format

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return SHORTUS when isDateIncludeTime false`() {

        val columnView = ColumnView(
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            format = ColumnView.Format.DATE,
            width = 0,
            isVisible = true,
            isHidden = false,
            isReadOnly = false,
            dateFormat = Block.Content.DataView.DateFormat.SHORTUS,
            timeFormat = Block.Content.DataView.TimeFormat.H12,
            isDateIncludeTime = false
        )

        val result = columnView.getDateRelationFormat()

        val expected = Block.Content.DataView.DateFormat.SHORTUS.format

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return MONTH_ABBR_BEFORE_DAY plus H12`() {

        val columnView = ColumnView(
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            format = ColumnView.Format.DATE,
            width = 0,
            isVisible = true,
            isHidden = false,
            isReadOnly = false,
            dateFormat = Block.Content.DataView.DateFormat.MONTH_ABBR_BEFORE_DAY,
            timeFormat = Block.Content.DataView.TimeFormat.H12,
            isDateIncludeTime = true
        )

        val result = columnView.getDateRelationFormat()

        val expected = Block.Content.DataView.DateFormat.MONTH_ABBR_BEFORE_DAY.format + " ${DateConst.TIME_H12}"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return MONTH_ABBR_BEFORE_DAY plus H24`() {

        val columnView = ColumnView(
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            format = ColumnView.Format.DATE,
            width = 0,
            isVisible = true,
            isHidden = false,
            isReadOnly = false,
            dateFormat = Block.Content.DataView.DateFormat.MONTH_ABBR_BEFORE_DAY,
            timeFormat = Block.Content.DataView.TimeFormat.H24,
            isDateIncludeTime = true
        )

        val result = columnView.getDateRelationFormat()

        val expected = Block.Content.DataView.DateFormat.MONTH_ABBR_BEFORE_DAY.format + " ${DateConst.TIME_H24}"

        assertEquals(
            expected = expected,
            actual = result
        )
    }
    //endregion

}