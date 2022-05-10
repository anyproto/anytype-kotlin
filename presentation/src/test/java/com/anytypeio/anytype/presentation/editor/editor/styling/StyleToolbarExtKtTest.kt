package com.anytypeio.anytype.presentation.editor.editor.styling

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Assert
import org.junit.Test

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
}