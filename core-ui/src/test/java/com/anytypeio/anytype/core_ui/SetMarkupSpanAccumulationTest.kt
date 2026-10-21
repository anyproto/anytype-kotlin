package com.anytypeio.anytype.core_ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.common.setMarkup
import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * Regression test for span accumulation in [setMarkup] (production ANR in
 * SpannableStringBuilder.countSpans during text selection).
 *
 * When a recycled text block is re-bound, setMarkup() removes only Span-typed
 * spans before copying the freshly built ones. Any span applied by the markup
 * pipeline that does not implement Span leaks and piles up with every rebind,
 * making the span set grow without bound.
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class SetMarkupSpanAccumulationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `should not accumulate spans on repeated setMarkup calls`() {
        val markup = object : Markup {
            override val body: String = "Hello @user, check this out"
            override var marks: List<Markup.Mark> = listOf(
                Markup.Mark.Bold(from = 0, to = 5),
                Markup.Mark.Mention.Base(from = 6, to = 11, param = "obj-id", isArchived = false)
            )
        }

        val editable = SpannableStringBuilder(markup.body)

        fun bind() = editable.setMarkup(
            markup = markup,
            context = context,
            click = {},
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 12f,
            textColor = Color.BLACK,
            underlineHeight = 1f
        )

        bind()

        val spanCountAfterFirstBind = editable.getSpans(0, editable.length, Any::class.java).size
        val clickableCountAfterFirstBind =
            editable.getSpans(0, editable.length, ClickableSpan::class.java).size

        repeat(49) { bind() }

        val spanCountAfterFiftyBinds = editable.getSpans(0, editable.length, Any::class.java).size
        val clickableCountAfterFiftyBinds =
            editable.getSpans(0, editable.length, ClickableSpan::class.java).size

        assertEquals(
            expected = clickableCountAfterFirstBind,
            actual = clickableCountAfterFiftyBinds,
            message = "ClickableSpan count must not grow with rebinds"
        )
        assertEquals(
            expected = spanCountAfterFirstBind,
            actual = spanCountAfterFiftyBinds,
            message = "Total span count must not grow with rebinds"
        )
    }
}
