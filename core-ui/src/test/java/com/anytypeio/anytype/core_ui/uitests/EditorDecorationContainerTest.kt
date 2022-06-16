package com.anytypeio.anytype.core_ui.uitests

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecorationWidget
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = ["androidx.loader.content"]
)
class EditorDecorationContainerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
    }

    @Test
    fun `container should be empty - when layout is created`() {
        val layout = EditorDecorationContainer(context)
        assertEquals(
            expected = 0,
            actual = layout.childCount
        )
    }

    /**
     * Block with background (rendered block)
     */
    @Test
    fun `should create one background view without any indentation`() {

        val layout = EditorDecorationContainer(context)

        val blue = ThemeColor.BLUE

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = blue
                )
            )
        )

        assertEquals(
            expected = 1,
            actual = layout.childCount
        )

        val child = layout.getChildAt(0)

        assertIs<DecorationWidget.Background>(child)

        val color = child.background as ColorDrawable

        assertEquals(
            expected = context.resources.getColor(R.color.palette_very_light_blue, null),
            actual = color.color
        )

        assertEquals(
            expected = 0,
            actual = child.marginStart
        )
    }

    /**
     * ...Block with background (rendered block)
     */
    @Test
    fun `should create one background view with one indentation`() {

        val layout = EditorDecorationContainer(context)

        val bg = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg
                )
            )
        )

        assertEquals(
            expected = 1,
            actual = layout.childCount
        )

        val child = layout.getChildAt(0)

        assertIs<DecorationWidget.Background>(child)

        val color = child.background as ColorDrawable

        assertEquals(
            expected = context.resources.veryLight(bg, 0),
            actual = color.color
        )

        assertEquals(
            expected = 0,
            actual = child.marginStart
        )
    }

    /**
     *   Block with background
     *   ...Child block with background (rendered block)
     */
    @Test
    fun `should create two background views with indents 0 and 1`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1
                ),
                BlockView.Decoration(
                    background = bg2
                )
            )
        )

        assertEquals(
            expected = 2,
            actual = layout.childCount
        )

        val child1 = layout.getChildAt(0)
        val child2 = layout.getChildAt(1)

        assertIs<DecorationWidget.Background>(child1)
        assertIs<DecorationWidget.Background>(child2)

        val child1Background = child1.background as ColorDrawable
        val child2Background = child2.background as ColorDrawable

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = child1Background.color
        )

        assertEquals(
            expected = 0,
            actual = child1.marginStart
        )

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = child2Background.color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = child2.marginStart
        )
    }


    /**
     *   Quote block (A) without background
     *   ...Child of A (rendered block) with background and extra-added space below
     */
    @Test
    fun `should create one background view and one part of the quote line - when quote without background contains one simple block with background`() {

        val layout = EditorDecorationContainer(context)

        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    style = BlockView.Decoration.Style.Highlight.End,
                ),
                BlockView.Decoration(
                    background = bg2
                )
            )
        )

        assertEquals(
            expected = 2,
            actual = layout.childCount
        )

        val rectWithVerticalQuoteLine = layout.getChildAt(0)
        val backgroundView = layout.getChildAt(1)

        assertIs<DecorationWidget.Highlight>(rectWithVerticalQuoteLine)
        assertIs<DecorationWidget.Background>(backgroundView)

        val child2Background = backgroundView.background as ColorDrawable

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = rectWithVerticalQuoteLine.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_highlight_content_margin_top).toInt(),
            actual = rectWithVerticalQuoteLine.marginBottom
        )

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = child2Background.color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt(),
            actual = backgroundView.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_highlight_content_margin_top).toInt(),
            actual = backgroundView.marginBottom
        )
    }

    /**
     *   Quote block (A) with default background
     *   ...Child of A (rendered block) with background and extra-added space below
     */
    @Test
    fun `should create one background view and one part of the quote line - when quote with default background contains one simple block with background`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.DEFAULT
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    style = BlockView.Decoration.Style.Highlight.End,
                    background = bg1
                ),
                BlockView.Decoration(
                    background = bg2
                )
            )
        )

        assertEquals(
            expected = 2,
            actual = layout.childCount
        )

        val rectWithVerticalQuoteLine = layout.getChildAt(0)
        val backgroundView = layout.getChildAt(1)

        assertIs<DecorationWidget.Highlight>(rectWithVerticalQuoteLine)
        assertIs<DecorationWidget.Background>(backgroundView)

        val child2Background = backgroundView.background as ColorDrawable

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = rectWithVerticalQuoteLine.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_highlight_content_margin_top).toInt(),
            actual = rectWithVerticalQuoteLine.marginBottom
        )

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = child2Background.color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt(),
            actual = backgroundView.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_highlight_content_margin_top).toInt(),
            actual = backgroundView.marginBottom
        )
    }

    /**
     *   Quote block (A)
     *   ...Child of A (rendered block) with background and extra-added space below
     */
    @Test
    fun `should create one background view and one part of the quote line - when quote with background contains one simple block without background`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    style = BlockView.Decoration.Style.Highlight.End,
                    background = bg1
                ),
                BlockView.Decoration()
            )
        )

        assertEquals(
            expected = 2,
            actual = layout.childCount
        )

        val backgroundView = layout.getChildAt(0)
        val rectWithVerticalQuoteLine = layout.getChildAt(1)

        assertIs<DecorationWidget.Highlight>(rectWithVerticalQuoteLine)
        assertIs<DecorationWidget.Background>(backgroundView)

        val child2Background = backgroundView.background as ColorDrawable

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = rectWithVerticalQuoteLine.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_highlight_content_margin_top).toInt(),
            actual = rectWithVerticalQuoteLine.marginBottom
        )

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = child2Background.color
        )

        assertEquals(
            expected = 0,
            actual = backgroundView.marginStart
        )

        assertEquals(
            expected = 0,
            actual = backgroundView.marginBottom
        )
    }

    /**
     *   Quote block (A)
     *   ...Child of A (B)
     *   ......Child of B (C, Rendered block)
     */
    @Test
    fun `should create two background views - when one quote block with contains one child, which contains one child`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg3 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1,
                    style = BlockView.Decoration.Style.Highlight.End
                ),
                BlockView.Decoration(
                    background = bg2
                ),
                BlockView.Decoration(
                    background = bg3
                )
            )
        )

        assertEquals(
            expected = 4,
            actual = layout.childCount
        )

        val view1 = layout.getChildAt(0) // background of A
        val view2 = layout.getChildAt(1) // line of A
        val view3 = layout.getChildAt(2) // background of B
        val view4 = layout.getChildAt(3) // background of C

        assertIs<DecorationWidget.Background>(view1)
        assertIs<DecorationWidget.Highlight>(view2)
        assertIs<DecorationWidget.Background>(view3)
        assertIs<DecorationWidget.Background>(view4)

        assertEquals(
            expected = 0,
            actual = view1.marginStart
        )

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = (view1.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view2.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt(),
            actual = view3.marginStart
        )

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = (view3.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48)
                .toInt() + context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view4.marginStart
        )

        assertEquals(
            expected = context.resources.veryLight(bg3, 0),
            actual = (view4.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_highlight_content_margin_top).toInt(),
            actual = view4.marginBottom
        )
    }

    /**
     *   Quote block (A) with background
     *   ...First child of A (B) with its own background
     *   ...Quote block, second child of A (C) with its own background
     *   ......Child of C (D, rendered block)
     */
    @Test
    fun `should render background and quote lines for a block contained in a quote, which itself is contained inside another quote block`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg3 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1,
                    style = BlockView.Decoration.Style.Highlight.End
                ),
                BlockView.Decoration(
                    background = bg2,
                    style = BlockView.Decoration.Style.Highlight.End
                ),
                BlockView.Decoration(
                    background = bg3
                )
            )
        )

        assertEquals(
            expected = 5,
            actual = layout.childCount
        )

        val view1 = layout.getChildAt(0) // background of A
        val view2 = layout.getChildAt(1) // line of A
        val view3 = layout.getChildAt(2) // background of C
        val view4 = layout.getChildAt(3) // line of C
        val view5 = layout.getChildAt(4) // background of D

        assertIs<DecorationWidget.Background>(view1)
        assertIs<DecorationWidget.Highlight>(view2)
        assertIs<DecorationWidget.Background>(view3)
        assertIs<DecorationWidget.Highlight>(view4)
        assertIs<DecorationWidget.Background>(view5)

        // First background [A]

        assertEquals(
            expected = 0,
            actual = view1.marginStart
        )

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = (view1.background as ColorDrawable).color
        )

        // First quote line [A]

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view2.marginStart
        )

        // Background of [C]

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt(),
            actual = view3.marginStart
        )

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = (view3.background as ColorDrawable).color
        )

        // Quote line of [C]

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48)
                .toInt() + context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view4.marginStart
        )

        // Background of [D]

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt() * 2,
            actual = view5.marginStart
        )

        assertEquals(
            expected = context.resources.veryLight(bg3, 0),
            actual = (view5.background as ColorDrawable).color
        )
    }

    /**
     *   A - Quote block, with its own background
     *   ...B - First child of A, with its own background
     *      ...C - Quote block, first child of B, with its own background
     *         ...D - Child of C, rendered block.
     */
    @Test
    fun `should render backgrounds and quote lines for a block contained in a quote, which itself contained inside a quote block, which contained inside a block contained inside a quote block`() {
        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg3 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg4 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1,
                    style = BlockView.Decoration.Style.Highlight.End
                ),
                BlockView.Decoration(
                    background = bg2
                ),
                BlockView.Decoration(
                    background = bg3,
                    style = BlockView.Decoration.Style.Highlight.End
                ),
                BlockView.Decoration(
                    background = bg4
                )
            )
        )

        assertEquals(
            expected = 6,
            actual = layout.childCount
        )

        val view1 = layout.getChildAt(0) // background of A
        val view2 = layout.getChildAt(1) // line of A
        val view3 = layout.getChildAt(2) // background of B
        val view4 = layout.getChildAt(3) // background of C
        val view5 = layout.getChildAt(4) // line of C
        val view6 = layout.getChildAt(5) // background of D

        assertIs<DecorationWidget.Background>(view1)
        assertIs<DecorationWidget.Highlight>(view2)
        assertIs<DecorationWidget.Background>(view3)
        assertIs<DecorationWidget.Background>(view4)
        assertIs<DecorationWidget.Highlight>(view5)
        assertIs<DecorationWidget.Background>(view6)

        // Background and start offset of [A]

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = (view1.background as ColorDrawable).color
        )

        assertEquals(
            expected = 0,
            actual = view1.marginStart
        )

        // First quote line [A]

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view2.marginStart
        )

        // Background and start offset of [B]

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = (view3.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt(),
            actual = view3.marginStart
        )

        // Background and start offset of [C]

        assertEquals(
            expected = context.resources.veryLight(bg3, 0),
            actual = (view4.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt() + context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view4.marginStart
        )

        // Quote line of [C]

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48)
                .toInt() + (context.resources.getDimension(R.dimen.default_indent).toInt() * 2),
            actual = view5.marginStart
        )

        // Background and start offset of [D]

        assertEquals(
            expected = context.resources.veryLight(bg4, 0),
            actual = (view6.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt() + context.resources.getDimension(R.dimen.default_indent).toInt() + context.resources.getDimension(R.dimen.dp_48).toInt(),
            actual = view6.marginStart
        )
    }

    /**
     *   A - Quote block, with its own background
     *   ...B - First child of A, with its own background
     *   ...C - Second  child of A, without background
     *         ...D - Child of C, with its own background, rendered block.
     */
    @Test
    fun `should render two backgrounds and one quote line`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1,
                    style = BlockView.Decoration.Style.Highlight.End
                ),
                BlockView.Decoration(),
                BlockView.Decoration(
                    background = bg2
                )
            )
        )

        assertEquals(
            expected = 3,
            actual = layout.childCount
        )

        val view1 = layout.getChildAt(0) // background of A
        val view2 = layout.getChildAt(1) // line of A
        val view3 = layout.getChildAt(2) // background of B

        assertIs<DecorationWidget.Background>(view1)
        assertIs<DecorationWidget.Highlight>(view2)
        assertIs<DecorationWidget.Background>(view3)

        // Background and start offset of [A]

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = (view1.background as ColorDrawable).color
        )

        assertEquals(
            expected = 0,
            actual = view1.marginStart
        )

        // First quote line [A]

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view2.marginStart
        )

        // Background and start offset of [D]

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = (view3.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.dp_48).toInt() + context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view3.marginStart
        )
    }

    /**
     *   A - Simple text block with background
     *   ...B - H2-block with its own background.
     *   H2 block should have extra space above and below.
     */
    @Test
    fun `paragraph with background containing h2-block with background`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1,
                    style = BlockView.Decoration.Style.None
                ),
                BlockView.Decoration(
                    background = bg2,
                    style = BlockView.Decoration.Style.Header.H2
                ),
            )
        )

        assertEquals(
            expected = 2,
            actual = layout.childCount
        )

        val view1 = layout.getChildAt(0) // background of A
        val view2 = layout.getChildAt(1) // background of B

        assertIs<DecorationWidget.Background>(view1)
        assertIs<DecorationWidget.Background>(view2)

        // Background of A and its offsets

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = (view1.background as ColorDrawable).color
        )

        assertEquals(
            expected = 0,
            actual = view1.marginStart
        )

        assertEquals(
            expected = 0,
            actual = view1.marginBottom
        )

        assertEquals(
            expected = 0,
            actual = view1.marginTop
        )

        assertEquals(
            expected = layout.height,
            actual = view1.height
        )

        // Background of B and its offsets

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = (view2.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view2.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_header_two_extra_space_top).toInt(),
            actual = view2.marginTop
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_header_two_extra_space_bottom).toInt(),
            actual = view2.marginBottom
        )
    }

    /**
     *   A - Simple text block with background
     *   ...B - H3-block with its own background.
     *   H3 block should have extra space above and below.
     */
    @Test
    fun `paragraph with background containing h3-block with background`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1,
                    style = BlockView.Decoration.Style.None
                ),
                BlockView.Decoration(
                    background = bg2,
                    style = BlockView.Decoration.Style.Header.H3
                ),
            )
        )

        assertEquals(
            expected = 2,
            actual = layout.childCount
        )

        val view1 = layout.getChildAt(0) // background of A
        val view2 = layout.getChildAt(1) // background of B

        assertIs<DecorationWidget.Background>(view1)
        assertIs<DecorationWidget.Background>(view2)

        // Background of A and its offsets

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = (view1.background as ColorDrawable).color
        )

        assertEquals(
            expected = 0,
            actual = view1.marginStart
        )

        assertEquals(
            expected = 0,
            actual = view1.marginBottom
        )

        assertEquals(
            expected = 0,
            actual = view1.marginTop
        )

        assertEquals(
            expected = layout.height,
            actual = view1.height
        )

        // Background of B and its offsets

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = (view2.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view2.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_header_three_extra_space_top).toInt(),
            actual = view2.marginTop
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_header_three_extra_space_bottom).toInt(),
            actual = view2.marginBottom
        )
    }

    /**
     *   A - Simple text block with background
     *   ...B - H3-block with its own background.
     *   H3 block should have extra space above and below.
     */
    @Test
    fun `paragraph with background containing h1-block with background`() {

        val layout = EditorDecorationContainer(context)

        val bg1 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        val bg2 = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()

        layout.decorate(
            decorations = listOf(
                BlockView.Decoration(
                    background = bg1,
                    style = BlockView.Decoration.Style.None
                ),
                BlockView.Decoration(
                    background = bg2,
                    style = BlockView.Decoration.Style.Header.H1
                ),
            )
        )

        assertEquals(
            expected = 2,
            actual = layout.childCount
        )

        val view1 = layout.getChildAt(0) // background of A
        val view2 = layout.getChildAt(1) // background of B

        assertIs<DecorationWidget.Background>(view1)
        assertIs<DecorationWidget.Background>(view2)

        // Background of A and its offsets

        assertEquals(
            expected = context.resources.veryLight(bg1, 0),
            actual = (view1.background as ColorDrawable).color
        )

        assertEquals(
            expected = 0,
            actual = view1.marginStart
        )

        assertEquals(
            expected = 0,
            actual = view1.marginBottom
        )

        assertEquals(
            expected = 0,
            actual = view1.marginTop
        )

        assertEquals(
            expected = layout.height,
            actual = view1.height
        )

        // Background of B and its offsets

        assertEquals(
            expected = context.resources.veryLight(bg2, 0),
            actual = (view2.background as ColorDrawable).color
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_indent).toInt(),
            actual = view2.marginStart
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_header_one_extra_space_top).toInt(),
            actual = view2.marginTop
        )

        assertEquals(
            expected = context.resources.getDimension(R.dimen.default_header_one_extra_space_bottom).toInt(),
            actual = view2.marginBottom
        )
    }
}