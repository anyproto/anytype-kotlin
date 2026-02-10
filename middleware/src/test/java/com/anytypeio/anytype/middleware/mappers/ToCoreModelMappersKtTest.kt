package com.anytypeio.anytype.middleware.mappers

import anytype.model.Range
import com.anytypeio.anytype.core_models.Block
import org.junit.Assert.assertEquals
import org.junit.Test

class ToCoreModelMappersKtTest {

    @Test
    fun `should set param as null when param_ is empty`() {

        val mark = MBMark(
            type = MBMarkType.Mention,
            range = Range(from = 0, to = 10),
            param_ = ""
        )

        val result = mark.toCoreModels()
        val expected = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should set param as string when param_ is empty`() {

        val param = "anytype.io"

        val mark = MBMark(
            type = MBMarkType.Mention,
            range = Range(from = 0, to = 10),
            param_ = param
        )

        val result = mark.toCoreModels()
        val expected = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        assertEquals(expected, result)
    }

    // region DROID-4327: Toggle Headers Display Support

    @Test
    fun `should map ToggleHeader1 to H1 for backward compatibility`() {
        val result = MBTextStyle.ToggleHeader1.toCoreModels()
        assertEquals(Block.Content.Text.Style.H1, result)
    }

    @Test
    fun `should map ToggleHeader2 to H2 for backward compatibility`() {
        val result = MBTextStyle.ToggleHeader2.toCoreModels()
        assertEquals(Block.Content.Text.Style.H2, result)
    }

    @Test
    fun `should map ToggleHeader3 to H3 for backward compatibility`() {
        val result = MBTextStyle.ToggleHeader3.toCoreModels()
        assertEquals(Block.Content.Text.Style.H3, result)
    }

    // endregion
}