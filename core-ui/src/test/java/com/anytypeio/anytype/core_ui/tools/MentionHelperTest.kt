package com.anytypeio.anytype.core_ui.tools

import com.anytypeio.anytype.core_ui.tools.MentionHelper.getSubSequenceFromStartWithLimit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MentionHelperTest {

    private val PREDICATE_CHAR = ' '

    @Test
    fun `should find mention in the middle of the block`() {

        val text = "Before @mention after"
        val startIndex = 7

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 20,
            predicate = PREDICATE_CHAR
        )

        val expected = "@mention"

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should find mention with size 11 in the middle of the block`() {

        val text = "Before @mentionAndBiggerThenEleven after"
        val startIndex = 7

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 11,
            predicate = PREDICATE_CHAR
        )

        val expected = "@mentionAnd"

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should find mention in the start of the block`() {

        val text = "@mention after"
        val startIndex = 0

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 20,
            predicate = PREDICATE_CHAR
        )

        val expected = "@mention"

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should find mention in the end of the block`() {

        val text = "Before @mentionRealBigWord"
        val startIndex = 7

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 11,
            predicate = PREDICATE_CHAR
        )

        val expected = "@mentionRea"

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should find empty mention`() {

        val text = ""
        val startIndex = 0

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 11,
            predicate = PREDICATE_CHAR
        )

        val expected = ""

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should find mention with size one`() {

        val text = "@"
        val startIndex = 0

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 11,
            predicate = PREDICATE_CHAR
        )

        val expected = "@"

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should find first word in text`() {

        val text = "text without mention"
        val startIndex = 0

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 11,
            predicate = PREDICATE_CHAR
        )

        val expected = "text"

        assertEquals(expected = expected, actual = result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw error when take number is negative`() {

        val text = "text without mention"
        val startIndex = 0

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = -11,
            predicate = PREDICATE_CHAR
        )

        val expected = "text"

        assertEquals(expected = expected, actual = result)
    }

    @Test(expected = StringIndexOutOfBoundsException::class)
    fun `should throw error when start index bigger then text length`() {

        val text = "text without mention"
        val startIndex = 100

        val result = getSubSequenceFromStartWithLimit(
            s = text,
            startIndex = startIndex,
            takeNumber = 0,
            predicate = PREDICATE_CHAR
        )

        val expected = "text"

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should isMentionDeleted be true when delete mention trigger`() {

        val start = 0
        val mentionPosition = 0

        val result = MentionHelper.isMentionDeleted(
            text = "",
            start = start,
            mentionPosition = mentionPosition,
            before = 1,
            count = 0,
        )

        assertTrue(result)
    }

    @Test
    fun `should isMentionDeleted be true when changes before mention position`() {

        val start = 11
        val mentionPosition = 12

        val result = MentionHelper.isMentionDeleted(
            text = "Add mention@",
            start = start,
            mentionPosition = mentionPosition,
            before = 1,
            count = 0,
        )

        assertTrue(result)
    }

    @Test
    fun `should isMentionDeleted be false when mention trigger is replaced by same char`() {

        val start = 12
        val mentionPosition = 12

        val result = MentionHelper.isMentionDeleted(
            text = "Add mention @",
            start = start,
            mentionPosition = mentionPosition,
            before = 1,
            count = 1,
        )

        assertFalse(result)
    }

    @Test
    fun `should isMentionDeleted be true when mention trigger is replaced by same other char`() {

        val start = 12
        val mentionPosition = 12

        val result = MentionHelper.isMentionDeleted(
            text = "Add mention $",
            start = start,
            mentionPosition = mentionPosition,
            before = 1,
            count = 1,
        )

        assertTrue(result)
    }

    @Test
    fun `should be deleted mention char when start smaller then mentionPosition`() {

        val start = 5
        val mentionPosition = 6

        val result = MentionHelper.isMentionDeleted(
            text = "text@",
            start = start,
            mentionPosition = mentionPosition,
            before = 0,
            count = 1
        )

        assertTrue(result)
    }

    @Test
    fun `should not delete mention char when start bigger then mentionPosition`() {

        val start = 13
        val mentionPosition = 12

        val result = MentionHelper.isMentionDeleted(
            text = "@",
            start = start,
            mentionPosition = mentionPosition,
            before = 0,
            count = 1
        )

        assertFalse(result)
    }

    @Test
    fun `should not delete mention char when start bigger then mentionPosition and after 0`() {

        val start = 7
        val mentionPosition = 6

        val result = MentionHelper.isMentionDeleted(
            text = "text @ t",
            start = start,
            mentionPosition = mentionPosition,
            before = 0,
            count = 1
        )

        assertFalse(result)
    }

    @Test
    fun `test text conditions with buffer`() {
        val textConditions = ConditionsWithBuffer

        textConditions.forEach { condition ->

            val result = MentionHelper.isMentionSuggestTriggered(
                s = condition.s,
                start = condition.start,
                count = condition.count
            )

            val expected = condition.isEvent
            assertEquals(
                expected = expected,
                actual = result,
                message = "Test failed on Condition:$condition"
            )
        }
    }

    @Test
    fun `test text conditions without buffer`() {
        val textConditions = ConditionsWithoutBuffer

        textConditions.forEach { condition ->

            val result = MentionHelper.isMentionSuggestTriggered(
                s = condition.s,
                start = condition.start,
                count = condition.count
            )

            val expected = condition.isEvent
            assertEquals(
                expected = expected,
                actual = result,
                message = "Test failed on Condition:$condition"
            )
        }
    }

    @Test
    fun `should isMentionSuggestTriggered be false 1`() {

        val  s = "text $"
        val start = 5
        val count = 1

        val result = MentionHelper.isMentionSuggestTriggered(s = s, start = start, count = count)

        assertFalse(result)
    }

    @Test
    fun `should isMentionSuggestTriggered be false 2`() {

        val  s = "text $@@"
        val start = 5
        val count = 3

        val result = MentionHelper.isMentionSuggestTriggered(s = s, start = start, count = count)

        assertFalse(result)
    }

    @Test
    fun `should isMentionSuggestTriggered be false 3`() {

        val  s = "text@"
        val start = 4
        val count = 1

        val result = MentionHelper.isMentionSuggestTriggered(s = s, start = start, count = count)

        assertFalse(result)
    }

    @Test
    fun `should isMentionSuggestTriggered be false 4`() {

        val  s = "text @t"
        val start = 5
        val count = 2

        val result = MentionHelper.isMentionSuggestTriggered(s = s, start = start, count = count)

        assertFalse(result)
    }

    @Test
    fun `should isMentionSuggestTriggered be true 1`() {

        val  s = "text @"
        val start = 5
        val count = 1

        val result = MentionHelper.isMentionSuggestTriggered(s = s, start = start, count = count)

        assertTrue(result)
    }

    data class TextChanged(
        val isEvent: Boolean,
        val s: CharSequence,
        val start: Int,
        val count: Int
    )

    companion object {

        val ConditionsWithBuffer = listOf(
            TextChanged(
                isEvent = true,
                s = "@",
                start = 0,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@ ",
                start = 1,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@t",
                start = 1,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@t ",
                start = 2,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@te",
                start = 2,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@",
                start = 0,
                count = 7
            ),
            TextChanged(
                isEvent = false,
                s = "before@ ",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@ m",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@m",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@me",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = true,
                s = "before @",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @ ",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @ m",
                start = 9,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @m",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @me",
                start = 9,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before men",
                start = 7,
                count = 3
            ),
            TextChanged(
                isEvent = false,
                s = "befor",
                start = 0,
                count = 5
            ),
            TextChanged(
                isEvent = false,
                s = " before men",
                start = 8,
                count = 3
            ),
            TextChanged(
                isEvent = false,
                s = " befor",
                start = 1,
                count = 5
            ),
            TextChanged(
                isEvent = false,
                s = "before",
                start = 5,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before",
                start = 0,
                count = 6
            ),
            TextChanged(
                isEvent = false,
                s = "@before@",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = true,
                s = "@before @",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@before @m",
                start = 9,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@before @m@",
                start = 10,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@before @m@ ",
                start = 11,
                count = 1
            ),
            TextChanged(
                isEvent = true,
                s = "@before @m@ @",
                start = 12,
                count = 1
            )
        )

        val ConditionsWithoutBuffer = listOf(
            TextChanged(
                isEvent = true,
                s = "@",
                start = 0,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@ ",
                start = 1,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@t",
                start = 1,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@t ",
                start = 2,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@te",
                start = 2,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@",
                start = 6,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@ ",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@ m",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@m",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before@me",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = true,
                s = "before @",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @ ",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @ m",
                start = 9,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @m",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before @me",
                start = 9,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before men",
                start = 9,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "befor",
                start = 4,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = " before men",
                start = 10,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = " befor",
                start = 5,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "before",
                start = 5,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@before@",
                start = 7,
                count = 1
            ),
            TextChanged(
                isEvent = true,
                s = "@before @",
                start = 8,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@before @m",
                start = 9,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@before @m@",
                start = 10,
                count = 1
            ),
            TextChanged(
                isEvent = false,
                s = "@before @m@ ",
                start = 11,
                count = 1
            ),
            TextChanged(
                isEvent = true,
                s = "@before @m@ @",
                start = 12,
                count = 1
            )
        )
    }

    //region {should not happened case}
    @org.junit.Test
    fun `mention add error 1`() {

        val text = "test "
        val start = 5
        val before = 0
        val count = 1

        val mentionStartPosition = 5
        val mention = ""

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = ""

        assertEquals(expected = expected, actual = result)
    }
    //endregion

    //region {add}
    @org.junit.Test
    fun `mention add 1`() {

        val text = "test @"
        val start = 5
        val before = 0
        val count = 1

        val mentionStartPosition = 5
        val mention = ""

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention add 2`() {

        val text = "test @m"
        val start = 6
        val before = 0
        val count = 1

        val mentionStartPosition = 5
        val mention = "@"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@m"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention add 3`() {

        val text = "test @mention"
        val start = 7
        val before = 0
        val count = 6

        val mentionStartPosition = 5
        val mention = "@m"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@mention"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention add 4`() {

        val text = "start @mention add end"
        val start = 17
        val before = 0
        val count = 3

        val mentionStartPosition = 7
        val mention = "@mention"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@mention"

        assertEquals(expected = expected, actual = result)
    }
    //endregion

    //region {update}
    @org.junit.Test
    fun `mention update 1`() {

        val text = "test @"
        val start = 5
        val before = 1
        val count = 1

        val mentionStartPosition = 5
        val mention = "@"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention update 2`() {

        val text = "test @"
        val start = 5
        val before = 1
        val count = 1

        val mentionStartPosition = 5
        val mention = ""

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention update 3`() {

        val text = "test @xyz"
        val start = 7
        val before = 2
        val count = 2

        val mentionStartPosition = 5
        val mention = "@xpr"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@xyz"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention update 4`() {

        val text = "test @qwerty"
        val start = 6
        val before = 3
        val count = 6

        val mentionStartPosition = 5
        val mention = "@xpr"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@qwerty"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention update 5`() {

        val text = "test @qwerty end"
        val start = 6
        val before = 3
        val count = 6

        val mentionStartPosition = 5
        val mention = "@xpr"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@qwerty"

        assertEquals(expected = expected, actual = result)
    }
    //endregion

    //region {delete}
    @org.junit.Test
    fun `mention delete 1`() {

        val text = "test "
        val start = 5
        val before = 1
        val count = 0

        val mentionStartPosition = 5
        val mention = "@"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = ""

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention delete 2`() {

        val text = "test @xyz"
        val start = 6
        val before = 3
        val count = 0

        val mentionStartPosition = 5
        val mention = "@xyz"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@"

        assertEquals(expected = expected, actual = result)
    }

    @org.junit.Test
    fun `mention delete 3`() {

        val text = "start @xyz "
        val start = 11
        val before = 3
        val count = 0

        val mentionStartPosition = 7
        val mention = "@xyz"

        val result = mention.updateMentionWhenTextChanged(
            text = text,
            start = start,
            before = before,
            count = count,
            mentionStart = mentionStartPosition
        )

        val expected = "@xyz"

        assertEquals(expected = expected, actual = result)
    }

    //endregion
}