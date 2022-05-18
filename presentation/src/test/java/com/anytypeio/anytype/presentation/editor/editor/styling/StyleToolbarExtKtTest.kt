package com.anytypeio.anytype.presentation.editor.editor.styling

import android.os.Build
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class StyleToolbarExtKtTest {

    @Test
    fun `should return style text state with nullable selected style`() {

        val child = MockDataFactory.randomUuid()

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf()
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.NUMBERED,
                text = MockDataFactory.randomString(),
                marks = listOf()
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf()
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3)
                .map { it.content.asText() }
                .getStyleTextToolbarState()

        val expected = StyleToolbarState.Text(
            textStyle = null
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style text state with bullet selected style`() {

        val child = MockDataFactory.randomUuid()

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf()
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf()
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf()
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3)
                .map { it.content.asText() }
                .getStyleTextToolbarState()

        val expected = StyleToolbarState.Text(
            textStyle = TextStyle.BULLET
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style background state with lime background`() {

        val child = MockDataFactory.randomUuid()

        val backgroundTeal = ThemeColor.TEAL.code

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf()
            ),
            backgroundColor = backgroundTeal,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(),
            backgroundColor = backgroundTeal,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Divider(
                style = Block.Content.Divider.Style.DOTS
            ),
            backgroundColor = backgroundTeal,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3).getStyleBackgroundToolbarState()

        val expected = StyleToolbarState.Background(
            background = backgroundTeal
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style color background state with lime background and red text color`() {

        val child = MockDataFactory.randomUuid()

        val backgroundLime = ThemeColor.TEAL.code
        val textColorRed = ThemeColor.RED.code

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.NUMBERED,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3).getStyleColorBackgroundToolbarState()

        val expected = StyleToolbarState.ColorBackground(
            background = backgroundLime,
            color = textColorRed
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style color background state with null background and null text color when colors are different`() {

        val child = MockDataFactory.randomUuid()

        val backgroundLime = ThemeColor.TEAL.code
        val textColorRed = ThemeColor.RED.code

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.NUMBERED,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = ThemeColor.ICE.code
            ),
            backgroundColor = ThemeColor.LIME.code,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3).getStyleColorBackgroundToolbarState()

        val expected = StyleToolbarState.ColorBackground(
            background = null,
            color = null
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style color background state with default background and default text color when colors are nulls`() {

        val child = MockDataFactory.randomUuid()

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = null
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = null
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.NUMBERED,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = null
            ),
            backgroundColor = null,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3).getStyleColorBackgroundToolbarState()

        val expected = StyleToolbarState.ColorBackground(
            background = ThemeColor.DEFAULT.code,
            color = ThemeColor.DEFAULT.code
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style color background state with null background and null text color when one block is not text`() {

        val child = MockDataFactory.randomUuid()

        val backgroundLime = ThemeColor.TEAL.code
        val textColorRed = ThemeColor.RED.code

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(),
            backgroundColor = ThemeColor.LIME.code,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3).getStyleColorBackgroundToolbarState()

        val expected = StyleToolbarState.ColorBackground.empty()

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style other state with bold and without align support`() {

        val child = MockDataFactory.randomUuid()

        val backgroundLime = ThemeColor.TEAL.code
        val textColorRed = ThemeColor.RED.code

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3).map { it.content.asText() }.getStyleOtherToolbarState()

        val expected = StyleToolbarState.Other(
            isSupportBold = true,
            isSupportItalic = true,
            isSupportStrikethrough = true,
            isSupportCode = true,
            isSupportLinked = true,
            isSupportAlignEnd = false,
            isSupportAlignCenter = false,
            isSupportAlignStart = false
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style other state without bold and start end align support`() {

        val child = MockDataFactory.randomUuid()

        val backgroundLime = ThemeColor.TEAL.code
        val textColorRed = ThemeColor.RED.code

        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.QUOTE,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given3 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.H3,
                text = MockDataFactory.randomString(),
                marks = listOf(),
                color = textColorRed
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val result =
            listOf(given1, given2, given3).map { it.content.asText() }.getStyleOtherToolbarState()

        val expected = StyleToolbarState.Other(
            isSupportBold = false,
            isSupportItalic = true,
            isSupportStrikethrough = true,
            isSupportCode = true,
            isSupportLinked = true,
            isSupportAlignEnd = true,
            isSupportAlignCenter = false,
            isSupportAlignStart = true
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `should return style other state with bold, italic, code and align center selected states`() {

        val child = MockDataFactory.randomUuid()

        val backgroundLime = ThemeColor.TEAL.code
        val textColorRed = ThemeColor.RED.code

        val given1Text = MockDataFactory.randomString()
        val given1 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = given1Text,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, given1Text.length),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(0, given1Text.length),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(0, given1Text.length),
                        type = Block.Content.Text.Mark.Type.KEYBOARD
                    )
                ),
                color = textColorRed,
                align = Block.Align.AlignCenter
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val given2Text = MockDataFactory.randomString()
        val given2 = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                text = given2Text,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, given2Text.length),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(0, given2Text.length),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(0, given2Text.length),
                        type = Block.Content.Text.Mark.Type.KEYBOARD
                    )
                ),
                color = textColorRed,
                align = Block.Align.AlignCenter
            ),
            backgroundColor = backgroundLime,
            children = emptyList()
        )

        val result =
            listOf(given1, given2).map { it.content.asText() }.getStyleOtherToolbarState()

        val expected = StyleToolbarState.Other(
            isSupportBold = true,
            isSupportItalic = true,
            isSupportStrikethrough = true,
            isSupportCode = true,
            isSupportLinked = true,
            isSupportAlignEnd = true,
            isSupportAlignCenter = true,
            isSupportAlignStart = true,
            isAlignCenterSelected = true,
            isBoldSelected = true,
            isItalicSelected = true,
            isCodeSelected = true
        )

        Assert.assertEquals(expected, result)
    }
}